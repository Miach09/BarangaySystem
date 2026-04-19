package barangay;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class GuestDashboardPanel extends JPanel {

    private final AppController ctrl;

    public GuestDashboardPanel(AppController ctrl) {
        this.ctrl = ctrl;
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
    }

    //Header bar
    private JPanel buildHeader() {
        JPanel bar = UITheme.headerBar("DASHBOARD");

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        right.setOpaque(false);

        JButton settings = UITheme.settingsButton();
        JButton logout   = UITheme.logoutButton();
        settings.addActionListener(e -> ctrl.showGuestSettings());
        logout.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) ctrl.logout();
        });

        right.add(settings);
        right.add(logout);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    //Body
    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(UITheme.BG_MAIN);
        body.setBorder(new EmptyBorder(20, 24, 20, 24));
        body.setMinimumSize(new Dimension(600, 480));
        body.setPreferredSize(new Dimension(820, 560));

        User user = Database.getCurrentUser();
        List<CertRequest> myReqs = Database.getRequestsForUser(user.email);

        long pending  = myReqs.stream().filter(r -> r.status == CertRequest.RequestStatus.PENDING).count();
        long resolved = myReqs.stream().filter(r -> r.status == CertRequest.RequestStatus.RESOLVED).count();

        //Stat cards
        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 0));
        stats.setOpaque(false);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        stats.add(UITheme.statCard("Total Request",   String.valueOf(myReqs.size()), UITheme.PRIMARY));
        stats.add(UITheme.statCard("Pending Request",  String.valueOf(pending),       UITheme.WARNING));
        stats.add(UITheme.statCard("Resolve Request",  String.valueOf(resolved),      UITheme.SUCCESS));

        //Recent requests list
        JPanel recentCard = UITheme.card();
        recentCard.setLayout(new BorderLayout(0, 8));
        recentCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel recentTitle = new JLabel("Recent Request");
        recentTitle.setFont(UITheme.FONT_HEADER);
        recentCard.add(recentTitle, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        List<CertRequest> recent = myReqs;
        if (recent.isEmpty()) {
            JLabel empty = new JLabel("No requests yet.");
            empty.setFont(UITheme.FONT_BODY);
            empty.setForeground(UITheme.TEXT_MUTED);
            listPanel.add(empty);
        } else {
            for (CertRequest req : recent) {
                listPanel.add(requestRow(req));
                listPanel.add(Box.createVerticalStrut(6));
            }
        }

        JScrollPane recentScroll = new JScrollPane(listPanel);
        recentScroll.setBorder(null);
        recentScroll.setOpaque(false);
        recentScroll.getViewport().setOpaque(false);
        recentScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        recentScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        recentScroll.getVerticalScrollBar().setUnitIncrement(10);
        recentScroll.setPreferredSize(new Dimension(0, 200));
        recentCard.add(recentScroll, BorderLayout.CENTER);

        //Action buttons
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton newReqBtn = UITheme.primaryButton("+ New Request");
        JButton histBtn   = UITheme.secondaryButton("Request History");
        newReqBtn.addActionListener(e -> ctrl.showNewRequest());
        histBtn.addActionListener(e -> ctrl.showRequestHistory());

        btnRow.add(newReqBtn);
        btnRow.add(histBtn);

        //Welcome label
        JLabel welcome = new JLabel("Welcome back, " + user.fullName + "!");
        welcome.setFont(UITheme.FONT_HEADER);
        welcome.setForeground(UITheme.TEXT_MUTED);
        welcome.setAlignmentX(CENTER_ALIGNMENT);

        body.add(welcome);
        body.add(Box.createVerticalStrut(12));
        body.add(stats);
        body.add(Box.createVerticalStrut(16));
        body.add(recentCard);
        body.add(Box.createVerticalStrut(16));
        body.add(btnRow);

        return body;
    }

    private JPanel requestRow(CertRequest req) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(new Color(240, 250, 240));
        row.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(8, 12, 8, 12)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JLabel typeLbl = new JLabel(req.requestType);
        typeLbl.setFont(UITheme.FONT_BODY);

        row.add(typeLbl, BorderLayout.CENTER);
        row.add(UITheme.statusBadge(req.status), BorderLayout.EAST);
        return row;
    }
}