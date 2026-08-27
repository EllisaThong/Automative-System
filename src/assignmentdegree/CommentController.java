package assignmentdegree;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CommentController {
    public static Comment addComment(String appointmentID, String userID, String text) {
        String id = FileHandler.generateCommentID();
        String dateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
        Comment c = new Comment(id, appointmentID, text, userID, dateTime);
        DataStore.allComments.add(c);
        FileHandler.writeAllFiles();
        return c;
    }

    public static void deleteComment(String commentID) {
        DataStore.allComments.removeIf(c -> c.getCommentID().equals(commentID));
        resequenceCommentIDs();
        FileHandler.writeAllFiles();
    }

    private static void resequenceCommentIDs() {
        int i = 1;
        for (Comment c : DataStore.allComments) {
            c.setCommentID(String.format("CMT%03d", i++));
        }
    }
}