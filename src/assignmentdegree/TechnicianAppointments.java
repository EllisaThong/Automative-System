package assignmentdegree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class TechnicianAppointments implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == completeButton) {
            markCompleted();
        } else if (e.getSource() == feedbackButton) {
            provideFeedback();
        } else if (e.getSource() == manageCommentButton) {
            openManageComments();
        } else if (e.getSource() == refreshButton) {
            loadAppointments();
        } else if (e.getSource() == clearFilterButton) {
            resetFilters();
            loadAppointments();
        } else if (e.getSource() == backButton) {
            frame.dispose();
            TechnicianFunction technicianFunction = new TechnicianFunction();
            technicianFunction.openTechnicianFunction(userID, userRole);
        }
    }

    private JFrame frame;
    private DefaultListModel<String> listModel;
    private JList<String> appointmentList;
    private JTextArea detailArea;
    private JButton completeButton, feedbackButton, manageCommentButton, refreshButton, backButton, clearFilterButton;
    private JComboBox<String> filterYearBox, filterMonthBox, filterStatusBox, filterDayBox;
    private JTextField searchField;
    private Appointment selectedAppointment;
    private String userID, userRole;
    private boolean updatingDays = false;

    public void openPage(String userID, String userRole) {
        this.userID = userID;
        this.userRole = userRole;
        
        FileHandler.writeSystemLog("(" + userRole + ")" + userID + " opened the technician appointment page " + userRole + " function.");

        frame = new JFrame("My Assigned Appointments");
        frame.setSize(950, 550);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null);

        listModel = new DefaultListModel<>();
        appointmentList = new JList<>(listModel);
        appointmentList.addListSelectionListener(e -> showDetails());

        detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        completeButton = new JButton("Mark as Completed");
        feedbackButton = new JButton("Provide Feedback");
        manageCommentButton = new JButton("Manage My Feedback");
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

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(appointmentList), new JScrollPane(detailArea));
        split.setDividerLocation(400);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(completeButton);
        bottomPanel.add(feedbackButton);
        bottomPanel.add(manageCommentButton);
        bottomPanel.add(backButton);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(split, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        completeButton.addActionListener(this);
        feedbackButton.addActionListener(this);
        manageCommentButton.addActionListener(this);
        refreshButton.addActionListener(this);
        clearFilterButton.addActionListener(this);
        backButton.addActionListener(this);

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
        filterStatusBox.addItem("Scheduled");
        filterStatusBox.addItem("Completed");
        for (int y = 2025; y <= 2035; y++) {
            filterYearBox.addItem(String.valueOf(y));
        }
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        for (String m : months) {
            filterMonthBox.addItem(m);
        }
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
        }
        updatingDays = false;
    }

    private void resetFilters() {
        filterYearBox.setSelectedIndex(0);
        filterMonthBox.setSelectedIndex(0);
        filterStatusBox.setSelectedIndex(0);
        searchField.setText("");
        rebuildDayBox(31);
    }

    private void loadAppointments() {
        listModel.clear();
        selectedAppointment = null;
        String yearF = (String) filterYearBox.getSelectedItem();
        String monthF = (String) filterMonthBox.getSelectedItem();
        String dayF = (String) filterDayBox.getSelectedItem();
        String statusF = (String) filterStatusBox.getSelectedItem();
        String keyword = searchField.getText().toLowerCase();

        ArrayList<Appointment> filteredAppts = new ArrayList<>();

        for (Appointment a : DataStore.allAppointments) {
            if (!a.isAssignedTo(userID)) {
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
            String customerName = LookupService.getCustomerNameByID(a.getCustomerID());
            if (!keyword.isEmpty() && !(a.getAppointmentID() + " " + customerName).toLowerCase().contains(keyword)) {
                continue;
            }
            filteredAppts.add(a);
            listModel.addElement(a.getAppointmentID() + " - " + customerName + " (" + a.getDate() + ") - " + a.getStatus());
        }

        Technician t = LookupService.getTechnicianByID(userID);
        if (t != null) {
            int assigned = t.getAssignedAppointmentsCount(filteredAppts);
            int completed = t.getCompletedAppointmentsCount(filteredAppts);
            detailArea.setText("--- Workload Summary (Filtered) ---\n" +
                               "Assigned in this Period: " + assigned + "\n" +
                               "Completed in this Period: " + completed + "\n\n" +
                               "Select an appointment to see details.");
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

                detailArea.setText(
                        a.getAppointmentSummary() +
                        "\n----------------\n" +
                        "Customer        : " + LookupService.getCustomerNameByID(a.getCustomerID()) +
                        "\nService         : " + (s != null ? s.getServiceInfo() : "Unknown") +
                        "\nCounter Staff   : " + LookupService.getStaffNameByID(a.getCounterStaffID()) +
                        "\nDate            : " + a.getDate() +
                        "\nTime            : " + a.getFullTimeRange() +
                        "\n\n--- Customer Rating ---\n" + LookupService.getRatingDisplay(a.getAppointmentID()) +
                        "\n\n--- Customer Comments ---\n" + LookupService.getCommentsForAppointment(a.getAppointmentID(), false) +
                        "\n\n--- Feedback History ---\n" + LookupService.getTechnicianFeedbackHistory(a.getAppointmentID()));
            }
        }
    }

    private void markCompleted() {
        if (selectedAppointment == null) {
            JOptionPane.showMessageDialog(frame, "Select an appointment first");
        } else if (selectedAppointment.getStatus().equalsIgnoreCase("Completed")) {
            JOptionPane.showMessageDialog(frame, "Already completed");
        } else {
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Mark " + selectedAppointment.getAppointmentID() + " as Completed?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = AppointmentController.markCompleted(selectedAppointment);
                if (success) {
                    FileHandler.writeSystemLog("(" + userRole + ")" + userID + " changed appointment [" + selectedAppointment.getAppointmentID() + "] status to Completed");
                    JOptionPane.showMessageDialog(frame, "Appointment marked as Completed and payment record created");
                    loadAppointments();
                } else {
                    JOptionPane.showMessageDialog(frame, "Cannot mark as completed before the appointment end date/time: " + selectedAppointment.getDate() + " " + selectedAppointment.getEndTime(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void provideFeedback() {
        if (selectedAppointment == null) {
            JOptionPane.showMessageDialog(frame, "Select an appointment first");
        } else {
            if (!selectedAppointment.getStatus().equalsIgnoreCase("Completed")) {
                JOptionPane.showMessageDialog(frame, "You can only provide feedback for Completed appointments");
            } else {
                JDialog dialog = new JDialog(frame, "Provide Feedback", true);
                dialog.setSize(550, 520);
                dialog.setLayout(new BorderLayout(10, 10));
                dialog.setLocationRelativeTo(frame);

                JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
                mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                JPanel headerPanel = new JPanel();
                headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
                JLabel titleLabel = new JLabel("Feedback for " + selectedAppointment.getAppointmentID());
                titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
                headerPanel.add(titleLabel);
                headerPanel.add(Box.createVerticalStrut(5));
                headerPanel.add(new JLabel("Customer: " + LookupService.getCustomerNameByID(selectedAppointment.getCustomerID())));
                headerPanel.add(Box.createVerticalStrut(3));
                headerPanel.add(new JLabel("Service: " + LookupService.getServiceNameByID(selectedAppointment.getServiceID())));
                headerPanel.add(Box.createVerticalStrut(10));
                
                JTextArea customerCommentsArea = new JTextArea(4, 20);
                customerCommentsArea.setEditable(false);
                customerCommentsArea.setLineWrap(true);
                customerCommentsArea.setWrapStyleWord(true);
                customerCommentsArea.setText("Customer Comments:\n" + LookupService.getCommentsForAppointment(selectedAppointment.getAppointmentID(), false));
                customerCommentsArea.setBackground(new Color(245, 245, 245));
                JScrollPane customerScroll = new JScrollPane(customerCommentsArea);
                customerScroll.setBorder(BorderFactory.createTitledBorder("Customer Feedback"));
                headerPanel.add(customerScroll);

                JTextArea feedbackArea = new JTextArea(6, 20);
                feedbackArea.setLineWrap(true);
                feedbackArea.setWrapStyleWord(true);
                feedbackArea.setFont(new Font("Arial", Font.PLAIN, 14));

                JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                JButton saveBtn = new JButton("Save Feedback");
                JButton cancelBtn = new JButton("Cancel");
                btnPanel.add(saveBtn);
                btnPanel.add(cancelBtn);

                mainPanel.add(headerPanel, BorderLayout.NORTH);
                mainPanel.add(new JScrollPane(feedbackArea), BorderLayout.CENTER);
                mainPanel.add(btnPanel, BorderLayout.SOUTH);
                dialog.add(mainPanel);

                cancelBtn.addActionListener(e -> dialog.dispose());
                saveBtn.addActionListener(e -> {
                    String feedback = feedbackArea.getText().trim();
                    if (ValidationUtils.validateComment(feedback)) {
                        CommentController.addComment(selectedAppointment.getAppointmentID(), userID, feedback);
                        FileHandler.writeSystemLog("(" + userRole + ")" + userID + " submitted comment for appointment [" + selectedAppointment.getAppointmentID() + "] content: " + feedback);
                        JOptionPane.showMessageDialog(dialog, "Feedback saved successfully");
                        dialog.dispose();
                        showDetails();
                    }
                });
                dialog.setVisible(true);
            }
        }
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
}