public class Customer {
    private int customerId;
    private String username;
    private String fullName;
    private String address;   // NEW

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAddress() { return address; }  // NEW
    public void setAddress(String address) { this.address = address; } // NEW
}
