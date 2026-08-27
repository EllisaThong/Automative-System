package assignmentdegree;

public class Comment implements TextRecord {

    private String commentID;
    private String appointmentID;
    private String commentText;
    private String userID;
    private String dateTime;

    public Comment(String commentID, String appointmentID, String commentText, String userID, String dateTime) {
        this.commentID = commentID;
        this.appointmentID = appointmentID;
        this.commentText = commentText;
        this.userID = userID;
        this.dateTime = dateTime;
    }

    public String getCommentID() {
        return commentID;
    }

    public void setCommentID(String commentID) {
        this.commentID = commentID;
    }

    public String getAppointmentID() {
        return appointmentID;
    }

    public void setAppointmentID(String appointmentID) {
        this.appointmentID = appointmentID;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getShortComment(int maxLength) {
        if (commentText == null || commentText.length() <= maxLength) {
            return commentText;
        }
        return commentText.substring(0, maxLength - 3) + "...";
    }

    public String getFormattedComment() {
        return String.format("[%s] User %s: %s", dateTime, userID, commentText);
    }

    @Override
    public String toRecord() {
        String safe = getCommentText() == null ? "" : getCommentText().replace("\n", "\\n").replace(",", ";");
        return getCommentID() + "," + getAppointmentID() + "," + safe + "," + getUserID() + "," + getDateTime();
    }
}