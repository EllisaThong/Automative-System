package assignmentdegree;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class ManagerAddService implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            handleAdd();
        }
        else if (e.getSource() == backButton) {
            frame.dispose();
            ManagerFunction managerFunction = new ManagerFunction();
            managerFunction.openManagerFunction(userID, userRole);
        }
    }

    private JFrame frame;
    private JTextField nameField;
    private JTextField priceField;
    private JComboBox<String> typeBox;

    private JButton addButton;
    private JButton backButton;

    private String userID;
    private String userRole;

    public void openAddServicePage(String userID, String role) {
        this.userID = userID;
        this.userRole = role;
        
        FileHandler.writeSystemLog("(" + userRole + ")" + userID + " opened the add service " + userRole + " function.");

        frame = new JFrame("Add Service");
        frame.setSize(400, 300);
        frame.setLayout(new GridLayout(4, 2, 10, 10));
        frame.setLocationRelativeTo(null);

        nameField = new JTextField();
        priceField = new JTextField();
        typeBox = new JComboBox<>(new String[]{"Normal", "Major"});

        addButton = new JButton("Add");
        backButton = new JButton("Back");

        frame.add(new JLabel("Service Name:"));
        frame.add(nameField);

        frame.add(new JLabel("Service Type:"));
        frame.add(typeBox);

        frame.add(new JLabel("Price:"));
        frame.add(priceField);

        frame.add(addButton);
        frame.add(backButton);

        addButton.addActionListener(this);
        backButton.addActionListener(this);

        frame.setVisible(true);
    }
    
    private void handleAdd() {
        String name = nameField.getText().trim();
        String type = (String) typeBox.getSelectedItem();
        String priceText = priceField.getText().trim();

        if (name.isEmpty() || priceText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please fill all fields");
        } else {
            try {
                double price = Double.parseDouble(priceText);
                String id = FileHandler.generateServiceID();

                Service service = new Service(id, name, type, price);
                DataStore.allServices.add(service);
                FileHandler.writeSystemLog("(" + userRole + ") " + userID + " added service: " + id + " [Name: " + name + ", Type: " + type + ", Price: RM " + String.format("%.2f", price) + "]");
                JOptionPane.showMessageDialog(frame, "Service added successfully");
                nameField.setText("");
                priceField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Invalid price");
            }
        }
    }
}