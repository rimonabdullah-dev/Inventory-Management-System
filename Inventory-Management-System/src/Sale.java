import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model class for a completed sale. Ties a Product and a Customer
 * together — this is the class that makes the four tabs feel like
 * one connected system rather than four separate mini-apps.
 */
public class Sale {
    private int id;
    private int productId;
    private final String productName;
    private final int customerId;
    private final String customerName;
    private int quantity;
    private final double unitPrice;
    private String paymentMethod;
    private final LocalDateTime dateTime;

    public Sale(int id, int productId, String productName, int customerId, String customerName,
                int quantity, double unitPrice, String paymentMethod) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.customerId = customerId;
        this.customerName = customerName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.paymentMethod = paymentMethod;
        this.dateTime = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public double getTotalAmount() {
        return quantity * unitPrice;
    }

    public String getFormattedDate() {
        return dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    @Override
    public String toString() {
        return String.format("Sale#%d: %d x %s -> %s", id, quantity, productName, customerName);
    }
}
