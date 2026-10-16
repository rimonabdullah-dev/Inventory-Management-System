import java.util.ArrayList;
import java.util.List;

public class SupplierManager implements Manageable<Supplier> {
    private final ArrayList<Supplier> suppliers = new ArrayList<>();
    private int nextId = 1;

    @Override
    public void add(Supplier item) {
        item.setId(nextId++);
        suppliers.add(item);
    }

    @Override
    public boolean update(int id, Supplier updatedItem) {
        for (int i = 0; i < suppliers.size(); i++) {
            if (suppliers.get(i).getId() == id) {
                updatedItem.setId(id);
                suppliers.set(i, updatedItem);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        return suppliers.removeIf(s -> s.getId() == id);
    }

    @Override
    public Supplier searchById(int id) {
        for (Supplier s : suppliers) {
            if (s.getId() == id) return s;
        }
        return null;
    }

    @Override
    public List<Supplier> searchByKeyword(String keyword) {
        List<Supplier> result = new ArrayList<>();
        String kw = keyword.toLowerCase();
        for (Supplier s : suppliers) {
            if (s.getName().toLowerCase().contains(kw)
                    || s.getCompanyName().toLowerCase().contains(kw)
                    || s.getSuppliedCategory().toLowerCase().contains(kw)) {
                result.add(s);
            }
        }
        return result;
    }

    @Override
    public List<Supplier> getAll() {
        return suppliers;
    }

    public void clearAll() {
        suppliers.clear();
        nextId = 1;
    }
}
