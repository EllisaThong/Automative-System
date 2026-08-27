package assignmentdegree;

import java.time.LocalDate;
import java.util.ArrayList;

public class ReportController {

    public static class DateFilter {
        public final String type;
        public final int fromDay, fromMonth, fromYear;
        public final int toDay, toMonth, toYear;

        public DateFilter(String type) {
            this.type = type;
            fromDay = 0;
            fromMonth = 0;
            fromYear = 0;
            toDay = 0;
            toMonth = 0;
            toYear = 0;
        }

        public DateFilter(int fromDay, int fromMonth, int fromYear, int toDay, int toMonth, int toYear) {
            this.type = "Custom Range";
            this.fromDay = fromDay;
            this.fromMonth = fromMonth;
            this.fromYear = fromYear;
            this.toDay = toDay;
            this.toMonth = toMonth;
            this.toYear = toYear;
        }
    }

    public static String buildReport(DateFilter filter) {
        int scheduled = 0, completed = 0;
        double totalFee = 0, totalCollected = 0, totalOutstanding = 0, totalApptIncome = 0;
        int successfulPay = 0, partialPay = 0, pendingPay = 0;
        int totalReceipts = 0;
        int mostUsedCount = 0; String mostUsedServiceID = null;
        int busiestTechCount = 0; String busiestTechID = null;
        int totalComments = 0, techFeedback = 0, custComments = 0;
        int totalRatings = 0; double sumRatings = 0;
        int[] ratingDist = new int[6]; // index 1-5

        ArrayList<Appointment> filteredAppts = new ArrayList<>();
        for (Appointment a : DataStore.allAppointments) {
            if (inFilter(a.getDate(), "dd MMMM yyyy", filter)) {
                filteredAppts.add(a);
                if (a.isCompleted()) completed++;
                else if (a.isPending()) scheduled++; // Using model behavior

                totalApptIncome += LookupService.getServicePriceByID(a.getServiceID());
            }
        }

        ArrayList<Payment> filteredPayments = new ArrayList<>();
        for (Payment p : DataStore.allPayments) {
            Appointment a = LookupService.getAppointmentByID(p.getAppointmentID());
            if (a == null || !inFilter(a.getDate(), "dd MMMM yyyy", filter)) continue;

            filteredPayments.add(p);
            totalFee += p.getTotalFee();
            totalCollected += p.getTotalPaid();
            totalOutstanding += p.getBalance();

            if (p.isFullyPaid()) successfulPay++;
            else if (p.isPartial()) partialPay++;
            else pendingPay++;
        }

        for (Technician t : DataStore.allTechnicians) {
            int count = t.getAssignedAppointmentsCount(filteredAppts);
            if (count > busiestTechCount) {
                busiestTechCount = count;
                busiestTechID = t.getUserID();
            }
        }

        // Find top customer
        double maxSpent = 0;
        String topCustomerID = null;
        for (Customer c : DataStore.allCustomers) {
            double spent = c.getTotalSpent(filteredPayments, filteredAppts);
            if (spent > maxSpent) {
                maxSpent = spent;
                topCustomerID = c.getUserID();
            }
        }
        String topCustomerName = topCustomerID == null ? "N/A" 
                : LookupService.getCustomerNameByID(topCustomerID) + " (RM " + String.format("%.2f", maxSpent) + ")";

        int custCount = 0, staffCount = 0, techCount = 0, managerCount = 0;
        for (Customer c : DataStore.allCustomers) {
            if (inFilter(c.getRegisterDate(), "yyyy-MM-dd HH:mm:ss", filter)) {
                custCount++;
            }
        }
        for (CounterStaff cs : DataStore.allCounterStaff) {
            if (inFilter(cs.getRegisterDate(), "yyyy-MM-dd HH:mm:ss", filter)) {
                staffCount++;
            }
        }
        for (Technician t : DataStore.allTechnicians) {
            if (inFilter(t.getRegisterDate(), "yyyy-MM-dd HH:mm:ss", filter)) {
                techCount++;
            }
        }
        for (Manager m : DataStore.allManagers) {
            if (inFilter(m.getRegisterDate(), "yyyy-MM-dd HH:mm:ss", filter)) {
                managerCount++;
            }
        }

        for (Comment c : DataStore.allComments) {
            if (inFilter(c.getDateTime(), "dd/MM/yyyy HH:mm", filter)) {
                totalComments++;
                String role = LookupService.getUserRoleByID(c.getUserID());
                if ("Technician".equalsIgnoreCase(role)) {
                    techFeedback++;
                } else if ("Customer".equalsIgnoreCase(role)) {
                    custComments++;
                }
            }
        }

        for (Rating r : DataStore.allRatings) {
            if (inFilter(r.getDateTime(), "yyyy-MM-dd HH:mm:ss", filter)) {
                totalRatings++;
                sumRatings += r.getRatingValue();
                if (r.getRatingValue() >= 1 && r.getRatingValue() <= 5) {
                    ratingDist[r.getRatingValue()]++;
                }
            }
        }

        for (Receipt r : DataStore.allReceipts) {
            Appointment a = LookupService.getAppointmentByID(r.getAppointmentID());
            if (a != null && filteredAppts.contains(a)) {
                totalReceipts++;
            }
        }

        for (Service s : DataStore.allServices) {
            int count = 0;
            for (Appointment a : filteredAppts) {
                if (a.getServiceID().equals(s.getServiceID())) {
                    count++;
                }
            }
            if (count > mostUsedCount) {
                mostUsedCount = count;
                mostUsedServiceID = s.getServiceID();
            }
        }

        String mostUsedName = mostUsedServiceID == null ? "N/A"
                : LookupService.getServiceNameByID(mostUsedServiceID) + " (" + mostUsedCount + " times)";
        String busiestName  = busiestTechID == null ? "N/A"
                : LookupService.getTechnicianNameByID(busiestTechID) + " (" + busiestTechCount + " appointments)";

        return "=======================================================\n"
             + "       APU AUTOMOTIVE SERVICE CENTRE - REPORTS\n"
             + "=======================================================\n\n"
             + "Report Type              : " + filter.type + "\n"
             + "Filter Period            : " + describeFilter(filter) + "\n\n"
             + "--- Appointment Summary ---\n"
             + "Scheduled                : " + scheduled  + "\n"
             + "Completed                : " + completed  + "\n\n"
             + "--- Income Summary ---\n"
             + String.format("Total Service Fee        : RM %.2f%n", totalFee)
             + String.format("Total Collected          : RM %.2f%n", totalCollected)
             + String.format("Outstanding Balance      : RM %.2f%n", totalOutstanding)
             + "Successful Payments      : " + successfulPay + "\n"
             + "Partial Payments         : " + partialPay    + "\n"
             + "Pending Payments         : " + pendingPay    + "\n\n"
             + "--- Receipt Summary ---\n"
             + "Total Receipts Issued    : " + totalReceipts + "\n\n"
             + "--- Service Popularity ---\n"
             + "Most Used Service        : " + mostUsedName  + "\n\n"
             + "--- Technician Workload ---\n"
             + "Busiest Technician       : " + busiestName   + "\n\n"
             + "--- Customer Insights ---\n"
             + "Top Spender              : " + topCustomerName + "\n\n"
             + "--- User Summary (New Registrations) ---\n"
             + "Total Registered Users   : " + (custCount + staffCount + techCount + managerCount) + "\n"
             + "Customers                : " + custCount    + "\n"
             + "Counter Staff            : " + staffCount + "\n"
             + "Technicians              : " + techCount  + "\n"
             + "Managers                 : " + managerCount     + "\n\n"
             + "--- Feedback & Comments ---\n"
             + "Total Comment Records    : " + totalComments + "\n"
             + "Technician Feedback      : " + techFeedback  + "\n"
             + "Customer Comments        : " + custComments  + "\n\n"
             + "--- Customer Ratings ---\n"
             + "Total Ratings Received   : " + totalRatings + "\n"
             + String.format("Average Rating           : %.2f / 5.00%n", (totalRatings == 0 ? 0 : sumRatings / totalRatings))
             + "Rating Distribution      : \n"
             + "   5 Stars: " + ratingDist[5] + "\n"
             + "   4 Stars: " + ratingDist[4] + "\n"
             + "   3 Stars: " + ratingDist[3] + "\n"
             + "   2 Stars: " + ratingDist[2] + "\n"
             + "   1 Star : " + ratingDist[1] + "\n\n"
             + "=======================================================\n";
    }

    private static boolean inFilter(String dateStr, String format, DateFilter f) {
        if (dateStr == null || dateStr.equals("Unknown")) {
            return false;
        }
        
        LocalDate date;
        try {
            if (format.equals("dd MMMM yyyy")) {
                String[] parts = dateStr.split(" ");
                int day = Integer.parseInt(parts[0]);
                int month = monthToInt(parts[1]) + 1;
                int year = Integer.parseInt(parts[2]);
                date = LocalDate.of(year, month, day);
            } else if (format.equals("yyyy-MM-dd HH:mm:ss")) {
                String[] parts = dateStr.split(" ")[0].split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);
                date = LocalDate.of(year, month, day);
            } else if (format.equals("dd/MM/yyyy HH:mm")) {
                String[] parts = dateStr.split(" ")[0].split("/");
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]);
                date = LocalDate.of(year, month, day);
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        LocalDate today = LocalDate.now();

        switch (f.type) {
            case "Today":
                return date.equals(today);

            case "This Month":
                return date.getMonthValue() == today.getMonthValue()
                    && date.getYear() == today.getYear();

            case "This Year":
                return date.getYear() == today.getYear();

            case "Custom Range":
                if (f.fromDay == 0 || f.toDay == 0) {
                    return true;
                }
                LocalDate from = LocalDate.of(f.fromYear, f.fromMonth, f.fromDay);
                LocalDate to = LocalDate.of(f.toYear, f.toMonth, f.toDay);
                return !date.isBefore(from) && !date.isAfter(to);

            default:
                return true;
        }
    }

    private static String describeFilter(DateFilter f) {
        switch (f.type) {
            case "Today":
                return "Today";
            case "This Month":
                return "This Month";
            case "This Year":
                return "This Year";
            case "Custom Range":
                return f.fromDay + "/" + f.fromMonth + "/" + f.fromYear + " — " + f.toDay + "/" + f.toMonth + "/" + f.toYear;
            default:
                return "All Records";
        }
    }

    private static int monthToInt(String month) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        for (int i = 0; i < months.length; i++) {
            if (months[i].equals(month)) {
                return i;
            }
        }
        return 0;
    }
}