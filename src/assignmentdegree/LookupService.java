package assignmentdegree;

import java.util.ArrayList;

public class LookupService {
    public static User getUserByID(String userID) {
        for (Customer c : DataStore.allCustomers) {
            if (c.getUserID().equals(userID)) {
                return c;
            }
        }
        
        for (CounterStaff cs: DataStore.allCounterStaff) {
            if (cs.getUserID().equals(userID)) {
                return cs;
            }
        }
        
        for (Technician t : DataStore.allTechnicians) {
            if (t.getUserID().equals(userID)) {
                return t;
            }
        }
        
        for (Manager m : DataStore.allManagers) {
            if (m.getUserID().equals(userID)) {
                return m;
            }
        }
        return null;
    }

    public static User getUserByName(String name, String role) {
        switch (role) {
            case "Customer":
                for (Customer c : DataStore.allCustomers) {
                    if (c.getName().equals(name)) {
                        return c;
                    }
                }
                break;

            case "Counter Staff":
                for (CounterStaff cs : DataStore.allCounterStaff) {
                    if (cs.getName().equals(name)) {
                        return cs;
                    }
                }
                break;

            case "Technician":
                for (Technician t : DataStore.allTechnicians) {
                    if (t.getName().equals(name)) {
                        return t;
                    }
                }
                break;

            case "Manager":
                for (Manager m : DataStore.allManagers) {
                    if (m.getName().equals(name)) {
                        return m;
                    }
                }
                break;
        }
        return null;
    }

    public static Manager getManagerByID(String userID) {
        for (Manager m : DataStore.allManagers) {
            if (m.getUserID().equals(userID)) {
                return m;
            }
        }
        return null;
    }

    public static Customer getCustomerByID(String userID) {
        for (Customer c : DataStore.allCustomers) {
            if (c.getUserID().equals(userID)) {
                return c;
            }
        }
        return null;
    }

    public static Technician getTechnicianByID(String userID) {
        for (Technician t : DataStore.allTechnicians) {
            if (t.getUserID().equals(userID)) {
                return t;
            }
        }
        return null;
    }

    public static CounterStaff getCounterStaffByID(String userID) {
        for (CounterStaff cs : DataStore.allCounterStaff) {
            if (cs.getUserID().equals(userID)) {
                return cs;
            }
        }
        return null;
    }

    public static String getUserNameByID(String userID) {
        User u = getUserByID(userID);
        if (u != null) {
            return u.getName();
        } else {
            return userID;
        }
    }

    public static String getUserRoleByID(String userID) {
        if (userID.startsWith("CS")) {
            return "Counter Staff";
        }

        if (userID.startsWith("C")) {
            return "Customer";
        }

        if (userID.startsWith("T")) {
            return "Technician";
        }

        if (userID.startsWith("M")) {
            return "Manager";
        }

        return "Unknown";
    }

    public static String getCustomerNameByID(String customerID) {
        for (Customer c : DataStore.allCustomers) {
            if (c.getUserID().equals(customerID)) {
                return c.getName();
            }
        }
        return customerID;
    }

    public static String getStaffNameByID(String staffID) {
        for (CounterStaff cs : DataStore.allCounterStaff) {
            if (cs.getUserID().equals(staffID)) {
                return cs.getName();
            }
        }
        return staffID;
    }

    public static String getTechnicianNameByID(String techID) {
        for (Technician t : DataStore.allTechnicians) {
            if (t.getUserID().equals(techID)) {
                return t.getName();
            }
        }
        return techID;
    }

    public static String getTechnicianNames(ArrayList<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return "N/A";
        }
        ArrayList<String> names = new ArrayList<>();
        for (String id : ids) {
            names.add(getTechnicianNameByID(id));
        }
        return String.join(", ", names);
    }

    public static String searchUserPassword(String name, String email, String contact, String role) {
        switch (role) {
            case "Customer":
                for (Customer c : DataStore.allCustomers) {
                    if (c.getName().equals(name) && c.getEmailAddress().equals(email)
                            && c.getContactNumber().equals(contact)) {
                        return c.getPassword();
                    }
                }
                break;
            case "Counter Staff":
                for (CounterStaff cs : DataStore.allCounterStaff) {
                    if (cs.getName().equals(name) && cs.getEmailAddress().equals(email)
                            && cs.getContactNumber().equals(contact)) {
                        return cs.getPassword();
                    }
                }
                break;
            case "Technician":
                for (Technician t : DataStore.allTechnicians) {
                    if (t.getName().equals(name) && t.getEmailAddress().equals(email)
                            && t.getContactNumber().equals(contact)) {
                        return t.getPassword();
                    }
                }
                break;
            case "Manager":
                for (Manager m : DataStore.allManagers) {
                    if (m.getName().equals(name) && m.getEmailAddress().equals(email)
                            && m.getContactNumber().equals(contact)) {
                        return m.getPassword();
                    }
                }
                break;
        }
        return null;
    }

    public static Service getServiceByID(String serviceID) {
        for (Service s : DataStore.allServices) {
            if (s.getServiceID().equals(serviceID)) {
                return s;
            }
        }
        return null;
    }

    public static String getServiceNameByID(String serviceID) {
        Service s = getServiceByID(serviceID);
        if (s != null) {
            return s.getServiceName() + " (" + s.getServiceType() + ")";
        } else {
            return serviceID;
        }
    }

    public static double getServicePriceByID(String serviceID) {
        Service s = getServiceByID(serviceID);
        if (s != null) {
            return s.getPrice();
        } else {
            return 0.0;
        }
    }

    public static Appointment getAppointmentByID(String appointmentID) {
        for (Appointment a : DataStore.allAppointments) {
            if (a.getAppointmentID().equals(appointmentID)) {
                return a;
            }
        }
        return null;
    }

    public static Payment getPaymentByID(String paymentID) {
        for (Payment p : DataStore.allPayments) {
            if (p.getPaymentID().equals(paymentID)) {
                return p;
            }
        }
        return null;
    }

    public static Payment getPaymentByAppointmentID(String appointmentID) {
        for (Payment p : DataStore.allPayments) {
            if (p.getAppointmentID().equals(appointmentID)) {
                return p;
            }
        }
        return null;
    }

    public static ArrayList<Comment> getCommentsByAppointmentID(String appointmentID) {
        ArrayList<Comment> result = new ArrayList<>();
        for (Comment c : DataStore.allComments) {
            if (c.getAppointmentID().equals(appointmentID)) {
                result.add(c);
            }
        }
        return result;
    }

    public static String getTechnicianFeedbackHistory(String appointmentID) {
        StringBuilder sb = new StringBuilder();
        for (Comment c : getCommentsByAppointmentID(appointmentID)) {
            if (c.getUserID().startsWith("T")) {
                sb.append("[").append(c.getDateTime()).append("] ")
                        .append(c.getUserID()).append(" : ").append(c.getCommentText()).append("\n");
            }
        }
        if (sb.length() == 0) {
            return "(No technician feedback yet)";
        } else {
            return sb.toString();
        }
    }

    public static String getCustomerCommentHistory(String appointmentID, String customerID) {
        StringBuilder sb = new StringBuilder();
        for (Comment c : getCommentsByAppointmentID(appointmentID)) {
            if (c.getUserID().equals(customerID)) {
                sb.append("[").append(c.getDateTime()).append("] ")
                        .append(c.getUserID()).append(" : ").append(c.getCommentText()).append("\n");
            }
        }
        if (sb.length() == 0) {
            return "(No comment yet)";
        } else {
            return sb.toString();
        }
    }

    public static String getCommentsForAppointment(String appointmentID, boolean technicianComments) {
        StringBuilder sb = new StringBuilder();
        for (Comment c : getCommentsByAppointmentID(appointmentID)) {
            boolean isTech = c.getUserID().startsWith("T");
            if (technicianComments == isTech) {
                sb.append("[").append(c.getDateTime()).append("] ")
                        .append(c.getUserID()).append(" : ").append(c.getCommentText()).append("\n");
            }
        }
        if (sb.length() == 0) {
            if (technicianComments) {
                return "(No feedback yet)";
            } else {
                return "(No customer comment yet)";
            }
        } else {
            return sb.toString();
        }
    }

    public static Rating getRatingByAppointmentID(String appointmentID) {
        for (Rating r : DataStore.allRatings) {
            if (r.getAppointmentID().equals(appointmentID)) {
                return r;
            }
        }
        return null;
    }

    public static String getRatingDisplay(String appointmentID) {
        Rating r = getRatingByAppointmentID(appointmentID);
        if (r == null) {
            return "(Not rated)";
        } else {
            StringBuilder stars = new StringBuilder();
            int val = r.getRatingValue();
            for (int i = 0; i < 5; i++) {
                if (i < val) {
                    stars.append("★");
                } else {
                    stars.append("☆");
                }
            }
            return stars.toString() + " (" + val + " / 5)";
        }
    }

    public static int getDaysInMonth(String month, int year) {
        switch (month) {
            case "February":
                if (isLeapYear(year)) {
                    return 29;
                } else {
                    return 28;
                }
            case "April":
            case "June":
            case "September":
            case "November":
                return 30;
            default:
                return 31;
        }
    }

    public static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }
}