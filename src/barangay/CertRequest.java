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
    public String rejectReason;

    // personal info fields
    public String civilStatus;
    public String sex;
    public String birthdate;
    public String birthplace;
    public String barangayOfficials;
    public String purpose;

    public enum RequestStatus { PENDING, ACCEPTED, REJECTED }

    public CertRequest(String requesterName, String contactNumber, String email,
                       String requestType, String description) {
        this.requesterName    = requesterName;
        this.contactNumber    = contactNumber;
        this.email            = email;
        this.requestType      = requestType;
        this.description      = description;
        this.status           = RequestStatus.PENDING;
        this.civilStatus      = "";
        this.sex              = "";
        this.birthdate        = "";
        this.birthplace       = "";
        this.barangayOfficials = "";
        this.purpose          = "";
    }
}