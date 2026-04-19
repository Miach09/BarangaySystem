package barangay;

public class User {
    public int     id;
    public String  fullName;
    public String  email;
    public String  phone;
    public String  password;
    public boolean isAdmin;

    public User(int id, String fullName, String email,
                String phone, String password, boolean isAdmin) {
        this.id       = id;
        this.fullName = fullName;
        this.email    = email;
        this.phone    = phone;
        this.password = password;
        this.isAdmin  = isAdmin;
    }
}