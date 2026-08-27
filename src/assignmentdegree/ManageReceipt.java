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

public class ManageReceipt implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == editButton) {
            editReceipt();
        } else if (e.getSource() == deleteButton) {
            deleteReceipt();
        } else if (e.getSource() == refreshButton) {
            loadReceipts();
            clearDetail();
        } else if (e.getSource() == clearFilterButton) {
            resetFilters();
            loadReceipts();
        } else if (e.getSource() == backButton) {
            frame.dispose();
            if (filterPaymentID != null) {
                ManagePayment managePayment = new ManagePayment();
                managePayment.openPage(userID, userRole);
            } else {
                CounterStaffFunction counterStaffFunction = new CounterStaffFunction();
                counterStaffFunction.openCounterStaffFunction(userID, userRole);
            }
        }
    }
    
    private JFrame frame;
    private String userID, userRole, filterPaymentID;

    private DefaultListModel<String> listModel;
    private JList<String> receiptList;
    private JTextArea detailArea;

    private JComboBox<String> filterYearBox, filterMonthBox, filterDayBox, filterStatusBox, filterMethodBox;
    private JTextField searchField;
    private JButton clearFilterButton, refreshButton, editButton, deleteButton, backButton;
    private boolean updatingDays = false;

    private Receipt selectedReceipt = null;

    public void openPage(String userID, String userRole, String filterPaymentID) {
        this.userID = userID;
        this.userRole = userRole;
        this.filterPaymentID = filterPaymentID;
        
        if (filterPaymentID == null) {
            FileHandler.writeSystemLog("(" + userRole + ") " + userID + " opened Manage Receipt page (All Payments)" + userRole + " function.");
        } else {
            FileHandler.writeSystemLog("(" + userRole + ") " + userID + " opened Manage Receipt page for payment: " + filterPaymentID + userRole + " function.");
        }

        frame = new JFrame(filterPaymentID != null ? "Receipts for Payment " + filterPaymentID : "Manage Receipts");
        frame.setSize(1100, 640);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null);

        filterYearBox = new JComboBox<>();
        filterMonthBox = new JComboBox<>();
        filterDayBox = new JComboBox<>();
        filterStatusBox = new JComboBox<>();
        filterMethodBox = new JComboBox<>();
        searchField = new JTextField(12);
        clearFilterButton = new JButton("Clear Filter");
        refreshButton = new JButton("Refresh");

        setupFilters();

        JPanel filterPanel = new JPanel(new GridLayout(2, 6, 8, 8));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Receipts"));
        filterPanel.add(new JLabel("Year"));
        filterPanel.add(new JLabel("Month"));
        filterPanel.add(new JLabel("Day"));
        filterPanel.add(new JLabel("Status"));
        filterPanel.add(new JLabel("Method"));
        filterPanel.add(new JLabel("Search"));
        filterPanel.add(filterYearBox);
        filterPanel.add(filterMonthBox);
        filterPanel.add(filterDayBox);
        filterPanel.add(filterStatusBox);
        filterPanel.add(filterMethodBox);
        filterPanel.add(searchField);

        JPanel topBtn = new JPanel();
        topBtn.add(refreshButton);
        topBtn.add(clearFilterButton);
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(filterPanel, BorderLayout.CENTER);
        topPanel.add(topBtn, BorderLayout.EAST);

        listModel = new DefaultListModel<>();
        receiptList = new JList<>(listModel);
        receiptList.setFont(new Font("Monospaced", Font.PLAIN, 13));
        receiptList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onSelected();
            }
        });

        detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        detailArea.setText("\n  Select a receipt to view details.");

        JPanel leftP = new JPanel(new BorderLayout());
        leftP.setBorder(BorderFactory.createTitledBorder("Receipt List"));
        leftP.add(new JScrollPane(receiptList), BorderLayout.CENTER);
        JPanel rightP = new JPanel(new BorderLayout());
        rightP.setBorder(BorderFactory.createTitledBorder("Receipt Details"));
        rightP.add(new JScrollPane(detailArea), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftP, rightP);
        split.setDividerLocation(480);

        editButton = new JButton("Edit Receipt");
        deleteButton = new JButton("Delete Receipt");
        backButton = new JButton(filterPaymentID != null ? "Back to Payment" : "Back");
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);

        editButton.addActionListener(this);
        deleteButton.addActionListener(this);
        backButton.addActionListener(this);
        refreshButton.addActionListener(this);
        clearFilterButton.addActionListener(this);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(backButton);
        bottomPanel.add(editButton);
        bottomPanel.add(deleteButton);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(split, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        loadReceipts();
        frame.setVisible(true);
    }

    private void setupFilters() {
        filterYearBox.addItem("All");
        filterMonthBox.addItem("All");
        filterDayBox.addItem("All");
        filterStatusBox.addItem("All");
        filterStatusBox.addItem("Partial");
        filterStatusBox.addItem("Successful");
        filterMethodBox.addItem("All");
        filterMethodBox.addItem("Cash");
        filterMethodBox.addItem("Card");
        for (int y = 2025; y <= 2035; y++) {
            filterYearBox.addItem(String.valueOf(y));
        }
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        for (String m : months) {
            filterMonthBox.addItem(m);
        }
        rebuildDayBox(31);

        filterYearBox.addActionListener(e -> {
            refreshDayBox();
            loadReceipts();
        });
        filterMonthBox.addActionListener(e -> {
            refreshDayBox();
            loadReceipts();
        });
        filterDayBox.addActionListener(e -> {
            if (!updatingDays) {
                loadReceipts();
            }
        });
        filterStatusBox.addActionListener(e -> loadReceipts());
        filterMethodBox.addActionListener(e -> loadReceipts());
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                loadReceipts();
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
        filterMethodBox.setSelectedIndex(0);
        searchField.setText("");
    }

    private void loadReceipts() {
        listModel.clear(); selectedReceipt = null;
        editButton.setEnabled(false); deleteButton.setEnabled(false);
        detailArea.setText("\n  Select a receipt to view details.");

        String yearF   = (String) filterYearBox.getSelectedItem();
        String monthF  = (String) filterMonthBox.getSelectedItem();
        String dayF    = (String) filterDayBox.getSelectedItem();
        String statusF = (String) filterStatusBox.getSelectedItem();
        String methodF = (String) filterMethodBox.getSelectedItem();
        String keyword = searchField.getText().trim().toLowerCase();

        for (Receipt r : DataStore.allReceipts) {
            if (filterPaymentID != null && !r.getPaymentID().equals(filterPaymentID)) continue;
            if (!"All".equals(statusF) && !r.getStatus().equalsIgnoreCase(statusF)) continue;
            if (!"All".equals(methodF) && !r.getMethod().equalsIgnoreCase(methodF)) continue;

            Appointment a = LookupService.getAppointmentByID(r.getAppointmentID());
            if (a != null && (!"All".equals(yearF) || !"All".equals(monthF) || !"All".equals(dayF))) {
                String[] parts = a.getDate().split(" ");
                if (!"All".equals(yearF)  && !parts[2].equals(yearF))  continue;
                if (!"All".equals(monthF) && !parts[1].equals(monthF)) continue;
                if (!"All".equals(dayF)   && !parts[0].equals(dayF))   continue;
            }

            String customer = a != null ? LookupService.getCustomerNameByID(a.getCustomerID()) : "?";
            String searchText = (r.getReceiptID()+" "+r.getPaymentID()+" "+r.getAppointmentID()+" "+customer).toLowerCase();
            if (!keyword.isEmpty() && !searchText.contains(keyword)) continue;

            listModel.addElement(r.getReceiptID() + " - " + r.getPaymentID() + " - " + customer
                    + "  RM " + String.format("%.2f", r.getAmountPaid())
                    + "  " + r.getMethod() + "  " + r.getDate() + "  [" + r.getStatus() + "]");
        }
        if (listModel.isEmpty()) detailArea.setText("\n  No receipts found.");
    }

    private void onSelected() {
        String sel = receiptList.getSelectedValue();
        if (sel == null) { clearDetail(); return; }
        String receiptID = sel.split(" - ")[0].trim();
        for (Receipt r : DataStore.allReceipts) {
            if (r.getReceiptID().equals(receiptID)) {
                selectedReceipt = r;
                showDetail(r);
                editButton.setEnabled(true); deleteButton.setEnabled(true);
                return;
            }
        }
    }

    private void showDetail(Receipt r) {
        Appointment a = LookupService.getAppointmentByID(r.getAppointmentID());
        String customer  = a != null ? LookupService.getCustomerNameByID(a.getCustomerID())          : "Unknown";
        String service   = a != null ? LookupService.getServiceNameByID(a.getServiceID())            : "Unknown";
        String techNames = a != null ? LookupService.getTechnicianNames(a.getTechnicianIDs())        : "Unknown";
        String staff     = a != null ? LookupService.getStaffNameByID(a.getCounterStaffID())         : "Unknown";
        String apptDate  = a != null ? a.getDate()                                                   : "Unknown";
        String apptTime  = a != null ? a.getStartTime() + " – " + a.getEndTime()                    : "Unknown";
        boolean isCash   = r.getMethod().equalsIgnoreCase("Cash");
        double tendered  = Math.round((r.getAmountPaid() + r.getChange()) * 100.0) / 100.0;

        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n     APU AUTOMOTIVE SERVICE CENTRE\n               RECEIPT\n========================================\n\n");
        sb.append(String.format("Receipt ID     : %s%n", r.getReceiptID()));
        sb.append(String.format("Payment ID     : %s%n", r.getPaymentID()));
        sb.append(String.format("Payment Date   : %s%n", r.getDate()));
        sb.append(String.format("Payment Method : %s%n", r.getMethod()));
        sb.append(String.format("Status         : %s%n%n", r.getStatus()));
        sb.append("----------------------------------------\nAPPOINTMENT DETAILS\n----------------------------------------\n");
        sb.append(String.format("Appointment ID : %s%n", r.getAppointmentID()));
        sb.append(String.format("Appt Date      : %s%n", apptDate));
        sb.append(String.format("Time           : %s%n", apptTime));
        sb.append(String.format("Customer       : %s%n", customer));
        sb.append(String.format("Service        : %s%n", service));
        sb.append(String.format("Technician(s)  : %s%n", techNames));
        sb.append(String.format("Counter Staff  : %s%n%n", staff));
        sb.append("----------------------------------------\nPAYMENT SUMMARY\n----------------------------------------\n");
        sb.append(String.format("Service Fee    : RM %.2f%n", r.getTotalFee()));
        sb.append(String.format("Amount Paid    : RM %.2f%n", r.getAmountPaid()));
        if (isCash) {
            sb.append(String.format("Tendered       : RM %.2f%n", tendered));
            sb.append(String.format("Change Given   : RM %.2f%n", r.getChange()));
        }
        sb.append(String.format("Total Paid     : RM %.2f%n", r.getTotalPaid()));
        sb.append(String.format("Balance Due    : RM %.2f%n", r.getBalance()));
        sb.append("----------------------------------------\n\n   Thank you for choosing APU-ASC!\n========================================");
        detailArea.setText(sb.toString()); detailArea.setCaretPosition(0);
    }

    private void editReceipt() {
        if (selectedReceipt == null) return;

        JDialog dialog = new JDialog(frame, "Edit Receipt " + selectedReceipt.getReceiptID(), true);
        dialog.setSize(420, 310); dialog.setLayout(null); dialog.setLocationRelativeTo(frame);

        JLabel methodLabel = new JLabel("Payment Method:"); methodLabel.setBounds(30,30,140,28); dialog.add(methodLabel);
        JComboBox<String> methodBox = new JComboBox<>(new String[]{"Cash","Card"});
        methodBox.setSelectedItem(selectedReceipt.getMethod()); methodBox.setBounds(180,30,180,28); dialog.add(methodBox);

        JLabel cardLabel = new JLabel("Card Number:"); cardLabel.setBounds(30,70,140,28);
        cardLabel.setVisible(selectedReceipt.getMethod().equalsIgnoreCase("Card")); dialog.add(cardLabel);
        JTextField cardField = new JTextField(); cardField.setBounds(180,70,180,28);
        cardField.setVisible(selectedReceipt.getMethod().equalsIgnoreCase("Card")); dialog.add(cardField);

        methodBox.addActionListener(e -> { boolean c = "Card".equals(methodBox.getSelectedItem()); cardLabel.setVisible(c); cardField.setVisible(c); });

        JLabel dateLabel = new JLabel("Payment Date:"); dateLabel.setBounds(30,115,140,28); dialog.add(dateLabel);
        String[] existingParts = selectedReceipt.getDate().split(" ");
        int existDay = Integer.parseInt(existingParts[0]); String existMon = existingParts[1]; int existYear = Integer.parseInt(existingParts[2]);

        JComboBox<Integer> dayBox  = new JComboBox<>();
        JComboBox<String>  monBox  = new JComboBox<>();
        JComboBox<Integer> yearBox = new JComboBox<>();
        String[] months3 = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        for (String m : months3) monBox.addItem(m); monBox.setSelectedItem(existMon);
        for (int y = 2025; y <= 2035; y++) yearBox.addItem(y); yearBox.setSelectedItem(existYear);
        rebuildEditDayBox(dayBox, existMon, existYear); dayBox.setSelectedItem(existDay);

        ActionListener refreshDays = e -> rebuildEditDayBox(dayBox, (String)monBox.getSelectedItem(), (Integer)yearBox.getSelectedItem());
        monBox.addActionListener(refreshDays); yearBox.addActionListener(refreshDays);

        dayBox.setBounds(180,115,55,28); monBox.setBounds(242,115,65,28); yearBox.setBounds(314,115,70,28);
        dialog.add(dayBox); dialog.add(monBox); dialog.add(yearBox);

        JButton saveBtn = new JButton("Save"); saveBtn.setBounds(60,230,120,32); dialog.add(saveBtn);
        JButton cancelBtn = new JButton("Cancel"); cancelBtn.setBounds(220,230,120,32); dialog.add(cancelBtn);
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            String newMethod = (String) methodBox.getSelectedItem();
            if ("Card".equals(newMethod) && !cardField.getText().trim().isEmpty()
                    && !cardField.getText().trim().matches("\\d{16}")) {
                JOptionPane.showMessageDialog(dialog, "Card number must be exactly 16 digits."); return;
            }
            if ("Card".equals(newMethod) && selectedReceipt.getChange() != 0.0) {
                int ok = JOptionPane.showConfirmDialog(dialog, "Switching to Card will set Change to RM 0.00. Continue?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (ok != JOptionPane.YES_OPTION) return;
            }
            String newDate = String.format("%02d %s %d", (Integer)dayBox.getSelectedItem(), (String)monBox.getSelectedItem(), (Integer)yearBox.getSelectedItem());

            ReceiptController.editReceipt(selectedReceipt, newMethod, newDate);
            
            FileHandler.writeSystemLog("(" + userRole + ") " + userID + " edited receipt: " + selectedReceipt.getReceiptID() + " [Payment: " + selectedReceipt.getPaymentID() + ", Method: " + newMethod + ", Date: " + newDate + "]");

            dialog.dispose();
            JOptionPane.showMessageDialog(frame, "Receipt updated successfully.");
            Receipt temp = selectedReceipt;
            loadReceipts();
            selectedReceipt = temp;
            showDetail(selectedReceipt);
        });

        dialog.setVisible(true);
    }

    private void rebuildEditDayBox(JComboBox<Integer> dayBox, String mon3, int year) {
        Object prev = dayBox.getSelectedItem();
        dayBox.removeAllItems();
        int max = LookupService.getDaysInMonth(monthAbbrevToFull(mon3), year);
        for (int d = 1; d <= max; d++) dayBox.addItem(d);
        if (prev instanceof Integer) dayBox.setSelectedItem(Math.min((Integer)prev, max));
    }

    private String monthAbbrevToFull(String a) {
        switch (a) {
            case "Jan": return "January";  case "Feb": return "February"; case "Mar": return "March";
            case "Apr": return "April";    case "May": return "May";      case "Jun": return "June";
            case "Jul": return "July";     case "Aug": return "August";   case "Sep": return "September";
            case "Oct": return "October";  case "Nov": return "November"; case "Dec": return "December";
            default: return a;
        }
    }

    private void deleteReceipt() {
        if (selectedReceipt == null) return;
        int remaining = 0;
        for (Receipt r : DataStore.allReceipts)
            if (r.getPaymentID().equals(selectedReceipt.getPaymentID())
                    && !r.getReceiptID().equals(selectedReceipt.getReceiptID())) remaining++;

        String consequence = remaining == 0
                ? "This is the only receipt.\nThe entire payment record will be deleted\nand the appointment reset to Scheduled."
                : "The parent payment totals will be recalculated.\n(" + remaining + " receipt(s) will remain.)";

        int confirm = JOptionPane.showConfirmDialog(frame,
                "Delete receipt " + selectedReceipt.getReceiptID() + "?\n\n" + consequence,
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        ReceiptController.deleteReceiptAndRecalculate(selectedReceipt.getReceiptID());
        
        FileHandler.writeSystemLog("(" + userRole + ") " + userID + " deleted receipt: " + selectedReceipt.getReceiptID() + " [Payment: " + selectedReceipt.getPaymentID() + "]");

        JOptionPane.showMessageDialog(frame, "Receipt deleted successfully.");
        selectedReceipt = null;
        loadReceipts(); clearDetail();
    }

    private void clearDetail() {
        detailArea.setText("\n  Select a receipt to view details.");
        selectedReceipt = null;
        editButton.setEnabled(false); deleteButton.setEnabled(false);
    }
}