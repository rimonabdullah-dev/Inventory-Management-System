import java.util.ArrayList;
import java.util.List;

/**
 * Manager/Logic layer for Product. Holds the ArrayList of products and
 * performs Add/Update/Delete/Search — never touches Swing.
 */
public class ProductManager implements Manageable<Product> {
    private final ArrayList<Product> products = new ArrayList<>();
    private int nextId = 1;

    @Override
    public void add(Product item) {
        item.setId(nextId++);
        products.add(item);
    }

    @Override
    public boolean update(int id, Product updatedItem) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId() == id) {
                updatedItem.setId(id);
                products.set(i, updatedItem);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        return products.removeIf(p -> p.getId() == id);
    }

    @Override
    public Product searchById(int id) {
        for (Product p : products) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    @Override
    public List<Product> searchByKeyword(String keyword) {
        List<Product> result = new ArrayList<>();
        String kw = keyword.toLowerCase();
        for (Product p : products) {
            if (p.getName().toLowerCase().contains(kw) || p.getCategory().toLowerCase().contains(kw)) {
                result.add(p);
            }
        }
        return result;
    }

    @Override
    public List<Product> getAll() {
        return products;
    }

    public void clearAll() {
        products.clear();
        nextId = 1;
    }

    /** Called by SalesManager when a sale is recorded. */
    public void reduceStock(int productId, int amount) {
        Product p = searchById(productId);
        if (p != null) {
            p.setQuantity(p.getQuantity() - amount);
        }
    }

    public double getTotalInventoryValue() {
        double total = 0;
        for (Product p : products) total += p.getTotalValue();
        return total;
    }

    /** Used by the Dashboard tab to flag how many items need restocking. */
    public int getLowStockCount() {
        int count = 0;
        for (Product p : products) if (p.isLowStock()) count++;
        return count;
    }
}

