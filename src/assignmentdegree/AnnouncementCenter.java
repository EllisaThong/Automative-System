package assignmentdegree;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class AnnouncementCenter {
    private JFrame frame;
    private DefaultListModel<String> listModel;
    private JList<String> list;
    private JTextArea detailsArea;

    private JComboBox<String> targetBox;
    private JComboBox<String> roleBox;
    private JComboBox<String> userBox;
    private JTextField userSearchField;
    private JTextArea messageArea;
    private JButton sendButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton backButton;
    private ArrayList<Announcement> displayedAnnouncements = new ArrayList<>();

    private String currentUserID;
    private String currentUserRole;

    public void openPage(String userID, String userRole) {
        this.currentUserID = userID;
        this.currentUserRole = userRole;
        
        FileHandler.writeSystemLog("(" + userRole + ")" + userID + " opened the anouncement center " + userRole + " function.");

        frame = new JFrame("Announcement Center");
        frame.setSize(1000, 650);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));
        ((JComponent) frame.getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        listModel = new DefaultListModel<>();
        list = new JList<>(listModel);
        list.setFont(new Font("Arial", Font.PLAIN, 13));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(e -> showDetails());
        JScrollPane listScroll = new JScrollPane(list);
        listScroll.setBorder(BorderFactory.createTitledBorder("Announcements"));
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setBorder(BorderFactory.createTitledBorder("Details"));

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                listScroll,
                detailsScroll
        );
        split.setDividerLocation(350);
        split.setResizeWeight(0.42);
        split.setContinuousLayout(true);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        if ("Manager".equalsIgnoreCase(userRole)) {
            topPanel.add(buildSendPanel(), BorderLayout.CENTER);
        } else {
            JLabel lbl = new JLabel("Announcements for " + userRole);
            lbl.setFont(new Font("Arial", Font.BOLD, 16));
            lbl.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            topPanel.add(lbl, BorderLayout.WEST);
        }

        refreshButton = new JButton("Refresh");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        backButton = new JButton("Back");
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(refreshButton);
        if ("Manager".equalsIgnoreCase(userRole)) {
            bottomPanel.add(editButton);
            bottomPanel.add(deleteButton);
        }
        bottomPanel.add(backButton);

        refreshButton.addActionListener(e -> loadAnnouncements());
        editButton.addActionListener(e -> editSelectedAnnouncement());
        deleteButton.addActionListener(e -> deleteSelectedAnnouncement());
        backButton.addActionListener(e -> {
            frame.dispose();
            goBack();
        });

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(split, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        loadAnnouncements();
        frame.setVisible(true);
    }

    private JPanel buildSendPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Send Announcement"),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        targetBox = new JComboBox<>(new String[]{
                "All Users",
                "All Customers",
                "All Counter Staff",
                "All Technicians",
                "Specific User"
        });
        roleBox = new JComboBox<>(new String[]{
                "Customer", "Counter Staff", "Technician"
        });
        userSearchField = new JTextField();
        userSearchField.setToolTipText("Search by user ID or name");
        userBox = new JComboBox<>();
        userBox.setMaximumRowCount(12);
        messageArea = new JTextArea(3, 40);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane messageScroll = new JScrollPane(messageArea);
        sendButton = new JButton("Send");

        targetBox.addActionListener(e -> refreshTargetControls());
        roleBox.addActionListener(e -> rebuildUserBox());
        userSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                rebuildUserBox();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                rebuildUserBox();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                rebuildUserBox();
            }
        });
        sendButton.addActionListener(e -> sendAnnouncement());

        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel("Target"), c);
        c.gridx = 1;
        panel.add(targetBox, c);

        c.gridx = 0;
        c.gridy = 1;
        panel.add(new JLabel("Role"), c);
        c.gridx = 1;
        panel.add(roleBox, c);

        c.gridx = 0;
        c.gridy = 2;
        panel.add(new JLabel("Search User"), c);
        c.gridx = 1;
        panel.add(userSearchField, c);

        c.gridx = 0;
        c.gridy = 3;
        panel.add(new JLabel("User"), c);
        c.gridx = 1;
        panel.add(userBox, c);

        c.gridx = 0;
        c.gridy = 4;
        panel.add(new JLabel("Message"), c);
        c.gridx = 1;
        panel.add(messageScroll, c);

        c.gridx = 1;
        c.gridy = 5;
        c.anchor = GridBagConstraints.EAST;
        panel.add(sendButton, c);

        refreshTargetControls();
        return panel;
    }

    private void refreshTargetControls() {
        boolean specificUser = "Specific User".equals(targetBox.getSelectedItem());
        roleBox.setEnabled(specificUser);
        userSearchField.setEnabled(specificUser);
        userBox.setEnabled(specificUser);
        if (!specificUser) {
            userSearchField.setText("");
        }
        rebuildUserBox();
    }

    private void rebuildUserBox() {
        if (userBox == null) {
            return;
        }
        userBox.removeAllItems();
        String role = roleBox == null ? "Customer" : (String) roleBox.getSelectedItem();
        if (role == null) {
            return;
        }
        String keyword = userSearchField == null ? "" : userSearchField.getText().trim().toLowerCase();
        int count = 0;
        for (User u : usersByRole(role)) {
            String entry = u.getUserID() + " - " + u.getName();
            if (keyword.isEmpty() || entry.toLowerCase().contains(keyword)) {
                userBox.addItem(entry);
                count++;
            }
        }
        if (count == 0) {
            userBox.addItem("No available user");
            userBox.setEnabled(false);
        } else {
            userBox.setEnabled(true);
        }
    }

    private ArrayList<User> usersByRole(String role) {
        ArrayList<User> users = new ArrayList<>();
        if ("Customer".equals(role)) {
            users.addAll(DataStore.allCustomers);
        } else if ("Counter Staff".equals(role)) {
            users.addAll(DataStore.allCounterStaff);
        } else if ("Technician".equals(role)) {
            users.addAll(DataStore.allTechnicians);
        } else if ("Manager".equals(role)) {
            users.addAll(DataStore.allManagers);
        }
        return users;
    }

    private void sendAnnouncement() {
        String message = messageArea.getText() == null ? "" : messageArea.getText().trim();
        if (!ValidationUtils.validateAnnouncementMessage(message)) {
            return;
        }

        String target = (String) targetBox.getSelectedItem();
        try {
            if ("Specific User".equals(target)) {
                String userEntry = (String) userBox.getSelectedItem();
                if (userEntry == null || !userEntry.contains(" - ") || "No available user".equals(userEntry)) {
                    JOptionPane.showMessageDialog(frame, "Please select a specific user.");
                    return;
                }
                String userID = userEntry.split(" - ")[0];
                AnnouncementController.sendToUser(currentUserID, userID, message);
                FileHandler.writeSystemLog("(Manager)" + currentUserID + " sent announcement to user " + userID + " at " + getCurrentTimestamp());
            } else if ("All Users".equals(target)) {
                AnnouncementController.sendToAllUsers(currentUserID, message);
                FileHandler.writeSystemLog("(Manager)" + currentUserID + " sent announcement to all users at " + getCurrentTimestamp());
            } else {
                String role = mapTargetToRole(target);
                AnnouncementController.sendToRole(currentUserID, role, message);
                FileHandler.writeSystemLog("(Manager)" + currentUserID + " sent announcement to role " + role + " at " + getCurrentTimestamp());
            }
            JOptionPane.showMessageDialog(frame, "Announcement sent successfully.");
            messageArea.setText("");
            loadAnnouncements();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Failed to send announcement.");
        }
    }

    private String mapTargetToRole(String target) {
        if ("All Users".equals(target)) {
            return "";
        }
        if ("All Customers".equals(target)) {
            return "Customer";
        }
        if ("All Counter Staff".equals(target)) {
            return "Counter Staff";
        }
        if ("All Technicians".equals(target)) {
            return "Technician";
        }
        return "Customer";
    }

    private void loadAnnouncements() {
        listModel.clear();
        detailsArea.setText("");
        if ("Manager".equalsIgnoreCase(currentUserRole)) {
            displayedAnnouncements = AnnouncementController.getAnnouncementsSentByManager(currentUserID);
        } else {
            displayedAnnouncements = AnnouncementController.getAnnouncementsForUser(currentUserID, currentUserRole);
        }
        for (Announcement a : displayedAnnouncements) {
            String from = LookupService.getUserNameByID(a.getSenderID());
            listModel.addElement(a.getAnnouncementID() + " | " + a.getDateTime() + " | From: " + from);
        }
        if (listModel.isEmpty()) {
            detailsArea.setText("No announcements available.");
        }
    }

    private void showDetails() {
        String selected = list.getSelectedValue();
        if (selected == null) {
            return;
        }
        String id = selected.split("\\|")[0].trim();
        for (Announcement a : displayedAnnouncements) {
            if (a.getAnnouncementID().equals(id)) {
                String senderName = LookupService.getUserNameByID(a.getSenderID());
                String target;
                if ("ALL_USERS".equalsIgnoreCase(a.getTargetType())) {
                    target = "All Users";
                } else if ("ROLE".equalsIgnoreCase(a.getTargetType())) {
                    target = "All " + a.getTargetRole();
                } else {
                    target = a.getTargetUserID() + " - " + LookupService.getUserNameByID(a.getTargetUserID());
                }
                detailsArea.setText(
                        "Announcement ID : " + a.getAnnouncementID() + "\n"
                                + "Date/Time      : " + a.getDateTime() + "\n"
                                + "From           : " + senderName + " (" + a.getSenderID() + ")\n"
                                + "Target         : " + target + "\n"
                                + "\nMessage:\n"
                                + a.getMessage()
                );
                break;
            }
        }
    }

    private Announcement getSelectedAnnouncement() {
        String selected = list.getSelectedValue();
        if (selected == null) {
            return null;
        }
        String id = selected.split("\\|")[0].trim();
        return AnnouncementController.getByID(id);
    }

    private void editSelectedAnnouncement() {
        Announcement selected = getSelectedAnnouncement();
        if (selected == null) {
            JOptionPane.showMessageDialog(frame, "Select an announcement to edit.");
            return;
        }
        if (!currentUserID.equals(selected.getSenderID())) {
            JOptionPane.showMessageDialog(frame, "You can only edit announcements you sent.");
            return;
        }
        JTextArea editArea = new JTextArea(selected.getMessage(), 8, 35);
        editArea.setLineWrap(true);
        editArea.setWrapStyleWord(true);
        int result = JOptionPane.showConfirmDialog(
                frame,
                new JScrollPane(editArea),
                "Edit Announcement " + selected.getAnnouncementID(),
                JOptionPane.OK_CANCEL_OPTION
        );
        if (result == JOptionPane.OK_OPTION) {
            String newMessage = editArea.getText() == null ? "" : editArea.getText().trim();
            if (newMessage.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Message cannot be empty.");
                return;
            }
            if (AnnouncementController.editMessage(selected.getAnnouncementID(), newMessage)) {
                FileHandler.writeSystemLog("(Manager)" + currentUserID + " edited announcement " + selected.getAnnouncementID());
                loadAnnouncements();
                JOptionPane.showMessageDialog(frame, "Announcement updated.");
            }
        }
    }

    private void deleteSelectedAnnouncement() {
        Announcement selected = getSelectedAnnouncement();
        if (selected == null) {
            JOptionPane.showMessageDialog(frame, "Select an announcement to delete.");
            return;
        }
        if (!currentUserID.equals(selected.getSenderID())) {
            JOptionPane.showMessageDialog(frame, "You can only delete announcements you sent.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                frame,
                "Delete announcement " + selected.getAnnouncementID() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            if (AnnouncementController.deleteAnnouncement(selected.getAnnouncementID())) {
                FileHandler.writeSystemLog("(Manager)" + currentUserID + " deleted announcement " + selected.getAnnouncementID());
                loadAnnouncements();
                JOptionPane.showMessageDialog(frame, "Announcement deleted.");
            }
        }
    }

    private void goBack() {
        if ("Manager".equalsIgnoreCase(currentUserRole)) {
            ManagerFunction m = new ManagerFunction();
            m.openManagerFunction(currentUserID, currentUserRole);
        } else if ("Counter Staff".equalsIgnoreCase(currentUserRole)) {
            CounterStaffFunction cs = new CounterStaffFunction();
            cs.openCounterStaffFunction(currentUserID, currentUserRole);
        } else if ("Technician".equalsIgnoreCase(currentUserRole)) {
            TechnicianFunction t = new TechnicianFunction();
            t.openTechnicianFunction(currentUserID, currentUserRole);
        } else if ("Customer".equalsIgnoreCase(currentUserRole)) {
            CustomerFunction c = new CustomerFunction();
            c.openCustomerFunction(currentUserID, currentUserRole);
        }
    }
    
    private String getCurrentTimestamp() {
        return LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
