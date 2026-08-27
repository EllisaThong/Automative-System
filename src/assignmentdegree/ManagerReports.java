package assignmentdegree;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ManagerReports implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == refreshButton) {
            loadReports();
        } else if (e.getSource() == clearFilterButton) {
            resetFilters();
            updateDatePanelVisibility();
            loadReports();
        } else if (e.getSource() == backButton) {
            frame.dispose();
            ManagerFunction managerFunction = new ManagerFunction();
            managerFunction.openManagerFunction(userID, userRole);
        }
    }
    
    private JFrame frame;
    private JTextArea reportArea;
    private JButton refreshButton, backButton, clearFilterButton;
    private JComboBox<String> reportTypeBox;
    private JComboBox<String> fromYearBox, fromMonthBox, fromDayBox;
    private JComboBox<String> toYearBox, toMonthBox, toDayBox;
    private JPanel fromDatePanel, toDatePanel;
    private String userID, userRole;

    public void openPage(String userID, String userRole) {
        this.userID = userID;
        this.userRole = userRole;
        
        FileHandler.writeSystemLog("(" + userRole + ")" + userID + " opened the view report " + userRole + " function.");

        frame = new JFrame("Analysed Reports");
        frame.setSize(950, 650);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null);

        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        reportArea.setMargin(new Insets(10, 10, 10, 10));

        refreshButton = new JButton("Refresh");
        backButton = new JButton("Back");
        clearFilterButton = new JButton("Clear Filter");

        reportTypeBox = new JComboBox<>(new String[]{"All", "Today", "This Month", "This Year", "Custom Range"});

        fromYearBox = new JComboBox<>();
        fromMonthBox = new JComboBox<>();
        fromDayBox = new JComboBox<>();
        toYearBox = new JComboBox<>();
        toMonthBox = new JComboBox<>();
        toDayBox = new JComboBox<>();

        setupDateBoxes();

        JPanel filterPanel = new JPanel(new BorderLayout(10, 10));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Report Filters"));

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));

        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        typePanel.add(new JLabel("Report Type"));
        reportTypeBox.setPreferredSize(new Dimension(180, 25));
        typePanel.add(reportTypeBox);

        fromDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        fromDatePanel.add(new JLabel("From Date"));
        fromDayBox.setPreferredSize(new Dimension(90, 25));
        fromMonthBox.setPreferredSize(new Dimension(130, 25));
        fromYearBox.setPreferredSize(new Dimension(100, 25));
        fromDatePanel.add(fromDayBox);
        fromDatePanel.add(fromMonthBox);
        fromDatePanel.add(fromYearBox);

        toDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toDatePanel.add(new JLabel("To Date  "));
        toDayBox.setPreferredSize(new Dimension(90, 25));
        toMonthBox.setPreferredSize(new Dimension(130, 25));
        toYearBox.setPreferredSize(new Dimension(100, 25));
        toDatePanel.add(toDayBox);
        toDatePanel.add(toMonthBox);
        toDatePanel.add(toYearBox);

        fieldsPanel.add(typePanel);
        fieldsPanel.add(fromDatePanel);
        fieldsPanel.add(toDatePanel);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        btnRow.add(clearFilterButton);
        btnRow.add(refreshButton);

        filterPanel.add(fieldsPanel, BorderLayout.CENTER);
        filterPanel.add(btnRow, BorderLayout.EAST);
        
        ActionListener dateChangeListener = e -> {
            if ("Custom Range".equals(reportTypeBox.getSelectedItem())) {
                loadReports();
            }
        };

        fromDayBox.addActionListener(dateChangeListener);
        fromMonthBox.addActionListener(dateChangeListener);
        fromYearBox.addActionListener(dateChangeListener);
        toDayBox.addActionListener(dateChangeListener);
        toMonthBox.addActionListener(dateChangeListener);
        toYearBox.addActionListener(dateChangeListener);

        reportTypeBox.addActionListener(e -> {
            updateDatePanelVisibility();
            loadReports();
        });
        refreshButton.addActionListener(this);
        clearFilterButton.addActionListener(this);
        backButton.addActionListener(this);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(backButton);

        frame.add(filterPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        updateDatePanelVisibility();
        loadReports();
        frame.setVisible(true);
    }

    private void setupDateBoxes() {
        fromDayBox.addItem("All"); toDayBox.addItem("All");
        for (int d = 1; d <= 31; d++) { fromDayBox.addItem(String.valueOf(d)); toDayBox.addItem(String.valueOf(d)); }

        String[] months = {"All","January","February","March","April","May","June","July","August","September","October","November","December"};
        for (String m : months) { fromMonthBox.addItem(m); toMonthBox.addItem(m); }

        fromYearBox.addItem("All"); toYearBox.addItem("All");
        for (int y = 2025; y <= 2035; y++) { fromYearBox.addItem(String.valueOf(y)); toYearBox.addItem(String.valueOf(y)); }
    }

    private void updateDatePanelVisibility() {
        boolean custom = "Custom Range".equals(reportTypeBox.getSelectedItem());
        fromDatePanel.setVisible(custom); toDatePanel.setVisible(custom);
        frame.revalidate(); frame.repaint();
    }

    private void resetFilters() {
        reportTypeBox.setSelectedIndex(0);
        fromDayBox.setSelectedIndex(0);
        fromMonthBox.setSelectedIndex(0);
        fromYearBox.setSelectedIndex(0);
        toDayBox.setSelectedIndex(0);
        toMonthBox.setSelectedIndex(0);
        toYearBox.setSelectedIndex(0);
    }

    private void loadReports() {
        String type = (String) reportTypeBox.getSelectedItem();

        ReportController.DateFilter filter;

        if ("Custom Range".equals(type)) {

            String fd = (String) fromDayBox.getSelectedItem();
            String fm = (String) fromMonthBox.getSelectedItem();
            String fy = (String) fromYearBox.getSelectedItem();
            String td = (String) toDayBox.getSelectedItem();
            String tm = (String) toMonthBox.getSelectedItem();
            String ty = (String) toYearBox.getSelectedItem();

            if ("All".equals(fd) && "All".equals(fm) && "All".equals(fy) &&
                "All".equals(td) && "All".equals(tm) && "All".equals(ty)) {

                filter = new ReportController.DateFilter("All");

            } else if ("All".equals(fd) || "All".equals(fm) || "All".equals(fy) ||
                       "All".equals(td) || "All".equals(tm) || "All".equals(ty)) {

                reportArea.setText("Please select full date range (no 'All' allowed in Custom Range).");
                return;

            } else {

                filter = new ReportController.DateFilter(
                        Integer.parseInt(fd), monthToInt(fm), Integer.parseInt(fy),
                        Integer.parseInt(td), monthToInt(tm), Integer.parseInt(ty)
                );
            }

        } else {
            filter = new ReportController.DateFilter(type);
        }

        reportArea.setText(ReportController.buildReport(filter));
        reportArea.setCaretPosition(0);
    }

    private int monthToInt(String month) {
        String[] months = {"January","February","March","April","May","June","July","August","September","October","November","December"};
        for (int i = 0; i < months.length; i++) if (months[i].equals(month)) return i + 1;
        return 1;
    }
}