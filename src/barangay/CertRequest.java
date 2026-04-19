package barangay;

public class CertRequest {
    public int    id;
    public String requesterName;
    public String contactNumber;
    public String email;
    public String requestType;
    public String description;
    public RequestStatus status;
    public String dateSubmitted;

    public enum RequestStatus { PENDING, RESOLVED }

    public CertRequest(String requesterName, String contactNumber, String email,
                       String requestType, String description) {
        this.requesterName = requesterName;
        this.contactNumber = contactNumber;
        this.email         = email;
        this.requestType   = requestType;
        this.description   = description;
        this.status        = RequestStatus.PENDING;
    }
}