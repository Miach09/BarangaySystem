package barangay;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class RequestHistoryPanel extends JPanel {

    private final AppController ctrl;

    public RequestHistoryPanel(AppController ctrl) {
        this.ctrl = ctrl;
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel bar = UITheme.headerBar("Request History");
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        right.setOpaque(false);
        JButton settings = UITheme.settingsButton();
        JButton logout   = UITheme.logoutButton();
        settings.addActionListener(e -> ctrl.showGuestSettings());
        logout.addActionListener(e -> ctrl.logout());
        right.add(settings);
        right.add(logout);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(UITheme.BG_MAIN);
        body.setBorder(new EmptyBorder(16, 24, 20, 24));
        body.setMinimumSize(new Dimension(600, 480));

        JButton backBtn = UITheme.backButton("Back to Dashboard");
        backBtn.addActionListener(e -> ctrl.showGuestDashboard());

        //Request list
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));

        JLabel title = new JLabel("All Requests");
        title.setFont(UITheme.FONT_HEADER);
        card.add(title, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        User user = Database.getCurrentUser();
        List<CertRequest> reqs = Database.getRequestsForUser(user.email);

        if (reqs.isEmpty()) {
            JLabel empty = new JLabel("You have no requests yet.");
            empty.setFont(UITheme.FONT_BODY);
            empty.setForeground(UITheme.TEXT_MUTED);
            listPanel.add(empty);
        } else {
            for (CertRequest req : reqs) {
                listPanel.add(buildRow(req));
                listPanel.add(Box.createVerticalStrut(6));
            }
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        card.add(scroll, BorderLayout.CENTER);

        body.add(backBtn, BorderLayout.NORTH);
        body.add(card, BorderLayout.CENTER);

        return body;
    }

    private JPanel buildRow(CertRequest req) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(new Color(240, 250, 240));
        row.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(10, 12, 10, 12)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);
        JLabel typeLbl = new JLabel(req.requestType);
        typeLbl.setFont(UITheme.FONT_BODY);
        JLabel dateLbl = new JLabel("Submitted: " + req.dateSubmitted);
        dateLbl.setFont(UITheme.FONT_SMALL);
        dateLbl.setForeground(UITheme.TEXT_MUTED);
        textPanel.add(typeLbl);
        textPanel.add(dateLbl);

        row.add(textPanel, BorderLayout.CENTER);
        row.add(UITheme.statusBadge(req.status), BorderLayout.EAST);
        return row;
    }
}