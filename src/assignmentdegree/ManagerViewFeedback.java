package assignmentdegree;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class ManagerViewFeedback implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == refreshButton) {
            FileHandler.readAllFiles();
            loadAppointments();
            clearMiddle();
            clearRight();
        } else if (e.getSource() == backButton) {
            frame.dispose();
            ManagerFunction managerFunction = new ManagerFunction();
            managerFunction.openManagerFunction(userID, userRole);
        } else if (e.getSource() == deleteButton) {
            deleteSelectedFeedback();
        }
    }
    
    private JFrame frame;
    private String userID, userRole;
    private DefaultListModel<String> apptListModel;
    private JList<String> apptList;
    private DefaultListModel<String> feedbackListModel;
    private JList<String> feedbackList;
    private JLabel middleTitle;
    private JTextArea detailArea;
    private JButton refreshButton, backButton, deleteButton;
    private JComboBox<String> roleFilterBox;
    private Appointment selectedAppointment = null;

    public void openPage(String userID, String userRole) {
        this.userID = userID;
        this.userRole = userRole;
        
        FileHandler.writeSystemLog("(" + userRole + ")" + userID + " opened the view appointment feedback " + userRole + " function.");

        frame = new JFrame("Feedback Management");
        frame.setSize(1100, 650);
        frame.setLayout(new BorderLayout(8, 8));
        frame.setLocationRelativeTo(null);

        frame.add(buildTopBar(), BorderLayout.NORTH);
        JSplitPane leftMiddle = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildLeftPanel(), buildMiddlePanel());
        leftMiddle.setDividerLocation(380);
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftMiddle, buildRightPanel());
        mainSplit.setDividerLocation(700);
        frame.add(mainSplit, BorderLayout.CENTER);
        frame.add(buildBottomBar(), BorderLayout.SOUTH);

        loadAppointments();
        frame.setVisible(true);
    }

    private JPanel buildTopBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(8, 10, 8, 10));
        JLabel title = new JLabel("Feedback Management");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(this);
        panel.add(title, BorderLayout.WEST);
        panel.add(refreshButton, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Appointments with Feedback"));
        apptListModel = new DefaultListModel<>();
        apptList = new JList<>(apptListModel);
        apptList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onAppointmentSelected();
            }
        });
        panel.add(new JScrollPane(apptList), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildMiddlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Communication"));

        middleTitle = new JLabel("Select appointment");
        middleTitle.setBorder(new EmptyBorder(5, 5, 5, 5));

        roleFilterBox = new JComboBox<>(new String[]{"All", "Technician", "Customer"});
        roleFilterBox.addActionListener(e -> {
            clearRight();
            if (selectedAppointment != null) {
                loadFeedbacks(selectedAppointment.getAppointmentID());
            }
        });

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.add(middleTitle, BorderLayout.WEST);
        topRow.add(roleFilterBox, BorderLayout.EAST);

        feedbackListModel = new DefaultListModel<>();
        feedbackList = new JList<>(feedbackListModel);
        feedbackList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onFeedbackSelected();
            }
        });

        panel.add(topRow, BorderLayout.NORTH);
        panel.add(new JScrollPane(feedbackList), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Details"));
        detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        panel.add(new JScrollPane(detailArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBottomBar() {
        backButton = new JButton("Back");
        backButton.addActionListener(this);
        deleteButton = new JButton("Delete Feedback");
        deleteButton.addActionListener(this);
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(deleteButton);
        panel.add(backButton);
        return panel;
    }

    private void loadAppointments() {
        apptListModel.clear();
        for (Appointment a : DataStore.allAppointments) {
            boolean hasFeedback = false;
            for (Comment c : DataStore.allComments) {
                if (c.getAppointmentID().equals(a.getAppointmentID())) {
                    hasFeedback = true;
                    break;
                }
            }
            if (!hasFeedback) {
                for (Rating r : DataStore.allRatings) {
                    if (r.getAppointmentID().equals(a.getAppointmentID())) {
                        hasFeedback = true;
                        break;
                    }
                }
            }

            if (hasFeedback) {
                apptListModel.addElement(a.getAppointmentID() + " | " + LookupService.getCustomerNameByID(a.getCustomerID()));
            }
        }
    }

    private void onAppointmentSelected() {
        String sel = apptList.getSelectedValue();
        if (sel == null) return;
        String apptID = sel.split(" \\| ")[0];
        selectedAppointment = LookupService.getAppointmentByID(apptID);
        middleTitle.setText("Feedback - " + apptID);
        loadFeedbacks(apptID); clearRight();
    }

    private void loadFeedbacks(String apptID) {
        feedbackListModel.clear();
        feedbackListModel.addElement("ALL | Full History View");
        String filter = (String) roleFilterBox.getSelectedItem();

        // Load Rating if any (only if filter is All or Customer)
        if ("All".equals(filter) || "Customer".equals(filter)) {
            Rating r = RatingController.getRatingByAppointmentID(apptID);
            if (r != null) {
                feedbackListModel.addElement(r.getRatingID() + " | Rating: " + r.getRatingValue() + "/5");
            }
        }

        // Load Comments
        for (Comment c : DataStore.allComments) {
            if (!c.getAppointmentID().equals(apptID)) continue;
            String role = LookupService.getUserRoleByID(c.getUserID());
            if (!"All".equals(filter) && !role.equalsIgnoreCase(filter)) continue;
            feedbackListModel.addElement(c.getCommentID() + " | " + LookupService.getUserNameByID(c.getUserID()));
        }
    }

    private void onFeedbackSelected() {
        String sel = feedbackList.getSelectedValue();
        if (sel == null) {
            return;
        }
        
        if (sel.startsWith("ALL")) {
            showFullHistory(selectedAppointment.getAppointmentID());
            return;
        }
        
        String id = sel.split(" \\| ")[0];

        if (id.startsWith("RT")) {
            Rating r = RatingController.getRatingByAppointmentID(selectedAppointment.getAppointmentID());
            if (r != null) {
                detailArea.setText(
                        "Type: Customer Rating\n" +
                        "User: " + LookupService.getUserNameByID(r.getCustomerID()) +
                        "\nDate: " + r.getDateTime() +
                        "\nRating: " + r.getRatingValue() + " / 5");
                return;
            }
        }

        for (Comment c : DataStore.allComments) {
            if (c.getCommentID().equals(id)) {
                detailArea.setText(
                        "Type: Comment/Feedback\n" +
                        "User: " + LookupService.getUserNameByID(c.getUserID()) +
                        " (" + LookupService.getUserRoleByID(c.getUserID()) + ")" +
                        "\nDate: " + c.getDateTime() +
                        "\n" + c.getFormattedComment());
                return;
            }
        }
    }

    private void showFullHistory(String apptID) {
        detailArea.setText(
                "--- Rating ---\n" + LookupService.getRatingDisplay(apptID) +
                "\n\n--- Technician Feedback History ---\n" + LookupService.getTechnicianFeedbackHistory(apptID) +
                "\n--- Customer Comment History ---\n"  + LookupService.getCommentsForAppointment(apptID, false));
    }

    private void deleteSelectedFeedback() {
        String sel = feedbackList.getSelectedValue();
        if (sel == null || sel.startsWith("ALL")) {
            JOptionPane.showMessageDialog(frame, "Select a feedback item to delete.");
            return;
        }

        String id = sel.split(" \\| ")[0];

        int confirm = JOptionPane.showConfirmDialog(frame, "Delete this feedback item?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        if (id.startsWith("RT")) {
            Rating r = RatingController.getRatingByAppointmentID(selectedAppointment.getAppointmentID());
            if (r != null) {
                RatingController.deleteRating(r.getRatingID());
                FileHandler.writeSystemLog("(" + userRole + ") " + userID + " deleted rating: " + r.getRatingValue() + " from appointment: " + selectedAppointment.getAppointmentID());
                JOptionPane.showMessageDialog(frame, "Rating deleted successfully.");
            }
        } else {
            Comment target = null;
            for (Comment c : DataStore.allComments) {
                if (c.getCommentID().equals(id)) {
                    target = c;
                    break;
                }
            }

            if (target != null) {
                CommentController.deleteComment(id);
                FileHandler.writeSystemLog("(" + userRole + ") " + userID + " deleted comment: " + target.getCommentText() + " from appointment: " + selectedAppointment.getAppointmentID());
                JOptionPane.showMessageDialog(frame, "Comment deleted successfully.");
            }
        }

        loadFeedbacks(selectedAppointment.getAppointmentID());
        clearRight();
    }

    private void clearMiddle() {
        feedbackListModel.clear();
        middleTitle.setText("Select appointment");
    }

    private void clearRight() {
        detailArea.setText("Select a comment.");
    }
}