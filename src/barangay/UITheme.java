package barangay;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class UITheme {

    //Colors
    public static final Color PRIMARY       = new Color(30, 100, 210);
    public static final Color PRIMARY_HOVER = new Color(20, 80, 180);
    public static final Color SUCCESS       = new Color(34, 160, 90);
    public static final Color WARNING       = new Color(220, 140, 20);
    public static final Color DANGER        = new Color(210, 50, 50);
    public static final Color BG_MAIN       = new Color(240, 248, 240);   // light mint
    public static final Color BG_CARD       = Color.WHITE;
    public static final Color BG_HEADER     = new Color(220, 240, 220);
    public static final Color TEXT_DARK     = new Color(30, 30, 30);
    public static final Color TEXT_MUTED    = new Color(100, 100, 110);
    public static final Color BORDER        = new Color(200, 210, 200);

    //Fonts
    public static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BTN    = new Font("Segoe UI", Font.BOLD,  13);

    //Builders 

    /** Rounded-corner blue primary button */
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? PRIMARY_HOVER :
                            getModel().isRollover() ? PRIMARY_HOVER : PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BTN);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, 36));
        return btn;
    }

    /** White outline / secondary button */
    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BTN);
        btn.setForeground(PRIMARY);
        btn.setBackground(Color.WHITE);
        btn.setBorder(new LineBorder(PRIMARY, 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Small danger (red) button */
    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? DANGER.darker() : DANGER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_SMALL);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(70, 28));
        return btn;
    }

    /** Small accept (green) button */
    public static JButton acceptButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? SUCCESS.darker() : SUCCESS);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_SMALL);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(70, 28));
        return btn;
    }

    /** Styled text field */
    public static JTextField textField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(6, 10, 6, 10)));
        tf.setToolTipText(placeholder);
        return tf;
    }

    /** Styled password field */
    public static JPasswordField passwordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(FONT_BODY);
        pf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(6, 10, 6, 10)));
        return pf;
    }

    /** White card panel with subtle border */
    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(16, 16, 16, 16)));
        return p;
    }

    /** Top navigation/header bar */
    public static JPanel headerBar(String title) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_HEADER);
        bar.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));
        bar.setPreferredSize(new Dimension(0, 50));
        JLabel lbl = new JLabel("  " + title);
        lbl.setFont(FONT_HEADER);
        lbl.setForeground(TEXT_DARK);
        bar.add(lbl, BorderLayout.WEST);
        return bar;
    }

    /** Stat summary card (for dashboard) */
    public static JPanel statCard(String label, String value, Color iconColor) {
        JPanel card = new JPanel(new BorderLayout(8, 4));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(12, 16, 12, 16)));

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valLbl.setForeground(TEXT_DARK);

        JLabel nameLbl = new JLabel(label);
        nameLbl.setFont(FONT_SMALL);
        nameLbl.setForeground(TEXT_MUTED);

        JLabel icon = new JLabel("●");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        icon.setForeground(iconColor);
        icon.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);
        textPanel.add(valLbl);
        textPanel.add(nameLbl);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(icon, BorderLayout.EAST);
        return card;
    }

    /** Status badge label */
    public static JLabel statusBadge(CertRequest.RequestStatus status) {
        JLabel lbl = new JLabel(status == CertRequest.RequestStatus.PENDING ? "Pending" : "Resolved");
        lbl.setFont(FONT_SMALL);
        lbl.setOpaque(true);
        if (status == CertRequest.RequestStatus.PENDING) {
            lbl.setBackground(new Color(255, 235, 170));
            lbl.setForeground(new Color(120, 70, 0));
        } else {
            lbl.setBackground(new Color(180, 240, 200));
            lbl.setForeground(new Color(0, 100, 40));
        }
        lbl.setBorder(new EmptyBorder(2, 8, 2, 8));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        return lbl;
    }

    /** Link-style button */
    public static JButton linkButton(String text) {
        JButton btn = new JButton("<html><u>" + text + "</u></html>");
        btn.setFont(FONT_BODY);
        btn.setForeground(PRIMARY);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Draws a gear icon as an ImageIcon */
    private static ImageIcon gearIcon(int size, Color color) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(size, size,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);
        int teeth = 8;
        int cx = size / 2, cy = size / 2;
        int outerR = size / 2 - 1, innerR = size / 2 - 5, holeR = size / 5;
        java.awt.geom.Path2D gear = new java.awt.geom.Path2D.Float();
        for (int i = 0; i < teeth * 2; i++) {
            double angle = Math.PI * i / teeth - Math.PI / (teeth * 2);
            int r = (i % 2 == 0) ? outerR : innerR;
            float x = cx + (float)(r * Math.cos(angle));
            float y = cy + (float)(r * Math.sin(angle));
            if (i == 0) gear.moveTo(x, y); else gear.lineTo(x, y);
        }
        gear.closePath();
        g.fill(gear);
        g.setColor(new Color(0, 0, 0, 0));
        g.setComposite(java.awt.AlphaComposite.Clear);
        g.fillOval(cx - holeR, cy - holeR, holeR * 2, holeR * 2);
        g.dispose();
        return new ImageIcon(img);
    }

    /** Draws a logout/door arrow icon as an ImageIcon */
    private static ImageIcon logoutIcon(int size, Color color) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(size, size,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        //Door rectangle
        int pad = size / 5;
        g.drawRect(pad, pad, size / 3, size - pad * 2);
        //Arrow pointing right
        int arrowY = size / 2;
        int arrowStartX = size / 2;
        int arrowEndX = size - pad;
        g.drawLine(arrowStartX, arrowY, arrowEndX, arrowY);
        g.drawLine(arrowEndX - 5, arrowY - 5, arrowEndX, arrowY);
        g.drawLine(arrowEndX - 5, arrowY + 5, arrowEndX, arrowY);
        g.dispose();
        return new ImageIcon(img);
    }

    /** Back to Dashboard button with arrow icon */
    public static JButton backButton(String label) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(18, 18,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(30, 100, 210));
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        //Arrow shaft
        g.drawLine(14, 9, 4, 9);
        //Arrow head
        g.drawLine(4, 9, 9, 4);
        g.drawLine(4, 9, 9, 14);
        g.dispose();
        ImageIcon icon = new ImageIcon(img);

        JButton btn = new JButton(label, icon) {
            @Override protected void paintComponent(Graphics g2d) {
                Graphics2D g2 = (Graphics2D) g2d.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(220, 232, 255) : new Color(236, 242, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g2d);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(PRIMARY);
        btn.setIconTextGap(6);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 12, 6, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Settings icon button for header bar */
    public static JButton settingsButton() {
        ImageIcon icon = gearIcon(18, new Color(50, 50, 50));
        JButton btn = new JButton("Settings", icon) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(210, 230, 210) : new Color(228, 242, 228));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(40, 40, 40));
        btn.setIconTextGap(6);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Logout icon button for header bar */
    public static JButton logoutButton() {
        ImageIcon icon = logoutIcon(18, new Color(180, 30, 30));
        JButton btn = new JButton("Logout", icon) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(255, 210, 210) : new Color(255, 230, 230));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(180, 30, 30));
        btn.setIconTextGap(6);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}