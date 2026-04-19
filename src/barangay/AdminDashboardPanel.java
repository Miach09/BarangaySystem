package barangay;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class AdminDashboardPanel extends JPanel {

    private final AppController ctrl;

    public AdminDashboardPanel(AppController ctrl) {
        this.ctrl = ctrl;
        buildUI();
    }

    private void buildUI() {
        removeAll();
        setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel buildHeader() {
        JPanel bar = UITheme.headerBar("ADMIN DASHBOARD");
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        right.setOpaque(false);
        JButton settings = UITheme.settingsButton();
        JButton logout   = UITheme.logoutButton();
        settings.addActionListener(e -> ctrl.showAdminSettings());
        logout.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) ctrl.logout();
        });
        right.add(settings);
        right.add(logout);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(UITheme.BG_MAIN);
        body.setBorder(new EmptyBorder(20, 24, 20, 24));
        body.setMinimumSize(new Dimension(600, 480));

        List<CertRequest> all = Database.getAllRequests();
        long pending  = all.stream().filter(r -> r.status == CertRequest.RequestStatus.PENDING).count();
        long finished = all.stream().filter(r -> r.status == CertRequest.RequestStatus.RESOLVED).count();

        //Stat cards
        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 0));
        stats.setOpaque(false);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        stats.add(UITheme.statCard("Total Request",    String.valueOf(all.size()), UITheme.PRIMARY));
        stats.add(UITheme.statCard("Pending Request",  String.valueOf(pending),    UITheme.WARNING));
        stats.add(UITheme.statCard("Finished Request", String.valueOf(finished),   UITheme.SUCCESS));

        //All requests card
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));
        JLabel cardTitle = new JLabel("All Request");
        cardTitle.setFont(UITheme.FONT_HEADER);
        card.add(cardTitle, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        if (all.isEmpty()) {
            JLabel empty = new JLabel("No requests yet.");
            empty.setFont(UITheme.FONT_BODY);
            empty.setForeground(UITheme.TEXT_MUTED);
            listPanel.add(empty);
        } else {
            for (CertRequest req : all) {
                listPanel.add(buildAdminRow(req));
                listPanel.add(Box.createVerticalStrut(6));
            }
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        card.add(scroll, BorderLayout.CENTER);

        body.add(stats);
        body.add(Box.createVerticalStrut(16));
        body.add(card);

        return body;
    }

    private JPanel buildAdminRow(CertRequest req) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, UITheme.BORDER),
            new EmptyBorder(10, 8, 10, 8)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);
        JLabel nameLbl = new JLabel(req.requesterName);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel typeLbl = new JLabel(req.requestType);
        typeLbl.setFont(UITheme.FONT_SMALL);
        typeLbl.setForeground(UITheme.TEXT_MUTED);
        textPanel.add(nameLbl);
        textPanel.add(typeLbl);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnPanel.setOpaque(false);

        if (req.status == CertRequest.RequestStatus.PENDING) {
            JButton acceptBtn = UITheme.acceptButton("Accept");
            acceptBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(null,
                    "Accept this request?\n" +
                    "\nRequester: " + req.requesterName + 
                    "\nType: " + req.requestType,
                    "Confirm Accept", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) return;
                Database.resolveRequest(req.id);
                buildUI();
            });
            btnPanel.add(acceptBtn);
        }

        JButton deleteBtn = UITheme.dangerButton("Delete");
        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this request?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                Database.deleteRequest(req.id);
                buildUI();
            }
        });
        btnPanel.add(deleteBtn);

        row.add(textPanel, BorderLayout.CENTER);
        row.add(btnPanel,  BorderLayout.EAST);

        return row;
    }
}