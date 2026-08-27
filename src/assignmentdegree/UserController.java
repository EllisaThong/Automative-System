package assignmentdegree;

import java.util.ArrayList;

public class UserController {
    
    public static User login(String email, String password, String role) {
        User user = null;
        switch (role) {
            case "Customer":
                for (Customer c : DataStore.allCustomers) {
                    if (c.getEmailAddress().equals(email)) {
                        user = c;
                        break;
                    }
                }
                break;
            case "Counter Staff":
                for (CounterStaff cs : DataStore.allCounterStaff) {
                    if (cs.getEmailAddress().equals(email)) {
                        user = cs;
                        break;
                    }
                }
                break;
            case "Technician":
                for (Technician t : DataStore.allTechnicians) {
                    if (t.getEmailAddress().equals(email)) {
                        user = t;
                        break;
                    }
                }
                break;
            case "Manager":
                for (Manager m : DataStore.allManagers) {
                    if (m.getEmailAddress().equals(email)) {
                        user = m;
                        break;
                    }
                }
                break;
        }

        if (user != null) {
            if (user.isLockedOut()) {
                throw new IllegalStateException("Account locked. Please try again in " + user.getLockoutRemainingSeconds() + " seconds.");
            }
            if (user.authenticate(password)) {
                user.resetFailedAttempts();
                return user;
            } else {
                user.incrementFailedAttempts();
                if (user.isLockedOut()) {
                    throw new IllegalStateException("Too many failed attempts. Account locked for 1 minute.");
                }
            }
        }
        return null;
    }

    public static void register(String name, String password, String email, String contact, String role) {
        FileHandler.writeToWaitingList(name, password, email, contact, role);
    }

    public static void addUser(String name, String password, String email, String contact, String role, String registerDate) {
        String newID = FileHandler.generateUserID(role);
        switch (role) {
            case "Customer":
                DataStore.allCustomers.add(new Customer(newID, name, password, email, contact, registerDate));
                break;
            case "Counter Staff":
                DataStore.allCounterStaff.add(new CounterStaff(newID, name, password, email, contact, registerDate));
                break;
            case "Technician":
                DataStore.allTechnicians.add(new Technician(newID, name, password, email, contact, registerDate));
                break;
            case "Manager":
                DataStore.allManagers.add(new Manager(newID, name, password, email, contact, registerDate));
                break;
        }
        FileHandler.writeAllFiles();
    }

    public static void updateProfile(User user, String name, String password, String email, String contact) {
        user.setName(name);
        user.setPassword(password);
        user.setEmailAddress(email);
        user.setContactNumber(contact);
        FileHandler.writeAllFiles();
    }

    public static boolean resetPassword(String name, String role, String newPassword) {
        User user = LookupService.getUserByName(name, role);
        if (user == null) {
            return false;
        }
        user.setPassword(newPassword);
        FileHandler.writeAllFiles();
        return true;
    }

    public static void deleteUser(String userID) {
        String userName = "Unknown";
        for (Customer c : DataStore.allCustomers) {
            if (c.getUserID().equals(userID)) {
                userName = c.getName();
                break;
            }
        }
        if (userName.equals("Unknown")) {
            for (CounterStaff cs : DataStore.allCounterStaff) {
                if (cs.getUserID().equals(userID)) {
                    userName = cs.getName();
                    break;
                }
            }
            for (Technician t : DataStore.allTechnicians) {
                if (t.getUserID().equals(userID)) {
                    userName = t.getName();
                    break;
                }
            }
            for (Manager m : DataStore.allManagers) {
                if (m.getUserID().equals(userID)) {
                    userName = m.getName();
                    break;
                }
            }
        }

        DataStore.allCustomers.removeIf(c -> c.getUserID().equals(userID));
        DataStore.allCounterStaff.removeIf(cs -> cs.getUserID().equals(userID));
        DataStore.allTechnicians.removeIf(t -> t.getUserID().equals(userID));
        DataStore.allManagers.removeIf(m -> m.getUserID().equals(userID));

        ArrayList<String> ownedAppts = new ArrayList<>();
        for (Appointment a : DataStore.allAppointments) {
            if (a.getCustomerID().equals(userID)) {
                ownedAppts.add(a.getAppointmentID());
            } else if (a.getCounterStaffID() != null && a.getCounterStaffID().equals(userID)) {
                a.setCounterStaffID("");
            }
        }

        for (Appointment a : DataStore.allAppointments) {
            if (a.getTechnicianIDs() != null) {
                a.getTechnicianIDs().removeIf(id -> id.equals(userID));
            }
        }

        // Appointments linked to user
        DataStore.allReceipts.removeIf(r -> {
            Payment p = LookupService.getPaymentByID(r.getPaymentID());
            return p != null && ownedAppts.contains(p.getAppointmentID());
        });

        DataStore.allPayments.removeIf(p -> ownedAppts.contains(p.getAppointmentID()));

        DataStore.allComments.removeIf(c -> ownedAppts.contains(c.getAppointmentID()));
        
        DataStore.allRatings.removeIf(r -> ownedAppts.contains(r.getAppointmentID()));

        DataStore.allAppointments.removeIf(a -> ownedAppts.contains(a.getAppointmentID()));
        
        DataStore.allComments.removeIf(c -> c.getUserID().equals(userID));

        DataStore.allRatings.removeIf(r -> r.getCustomerID().equals(userID));

        DataStore.allAnnouncements.removeIf(a ->
                userID.equals(a.getSenderID())
                || ("USER".equalsIgnoreCase(a.getTargetType()) && userID.equals(a.getTargetUserID()))
        );

        resequenceCommentIDs();
        RatingController.resequenceRatingIDs();
        FileHandler.writeAllFiles();

        FileHandler.writeSystemLog("User Deleted: " + userName + " (" + userID + ") was removed from the system.");
    }

    private static void resequenceCommentIDs() {
        int i = 1;
        for (Comment c : DataStore.allComments) {
            c.setCommentID(String.format("CMT%03d", i++));
        }
    }
}