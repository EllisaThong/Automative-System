package assignmentdegree;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PaymentController {
    
    public static class PaymentResult {
        public final Receipt receipt;
        public final Payment payment;
        public final boolean isNew;
        public final double changeGiven;
        public final double newBalance;
        public final String status;

        PaymentResult(Receipt r, Payment p, boolean isNew, double change, double bal, String status) {
            this.receipt = r;
            this.payment = p;
            this.isNew = isNew;
            this.changeGiven = change;
            this.newBalance = bal;
            this.status = status;
        }
    }

    public static PaymentResult processPayment(Appointment appointment,
                                               double tendered,
                                               boolean isCard) {
        double serviceFee = LookupService.getServicePriceByID(appointment.getServiceID());
        Payment existing = LookupService.getPaymentByAppointmentID(appointment.getAppointmentID());
        double alreadyPaid = (existing != null) ? existing.getTotalPaid() : 0.0;
        double balanceDue = round(serviceFee - alreadyPaid);

        double actualPaid = Math.min(tendered, balanceDue);
        double changeGiven = isCard ? 0.0 : round(Math.max(0, tendered - actualPaid));
        double newTotalPaid = round(alreadyPaid + actualPaid);
        double newBalance = round(Math.max(0, serviceFee - newTotalPaid));
        String method = isCard ? "Card" : "Cash";
        String date = new SimpleDateFormat("dd MMM yyyy").format(new Date());

        String paymentID;
        boolean isNew = (existing == null);
        if (isNew) {
            paymentID = FileHandler.generatePaymentID();
            existing = new Payment(paymentID, appointment.getAppointmentID(),
                    serviceFee, 0, serviceFee, "Pending");
            existing.updateBalance(actualPaid);
            DataStore.allPayments.add(existing);
        } else {
            paymentID = existing.getPaymentID();
            existing.updateBalance(actualPaid);
        }

        String receiptID = FileHandler.generateReceiptID();
        Receipt receipt = new Receipt(receiptID, paymentID,
                serviceFee, actualPaid, existing.getTotalPaid(), existing.getBalance(), changeGiven, date, method, existing.getStatus());
        DataStore.allReceipts.add(receipt);

        FileHandler.writeAllFiles();
        FileHandler.exportReceiptToFile(receipt);

        return new PaymentResult(receipt, existing, isNew, changeGiven, newBalance, existing.getStatus());
    }

    public static void deletePayment(String paymentID) {
        for (Payment p : DataStore.allPayments) {
            if (p.getPaymentID().equals(paymentID)) {
                Appointment a = LookupService.getAppointmentByID(p.getAppointmentID());
                if (a != null && a.getStatus().equalsIgnoreCase("Completed")) {
                    a.setStatus("Scheduled");
                }
                break;
            }
        }
        DataStore.allPayments.removeIf(p -> p.getPaymentID().equals(paymentID));
        DataStore.allReceipts.removeIf(r -> r.getPaymentID().equals(paymentID));
        FileHandler.writeAllFiles();
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}