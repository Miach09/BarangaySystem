package barangay;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class LoginPanel extends JPanel {

    private final AppController ctrl;
    private JTextField emailField;
    private JPasswordField passField;

    public LoginPanel(AppController ctrl) {
        this.ctrl = ctrl;
        setBackground(UITheme.BG_MAIN);
        setLayout(new GridBagLayout());
        add(buildCard());
    }

    private JPanel buildCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER, 1, true),
            new EmptyBorder(40, 50, 40, 50)));
        card.setPreferredSize(new Dimension(500, 530));

        //Logo
        ImageIcon icon = new ImageIcon(getClass().getResource("/barangay/brgyLogo.png"));
        Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
        JLabel logo = new JLabel(new ImageIcon(img));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        logo.setAlignmentX(CENTER_ALIGNMENT);

        //Title
        JLabel title = new JLabel("Barangay Bagong Sikat", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(UITheme.TEXT_DARK);
        title.setAlignmentX(CENTER_ALIGNMENT);

        //Subtitle
        JLabel subtitle = new JLabel("Barangay Certificate Request System", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(UITheme.TEXT_MUTED);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        //Fields
        emailField = UITheme.textField("youremail@gmail.com");
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        passField = UITheme.passwordField();
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        JPanel form = new JPanel(new GridLayout(2, 1, 0, 14));
        form.setOpaque(false);
        form.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        form.setAlignmentX(CENTER_ALIGNMENT);
        form.add(labeled("Email Address", emailField));
        form.add(labeled("Password", passField));

        //Buttons
        JButton loginBtn = UITheme.primaryButton("Login");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        loginBtn.setAlignmentX(CENTER_ALIGNMENT);

        loginBtn.addActionListener(e -> doLogin());
        passField.addActionListener(e -> doLogin());

        //Create account link
        JButton createLink = UITheme.linkButton("Create Account");
        createLink.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JLabel noAccountLbl = new JLabel("Don't have an account?");
        noAccountLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        linkRow.setOpaque(false);
        linkRow.setAlignmentX(CENTER_ALIGNMENT);
        linkRow.add(noAccountLbl);
        linkRow.add(createLink);
        createLink.addActionListener(e -> ctrl.showRegister());

        card.add(logo);
        card.add(Box.createVerticalStrut(8));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(24));
        card.add(form);
        card.add(Box.createVerticalStrut(16));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(14));
        card.add(linkRow);

        return card;
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(UITheme.TEXT_DARK);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String pass  = new String(passField.getPassword());

        if (email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both email and password.",
                "Login Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        //Works for both guest and admin — routes based on is_admin flag in DB
        User user = Database.login(email, pass);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Invalid email or password.",
                "Login Failed", JOptionPane.ERROR_MESSAGE);
        } else {
            if (user.isAdmin) ctrl.showAdminDashboard();
            else              ctrl.showGuestDashboard();
        }
    }
}