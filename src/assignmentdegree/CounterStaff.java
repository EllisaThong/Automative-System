package assignmentdegree;

import java.util.ArrayList;

public class CounterStaff extends User{
    public CounterStaff(String userID, String name, String password, String emailAddress, String contactNumber, String registerDate) {
        super(userID, name, password, emailAddress, contactNumber, registerDate);
    }
    
    @Override
    public String getRole() {
        return "Counter Staff";
    }

    @Override
    public void openMainMenu() {
        CounterStaffFunction counterStaffFunction = new CounterStaffFunction();
        counterStaffFunction.openCounterStaffFunction(this.getUserID(), this.getRole());
    }

    public String getStaffSummary() {
        return String.format("Staff ID: %s, Name: %s", getUserID(), getName());
    }

    public int getManagedAppointmentsCount(ArrayList<Appointment> allAppointments) {
        int count = 0;
        for (Appointment a : allAppointments) {
            if (a.getCounterStaffID().equals(getUserID())) {
                count++;
            }
        }
        return count;
    }
}
