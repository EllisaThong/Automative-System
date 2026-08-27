package assignmentdegree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ForgetPassword implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == searchButton) {
            handleSearch();
        } else if (e.getSource() == resetButton) {
            handleReset();
        } else if (e.getSource() == backButton) {
            frame.dispose();
            LoginPage loginPage = new LoginPage();
            loginPage.openLoginPage();
        }
    }

    private JFrame frame;
    private JTextField nameField, emailField, contactField;
    private JComboBox<String> roleSelector;
    private JTextArea currentPasswordArea;
    private JPasswordField newPasswordField;
    private JButton searchButton, resetButton, backButton;
    private JCheckBox showPassword;

    public void openForgetPassword() {
        frame = new JFrame("Forgot Password");
        frame.setSize(500, 450);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(Color.WHITE);

        JLabel title = new JLabel("Forgot Password", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(15,10,10,10));
        frame.add(title, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(8, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20,40,20,40));
        panel.setBackground(Color.WHITE);

        nameField = new JTextField();
        emailField = new JTextField();
        contactField = new JTextField();

        roleSelector = new JComboBox<>(new String[]{"Customer", "Counter Staff", "Technician", "Manager"});
        roleSelector.addActionListener(e -> clearFields());

        currentPasswordArea = new JTextArea();
        currentPasswordArea.setEditable(false);
        currentPasswordArea.setBackground(new Color(240, 240, 240));

        newPasswordField = new JPasswordField();
        showPassword = new JCheckBox("Show Password");
        showPassword.setBackground(Color.WHITE);
        showPassword.addActionListener(e -> newPasswordField.setEchoChar(showPassword.isSelected() ? (char) 0 : '*'));

        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Contact:"));
        panel.add(contactField);
        panel.add(new JLabel("Role:"));
        panel.add(roleSelector);
        panel.add(new JLabel("Current Password:"));
        panel.add(new JScrollPane(currentPasswordArea));
        panel.add(new JLabel("New Password:"));
        panel.add(newPasswordField);
        panel.add(new JLabel(""));
        panel.add(showPassword);
        frame.add(panel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        searchButton = new JButton("Search User");
        resetButton = new JButton("Reset Password");
        resetButton.setEnabled(false);
        backButton = new JButton("Back");
        btnPanel.add(searchButton);
        btnPanel.add(resetButton);
        btnPanel.add(backButton);
        frame.add(btnPanel, BorderLayout.SOUTH);

        searchButton.addActionListener(this);
        resetButton.addActionListener(this);
        backButton.addActionListener(this);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void clearFields() {
        nameField.setText("");
        emailField.setText("");
        contactField.setText("");
        currentPasswordArea.setText("");
        newPasswordField.setText("");
        resetButton.setEnabled(false);
    }

    private void handleSearch() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String contact = contactField.getText().trim();
        String role = (String) roleSelector.getSelectedItem();
        if (name.isEmpty() || email.isEmpty() || contact.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please fill all fields");
        } else {
            String password = LookupService.searchUserPassword(name, email, contact, role);
            if (password != null) {
                currentPasswordArea.setText(password);
                resetButton.setEnabled(true);
                JOptionPane.showMessageDialog(frame, "User Found");
            } else {
                JOptionPane.showMessageDialog(frame, "User Not Found");
                clearFields();
            }
        }
    }

    private void handleReset() {
        String name = nameField.getText();
        String role = (String) roleSelector.getSelectedItem();
        String newPassword = new String(newPasswordField.getPassword());
        if (newPassword.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Enter new password");
        } else {
            boolean success = UserController.resetPassword(name, role, newPassword);
            if (success) {
                JOptionPane.showMessageDialog(frame, "Password Reset Successful");
                frame.dispose();
                LoginPage loginPage = new LoginPage();
                loginPage.openLoginPage();
            }
        }
    }
}