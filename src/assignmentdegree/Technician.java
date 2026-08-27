package assignmentdegree;

import java.util.ArrayList;

public class Technician extends User{
    public Technician(String userID, String name, String password, String emailAddress, String contactNumber, String registerDate) {
        super(userID, name, password, emailAddress, contactNumber, registerDate);
    }
    
    @Override
    public String getRole() {
        return "Technician";
    }

    @Override
    public void openMainMenu() {
        TechnicianFunction technicianFunction = new TechnicianFunction();
        technicianFunction.openTechnicianFunction(this.getUserID(), this.getRole());
    }

    public String getTechnicianIDFormatted() {
        return "Tech-" + getUserID();
    }

    public int getAssignedAppointmentsCount(ArrayList<Appointment> allAppointments) {
        int count = 0;
        for (Appointment a : allAppointments) {
            if (a.isAssignedTo(getUserID())) {
                count++;
            }
        }
        return count;
    }

    public int getCompletedAppointmentsCount(ArrayList<Appointment> allAppointments) {
        int count = 0;
        for (Appointment a : allAppointments) {
            if (a.isAssignedTo(getUserID()) && a.isCompleted()) {
                count++;
            }
        }
        return count;
    }
}
