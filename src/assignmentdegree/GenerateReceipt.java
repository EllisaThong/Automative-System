package assignmentdegree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

public class GenerateReceipt implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == printButton) {
            if (selectedReceipt == null) {
                JOptionPane.showMessageDialog(frame, "Please select a receipt first.");
                return;
            }

            FileHandler.exportReceiptToFile(selectedReceipt);
            
            Payment p = LookupService.getPaymentByID(selectedReceipt.getPaymentID());
            String apptID = (p != null) ? p.getAppointmentID() : "Unknown";
            FileHandler.writeSystemLog("(" + userRole + ") " + userID + " generated receipt: " + selectedReceipt.getReceiptID() + " for appointment: " + apptID);

            JOptionPane.showMessageDialog(frame,
                "Receipt exported successfully to D:/ as " + selectedReceipt.getReceiptID() + ".txt");
        } else if (e.getSource() == refreshButton) {
            FileHandler.readAllFiles();
            loadAppointments();
            clearMiddlePanel();
            clearRightPanel();
        } else if (e.getSource() == backButton) {
            frame.dispose();
            CounterStaffFunction csf = new CounterStaffFunction();
            csf.openCounterStaffFunction(userID, userRole);
        }
    }

    private JFrame frame;
    private String userID;
    private String userRole;

    private DefaultListModel<String> apptListModel;
    private JList<String> apptList;
    private JTextField searchField;

    private DefaultListModel<String> receiptListModel;
    private JList<String> receiptList;
    private JLabel middleTitleLabel;

    private JTextArea receiptArea;
    private JButton printButton;

    private JButton refreshButton;
    private JButton backButton;

    private Appointment selectedAppointment = null;
    private Receipt selectedReceipt = null;
    
    public void openPage(String userID, String userRole) {
        this.userID = userID;
        this.userRole = userRole;
        
        FileHandler.writeSystemLog("(" + userRole + ")" + userID + " opened the generate receipt" + userRole + " function.");

        frame = new JFrame("Generate & Print Receipts");
        frame.setSize(1100, 650);
        frame.setLayout(new BorderLayout(8, 8));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel topBar = buildTopBar();

        JPanel leftPanel = buildLeftPanel();
        JPanel middlePanel = buildMiddlePanel();
        JPanel rightPanel = buildRightPanel();

        JSplitPane leftMiddleSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, middlePanel);
        leftMiddleSplit.setDividerLocation(330);
        leftMiddleSplit.setResizeWeight(0.35);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftMiddleSplit, rightPanel);
        mainSplit.setDividerLocation(650);
        mainSplit.setResizeWeight(0.6);

        JPanel bottomBar = buildBottomBar();

        frame.add(topBar, BorderLayout.NORTH);
        frame.add(mainSplit, BorderLayout.CENTER);
        frame.add(bottomBar, BorderLayout.SOUTH);

        loadAppointments();
        frame.setVisible(true);
    }

    private JPanel buildTopBar() {
        JPanel panel = new JPanel(new BorderLayout(8, 4));
        panel.setBorder(new EmptyBorder(8, 10, 4, 10));

        JLabel title = new JLabel("Receipt Management  —  " + LookupService.getStaffNameByID(userID));
        title.setFont(new Font("SansSerif", Font.BOLD, 15));

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(this);

        panel.add(title, BorderLayout.WEST);
        panel.add(refreshButton, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("All Appointments"));

        searchField = new JTextField();
        searchField.setToolTipText("Search by appointment ID or customer name");
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                loadAppointments();
            }
        });

        JPanel searchRow = new JPanel(new BorderLayout(4, 0));
        searchRow.add(new JLabel("Search: "), BorderLayout.WEST);
        searchRow.add(searchField, BorderLayout.CENTER);
        searchRow.setBorder(new EmptyBorder(4, 4, 4, 4));

        apptListModel = new DefaultListModel<>();
        apptList = new JList<>(apptListModel);
        apptList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        apptList.setCellRenderer(new AppointmentCellRenderer());
        apptList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onAppointmentSelected();
            }
        });

        panel.add(searchRow, BorderLayout.NORTH);
        panel.add(new JScrollPane(apptList), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildMiddlePanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));

        middleTitleLabel = new JLabel("  Select an appointment →");
        middleTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        middleTitleLabel.setBorder(new EmptyBorder(6, 6, 6, 6));

        receiptListModel = new DefaultListModel<>();
        receiptList = new JList<>(receiptListModel);
        receiptList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        receiptList.setCellRenderer(new ReceiptCellRenderer());
        receiptList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onReceiptSelected();
            }
        });

        panel.setBorder(BorderFactory.createTitledBorder("Receipts for Appointment"));
        panel.add(middleTitleLabel, BorderLayout.NORTH);
        panel.add(new JScrollPane(receiptList), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Receipt Detail"));

        receiptArea = new JTextArea();
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        receiptArea.setText("\n\n   Select a receipt to preview it here.");

        panel.add(new JScrollPane(receiptArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBottomBar() {
        printButton = new JButton("Print Selected Receipt");
        printButton.setEnabled(false);
        printButton.addActionListener(this);

        backButton = new JButton("Back to Menu");
        backButton.addActionListener(this);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        panel.add(backButton);
        panel.add(printButton);
        return panel;
    }

    private void loadAppointments() {
        apptListModel.clear();
        String keyword = searchField.getText().toLowerCase().trim();

        for (Appointment a : DataStore.allAppointments) {
            // Show appointment if it is Completed OR if it already has receipts
            if (!a.isCompleted() && !hasReceipts(a.getAppointmentID())) {
                continue;
            }

            String customerName = LookupService.getCustomerNameByID(a.getCustomerID()).toLowerCase();
            String apptID = a.getAppointmentID().toLowerCase();
            if (!keyword.isEmpty() && !apptID.contains(keyword) && !customerName.contains(keyword)) {
                continue;
            }

            apptListModel.addElement(a.getAppointmentID());
        }

        if (apptListModel.isEmpty()) {
            clearMiddlePanel();
            clearRightPanel();
        }
    }

    private boolean hasReceipts(String apptID) {
        Payment p = LookupService.getPaymentByAppointmentID(apptID);
        if (p == null) return false;
        
        String targetPaymentID = p.getPaymentID();
        for (Receipt r : DataStore.allReceipts) {
            if (r.getPaymentID().equals(targetPaymentID)) {
                return true;
            }
        }
        return false;
    }

    private void loadReceipts(String apptID) {
        receiptListModel.clear();
        Payment p = LookupService.getPaymentByAppointmentID(apptID);
        if (p == null) {
            middleTitleLabel.setText(middleTitleLabel.getText() + " (No payment record)");
            receiptArea.setText("\n\n   This appointment has no payment record yet.\n\n   Please use the 'Collect Payment' function\n   to process payment first.");
            return;
        }

        String targetPaymentID = p.getPaymentID();
        boolean found = false;
        for (Receipt r : DataStore.allReceipts) {
            if (r.getPaymentID().equals(targetPaymentID)) {
                receiptListModel.addElement(r.getReceiptID());
                found = true;
            }
        }
        
        if (!found) {
            middleTitleLabel.setText(middleTitleLabel.getText() + " (No receipts yet)");
            receiptArea.setText("\n\n   This appointment has a payment record (ID: " + targetPaymentID + ")\n   but no receipts have been generated yet.");
        }
    }

    private void onAppointmentSelected() {
        String apptID = apptList.getSelectedValue();
        if (apptID != null) {
            selectedAppointment = LookupService.getAppointmentByID(apptID);
            if (selectedAppointment != null) {
                middleTitleLabel.setText("  Customer: " + LookupService.getCustomerNameByID(selectedAppointment.getCustomerID()));
                loadReceipts(apptID);
                clearRightPanel();
            }
        }
    }

    private void onReceiptSelected() {
        String selectedID = receiptList.getSelectedValue();
        if (selectedID != null) {
            for (Receipt r : DataStore.allReceipts) {
                if (r.getReceiptID().equals(selectedID)) {
                    selectedReceipt = r;
                    showReceiptDetail(r);
                    printButton.setEnabled(true);
                }
            }
        }
    }

    private void showReceiptDetail(Receipt r) {
        Payment pay = LookupService.getPaymentByID(r.getPaymentID());
        String apptID = (pay != null) ? pay.getAppointmentID() : "Unknown";
        Appointment appt = LookupService.getAppointmentByID(apptID);

        String customerName = appt != null ? LookupService.getCustomerNameByID(appt.getCustomerID())  : "Unknown";
        String serviceName  = appt != null ? LookupService.getServiceNameByID(appt.getServiceID())    : "Unknown";
        String staffName    = appt != null ? LookupService.getStaffNameByID(appt.getCounterStaffID()) : "Unknown";
        String techNames    = appt != null ? LookupService.getTechnicianNames(appt.getTechnicianIDs()): "Unknown";
        String apptDate     = appt != null ? appt.getDate()                                         : "Unknown";
        String apptTime     = appt != null ? appt.getStartTime() + " – " + appt.getEndTime()       : "Unknown";

        int total = 0, index = 0, count = 0;
        for (Receipt rx : DataStore.allReceipts) {
            if (rx.getPaymentID().equals(r.getPaymentID())) {
                total++;
                count++;
                if (rx.getReceiptID().equals(r.getReceiptID())) index = count;
            }
        }

        boolean isCash = r.getMethod().equalsIgnoreCase("Cash");
        double tendered = Math.round((r.getAmountPaid() + r.getChange()) * 100.0) / 100.0;

        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("     APU AUTOMOTIVE SERVICE CENTRE\n");
        sb.append("               RECEIPT\n");
        sb.append("========================================\n\n");
        sb.append("Receipt ID     : ").append(r.getReceiptID());
        if (total > 1) sb.append("  (").append(index).append(" of ").append(total).append(" for this appt)");
        sb.append("\n");
        sb.append("Payment ID     : ").append(r.getPaymentID()).append("\n");
        sb.append("Payment Date   : ").append(r.getDate()).append("\n");
        sb.append("Payment Method : ").append(r.getMethod()).append("\n");
        sb.append("Status         : ").append(r.getStatus()).append("\n\n");
        sb.append("----------------------------------------\n");
        sb.append("APPOINTMENT DETAILS\n");
        sb.append("----------------------------------------\n");
        sb.append("Appointment ID : ").append(apptID).append("\n");
        sb.append("Appt Date      : ").append(apptDate).append("\n");
        sb.append("Time           : ").append(apptTime).append("\n");
        sb.append("Customer       : ").append(customerName).append("\n");
        sb.append("Service        : ").append(serviceName).append("\n");
        sb.append("Technician(s)  : ").append(techNames).append("\n");
        sb.append("Counter Staff  : ").append(staffName).append("\n\n");
        sb.append("----------------------------------------\n");
        sb.append("PAYMENT SUMMARY\n");
        sb.append("----------------------------------------\n");
        sb.append("Service Fee    : RM ").append(String.format("%.2f", r.getTotalFee())).append("\n");
        sb.append("Amount Paid    : RM ").append(String.format("%.2f", r.getAmountPaid())).append("\n");
        if (isCash) {
            sb.append("Tendered       : RM ").append(String.format("%.2f", tendered)).append("\n");
            sb.append("Change Given   : RM ").append(String.format("%.2f", r.getChange())).append("\n");
        }
        sb.append("Total Paid     : RM ").append(String.format("%.2f", r.getTotalPaid())).append("\n");
        sb.append("Balance Due    : RM ").append(String.format("%.2f", r.getBalance())).append("\n");
        sb.append("----------------------------------------\n\n");
        sb.append("   Thank you for choosing APU-ASC!\n");
        sb.append("========================================");

        receiptArea.setText(sb.toString());
        receiptArea.setCaretPosition(0);
    }

    private void clearMiddlePanel() {
        receiptListModel.clear();
        middleTitleLabel.setText("  Select an appointment →");
        selectedAppointment = null;
    }

    private void clearRightPanel() {
        receiptArea.setText("\n\n   Select a receipt to preview it here.");
        selectedReceipt = null;
        printButton.setEnabled(false);
    }

    private class AppointmentCellRenderer extends JPanel implements ListCellRenderer<String> {
        private final JLabel idLabel      = new JLabel();
        private final JLabel nameLabel    = new JLabel();
        private final JLabel detailLabel  = new JLabel();
        private final JLabel statusDot    = new JLabel("●");

        AppointmentCellRenderer() {
            setLayout(new BorderLayout(6, 0));
            setBorder(new EmptyBorder(5, 8, 5, 8));

            statusDot.setFont(new Font("SansSerif", Font.PLAIN, 10));

            JPanel left = new JPanel(new BorderLayout(2, 2));
            left.setOpaque(false);

            JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            topRow.setOpaque(false);
            topRow.add(statusDot);
            topRow.add(idLabel);
            topRow.add(nameLabel);

            left.add(topRow,    BorderLayout.NORTH);
            left.add(detailLabel, BorderLayout.SOUTH);

            add(left, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list,
                String value, int index, boolean isSelected, boolean cellHasFocus) {

            Appointment a = LookupService.getAppointmentByID(value);
            if (a == null) { idLabel.setText(value); return this; }

            String customer = LookupService.getCustomerNameByID(a.getCustomerID());
            String service  = LookupService.getServiceNameByID(a.getServiceID());
            Payment pay     = LookupService.getPaymentByAppointmentID(a.getAppointmentID());
            String payStatus = pay != null ? pay.getStatus() : "Unknown";

            idLabel.setText(a.getAppointmentID());
            idLabel.setFont(new Font("Monospaced", Font.BOLD, 12));

            nameLabel.setText("  " + customer);
            nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

            detailLabel.setText("   " + a.getDate() + "  ·  " + service);
            detailLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));

            switch (payStatus.toLowerCase()) {
                case "successful": statusDot.setForeground(new Color(34, 139, 34));  break;
                case "partial":    statusDot.setForeground(new Color(220, 140, 0));  break;
                default:           statusDot.setForeground(Color.GRAY);
            }

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                idLabel.setForeground(list.getSelectionForeground());
                nameLabel.setForeground(list.getSelectionForeground());
                detailLabel.setForeground(list.getSelectionForeground());
            } else {
                setBackground(index % 2 == 0 ? Color.WHITE : new Color(245, 245, 250));
                setForeground(Color.BLACK);
                idLabel.setForeground(new Color(30, 60, 120));
                nameLabel.setForeground(Color.DARK_GRAY);
                detailLabel.setForeground(Color.GRAY);
            }
            setOpaque(true);
            return this;
        }
    }

    private class ReceiptCellRenderer extends JPanel implements ListCellRenderer<String> {
        private final JLabel topLabel    = new JLabel();
        private final JLabel bottomLabel = new JLabel();
        private final JLabel statusBadge = new JLabel();

        ReceiptCellRenderer() {
            setLayout(new BorderLayout(6, 0));
            setBorder(new EmptyBorder(5, 8, 5, 8));

            statusBadge.setFont(new Font("SansSerif", Font.BOLD, 11));
            statusBadge.setOpaque(true);
            statusBadge.setBorder(new EmptyBorder(2, 6, 2, 6));

            JPanel textPane = new JPanel(new GridLayout(2, 1, 0, 2));
            textPane.setOpaque(false);
            textPane.add(topLabel);
            textPane.add(bottomLabel);

            JPanel badgePane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
            badgePane.setOpaque(false);
            badgePane.add(statusBadge);

            add(textPane,  BorderLayout.CENTER);
            add(badgePane, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list,
                String value, int index, boolean isSelected, boolean cellHasFocus) {

            Receipt r = null;
            for (Receipt rx : DataStore.allReceipts)
                if (rx.getReceiptID().equals(value)) { r = rx; break; }

            if (r == null) { topLabel.setText(value); return this; }

            topLabel.setText(r.getReceiptID() + "  ·  " + r.getDate());
            topLabel.setFont(new Font("Monospaced", Font.BOLD, 12));

            boolean isCash = r.getMethod().equalsIgnoreCase("Cash");
            String changeStr = isCash ? "   |   Change: RM " + String.format("%.2f", r.getChange()) : "";
            bottomLabel.setText("RM " + String.format("%.2f", r.getAmountPaid()) + " paid"
                + "   |   Balance: RM " + String.format("%.2f", r.getBalance())
                + changeStr
                + "   |   " + r.getMethod());
            bottomLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

            switch (r.getStatus().toLowerCase()) {
                case "successful":
                    statusBadge.setText("Successful");
                    statusBadge.setBackground(new Color(34, 139, 34));
                    statusBadge.setForeground(Color.WHITE);
                    break;
                case "partial":
                    statusBadge.setText("Partial");
                    statusBadge.setBackground(new Color(220, 140, 0));
                    statusBadge.setForeground(Color.WHITE);
                    break;
                default:
                    statusBadge.setText(r.getStatus());
                    statusBadge.setBackground(Color.LIGHT_GRAY);
                    statusBadge.setForeground(Color.DARK_GRAY);
            }

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                topLabel.setForeground(list.getSelectionForeground());
                bottomLabel.setForeground(list.getSelectionForeground());
            } else {
                setBackground(index % 2 == 0 ? Color.WHITE : new Color(245, 245, 250));
                topLabel.setForeground(new Color(30, 60, 120));
                bottomLabel.setForeground(Color.DARK_GRAY);
            }
            setOpaque(true);
            return this;
        }
    }
}
