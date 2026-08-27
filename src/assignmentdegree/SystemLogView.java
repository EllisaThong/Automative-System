package assignmentdegree;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class SystemLogView {
    
    private JComboBox<String> yearComboBox;
    private JComboBox<String> monthComboBox;
    private JTextArea logArea;
    private JLabel statusLabel;
    
    private String managerID;
    private String userRole;
    
    private String selectedYear;
    private String selectedMonth;
    
    private static final String[] months = {
        "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
        "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
    };
    
    private static final String[] years = {
        "2025", "2026", "2027", "2028", "2029", "2030", 
        "2031", "2032", "2033", "2034", "2035"
    };

    public void openSystemLogView(String managerID, String role) {
        this.managerID = managerID;
        this.userRole = role;
        
        FileHandler.writeSystemLog("(" + userRole + ")" + managerID + " opened the view system log " + userRole + " function.");

        selectedYear = "2025";
        selectedMonth = "JANUARY";

        JFrame frame = new JFrame("System Log Viewer");
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new FlowLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("Log Filter"));

        yearComboBox = new JComboBox<>(years);
        monthComboBox = new JComboBox<>(months);
        
        yearComboBox.setSelectedItem(selectedYear);
        monthComboBox.setSelectedItem(selectedMonth);

        filterPanel.add(new JLabel("Year:"));
        filterPanel.add(yearComboBox);
        filterPanel.add(new JLabel("Month:"));
        filterPanel.add(monthComboBox);

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Showing logs: 0");
        statusPanel.add(statusLabel);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JButton clearButton = new JButton("Clear Filtered Logs");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to clear logs for " + selectedMonth + " " + selectedYear + "?", "Confirm Clear", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    clearSystemLogs(selectedYear, selectedMonth);
                }
            }
        });

        JButton backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                ManagerFunction managerFunction = new ManagerFunction();
                managerFunction.openManagerFunction(managerID, userRole);
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(clearButton);
        buttonPanel.add(backButton);

        frame.add(filterPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(statusPanel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        yearComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedYear = (String) yearComboBox.getSelectedItem();
                filterLogs();
            }
        });

        monthComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedMonth = (String) monthComboBox.getSelectedItem();
                filterLogs();
            }
        });

        filterLogs();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void filterLogs() {
        String intMonth = String.format("%02d", getMonthNumber(selectedMonth));
        logArea.setText("");

        try (BufferedReader reader = new BufferedReader(new FileReader("systemLogs.txt"))) {
            String line;
            int logCount = 0;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split(" ");
                if (parts.length < 2) {
                    continue;
                }

                String date = parts[0]; // yyyy-MM-dd
                String[] dateParts = date.split("-");
                if (dateParts.length < 2) {
                    continue;
                }

                String logYear = dateParts[0];
                String logMonth = dateParts[1];

                if (logYear.equals(selectedYear) && logMonth.equals(intMonth)) {
                    logArea.append(line + "\n");
                    logCount++;
                }
            }

            statusLabel.setText("Showing logs: " + logCount);

            if (logCount == 0) {
                logArea.append("\nNo logs found for " + selectedMonth + " " + selectedYear);
            }
        } catch (IOException e) {
            // File might not exist yet
            statusLabel.setText("Showing logs: 0");
            logArea.append("\nNo logs found (Log file does not exist yet).");
        }
    }

    private void clearSystemLogs(String year, String month) {
        int yearInt = Integer.parseInt(year);
        FileHandler.clearLogFile(yearInt, month);
        FileHandler.writeSystemLog("(Manager) " + managerID + " cleared the system logs of " + month + ", " + year);
        filterLogs();
    }
    
    public static int getMonthNumber(String monthName) {
        switch (monthName.toUpperCase()) {
            case "JANUARY":
                return 1;
            case "FEBRUARY":
                return 2;
            case "MARCH":
                return 3;
            case "APRIL":
                return 4;
            case "MAY":
                return 5;
            case "JUNE":
                return 6;
            case "JULY":
                return 7;
            case "AUGUST":
                return 8;
            case "SEPTEMBER":
                return 9;
            case "OCTOBER":
                return 10;
            case "NOVEMBER":
                return 11;
            case "DECEMBER":
                return 12;
            default:
                return 0;
        }
    }
}
