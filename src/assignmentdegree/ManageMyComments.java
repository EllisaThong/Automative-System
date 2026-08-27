package assignmentdegree;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

public class ManageMyComments implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == deleteBtn) {
            deleteComment();
        } else if (e.getSource() == backBtn) {
            mFrame.dispose();
            if (userRole.equalsIgnoreCase("Customer")) {
                CustomerAppointmentPage customerAppointmentPage = new CustomerAppointmentPage();
                customerAppointmentPage.openPage(userID, userRole);
            } else {
                TechnicianAppointments technicianAppointments = new TechnicianAppointments();
                technicianAppointments.openPage(userID, userRole);
            }
        }
    }

    private JFrame mFrame;
    private String userID, userRole;
    private Appointment selectedAppointment;
    private DefaultListModel<String> commentModel;
    private JList<String> commentList;
    private JTextArea detail;
    private JButton deleteBtn, backBtn;

    public void openPage(String userID, String userRole, Appointment selectedAppointment) {
        this.userID = userID;
        this.userRole = userRole;
        this.selectedAppointment = selectedAppointment;
        
        FileHandler.writeSystemLog("(" + userRole + ")" + userID + " opened the manage own comments for " + userRole + " function" + selectedAppointment.getAppointmentID() + ".");

        mFrame = new JFrame("Manage My Comments");
        mFrame.setSize(900, 520);
        mFrame.setLayout(new BorderLayout(8, 8));
        mFrame.setLocationRelativeTo(null);

        commentModel = new DefaultListModel<>();
        commentList = new JList<>(commentModel);

        detail = new JTextArea();
        detail.setEditable(false);
        detail.setFont(new Font("Monospaced", Font.PLAIN, 13));

        deleteBtn = new JButton("Delete Selected");
        backBtn = new JButton("Back");
        deleteBtn.addActionListener(this);
        backBtn.addActionListener(this);

        loadUserComments();

        commentList.addListSelectionListener(e -> {
            String sel = commentList.getSelectedValue();
            if (sel != null) {
                String id = sel.split(" \\| ")[0];
                for (Comment c : DataStore.allComments) {
                    if (c.getCommentID().equals(id)) {
                        detail.setText("Time: " + c.getDateTime() + "\n\n" + c.getCommentText());
                        break;
                    }
                }
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(commentList), new JScrollPane(detail));
        split.setDividerLocation(300);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(backBtn);
        bottom.add(deleteBtn);

        mFrame.add(split, BorderLayout.CENTER);
        mFrame.add(bottom, BorderLayout.SOUTH);
        mFrame.setVisible(true);
    }

    private void loadUserComments() {
        commentModel.clear();
        boolean hasComments = false;
        for (Comment c : DataStore.allComments) {
            if (c.getAppointmentID().equals(selectedAppointment.getAppointmentID())
                    && c.getUserID().equals(userID)) {
                commentModel.addElement(c.getCommentID() + " | " + c.getDateTime());
                hasComments = true;
            }
        }
        if (!hasComments) detail.setText("You have no comments to manage for this appointment.");
    }

    private void deleteComment() {
        String sel = commentList.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(mFrame, "Select a comment first.");
        } else {
            int confirm = JOptionPane.showConfirmDialog(mFrame, "Delete this comment?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                String id = sel.split(" \\| ")[0];

                String commentText = "";
                for (Comment c : DataStore.allComments) {
                    if (c.getCommentID().equals(id)) {
                        commentText = c.getCommentText();
                        break;
                    }
                }

                CommentController.deleteComment(id);

                FileHandler.writeSystemLog("(" + userRole + ") " + userID + " deleted comment for appointment: " + selectedAppointment.getAppointmentID() + " [Comment: " + commentText + "]");

                JOptionPane.showMessageDialog(mFrame, "Deleted successfully.");
                loadUserComments();
                detail.setText("");
            }
        }
    }
}