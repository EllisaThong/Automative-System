package assignmentdegree;

import java.awt.BorderLayout;
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

public class CounterStaffFunction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == updateInfoButton) {
            frame.dispose();
            OpenUpdateInfo openUpdateInfo = new OpenUpdateInfo();
            openUpdateInfo.openUpdateInfoPage(loggedInStaff, userID, userRole);

        } else if (e.getSource() == manageCustomerButton) {
            frame.dispose();
            ManagerManageUsers managerManageUsers = new ManagerManageUsers();
            managerManageUsers.openManageUsersPage(userID, userRole);

        } else if (e.getSource() == addCustomerButton) {
            frame.dispose();
            ManagerAddUser managerAddUser = new ManagerAddUser();
            managerAddUser.openAddUserPage(userID, userRole);

        } else if (e.getSource() == approveRequestButton) {
            frame.dispose();
            ApproveRejectRequest approveRejectRequest = new ApproveRejectRequest();
            approveRejectRequest.openApproveRejectRequestPage(userID, userRole);

        } else if (e.getSource() == createAppointmentButton) {
            frame.dispose();
            CreateAppointment createAppointment = new CreateAppointment();
            createAppointment.openPage(userID, userRole);

        } else if (e.getSource() == collectPaymentButton) {
            frame.dispose();
            PayAppointment payAppointment = new PayAppointment();
            payAppointment.openPage(userID, userRole);

        } else if (e.getSource() == generateReceiptButton) {
            frame.dispose();
            GenerateReceipt gr = new GenerateReceipt();
            gr.openPage(userID, userRole);

        } else if (e.getSource() == managePaymentButton) {
            frame.dispose();
            ManagePayment mp = new ManagePayment();
            mp.openPage(userID, userRole);

        } else if (e.getSource() == manageReceiptButton) {
            frame.dispose();
            ManageReceipt mr = new ManageReceipt();
            mr.openPage(userID, userRole, null);

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
    private JButton manageCustomerButton;
    private JButton addCustomerButton;
    private JButton approveRequestButton;
    private JButton createAppointmentButton;
    private JButton collectPaymentButton;
    private JButton generateReceiptButton;
    private JButton managePaymentButton;
    private JButton manageReceiptButton;
    private JButton viewAnnouncementButton;
    private JButton logoutButton;

    private CounterStaff loggedInStaff;
    private String userID;
    private String userRole;

    public void openCounterStaffFunction(String userID, String role) {
        this.userID = userID;
        this.userRole = role;
        
        FileHandler.writeSystemLog("(" + userRole + ")" + userID + " opened the counter staff function page.");

        for (CounterStaff cs : DataStore.allCounterStaff) {
            if (cs.getUserID().equals(userID)) {
                loggedInStaff = cs;
                break;
            }
        }

        frame = new JFrame("Counter Staff Functions");
        frame.setSize(500, 550); // Reduced size
        frame.setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout(20, 20));

        titleLabel = new JLabel("Welcome! " + loggedInStaff.getStaffSummary());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        int managed = loggedInStaff.getManagedAppointmentsCount(DataStore.allAppointments);
        JLabel managedLabel = new JLabel("Appointments Managed: " + managed);
        managedLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(titleLabel, BorderLayout.NORTH);
        topContainer.add(managedLabel, BorderLayout.CENTER);

        buttonPanel = new JPanel(new GridLayout(11, 1, 10, 10)); // Reduced grid size

        updateInfoButton = createButton("Update Personal Info");
        manageCustomerButton = createButton("Manage Customers");
        addCustomerButton = createButton("Add Customer");
        approveRequestButton = createButton("Approve Register Requests");
        createAppointmentButton = createButton("Create/Manage Appointments");
        collectPaymentButton = createButton("Collect Payment");
        generateReceiptButton = createButton("Generate/Print Receipts");
        managePaymentButton = createButton("Manage Payments");
        manageReceiptButton = createButton("Manage Receipts");
        viewAnnouncementButton = createButton("View Announcements");
        logoutButton = createButton("Logout");

        buttonPanel.add(updateInfoButton);
        buttonPanel.add(manageCustomerButton);
        buttonPanel.add(addCustomerButton);
        buttonPanel.add(approveRequestButton);
        buttonPanel.add(createAppointmentButton);
        buttonPanel.add(collectPaymentButton);
        buttonPanel.add(generateReceiptButton);
        buttonPanel.add(managePaymentButton);
        buttonPanel.add(manageReceiptButton);
        buttonPanel.add(viewAnnouncementButton);
        buttonPanel.add(logoutButton);

        mainPanel.add(topContainer, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.setVisible(true);

        updateInfoButton.addActionListener(this);
        manageCustomerButton.addActionListener(this);
        addCustomerButton.addActionListener(this);
        approveRequestButton.addActionListener(this);
        createAppointmentButton.addActionListener(this);
        collectPaymentButton.addActionListener(this);
        generateReceiptButton.addActionListener(this);
        managePaymentButton.addActionListener(this);
        manageReceiptButton.addActionListener(this);
        viewAnnouncementButton.addActionListener(this);
        logoutButton.addActionListener(this);
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
            FileHandler.writeSystemLog("(" + userRole + ") " + loggedInStaff.getUserID() + " [" + loggedInStaff.getName() + "] logout the system.");
            LoginPage loginpage = new LoginPage();
            loginpage.openLoginPage();
        }
    }
}