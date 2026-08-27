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
import java.time.YearMonth;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class PayAppointment implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == payButton) {
            openPaymentForm();
        } else if (e.getSource() == refreshButton) {
            loadAppointments();
        } else if (e.getSource() == backButton) {
            frame.dispose();
            CounterStaffFunction counterStaffFunction = new CounterStaffFunction();
            counterStaffFunction.openCounterStaffFunction(userID, userRole);
        }
    }
    
    private JFrame frame;
    private DefaultListModel<String> listModel;
    private JList<String> appointmentList;
    private JTextArea detailArea;

    private JButton payButton, refreshButton, backButton;
    private JComboBox<String> filterYearBox, filterMonthBox, filterDayBox, filterStatusBox;
    private JTextField searchField;
    private boolean updatingFilterDays = false;

    private Appointment selectedAppointment;
    private String userID, userRole;

    public void openPage(String userID, String userRole) {
        this.userID = userID;
        this.userRole = userRole;
        
        FileHandler.writeSystemLog("(" + userRole + ")" + userID + " opened the pay appointment " + userRole + " function.");

        frame = new JFrame("Pay Appointment");
        frame.setSize(1000, 600);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        listModel = new DefaultListModel<>();
        appointmentList = new JList<>(listModel);
        appointmentList.addListSelectionListener(e -> {
            showDetails();
            payButton.setEnabled(appointmentList.getSelectedIndex() != -1);
        });

        detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        payButton = new JButton("Pay");
        payButton.setEnabled(false);
        refreshButton = new JButton("Refresh");
        backButton = new JButton("Back");

        filterYearBox = new JComboBox<>();
        filterMonthBox = new JComboBox<>();
        filterDayBox = new JComboBox<>();
        filterStatusBox = new JComboBox<>();
        searchField = new JTextField(15);

        setupFilters();

        JPanel filterPanel = new JPanel(new GridLayout(2, 5, 5, 5));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filters"));
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
        JPanel refreshPanel = new JPanel(new FlowLayout());
        refreshPanel.add(refreshButton);
        topPanel.add(refreshPanel, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(payButton);
        bottomPanel.add(backButton);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(appointmentList), new JScrollPane(detailArea));
        split.setDividerLocation(400);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(split, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        filterYearBox.addActionListener(e -> {
            refreshDayBox();
            loadAppointments();
        });
        filterMonthBox.addActionListener(e -> {
            refreshDayBox();
            loadAppointments();
        });
        filterDayBox.addActionListener(e -> {
            if (!updatingFilterDays) {
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
        payButton.addActionListener(this);
        refreshButton.addActionListener(this);
        backButton.addActionListener(this);

        loadAppointments();
        frame.setVisible(true);
    }

    private void setupFilters() {
        filterYearBox.addItem("All");
        filterMonthBox.addItem("All");
        filterDayBox.addItem("All");
        filterStatusBox.addItem("All");
        filterStatusBox.addItem("Pending");
        filterStatusBox.addItem("Partial");
        for (int y = 2025; y <= 2035; y++) {
            filterYearBox.addItem(String.valueOf(y));
        }
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        for (String m : months) {
            filterMonthBox.addItem(m);
        }
        rebuildDayBox(31);
    }

    private void refreshDayBox() {
        String month = (String) filterMonthBox.getSelectedItem();
        String yearStr = (String) filterYearBox.getSelectedItem();
        int days = 31;
        if (!"All".equals(month)) {
            if ("All".equals(yearStr)) {
                switch (month) {
                    case "February": days = 28; break;
                    case "April": case "June": case "September": case "November": days = 30; break;
                }
            } else {
                int year = Integer.parseInt(yearStr);
                int monthValue = monthToInt(month) + 1;
                days = YearMonth.of(year, monthValue).lengthOfMonth();
            }
        }
        rebuildDayBox(days);
    }

    private void rebuildDayBox(int max) {
        updatingFilterDays = true;
        String prev = (String) filterDayBox.getSelectedItem();
        filterDayBox.removeAllItems();
        filterDayBox.addItem("All");
        for (int i = 1; i <= max; i++) filterDayBox.addItem(String.valueOf(i));
        filterDayBox.setSelectedItem(prev != null ? prev : "All");
        updatingFilterDays = false;
    }

    private void loadAppointments() {
        listModel.clear(); selectedAppointment = null; payButton.setEnabled(false);
        String year = (String) filterYearBox.getSelectedItem();
        String month = (String) filterMonthBox.getSelectedItem();
        String day = (String) filterDayBox.getSelectedItem();
        String status = (String) filterStatusBox.getSelectedItem();
        String keyword = searchField.getText().toLowerCase();

        for (Appointment a : DataStore.allAppointments) {
            Payment p = LookupService.getPaymentByAppointmentID(a.getAppointmentID());
            if (p == null || p.isFullyPaid()) continue;

            String[] parts = a.getDate().split(" ");
            if (!"All".equals(year)   && !parts[2].equals(year))   continue;
            if (!"All".equals(month)  && !parts[1].equals(month))  continue;
            if (!"All".equals(day)    && !parts[0].equals(day))    continue;
            if (!"All".equals(status) && !p.getStatus().equalsIgnoreCase(status)) continue;

            String customerName = LookupService.getCustomerNameByID(a.getCustomerID());
            if (!keyword.isEmpty() && !(a.getAppointmentID() + " " + customerName).toLowerCase().contains(keyword)) continue;

            listModel.addElement(a.getAppointmentID() + " - " + customerName + " | " + p.getPaymentSummary());
        }
        if (listModel.isEmpty()) detailArea.setText("No pending or partial payment records found.");
    }

    private void showDetails() {
        String sel = appointmentList.getSelectedValue();
        if (sel == null) return;
        String id = sel.split(" - ")[0];
        Appointment a = LookupService.getAppointmentByID(id);
        if (a == null) return;

        selectedAppointment = a;
        Payment p = LookupService.getPaymentByAppointmentID(id);
        String customer = LookupService.getCustomerNameByID(a.getCustomerID());
        String service  = LookupService.getServiceNameByID(a.getServiceID());

        StringBuilder receipts = new StringBuilder();
        if (p != null)
            for (Receipt r : DataStore.allReceipts)
                if (r.getPaymentID().equals(p.getPaymentID()))
                    receipts.append("\nReceipt: ").append(r.getReceiptID())
                            .append(" | RM ").append(String.format("%.2f", r.getAmountPaid()))
                            .append(" | ").append(r.getMethod());

        if (receipts.length() == 0) receipts.append("\nNo receipts yet.");

        detailArea.setText(
                "Appointment Details:\n" +
                a.getAppointmentSummary() +
                "\nCustomer: " + customer + "\nService: " + service +
                "\nDate: " + a.getDate() + "\nTime: " + a.getFullTimeRange() +
                "\n\nPayment Status:\n" + (p != null ? p.getPaymentSummary() : "No Record") +
                "\n\n--- Receipt History ---" + receipts);
    }

    private void openPaymentForm() {
        if (selectedAppointment == null) { JOptionPane.showMessageDialog(frame, "Select an appointment first"); return; }

        double serviceFee  = LookupService.getServicePriceByID(selectedAppointment.getServiceID());
        Payment existing   = LookupService.getPaymentByAppointmentID(selectedAppointment.getAppointmentID());
        double alreadyPaid = existing != null ? existing.getTotalPaid() : 0.0;
        double balanceDue  = Math.round((serviceFee - alreadyPaid) * 100.0) / 100.0;

        JFrame pf = new JFrame("Make Payment");
        pf.setSize(460, 490); pf.setLayout(null); pf.setLocationRelativeTo(frame);

        // Info
        JLabel feeLabel  = new JLabel("Service Fee:   RM " + String.format("%.2f", serviceFee));
        JLabel paidLabel = new JLabel("Already Paid:  RM " + String.format("%.2f", alreadyPaid));
        JLabel balLabel  = new JLabel("Balance Due:   RM " + String.format("%.2f", balanceDue));
        balLabel.setFont(balLabel.getFont().deriveFont(Font.BOLD));
        feeLabel.setBounds(30,20,380,25);
        paidLabel.setBounds(30,48,380,25);
        balLabel.setBounds(30,76,380,25);
        pf.add(feeLabel);
        pf.add(paidLabel);
        pf.add(balLabel);
        JSeparator s1 = new JSeparator();
        s1.setBounds(30,108,390,2);
        pf.add(s1);

        JLabel amtLbl = new JLabel("Amount Tendered (RM):");
        amtLbl.setBounds(30,118,185,30); pf.add(amtLbl);
        JTextField amountField = new JTextField();
        amountField.setBounds(225,118,175,30);
        pf.add(amountField);

        JLabel methodLbl = new JLabel("Payment Method:");
        methodLbl.setBounds(30,162,140,30);
        pf.add(methodLbl);
        
        JRadioButton cashRadio = new JRadioButton("Cash", true);
        JRadioButton cardRadio = new JRadioButton("Card");
        cashRadio.setBounds(180,162,80,30);
        cardRadio.setBounds(268,162,80,30);
        ButtonGroup bg = new ButtonGroup();
        bg.add(cashRadio); bg.add(cardRadio);
        pf.add(cashRadio);
        pf.add(cardRadio);

        JLabel cardLabel = new JLabel("Card Number:");
        cardLabel.setBounds(30,200,130,30);
        cardLabel.setVisible(false);
        pf.add(cardLabel);
        JTextField cardField = new JTextField();
        cardField.setBounds(170,200,230,30);
        cardField.setVisible(false);
        pf.add(cardField);
        JLabel noteLabel = new JLabel("* Card payments cannot exceed the balance due.");
        noteLabel.setFont(noteLabel.getFont().deriveFont(Font.ITALIC, 11f));
        noteLabel.setBounds(30,232,390,20);
        noteLabel.setForeground(Color.GRAY);
        noteLabel.setVisible(false);
        pf.add(noteLabel);

        JSeparator s2 = new JSeparator(); s2.setBounds(30,256,390,2); pf.add(s2);
        JLabel previewAmt = new JLabel("Amount Tendered:   RM -");
        previewAmt.setBounds(30,266,390,22);
        pf.add(previewAmt);
        JLabel previewChange = new JLabel("Change:            RM -");
        previewChange.setFont(previewChange.getFont().deriveFont(Font.BOLD));
        previewChange.setBounds(30,292,390,22);
        pf.add(previewChange);
        JLabel previewBal = new JLabel("Remaining Balance: RM -");
        previewBal.setBounds(30,318,390,22);
        pf.add(previewBal);

        JButton confirm = new JButton("Confirm");
        confirm.setBounds(60,400,130,35);
        pf.add(confirm);
        JButton cancel  = new JButton("Cancel");
        cancel.setBounds(250,400,130,35);
        pf.add(cancel);

        Runnable preview = () -> updatePreview(amountField, balanceDue, cardRadio.isSelected(), previewAmt, previewChange, previewBal);
        cardRadio.addActionListener(e -> {
            cardLabel.setVisible(true);
            cardField.setVisible(true);
            noteLabel.setVisible(true);
            preview.run();
        });
        cashRadio.addActionListener(e -> {
            cardLabel.setVisible(false);
            cardField.setVisible(false);
            noteLabel.setVisible(false);
            preview.run();
        });
        
        amountField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                preview.run(); }
        });
        
        cancel.addActionListener(e -> pf.dispose());

        confirm.addActionListener(e -> {
            try {
                double tendered = Double.parseDouble(amountField.getText().trim());
                boolean isCard  = cardRadio.isSelected();
                if (tendered <= 0) { JOptionPane.showMessageDialog(pf, "Amount must be greater than zero."); return; }
                if (isCard && tendered > balanceDue) { JOptionPane.showMessageDialog(pf, "Card payment cannot exceed balance (RM " + String.format("%.2f", balanceDue) + ")"); return; }
                if (isCard && !cardField.getText().trim().matches("\\d{16}")) { JOptionPane.showMessageDialog(pf, "Card number must be exactly 16 digits."); return; }

                // — delegate all logic to controller —
                PaymentController.PaymentResult result = PaymentController.processPayment(selectedAppointment, tendered, isCard);
                
                FileHandler.writeSystemLog("(" + userRole + ")" + userID + " paid appointment " + selectedAppointment.getAppointmentID() + " | receipt " + result.receipt.getReceiptID() + " | method " + result.receipt.getMethod() + " | RM " + String.format("%.2f", result.receipt.getAmountPaid()));

                pf.dispose();
                loadAppointments();
                detailArea.setText("");

                StringBuilder msg = new StringBuilder("Payment recorded successfully!\n\n");
                msg.append(String.format("Receipt ID   : %s%n", result.receipt.getReceiptID()));
                msg.append(String.format("Method       : %s%n", result.receipt.getMethod()));
                msg.append(String.format("Service Fee  : RM %.2f%n", serviceFee));
                msg.append(String.format("Amount Paid  : RM %.2f%n", result.receipt.getAmountPaid()));
                if (!isCard) {
                    msg.append(String.format("Tendered     : RM %.2f%n", tendered));
                    msg.append(String.format("Change       : RM %.2f%n", result.changeGiven));
                }
                msg.append(String.format("Remaining    : RM %.2f%n", result.newBalance));
                msg.append(String.format("Status       : %s", result.status));
                JOptionPane.showMessageDialog(frame, msg.toString(), "Payment Successful", JOptionPane.INFORMATION_MESSAGE);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(pf, "Please enter a valid number.");
            }
        });

        pf.setVisible(true);
    }

    private void updatePreview(JTextField amountField, double balanceDue, boolean isCard,
                               JLabel amtLbl, JLabel changeLbl, JLabel balLbl) {
        try {
            double t = Double.parseDouble(amountField.getText().trim());
            if (isCard) {
                double paid = Math.min(t, balanceDue);
                amtLbl.setText(String.format("Amount Tendered:   RM %.2f", t));
                changeLbl.setText("Change:            RM 0.00  (card)");
                balLbl.setText(String.format("Remaining Balance: RM %.2f", Math.max(0, balanceDue - paid)));
            } else {
                double change = Math.max(0, t - balanceDue);
                double newBal = t >= balanceDue ? 0.0 : balanceDue - t;
                amtLbl.setText(String.format("Amount Tendered:   RM %.2f", t));
                changeLbl.setText(String.format("Change:            RM %.2f", change));
                balLbl.setText(String.format("Remaining Balance: RM %.2f", newBal));
            }
        } catch (NumberFormatException ignored) {
            amtLbl.setText("Amount Tendered:   RM -");
            changeLbl.setText("Change:            RM -");
            balLbl.setText("Remaining Balance: RM -");
        }
    }

    private int monthToInt(String m) {
        String[] months = {"January","February","March","April","May","June", "July","August","September","October","November","December"};
        for (int i = 0; i < months.length; i++) {
            if (months[i].equals(m)) {
                return i;
            }
        }
        return 0;
    }
}