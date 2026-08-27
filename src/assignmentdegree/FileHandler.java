package assignmentdegree;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class FileHandler {
    private static final String SYSTEM_LOG_FILE = "systemLogs.txt";

    private static void writeRecords(String fileName, List<? extends TextRecord> list, String errorMessage) {
        try (PrintWriter w = new PrintWriter(new FileWriter(fileName))) {
            for (TextRecord r : list) {
                w.println(r.toRecord());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, errorMessage);
        }
    }

    public static void writeSystemLog(String logMessage) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SYSTEM_LOG_FILE, true))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.write(timestamp + " - " + logMessage);
            writer.newLine();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "There are some errors writing the system log file.");
        }
    }

    public static void clearLogFile(int year, String month) {
        ArrayList<String> updatedLogs = new ArrayList<>();
        String targetMonth = String.format("%02d", SystemLogView.getMonthNumber(month));
        try (BufferedReader reader = new BufferedReader(new FileReader(SYSTEM_LOG_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lineParts = line.split(" ");
                if (lineParts.length > 0) {
                    String timestamp = lineParts[0];
                    String[] dateParts = timestamp.split("-");
                    if (dateParts.length >= 2) {
                        String logYear = dateParts[0];
                        String logMonth = dateParts[1];
                        if (!logYear.equals(String.valueOf(year)) || !logMonth.equals(targetMonth)) {
                            updatedLogs.add(line);
                        }
                    }
                }
            }
            try (FileWriter writer = new FileWriter(SYSTEM_LOG_FILE)) {
                for (String log : updatedLogs) {
                    writer.write(log + "\n");
                }
            }
            JOptionPane.showMessageDialog(null, "Logs for " + month + " " + year + " cleared.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error clearing log file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void readAllFiles() {
        DataStore.allCustomers.clear();
        DataStore.allCounterStaff.clear();
        DataStore.allTechnicians.clear();
        DataStore.allManagers.clear();
        DataStore.allServices.clear();
        DataStore.allAppointments.clear();
        DataStore.allPayments.clear();
        DataStore.allReceipts.clear();
        DataStore.allComments.clear();
        DataStore.allAnnouncements.clear();
        DataStore.allRatings.clear();

        readUsers("customers.txt", "Customer");
        readUsers("counterstaff.txt", "Counter Staff");
        readUsers("technicians.txt", "Technician");
        readUsers("managers.txt", "Manager");
        readServices();
        readAppointments();
        readPayments();
        readReceipts();
        readComments();
        readAnnouncements();
        readRatings();
    }

    public static void writeAllFiles() {
        writeUsers("customers.txt", DataStore.allCustomers);
        writeUsers("counterstaff.txt", DataStore.allCounterStaff);
        writeUsers("technicians.txt", DataStore.allTechnicians);
        writeUsers("managers.txt", DataStore.allManagers);
        writeServices();
        writeAppointments();
        writePayments();
        writeReceipts();
        writeComments();
        writeAnnouncements();
        writeRatings();
    }

    private static void readUsers(String fileName, String role) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] d = line.split(",");
                String id = d[0];
                String name = d[1];
                String pwd = d[2];
                String email = d[3];
                String contact = d[4];
                String regDate = (d.length > 5) ? d[5] : "Unknown";
                switch (role) {
                    case "Customer":
                        DataStore.allCustomers.add(new Customer(id, name, pwd, email, contact, regDate));
                        break;
                    case "Counter Staff":
                        DataStore.allCounterStaff.add(new CounterStaff(id, name, pwd, email, contact, regDate));
                        break;
                    case "Technician":
                        DataStore.allTechnicians.add(new Technician(id, name, pwd, email, contact, regDate));
                        break;
                    case "Manager":
                        DataStore.allManagers.add(new Manager(id, name, pwd, email, contact, regDate));
                        break;
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, fileName + " not found – starting empty.");
        }
    }

    private static void writeUsers(String fileName, ArrayList<? extends User> list) {
        writeRecords(fileName, list, "Error writing " + fileName);
    }
    
    public static void writeToWaitingList(String name, String password,
                                          String email, String contact, String role) {
        String waitingID = generateWaitingID();
        try (BufferedWriter w = new BufferedWriter(new FileWriter("waitingList.txt", true))) {
            w.write(waitingID + "," + name + "," + password + "," + email + "," + contact + "," + role);
            w.newLine();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error writing to waiting list.");
        }
    }

    public static void resequenceWaitingIDs() {
        File input = new File("waitingList.txt");
        File temp = new File("temp_waiting.txt");
        try (BufferedReader r = new BufferedReader(new FileReader(input));
             BufferedWriter w = new BufferedWriter(new FileWriter(temp))) {
            String line;
            int count = 1;
            while ((line = r.readLine()) != null) {
                String[] d = line.split(",");
                w.write(String.format("W%03d", count++) + "," + d[1] + "," + d[2]
                        + "," + d[3] + "," + d[4] + "," + d[5]);
                w.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error resequencing waiting list.");
        }
        input.delete();
        temp.renameTo(input);
    }

    private static void readServices() {
        try (BufferedReader reader = new BufferedReader(new FileReader("services.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] d = line.split(",");
                DataStore.allServices.add(new Service(d[0], d[1], d[2], Double.parseDouble(d[3])));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "services.txt not found – starting empty.");
        }
    }

    private static void writeServices() {
        writeRecords("services.txt", DataStore.allServices, "Error writing services.txt");
    }

    private static void readAppointments() {
        try (BufferedReader reader = new BufferedReader(new FileReader("appointments.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",(?![^\\[]*\\])");
                if (parts.length < 9) continue;
                try {
                    ArrayList<String> techIDs = new ArrayList<>();
                    String techField = parts[3].trim();
                    if (techField.startsWith("[") && techField.endsWith("]")) {
                        String content = techField.substring(1, techField.length() - 1).trim();
                        if (!content.isEmpty())
                            for (String t : content.split(","))
                                if (!t.trim().isEmpty()) techIDs.add(t.trim());
                    }
                    DataStore.allAppointments.add(new Appointment(
                            parts[0].trim(), parts[1].trim(), parts[2].trim(), techIDs,
                            parts[4].trim(), parts[5].trim(), parts[6].trim(),
                            parts[7].trim(), parts[8].trim()));
                } catch (Exception ex) {
                    System.out.println("Error parsing appointment: " + line);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "appointments.txt not found – starting empty.");
        }
    }

    private static void writeAppointments() {
        writeRecords("appointments.txt", DataStore.allAppointments, "Error writing appointments.txt");
    }

    private static void readPayments() {
        try (BufferedReader reader = new BufferedReader(new FileReader("payments.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] d = line.split(",");
                if (d.length < 6) continue;
                DataStore.allPayments.add(new Payment(
                        d[0].trim(), d[1].trim(),
                        Double.parseDouble(d[2].trim()),
                        Double.parseDouble(d[3].trim()),
                        Double.parseDouble(d[4].trim()),
                        d[5].trim()));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "payments.txt not found – starting empty.");
        }
    }

    private static void writePayments() {
        writeRecords("payments.txt", DataStore.allPayments, "Error writing payments.txt");
    }

    private static void readReceipts() {
        try (BufferedReader reader = new BufferedReader(new FileReader("receipts.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] d = line.split(",");
                if (d.length < 9) continue;
                boolean hasChange = d.length >= 10;
                double change    = hasChange ? Double.parseDouble(d[6].trim()) : 0.0;
                String date      = hasChange ? d[7].trim()  : d[6].trim();
                String method    = hasChange ? d[8].trim()  : d[7].trim();
                String status    = hasChange ? d[9].trim() : d[8].trim();
                DataStore.allReceipts.add(new Receipt(
                        d[0].trim(), d[1].trim(),
                        Double.parseDouble(d[2].trim()),
                        Double.parseDouble(d[3].trim()),
                        Double.parseDouble(d[4].trim()),
                        Double.parseDouble(d[5].trim()),
                        change, date, method, status));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "receipts.txt not found – starting empty.");
        }
    }

    private static void writeReceipts() {
        writeRecords("receipts.txt", DataStore.allReceipts, "Error writing receipts.txt");
    }

    private static void readComments() {
        try (BufferedReader reader = new BufferedReader(new FileReader("comments.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] d = line.split(",", 5);
                if (d.length < 5) continue;
                DataStore.allComments.add(new Comment(
                        d[0].trim(), d[1].trim(),
                        d[2].replace("\\n", "\n"),
                        d[3].trim(), d[4].trim()));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "comments.txt not found – starting empty.");
        }
    }

    private static void writeComments() {
        writeRecords("comments.txt", DataStore.allComments, "Error writing comments.txt");
    }

    private static void readAnnouncements() {
        try (BufferedReader reader = new BufferedReader(new FileReader("announcements.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] d = line.split(",", 7);
                if (d.length < 7) continue;
                DataStore.allAnnouncements.add(new Announcement(
                        d[0].trim(),
                        d[1].trim(),
                        d[2].trim(),
                        d[3].trim(),
                        d[4].trim(),
                        d[5].replace("\\n", "\n"),
                        d[6].trim()
                ));
            }
        } catch (IOException e) {
            // Keep announcement storage optional until first save.
        }
    }

    private static void writeAnnouncements() {
        writeRecords("announcements.txt", DataStore.allAnnouncements, "Error writing announcements.txt");
    }

    private static void readRatings() {
        try (BufferedReader reader = new BufferedReader(new FileReader("ratings.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] d = line.split(",");
                if (d.length < 5) continue;
                DataStore.allRatings.add(new Rating(
                        d[0].trim(), d[1].trim(),
                        Integer.parseInt(d[2].trim()),
                        d[3].trim(), d[4].trim()));
            }
        } catch (IOException e) {
            // Optional until first save
        }
    }

    private static void writeRatings() {
        writeRecords("ratings.txt", DataStore.allRatings, "Error writing ratings.txt");
    }

    public static String generateUserID(String role) {
        switch (role) {
            case "Customer":
                return generateNextID(DataStore.allCustomers,    "C");
            case "Counter Staff":
                return generateNextID(DataStore.allCounterStaff, "CS");
            case "Technician":
                return generateNextID(DataStore.allTechnicians,  "T");
            case "Manager":
                return generateNextID(DataStore.allManagers,     "M");
        }
        return "";
    }

    public static String generateServiceID()     {
        return generateNextID(DataStore.allServices,     "S");
    }
    
    public static String generateAppointmentID() {
        return generateNextID(DataStore.allAppointments, "A");
    }
    
    public static String generatePaymentID()     {
        return generateNextID(DataStore.allPayments,     "P");
    }
    
    public static String generateReceiptID()     {
        return generateNextID(DataStore.allReceipts,     "R");
    }
    
    public static String generateCommentID()     {
        return generateNextID(DataStore.allComments,     "CMT");
    }

    public static String generateAnnouncementID() {
        return generateNextID(DataStore.allAnnouncements, "AN");
    }

    public static String generateRatingID() {
        return generateNextID(DataStore.allRatings, "RT");
    }

    public static String generateWaitingID() {
        int count = 0;
        try (BufferedReader r = new BufferedReader(new FileReader("waitingList.txt"))) {
            while (r.readLine() != null) count++;
        } catch (Exception ignored) {}
        return String.format("W%03d", count + 1);
    }

    private static String generateNextID(List<?> list, String prefix) {
        int max = 0;
        for (Object obj : list) {
            String id = extractID(obj);
            if (id != null && id.startsWith(prefix)) {
                try {
                    int num = Integer.parseInt(id.substring(prefix.length()));
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return prefix + String.format("%03d", max + 1);
    }

    private static String extractID(Object obj) {
        if (obj instanceof Customer) {
            return ((Customer) obj).getUserID();
        }
        
        if (obj instanceof CounterStaff) {
            return ((CounterStaff) obj).getUserID();
        }
        
        if (obj instanceof Technician) {
            return ((Technician) obj).getUserID();
        }
        
        if (obj instanceof Manager) {
            return ((Manager) obj).getUserID();
        }
        
        if (obj instanceof Service) {
            return ((Service) obj).getServiceID();
        }
        
        if (obj instanceof Appointment) {
            return ((Appointment) obj).getAppointmentID();
        }
        
        if (obj instanceof Payment) {
            return ((Payment) obj).getPaymentID();
        }
        
        if (obj instanceof Receipt) {
            return ((Receipt) obj).getReceiptID();
        }
        
        if (obj instanceof Comment) {
            return ((Comment) obj).getCommentID();
        }

        if (obj instanceof Announcement) {
            return ((Announcement) obj).getAnnouncementID();
        }

        if (obj instanceof Rating) {
            return ((Rating) obj).getRatingID();
        }
        
        return null;
    }

    public static void exportReceiptToFile(Receipt r) {
        try {
            File dir  = new File("D:\\DD");
            if (!dir.exists() && !dir.mkdirs())
                throw new IOException("Cannot create directory: " + dir);

            File file = new File(dir, r.getReceiptID() + ".txt");

            Appointment a   = LookupService.getAppointmentByID(r.getAppointmentID());
            String customer = a != null ? LookupService.getCustomerNameByID(a.getCustomerID()) : "Unknown";
            String service  = a != null ? LookupService.getServiceNameByID(a.getServiceID()) : "Unknown";
            String tech     = a != null ? LookupService.getTechnicianNames(a.getTechnicianIDs()) : "Unknown";
            String staff    = a != null ? LookupService.getStaffNameByID(a.getCounterStaffID()) : "Unknown";
            String date     = a != null ? a.getDate() : "Unknown";

            try (FileWriter fw = new FileWriter(file)) {
                fw.write("========================================\n");
                fw.write("     APU AUTOMOTIVE SERVICE CENTRE\n");
                fw.write("               RECEIPT\n");
                fw.write("========================================\n\n");
                fw.write("Receipt ID     : " + r.getReceiptID() + "\n");
                fw.write("Payment ID     : " + r.getPaymentID() + "\n");
                fw.write("Payment Date   : " + r.getDate() + "\n");
                fw.write("Method         : " + r.getMethod() + "\n");
                fw.write("Status         : " + r.getStatus() + "\n");
                fw.write("\n----------------------------------------\n");
                fw.write("APPOINTMENT DETAILS\n");
                fw.write("----------------------------------------\n");
                fw.write("Appointment ID : " + r.getAppointmentID() + "\n");
                fw.write("Date           : " + date + "\n");
                fw.write("Customer       : " + customer + "\n");
                fw.write("Service        : " + service + "\n");
                fw.write("Technician(s)  : " + tech + "\n");
                fw.write("Staff          : " + staff + "\n");
                fw.write("\n----------------------------------------\n");
                fw.write("PAYMENT SUMMARY\n");
                fw.write("----------------------------------------\n");
                fw.write(String.format("Service Fee    : RM %.2f%n", r.getTotalFee()));
                fw.write(String.format("Amount Paid    : RM %.2f%n", r.getAmountPaid()));
                fw.write(String.format("Total Paid     : RM %.2f%n", r.getTotalPaid()));
                fw.write(String.format("Balance        : RM %.2f%n", r.getBalance()));
                fw.write(String.format("Change         : RM %.2f%n", r.getChange()));
                fw.write("\n========================================\n");
                fw.write("   Thank you for choosing APU-ASC!\n");
                fw.write("========================================");
            }

            if (Desktop.isDesktopSupported())
                Desktop.getDesktop().open(file);
            else
                JOptionPane.showMessageDialog(null, "Saved to: " + file.getAbsolutePath());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Export error: " + e.getMessage());
        }
    }
}