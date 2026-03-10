import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {
    private int orderId;
    private int customerId;
    private LocalDateTime orderDate;
    private String status;
    private BigDecimal totalAmount;
    private String customerName; // NEW (optional, mainly for employee view)

    // Old constructor still works
    public Order(int orderId, int customerId, LocalDateTime orderDate,
                 String status, BigDecimal totalAmount) {
        this(orderId, customerId, orderDate, status, totalAmount, null);
    }

    // New constructor with customerName
    public Order(int orderId, int customerId, LocalDateTime orderDate,
                 String status, BigDecimal totalAmount, String customerName) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.customerName = customerName;
    }

    public int getOrderId() { return orderId; }
    public int getCustomerId() { return customerId; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public String getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
}
