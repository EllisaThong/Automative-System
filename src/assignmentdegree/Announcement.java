package assignmentdegree;

public class Announcement implements TextRecord {
    private String announcementID;
    private String senderID;
    private String targetType;
    private String targetRole;
    private String targetUserID;
    private String message;
    private String dateTime;

    public Announcement(String announcementID, String senderID, String targetType,
                        String targetRole, String targetUserID, String message, String dateTime) {
        this.announcementID = announcementID;
        this.senderID = senderID;
        this.targetType = targetType;
        this.targetRole = targetRole;
        this.targetUserID = targetUserID;
        this.message = message;
        this.dateTime = dateTime;
    }

    public String getAnnouncementID() {
        return announcementID;
    }

    public String getSenderID() {
        return senderID;
    }

    public String getTargetType() {
        return targetType;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public String getTargetUserID() {
        return targetUserID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDateTime() {
        return dateTime;
    }

    public boolean isVisibleTo(String userID, String userRole) {
        if ("ALL_USERS".equalsIgnoreCase(targetType)) {
            return !"Manager".equalsIgnoreCase(userRole);
        }
        if ("ROLE".equalsIgnoreCase(targetType)) {
            return targetRole != null && targetRole.equalsIgnoreCase(userRole);
        }
        if ("USER".equalsIgnoreCase(targetType)) {
            return targetUserID != null && targetUserID.equalsIgnoreCase(userID);
        }
        return false;
    }

    @Override
    public String toRecord() {
        String safeMessage = message == null ? "" : message.replace("\n", "\\n").replace(",", ";");
        return announcementID + "," + senderID + "," + targetType + ","
                + (targetRole == null ? "" : targetRole) + ","
                + (targetUserID == null ? "" : targetUserID) + ","
                + safeMessage + "," + dateTime;
    }
}
