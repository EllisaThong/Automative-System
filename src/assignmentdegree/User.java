package assignmentdegree;

public abstract class User implements TextRecord {
    private String userID;
    private String name;
    private String password;
    private String emailAddress;
    private String contactNumber;
    private final String registerDate;
    
    // Login lockout tracking (in-memory)
    private int failedLoginAttempts = 0;
    private long lockoutEndTime = 0;

    public User(String userID, String name, String password, String emailAddress, String contactNumber, String registerDate) {
        this.userID = userID;
        this.name = name;
        this.password = password;
        this.emailAddress = emailAddress;
        this.contactNumber = contactNumber;
        this.registerDate = registerDate;
    }
    
    public String getUserID() {
        return userID;
    }
    
    public void setUserID(String newUserID) {
        this.userID = newUserID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getRegisterDate() {
        return registerDate;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 3) {
            this.lockoutEndTime = System.currentTimeMillis() + 60000; // 1 minute
        }
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.lockoutEndTime = 0;
    }

    public boolean isLockedOut() {
        if (lockoutEndTime == 0) return false;
        if (System.currentTimeMillis() > lockoutEndTime) {
            resetFailedAttempts();
            return false;
        }
        return true;
    }

    public long getLockoutRemainingSeconds() {
        if (!isLockedOut()) return 0;
        return (lockoutEndTime - System.currentTimeMillis()) / 1000;
    }

    public boolean authenticate(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    public String getFullDetails() {
        return String.format("ID: %s\nName: %s\nEmail: %s\nContact: %s\nRole: %s",
                userID, name, emailAddress, contactNumber, getRole());
    }

    public abstract String getRole();

    public abstract void openMainMenu();

    @Override
    public String toRecord() {
        return getUserID() + "," + getName() + "," + getPassword()
                + "," + getEmailAddress() + "," + getContactNumber() + "," + getRegisterDate();
    }
}