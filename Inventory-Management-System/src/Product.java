/**
 * Model class — stores product data only. No logic, no UI
 * (Separation of Concerns, PDF section 6).
 */
public class Product {
    private static final int LOW_STOCK_THRESHOLD = 5;

    private int id;
    private String name;
    private String category;
    private int quantity;
    private double price;

    public Product(int id, String name, String category, int quantity, double price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public boolean isLowStock() {
        return quantity < LOW_STOCK_THRESHOLD;
    }

    public double getTotalValue() {
        return quantity * price;
    }

    @Override
    public String toString() {
        return name + " (Stock: " + quantity + ")";
    }
}
