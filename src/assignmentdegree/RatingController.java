package assignmentdegree;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RatingController {

    public static void addRating(String appointmentID, int ratingValue, String customerID) {
        String ratingID = FileHandler.generateRatingID();
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        Rating newRating = new Rating(ratingID, appointmentID, ratingValue, customerID, dateTime);
        DataStore.allRatings.add(newRating);
        FileHandler.writeAllFiles();
    }

    public static void updateRating(Rating r, int newValue) {
        r.setRatingValue(newValue);
        r.setDateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        FileHandler.writeAllFiles();
    }

    public static void deleteRating(String ratingID) {
        DataStore.allRatings.removeIf(r -> r.getRatingID().equals(ratingID));
        resequenceRatingIDs();
        FileHandler.writeAllFiles();
    }

    public static Rating getRatingByAppointmentID(String appointmentID) {
        for (Rating r : DataStore.allRatings) {
            if (r.getAppointmentID().equals(appointmentID)) {
                return r;
            }
        }
        return null;
    }

    public static void resequenceRatingIDs() {
        int i = 1;
        for (Rating r : DataStore.allRatings) {
            r.setRatingID(String.format("RT%03d", i++));
        }
    }
}
