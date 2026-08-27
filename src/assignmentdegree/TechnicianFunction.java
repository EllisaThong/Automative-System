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

public class TechnicianFunction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == updateInfoButton) {
            frame.dispose();
            OpenUpdateInfo openUpdateInfo = new OpenUpdateInfo();
            openUpdateInfo.openUpdateInfoPage(loggedInTechnician, userID, userRole);

        } else if (e.getSource() == viewAppointmentButton) {
            frame.dispose();
            TechnicianAppointments ta = new TechnicianAppointments();
            ta.openPage(userID, userRole);

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
    private JButton viewAppointmentButton;
    private JButton viewAnnouncementButton;
    private JButton logoutButton;

    private Technician loggedInTechnician;
    private String userID;
    private String userRole;

    public void openTechnicianFunction(String userID, String role) {
        this.userID = userID;
        this.userRole = role;
        
        FileHandler.writeSystemLog("(" + userRole + ")" + userID + " opened the technician function page.");

        for (Technician t : DataStore.allTechnicians) {
            if (t.getUserID().equals(userID)) {
                loggedInTechnician = t;
                break;
            }
        }

        frame = new JFrame("Technician Functions");
        frame.setSize(500, 450); // Reduced size
        frame.setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout(20, 20));

        titleLabel = new JLabel("Welcome, " + loggedInTechnician.getName() + " (" + loggedInTechnician.getTechnicianIDFormatted() + ")");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);
        int assigned = loggedInTechnician.getAssignedAppointmentsCount(DataStore.allAppointments);
        int completed = loggedInTechnician.getCompletedAppointmentsCount(DataStore.allAppointments);
        JLabel assignedLabel = new JLabel("Total Assigned: " + assigned);
        assignedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel completedLabel = new JLabel("Total Completed: " + completed);
        completedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoPanel.add(assignedLabel);
        infoPanel.add(completedLabel);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(titleLabel, BorderLayout.NORTH);
        topContainer.add(infoPanel, BorderLayout.CENTER);

        buttonPanel = new JPanel(new GridLayout(4, 1, 10, 10)); // Reduced grid size

        updateInfoButton = createButton("Update Personal Info");
        viewAppointmentButton = createButton("View Assigned Appointments");
        viewAnnouncementButton = createButton("View Announcements");
        logoutButton = createButton("Logout");

        buttonPanel.add(updateInfoButton);
        buttonPanel.add(viewAppointmentButton);
        buttonPanel.add(viewAnnouncementButton);
        buttonPanel.add(logoutButton);

        mainPanel.add(topContainer, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.setVisible(true);

        updateInfoButton.addActionListener(this);
        viewAppointmentButton.addActionListener(this);
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
            FileHandler.writeSystemLog("(" + userRole + ") " + loggedInTechnician.getUserID() + " [" + loggedInTechnician.getName() + "] logout the system.");
            LoginPage loginpage = new LoginPage();
            loginpage.openLoginPage();
        }
    }
}