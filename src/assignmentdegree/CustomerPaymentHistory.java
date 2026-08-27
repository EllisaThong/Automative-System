package assignmentdegree;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public class CustomerPaymentHistory implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == refreshButton) {
            loadPayments();
        } else if (e.getSource() == backButton) {
            frame.dispose();
            CustomerFunction cf = new CustomerFunction();
            cf.openCustomerFunction(userID, userRole);
        } else if (e.getSource() == clearFilterButton) {
            resetFilters();
            loadPayments();
        }
    }

    private JFrame frame;
    private DefaultListModel<String> listModel;
    private JList<String> paymentList;
    private JTextArea detailArea;

    private JButton refreshButton, backButton, clearFilterButton;

    private JComboBox<String> filterYearBox, filterMonthBox, filterDayBox, filterStatusBox;
    private JTextField searchField;

    private String userID;
    private String userRole;

    private boolean updatingDays = false;

    public void openPage(String userID, String userRole) {
        this.userID = userID;
        this.userRole = userRole;
        
        FileHandler.writeSystemLog("(" + userRole + ")" + userID + " opened the payment history " + userRole + " function.");

        frame = new JFrame("My Payment History");
        frame.setSize(950, 550);
        frame.setLayout(new BorderLayout(10,10));
        frame.setLocationRelativeTo(null);

        listModel = new DefaultListModel<>();
        paymentList = new JList<>(listModel);
        paymentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paymentList.addListSelectionListener(e -> showDetails());

        detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

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

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(filterPanel, BorderLayout.CENTER);

        JPanel topBtn = new JPanel();
        topBtn.add(refreshButton);
        topBtn.add(clearFilterButton);
        topPanel.add(topBtn, BorderLayout.EAST);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(paymentList),
                new JScrollPane(detailArea)
        );
        splitPane.setDividerLocation(400);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(backButton);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(splitPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        refreshButton.addActionListener(this);
        backButton.addActionListener(this);
        clearFilterButton.addActionListener(this);

        filterMonthBox.addActionListener(e -> {
            refreshDayBox();
            loadPayments();
        });

        filterYearBox.addActionListener(e -> {
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

        loadPayments();
        frame.setVisible(true);
    }

    private void setupFilters() {
        filterYearBox.addItem("All");
        filterMonthBox.addItem("All");
        filterDayBox.addItem("All");
        filterStatusBox.addItem("All");

        for (int y = 2025; y <= 2035; y++) {
            filterYearBox.addItem(String.valueOf(y));
        }

        String[] months = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        for (String m : months) {
            filterMonthBox.addItem(m);
        }

        filterStatusBox.addItem("Pending");
        filterStatusBox.addItem("Partial");
        filterStatusBox.addItem("Successful");

        rebuildDayBox(31);
    }

    private void rebuildDayBox(int maxDays) {
        updatingDays = true;

        String prev = (String) filterDayBox.getSelectedItem();

        filterDayBox.removeAllItems();
        filterDayBox.addItem("All");

        for (int d = 1; d <= maxDays; d++) {
            filterDayBox.addItem(String.valueOf(d));
        }

        if (prev != null) {
            filterDayBox.setSelectedItem(prev);
        }

        updatingDays = false;
    }

    private void refreshDayBox() {
        String month = (String) filterMonthBox.getSelectedItem();
        String yearStr = (String) filterYearBox.getSelectedItem();

        if ("All".equals(month)) {
            rebuildDayBox(31);
            return;
        }

        int maxDays;

        if ("All".equals(yearStr)) {
            switch (month) {
                case "February":
                    maxDays = 28;
                    break;
                case "April":
                case "June":
                case "September":
                case "November":
                    maxDays = 30;
                    break;
                default:
                    maxDays = 31;
            }
        } else {
            int year = Integer.parseInt(yearStr);
            maxDays = LookupService.getDaysInMonth(month, year);
        }

        rebuildDayBox(maxDays);
    }

    private void resetFilters() {
        filterYearBox.setSelectedIndex(0);
        filterMonthBox.setSelectedIndex(0);
        filterStatusBox.setSelectedIndex(0);
        searchField.setText("");
        rebuildDayBox(31);
        filterDayBox.setSelectedItem("All");
    }

    private void loadPayments() {
        listModel.clear();
        detailArea.setText("");

        String yearF = (String) filterYearBox.getSelectedItem();
        String monthF = (String) filterMonthBox.getSelectedItem();
        String dayF = (String) filterDayBox.getSelectedItem();
        String statusF = (String) filterStatusBox.getSelectedItem();
        String keyword = searchField.getText().trim().toLowerCase();

        ArrayList<Payment> filteredPayments = new ArrayList<>();
        ArrayList<Appointment> filteredAppts = new ArrayList<>();

        for (Payment p : DataStore.allPayments) {
            Appointment a = LookupService.getAppointmentByID(p.getAppointmentID());
            if (a == null || !a.getCustomerID().equals(userID)) {
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
            if (!"All".equals(statusF) && !p.getStatus().equalsIgnoreCase(statusF)) {
                continue;
            }

            String serviceName = LookupService.getServiceNameByID(a.getServiceID());
            String searchText = (p.getPaymentID() + " " + serviceName + " " + a.getDate() + " " + p.getStatus()).toLowerCase();
            if (!keyword.isEmpty() && !searchText.contains(keyword)) {
                continue;
            }

            filteredPayments.add(p);
            filteredAppts.add(a);
            listModel.addElement(p.getPaymentID() + " - " + a.getDate() + " - " + serviceName + " - " + p.getStatus());
        }

        Customer c = LookupService.getCustomerByID(userID);
        if (c != null) {
            double total = c.getTotalSpent(filteredPayments, filteredAppts);
            int unpaid = c.getUnpaidCount(filteredPayments, filteredAppts);
            detailArea.setText("--- Payment Summary (Filtered) ---\n" +
                               "Total Paid in this Period: RM " + String.format("%.2f", total) + "\n" +
                               "Unpaid/Partial Records: " + unpaid + "\n\n" +
                               "Select a record from the list to see full details.");
        }

        if (listModel.isEmpty()) {
            detailArea.setText("No matching payment records.");
        }
    }

    private void showDetails() {
        String sel = paymentList.getSelectedValue();
        if (sel != null) {
            String pid = sel.split(" - ")[0];
            Payment p = LookupService.getPaymentByID(pid);
            if (p != null) {
                Appointment a = LookupService.getAppointmentByID(p.getAppointmentID());
                String service = LookupService.getServiceNameByID(a.getServiceID());

                StringBuilder sb = new StringBuilder();
                sb.append("Payment ID      : ").append(p.getPaymentID()).append("\n");
                sb.append("Appointment ID  : ").append(p.getAppointmentID()).append("\n");
                sb.append("Service         : ").append(service).append("\n");
                sb.append("Date            : ").append(a.getDate()).append("\n");
                sb.append("Status          : ").append(p.getStatus()).append("\n\n");
                sb.append("Total Fee       : RM ").append(String.format("%.2f", p.getTotalFee())).append("\n");
                sb.append("Total Paid      : RM ").append(String.format("%.2f", p.getTotalPaid())).append("\n");
                sb.append("Balance         : RM ").append(String.format("%.2f", p.getBalance())).append("\n\n");
                sb.append("--- Receipt History ---\n");

                boolean hasReceipt = false;
                for (Receipt r : DataStore.allReceipts) {
                    if (r.getPaymentID().equals(p.getPaymentID())) {
                        sb.append("Receipt: ").append(r.getReceiptID())
                                .append(" | RM ").append(String.format("%.2f", r.getAmountPaid()))
                                .append(" | ").append(r.getMethod()).append("\n");
                        hasReceipt = true;
                    }
                }
                if (!hasReceipt) {
                    sb.append("No receipts yet.");
                }

                detailArea.setText(sb.toString());
            }
        }
    }
}