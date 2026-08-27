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

public class ServiceManagement implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            frame.dispose();
            openAddServicePage();
        } else if (e.getSource() == editButton) {
            if (selectedService == null) {
                JOptionPane.showMessageDialog(frame, "Select service first");
            } else {
                frame.dispose();
                openEditServicePage(selectedService);
            }
        } else if (e.getSource() == deleteButton) {
            deleteService();
        } else if (e.getSource() == refreshButton) {
            loadServices();
        } else if (e.getSource() == backButton) {
            frame.dispose();
            ManagerFunction managerFunction = new ManagerFunction();
            managerFunction.openManagerFunction(userID, userRole);
        }
    }
    
    private JFrame frame;
    private JComboBox<String> typeSelector;
    private DefaultListModel<String> listModel;
    private JList<String> serviceList;
    private JTextArea detailArea;
    private JTextField searchField;
    private JButton addButton, editButton, deleteButton, refreshButton, backButton;
    private Service selectedService;
    private String userID, userRole;

    public void openServiceManagement(String userID, String role) {
        this.userID = userID;
        this.userRole = role;
        
        FileHandler.writeSystemLog("(" + userRole + ")" + userID + " opened the manage service " + userRole + " function.");

        frame = new JFrame("Manage Services");
        frame.setSize(750, 500);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        typeSelector = new JComboBox<>(new String[]{"All", "Normal", "Major"});
        typeSelector.addActionListener(e -> loadServices());

        searchField = new JTextField(15);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadServices();
            }
        });

        listModel = new DefaultListModel<>();
        serviceList = new JList<>(listModel);
        serviceList.addListSelectionListener(e -> showDetails());

        detailArea = new JTextArea();
        detailArea.setEditable(false);

        addButton = new JButton("Add");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        refreshButton = new JButton("Refresh");
        backButton = new JButton("Back");

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Type:"));
        topPanel.add(typeSelector);
        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);
        topPanel.add(refreshButton);

        JPanel btnPanel = new JPanel();
        btnPanel.add(addButton);
        btnPanel.add(editButton);
        btnPanel.add(deleteButton);
        btnPanel.add(backButton);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(serviceList), new JScrollPane(detailArea));
        split.setDividerLocation(300);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(split, BorderLayout.CENTER);
        frame.add(btnPanel, BorderLayout.SOUTH);

        addButton.addActionListener(this);
        editButton.addActionListener(this);
        deleteButton.addActionListener(this);
        refreshButton.addActionListener(this);
        backButton.addActionListener(this);

        loadServices();
        frame.setVisible(true);
    }

    private void loadServices() {
        listModel.clear();
        detailArea.setText("");
        selectedService = null;
        String type = (String) typeSelector.getSelectedItem();
        String keyword = searchField.getText().trim().toLowerCase();

        for (Service s : DataStore.allServices) {
            if (!type.equals("All") && !s.getServiceType().equalsIgnoreCase(type)) {
                continue;
            }
            String searchable = (s.getServiceID() + " " + s.getServiceName() + " " + s.getServiceType() + " " + s.getPrice()).toLowerCase();
            if (!keyword.isEmpty() && !searchable.contains(keyword)) {
                continue;
            }
            listModel.addElement(s.getServiceID() + " - " + s.getServiceName());
        }
        if (listModel.isEmpty()) {
            detailArea.setText("No services found.");
        }
    }

    private void showDetails() {
        String sel = serviceList.getSelectedValue();
        if (sel != null) {
            String id = sel.split(" - ")[0];
            selectedService = LookupService.getServiceByID(id);
            if (selectedService != null) {
                detailArea.setText(
                        "Service Details:\n" +
                        "----------------\n" +
                        selectedService.getServiceInfo() +
                        "\nPrice: " + selectedService.getFormattedPrice() +
                        "\nEstimated Duration: " + selectedService.getEstimatedDuration());
            }
        }
    }

    private void deleteService() {
        if (selectedService == null) {
            JOptionPane.showMessageDialog(frame, "Select service first");
        } else {
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Delete this service? This will also delete all linked appointments, payments, receipts, and comments.",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                ServiceController.deleteService(selectedService.getServiceID());
                loadServices();
                detailArea.setText("");
                JOptionPane.showMessageDialog(frame, "Service and all related data deleted.");
            }
        }
    }

    private void openAddServicePage() {
        JFrame af = new JFrame("Add Service");
        af.setSize(400, 320);
        af.setLayout(null);
        af.setLocationRelativeTo(null);

        JTextField nameField = new JTextField();
        nameField.setBounds(150, 40, 180, 30);
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Normal", "Major"});
        typeBox.setBounds(150, 90, 180, 30);
        JTextField priceField = new JTextField();
        priceField.setBounds(150, 140, 180, 30);

        JButton saveBtn = new JButton("Add");
        saveBtn.setBounds(80, 210, 100, 30);
        JButton backBtn = new JButton("Back");
        backBtn.setBounds(200, 210, 100, 30);

        addLabel(af, "Name:", 40, 40, 100, 30);
        af.add(nameField);
        addLabel(af, "Type:", 40, 90, 100, 30);
        af.add(typeBox);
        addLabel(af, "Price:", 40, 140, 100, 30);
        af.add(priceField);
        af.add(saveBtn);
        af.add(backBtn);

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String priceText = priceField.getText().trim();

            if (ValidationUtils.validateServiceInput(name, priceText)) {

                double price = Double.parseDouble(priceText);
                String type = (String) typeBox.getSelectedItem();

                ServiceController.addService(name, type, price);

                FileHandler.writeSystemLog("(" + userRole + ")" + userID + " added new service: " + name + " | Type: " + type + " | RM " + String.format("%.2f", price));

                JOptionPane.showMessageDialog(af, "Service added successfully!");
                af.dispose();
                openServiceManagement(userID, userRole);
            }
        });
        backBtn.addActionListener(e -> {
            af.dispose();
            openServiceManagement(userID, userRole);
        });
        af.setVisible(true);
    }

    private void openEditServicePage(Service service) {
        JFrame ef = new JFrame("Edit Service");
        ef.setSize(400, 320);
        ef.setLayout(null);
        ef.setLocationRelativeTo(null);

        JTextField nameField = new JTextField(service.getServiceName());
        nameField.setBounds(150, 40, 180, 30);
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Normal", "Major"});
        typeBox.setSelectedItem(service.getServiceType());
        typeBox.setBounds(150, 90, 180, 30);
        JTextField priceField = new JTextField(String.valueOf(service.getPrice()));
        priceField.setBounds(150, 140, 180, 30);

        JButton saveBtn = new JButton("Save");
        saveBtn.setBounds(80, 210, 100, 30);
        JButton backBtn = new JButton("Back");
        backBtn.setBounds(200, 210, 100, 30);

        addLabel(ef, "Name:", 40, 40, 100, 30);
        ef.add(nameField);
        addLabel(ef, "Type:", 40, 90, 100, 30);
        ef.add(typeBox);
        addLabel(ef, "Price:", 40, 140, 100, 30);
        ef.add(priceField);
        ef.add(saveBtn);
        ef.add(backBtn);

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String priceText = priceField.getText().trim();
            String type = (String) typeBox.getSelectedItem();

            if (ValidationUtils.validateServiceInput(name, priceText)) {

                String oldName = service.getServiceName();
                String oldType = service.getServiceType();
                double oldPrice = service.getPrice();

                double newPrice = Double.parseDouble(priceText);

                ServiceController.editService(service, name, type, newPrice);

                FileHandler.writeSystemLog("(" + userRole + ")" + userID + " edited service " + service.getServiceID() + " | OLD: " + oldName + " (" + oldType + ") RM " + String.format("%.2f", oldPrice) + " → NEW: " + name + " (" + type + ") RM " + String.format("%.2f", newPrice));

                JOptionPane.showMessageDialog(ef, "Service updated successfully!");
                ef.dispose();
                openServiceManagement(userID, userRole);
            }
        });
        backBtn.addActionListener(e -> {
            ef.dispose();
            openServiceManagement(userID, userRole);
        });
        ef.setVisible(true);
    }

    private void addLabel(JFrame f, String t, int x, int y, int w, int h) {
        JLabel l = new JLabel(t);
        l.setBounds(x, y, w, h);
        f.add(l);
    }
}