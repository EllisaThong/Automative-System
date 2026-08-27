package assignmentdegree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class CustomerAppointmentPage implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submitCommentButton) {
            submitComment();
        } else if (e.getSource() == manageCommentButton) {
            openManageComments();
        } else if (e.getSource() == refreshButton) {
            loadAppointments();
        } else if (e.getSource() == clearFilterButton) {
            resetFilters();
            loadAppointments();
        } else if (e.getSource() == backButton) {
            frame.dispose();
            CustomerFunction customerFunction = new CustomerFunction();
            customerFunction.openCustomerFunction(userID, userRole);
        }
    }

    private JFrame frame;
    private DefaultListModel<String> listModel;
    private JList<String> appointmentList;
    private JTextArea detailArea;
    private JButton submitCommentButton, manageCommentButton, refreshButton, backButton, clearFilterButton;
    private JComboBox<String> filterYearBox, filterMonthBox, filterStatusBox, filterDayBox;
    private JTextField searchField;
    private Appointment selectedAppointment;
    private String userID, userRole;
    private boolean updatingDays = false;

    // Rating UI components
    private JPanel ratingPanel;
    private JButton[] starButtons;
    private JButton cancelRatingButton;
    private JLabel ratingStatusLabel;

    public void openPage(String userID, String userRole) {
        this.userID = userID;
        this.userRole = userRole;
        
        FileHandler.writeSystemLog("(" + userRole + ")" + userID + " opened the service history management " + userRole + " function.");

        frame = new JFrame("Service History Management");
        frame.setSize(950, 550);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null);

        listModel = new DefaultListModel<>();
        appointmentList = new JList<>(listModel);
        appointmentList.addListSelectionListener(e -> showDetails());

        detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        setupRatingPanel();

        submitCommentButton = new JButton("Submit Comment");
        manageCommentButton = new JButton("Manage My Comments");
        refreshButton = new JButton("Refresh");
        backButton = new JButton("Back");
        clearFilterButton = new JButton("Clear Filter");

        filterYearBox = new JComboBox<>();
        filterMonthBox = new JComboBox<>();
        filterDayBox = new JComboBox<>();
        filterStatusBox = new JComboBox<>();
        searchField = new JTextField(12);
        setupFilters();

        JPanel filterPanel = new JPanel(new GridLayout(2, 5, 8, 8));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter"));
        filterPanel.add(new JLabel("Year"));
        filterPanel.add(new JLabel("Month"));
        filterPanel.add(new JLabel("Day"));
        filterPanel.add(new JLabel("Status"));
        filterPanel.add(new JLabel("Search"));
        filterPanel.add(filterYearBox);
        filterPanel.add(filterMonthBox);
        filterPanel.add(filterDayBox);
        filterPanel.add(filterStatusBox);
        filterPanel.add(searchField);

        JPanel topBtn = new JPanel();
        topBtn.add(refreshButton);
        topBtn.add(clearFilterButton);
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(filterPanel, BorderLayout.CENTER);
        topPanel.add(topBtn, BorderLayout.EAST);

        JPanel rightWrapper = new JPanel(new BorderLayout());
        rightWrapper.add(new JScrollPane(detailArea), BorderLayout.CENTER);
        rightWrapper.add(ratingPanel, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(appointmentList), rightWrapper);
        split.setDividerLocation(400);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(submitCommentButton);
        bottomPanel.add(manageCommentButton);
        bottomPanel.add(backButton);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(split, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        submitCommentButton.addActionListener(this);
        manageCommentButton.addActionListener(this);
        refreshButton.addActionListener(this);
        backButton.addActionListener(this);
        clearFilterButton.addActionListener(this);

        filterMonthBox.addActionListener(e -> {
            refreshDayBox();
            loadAppointments();
        });
        filterYearBox.addActionListener(e -> {
            refreshDayBox();
            loadAppointments();
        });
        filterDayBox.addActionListener(e -> {
            if (!updatingDays) {
                loadAppointments();
            }
        });
        filterStatusBox.addActionListener(e -> loadAppointments());
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadAppointments();
            }
        });

        loadAppointments();
        frame.setVisible(true);
    }

    private void setupFilters() {
        filterYearBox.addItem("All");
        filterMonthBox.addItem("All");
        filterStatusBox.addItem("All");
        for (int y = 2025; y <= 2035; y++) {
            filterYearBox.addItem(String.valueOf(y));
        }
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        for (String m : months) {
            filterMonthBox.addItem(m);
        }
        filterStatusBox.addItem("Scheduled");
        filterStatusBox.addItem("Completed");
        rebuildDayBox(31);
    }

    private void refreshDayBox() {
        String month = (String) filterMonthBox.getSelectedItem();
        String yearStr = (String) filterYearBox.getSelectedItem();
        if ("All".equals(month)) {
            rebuildDayBox(31);
            return;
        }
        int max;
        if ("All".equals(yearStr)) {
            switch (month) {
                case "February":
                    max = 28;
                    break;
                case "April":
                case "June":
                case "September":
                case "November":
                    max = 30;
                    break;
                default:
                    max = 31;
            }
        } else {
            max = LookupService.getDaysInMonth(month, Integer.parseInt(yearStr));
        }
        rebuildDayBox(max);
    }

    private void rebuildDayBox(int max) {
        updatingDays = true;
        String prev = (String) filterDayBox.getSelectedItem();
        filterDayBox.removeAllItems();
        filterDayBox.addItem("All");
        for (int d = 1; d <= max; d++) {
            filterDayBox.addItem(String.valueOf(d));
        }
        if (prev != null) {
            filterDayBox.setSelectedItem(prev);
        } else {
            filterDayBox.setSelectedItem("All");
        }
        updatingDays = false;
    }

    private void resetFilters() {
        filterYearBox.setSelectedIndex(0);
        filterMonthBox.setSelectedIndex(0);
        filterStatusBox.setSelectedIndex(0);
        searchField.setText("");
        rebuildDayBox(31);
        filterDayBox.setSelectedItem("All");
    }

    private void loadAppointments() {
        listModel.clear();
        detailArea.setText("");
        selectedAppointment = null;
        String yearF = (String) filterYearBox.getSelectedItem();
        String monthF = (String) filterMonthBox.getSelectedItem();
        String dayF = (String) filterDayBox.getSelectedItem();
        String statusF = (String) filterStatusBox.getSelectedItem();
        String keyword = searchField.getText().trim().toLowerCase();

        for (Appointment a : DataStore.allAppointments) {
            if (!a.getCustomerID().equals(userID)) {
                continue;
            }
            String[] parts = a.getDate().split(" ");
            if (!"All".equals(yearF) && !parts[2].equals(yearF)) {
                continue;
            }
            if (!"All".equals(monthF) && !parts[1].equals(monthF)) {
                continue;
            }
            if (!"All".equals(dayF) && !parts[0].equals(dayF)) {
                continue;
            }
            if (!"All".equals(statusF) && !a.getStatus().equalsIgnoreCase(statusF)) {
                continue;
            }
            String serviceName = LookupService.getServiceNameByID(a.getServiceID());
            String searchText = (a.getAppointmentID() + " " + serviceName + " " + a.getDate() + " " + a.getStatus()).toLowerCase();
            if (!keyword.isEmpty() && !searchText.contains(keyword)) {
                continue;
            }
            listModel.addElement(a.getAppointmentID() + " - " + serviceName + " (" + a.getDate() + ") - " + a.getStatus());
        }
        if (listModel.isEmpty()) {
            detailArea.setText("No matching appointments.");
        }
    }

    private void showDetails() {
        String sel = appointmentList.getSelectedValue();
        if (sel != null) {
            String id = sel.split(" - ")[0];
            Appointment a = LookupService.getAppointmentByID(id);
            if (a != null) {
                selectedAppointment = a;
                Service s = LookupService.getServiceByID(a.getServiceID());

                String staffID = a.getCounterStaffID();
                String staffName = (staffID == null || staffID.isEmpty()) ? "N/A" : LookupService.getStaffNameByID(staffID);

                detailArea.setText(
                        a.getAppointmentSummary() +
                        "\n----------------\n" +
                        "Service        : " + (s != null ? s.getServiceInfo() : "Unknown") +
                        "\nTechnician(s)  : " + LookupService.getTechnicianNames(a.getTechnicianIDs()) +
                        "\nCounter Staff  : " + staffName +
                        "\nDate           : " + a.getDate() +
                        "\nTime           : " + a.getFullTimeRange() +
                        "\nService Fee    : " + (s != null ? s.getFormattedPrice() : "Unknown") +
                        "\n\n--- Rating ---\n" + LookupService.getRatingDisplay(a.getAppointmentID()) +
                        "\n\n--- Technician Feedback History ---\n" + LookupService.getTechnicianFeedbackHistory(a.getAppointmentID()) +
                        "\n\n--- Your Comment History ---\n" + LookupService.getCustomerCommentHistory(a.getAppointmentID(), userID));
                
                refreshRatingPanel();
            }
        } else {
            ratingPanel.setVisible(false);
        }
    }

    private void setupRatingPanel() {
        ratingPanel = new JPanel();
        ratingPanel.setLayout(new BoxLayout(ratingPanel, BoxLayout.Y_AXIS));
        ratingPanel.setBorder(BorderFactory.createTitledBorder("Your Appointment Rating"));
        ratingPanel.setBackground(new Color(245, 245, 245));
        ratingPanel.setVisible(false);

        JPanel innerStarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        innerStarPanel.setOpaque(false);
        starButtons = new JButton[5];
        for (int i = 0; i < 5; i++) {
            final int val = i + 1;
            starButtons[i] = new JButton("☆");
            starButtons[i].setFont(new Font("Serif", Font.PLAIN, 28));
            starButtons[i].setBorderPainted(false);
            starButtons[i].setContentAreaFilled(false);
            starButtons[i].setFocusPainted(false);
            starButtons[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            starButtons[i].addActionListener(e -> handleStarClick(val));
            innerStarPanel.add(starButtons[i]);
        }

        ratingStatusLabel = new JLabel("Click a star to rate", SwingConstants.CENTER);
        ratingStatusLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        ratingStatusLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        cancelRatingButton = new JButton("Cancel My Rating");
        cancelRatingButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        cancelRatingButton.setFont(new Font("Arial", Font.PLAIN, 11));
        cancelRatingButton.setMargin(new Insets(2, 5, 2, 5));
        cancelRatingButton.addActionListener(e -> handleCancelRating());

        ratingPanel.add(innerStarPanel);
        ratingPanel.add(ratingStatusLabel);
        ratingPanel.add(Box.createVerticalStrut(5));
        ratingPanel.add(cancelRatingButton);
        ratingPanel.add(Box.createVerticalStrut(5));
    }

    private void refreshRatingPanel() {
        if (selectedAppointment == null || !"Completed".equalsIgnoreCase(selectedAppointment.getStatus())) {
            ratingPanel.setVisible(false);
            return;
        }

        ratingPanel.setVisible(true);
        Rating r = RatingController.getRatingByAppointmentID(selectedAppointment.getAppointmentID());
        int val = (r != null) ? r.getRatingValue() : 0;
        
        for (int i = 0; i < 5; i++) {
            starButtons[i].setText(i < val ? "★" : "☆");
            starButtons[i].setForeground(i < val ? new Color(255, 180, 0) : Color.GRAY);
        }
        
        if (r != null) {
            ratingStatusLabel.setText("You rated this: " + val + " / 5 (Click to change)");
            cancelRatingButton.setVisible(true);
        } else {
            ratingStatusLabel.setText("Click a star to rate this service");
            cancelRatingButton.setVisible(false);
        }
        ratingPanel.revalidate();
        ratingPanel.repaint();
    }

    private void handleCancelRating() {
        if (selectedAppointment == null) return;
        Rating r = RatingController.getRatingByAppointmentID(selectedAppointment.getAppointmentID());
        if (r != null) {
            int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to remove your rating?", "Confirm Cancel", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                RatingController.deleteRating(r.getRatingID());
                FileHandler.writeSystemLog("(" + userRole + ")" + userID + " cancelled rating for appointment [" + selectedAppointment.getAppointmentID() + "]");
                JOptionPane.showMessageDialog(frame, "Rating removed.");
                showDetails();
            }
        }
    }

    private void handleStarClick(int val) {
        if (selectedAppointment == null) return;
        
        Rating existing = RatingController.getRatingByAppointmentID(selectedAppointment.getAppointmentID());
        if (existing == null) {
            RatingController.addRating(selectedAppointment.getAppointmentID(), val, userID);
            FileHandler.writeSystemLog("(" + userRole + ")" + userID + " rated appointment [" + selectedAppointment.getAppointmentID() + "] value: " + val);
            JOptionPane.showMessageDialog(frame, "Thank you for your rating!");
        } else {
            RatingController.updateRating(existing, val);
            FileHandler.writeSystemLog("(" + userRole + ")" + userID + " updated rating for appointment [" + selectedAppointment.getAppointmentID() + "] to value: " + val);
            JOptionPane.showMessageDialog(frame, "Rating updated!");
        }
        showDetails();
    }

    private void openManageComments() {
        if (selectedAppointment == null) {
            JOptionPane.showMessageDialog(frame, "Select an appointment first.");
        } else {
            if (!selectedAppointment.getStatus().equalsIgnoreCase("Completed")) {
                JOptionPane.showMessageDialog(frame, "You can only manage comments for COMPLETED appointments.");
            } else {
                frame.dispose();
                ManageMyComments manageMyComments = new ManageMyComments();
                manageMyComments.openPage(userID, userRole, selectedAppointment);
            }
        }
    }

    private void submitComment() {
        if (selectedAppointment == null) {
            JOptionPane.showMessageDialog(frame, "Select an appointment first");
        } else {
            if (!selectedAppointment.getStatus().equalsIgnoreCase("Completed")) {
                JOptionPane.showMessageDialog(frame, "You can only comment on Completed appointments");
            } else {
                JFrame cf = new JFrame("Submit Comment");
                cf.setSize(520, 420);
                cf.setLayout(new BorderLayout(10, 10));
                cf.setLocationRelativeTo(frame);

                JPanel infoPanel = new JPanel(new GridLayout(3, 1));
                infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
                infoPanel.add(new JLabel("Appointment ID: " + selectedAppointment.getAppointmentID()));
                infoPanel.add(new JLabel("Technician(s): " + LookupService.getTechnicianNames(selectedAppointment.getTechnicianIDs())));
                infoPanel.add(new JLabel("Counter Staff: " + LookupService.getStaffNameByID(selectedAppointment.getCounterStaffID())));

                JTextArea historyArea = new JTextArea();
                historyArea.setEditable(false);
                historyArea.setLineWrap(true);
                historyArea.setWrapStyleWord(true);
                historyArea.setText(LookupService.getCustomerCommentHistory(selectedAppointment.getAppointmentID(), userID));

                JTextArea newCommentArea = new JTextArea();
                newCommentArea.setLineWrap(true);
                newCommentArea.setWrapStyleWord(true);

                JPanel historyPanel = new JPanel(new BorderLayout());
                historyPanel.add(new JLabel("Previous Comment History:"), BorderLayout.NORTH);
                historyPanel.add(new JScrollPane(historyArea), BorderLayout.CENTER);

                JPanel inputPanel = new JPanel(new BorderLayout());
                inputPanel.add(new JLabel("New Comment:"), BorderLayout.NORTH);
                inputPanel.add(new JScrollPane(newCommentArea), BorderLayout.CENTER);

                JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
                centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                centerPanel.add(historyPanel);
                centerPanel.add(inputPanel);

                JButton saveBtn = new JButton("Save");
                JButton cancelBtn = new JButton("Cancel");
                JPanel btnPanel = new JPanel();
                btnPanel.add(saveBtn);
                btnPanel.add(cancelBtn);

                cf.add(infoPanel, BorderLayout.NORTH);
                cf.add(centerPanel, BorderLayout.CENTER);
                cf.add(btnPanel, BorderLayout.SOUTH);

                cancelBtn.addActionListener(e -> cf.dispose());
                saveBtn.addActionListener(e -> {
                    String comment = newCommentArea.getText().trim();
                    if (ValidationUtils.validateComment(comment)) {
                        CommentController.addComment(selectedAppointment.getAppointmentID(), userID, comment);
                        FileHandler.writeSystemLog("(" + userRole + ")" + userID + " submitted comment for appointment [" + selectedAppointment.getAppointmentID() + "] content: " + comment);
                        JOptionPane.showMessageDialog(cf, "Comment submitted successfully");
                        cf.dispose();
                        showDetails();
                    }
                });
                cf.setVisible(true);
            }
        }
    }
}