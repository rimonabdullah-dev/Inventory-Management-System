/**
 * Model class for Customer. Extends Person, same as Supplier —
 * two different subclasses sharing one parent (Inheritance),
 * each giving getRole() its own meaning (Polymorphism/Abstraction).
 */
public class Customer extends Person {
    private String email;
    private int loyaltyPoints;

    public Customer(int id, String name, String phone, String address, String email) {
        super(id, name, phone, address);
        this.email = email;
        this.loyaltyPoints = 0;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void addLoyaltyPoints(int points) { this.loyaltyPoints += points; }

    @Override
    public String getRole() {
        return "Customer";
    }
}

