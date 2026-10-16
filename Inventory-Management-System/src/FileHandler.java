// Storage layer: FileHandler (CSV save/load), AdminAccount (login credentials file)

import javax.swing.*;
import java.io.*;
import java.util.List;
import java.util.Properties;

// ======================================================================
// FileHandler
// ======================================================================
/**
 * Bonus feature: save/load data to plain CSV files using JFileChooser,
 * so the app doesn't reset to empty every time it's restarted.
 * Sales history is exported for record-keeping (not reloaded, since
 * it is a historical log rather than editable master data).
 */
public class FileHandler {

    public static void saveAll(JFrame parent, ProductManager pm, SupplierManager sm,
                               CustomerManager cm, SalesManager salesManager) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a folder to save data into");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File dir = chooser.getSelectedFile();
        try {
            writeProducts(new File(dir, "products.csv"), pm.getAll());
            writeSuppliers(new File(dir, "suppliers.csv"), sm.getAll());
            writeCustomers(new File(dir, "customers.csv"), cm.getAll());
            writeSales(new File(dir, "sales_report.csv"), salesManager.getAll());
            JOptionPane.showMessageDialog(parent,
                    "Data saved successfully to:\n" + dir.getAbsolutePath(),
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent, "Error saving data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void loadAll(JFrame parent, ProductManager pm, SupplierManager sm, CustomerManager cm) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose the folder that has products.csv / suppliers.csv / customers.csv");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File dir = chooser.getSelectedFile();
        try {
            readProducts(new File(dir, "products.csv"), pm);
            readSuppliers(new File(dir, "suppliers.csv"), sm);
            readCustomers(new File(dir, "customers.csv"), cm);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent, "Error loading data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Silent version of saveAll() — no JFileChooser, no dialogs.
     * Used by Main on app close to auto-save into a fixed app-data folder.
     */
    public static void saveAllSilently(File dir, ProductManager pm, SupplierManager sm,
                                        CustomerManager cm, SalesManager salesManager) {
        try {
            if (!dir.exists()) dir.mkdirs();
            writeProducts(new File(dir, "products.csv"), pm.getAll());
            writeSuppliers(new File(dir, "suppliers.csv"), sm.getAll());
            writeCustomers(new File(dir, "customers.csv"), cm.getAll());
            writeSales(new File(dir, "sales_report.csv"), salesManager.getAll());
        } catch (IOException e) {
            System.err.println("Auto-save failed: " + e.getMessage());
        }
    }

    /**
     * Silent version of loadAll() — no JFileChooser, no dialogs.
     * Used by Main on startup to auto-load from the same fixed folder.
     * Returns true if any saved data was found and loaded.
     */
    public static boolean loadAllSilently(File dir, ProductManager pm, SupplierManager sm, CustomerManager cm) {
        File productsFile = new File(dir, "products.csv");
        if (!dir.exists() || !productsFile.exists()) return false;
        try {
            readProducts(productsFile, pm);
            readSuppliers(new File(dir, "suppliers.csv"), sm);
            readCustomers(new File(dir, "customers.csv"), cm);
            return true;
        } catch (IOException e) {
            System.err.println("Auto-load failed: " + e.getMessage());
            return false;
        }
    }

    // ---------- writers ----------

    private static void writeProducts(File file, List<Product> products) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            out.println("name,category,quantity,price");
            for (Product p : products) {
                out.printf("%s,%s,%d,%.2f%n", p.getName(), p.getCategory(), p.getQuantity(), p.getPrice());
            }
        }
    }

    private static void writeSuppliers(File file, List<Supplier> suppliers) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            out.println("name,phone,address,companyName,suppliedCategory");
            for (Supplier s : suppliers) {
                out.printf("%s,%s,%s,%s,%s%n", s.getName(), s.getPhone(), s.getAddress(),
                        s.getCompanyName(), s.getSuppliedCategory());
            }
        }
    }

    private static void writeCustomers(File file, List<Customer> customers) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            out.println("name,phone,address,email");
            for (Customer c : customers) {
                out.printf("%s,%s,%s,%s%n", c.getName(), c.getPhone(), c.getAddress(), c.getEmail());
            }
        }
    }

    private static void writeSales(File file, List<Sale> sales) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            out.println("id,product,customer,quantity,unitPrice,total,payment,date");
            for (Sale s : sales) {
                out.printf("%d,%s,%s,%d,%.2f,%.2f,%s,%s%n", s.getId(), s.getProductName(), s.getCustomerName(),
                        s.getQuantity(), s.getUnitPrice(), s.getTotalAmount(), s.getPaymentMethod(), s.getFormattedDate());
            }
        }
    }

    // ---------- readers ----------

    private static void readProducts(File file, ProductManager pm) throws IOException {
        if (!file.exists()) return;
        pm.clearAll();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] t = line.split(",");
                if (t.length < 4) continue;
                pm.add(new Product(0, t[0], t[1], Integer.parseInt(t[2]), Double.parseDouble(t[3])));
            }
        }
    }

    private static void readSuppliers(File file, SupplierManager sm) throws IOException {
        if (!file.exists()) return;
        sm.clearAll();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] t = line.split(",");
                if (t.length < 5) continue;
                sm.add(new Supplier(0, t[0], t[1], t[2], t[3], t[4]));
            }
        }
    }

    private static void readCustomers(File file, CustomerManager cm) throws IOException {
        if (!file.exists()) return;
        cm.clearAll();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] t = line.split(",");
                if (t.length < 4) continue;
                cm.add(new Customer(0, t[0], t[1], t[2], t[3]));
            }
        }
    }
}

// ======================================================================
// AdminAccount
// ======================================================================
/**
 * Simple admin account: one username/password pair, persisted to
 * data/admin.properties so a changed password survives a restart —
 * same fixed-folder pattern FileHandler already uses for CSV data.
 * This is a student Swing project, not a security system: plain-text
 * storage is fine here, the point is demonstrating a login gate.
 */
class AdminAccount {
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin123";

    private String username;
    private String password;
    private final File credentialsFile;

    public AdminAccount(File dataDir) {
        this.credentialsFile = new File(dataDir, "admin.properties");
        load();
    }

    /** Loads saved credentials if present, otherwise falls back to the defaults above. */
    private void load() {
        if (credentialsFile.exists()) {
            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(credentialsFile)) {
                props.load(in);
                username = props.getProperty("username", DEFAULT_USERNAME);
                password = props.getProperty("password", DEFAULT_PASSWORD);
                return;
            } catch (IOException e) {
                System.err.println("Could not read admin credentials, using defaults: " + e.getMessage());
            }
        }
        username = DEFAULT_USERNAME;
        password = DEFAULT_PASSWORD;
    }

    private void save() {
        Properties props = new Properties();
        props.setProperty("username", username);
        props.setProperty("password", password);
        try {
            File parent = credentialsFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            try (FileOutputStream out = new FileOutputStream(credentialsFile)) {
                props.store(out, "Inventory Management System - Admin Credentials");
            }
        } catch (IOException e) {
            System.err.println("Could not save admin credentials: " + e.getMessage());
        }
    }

    public boolean authenticate(String enteredUsername, String enteredPassword) {
        return username.equals(enteredUsername) && password.equals(enteredPassword);
    }

    public String getUsername() {
        return username;
    }

    /** Only changes and persists the password if oldPassword matches the current one. */
    public boolean changePassword(String oldPassword, String newPassword) {
        if (!password.equals(oldPassword)) return false;
        if (newPassword == null || newPassword.trim().isEmpty()) return false;
        this.password = newPassword;
        save();
        return true;
    }
}
