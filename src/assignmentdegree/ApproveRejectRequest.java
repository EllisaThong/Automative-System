package assignmentdegree;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

public class ApproveRejectRequest implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == approveButton) {
            processRequest(true);
        } else if (e.getSource() == rejectButton) {
            processRequest(false);
        } else if (e.getSource() == backButton) {
            frame.dispose();
            if (userRole.equals("Manager")) {
                ManagerFunction managerFunction = new ManagerFunction();
                managerFunction.openManagerFunction(managerID, userRole);
            } else {
                CounterStaffFunction counterStaffFunction = new CounterStaffFunction();
                counterStaffFunction.openCounterStaffFunction(managerID, userRole);
            }
        }
    }

    private JFrame frame;
    private DefaultListModel<String> listModel;
    private JList<String> requestList;
    private JTextArea detailArea;
    private JButton approveButton, rejectButton, backButton;
    private JComboBox<String> roleSelector;
    private JTextField searchField;

    private String selectedLine;
    private String managerID, userRole;

    public void openApproveRejectRequestPage(String managerID, String role) {
        this.managerID = managerID;
        this.userRole = role;
        
        FileHandler.writeSystemLog("(" + role + ")" + managerID + " opened the approve reject register request " + role + " function.");

        frame = new JFrame("Approve / Reject Registration");
        frame.setSize(700, 520);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        requestList = new JList<>(listModel);
        detailArea = new JTextArea();
        detailArea.setEditable(false);
        requestList.addListSelectionListener(e -> showDetails());

        approveButton = new JButton("Approve");
        rejectButton = new JButton("Reject");
        backButton = new JButton("Back");

        if (role.equals("Manager")) {
            roleSelector = new JComboBox<>(new String[]{"All", "Counter Staff", "Technician", "Manager"});
        } else {
            roleSelector = new JComboBox<>(new String[]{"All", "Customer"});
        }

        roleSelector.addActionListener(e -> loadRequests());

        searchField = new JTextField(15);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadRequests();
            }
        });

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Filter Role:"));
        topPanel.add(roleSelector);
        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(requestList), new JScrollPane(detailArea));
        split.setDividerLocation(220);

        JPanel btnPanel = new JPanel();
        btnPanel.add(approveButton);
        btnPanel.add(rejectButton);
        btnPanel.add(backButton);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(split, BorderLayout.CENTER);
        frame.add(btnPanel, BorderLayout.SOUTH);

        approveButton.addActionListener(this);
        rejectButton.addActionListener(this);
        backButton.addActionListener(this);

        loadRequests();
        frame.setVisible(true);
    }

    private void loadRequests() {
        listModel.clear();
        String selectedRole = (String) roleSelector.getSelectedItem();
        String keyword = searchField.getText().toLowerCase();

        try (BufferedReader reader = new BufferedReader(new FileReader("waitingList.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] d = line.split(",");
                String waitingID = d[0];
                String name = d[1];
                String role = d[5];

                boolean allowed;
                if (userRole.equals("Manager")) {
                    allowed = role.equals("Counter Staff") || role.equals("Technician") || role.equals("Manager");
                } else {
                    allowed = role.equals("Customer");
                }

                boolean matchSearch = keyword.isEmpty() || (waitingID + " " + name).toLowerCase().contains(keyword);

                if (allowed && (selectedRole.equals("All") || role.equals(selectedRole)) && matchSearch) {
                    listModel.addElement(waitingID + " - " + name + " (" + role + ")");
                }
            }
            detailArea.setText("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error reading waiting list");
        }
    }

    private void showDetails() {
        String sel = requestList.getSelectedValue();
        if (sel != null) {
            String waitingID = sel.split(" - ")[0];

            try (BufferedReader reader = new BufferedReader(new FileReader("waitingList.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] d = line.split(",");
                    if (d[0].equals(waitingID)) {
                        selectedLine = line;
                        detailArea.setText(
                                "Waiting ID: " + d[0] + "\nName: " + d[1] +
                                        "\nEmail: " + d[3] + "\nContact: " + d[4] + "\nRole: " + d[5]);
                        break;
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Error reading request");
            }
        }
    }

    private void processRequest(boolean approve) {
        if (selectedLine == null) {
            JOptionPane.showMessageDialog(frame, "Please select a request");
        } else {
            String[] d = selectedLine.split(",");
            if (d.length < 6) {
                JOptionPane.showMessageDialog(frame, "Invalid request record. Please check waitingList.txt");
                return;
            }
            String name = d[1];
            String password = d[2];
            String email = d[3];
            String contact = d[4];
            String role = d[5];

            if (userRole.equals("Counter Staff") && !role.equals("Customer")) {
                JOptionPane.showMessageDialog(frame, "You are not allowed to process this request!");
            } else if (userRole.equals("Manager") && role.equals("Customer")) {
                JOptionPane.showMessageDialog(frame, "Manager does not handle customer requests!");
            } else {
                if (approve) {
                    String registerDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    UserController.addUser(name, password, email, contact, role, registerDate);
                    FileHandler.writeSystemLog("(Manager) " + managerID + " approve a registration request: " + email + "(" + role + ")");
                    JOptionPane.showMessageDialog(frame, "User Approved");
                } else {
                    JOptionPane.showMessageDialog(frame, "User Rejected");
                    FileHandler.writeSystemLog("(Manager) " + managerID + " reject a registration request: " + email + "(" + role + ")");
                }

                removeFromWaitingList();
                FileHandler.resequenceWaitingIDs();
                selectedLine = null;
                detailArea.setText("");
                loadRequests();
            }
        }
    }

    private void removeFromWaitingList() {
        File input = new File("waitingList.txt");
        File temp = new File("temp_waiting.txt");
        try (BufferedReader r = new BufferedReader(new FileReader(input));
             BufferedWriter w = new BufferedWriter(new FileWriter(temp))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (!line.equals(selectedLine)) {
                    w.write(line);
                    w.newLine();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error updating waiting list");
        }
        input.delete();
        temp.renameTo(input);
    }
}