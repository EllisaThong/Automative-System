package assignmentdegree;

import java.util.ArrayList;

public class ServiceController {
    public static void addService(String name, String type, double price) {
        String id = FileHandler.generateServiceID();
        DataStore.allServices.add(new Service(id, name, type, price));
        FileHandler.writeAllFiles();
    }

    public static void editService(Service service, String name, String type, double price) {
        service.setServiceName(name);
        service.setServiceType(type);
        service.setPrice(price);
        FileHandler.writeAllFiles();
    }

    /** Deletes service and every appointment / payment / receipt / comment linked to it. */
    public static void deleteService(String serviceID) {

        // 1. Collect affected appointment IDs
        ArrayList<String> apptIDs = new ArrayList<>();
        for (Appointment a : DataStore.allAppointments) {
            if (a.getServiceID().equals(serviceID)) {
                apptIDs.add(a.getAppointmentID());
            }
        }

        // 2. Collect payment IDs BEFORE deleting anything
        ArrayList<String> payIDs = new ArrayList<>();
        for (Payment p : DataStore.allPayments) {
            if (apptIDs.contains(p.getAppointmentID())) {
                payIDs.add(p.getPaymentID());
            }
        }

        // 3. Remove receipts FIRST (depends on payment)
        DataStore.allReceipts.removeIf(r -> payIDs.contains(r.getPaymentID()));

        // 4. Remove payments
        DataStore.allPayments.removeIf(p -> apptIDs.contains(p.getAppointmentID()));

        // 5. Remove comments (independent)
        DataStore.allComments.removeIf(c -> apptIDs.contains(c.getAppointmentID()));

        // 6. Remove ratings
        DataStore.allRatings.removeIf(r -> apptIDs.contains(r.getAppointmentID()));

        // 7. Remove appointments
        DataStore.allAppointments.removeIf(a -> apptIDs.contains(a.getAppointmentID()));

        // 8. Finally remove the service
        DataStore.allServices.removeIf(s -> s.getServiceID().equals(serviceID));

        resequenceCommentIDs();
        RatingController.resequenceRatingIDs();
        FileHandler.writeAllFiles();
    }

    private static void resequenceCommentIDs() {
        int i = 1;
        for (Comment c : DataStore.allComments) {
            c.setCommentID(String.format("CMT%03d", i++));
        }
    }
}