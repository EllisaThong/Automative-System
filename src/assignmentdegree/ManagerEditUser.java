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

public class ManagerEditUser implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == saveButton) {
            handleEdit();
        } else if (e.getSource() == backButton) {
            frame.dispose();
            ManagerManageUsers managerManageUsers = new ManagerManageUsers();
            managerManageUsers.openManageUsersPage(userID, userRole);
        }
    }
    
    private JFrame frame;
    private JTextField nameField, emailField, contactField;
    private JPasswordField passwordField;
    private JCheckBox showPassword;
    private JButton saveButton, backButton;
    private User user;
    private String userID, userRole;

    public void openManagerEditUserPage(User u, String userID, String role) {
        this.user = u;
        this.userID = userID;
        this.userRole = role;
        
        FileHandler.writeSystemLog("(" + userRole + ")" + userID + " opened the edit user " + userRole + " function.");

        frame = new JFrame("Edit User");
        frame.setSize(400, 420);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);

        nameField = new JTextField(u.getName());
        emailField = new JTextField(u.getEmailAddress());
        contactField = new JTextField(u.getContactNumber());
        passwordField = new JPasswordField(u.getPassword());
        showPassword = new JCheckBox("Show Password");
        showPassword.addActionListener(e -> passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : '*'));
        saveButton = new JButton("Save");
        backButton = new JButton("Back");

        int xL = 40, xF = 150, wF = 180, h = 30;
        addLabel("Name:", xL, 40, 100, h);
        add(nameField, xF, 40, wF, h);
        addLabel("Email:", xL, 90, 100, h);
        add(emailField, xF, 90, wF, h);
        addLabel("Contact:", xL, 140, 100, h);
        add(contactField, xF, 140, wF, h);
        addLabel("Register Date:", xL, 190, 100, h);
        JLabel regDateLabel = new JLabel(u.getRegisterDate());
        regDateLabel.setBounds(xF, 190, wF, h);
        frame.add(regDateLabel);
        addLabel("Password:", xL, 230, 100, h);
        add(passwordField, xF, 230, wF, h);
        add(showPassword, xF, 260, 150, 25);
        add(saveButton, 80, 310, 100, 30);
        add(backButton, 200, 310, 100, 30);

        saveButton.addActionListener(this);
        backButton.addActionListener(this);
        frame.setVisible(true);
    }

    private void handleEdit() {
        String name     = nameField.getText().trim();
        String email    = emailField.getText().trim();
        String contact  = contactField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (ValidationUtils.validateNotEmpty(name, email, contact, password)) {
            if (ValidationUtils.validateName(name)) {
                if (ValidationUtils.validateEmail(email)) {
                    if (ValidationUtils.validateContact(contact)) {
                        if (ValidationUtils.validatePassword(password)) {
                            if (ValidationUtils.isDuplicateExceptCurrent(email, contact, user.getUserID())) {
                                JOptionPane.showMessageDialog(frame, "Email or contact already exists");
                            } else {
                                UserController.updateProfile(user, name, password, email, contact);
                                FileHandler.writeSystemLog("(" + userRole + ") " + userID + " updated user profile: " + user.getUserID());
                                JOptionPane.showMessageDialog(frame, "User Updated");
                                frame.dispose();
                                ManagerManageUsers managerManageUsers = new ManagerManageUsers();
                                managerManageUsers.openManageUsersPage(userID, userRole);
                            }
                        }
                    }
                }
            }
        }
    }

    private void addLabel(String t, int x, int y, int w, int h) {
        JLabel l = new JLabel(t);
        l.setBounds(x,y,w,h); frame.add(l);
    }
    private void add(JComponent c, int x, int y, int w, int h) {
        c.setBounds(x,y,w,h);
        frame.add(c);
    }
}