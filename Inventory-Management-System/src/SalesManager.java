import java.util.ArrayList;
import java.util.List;

/**
 * Manager/Logic layer for Sale. This is the one manager that depends on
 * two others (ProductManager, CustomerManager) — a good example to trace
 * end-to-end in the viva: recordSale() validates, creates the Sale,
 * reduces stock, and rewards the customer, all in one place.
 */
public class SalesManager implements Manageable<Sale> {
    private final ArrayList<Sale> sales = new ArrayList<>();
    private int nextId = 1;

    private final ProductManager productManager;
    private final CustomerManager customerManager;

    public SalesManager(ProductManager productManager, CustomerManager customerManager) {
        this.productManager = productManager;
        this.customerManager = customerManager;
    }

    /** The real business operation — not just a plain add(). */
    public Sale recordSale(int productId, int customerId, int quantity, String paymentMethod) {
        Product product = productManager.searchById(productId);
        Customer customer = customerManager.searchById(customerId);

        if (product == null) throw new IllegalArgumentException("Product not found!");
        if (customer == null) throw new IllegalArgumentException("Customer not found!");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than zero!");
        if (product.getQuantity() < quantity)
            throw new IllegalArgumentException("Not enough stock! Only " + product.getQuantity() + " left.");

        Sale sale = new Sale(nextId++, product.getId(), product.getName(),
                customer.getId(), customer.getName(), quantity, product.getPrice(), paymentMethod);
        sales.add(sale);

        productManager.reduceStock(productId, quantity);
        customer.addLoyaltyPoints(quantity);

        return sale;
    }

    @Override
    public void add(Sale item) {
        item.setId(nextId++);
        sales.add(item);
    }

    @Override
    public boolean update(int id, Sale updatedItem) {
        for (int i = 0; i < sales.size(); i++) {
            if (sales.get(i).getId() == id) {
                updatedItem.setId(id);
                sales.set(i, updatedItem);
                return true;
            }
        }
        return false;
    }

    /**
     * Proper Update for the Sales tab: changes a sale's quantity/payment
     * method and adjusts product stock + customer loyalty points by the
     * difference, instead of just overwriting the record in place.
     * Throws IllegalArgumentException if the new quantity exceeds available stock.
     */
    public void updateSale(int saleId, int newQuantity, String newPaymentMethod) {
        Sale sale = searchById(saleId);
        if (sale == null) throw new IllegalArgumentException("Sale not found!");
        if (newQuantity <= 0) throw new IllegalArgumentException("Quantity must be greater than zero!");

        int delta = newQuantity - sale.getQuantity(); // positive = selling more, negative = selling less
        Product product = productManager.searchById(sale.getProductId());
        if (product != null) {
            if (delta > 0 && product.getQuantity() < delta) {
                throw new IllegalArgumentException("Not enough stock! Only " + product.getQuantity() + " left.");
            }
            product.setQuantity(product.getQuantity() - delta);
        }
        Customer customer = customerManager.searchById(sale.getCustomerId());
        if (customer != null) {
            customer.addLoyaltyPoints(delta);
        }

        sale.setQuantity(newQuantity);
        sale.setPaymentMethod(newPaymentMethod);
    }

    @Override
    public boolean delete(int id) {
        return sales.removeIf(s -> s.getId() == id);
    }

    /**
     * Deletes a sale AND reverses its side effects — restores the product's
     * stock and removes the loyalty points that were awarded — so deleting
     * a record doesn't leave inventory/loyalty data inconsistent. This is
     * what the Sales tab's Delete button calls, not the plain delete() above.
     */
    public boolean deleteSale(int saleId) {
        Sale sale = searchById(saleId);
        if (sale == null) return false;

        Product product = productManager.searchById(sale.getProductId());
        if (product != null) {
            product.setQuantity(product.getQuantity() + sale.getQuantity());
        }
        Customer customer = customerManager.searchById(sale.getCustomerId());
        if (customer != null) {
            customer.addLoyaltyPoints(-sale.getQuantity());
        }
        return sales.remove(sale);
    }

    @Override
    public Sale searchById(int id) {
        for (Sale s : sales) {
            if (s.getId() == id) return s;
        }
        return null;
    }

    @Override
    public List<Sale> searchByKeyword(String keyword) {
        List<Sale> result = new ArrayList<>();
        String kw = keyword.toLowerCase();
        for (Sale s : sales) {
            if (s.getProductName().toLowerCase().contains(kw) || s.getCustomerName().toLowerCase().contains(kw)) {
                result.add(s);
            }
        }
        return result;
    }

    @Override
    public List<Sale> getAll() {
        return sales;
    }

    public void clearAll() {
        sales.clear();
        nextId = 1;
    }

    public double getTotalRevenue() {
        double total = 0;
        for (Sale s : sales) total += s.getTotalAmount();
        return total;
    }
}

