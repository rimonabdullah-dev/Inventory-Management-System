/**
 * Model class for Supplier. Extends Person — real Inheritance, not
 * decorative: name/phone/address come from the parent, only the
 * supplier-specific fields live here.
 */
public class Supplier extends Person {
    private String companyName;
    private String suppliedCategory;

    public Supplier(int id, String name, String phone, String address,
                    String companyName, String suppliedCategory) {
        super(id, name, phone, address);
        this.companyName = companyName;
        this.suppliedCategory = suppliedCategory;
    }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getSuppliedCategory() { return suppliedCategory; }
    public void setSuppliedCategory(String suppliedCategory) { this.suppliedCategory = suppliedCategory; }

    @Override
    public String getRole() {
        return "Supplier";
    }
}

