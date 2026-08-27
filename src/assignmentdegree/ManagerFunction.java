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

public class ManagerFunction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == approveRequestButton) {
            frame.dispose();
            ApproveRejectRequest approveRejectRequest = new ApproveRejectRequest();
            approveRejectRequest.openApproveRejectRequestPage(managerID, userRole);

        } else if (e.getSource() == manageUserButton) {
            frame.dispose();
            ManagerManageUsers managerManageUsers = new ManagerManageUsers();
            managerManageUsers.openManageUsersPage(managerID, userRole);

        } else if (e.getSource() == addUserButton) {
            frame.dispose();
            ManagerAddUser managerAddUser = new ManagerAddUser();
            managerAddUser.openAddUserPage(managerID, userRole);

        } else if (e.getSource() == manageServiceButton) {
            frame.dispose();
            ServiceManagement serviceManagement = new ServiceManagement();
            serviceManagement.openServiceManagement(managerID, userRole);

        } else if (e.getSource() == viewFeedbackButton) {
            frame.dispose();
            ManagerViewFeedback mvf = new ManagerViewFeedback();
            mvf.openPage(managerID, userRole);

        } else if (e.getSource() == viewReportButton) {
            frame.dispose();
            ManagerReports mr = new ManagerReports();
            mr.openPage(managerID, userRole);

        } else if (e.getSource() == viewLogButton) {
            frame.dispose();
            SystemLogView slv = new SystemLogView();
            slv.openSystemLogView(managerID, userRole);

        } else if (e.getSource() == announcementButton) {
            frame.dispose();
            AnnouncementCenter announcementCenter = new AnnouncementCenter();
            announcementCenter.openPage(managerID, userRole);

        } else if (e.getSource() == logoutButton) {
            logout();
        }
    }

    private JFrame frame;
    private JPanel mainPanel, buttonPanel;
    private JLabel titleLabel;

    private JButton approveRequestButton;
    private JButton manageUserButton;
    private JButton addUserButton;
    private JButton manageServiceButton;
    private JButton viewFeedbackButton;
    private JButton viewReportButton;
    private JButton viewLogButton;
    private JButton announcementButton;
    private JButton logoutButton;

    private Manager loggedInManager;
    private String managerID;
    private String userRole;

    public void openManagerFunction(String managerID, String role) {
        this.managerID = managerID;
        this.userRole = role;
        
        FileHandler.writeSystemLog("(" + userRole + ")" + managerID + " opened the manager function page.");

        for (Manager m : DataStore.allManagers) {
            if (m.getUserID().equals(managerID)) {
                loggedInManager = m;
                break;
            }
        }

        frame = new JFrame("Manager Functions");
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout(20, 20));

        titleLabel = new JLabel(loggedInManager.getAdminDashboardTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        int totalUsers = loggedInManager.countTotalUsers(DataStore.allCustomers, DataStore.allCounterStaff, DataStore.allTechnicians, DataStore.allManagers);
        JLabel userCountLabel = new JLabel("Total Registered Users: " + totalUsers);
        userCountLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(titleLabel, BorderLayout.NORTH);
        topContainer.add(userCountLabel, BorderLayout.CENTER);

        buttonPanel = new JPanel(new GridLayout(9, 1, 10, 10)); // Reduced grid size

        approveRequestButton = createButton("Approve Register Requests");
        manageUserButton = createButton("View/Edit/Delete Users");
        addUserButton = createButton("Add Users");
        manageServiceButton = createButton("Manage Services");
        viewFeedbackButton = createButton("Manage Comments & Rating");
        viewReportButton = createButton("View Reports");
        viewLogButton = createButton("View System Logs");
        announcementButton = createButton("Announcement Center");
        logoutButton = createButton("Logout");

        buttonPanel.add(approveRequestButton);
        buttonPanel.add(manageUserButton);
        buttonPanel.add(addUserButton);
        buttonPanel.add(manageServiceButton);
        buttonPanel.add(viewFeedbackButton);
        buttonPanel.add(viewReportButton);
        buttonPanel.add(viewLogButton);
        buttonPanel.add(announcementButton);
        buttonPanel.add(logoutButton);

        mainPanel.add(topContainer, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.setVisible(true);

        approveRequestButton.addActionListener(this);
        manageUserButton.addActionListener(this);
        addUserButton.addActionListener(this);
        manageServiceButton.addActionListener(this);
        viewFeedbackButton.addActionListener(this);
        viewReportButton.addActionListener(this);
        viewLogButton.addActionListener(this);
        announcementButton.addActionListener(this);
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
            FileHandler.writeSystemLog("(" + userRole + ") " + loggedInManager.getUserID() + " [" + loggedInManager.getName() + "] logout the system.");
            LoginPage loginpage = new LoginPage();
            loginpage.openLoginPage();
        }
    }
}