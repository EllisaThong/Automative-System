package assignmentdegree;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class AppointmentController {
    public static Appointment createAppointment(String customerID, String counterStaffID, ArrayList<String> technicianIDs, String serviceID, String date, String startTime, String endTime) {
        String id = FileHandler.generateAppointmentID();
        Appointment a = new Appointment(id, customerID, counterStaffID, technicianIDs, serviceID, date, startTime, endTime, "Scheduled");
        DataStore.allAppointments.add(a);
        FileHandler.writeAllFiles();
        return a;
    }

    public static void editAppointment(Appointment a, String customerID, String counterStaffID, ArrayList<String> technicianIDs, String serviceID, String date, String startTime, String endTime) {
        a.setCustomerID(customerID);
        a.setCounterStaffID(counterStaffID);
        a.setTechnicianIDs(technicianIDs);
        a.setServiceID(serviceID);
        a.setDate(date);
        a.setStartTime(startTime);
        a.setEndTime(endTime);
        FileHandler.writeAllFiles();
    }

    public static boolean markCompleted(Appointment a) {
        if (!isDateValidForCompletion(a.getDate(), a.getEndTime())) {
            return false;
        }

        a.setStatus("Completed");

        Payment existing = LookupService.getPaymentByAppointmentID(a.getAppointmentID());
        if (existing == null) {
            double fee = LookupService.getServicePriceByID(a.getServiceID());
            String paymentID = FileHandler.generatePaymentID();
            DataStore.allPayments.add(new Payment(paymentID, a.getAppointmentID(),
                    fee, 0.0, fee, "Pending"));
        }

        FileHandler.writeAllFiles();
        return true;
    }

    private static boolean isDateValidForCompletion(String appointmentDateStr, String appointmentEndTime) {
        try {
            String[] dateParts = appointmentDateStr.split(" ");
            int day = Integer.parseInt(dateParts[0]);
            String monthName = dateParts[1];
            int year = Integer.parseInt(dateParts[2]);

            String[] timeParts = appointmentEndTime.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            LocalDate apptDate = LocalDate.of(year, getMonthIndex(monthName) + 1, day);
            LocalDateTime appointmentEndDateTime = apptDate.atTime(hour, minute);
            LocalDateTime now = LocalDateTime.now();
            return now.isAfter(appointmentEndDateTime);
            
        } catch (Exception e) {
            return true;
        }
    }
    
    private static int getMonthIndex(String monthName) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        for (int i = 0; i < months.length; i++) {
            if (months[i].equalsIgnoreCase(monthName)) {
                return i;
            }
        }
        return 0;
    }

    public static void deleteAppointment(String appointmentID) {

        // 1. Remove RECEIPTS first (depends on payment → appointment)
        DataStore.allReceipts.removeIf(r -> {
            Payment p = LookupService.getPaymentByID(r.getPaymentID());
            return p != null && p.getAppointmentID().equals(appointmentID);
        });

        // 2. Remove PAYMENTS
        DataStore.allPayments.removeIf(p -> p.getAppointmentID().equals(appointmentID));

        // 3. Remove COMMENTS
        DataStore.allComments.removeIf(c -> c.getAppointmentID().equals(appointmentID));

        // 4. Remove RATINGS
        DataStore.allRatings.removeIf(r -> r.getAppointmentID().equals(appointmentID));

        // 5. Remove APPOINTMENTS
        DataStore.allAppointments.removeIf(a -> a.getAppointmentID().equals(appointmentID));

        resequenceCommentIDs();
        RatingController.resequenceRatingIDs();
        FileHandler.writeAllFiles();
    }
    
    public static boolean hasConflict(String staffID, String date, String start, String end, String excludeID) {
        for (Appointment a : DataStore.allAppointments) {
            if (a.getAppointmentID().equals(excludeID)) continue;
            if (!a.getDate().equals(date)) continue;

            boolean isInvolved = false;
            if (a.getTechnicianIDs() != null && a.getTechnicianIDs().contains(staffID)) {
                isInvolved = true;
            }

            if (isInvolved && isTimeOverlap(start, end, a.getStartTime(), a.getEndTime())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isTimeOverlap(String s1, String e1, String s2, String e2) {
        return s1.compareTo(e2) < 0 && e1.compareTo(s2) > 0;
    }

    private static void resequenceCommentIDs() {
        int i = 1;
        for (Comment c : DataStore.allComments) {
            c.setCommentID(String.format("CMT%03d", i++));
        }
    }
}