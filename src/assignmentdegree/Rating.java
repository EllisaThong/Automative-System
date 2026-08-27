package assignmentdegree;

public class Rating implements TextRecord {
    private String ratingID;
    private String appointmentID;
    private int ratingValue;
    private String customerID;
    private String dateTime;

    public Rating(String ratingID, String appointmentID, int ratingValue, String customerID, String dateTime) {
        this.ratingID = ratingID;
        this.appointmentID = appointmentID;
        this.ratingValue = ratingValue;
        this.customerID = customerID;
        this.dateTime = dateTime;
    }

    public String getRatingID() {
        return ratingID;
    }

    public void setRatingID(String ratingID) {
        this.ratingID = ratingID;
    }

    public String getAppointmentID() {
        return appointmentID;
    }

    public void setAppointmentID(String appointmentID) {
        this.appointmentID = appointmentID;
    }

    public int getRatingValue() {
        return ratingValue;
    }

    public void setRatingValue(int ratingValue) {
        this.ratingValue = ratingValue;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toRecord() {
        return String.join(",",
                getRatingID(),
                getAppointmentID(),
                String.valueOf(getRatingValue()),
                getCustomerID(),
                getDateTime());
    }
}
