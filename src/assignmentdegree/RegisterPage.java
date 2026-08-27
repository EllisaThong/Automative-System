package assignmentdegree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class RegisterPage implements ActionListener {

    private JFrame frame;
    private JTextField nameField, emailField, contactField;
    private JPasswordField passwordField, confirmPasswordField;
    private JCheckBox showPassword;
    private JComboBox<String> roleSelector;
    private JButton registerButton, loginPageButton;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == registerButton) {
            handleRegister();
        } else if (e.getSource() == loginPageButton) {
            frame.dispose();
            LoginPage loginPage = new LoginPage();
            loginPage.openLoginPage();
        }
    }

    public void openRegisterPage() {
        frame = new JFrame("Register Page");
        frame.setSize(450, 500);
        frame.setLayout(null);

        nameField = new JTextField();
        emailField = new JTextField();
        contactField = new JTextField();
        passwordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();

        showPassword = new JCheckBox("Show Password");
        showPassword.addActionListener(e -> {
            boolean show = showPassword.isSelected();
            passwordField.setEchoChar(show ? (char) 0 : '*');
            confirmPasswordField.setEchoChar(show ? (char) 0 : '*');
        });

        roleSelector = new JComboBox<>(new String[]{"Customer", "Counter Staff", "Technician", "Manager"});
        registerButton = new JButton("Register");
        loginPageButton = new JButton("Back to Login");

        int xL = 50, xF = 180, wL = 120, wF = 200, h = 30, y = 50, gap = 40;

        addLabel("Name:", xL, y, wL, h);
        add(nameField, xF, y, wF, h);
        addLabel("Password:", xL, y += gap, wL, h);
        add(passwordField, xF, y, wF, h);
        addLabel("Confirm Password:", xL, y += gap, wL, h);
        add(confirmPasswordField, xF, y, wF, h);
        add(showPassword, xF, y += 30, wF, h);
        addLabel("Email:", xL, y += gap, wL, h);
        add(emailField, xF, y, wF, h);
        addLabel("Contact Number:", xL, y += gap, wL, h);
        add(contactField, xF, y, wF, h);
        addLabel("Role:", xL, y += gap, wL, h);
        add(roleSelector, xF, y, wF, h);
        add(registerButton, 100, y += gap + 20, 120, 35);
        add(loginPageButton, 240, y, 120, 35);

        registerButton.addActionListener(this);
        loginPageButton.addActionListener(this);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void handleRegister() {
        String name            = nameField.getText().trim();
        String email           = emailField.getText().trim();
        String contact         = contactField.getText().trim();
        String password        = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String role            = (String) roleSelector.getSelectedItem();

        if (ValidationUtils.validateRegisterInput(name, password, confirmPassword, email, contact)) {
            if (ValidationUtils.isDuplicate(email, contact)) {
                JOptionPane.showMessageDialog(frame, "Email or contact already exists");
            } else {
                UserController.register(name, password, email, contact, role);
                JOptionPane.showMessageDialog(frame, "Registration submitted.\nPlease wait for manager approval.");
                clearForm();
            }
        }
    }

    private void clearForm() {
        nameField.setText("");
        emailField.setText("");
        contactField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        roleSelector.setSelectedIndex(0);
        showPassword.setSelected(false);
        passwordField.setEchoChar('*');
        confirmPasswordField.setEchoChar('*');
    }

    private void addLabel(String text, int x, int y, int w, int h) {
        JLabel l = new JLabel(text);
        l.setBounds(x, y, w, h);
        frame.add(l);
    }

    private void add(JComponent c, int x, int y, int w, int h) {
        c.setBounds(x, y, w, h);
        frame.add(c);
    }
}