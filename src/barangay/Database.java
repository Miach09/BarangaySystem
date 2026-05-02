package barangay;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class Database {

    private static final String DB_URL  = "jdbc:mysql://localhost:3306/barangay_db"
                                        + "?useSSL=false&serverTimezone=Asia/Manila"
                                        + "&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    public static final int MAX_PENDING_REQUESTS = 3;

    private static User currentUser = null;

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("[DB] Connected: " + conn.getMetaData().getURL());
            return true;
        } catch (SQLException e) {
            System.err.println("[DB] Connection FAILED: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                "Cannot connect to MySQL.\n\n"
              + "Please check:\n"
              + "  1. XAMPP is open and MySQL is STARTED\n"
              + "  2. You ran barangay_db.sql in phpMyAdmin\n"
              + "  3. DB user is 'root', password is empty\n\n"
              + "Error: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // AUTH

    public static User login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                currentUser = mapUser(rs);
                System.out.println("[DB] Login OK: " + email + " | admin=" + currentUser.isAdmin);
                return currentUser;
            }
        } catch (SQLException e) {
            System.err.println("[DB] login error: " + e.getMessage());
        }
        return null;
    }

    public static boolean register(String fullName, String email, String phone,
                                   String address, String password) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id FROM users WHERE email = ?")) {
            ps.setString(1, email);
            if (ps.executeQuery().next()) return false;
        } catch (SQLException e) {
            System.err.println("[DB] register-check error: " + e.getMessage());
            return false;
        }

        String sql = "INSERT INTO users (full_name, email, phone, address, password) VALUES (?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, address);
            ps.setString(5, password);
            int rows = ps.executeUpdate();
            System.out.println("[DB] Registered: " + email);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[DB] register error: " + e.getMessage());
            return false;
        }
    }

    public static User getCurrentUser() { return currentUser; }
    public static void logout()         { currentUser = null; }

    public static String getUserAddress(String email) {
        if (email == null || email.isEmpty()) return "";
        String sql = "SELECT address FROM users WHERE email = ?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String addr = rs.getString("address");
                return addr != null ? addr : "";
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return "";
    }

    // REQUESTS

    public static List<CertRequest> getAllRequests() {
        List<CertRequest> list = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT * FROM requests ORDER BY submitted_at DESC")) {
            while (rs.next()) list.add(mapRequest(rs));
        } catch (SQLException e) {
            System.err.println("[DB] getAllRequests error: " + e.getMessage());
        }
        return list;
    }

    public static List<CertRequest> getAllRequestsSorted(String sortBy) {
        List<CertRequest> list = new ArrayList<>();
        String orderClause;
        switch (sortBy) {
            case "name_asc":  orderClause = "requester_name ASC"; break;
            case "name_desc": orderClause = "requester_name DESC"; break;
            case "date_asc":  orderClause = "submitted_at ASC"; break;
            default:          orderClause = "submitted_at DESC"; break;
        }
        String sql = "SELECT * FROM requests ORDER BY " + orderClause;
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRequest(rs));
        } catch (SQLException e) {
            System.err.println("[DB] getAllRequestsSorted error: " + e.getMessage());
        }
        return list;
    }

    public static List<CertRequest> getRequestsForUser(String email) {
        List<CertRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM requests WHERE email = ? ORDER BY submitted_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRequest(rs));
        } catch (SQLException e) {
            System.err.println("[DB] getRequestsForUser error: " + e.getMessage());
        }
        return list;
    }

    public static int countPendingForUser(String email) {
        String sql = "SELECT COUNT(*) FROM requests WHERE email = ? AND status = 'PENDING'";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DB] countPendingForUser error: " + e.getMessage());
        }
        return 0;
    }

    public static boolean addRequest(CertRequest req) {
        String sql = "INSERT INTO requests (requester_name, contact_number, email, "
                   + "request_type, description, civil_status, sex, birthdate, birthplace) "
                   + "VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, req.requesterName);
            ps.setString(2, req.contactNumber);
            ps.setString(3, req.email);
            ps.setString(4, req.requestType);
            ps.setString(5, req.description);
            ps.setString(6, req.civilStatus);
            ps.setString(7, req.sex);
            ps.setString(8, req.birthdate);
            ps.setString(9, req.birthplace);
            int rows = ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) req.id = keys.getInt(1);
            System.out.println("[DB] Request inserted: id=" + req.id + " type=" + req.requestType);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[DB] addRequest error: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateRequestAdminFields(int id, String officials, String purpose) {
        String sql = "UPDATE requests SET barangay_officials=?, purpose=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, officials);
            ps.setString(2, purpose);
            ps.setInt(3, id);
            int rows = ps.executeUpdate();
            System.out.println("[DB] Admin fields updated id=" + id);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[DB] updateRequestAdminFields error: " + e.getMessage());
            return false;
        }
    }

    public static boolean resolveRequest(int id) {
        // Get request info first for notification
        String email = "", type = "";
        String getReq = "SELECT email, request_type FROM requests WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(getReq)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) { email = rs.getString("email"); type = rs.getString("request_type"); }
        } catch (SQLException e) { System.err.println("[DB] resolveRequest-fetch error: " + e.getMessage()); }

        String sql = "UPDATE requests SET status = 'ACCEPTED' WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0 && !email.isEmpty()) {
                createNotification(email,
                    "Your request for \"" + type + "\" has been ACCEPTED by the admin.",
                    "ACCEPTED");
            }
            System.out.println("[DB] Resolved id=" + id);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[DB] resolveRequest error: " + e.getMessage());
            return false;
        }
    }

    public static boolean rejectRequest(int id, String reason) {
        // Get request info first for notification
        String email = "", type = "";
        String getReq = "SELECT email, request_type FROM requests WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(getReq)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) { email = rs.getString("email"); type = rs.getString("request_type"); }
        } catch (SQLException e) { System.err.println("[DB] rejectRequest-fetch error: " + e.getMessage()); }

        String sql = "UPDATE requests SET status = 'REJECTED', reject_reason = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reason);
            ps.setInt(2, id);
            int rows = ps.executeUpdate();
            if (rows > 0 && !email.isEmpty()) {
                createNotification(email,
                    "Your request for \"" + type + "\" has been REJECTED. Reason: " + reason,
                    "REJECTED");
            }
            System.out.println("[DB] Rejected id=" + id + " reason=" + reason);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[DB] rejectRequest error: " + e.getMessage());
            return false;
        }
    }


    // NOTIFICATIONS

    public static List<Notification> getNotificationsForUser(String email) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_email = ? ORDER BY created_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Notification(
                    rs.getInt("id"),
                    rs.getString("user_email"),
                    rs.getString("message"),
                    rs.getString("type"),
                    rs.getInt("is_read") == 1,
                    rs.getString("created_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[DB] getNotificationsForUser error: " + e.getMessage());
        }
        return list;
    }

    public static int countUnreadNotifications(String email) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_email = ? AND is_read = 0";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DB] countUnread error: " + e.getMessage());
        }
        return 0;
    }

    public static void markAllNotificationsRead(String email) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE user_email = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] markAllRead error: " + e.getMessage());
        }
    }


    public static void dismissAlert(int requestId, String userEmail) {
        String sql = "INSERT IGNORE INTO dismissed_alerts (request_id, user_email) VALUES (?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.setString(2, userEmail);
            ps.executeUpdate();
            System.out.println("[DB] Alert dismissed: requestId=" + requestId);
        } catch (SQLException e) {
            System.err.println("[DB] dismissAlert error: " + e.getMessage());
        }
    }

    public static boolean isAlertDismissed(int requestId, String userEmail) {
        String sql = "SELECT 1 FROM dismissed_alerts WHERE request_id = ? AND user_email = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.setString(2, userEmail);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            System.err.println("[DB] isAlertDismissed error: " + e.getMessage());
            return false;
        }
    }

    private static void createNotification(String userEmail, String message, String type) {
        String sql = "INSERT INTO notifications (user_email, message, type) VALUES (?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userEmail);
            ps.setString(2, message);
            ps.setString(3, type);
            ps.executeUpdate();
            System.out.println("[DB] Notification created for: " + userEmail);
        } catch (SQLException e) {
            System.err.println("[DB] createNotification error: " + e.getMessage());
        }
    }

    // SETTINGS

    public static boolean updateProfile(String name, String phone, String address, String email) {
        if (currentUser == null) return false;
        String sql = "UPDATE users SET full_name=?, phone=?, address=?, email=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, address);
            ps.setString(4, email);
            ps.setInt(5, currentUser.id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                currentUser.fullName = name;
                currentUser.phone    = phone;
                currentUser.address  = address;
                currentUser.email    = email;
            }
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[DB] updateProfile error: " + e.getMessage());
            return false;
        }
    }

    public static boolean updatePassword(String current, String newPass) {
        if (currentUser == null) return false;
        if (!currentUser.password.equals(current)) return false;
        String sql = "UPDATE users SET password=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPass);
            ps.setInt(2, currentUser.id);
            int rows = ps.executeUpdate();
            if (rows > 0) currentUser.password = newPass;
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[DB] updatePassword error: " + e.getMessage());
            return false;
        }
    }

    // HELPERS

    private static User mapUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("address") != null ? rs.getString("address") : "",
            rs.getString("password"),
            rs.getInt("is_admin") == 1
        );
    }

    private static CertRequest mapRequest(ResultSet rs) throws SQLException {
        CertRequest req = new CertRequest(
            rs.getString("requester_name"),
            rs.getString("contact_number"),
            rs.getString("email"),
            rs.getString("request_type"),
            rs.getString("description")
        );
        req.id                = rs.getInt("id");
        req.dateSubmitted     = rs.getString("submitted_at");
        req.rejectReason      = rs.getString("reject_reason");
        req.civilStatus       = safe(rs.getString("civil_status"));
        req.sex               = safe(rs.getString("sex"));
        req.birthdate         = safe(rs.getString("birthdate"));
        req.birthplace        = safe(rs.getString("birthplace"));
        req.barangayOfficials = safe(rs.getString("barangay_officials"));
        req.purpose           = safe(rs.getString("purpose"));
        String status         = rs.getString("status");
        switch (status) {
            case "ACCEPTED": req.status = CertRequest.RequestStatus.ACCEPTED; break;
            case "REJECTED": req.status = CertRequest.RequestStatus.REJECTED; break;
            default:         req.status = CertRequest.RequestStatus.PENDING;  break;
        }
        return req;
    }

    private static String safe(String s) { return s != null ? s : ""; }
}