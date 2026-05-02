package barangay;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Floating toast popup that appears in the top-right corner of the parent frame.
 * Shows one card per unread/undismissed accepted or rejected request.
 * Each card is individually closable.
 */
public class ToastNotification {

    public static void show(JFrame parent, String userEmail) {
        List<CertRequest> requests = Database.getRequestsForUser(userEmail);

        // Only show accepted/rejected that haven't been dismissed
        List<CertRequest> toShow = new java.util.ArrayList<>();
        for (CertRequest r : requests) {
            if ((r.status == CertRequest.RequestStatus.ACCEPTED
                    || r.status == CertRequest.RequestStatus.REJECTED)
                    && !Database.isAlertDismissed(r.id, userEmail)) {
                toShow.add(r);
            }
        }

        if (toShow.isEmpty()) return;

        // Build one floating panel that stacks all toast cards
        JWindow toast = new JWindow(parent);
        toast.setLayout(new BoxLayout(toast.getContentPane(), BoxLayout.Y_AXIS));
        toast.getContentPane().setBackground(new Color(0, 0, 0, 0));
        toast.setBackground(new Color(0, 0, 0, 0));

        for (CertRequest req : toShow) {
            JPanel card = buildCard(req, userEmail, toast, parent);
            toast.add(card);
            toast.add(Box.createVerticalStrut(6));
        }

        toast.pack();
        positionToast(toast, parent);
        toast.setVisible(true);

        // Reposition if parent moves or resizes
        parent.addComponentListener(new ComponentAdapter() {
            @Override public void componentMoved(ComponentEvent e)  { positionToast(toast, parent); }
            @Override public void componentResized(ComponentEvent e) { positionToast(toast, parent); }
        });
    }

    private static JPanel buildCard(CertRequest req, String userEmail,
                                     JWindow toast, JFrame parent) {
        boolean isAccepted = req.status == CertRequest.RequestStatus.ACCEPTED;

        JPanel card = new JPanel(new BorderLayout(10, 6));
        card.setBackground(isAccepted ? new Color(235, 252, 240) : new Color(255, 235, 235));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(isAccepted ? new Color(60, 180, 100) : new Color(210, 60, 60), 2, true),
            new EmptyBorder(12, 14, 12, 10)));
        card.setMaximumSize(new Dimension(340, 100));
        card.setPreferredSize(new Dimension(340, isAccepted ? 80 : 95));

        // Icon
        JLabel icon = new JLabel(isAccepted ? "✅" : "❌");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        icon.setVerticalAlignment(SwingConstants.TOP);

        // Text
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLbl = new JLabel(isAccepted ? "Request Accepted!" : "Request Rejected");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(isAccepted ? new Color(0, 110, 40) : new Color(160, 20, 20));

        JLabel typeLbl = new JLabel(req.requestType);
        typeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        typeLbl.setForeground(new Color(60, 60, 60));

        textPanel.add(titleLbl);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(typeLbl);

        if (!isAccepted && req.rejectReason != null && !req.rejectReason.isEmpty()) {
            JLabel reasonLbl = new JLabel("<html>Reason: " + req.rejectReason + "</html>");
            reasonLbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            reasonLbl.setForeground(new Color(120, 40, 40));
            reasonLbl.setMaximumSize(new Dimension(240, 30));
            textPanel.add(Box.createVerticalStrut(2));
            textPanel.add(reasonLbl);
        }

        // Close button
        JButton closeBtn = new JButton("×");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        closeBtn.setForeground(new Color(130, 130, 130));
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.setToolTipText("Dismiss");
        closeBtn.addActionListener(e -> {
            Database.dismissAlert(req.id, userEmail);
            // Remove this card and its spacer
            Container content = toast.getContentPane();
            int idx = -1;
            for (int i = 0; i < content.getComponentCount(); i++) {
                if (content.getComponent(i) == card) { idx = i; break; }
            }
            if (idx >= 0) {
                content.remove(card);
                // Remove strut after card if present
                if (idx < content.getComponentCount()) {
                    Component next = content.getComponent(idx);
                    if (!(next instanceof JPanel)) content.remove(idx);
                }
            }
            if (content.getComponentCount() == 0) {
                toast.dispose();
            } else {
                toast.pack();
                positionToast(toast, parent);
                toast.revalidate();
                toast.repaint();
            }
        });

        card.add(icon,     BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        card.add(closeBtn,  BorderLayout.EAST);

        return card;
    }

    private static void positionToast(JWindow toast, JFrame parent) {
        // Top-right corner of the parent frame, just below the header bar
        Point loc  = parent.getLocationOnScreen();
        Dimension  fs = parent.getSize();
        int x = loc.x + fs.width  - toast.getWidth()  - 16;
        int y = loc.y + 70; // below the header bar
        toast.setLocation(x, y);
    }
}