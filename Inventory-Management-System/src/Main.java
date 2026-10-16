//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * Entry point. Builds the JFrame and JTabbedPane only — kept small,
 * exactly as the project guide's Separation of Concerns table asks for.
 */
public class Main {
    /** Fixed folder the app auto-saves to / auto-loads from — no dialogs needed. */
    private static final File DATA_DIR = new File("data");

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        SwingUtilities.invokeLater(Main::createAndShowGui);
    }

    private static void createAndShowGui() {
        AdminAccount adminAccount = new AdminAccount(DATA_DIR);

        LoginDialog loginDialog = new LoginDialog(adminAccount);
        loginDialog.setVisible(true); // blocks here until login succeeds or app exits
        if (!loginDialog.isAuthenticated()) {
            System.exit(0);
        }

        JFrame frame = new JFrame("Inventory Management System");
        frame.setSize(1150, 720);
        frame.setMinimumSize(new Dimension(980, 620));
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // Shared manager instances so every tab sees the same data
        ProductManager productManager = new ProductManager();
        SupplierManager supplierManager = new SupplierManager();
        CustomerManager customerManager = new CustomerManager();
        SalesManager salesManager = new SalesManager(productManager, customerManager);

        // Auto-load: if data/products.csv etc. exist from a previous run, use them.
        // Only fall back to sample data on a genuinely first run.
        boolean loaded = FileHandler.loadAllSilently(DATA_DIR, productManager, supplierManager, customerManager);
        if (!loaded) {
            seedSampleData(productManager, supplierManager, customerManager);
        }

        frame.add(buildHeader(adminAccount), BorderLayout.NORTH);

        ProductPanel productPanel = new ProductPanel(productManager);
        SupplierPanel supplierPanel = new SupplierPanel(supplierManager);
        CustomerPanel customerPanel = new CustomerPanel(customerManager);
        SalesPanel salesPanel = new SalesPanel(salesManager, productManager, customerManager);
        DashboardPanel dashboardPanel = new DashboardPanel(productManager, supplierManager, customerManager, salesManager);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.FONT_HEADER);
        tabs.addTab("  Dashboard  ", dashboardPanel);
        tabs.addTab("  Products  ", productPanel);
        tabs.addTab("  Suppliers  ", supplierPanel);
        tabs.addTab("  Customers  ", customerPanel);
        tabs.addTab("  Sales  ", salesPanel);

        // Sales tab needs fresh Product/Customer lists every time it's opened;
        // Products/Customers tabs need fresh data too, since deleting a Sale
        // reverses stock and loyalty points on those records; Dashboard
        // needs fresh totals every time it's opened.
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedComponent() == salesPanel) {
                salesPanel.refreshCombos();
                productPanel.refreshTable();
            } else if (tabs.getSelectedComponent() == dashboardPanel) {
                dashboardPanel.refresh();
            } else if (tabs.getSelectedComponent() == productPanel) {
                productPanel.refreshTable();
            } else if (tabs.getSelectedComponent() == customerPanel) {
                customerPanel.refreshTable();
            }
        });

        frame.add(tabs, BorderLayout.CENTER);
        frame.setJMenuBar(buildMenuBar(frame, productManager, supplierManager, customerManager, salesManager,
                productPanel, supplierPanel, customerPanel, salesPanel, adminAccount));

        // Auto-save: whatever is in memory gets written to DATA_DIR right before the
        // window closes, so the next launch's auto-load picks it back up.
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                FileHandler.saveAllSilently(DATA_DIR, productManager, supplierManager, customerManager, salesManager);
                frame.dispose();
                System.exit(0);
            }
        });

        frame.setVisible(true);
    }

    private static JPanel buildHeader(AdminAccount adminAccount) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.SIDEBAR);
        header.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JLabel title = new JLabel("Inventory Management System");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("Logged in as: " + adminAccount.getUsername());
        subtitle.setFont(Theme.FONT_LABEL);
        subtitle.setForeground(new Color(200, 205, 215));
        header.add(subtitle, BorderLayout.EAST);
        return header;
    }

    private static JMenuBar buildMenuBar(JFrame frame, ProductManager pm, SupplierManager sm, CustomerManager cm,
                                         SalesManager salesManager, ProductPanel pp, SupplierPanel sp,
                                         CustomerPanel cp, SalesPanel salp, AdminAccount adminAccount) {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem saveItem = new JMenuItem("Save Data...");
        JMenuItem loadItem = new JMenuItem("Load Data...");
        JMenuItem exitItem = new JMenuItem("Exit");

        saveItem.addActionListener(e -> FileHandler.saveAll(frame, pm, sm, cm, salesManager));
        loadItem.addActionListener(e -> {
            FileHandler.loadAll(frame, pm, sm, cm);
            pp.refreshTable();
            sp.refreshTable();
            cp.refreshTable();
            salp.refreshCombos();
            salp.refreshTable();
            JOptionPane.showMessageDialog(frame, "Data loaded successfully!", "Loaded", JOptionPane.INFORMATION_MESSAGE);
        });
        // Route through the same windowClosing handler so Exit also auto-saves
        exitItem.addActionListener(e -> frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));

        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "Inventory Management System\nJava Swing Group Project\nBuilt with JFrame, JTabbedPane, JTable & more.",
                "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        JMenu adminMenu = new JMenu("Admin");
        JMenuItem changePasswordItem = new JMenuItem("Change Password...");
        JMenuItem logoutItem = new JMenuItem("Logout");

        changePasswordItem.addActionListener(e -> {
            JPasswordField oldPass = new JPasswordField();
            JPasswordField newPass = new JPasswordField();
            JPasswordField confirmPass = new JPasswordField();
            JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
            panel.add(new JLabel("Current Password:")); panel.add(oldPass);
            panel.add(new JLabel("New Password:"));     panel.add(newPass);
            panel.add(new JLabel("Confirm New Password:")); panel.add(confirmPass);

            int result = JOptionPane.showConfirmDialog(frame, panel, "Change Admin Password",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) return;

            String oldP = new String(oldPass.getPassword());
            String newP = new String(newPass.getPassword());
            String confirmP = new String(confirmPass.getPassword());

            if (!newP.equals(confirmP)) {
                JOptionPane.showMessageDialog(frame, "New passwords do not match.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } else if (adminAccount.changePassword(oldP, newP)) {
                JOptionPane.showMessageDialog(frame, "Password changed successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Current password is incorrect, or new password is empty.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        logoutItem.addActionListener(e -> {
            // Auto-save current data before locking the app, same as a normal close
            FileHandler.saveAllSilently(DATA_DIR, pm, sm, cm, salesManager);
            frame.setVisible(false);
            LoginDialog loginDialog = new LoginDialog(adminAccount);
            loginDialog.setVisible(true);
            if (loginDialog.isAuthenticated()) {
                frame.setVisible(true);
            } else {
                System.exit(0);
            }
        });

        adminMenu.add(changePasswordItem);
        adminMenu.add(logoutItem);

        menuBar.add(fileMenu);
        menuBar.add(adminMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    /** A little starter data so a live demo never opens to an empty screen. */
    private static void seedSampleData(ProductManager pm, SupplierManager sm, CustomerManager cm) {
        pm.add(new Product(0, "Wireless Mouse", "Electronics", 25, 12.50));
        pm.add(new Product(0, "Notebook", "Stationery", 100, 1.20));
        pm.add(new Product(0, "Office Chair", "Furniture", 4, 85.00));
        pm.add(new Product(0, "USB Cable", "Electronics", 3, 3.75));

        sm.add(new Supplier(0, "Karim Hossain", "01711111111", "Dhaka", "Karim Traders Ltd.", "Electronics"));
        sm.add(new Supplier(0, "Rafi Ahmed", "01822222222", "Chattogram", "Rafi & Co.", "Stationery"));

        cm.add(new Customer(0, "Anisul Islam", "01933333333", "Dhaka", "anisul@mail.com"));
        cm.add(new Customer(0, "Farhana Akter", "01944444444", "Sylhet", "farhana@mail.com"));
    }
}
