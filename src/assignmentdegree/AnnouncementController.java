package assignmentdegree;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

public class AnnouncementController {
    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Announcement sendToRole(String managerID, String role, String message) {
        if ("Manager".equalsIgnoreCase(role)) {
            throw new IllegalArgumentException("Announcements cannot be sent to managers.");
        }
        String id = FileHandler.generateAnnouncementID();
        String ts = LocalDateTime.now().format(TS_FORMAT);
        Announcement a = new Announcement(id, managerID, "ROLE", role, "", message, ts);
        DataStore.allAnnouncements.add(a);
        FileHandler.writeAllFiles();
        return a;
    }

    public static Announcement sendToUser(String managerID, String targetUserID, String message) {
        String targetRole = LookupService.getUserRoleByID(targetUserID);
        if ("Manager".equalsIgnoreCase(targetRole)) {
            throw new IllegalArgumentException("Announcements cannot be sent to managers.");
        }
        String id = FileHandler.generateAnnouncementID();
        String ts = LocalDateTime.now().format(TS_FORMAT);
        Announcement a = new Announcement(id, managerID, "USER", "", targetUserID, message, ts);
        DataStore.allAnnouncements.add(a);
        FileHandler.writeAllFiles();
        return a;
    }

    public static Announcement sendToAllUsers(String managerID, String message) {
        String id = FileHandler.generateAnnouncementID();
        String ts = LocalDateTime.now().format(TS_FORMAT);
        Announcement a = new Announcement(id, managerID, "ALL_USERS", "", "", message, ts);
        DataStore.allAnnouncements.add(a);
        FileHandler.writeAllFiles();
        return a;
    }

    public static ArrayList<Announcement> getAnnouncementsForUser(String userID, String role) {
        ArrayList<Announcement> result = new ArrayList<>();
        User user = LookupService.getUserByID(userID);
        if (user == null) {
            return result;
        }
        for (Announcement a : DataStore.allAnnouncements) {
            if (a.isVisibleTo(userID, role) && isUserEligibleByRegisterTime(user, a)) {
                result.add(a);
            }
        }
        Collections.reverse(result);
        return result;
    }

    public static ArrayList<Announcement> getAnnouncementsSentByManager(String managerID) {
        ArrayList<Announcement> result = new ArrayList<>();
        for (Announcement a : DataStore.allAnnouncements) {
            if (managerID.equals(a.getSenderID())) {
                result.add(a);
            }
        }
        Collections.reverse(result);
        return result;
    }

    public static Announcement getByID(String announcementID) {
        for (Announcement a : DataStore.allAnnouncements) {
            if (a.getAnnouncementID().equals(announcementID)) {
                return a;
            }
        }
        return null;
    }

    public static boolean editMessage(String announcementID, String newMessage) {
        Announcement a = getByID(announcementID);
        if (a == null) return false;
        a.setMessage(newMessage);
        FileHandler.writeAllFiles();
        return true;
    }

    public static boolean deleteAnnouncement(String announcementID) {
        boolean removed = DataStore.allAnnouncements.removeIf(a -> a.getAnnouncementID().equals(announcementID));
        if (removed) {
            FileHandler.writeAllFiles();
        }
        return removed;
    }

    private static boolean isUserEligibleByRegisterTime(User user, Announcement announcement) {
        try {
            LocalDateTime registerTime = LocalDateTime.parse(user.getRegisterDate(), TS_FORMAT);
            LocalDateTime announcementTime = LocalDateTime.parse(announcement.getDateTime(), TS_FORMAT);
            return !registerTime.isAfter(announcementTime);
        } catch (Exception e) {
            // Preserve old data visibility if timestamp format is malformed.
            return true;
        }
    }
}
