package assignmentdegree;

public class Payment implements TextRecord {

    private String paymentID;
    private String appointmentID;
    private double totalFee;
    private double totalPaid;
    private double balance;
    private String status;

    public Payment(String paymentID, String appointmentID, double totalFee, double totalPaid, double balance, String status) {
        this.paymentID = paymentID;
        this.appointmentID = appointmentID;
        this.totalFee = totalFee;
        this.totalPaid = totalPaid;
        this.balance = balance;
        this.status = status;
    }

    public String getPaymentID() {
        return paymentID;
    }
    
    public void setPaymentID(String paymentID) {
        this.paymentID = paymentID;
    }
    

    public String getAppointmentID() {
        return appointmentID;
    }
    
    public void setAppointmentID(String appointmentID) {
        this.appointmentID = appointmentID;
    }

    public double getTotalFee() {
        return totalFee;
    }
    
    public void setTotalFee(double totalFee) {
        this.totalFee = totalFee;
    }

    public double getTotalPaid() {
        return totalPaid;
    }
    
    public void setTotalPaid(double totalPaid) {
        this.totalPaid = totalPaid;
    }

    public double getBalance() {
        return balance;
    }
    
    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isFullyPaid() {
        return "Successful".equalsIgnoreCase(status) || balance <= 0;
    }

    public boolean isPartial() {
        return "Partial".equalsIgnoreCase(status);
    }

    public void updateBalance(double amountPaid) {
        this.totalPaid += amountPaid;
        this.balance = this.totalFee - this.totalPaid;
        if (this.balance <= 0) {
            this.status = "Successful";
        } else {
            this.status = "Partial";
        }
    }

    public String getPaymentSummary() {
        return String.format("Fee: RM%.2f, Paid: RM%.2f, Balance: RM%.2f (%s)", 
                totalFee, totalPaid, balance, status);
    }

    @Override
    public String toRecord() {
        return getPaymentID() + "," + getAppointmentID() + ","
                + getTotalFee() + "," + getTotalPaid() + ","
                + getBalance() + "," + getStatus();
    }
}