package barangay;

public class Notification {
    public int    id;
    public String userEmail;
    public String message;
    public String type;       // "RESOLVED" | "REJECTED"
    public boolean isRead;
    public String createdAt;

    public Notification(int id, String userEmail, String message,
                        String type, boolean isRead, String createdAt) {
        this.id        = id;
        this.userEmail = userEmail;
        this.message   = message;
        this.type      = type;
        this.isRead    = isRead;
        this.createdAt = createdAt;
    }
}