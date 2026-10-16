// Shared base: Validator (input checks), Manageable<T> interface, Person (abstract parent of Customer & Supplier)

import java.util.List;

// ======================================================================
// Validator
// ======================================================================
/**
 * Static utility class for validating user input before it reaches
 * any Manager. Keeping validation here (instead of copy-pasting checks
 * into every Panel) demonstrates the 'static' keyword and DRY design.
 */
public class Validator {

    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    public static boolean isValidInteger(String text) {
        if (isEmpty(text)) return false;
        try {
            Integer.parseInt(text.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidNumber(String text) {
        if (isEmpty(text)) return false;
        try {
            Double.parseDouble(text.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidPhone(String phone) {
        return !isEmpty(phone) && phone.trim().matches("[0-9+\\-\\s]{7,15}");
    }

    public static boolean isValidEmail(String email) {
        return !isEmpty(email) && email.trim().matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    }
}

// ======================================================================
// Manageable
// ======================================================================
/**
 * A generic contract every "Manager" class in this app follows.
 * ProductManager, SupplierManager, CustomerManager and SalesManager
 * all implement this with a different type T — the same interface,
 * used polymorphically across four different entities.
 */
interface Manageable<T> {
    void add(T item);
    boolean update(int id, T updatedItem);
    boolean delete(int id);
    T searchById(int id);
    List<T> searchByKeyword(String keyword);
    List<T> getAll();
}

// ======================================================================
// Person
// ======================================================================
/**
 * Abstract base class for anyone the business deals with as a "contact":
 * Supplier and Customer both extend this. Demonstrates Inheritance
 * (shared fields/behaviour) and Abstraction (getRole() has no single
 * meaning until a subclass defines it).
 */
abstract class Person {
    protected int id;
    protected String name;
    protected String phone;
    protected String address;

    public Person(int id, String name, String phone, String address) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    /** Abstract method — every subclass must say what kind of Person it is. */
    public abstract String getRole();

    @Override
    public String toString() {
        return name + " (" + getRole() + ")";
    }
}
