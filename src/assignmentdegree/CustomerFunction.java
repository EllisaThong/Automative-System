package assignmentdegree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class CustomerFunction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == updateInfoButton) {
            frame.dispose();
            OpenUpdateInfo openUpdateInfo = new OpenUpdateInfo();
            openUpdateInfo.openUpdateInfoPage(loggedInCustomer, userID, userRole);

        } else if (e.getSource() == serviceHistoryButton) {
            frame.dispose();
            CustomerAppointmentPage cap = new CustomerAppointmentPage();
            cap.openPage(userID, userRole);

        } else if (e.getSource() == viewPaymentHistoryButton) {
            frame.dispose();
            CustomerPaymentHistory cph = new CustomerPaymentHistory();
            cph.openPage(userID, userRole);

        } else if (e.getSource() == viewAnnouncementButton) {
            frame.dispose();
            AnnouncementCenter announcementCenter = new AnnouncementCenter();
            announcementCenter.openPage(userID, userRole);

        } else if (e.getSource() == logoutButton) {
            logout();
        }
    }

    private JFrame frame;
    private JPanel mainPanel, buttonPanel;
    private JLabel titleLabel;

    private JButton updateInfoButton;
    private JButton serviceHistoryButton;
    private JButton viewPaymentHistoryButton;
    private JButton viewAnnouncementButton;
    private JButton logoutButton;

    private Customer loggedInCustomer;
    private String userID;
    private String userRole;

    public void openCustomerFunction(String userID, String role) {
        this.userID = userID;
        this.userRole = role;
        
        FileHandler.writeSystemLog("(" + userRole + ")" + userID + " opened the customer function page.");

        for (Customer c : DataStore.allCustomers) {
            if (c.getUserID().equals(userID)) {
                loggedInCustomer = c;
                break;
            }
        }

        frame = new JFrame("Customer Functions");
        frame.setSize(500, 500); // Increased size to show more info
        frame.setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(new Color(240, 248, 255));

        titleLabel = new JLabel(loggedInCustomer.getWelcomeMessage());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);
        double totalSpent = loggedInCustomer.getTotalSpent(DataStore.allPayments, DataStore.allAppointments);
        JLabel spentLabel = new JLabel("Total Spent: RM " + String.format("%.2f", totalSpent));
        spentLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoPanel.add(spentLabel);

        int unpaid = loggedInCustomer.getUnpaidCount(DataStore.allPayments, DataStore.allAppointments);
        if (unpaid > 0) {
            JLabel unpaidLabel = new JLabel("Pending Payments: " + unpaid);
            unpaidLabel.setForeground(Color.RED);
            unpaidLabel.setHorizontalAlignment(SwingConstants.CENTER);
            infoPanel.add(unpaidLabel);
        }

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(titleLabel, BorderLayout.NORTH);
        topContainer.add(infoPanel, BorderLayout.CENTER);

        buttonPanel = new JPanel(new GridLayout(5, 1, 10, 10)); // Reduced grid size

        updateInfoButton = createButton("Update Personal Info");
        serviceHistoryButton = createButton("Service History Management");
        viewPaymentHistoryButton = createButton("View Payment History");
        viewAnnouncementButton = createButton("View Announcements");
        logoutButton = createButton("Logout");

        buttonPanel.add(updateInfoButton);
        buttonPanel.add(serviceHistoryButton);
        buttonPanel.add(viewPaymentHistoryButton);
        buttonPanel.add(viewAnnouncementButton);
        buttonPanel.add(logoutButton);

        mainPanel.add(topContainer, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.setVisible(true);

        updateInfoButton.addActionListener(this);
        serviceHistoryButton.addActionListener(this);
        viewPaymentHistoryButton.addActionListener(this);
        viewAnnouncementButton.addActionListener(this);
        logoutButton.addActionListener(this);

        if (unpaid > 0) {
            JOptionPane.showMessageDialog(null, 
                "You have " + unpaid + " unpaid or partially paid appointment(s).\nPlease check your payment history.", 
                "Payment Reminder", 
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private JButton createButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 16));
        return b;
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(frame,
                "Logout notification!!!\nYou are logging out from the system!!!",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            frame.dispose();
            FileHandler.writeSystemLog("(" + userRole + ") " + loggedInCustomer.getUserID() + " [" + loggedInCustomer.getName() + "] logout the system.");
            LoginPage loginpage = new LoginPage();
            loginpage.openLoginPage();
        }
    }
}