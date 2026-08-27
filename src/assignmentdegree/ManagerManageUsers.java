package assignmentdegree;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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

public class ManagerManageUsers implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == deleteButton) {
            deleteUser();
        } else if (e.getSource() == editButton) {
            if (selectedUser == null) {
                JOptionPane.showMessageDialog(frame, "Select user first");
            } else {
                frame.dispose();
                ManagerEditUser managerEditUser = new ManagerEditUser();
                managerEditUser.openManagerEditUserPage(selectedUser, userID, userRole);
            }
        } else if (e.getSource() == refreshButton) {
            loadUsers();
        } else if (e.getSource() == backButton) {
            frame.dispose();
            if (userRole.equals("Manager")) {
                ManagerFunction managerFunction = new ManagerFunction();
                managerFunction.openManagerFunction(userID, userRole);
            } else {
                CounterStaffFunction counterStaffFunction = new CounterStaffFunction();
                counterStaffFunction.openCounterStaffFunction(userID, userRole);
            }
        }
    }
    
    private JFrame frame;
    private JComboBox<String> roleSelector;
    private JTextField searchField;
    private DefaultListModel<String> listModel;
    private JList<String> userList;
    private JTextArea detailArea;
    private JButton editButton, deleteButton, refreshButton, backButton;
    private User selectedUser;
    private String userID, userRole;

    public void openManageUsersPage(String userID, String role) {
        this.userID = userID;
        this.userRole = role;
        
        FileHandler.writeSystemLog("(" + userRole + ")" + userID + " opened the manage user " + userRole + " function.");

        frame = new JFrame("Manage Users");
        frame.setSize(700, 500);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        roleSelector = role.equals("Manager")
                ? new JComboBox<>(new String[]{"All", "Counter Staff", "Technician", "Manager"})
                : new JComboBox<>(new String[]{"Customer"});
        roleSelector.addActionListener(e -> loadUsers());

        searchField = new JTextField(15);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadUsers();
            }
        });

        listModel = new DefaultListModel<>();
        userList = new JList<>(listModel);
        userList.addListSelectionListener(e -> showDetails());

        detailArea = new JTextArea();
        detailArea.setEditable(false);

        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        refreshButton = new JButton("Refresh");
        backButton = new JButton("Back");

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Role:"));
        topPanel.add(roleSelector);
        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);
        topPanel.add(refreshButton);

        JPanel btnPanel = new JPanel();
        btnPanel.add(editButton);
        btnPanel.add(deleteButton);
        btnPanel.add(backButton);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(userList), new JScrollPane(detailArea));
        split.setDividerLocation(300);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(split, BorderLayout.CENTER);
        frame.add(btnPanel, BorderLayout.SOUTH);

        editButton.addActionListener(this);
        deleteButton.addActionListener(this);
        refreshButton.addActionListener(this);
        backButton.addActionListener(this);

        loadUsers();
        frame.setVisible(true);
    }
    
    private void addUserToList(String id, String name, String keyword) {
        if (keyword.isEmpty() || (id + " " + name).toLowerCase().contains(keyword)) {
            listModel.addElement(id + " - " + name);
        }
    }

    private void loadUsers() {
        listModel.clear();
        detailArea.setText("");
        selectedUser = null;

        String role = (String) roleSelector.getSelectedItem();
        String keyword = searchField.getText().toLowerCase().trim();

        switch (role) {
            case "Customer":
                for (Customer c : DataStore.allCustomers) {
                    addUserToList(c.getUserID(), c.getName(), keyword);
                }
                break;

            case "Counter Staff":
                for (CounterStaff cs : DataStore.allCounterStaff) {
                    addUserToList(cs.getUserID(), cs.getName(), keyword);
                }
                break;

            case "Technician":
                for (Technician t : DataStore.allTechnicians) {
                    addUserToList(t.getUserID(), t.getName(), keyword);
                }
                break;

            case "Manager":
                for (Manager m : DataStore.allManagers) {
                    addUserToList(m.getUserID(), m.getName(), keyword);
                }
                break;

            case "All":
                for (CounterStaff cs : DataStore.allCounterStaff) {
                    addUserToList(cs.getUserID(), cs.getName(), keyword);
                }
                for (Technician t : DataStore.allTechnicians) {
                    addUserToList(t.getUserID(), t.getName(), keyword);
                }
                for (Manager m : DataStore.allManagers) {
                    addUserToList(m.getUserID(), m.getName(), keyword);
                }
                break;
        }
    }

    private void showDetails() {
        String sel = userList.getSelectedValue();
        if (sel == null) return;
        String id   = sel.split(" - ")[0];
        String role = (String) roleSelector.getSelectedItem();
        selectedUser = role.equals("All") ? LookupService.getUserByID(id) : getUserByRole(id, role);

        if (selectedUser != null)
            detailArea.setText(
                    "User ID: " + selectedUser.getUserID() +
                    "\nName: " + selectedUser.getName() +
                    "\nPassword: " + selectedUser.getPassword() +
                    "\nEmail: " + selectedUser.getEmailAddress() +
                    "\nContact: " + selectedUser.getContactNumber() +
                    "\nRegister Date: " + selectedUser.getRegisterDate());
    }

    private User getUserByRole(String id, String role) {
        switch (role) {
            case "Customer":
                for (Customer c : DataStore.allCustomers)
                    if (c.getUserID().equals(id))  {
                        return c;
                    }
                break;
                
            case "Counter Staff": 
                for (CounterStaff cs : DataStore.allCounterStaff) 
                    if (cs.getUserID().equals(id)) {
                        return cs;
                    }
                break;
                
            case "Technician":
                for (Technician t : DataStore.allTechnicians)
                    if (t.getUserID().equals(id)) {
                        return t;
                    }
                break;
                
            case "Manager":
                for (Manager m : DataStore.allManagers)
                    if (m.getUserID().equals(id)) {
                        return m;
                    }
                break;
        }
        return null;
    }

    private void deleteUser() {
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(frame, "Select user first");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete this user?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        UserController.deleteUser(selectedUser.getUserID());
        FileHandler.writeSystemLog("(" + userRole + ") " + userID + " deleted user: " + selectedUser.getUserID() + " (" + selectedUser.getName() + ")");

        loadUsers();
        detailArea.setText("");
        JOptionPane.showMessageDialog(frame, "User and all related data deleted.");
    }
}