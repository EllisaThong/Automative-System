package assignmentdegree;

import java.util.ArrayList;

public class Appointment implements TextRecord {
    private String appointmentID;
    private String customerID;
    private String counterStaffID;
    private ArrayList<String> technicianIDs;
    private String serviceID;
    private String date;
    private String startTime;
    private String endTime;
    private String status;

    public Appointment(String appointmentID, String customerID, String counterStaffID, ArrayList<String> technicianIDs, String serviceID, String date, String startTime, String endTime, String status) {
        this.appointmentID = appointmentID;
        this.customerID = customerID;
        this.counterStaffID = counterStaffID;
        this.technicianIDs = technicianIDs;
        this.serviceID = serviceID;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }
    
    public String getAppointmentID() {
        return appointmentID;
    }

    public void setAppointmentID(String newAppointmentID) {
        this.appointmentID = newAppointmentID;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String newCustomerID) {
        this.customerID = newCustomerID;
    }
    
    public String getCounterStaffID() {
        return counterStaffID;
    }

    public void setCounterStaffID(String newCounterStaffID) {
        this.counterStaffID = newCounterStaffID;
    }

    public ArrayList<String> getTechnicianIDs() {
        return technicianIDs;
    }

    public void setTechnicianIDs(ArrayList<String> technicianIDs) {
        this.technicianIDs = technicianIDs;
    }

    public String getServiceID() {
        return serviceID;
    }

    public void setServiceID(String newServiceID) {
        this.serviceID = newServiceID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String newDate) {
        this.date = newDate;
    }
    
    public String getStartTime() {
        return startTime;
    }
    
    public void setStartTime(String newStartTime) {
        this.startTime = newStartTime;
    }
    
    public String getEndTime() {
        return endTime;
    }
    
    public void setEndTime(String newEndTime) {
        this.endTime = newEndTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String newStatus) {
        this.status = newStatus;
    }

    public boolean isCompleted() {
        return "Completed".equalsIgnoreCase(status);
    }

    public boolean isPending() {
        return "Pending".equalsIgnoreCase(status);
    }

    public boolean isAssignedTo(String technicianID) {
        return technicianIDs != null && technicianIDs.contains(technicianID);
    }

    public String getFullTimeRange() {
        return startTime + " - " + endTime;
    }

    public String getAppointmentSummary() {
        return String.format("Appointment %s on %s at %s (%s)", appointmentID, date, startTime, status);
    }

    @Override
    public String toRecord() {
        String techField = (technicianIDs == null || technicianIDs.isEmpty())
                ? "[]" : "[" + String.join(", ", technicianIDs) + "]";
        return String.join(",",
                getAppointmentID(), getCustomerID(), getCounterStaffID(),
                techField, getServiceID(), getDate(),
                getStartTime(), getEndTime(), getStatus());
    }
}