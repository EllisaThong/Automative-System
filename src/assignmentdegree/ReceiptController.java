package assignmentdegree;

public class ReceiptController {
    
    public static void editReceipt(Receipt receipt, String newMethod, String newDate) {
        if ("Card".equals(newMethod) && receipt.getChange() != 0.0) {
            receipt.setChange(0.0);
        }

        receipt.setMethod(newMethod);
        receipt.setDate(newDate);
        FileHandler.writeAllFiles();
    }

    public static void deleteReceiptAndRecalculate(String receiptID) {
        Receipt target = null;
        for (Receipt r : DataStore.allReceipts) {
            if (r.getReceiptID().equals(receiptID)) {
                target = r;
                break;
            }
        }

        if (target != null) {
            String paymentID = target.getPaymentID();
            DataStore.allReceipts.remove(target);

            Payment payment = LookupService.getPaymentByID(paymentID);
            if (payment == null) {
                FileHandler.writeAllFiles();
            } else {
                double newTotalPaid = 0.0;
                for (Receipt r : DataStore.allReceipts) {
                    if (r.getPaymentID().equals(paymentID)) {
                        newTotalPaid += r.getAmountPaid();
                    }
                }
                newTotalPaid = round(newTotalPaid);

                double newBalance = round(payment.getTotalFee() - newTotalPaid);

                String newStatus;
                if (newTotalPaid == 0.0) {
                    newStatus = "Pending";
                } else if (newBalance == 0) {
                    newStatus = "Successful";
                } else {
                    newStatus = "Partial";
                }

                if (newTotalPaid == 0.0) {
                    Appointment a = LookupService.getAppointmentByID(payment.getAppointmentID());
                    if (a != null && a.getStatus().equalsIgnoreCase("Completed")) {
                        a.setStatus("Scheduled");
                    }
                    DataStore.allPayments.remove(payment);
                } else {
                    payment.setTotalPaid(newTotalPaid);
                    payment.setBalance(newBalance);
                    payment.setStatus(newStatus);

                    for (Receipt r : DataStore.allReceipts) {
                        if (r.getPaymentID().equals(paymentID)) {
                            r.setTotalPaid(newTotalPaid);
                            r.setBalance(newBalance);
                            r.setStatus(newStatus);
                        }
                    }
                }

                FileHandler.writeAllFiles();
            }
        }
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}