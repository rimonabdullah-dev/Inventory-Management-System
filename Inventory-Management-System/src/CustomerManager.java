import java.util.ArrayList;
import java.util.List;

public class CustomerManager implements Manageable<Customer> {
    private final ArrayList<Customer> customers = new ArrayList<>();
    private int nextId = 1;

    @Override
    public void add(Customer item) {
        item.setId(nextId++);
        customers.add(item);
    }

    @Override
    public boolean update(int id, Customer updatedItem) {
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getId() == id) {
                updatedItem.setId(id);
                customers.set(i, updatedItem);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        return customers.removeIf(c -> c.getId() == id);
    }

    @Override
    public Customer searchById(int id) {
        for (Customer c : customers) {
            if (c.getId() == id) return c;
        }
        return null;
    }

    @Override
    public List<Customer> searchByKeyword(String keyword) {
        List<Customer> result = new ArrayList<>();
        String kw = keyword.toLowerCase();
        for (Customer c : customers) {
            if (c.getName().toLowerCase().contains(kw) || c.getEmail().toLowerCase().contains(kw)) {
                result.add(c);
            }
        }
        return result;
    }

    @Override
    public List<Customer> getAll() {
        return customers;
    }

    public void clearAll() {
        customers.clear();
        nextId = 1;
    }
}
