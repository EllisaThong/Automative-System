package assignmentdegree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class OpenUpdateInfo implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == saveButton) {
            handleSave();
        } else if (e.getSource() == cancelButton) {
            frame.dispose();
            switch (role) {
                case "Customer":
                    CustomerFunction customerFunction = new CustomerFunction();
                    customerFunction.openCustomerFunction(userID, role);
                    break;
                case "Counter Staff":
                    CounterStaffFunction counterStaffFunction = new CounterStaffFunction();
                    counterStaffFunction.openCounterStaffFunction(userID, role);
                    break;
                case "Technician":
                    TechnicianFunction technicianFunction = new TechnicianFunction();
                    technicianFunction.openTechnicianFunction(userID, role);
                    break;
            }
        }
    }
    
    private JFrame frame;
    private JTextField nameField, emailField, contactField;
    private JPasswordField passwordField, confirmPasswordField;
    private JCheckBox showPassword;
    private JButton saveButton, cancelButton;
    private User user;
    private String userID, role;

    public void openUpdateInfoPage(User user, String userID, String role) {
        this.user = user;
        this.userID = userID;
        this.role = role;
        
        FileHandler.writeSystemLog("(" + role + ")" + userID + " opened the update personal info " + role + " function.");

        frame = new JFrame("Update Personal Information");
        frame.setSize(450, 500);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);

        nameField = new JTextField(user.getName());
        passwordField = new JPasswordField(user.getPassword());
        confirmPasswordField = new JPasswordField(user.getPassword());
        emailField = new JTextField(user.getEmailAddress());
        contactField = new JTextField(user.getContactNumber());

        showPassword = new JCheckBox("Show Password");
        showPassword.addActionListener(e -> {
            boolean show = showPassword.isSelected();
            passwordField.setEchoChar(show ? (char) 0 : '*');
            confirmPasswordField.setEchoChar(show ? (char) 0 : '*');
        });

        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");

        int xL = 50, xF = 170, w = 200, h = 30, y = 40, gap = 45;
        addLabel("Name:", xL, y, 120, h);
        add(nameField, xF, y, w, h);
        addLabel("Password:", xL, y += gap, 120, h);
        add(passwordField, xF, y, w, h);
        addLabel("Confirm Password:", xL, y += gap, 120, h);
        add(confirmPasswordField, xF, y, w, h);
        add(showPassword, xF, y += 30, 150, h);
        addLabel("Email:", xL, y += gap, 120, h);
        add(emailField, xF, y, w, h);
        addLabel("Contact:", xL, y += gap, 120, h);
        add(contactField, xF, y, w, h);
        addLabel("Register Date:", xL, y += gap, 120, h);
        JLabel regDateLabel = new JLabel(user.getRegisterDate());
        regDateLabel.setBounds(xF, y, w, h);
        frame.add(regDateLabel);
        add(saveButton, 100, y += 60, 100, 35);
        add(cancelButton, 220, y, 100, 35);

        saveButton.addActionListener(this);
        cancelButton.addActionListener(this);
        frame.setVisible(true);
    }

    private void handleSave() {
        String name = nameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String email = emailField.getText().trim();
        String contact = contactField.getText().trim();

        if (!ValidationUtils.validateRegisterInput(name, password, confirmPassword, email, contact)) {
            return;
        }

        if (ValidationUtils.isDuplicateExceptCurrent(email, contact, userID)) {
            JOptionPane.showMessageDialog(frame, "Email or contact already exists");
            return;
        }

        String oldName = user.getName();
        String oldPassword = user.getPassword();
        String oldEmail = user.getEmailAddress();
        String oldContact = user.getContactNumber();

        boolean changed = false;
        String log = "(" + role + ") " + userID + " updated personal info: ";

        if (!oldName.equals(name)) {
            log += "[Name: '" + oldName + "' → '" + name + "'] ";
            changed = true;
        }

        if (!oldPassword.equals(password)) {
            log += "[Password: changed] ";
            changed = true;
        }

        if (!oldEmail.equals(email)) {
            log += "[Email: '" + oldEmail + "' → '" + email + "'] ";
            changed = true;
        }

        if (!oldContact.equals(contact)) {
            log += "[Contact: '" + oldContact + "' → '" + contact + "'] ";
            changed = true;
        }

        if (changed) {
            FileHandler.writeSystemLog(log);
        }

        UserController.updateProfile(user, name, password, email, contact);

        JOptionPane.showMessageDialog(frame, "Information updated successfully");

        nameField.setText(user.getName());
        passwordField.setText(user.getPassword());
        confirmPasswordField.setText(user.getPassword());
        emailField.setText(user.getEmailAddress());
        contactField.setText(user.getContactNumber());
    }

    private void addLabel(String t, int x, int y, int w, int h) {
        JLabel l = new JLabel(t);
        l.setBounds(x, y, w, h);
        frame.add(l);
    }

    private void add(JComponent c, int x, int y, int w, int h) {
        c.setBounds(x, y, w, h);
        frame.add(c);
    }
}