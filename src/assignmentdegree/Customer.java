package assignmentdegree;

import java.util.ArrayList;

public class Customer extends User{
    public Customer(String userID, String name, String password, String emailAddress, String contactNumber, String registerDate) {
        super(userID, name, password, emailAddress, contactNumber, registerDate);
    }
    
    @Override
    public String getRole() {
        return "Customer";
    }

    @Override
    public void openMainMenu() {
        CustomerFunction customerFunction = new CustomerFunction();
        customerFunction.openCustomerFunction(this.getUserID(), this.getRole());
    }

    public String getWelcomeMessage() {
        return "Welcome back, " + getName() + "! We are happy to serve you.";
    }

    public int getUnpaidCount(ArrayList<Payment> allPayments, ArrayList<Appointment> allAppointments) {
        int count = 0;
        for (Payment p : allPayments) {
            if (p.isFullyPaid()) continue;
            for (Appointment a : allAppointments) {
                if (a.getAppointmentID().equals(p.getAppointmentID()) && a.getCustomerID().equals(getUserID())) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    public double getTotalSpent(ArrayList<Payment> allPayments, ArrayList<Appointment> allAppointments) {
        double total = 0;
        for (Payment p : allPayments) {
            for (Appointment a : allAppointments) {
                if (a.getAppointmentID().equals(p.getAppointmentID()) && a.getCustomerID().equals(getUserID())) {
                    total += p.getTotalPaid();
                    break;
                }
            }
        }
        return total;
    }
}
