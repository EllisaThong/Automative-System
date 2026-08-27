package assignmentdegree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class ManagerAddUser implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            handleAdd();
        } else if (e.getSource() == backButton) {
            frame.dispose();
            if (userRole.equals("Manager")) {
                ManagerFunction managerFunction = new ManagerFunction();
                managerFunction.openManagerFunction(userID, userRole);
            } else {
                CounterStaffFunction counterStaffFunction = new CounterStaffFunction();
                counterStaffFunction.openCounterStaffFunction(userID, userRole);
            }
        }
    }

    private JFrame frame;
    private JTextField nameField, emailField, contactField;
    private JPasswordField passwordField;
    private JCheckBox showPassword;
    private JComboBox<String> roleSelector;
    private JButton addButton, backButton;
    private String userID, userRole;

    public void openAddUserPage(String userID, String role) {
        this.userID = userID;
        this.userRole = role;
        
        FileHandler.writeSystemLog("(" + userRole + ")" + userID + " opened the add user " + userRole + " function.");

        frame = new JFrame("Add User");
        frame.setSize(400, 420);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);

        nameField = new JTextField();
        emailField = new JTextField();
        contactField = new JTextField();
        passwordField = new JPasswordField();
        showPassword = new JCheckBox("Show Password");
        showPassword.addActionListener(e -> passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : '*'));

        roleSelector = role.equals("Manager") ? new JComboBox<>(new String[]{"Counter Staff", "Technician", "Manager"}) : new JComboBox<>(new String[]{"Customer"});

        addButton = new JButton("Add User");
        backButton = new JButton("Back");

        int xL = 40, xF = 150, wF = 180, h = 30;
        addLabel("Name:", xL, 40, 100, h);
        add(nameField, xF, 40, wF, h);
        addLabel("Email:", xL, 80, 100, h);
        add(emailField, xF, 80, wF, h);
        addLabel("Contact:", xL, 120, 100, h);
        add(contactField, xF, 120, wF, h);
        addLabel("Password:", xL, 160, 100, h);
        add(passwordField, xF, 160, wF, h);
        add(showPassword, xF, 190, 150, 25);
        addLabel("Role:", xL, 220, 100, h);
        add(roleSelector, xF, 220, wF, h);
        add(addButton, 80, 280, 110, 35);
        add(backButton, 210, 280, 110, 35);

        addButton.addActionListener(this);
        backButton.addActionListener(this);
        frame.setVisible(true);
    }

    private void handleAdd() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String contact = contactField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String role = (String) roleSelector.getSelectedItem();

        if (ValidationUtils.validateNotEmpty(name, email, contact, password)) {
            if (ValidationUtils.validateName(name)) {
                if (ValidationUtils.validatePassword(password)) {
                    if (ValidationUtils.validateEmail(email)) {
                        if (ValidationUtils.validateContact(contact)) {
                            if (ValidationUtils.isDuplicate(email, contact)) {
                                JOptionPane.showMessageDialog(frame, "Email or Contact Number already exists!");
                            } else {
                                String registerDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                                UserController.addUser(name, password, email, contact, role, registerDate);
                                FileHandler.writeSystemLog("(" + userRole + ") " + userID + " added new user: " + email + " [" + role + "]");
                                JOptionPane.showMessageDialog(frame, "User Added Successfully");
                                nameField.setText("");
                                emailField.setText("");
                                contactField.setText("");
                                passwordField.setText("");
                                roleSelector.setSelectedIndex(0);
                                showPassword.setSelected(false);
                                passwordField.setEchoChar('*');
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