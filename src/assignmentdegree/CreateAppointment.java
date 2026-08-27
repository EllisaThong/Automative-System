package assignmentdegree;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.ArrayList;
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
import javax.swing.ListSelectionModel;

public class CreateAppointment implements ActionListener {
    private static final String OPEN_TIME = "10:00";
    private static final String CLOSE_TIME = "18:00"; // end time must be <= close time
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            frame.dispose();
            openAddPage();
        } else if (e.getSource() == editButton) {
            if (selectedAppointment == null) {
                JOptionPane.showMessageDialog(frame, "Select appointment first");
            } else if (selectedAppointment.getStatus().equalsIgnoreCase("Completed")) {
                JOptionPane.showMessageDialog(frame, "Completed appointment cannot be edited.");
            } else {
                frame.dispose();
                openEditPage(selectedAppointment);
            }
        } else if (e.getSource() == deleteButton) {
            deleteAppointment();
        } else if (e.getSource() == pickUpButton) {
            pickUpAppointment();
        } else if (e.getSource() == refreshButton) {
            loadAppointments();
        } else if (e.getSource() == backButton) {
            frame.dispose();
            CounterStaffFunction counterStaffFunction = new CounterStaffFunction();
            counterStaffFunction.openCounterStaffFunction(userID, userRole);
        } else if (e.getSource() == clearFilterButton) {
            resetFilters();
            loadAppointments();
        }
    }
    
    private JFrame frame;
    private DefaultListModel<String> listModel;
    private JList<String> appointmentList;
    private JTextArea detailArea;

    private JButton addButton, editButton, deleteButton, refreshButton, backButton, clearFilterButton, pickUpButton;

    private JComboBox<String> filterYearBox, filterMonthBox, filterCustomerBox, filterStatusBox;
    private JComboBox<String> filterDayBox;
    private JTextField searchField;

    private boolean updatingFilterDays = false;
    private Appointment selectedAppointment;
    private String userID;
    private String userRole;

    public void openPage(String userID, String role) {
        this.userID = userID;
        this.userRole = role;
        
        FileHandler.writeSystemLog("(" + role + ")" + userID + " opened the manage appointment" + role + " function.");


        frame = new JFrame("Manage Appointments");
        frame.setSize(1000, 600);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null);

        listModel = new DefaultListModel<>();
        appointmentList = new JList<>(listModel);
        appointmentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentList.addListSelectionListener(e -> showDetails());

        detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        addButton = new JButton("Add");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        pickUpButton = new JButton("Pick Up Appointment");
        refreshButton = new JButton("Refresh");
        backButton = new JButton("Back");
        clearFilterButton = new JButton("Clear Filter");

        pickUpButton.setVisible("Counter Staff".equalsIgnoreCase(userRole));
        pickUpButton.setEnabled(false);

        filterYearBox = new JComboBox<>();
        filterMonthBox = new JComboBox<>();
        filterDayBox = new JComboBox<>();
        filterCustomerBox = new JComboBox<>();
        filterStatusBox = new JComboBox<>();
        searchField = new JTextField(15);

        setupFilters();

        JPanel filterPanel = new JPanel(new GridLayout(2, 6, 8, 8));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Appointments"));
        filterPanel.add(new JLabel("Year"));
        filterPanel.add(new JLabel("Month"));
        filterPanel.add(new JLabel("Day"));
        filterPanel.add(new JLabel("Customer"));
        filterPanel.add(new JLabel("Status"));
        filterPanel.add(new JLabel("Search"));
        filterPanel.add(filterYearBox);
        filterPanel.add(filterMonthBox);
        filterPanel.add(filterDayBox);
        filterPanel.add(filterCustomerBox);
        filterPanel.add(filterStatusBox);
        filterPanel.add(searchField);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(filterPanel, BorderLayout.CENTER);
        JPanel topButtonPanel = new JPanel();
        topButtonPanel.add(refreshButton);
        topButtonPanel.add(clearFilterButton);
        topPanel.add(topButtonPanel, BorderLayout.EAST);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Appointment List"));
        leftPanel.add(new JScrollPane(appointmentList), BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Appointment Details"));
        rightPanel.add(new JScrollPane(detailArea), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        split.setDividerLocation(450);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(pickUpButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(backButton);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(split, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(this);
        editButton.addActionListener(this);
        deleteButton.addActionListener(this);
        pickUpButton.addActionListener(this);
        refreshButton.addActionListener(this);
        backButton.addActionListener(this);
        clearFilterButton.addActionListener(this);

        filterMonthBox.addActionListener(e -> {
            refreshFilterDayBox();
            loadAppointments();
        });
        filterYearBox.addActionListener(e -> {
            refreshFilterDayBox();
            loadAppointments();
        });
        filterDayBox.addActionListener(e -> {
            if (!updatingFilterDays) {
                loadAppointments();
            }
        });
        filterCustomerBox.addActionListener(e -> loadAppointments());
        filterStatusBox.addActionListener(e -> loadAppointments());
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadAppointments();
            }
        });

        loadAppointments();
        remindUnassignedAppointments();
        frame.setVisible(true);
    }

    private void setupFilters() {
        filterYearBox.addItem("All");
        filterMonthBox.addItem("All");
        filterDayBox.addItem("All");
        filterCustomerBox.addItem("All");
        filterStatusBox.addItem("All");

        for (int y = 2025; y <= 2035; y++) {
            filterYearBox.addItem(String.valueOf(y));
        }

        String[] months = {"January","February","March","April","May","June","July","August","September","October","November","December"};
        for (String m : months) {
            filterMonthBox.addItem(m);
        }

        for (Customer c : DataStore.allCustomers) {
            filterCustomerBox.addItem(c.getUserID() + " - " + c.getName());
        }
        filterStatusBox.addItem("Scheduled");
        filterStatusBox.addItem("Completed");
        filterStatusBox.addItem("No Technician");
        filterStatusBox.addItem("No Counter Staff");

        rebuildFilterDayBox(31);
    }

    private void refreshFilterDayBox() {
        String month = (String) filterMonthBox.getSelectedItem();
        String yearStr = (String) filterYearBox.getSelectedItem();

        int maxDays = 31;

        if (!"All".equals(month)) {
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
        }
        rebuildFilterDayBox(maxDays);
    }

    private void rebuildFilterDayBox(int maxDays) {
        updatingFilterDays = true;
        String previous = (String) filterDayBox.getSelectedItem();

        filterDayBox.removeAllItems();
        filterDayBox.addItem("All");
        for (int d = 1; d <= maxDays; d++) filterDayBox.addItem(String.valueOf(d));

        if (previous != null && previous.matches("\\d+")) {
            int prev = Integer.parseInt(previous);
            if (prev <= maxDays) filterDayBox.setSelectedItem(previous);
            else filterDayBox.setSelectedItem("All");
        } else filterDayBox.setSelectedItem("All");

        updatingFilterDays = false;
    }

    private void resetFilters() {
        filterYearBox.setSelectedItem("All");
        filterMonthBox.setSelectedItem("All");
        filterDayBox.setSelectedItem("All");
        filterCustomerBox.setSelectedIndex(0);
        filterStatusBox.setSelectedIndex(0);
        searchField.setText("");
    }

    private void loadAppointments() {
        listModel.clear();
        detailArea.setText("");
        selectedAppointment = null;

        String selectedYear     = (String) filterYearBox.getSelectedItem();
        String selectedMonth    = (String) filterMonthBox.getSelectedItem();
        String selectedDay      = (String) filterDayBox.getSelectedItem();
        String selectedCustomer = (String) filterCustomerBox.getSelectedItem();
        String selectedStatus   = (String) filterStatusBox.getSelectedItem();
        String keyword          = searchField.getText().trim().toLowerCase();

        for (Appointment a : DataStore.allAppointments) {
            String customerName = LookupService.getCustomerNameByID(a.getCustomerID());
            String serviceName  = LookupService.getServiceNameByID(a.getServiceID());

            String[] parts = a.getDate().split(" ");
            String day = parts[0];
            String month = parts[1];
            String year = parts[2];

            boolean match = true;

            if (!"All".equals(selectedYear) && !year.equals(selectedYear)) match = false;
            if (!"All".equals(selectedMonth) && !month.equals(selectedMonth)) match = false;
            if (!"All".equals(selectedDay) && !day.equals(selectedDay)) match = false;

            if (!"All".equals(selectedCustomer)) {
                String customerID = selectedCustomer.split(" - ")[0];
                if (!a.getCustomerID().equals(customerID)) match = false;
            }

            if ("No Technician".equals(selectedStatus)) {
                if (!hasNoTechnicianAssigned(a)) {
                    match = false;
                }
            } else if ("No Counter Staff".equals(selectedStatus)) {
                if (a.getCounterStaffID() != null && !a.getCounterStaffID().isEmpty()) {
                    match = false;
                }
            } else if (!"All".equals(selectedStatus) && !a.getStatus().equalsIgnoreCase(selectedStatus)) {
                match = false;
            }

            String searchableText = (a.getAppointmentID() + " " + customerName + " " + a.getCustomerID() + " " +
                    a.getCounterStaffID() + " " + serviceName + " " + a.getDate() + " " + a.getStatus()).toLowerCase();

            if (!keyword.isEmpty() && !searchableText.contains(keyword)) match = false;

            if (match) listModel.addElement(a.getAppointmentID() + " - " + customerName + " (" + a.getDate() + ")");
        }

        if (listModel.isEmpty()) detailArea.setText("No appointments found.");
    }

    private boolean hasNoTechnicianAssigned(Appointment a) {
        return a.getTechnicianIDs() == null || a.getTechnicianIDs().isEmpty();
    }

    private void remindUnassignedAppointments() {
        ArrayList<String> noTechAppointments = new ArrayList<>();
        ArrayList<String> noStaffAppointments = new ArrayList<>();

        for (Appointment a : DataStore.allAppointments) {

            // No technician assigned
            if (a.getTechnicianIDs() == null || a.getTechnicianIDs().isEmpty()) {
                noTechAppointments.add(a.getAppointmentID());
            }

            // No counter staff assigned
            if (a.getCounterStaffID() == null || a.getCounterStaffID().isEmpty()) {
                noStaffAppointments.add(a.getAppointmentID());
            }
        }

        // Nothing to remind
        if (noTechAppointments.isEmpty() && noStaffAppointments.isEmpty()) {
            return;
        }

        StringBuilder message = new StringBuilder();

        if (!noTechAppointments.isEmpty()) {
            message.append("Reminder: ")
                   .append(noTechAppointments.size())
                   .append(" appointment(s) do not have technician assigned yet.\n");

            message.append("This may happen due to technician deletion or unassigned appointments.\n");

            message.append("Appointment IDs: ")
                   .append(String.join(", ", noTechAppointments))
                   .append("\n\n");
        }

        if (!noStaffAppointments.isEmpty()) {
            message.append("Reminder: ")
                   .append(noStaffAppointments.size())
                   .append(" appointment(s) do not have counter staff assigned yet.\n");

            message.append("This may happen due to counter staff deletion or unassigned appointments.\n");

            message.append("Appointment IDs: ")
                   .append(String.join(", ", noStaffAppointments))
                   .append("\n\n");
        }

        message.append("Tip: use Status filter \"No Technician\" or \"No Counter Staff\".");

        JOptionPane.showMessageDialog(
                frame,
                message.toString(),
                "Appointment Assignment Reminder",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void showDetails() {
        String selected = appointmentList.getSelectedValue();
        if (selected != null) {
            String id = selected.split(" - ")[0];
            for (Appointment a : DataStore.allAppointments) {
                if (a.getAppointmentID().equals(id)) {
                    selectedAppointment = a;
                    String customerName = LookupService.getCustomerNameByID(a.getCustomerID());
                    String serviceName = LookupService.getServiceNameByID(a.getServiceID());
                    String techNames = LookupService.getTechnicianNames(a.getTechnicianIDs());
                    
                    String staffID = a.getCounterStaffID();
                    String staffDisplay = (staffID == null || staffID.isEmpty()) ? "N/A" : staffID;

                    detailArea.setText(a.getAppointmentSummary() +
                            "\n----------------\n" +
                            "Customer ID: " + a.getCustomerID() +
                            "\nCustomer Name: " + customerName +
                            "\nCounter Staff ID: " + staffDisplay +
                            "\nService: " + serviceName +
                            "\nTechnicians: " + techNames +
                            "\nDate: " + a.getDate() +
                            "\nTime: " + a.getFullTimeRange() +
                            "\n\n--- Customer Rating ---\n" + LookupService.getRatingDisplay(a.getAppointmentID()));

                    // Enable pick up button if role is counter staff and no staff assigned yet
                    if ("Counter Staff".equalsIgnoreCase(userRole)) {
                        pickUpButton.setEnabled(staffID == null || staffID.isEmpty());
                    }
                    break;
                }
            }
        }
    }

    private void openAddPage() {
        JFrame addFrame = createFormFrame("Add Appointment", null);
        addFrame.setVisible(true);
    }

    private void openEditPage(Appointment a) {
        JFrame editFrame = createFormFrame("Edit Appointment", a);
        editFrame.setVisible(true);
    }

    private JFrame createFormFrame(String title, Appointment a) {
        JFrame f = new JFrame(title);
        f.setSize(450, 450);
        f.setLayout(null);
        f.setLocationRelativeTo(null);

        JLabel customerLabel = new JLabel("Customer:");
        JLabel serviceLabel  = new JLabel("Service:");
        JLabel techLabel     = new JLabel("Technicians:");
        JLabel dateLabel     = new JLabel("Date:");
        JLabel timeLabel     = new JLabel("Start Time:");

        JComboBox<String> customerBox = new JComboBox<>();
        for (Customer c : DataStore.allCustomers) {
            customerBox.addItem(c.getUserID() + " - " + c.getName());
        }

        JComboBox<String> serviceBox = new JComboBox<>();
        for (Service s : DataStore.allServices) {
            serviceBox.addItem(s.getServiceInfo());
        }

        DefaultListModel<String> techModel = new DefaultListModel<>();
        for (Technician t : DataStore.allTechnicians) {
            techModel.addElement(t.getUserID() + " - " + t.getName());
        }

        JList<String> techList = new JList<>(techModel);
        techList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane techScroll = new JScrollPane(techList);

        JComboBox<Integer> dayBox   = new JComboBox<>();
        JComboBox<String>  monthBox = new JComboBox<>();
        JComboBox<Integer> yearBox  = new JComboBox<>();

        String[] months = {"January","February","March","April","May","June",
                "July","August","September","October","November","December"};
        for (String m : months) monthBox.addItem(m);

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        int tomorrowDay = tomorrow.getDayOfMonth();
        String tomorrowMonth = monthIntToString(tomorrow.getMonthValue() - 1);
        int tomorrowYear = tomorrow.getYear();

        for (int y = tomorrowYear; y <= tomorrowYear + 5; y++) yearBox.addItem(y);

        monthBox.setSelectedItem(tomorrowMonth);
        yearBox.setSelectedItem(tomorrowYear);

        updateDays(dayBox, tomorrowMonth, tomorrowYear);
        dayBox.setSelectedItem(tomorrowDay);

        monthBox.addActionListener(e -> {
            if (monthBox.getSelectedItem() != null && yearBox.getSelectedItem() != null) {
                updateDays(dayBox, monthBox.getSelectedItem().toString(), (Integer) yearBox.getSelectedItem());
            }
        });

        yearBox.addActionListener(e -> {
            if (monthBox.getSelectedItem() != null && yearBox.getSelectedItem() != null) {
                updateDays(dayBox, monthBox.getSelectedItem().toString(), (Integer) yearBox.getSelectedItem());
            }
        });

        JComboBox<String> timeBox = new JComboBox<>();
        populateTimeSlots(timeBox);

        JButton saveButton = new JButton("Save");
        JButton backButton = new JButton("Back");

        customerLabel.setBounds(30,30,100,25);
        customerBox.setBounds(150,30,250,25);
        serviceLabel.setBounds(30,70,100,25);
        serviceBox.setBounds(150,70,250,25);
        techLabel.setBounds(30,110,100,25);
        techScroll.setBounds(150,110,250,80);
        dateLabel.setBounds(30,210,100,25);
        dayBox.setBounds(150,210,70,25);
        monthBox.setBounds(230,210,100,25);
        yearBox.setBounds(340,210,80,25);
        timeLabel.setBounds(30,250,100,25);
        timeBox.setBounds(150,250,250,25);
        saveButton.setBounds(80,310,100,30);
        backButton.setBounds(220,310,100,30);

        f.add(customerLabel); f.add(customerBox);
        f.add(serviceLabel); f.add(serviceBox);
        f.add(techLabel); f.add(techScroll);
        f.add(dateLabel); f.add(dayBox); f.add(monthBox); f.add(yearBox);
        f.add(timeLabel); f.add(timeBox);
        f.add(saveButton); f.add(backButton);

        if (a != null) {
            customerBox.setSelectedItem(a.getCustomerID() + " - " + LookupService.getCustomerNameByID(a.getCustomerID()));

            Service service = LookupService.getServiceByID(a.getServiceID());
            if (service != null) {
                serviceBox.setSelectedItem(service.getServiceInfo());
            }

            String[] parts = a.getDate().split(" ");
            int day = Integer.parseInt(parts[0]);
            String month = parts[1];
            int year = Integer.parseInt(parts[2]);

            monthBox.setSelectedItem(month);
            yearBox.setSelectedItem(year);
            updateDays(dayBox, month, year);
            dayBox.setSelectedItem(day);

            ArrayList<String> techIDs = a.getTechnicianIDs();
            ArrayList<Integer> selectedIndices = new ArrayList<>();
            for (String techID : techIDs) {
                for (int i = 0; i < techModel.size(); i++) {
                    if (techModel.get(i).startsWith(techID + " - ")) {
                        selectedIndices.add(i);
                        break;
                    }
                }
            }
            int[] indices = new int[selectedIndices.size()];
            for (int i = 0; i < selectedIndices.size(); i++) indices[i] = selectedIndices.get(i);
            techList.setSelectedIndices(indices);

            timeBox.setSelectedItem(a.getStartTime());
        }

        ActionListener refreshTimes = e -> {
            if (serviceBox.getSelectedItem() == null) return;

            String serviceID = serviceBox.getSelectedItem().toString().split(" - ")[0];
            Service selectedService = LookupService.getServiceByID(serviceID);
            if (selectedService == null) return;

            ArrayList<String> selectedTechs = new ArrayList<>();
            for (String techItem : techList.getSelectedValuesList()) {
                selectedTechs.add(techItem.split(" - ")[0]);
            }

            if (dayBox.getSelectedItem() == null || monthBox.getSelectedItem() == null || yearBox.getSelectedItem() == null) return;

            int    selectedDay   = (Integer) dayBox.getSelectedItem();
            String selectedMonth = (String)  monthBox.getSelectedItem();
            int    selectedYear  = (Integer) yearBox.getSelectedItem();
            String date = String.format("%02d %s %d", selectedDay, selectedMonth, selectedYear);

            updateAvailableTimes(timeBox, date, selectedService.getDuration(), selectedTechs, a);
        };

        serviceBox.addActionListener(refreshTimes);
        techList.addListSelectionListener(e -> refreshTimes.actionPerformed(null));
        dayBox.addActionListener(refreshTimes);
        monthBox.addActionListener(refreshTimes);
        yearBox.addActionListener(refreshTimes);

        saveButton.addActionListener(e -> {
            if (a != null && a.getStatus().equalsIgnoreCase("Completed")) {
                JOptionPane.showMessageDialog(f, "Completed appointment cannot be edited.");
                return;
            }

            if (customerBox.getSelectedItem() == null ||
                    serviceBox.getSelectedItem() == null ||
                    techList.getSelectedValuesList().isEmpty() ||
                    dayBox.getSelectedItem() == null ||
                    monthBox.getSelectedItem() == null ||
                    yearBox.getSelectedItem() == null ||
                    timeBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(f, "All fields are required!");
                return;
            }

            int day = (Integer) dayBox.getSelectedItem();
            String month = (String)  monthBox.getSelectedItem();
            int year  = (Integer) yearBox.getSelectedItem();

            LocalDate selectedDate = LocalDate.of(year, monthStringToInt(month) + 1, day);
            LocalDate today = LocalDate.now();

            if (!selectedDate.isAfter(today)) {
                JOptionPane.showMessageDialog(f, "Cannot select a current/past date!");
                return;
            }

            String customerID = ((String) customerBox.getSelectedItem()).split(" - ")[0];
            String serviceID = ((String) serviceBox.getSelectedItem()).split(" - ")[0];

            Service selectedService = LookupService.getServiceByID(serviceID);
            if (selectedService == null) {
                JOptionPane.showMessageDialog(f, "Invalid service selected!");
                return;
            }

            String date = String.format("%02d %s %d", day, month, year);
            String startTime = (String) timeBox.getSelectedItem();
            if (startTime == null || "No available time slots".equals(startTime)) {
                JOptionPane.showMessageDialog(f, "No available time slots for the selected date/technician/service.");
                return;
            }
            String endTime = calculateEndTime(selectedService.getDuration(), startTime);

            if (!isWithinBusinessHours(startTime, endTime)) {
                JOptionPane.showMessageDialog(f, "Selected time is outside business hours (" + OPEN_TIME + " - " + CLOSE_TIME + ").");
                return;
            }

            ArrayList<String> techs = new ArrayList<>();
            for (String techItem : techList.getSelectedValuesList()) {
                techs.add(techItem.split(" - ")[0]);
            }

            if (isTimeConflict(date, startTime, endTime, techs, a)) {
                JOptionPane.showMessageDialog(f, "Selected time conflicts with existing appointment");
                return;
            }

            if (a == null) {
                String id = FileHandler.generateAppointmentID();
                Appointment newA = new Appointment(
                        id, customerID, userID, techs, serviceID, date, startTime, endTime, "Scheduled"
                );
                DataStore.allAppointments.add(newA);
                FileHandler.writeSystemLog("(" + userRole + ") " + userID + " created appointment: " + id + " [Customer: " + customerID + ", Service: " + serviceID + ", Date: " + date + ", Time: " + startTime + "-" + endTime + "]");
                JOptionPane.showMessageDialog(f, "Appointment Created");
            } else {
                a.setCustomerID(customerID);
                a.setTechnicianIDs(techs);
                a.setServiceID(serviceID);
                a.setDate(date);
                a.setStartTime(startTime);
                a.setEndTime(endTime);
                a.setStatus("Scheduled");
                FileHandler.writeSystemLog("(" + userRole + ") " + userID + " updated appointment: " + a.getAppointmentID() + " [Customer: " + customerID + ", Service: " + serviceID + ", Date: " + date + ", Time: " + startTime + "-" + endTime + "]");
                JOptionPane.showMessageDialog(f, "Appointment Updated");
            }

            FileHandler.writeAllFiles();
            f.dispose();
            openPage(userID, userRole);
        });

        backButton.addActionListener(e -> {
            f.dispose();
            openPage(userID, userRole);
        });

        refreshTimes.actionPerformed(null);

        return f;
    }

    private void populateTimeSlots(JComboBox<String> timeBox) {
        timeBox.removeAllItems();
        for (int i = 10; i <= 17; i++) {
            timeBox.addItem(String.format("%02d:00", i));
        }
    }

    private void updateAvailableTimes(JComboBox<String> timeBox, String date, int durationMinutes, ArrayList<String> selectedTechs, Appointment editing) {
        timeBox.removeAllItems();
        int added = 0;
        for (int i = 10; i <= 17; i++) {
            String start = String.format("%02d:00", i);
            String end   = calculateEndTime(durationMinutes, start);
            if (isWithinBusinessHours(start, end) && !isTimeConflict(date, start, end, selectedTechs, editing)) {
                timeBox.addItem(start);
                added++;
            }
        }
        if (added == 0) {
            timeBox.addItem("No available time slots");
            timeBox.setEnabled(false);
        } else {
            timeBox.setEnabled(true);
        }
    }

    private boolean isTimeConflict(String date, String startTime, String endTime, ArrayList<String> techs, Appointment ignore) {
        for (Appointment a : DataStore.allAppointments) {
            if (ignore != null && a == ignore) continue;
            if (!a.getDate().equals(date)) continue;
            for (String tech : techs) {
                if (a.getTechnicianIDs() != null && a.getTechnicianIDs().contains(tech)) {
                    if (!(endTime.compareTo(a.getStartTime()) <= 0 || startTime.compareTo(a.getEndTime()) >= 0)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isWithinBusinessHours(String startTime, String endTime) {
        // start must be >= open, end must be <= close
        return startTime.compareTo(OPEN_TIME) >= 0 && endTime.compareTo(CLOSE_TIME) <= 0;
    }

    private String calculateEndTime(int durationMinutes, String startTime) {
        int hour   = Integer.parseInt(startTime.split(":")[0]);
        int minute = Integer.parseInt(startTime.split(":")[1]);
        int totalMinutes = hour * 60 + minute + durationMinutes;
        return String.format("%02d:%02d", totalMinutes / 60, totalMinutes % 60);
    }

    private void deleteAppointment() {
        if (selectedAppointment == null) {
            JOptionPane.showMessageDialog(frame, "Select appointment first");
        } else {
            int confirm = JOptionPane.showConfirmDialog(frame, "Delete this appointment and all related payments, receipts, and comments?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                AppointmentController.deleteAppointment(selectedAppointment.getAppointmentID());
                FileHandler.writeSystemLog("(" + userRole + ") " + userID + " deleted appointment: " + selectedAppointment.getAppointmentID() + " [Customer: " + selectedAppointment.getCustomerID() + ", Service: " + selectedAppointment.getServiceID() + ", Date: " + selectedAppointment.getDate() + ", Time: " + selectedAppointment.getStartTime() + "-" + selectedAppointment.getEndTime() + "]");
                FileHandler.writeAllFiles();
                loadAppointments();
                detailArea.setText("");
                JOptionPane.showMessageDialog(frame, "Deleted");
            }
        }
    }

    private void updateDays(JComboBox<Integer> dayBox, String month, int year) {
        if (month == null || year <= 0) return;

        Object selectedDay = dayBox.getSelectedItem();
        dayBox.removeAllItems();

        int days;
        switch (month) {
            case "February":
                days = LookupService.isLeapYear(year) ? 29 : 28;
                break;
            case "April":
            case "June":
            case "September":
            case "November":
                days = 30;
                break;
            default:
                days = 31;
                break;
        }

        for (int d = 1; d <= days; d++) {
            dayBox.addItem(d);
        }

        if (selectedDay instanceof Integer old) {
            if (old <= days) {
                dayBox.setSelectedItem(old);
            }
        }
    }

    private void pickUpAppointment() {
        if (selectedAppointment == null) {
            JOptionPane.showMessageDialog(frame, "Select appointment first");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to pick up this appointment?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            selectedAppointment.setCounterStaffID(userID);
            FileHandler.writeAllFiles();
            FileHandler.writeSystemLog("(" + userRole + ") " + userID + " picked up appointment: " + selectedAppointment.getAppointmentID());
            loadAppointments();
            detailArea.setText("");
            JOptionPane.showMessageDialog(frame, "Appointment picked up successfully!");
        }
    }

    private int monthStringToInt(String month) {
        switch (month) {
            case "January":
                return 0;
            case "February":
                return 1;
            case "March":
                return 2;
            case "April":
                return 3;
            case "May":
                return 4;
            case "June":
                return 5;
            case "July":
                return 6;
            case "August":
                return 7;
            case "September":
                return 8;
            case "October":
                return 9;
            case "November":
                return 10;
            case "December":
                return 11;
            default:
                return 0;
        }
    }

    private String monthIntToString(int monthIndex) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        if (monthIndex < 0 || monthIndex >= months.length) {
            return "January";
        }
        return months[monthIndex];
    }
}