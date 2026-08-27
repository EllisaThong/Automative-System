package assignmentdegree;

public class Receipt implements TextRecord {

    private String receiptID;
    private String paymentID;
    private double totalFee;
    private double amountPaid;
    private double totalPaid;
    private double balance;
    private double change;
    private String date;
    private String method;
    private String status;

    public Receipt(String receiptID, String paymentID,
                   double totalFee, double amountPaid, double totalPaid,
                   double balance, double change,
                   String date, String method, String status) {

        this.receiptID = receiptID;
        this.paymentID = paymentID;
        this.totalFee = totalFee;
        this.amountPaid = amountPaid;
        this.totalPaid = totalPaid;
        this.balance = balance;
        this.change = change;
        this.date = date;
        this.method = method;
        this.status = status;
    }

    public String getReceiptID() {
        return receiptID;
    }

    public void setReceiptID(String receiptID) {
        this.receiptID = receiptID;
    }

    public String getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(String paymentID) {
        this.paymentID = paymentID;
    }

    public String getAppointmentID() {
        Payment p = LookupService.getPaymentByID(paymentID);
        return p != null ? p.getAppointmentID() : "";
    }

    public double getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(double totalFee) {
        this.totalFee = totalFee;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
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

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSuccessful() {
        return "Successful".equalsIgnoreCase(status);
    }

    public String getReceiptHeader() {
        return "---------------------------------\n" +
               "         OFFICIAL RECEIPT        \n" +
               "---------------------------------\n" +
               "Receipt ID: " + receiptID + "\n" +
               "Date:       " + date + "\n" +
               "Method:     " + method + "\n";
    }

    public String getReceiptFooter() {
        return "---------------------------------\n" +
               "   Thank you for your business!  \n" +
               "---------------------------------";
    }

    @Override
    public String toRecord() {
        return getReceiptID() + "," + getPaymentID() + ","
                + getTotalFee() + "," + getAmountPaid() + ","
                + getTotalPaid() + "," + getBalance() + "," + getChange() + ","
                + getDate() + "," + getMethod() + "," + getStatus();
    }
}