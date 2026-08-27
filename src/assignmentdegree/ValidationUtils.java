package assignmentdegree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JOptionPane;

public class ValidationUtils {
    public static boolean validateLoginInput(String email, String password) {
        if (!validateNotEmpty(email, password)) {
            return false;
        }
        if (!validateEmail(email)) {
            return false;
        }
        if (!validatePasswordLength(password)) {
            return false;
        }
        return true;
    }
    
    public static boolean validateRegisterInput(String name, String password, String confirmPassword, String email, String contact) {
        if (!validateNotEmpty(name, password, confirmPassword, email, contact)) {
            return false;
        }
        if (!validateName(name)) {
            return false;
        }
        if (!validatePassword(password)) {
            return false;
        }
        if (!validateConfirmPassword(password, confirmPassword)) {
            return false;
        }
        if (!validateEmail(email)) {
            return false;
        }
        if (!validateContact(contact)) {
            return false;
        }
        
        return true;
    }

    public static boolean validateNotEmpty(String... fields) {
        for (String f : fields) {
            if (f == null || f.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "All fields must be filled.");
                return false;
            }
        }
        return true;
    }
    
    public static boolean validateName(String name) {
        if (name.length() > 20) {
            JOptionPane.showMessageDialog(null, "Name cannot exceed 20 characters.");
            return false;
        }
        return true;
    }

    public static boolean validatePassword(String password) {
        if (!validatePasswordLength(password)) {
            return false;
        }
        int letters = 0;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                letters++;
            }
        }
        if (letters < 3) {
            JOptionPane.showMessageDialog(null, "Password must contain at least 3 alphabetic characters.");
            return false;
        }
        return true;
    }

    public static boolean validatePasswordLength(String password) {
        if (password.length() < 8 || password.length() > 20) {
            JOptionPane.showMessageDialog(null, "Password must be between 8 and 20 characters.");
            return false;
        }
        return true;
    }

    public static boolean validateConfirmPassword(String password, String confirm) {
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(null, "Passwords do not match.");
            return false;
        }
        return true;
    }

    public static boolean validateEmail(String email) {
        // Accept common email formats (simple + practical for coursework)
        if (email == null) return false;
        String e = email.trim();
        if (e.length() < 6 || e.length() > 50) {
            JOptionPane.showMessageDialog(null, "Email must be between 6 and 50 characters.");
            return false;
        }
        if (!e.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            JOptionPane.showMessageDialog(null, "Invalid email format (example: name@example.com).");
            return false;
        }
        return true;
    }

    public static boolean validateContact(String contact) {
        if (!contact.matches("^\\d{3}-\\d{7}$") && !contact.matches("^\\d{3}-\\d{8}$")) {
            JOptionPane.showMessageDialog(null, "Contact must be format 012-1234567 or 011-12345678.");
            return false;
        }
        return true;
    }

    public static boolean validateServiceInput(String name, String priceText) {
        if (!validateNotEmpty(name, priceText)) {
            return false;
        }
        if (!validateServiceName(name)) {
            return false;
        }
        if (!validatePrice(priceText)) {
            return false;
        }
        return true;
    }

    public static boolean validateServiceName(String name) {
        if (name.length() < 3 || name.length() > 30) {
            JOptionPane.showMessageDialog(null, "Service name must be between 3 and 30 characters.");
            return false;
        }
        return true;
    }

    public static boolean validateComment(String comment) {
        if (comment == null || comment.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Comment cannot be empty.");
            return false;
        }
        if (comment.length() < 5 || comment.length() > 50) {
            JOptionPane.showMessageDialog(null, "Comment must be between 5 and 50 characters.");
            return false;
        }
        if (comment.contains("\\")) {
            JOptionPane.showMessageDialog(null, "Backslash (\\) is not allowed.");
            return false;
        }
        return true;
    }

    public static boolean validatePrice(String priceText) {
        try {
            double price = Double.parseDouble(priceText);
            if (price < 0) {
                JOptionPane.showMessageDialog(null, "Price cannot be negative!");
                return false;
            }
            if (price > 100) {
                JOptionPane.showMessageDialog(null, "Price cannot be more than 100!");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid price! Enter a valid number.");
            return false;
        }
        return true;
    }

    public static boolean isDuplicate(String email, String contact) {
        for (Customer c : DataStore.allCustomers) {
            if (matches(c, email, contact)) {
                return true;
            }
        }

        for (CounterStaff cs : DataStore.allCounterStaff) {
            if (matches(cs, email, contact)) {
                return true;
            }
        }

        for (Technician t : DataStore.allTechnicians) {
            if (matches(t, email, contact)) {
                return true;
            }
        }

        for (Manager m : DataStore.allManagers) {
            if (matches(m, email, contact)) {
                return true;
            }
        }
        
        if (isDuplicateInWaitingList(email, contact)) {
            return true;
        }

        return false;
    }

    public static boolean isDuplicateExceptCurrent(String email, String contact, String excludeID) {
        for (Customer c : DataStore.allCustomers) {
            if (!c.getUserID().equals(excludeID) && matches(c, email, contact)) {
                return true;
            }
        }

        for (CounterStaff cs : DataStore.allCounterStaff) {
            if (!cs.getUserID().equals(excludeID) && matches(cs, email, contact)) {
                return true;
            }
        }

        for (Technician t : DataStore.allTechnicians) {
            if (!t.getUserID().equals(excludeID) && matches(t, email, contact)) {
                return true;
            }
        }

        for (Manager m : DataStore.allManagers) {
            if (!m.getUserID().equals(excludeID) && matches(m, email, contact)) {
                return true;
            }
        }
        
        if (isDuplicateInWaitingListExceptCurrent(email, contact, excludeID)) {
            return true;
        }

        return false;
    }

    private static boolean matches(User u, String email, String contact) {
        return u.getEmailAddress().equalsIgnoreCase(email) || u.getContactNumber().equals(contact);
    }

    public static boolean isDuplicateInWaitingList(String email, String contact) {
        try (BufferedReader br = new BufferedReader(new FileReader("waitingList.txt"))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

                String fileEmail = data[3];
                String fileContact = data[4];

                if (fileEmail.equalsIgnoreCase(email) || fileContact.equals(contact)) {
                    return true;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
    
    public static boolean isDuplicateInWaitingListExceptCurrent(String email, String contact, String excludeID) {
        try (BufferedReader br = new BufferedReader(new FileReader("waitingList.txt"))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

                String id = data[0];         // W001
                String fileEmail = data[3];  // email
                String fileContact = data[4];// contact

                if (!id.equals(excludeID) &&
                    (fileEmail.equalsIgnoreCase(email) || fileContact.equals(contact))) {
                    return true;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
    
    public static boolean validateAnnouncementMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Announcement message cannot be empty.");
            return false;
        }
        
        if (message.length() < 5 || message.length() > 100) {
            JOptionPane.showMessageDialog(null, "Message must be between 5 and 100 characters.");
            return false;
        }
        return true;
    }
}