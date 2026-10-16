// UI layer: Theme (colors/fonts/helpers), LoginDialog, DashboardPanel

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

// ======================================================================
// Theme
// ======================================================================
/**
 * Central place for colors, fonts and small UI helpers.
 * Every Panel uses this class so the whole app looks consistent —
 * this is what gives the project a good "first impression".
 */
public class Theme {
    public static final Color PRIMARY    = new Color(41, 98, 255);
    public static final Color SIDEBAR    = new Color(28, 32, 43);
    public static final Color BACKGROUND = new Color(244, 246, 250);
    public static final Color CARD       = Color.WHITE;
    public static final Color SUCCESS    = new Color(34, 173, 91);
    public static final Color DANGER     = new Color(220, 53, 69);
    public static final Color WARNING    = new Color(255, 159, 28);
    public static final Color NEUTRAL    = new Color(108, 117, 125);
    public static final Color TEXT_DARK  = new Color(33, 37, 41);
    public static final Color TEXT_MUTED = new Color(108, 117, 125);
    public static final Color BORDER     = new Color(222, 226, 230);
    public static final Color LOW_STOCK  = new Color(255, 231, 231);

    public static final Font FONT_TITLE        = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADER       = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_LABEL        = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BUTTON       = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TABLE        = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_TABLE_HEADER = new Font("Segoe UI", Font.BOLD, 13);

    /** Creates a flat, colored button with consistent styling used everywhere. */
    public static JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Wraps a label above a field, used to build clean vertical forms. */
    public static JPanel labeledField(String label, JComponent field, Color cardColor) {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBackground(cardColor);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        wrap.add(lbl);
        wrap.add(Box.createRigidArea(new Dimension(0, 4)));
        wrap.add(field);
        wrap.add(Box.createRigidArea(new Dimension(0, 10)));
        return wrap;
    }

    /** Standard "form card" container used on the left side of every tab. */
    public static JPanel cardPanel() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        return card;
    }
}

// ======================================================================
// LoginDialog
// ======================================================================
/**
 * Modal login gate shown before the main application window.
 * Blocks access to the app until valid admin credentials are entered,
 * the attempt limit is hit, or the user exits.
 */
class LoginDialog extends JDialog {
    private static final int MAX_ATTEMPTS = 3;

    private final AdminAccount adminAccount;
    private boolean authenticated = false;
    private int attemptsLeft = MAX_ATTEMPTS;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;

    public LoginDialog(AdminAccount adminAccount) {
        super((Frame) null, "Admin Login", true);
        this.adminAccount = adminAccount;
        buildUI();
    }

    private void buildUI() {
        // No default close action — Exit button / max attempts are the only ways out
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(Theme.BACKGROUND);
        setLayout(new BorderLayout());

        JPanel card = Theme.cardPanel();
        card.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        JLabel title = new JLabel("Inventory Management System");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Admin sign-in required");
        subtitle.setFont(Theme.FONT_LABEL);
        subtitle.setForeground(Theme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(subtitle);
        card.add(Box.createRigidArea(new Dimension(0, 20)));

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        card.add(Theme.labeledField("Username", usernameField, Theme.CARD));
        card.add(Theme.labeledField("Password", passwordField, Theme.CARD));

        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.FONT_LABEL);
        errorLabel.setForeground(Theme.DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(errorLabel);
        card.add(Box.createRigidArea(new Dimension(0, 6)));

        JButton loginBtn = Theme.createButton("Login", Theme.PRIMARY);
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> attemptLogin());
        card.add(loginBtn);
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        JButton exitBtn = Theme.createButton("Exit", Theme.NEUTRAL);
        exitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        exitBtn.addActionListener(e -> System.exit(0));
        card.add(exitBtn);

        // Enter key submits from either field
        KeyAdapter enterToLogin = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) attemptLogin();
            }
        };
        usernameField.addKeyListener(enterToLogin);
        passwordField.addKeyListener(enterToLogin);

        add(card, BorderLayout.CENTER);
        pack();
        setSize(380, getHeight());
        setLocationRelativeTo(null);
    }

    private void attemptLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (adminAccount.authenticate(user, pass)) {
            authenticated = true;
            dispose();
            return;
        }

        attemptsLeft--;
        if (attemptsLeft <= 0) {
            JOptionPane.showMessageDialog(this, "Too many failed attempts. Closing application.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        errorLabel.setText("Invalid credentials — " + attemptsLeft + " attempt(s) left.");
        passwordField.setText("");
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}

// ======================================================================
// DashboardPanel
// ======================================================================
/**
 * UI / View layer for the Dashboard tab. Pure read-only summary —
 * pulls numbers already computed by the existing Managers
 * (ProductManager.getTotalInventoryValue(), SalesManager.getTotalRevenue(), etc.)
 * and lays them out as stat cards. No new business logic lives here.
 */
class DashboardPanel extends JPanel {

    private final ProductManager productManager;
    private final SupplierManager supplierManager;
    private final CustomerManager customerManager;
    private final SalesManager salesManager;

    private JLabel inventoryValueLabel;
    private JLabel revenueLabel;
    private JLabel lowStockLabel;
    private JLabel productCountLabel;
    private JLabel customerCountLabel;
    private JLabel supplierCountLabel;

    public DashboardPanel(ProductManager productManager, SupplierManager supplierManager,
                           CustomerManager customerManager, SalesManager salesManager) {
        this.productManager = productManager;
        this.supplierManager = supplierManager;
        this.customerManager = customerManager;
        this.salesManager = salesManager;

        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildStatGrid(), BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND);
        JLabel title = new JLabel("Dashboard");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);
        panel.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("Live snapshot of the whole system");
        subtitle.setFont(Theme.FONT_LABEL);
        subtitle.setForeground(Theme.TEXT_MUTED);
        panel.add(subtitle, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildStatGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 3, 16, 16));
        grid.setBackground(Theme.BACKGROUND);

        inventoryValueLabel = new JLabel();
        revenueLabel = new JLabel();
        lowStockLabel = new JLabel();
        productCountLabel = new JLabel();
        customerCountLabel = new JLabel();
        supplierCountLabel = new JLabel();

        grid.add(statCard("Total Inventory Value", inventoryValueLabel, Theme.PRIMARY));
        grid.add(statCard("Total Revenue", revenueLabel, Theme.SUCCESS));
        grid.add(statCard("Low Stock Items", lowStockLabel, Theme.DANGER));
        grid.add(statCard("Products", productCountLabel, Theme.NEUTRAL));
        grid.add(statCard("Customers", customerCountLabel, Theme.NEUTRAL));
        grid.add(statCard("Suppliers", supplierCountLabel, Theme.NEUTRAL));

        return grid;
    }

    /** One rounded "stat card": a muted title on top, a big colored number below. */
    private JPanel statCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = Theme.cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.FONT_LABEL);
        titleLabel.setForeground(Theme.TEXT_MUTED);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valueLabel.setForeground(accent);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(valueLabel);
        card.add(Box.createVerticalGlue());
        return card;
    }

    /** Re-pulls every number from the Managers. Call whenever the tab is opened. */
    public void refresh() {
        inventoryValueLabel.setText(String.format("%.2f", productManager.getTotalInventoryValue()));
        revenueLabel.setText(String.format("%.2f", salesManager.getTotalRevenue()));
        lowStockLabel.setText(String.valueOf(productManager.getLowStockCount()));
        productCountLabel.setText(String.valueOf(productManager.getAll().size()));
        customerCountLabel.setText(String.valueOf(customerManager.getAll().size()));
        supplierCountLabel.setText(String.valueOf(supplierManager.getAll().size()));
    }
}
