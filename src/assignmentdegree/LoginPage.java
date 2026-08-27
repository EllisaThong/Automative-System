package assignmentdegree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LoginPage implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            handleLogin();
        } else if (e.getSource() == registerButton) {
            frame.dispose();
            RegisterPage registerPage = new RegisterPage();
            registerPage.openRegisterPage();
        } else if (e.getSource() == exitButton) {
            FileHandler.writeAllFiles();
            FileHandler.writeSystemLog("SYSTEM CLOSED: application terminated by user");
            System.exit(0);
        }
    }

    private JFrame frame;
    private JPanel leftPanel, rightPanel, mainPanel;
    private JLabel titleLabel, titleLabel2;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox showPassword;
    private JComboBox<String> roleSelector;
    private JButton loginButton, registerButton, exitButton;
    private JLabel forgetPasswordLabel;

    public void openLoginPage() {
        frame = new JFrame("Login Page");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 450);
        frame.setLayout(new BorderLayout());

        leftPanel = new JPanel(null);
        leftPanel.setBackground(new Color(34, 139, 34));

        titleLabel = new JLabel("APU Automotive Service", SwingConstants.CENTER);
        titleLabel2 = new JLabel("Centre (APU – ASC)", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel2.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel2.setForeground(Color.WHITE);
        titleLabel.setBounds(0, 160, 430, 40);
        titleLabel2.setBounds(0, 200, 430, 40);
        leftPanel.add(titleLabel);
        leftPanel.add(titleLabel2);

        rightPanel = new JPanel(null);
        rightPanel.setBackground(Color.WHITE);
        buildFormUI();

        mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void buildFormUI() {
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        showPassword = new JCheckBox("Show Password");
        showPassword.addActionListener(e -> passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : '*'));

        roleSelector = new JComboBox<>(new String[]{"Customer", "Counter Staff", "Technician", "Manager"});

        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        exitButton = new JButton("Exit");

        forgetPasswordLabel = new JLabel("Forgot Password?");
        forgetPasswordLabel.setForeground(Color.BLUE);
        forgetPasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgetPasswordLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                frame.dispose();
                ForgetPassword forgetPassword = new ForgetPassword();
                forgetPassword.openForgetPassword();
            }
        });

        int y = 60;
        placeLabel(rightPanel, "Email Address:", 50, y, 120, 30);
        place(rightPanel, usernameField, 160, y, 250, 30);
        placeLabel(rightPanel, "Password:", 50, y += 40, 120, 30);
        place(rightPanel, passwordField, 160, y, 250, 30);
        place(rightPanel, showPassword,  160, y += 30, 150, 20);
        placeLabel(rightPanel, "Role:", 50, y += 40, 120, 30);
        place(rightPanel, roleSelector,  160, y, 250, 30);
        place(rightPanel, loginButton,   100, y += 60, 100, 30);
        place(rightPanel, registerButton,220, y,      100, 30);
        place(rightPanel, exitButton,    160, y + 50, 100, 30);
        place(rightPanel, forgetPasswordLabel, 160, y + 100, 200, 30);

        loginButton.addActionListener(this);
        registerButton.addActionListener(this);
        exitButton.addActionListener(this);
    }

    private void handleLogin() {
        String email    = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String role     = (String) roleSelector.getSelectedItem();

        if (ValidationUtils.validateLoginInput(email, password)) {
            try {
                User user = UserController.login(email, password, role);
                if (user == null) {
                    JOptionPane.showMessageDialog(frame, "Invalid Credentials", "Error", JOptionPane.ERROR_MESSAGE);
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(frame, "Login Successful as " + role);
                    frame.dispose();
                    user.openMainMenu();
                }
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Account Locked", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void clearForm() {
        usernameField.setText("");
        passwordField.setText("");
        roleSelector.setSelectedIndex(0);
        showPassword.setSelected(false);
        passwordField.setEchoChar('*');
    }

    private void placeLabel(JPanel p, String text, int x, int y, int w, int h) {
        JLabel l = new JLabel(text);
        l.setBounds(x, y, w, h);
        p.add(l);
    }
    
    private void place(JPanel p, JComponent c, int x, int y, int w, int h) {
        c.setBounds(x, y, w, h);
        p.add(c);
    }
}