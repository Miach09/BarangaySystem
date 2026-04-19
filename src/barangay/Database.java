package barangay;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Database layer — connects to MySQL via JDBC.
 *
 * Connection targets:
 *   Host    : localhost
 *   Port    : 3306
 *   DB name : barangay_db
 *   User    : root
 *   Pass    : (empty — default XAMPP)
 *
 */
public class Database {

    //Connection config
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/barangay_db"
                                        + "?useSSL=false&serverTimezone=Asia/Manila"
                                        + "&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";          // XAMPP default is blank

    private static User currentUser = null;

    //Get a connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    /**
     * Call once at startup (in Main.java) to verify the DB is reachable.
     * Returns false and shows an error dialog if it cannot connect.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("[DB] Connected: " + conn.getMetaData().getURL());
            return true;
        } catch (SQLException e) {
            System.err.println("[DB] Connection FAILED: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                "Cannot connect to MySQL.\n\n"
              + "Please check:\n"
              + "  1. XAMPP is open and MySQL is STARTED (green)\n"
              + "  2. You ran barangay_db.sql in phpMyAdmin\n"
              + "  3. DB user is 'root', password is empty\n\n"
              + "Error: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    //Authenticate

    public static User login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                boolean isAdmin = rs.getInt("is_admin") == 1;
                currentUser = new User(
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("password"),
                    isAdmin
                );
                System.out.println("[DB] Login OK: " + email + " | admin=" + isAdmin);
                return currentUser;
            }
        } catch (SQLException e) {
            System.err.println("[DB] login error: " + e.getMessage());
        }
        return null;
    }

    public static boolean register(String fullName, String email, String phone, String password) {
        //Check for duplicate email first
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id FROM users WHERE email = ?")) {
            ps.setString(1, email);
            if (ps.executeQuery().next()) return false;
        } catch (SQLException e) {
            System.err.println("[DB] register-check error: " + e.getMessage());
            return false;
        }

        String sql = "INSERT INTO users (full_name, email, phone, password) VALUES (?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, password);
            int rows = ps.executeUpdate();
            System.out.println("[DB] Registered: " + email + " (" + rows + " row)");
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[DB] register error: " + e.getMessage());
            return false;
        }
    }

    public static User getCurrentUser() { return currentUser; }
    public static void logout()         { currentUser = null; }

    //Requests
    public static List<CertRequest> getAllRequests() {
        List<CertRequest> list = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT * FROM requests ORDER BY submitted_at DESC")) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DB] getAllRequests error: " + e.getMessage());
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
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DB] getRequestsForUser error: " + e.getMessage());
        }
        return list;
    }

    public static boolean addRequest(CertRequest req) {
        String sql = "INSERT INTO requests (requester_name, contact_number, email, "
                   + "request_type, description) VALUES (?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, req.requesterName);
            ps.setString(2, req.contactNumber);
            ps.setString(3, req.email);
            ps.setString(4, req.requestType);
            ps.setString(5, req.description);

            int rows = ps.executeUpdate();

            //Get the MySQL auto-generated ID
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) req.id = keys.getInt(1);

            System.out.println("[DB] Request inserted: id=" + req.id
                + " | type=" + req.requestType + " | " + rows + " row");
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[DB] addRequest error: " + e.getMessage());
            return false;
        }
    }

    public static boolean resolveRequest(int id) {
        String sql = "UPDATE requests SET status = 'RESOLVED' WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            System.out.println("[DB] Resolved id=" + id + " (" + rows + " row)");
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[DB] resolveRequest error: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteRequest(int id) {
        String sql = "DELETE FROM requests WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            System.out.println("[DB] Deleted id=" + id + " (" + rows + " row)");
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[DB] deleteRequest error: " + e.getMessage());
            return false;
        }
    }

    //Settings
    public static boolean updateProfile(String name, String phone, String email) {
        if (currentUser == null) return false;
        String sql = "UPDATE users SET full_name=?, phone=?, email=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, email);
            ps.setInt(4, currentUser.id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                currentUser.fullName = name;
                currentUser.phone    = phone;
                currentUser.email    = email;
                System.out.println("[DB] Profile updated id=" + currentUser.id);
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
            System.out.println("[DB] Password changed id=" + currentUser.id);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[DB] updatePassword error: " + e.getMessage());
            return false;
        }
    }

    //Helpers
    private static CertRequest mapRow(ResultSet rs) throws SQLException {
        CertRequest req = new CertRequest(
            rs.getString("requester_name"),
            rs.getString("contact_number"),
            rs.getString("email"),
            rs.getString("request_type"),
            rs.getString("description")
        );
        req.id            = rs.getInt("id");
        req.status        = "RESOLVED".equals(rs.getString("status"))
                            ? CertRequest.RequestStatus.RESOLVED
                            : CertRequest.RequestStatus.PENDING;
        req.dateSubmitted = rs.getString("submitted_at");
        return req;
    }
}