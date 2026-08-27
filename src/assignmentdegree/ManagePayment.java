package assignmentdegree;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
import javax.swing.JTextField;

public class ManagePayment implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == deleteButton) {
            deletePayment();
        } else if (e.getSource() == viewReceiptsButton) {
            viewReceipts();
        } else if (e.getSource() == refreshButton) {
            loadPayments();
            clearDetail();
        } else if (e.getSource() == clearFilterButton) {
            resetFilters();
            loadPayments();
        } else if (e.getSource() == backButton) {
            frame.dispose();
            CounterStaffFunction counterStaffFunction = new CounterStaffFunction();
            counterStaffFunction.openCounterStaffFunction(userID, userRole);
        }
    }
    
    private JFrame frame;
    private String userID, userRole;
    private DefaultListModel<String> listModel;
    private JList<String> paymentList;
    private JTextArea detailArea;

    private JComboBox<String> filterYearBox, filterMonthBox, filterDayBox, filterStatusBox;
    private JTextField searchField;
    private JButton clearFilterButton, refreshButton, deleteButton, viewReceiptsButton, backButton;
    private boolean updatingDays = false;

    private Payment selectedPayment = null;

    public void openPage(String userID, String userRole) {
        this.userID = userID;
        this.userRole = userRole;
        
        FileHandler.writeSystemLog("(" + userRole + ")" + userID + " opened the manage payment " + userRole + " function.");

        frame = new JFrame("Manage Payments");
        frame.setSize(1050, 640);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null);

        filterYearBox = new JComboBox<>();
        filterMonthBox = new JComboBox<>();
        filterDayBox = new JComboBox<>();
        filterStatusBox = new JComboBox<>();
        searchField = new JTextField(15);
        clearFilterButton = new JButton("Clear Filter");
        refreshButton = new JButton("Refresh");

        setupFilters();

        JPanel filterPanel = new JPanel(new GridLayout(2, 5, 8, 8));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Payments"));
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

        JPanel topButtonPanel = new JPanel();
        topButtonPanel.add(refreshButton);
        topButtonPanel.add(clearFilterButton);
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(filterPanel, BorderLayout.CENTER);
        topPanel.add(topButtonPanel, BorderLayout.EAST);

        listModel = new DefaultListModel<>();
        paymentList = new JList<>(listModel);
        paymentList.setFont(new Font("Monospaced", Font.PLAIN, 13));
        paymentList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onSelected();
            }
        });

        detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        detailArea.setText("\n  Select a payment to view details.");

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Payment List"));
        leftPanel.add(new JScrollPane(paymentList), BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Payment Details"));
        rightPanel.add(new JScrollPane(detailArea), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        split.setDividerLocation(450);

        deleteButton = new JButton("Delete Payment");
        viewReceiptsButton = new JButton("View Receipts");
        backButton = new JButton("Back");
        deleteButton.setEnabled(false);
        viewReceiptsButton.setEnabled(false);

        deleteButton.addActionListener(this);
        viewReceiptsButton.addActionListener(this);
        backButton.addActionListener(this);
        refreshButton.addActionListener(this);
        clearFilterButton.addActionListener(this);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(backButton);
        bottomPanel.add(viewReceiptsButton);
        bottomPanel.add(deleteButton);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(split, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        loadPayments();
        frame.setVisible(true);
    }

    private void setupFilters() {
        filterYearBox.addItem("All");
        filterMonthBox.addItem("All");
        filterDayBox.addItem("All");
        filterStatusBox.addItem("All");
        filterStatusBox.addItem("Pending");
        filterStatusBox.addItem("Partial");
        filterStatusBox.addItem("Successful");
        for (int y = 2025; y <= 2035; y++) {
            filterYearBox.addItem(String.valueOf(y));
        }
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        for (String m : months) {
            filterMonthBox.addItem(m);
        }
        rebuildDayBox(31);

        filterYearBox.addActionListener(e -> {
            refreshDayBox();
            loadPayments();
        });
        filterMonthBox.addActionListener(e -> {
            refreshDayBox();
            loadPayments();
        });
        filterDayBox.addActionListener(e -> {
            if (!updatingDays) {
                loadPayments();
            }
        });
        filterStatusBox.addActionListener(e -> loadPayments());
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                loadPayments();
            }
        });
    }

    private void refreshDayBox() {
        String month = (String) filterMonthBox.getSelectedItem();
        String yearStr = (String) filterYearBox.getSelectedItem();
        int max = 31;
        if (!"All".equals(month)) {
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
                }
            } else {
                max = LookupService.getDaysInMonth(month, Integer.parseInt(yearStr));
            }
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
        filterDayBox.setSelectedItem((prev != null && prev.matches("\\d+") && Integer.parseInt(prev) <= max) ? prev : "All");
        updatingDays = false;
    }

    private void resetFilters() {
        filterYearBox.setSelectedItem("All");
        filterMonthBox.setSelectedItem("All");
        filterDayBox.setSelectedItem("All");
        filterStatusBox.setSelectedIndex(0);
        searchField.setText("");
    }

    private void loadPayments() {
        listModel.clear(); selectedPayment = null;
        deleteButton.setEnabled(false); viewReceiptsButton.setEnabled(false);
        detailArea.setText("\n  Select a payment to view details.");

        String yearF  = (String) filterYearBox.getSelectedItem();
        String monthF = (String) filterMonthBox.getSelectedItem();
        String dayF   = (String) filterDayBox.getSelectedItem();
        String statusF = (String) filterStatusBox.getSelectedItem();
        String keyword = searchField.getText().trim().toLowerCase();

        for (Payment p : DataStore.allPayments) {
            if (!"All".equals(statusF) && !p.getStatus().equalsIgnoreCase(statusF)) continue;

            Appointment a = LookupService.getAppointmentByID(p.getAppointmentID());
            String customerName = a != null ? LookupService.getCustomerNameByID(a.getCustomerID()) : "?";

            if (a != null) {
                String[] parts = a.getDate().split(" ");
                if (!"All".equals(yearF)  && !parts[2].equals(yearF))  continue;
                if (!"All".equals(monthF) && !parts[1].equals(monthF)) continue;
                if (!"All".equals(dayF)   && !parts[0].equals(dayF))   continue;
            }

            String searchText = (p.getPaymentID() + " " + p.getAppointmentID() + " " + customerName).toLowerCase();
            if (!keyword.isEmpty() && !searchText.contains(keyword)) continue;

            listModel.addElement(p.getPaymentID() + " - " + p.getAppointmentID() + " - " + customerName
                    + "  (RM " + String.format("%.2f", p.getTotalPaid()) + " / " + String.format("%.2f", p.getTotalFee())
                    + ")  [" + p.getStatus() + "]");
        }
        if (listModel.isEmpty()) detailArea.setText("\n  No payments found.");
    }

    private void onSelected() {
        String sel = paymentList.getSelectedValue();
        if (sel == null) {
            clearDetail();
        } else {
            String paymentID = sel.split(" - ")[0].trim();
            for (Payment p : DataStore.allPayments) {
                if (p.getPaymentID().equals(paymentID)) {
                    selectedPayment = p;
                    showDetail(p);
                    deleteButton.setEnabled(true);
                    viewReceiptsButton.setEnabled(true);
                    break;
                }
            }
        }
    }

    private void showDetail(Payment p) {
        Appointment a = LookupService.getAppointmentByID(p.getAppointmentID());
        String customer = a != null ? LookupService.getCustomerNameByID(a.getCustomerID()) : "Unknown";
        String service  = a != null ? LookupService.getServiceNameByID(a.getServiceID())   : "Unknown";
        String date     = a != null ? a.getDate()                                           : "Unknown";

        int receiptCount = 0;
        for (Receipt r : DataStore.allReceipts)
            if (r.getPaymentID().equals(p.getPaymentID())) receiptCount++;

        StringBuilder sb = new StringBuilder();
        sb.append("===========================================\n  PAYMENT DETAILS\n===========================================\n\n");
        sb.append(String.format("Payment ID     : %s%n", p.getPaymentID()));
        sb.append(String.format("Appointment ID : %s%n", p.getAppointmentID()));
        sb.append(String.format("Customer       : %s%n", customer));
        sb.append(String.format("Service        : %s%n", service));
        sb.append(String.format("Appt Date      : %s%n", date));
        sb.append("\n-------------------------------------------\n  FINANCIALS\n-------------------------------------------\n");
        sb.append(String.format("Total Fee      : RM %.2f%n", p.getTotalFee()));
        sb.append(String.format("Total Paid     : RM %.2f%n", p.getTotalPaid()));
        sb.append(String.format("Balance        : RM %.2f%n", p.getBalance()));
        sb.append(String.format("Status         : %s%n", p.getStatus()));
        sb.append(String.format("\n-------------------------------------------\n  Linked Receipts: %d%n-------------------------------------------\n", receiptCount));
        for (Receipt r : DataStore.allReceipts)
            if (r.getPaymentID().equals(p.getPaymentID()))
                sb.append(String.format("  %s  |  RM %.2f  |  %s  |  %s%n",
                        r.getReceiptID(), r.getAmountPaid(), r.getMethod(), r.getDate()));

        detailArea.setText(sb.toString()); detailArea.setCaretPosition(0);
    }

    private void deletePayment() {
        if (selectedPayment != null) {
            int receiptCount = 0;
            for (Receipt r : DataStore.allReceipts) {
                if (r.getPaymentID().equals(selectedPayment.getPaymentID())) {
                    receiptCount++;
                }
            }

            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Delete payment " + selectedPayment.getPaymentID() + "?\n\n"
                            + "This will also delete " + receiptCount + " linked receipt(s).\n"
                            + "The appointment will be reset to Scheduled.",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                PaymentController.deletePayment(selectedPayment.getPaymentID());
                
                FileHandler.writeSystemLog("(" + userRole + ") " + userID + " deleted payment: " + selectedPayment.getPaymentID() + " [Appointment: " + selectedPayment.getAppointmentID() + "]");

                JOptionPane.showMessageDialog(frame, "Payment and receipts deleted.");
                selectedPayment = null;
                loadPayments();
                clearDetail();
            }
        }
    }

    private void viewReceipts() {
        if (selectedPayment != null) {
            frame.dispose();
            ManageReceipt manageReceipt = new ManageReceipt();
            manageReceipt.openPage(userID, userRole, selectedPayment.getPaymentID());
        }
    }

    private void clearDetail() {
        detailArea.setText("\n  Select a payment to view details.");
        selectedPayment = null;
        deleteButton.setEnabled(false); viewReceiptsButton.setEnabled(false);
    }
}