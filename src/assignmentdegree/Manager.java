package assignmentdegree;

import java.util.ArrayList;

public class Manager extends User{
    public Manager(String userID, String name, String password, String emailAddress, String contactNumber, String registerDate) {
        super(userID, name, password, emailAddress, contactNumber, registerDate);
    }
    
    @Override
    public String getRole() {
        return "Manager";
    }

    @Override
    public void openMainMenu() {
        ManagerFunction managerFunction = new ManagerFunction();
        managerFunction.openManagerFunction(this.getUserID(), this.getRole());
    }

    public String getAdminDashboardTitle() {
        return "Manager Admin Dashboard - " + getName();
    }

    public String getManagerSummary() {
        return "Manager: " + getName() + " (ID: " + getUserID() + ")";
    }

    public int countTotalUsers(ArrayList<Customer> customers, ArrayList<CounterStaff> staff, ArrayList<Technician> techs, ArrayList<Manager> managers) {
        return customers.size() + staff.size() + techs.size() + managers.size();
    }
}
