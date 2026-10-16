import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * UI / View layer for Sales. This tab reads from ProductManager and
 * CustomerManager (via combo boxes) and calls SalesManager.recordSale(),
 * which is where the cross-manager logic (reduce stock, add loyalty
 * points) actually happens.
 */
public class SalesPanel extends JPanel {

    private final SalesManager salesManager;
    private final ProductManager productManager;
    private final CustomerManager customerManager;

    private SalesTableModel tableModel;
    private JTable table;
    private JComboBox<Product> productBox;
    private JComboBox<Customer> customerBox;
    private JTextField quantityField;
    private JTextField searchField;
    private JRadioButton cashRadio, cardRadio;
    private JLabel revenueLabel;
    private JButton sellBtn, updateBtn;
    private int selectedSaleId = -1;

    public SalesPanel(SalesManager salesManager, ProductManager productManager, CustomerManager customerManager) {
        this.salesManager = salesManager;
        this.productManager = productManager;
        this.customerManager = customerManager;

        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildHeader(), BorderLayout.NORTH);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildFormCard(), buildTableCard());
        split.setDividerLocation(320);
        split.setBorder(null);
        split.setBackground(Theme.BACKGROUND);
        add(split, BorderLayout.CENTER);

        refreshCombos();
        refreshTable();
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND);
        JLabel title = new JLabel("Sales / Transactions");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);
        panel.add(title, BorderLayout.WEST);

        revenueLabel = new JLabel();
        revenueLabel.setFont(Theme.FONT_HEADER);
        revenueLabel.setForeground(Theme.SUCCESS);
        panel.add(revenueLabel, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildFormCard() {
        JPanel card = Theme.cardPanel();
        JLabel formTitle = new JLabel("Record a Sale");
        formTitle.setFont(Theme.FONT_HEADER);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(formTitle);
        card.add(Box.createRigidArea(new Dimension(0, 15)));

        productBox = new JComboBox<>();
        customerBox = new JComboBox<>();
        quantityField = new JTextField();

        card.add(Theme.labeledField("Product", productBox, Theme.CARD));
        card.add(Theme.labeledField("Customer", customerBox, Theme.CARD));
        card.add(Theme.labeledField("Quantity", quantityField, Theme.CARD));

        JLabel paymentLabel = new JLabel("Payment Method");
        paymentLabel.setFont(Theme.FONT_LABEL);
        paymentLabel.setForeground(Theme.TEXT_MUTED);
        paymentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(paymentLabel);
        card.add(Box.createRigidArea(new Dimension(0, 4)));

        cashRadio = new JRadioButton("Cash", true);
        cardRadio = new JRadioButton("Card");
        cashRadio.setBackground(Theme.CARD);
        cardRadio.setBackground(Theme.CARD);
        ButtonGroup group = new ButtonGroup();
        group.add(cashRadio);
        group.add(cardRadio);
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        radioPanel.setBackground(Theme.CARD);
        radioPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        radioPanel.add(cashRadio);
        radioPanel.add(cardRadio);
        card.add(radioPanel);
        card.add(Box.createRigidArea(new Dimension(0, 14)));

        JButton refreshBtn = Theme.createButton("Refresh Lists", Theme.NEUTRAL);
        refreshBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        refreshBtn.addActionListener(e -> refreshCombos());
        card.add(refreshBtn);
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        sellBtn = Theme.createButton("Record Sale", Theme.SUCCESS);
        sellBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        sellBtn.addActionListener(e -> onRecordSale());
        card.add(sellBtn);
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        updateBtn = Theme.createButton("Update Selected (Qty/Payment)", Theme.PRIMARY);
        updateBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        updateBtn.setEnabled(false);
        updateBtn.addActionListener(e -> onUpdateSale());
        card.add(updateBtn);

        card.add(Box.createVerticalGlue());
        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.CARD);
        card.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setBackground(Theme.BACKGROUND);
        searchField = new JTextField(18);
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { doSearch(); }
            public void removeUpdate(DocumentEvent e) { doSearch(); }
            public void changedUpdate(DocumentEvent e) { doSearch(); }
        });
        card.add(searchPanel, BorderLayout.NORTH);

        tableModel = new SalesTableModel(salesManager.getAll());
        table = new JTable(tableModel);
        table.setFont(Theme.FONT_TABLE);
        table.setRowHeight(28);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        headerRenderer.setBackground(Theme.SIDEBAR);
        headerRenderer.setForeground(Color.WHITE);
        headerRenderer.setFont(Theme.FONT_TABLE_HEADER);
        table.setSelectionBackground(new Color(210, 225, 255));
        table.setGridColor(Theme.BORDER);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRowSelected();
        });

        card.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        footer.setBackground(Theme.CARD);
        JButton deleteBtn = Theme.createButton("Delete Selected", Theme.DANGER);
        deleteBtn.addActionListener(e -> onDeleteSale());
        footer.add(deleteBtn);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    private void doSearch() {
        String keyword = searchField.getText().trim();
        List<Sale> results = keyword.isEmpty() ? salesManager.getAll() : salesManager.searchByKeyword(keyword);
        tableModel.setData(results);
    }

    private void onDeleteSale() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a sale record to delete first!",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int saleId = tableModel.getIdAt(row);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this sale record? This will restore the product's stock and reverse the customer's loyalty points.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        if (salesManager.deleteSale(saleId)) {
            selectedSaleId = -1;
            clearFormToAddMode();
            refreshTable();
            JOptionPane.showMessageDialog(this, "Sale record deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /** Selecting a row switches the form into "edit this sale" mode. */
    private void onRowSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            selectedSaleId = -1;
            clearFormToAddMode();
            return;
        }
        Sale sale = salesManager.searchById(tableModel.getIdAt(row));
        if (sale == null) return;

        selectedSaleId = sale.getId();
        quantityField.setText(String.valueOf(sale.getQuantity()));
        if ("Card".equals(sale.getPaymentMethod())) {
            cardRadio.setSelected(true);
        } else {
            cashRadio.setSelected(true);
        }
        sellBtn.setEnabled(false);   // Recording a new sale isn't valid while editing an old one
        updateBtn.setEnabled(true);
    }

    private void clearFormToAddMode() {
        quantityField.setText("");
        cashRadio.setSelected(true);
        sellBtn.setEnabled(true);
        updateBtn.setEnabled(false);
        table.clearSelection();
    }

    private void onUpdateSale() {
        if (selectedSaleId == -1) return;
        if (!Validator.isValidInteger(quantityField.getText()) || Integer.parseInt(quantityField.getText().trim()) <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be a positive whole number!",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int newQuantity = Integer.parseInt(quantityField.getText().trim());
        String newPayment = cashRadio.isSelected() ? "Cash" : "Card";

        try {
            salesManager.updateSale(selectedSaleId, newQuantity, newPayment);
            selectedSaleId = -1;
            clearFormToAddMode();
            refreshTable();
            refreshCombos();
            JOptionPane.showMessageDialog(this, "Sale record updated.", "Updated", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Update Sale", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Call this whenever Products/Customers may have changed (e.g. tab switch). */
    public void refreshCombos() {
        Product selectedProduct = (Product) productBox.getSelectedItem();
        Customer selectedCustomer = (Customer) customerBox.getSelectedItem();

        productBox.removeAllItems();
        for (Product p : productManager.getAll()) productBox.addItem(p);

        customerBox.removeAllItems();
        for (Customer c : customerManager.getAll()) customerBox.addItem(c);

        if (selectedProduct != null) productBox.setSelectedItem(selectedProduct);
        if (selectedCustomer != null) customerBox.setSelectedItem(selectedCustomer);
    }

    private void onRecordSale() {
        Product product = (Product) productBox.getSelectedItem();
        Customer customer = (Customer) customerBox.getSelectedItem();

        if (product == null || customer == null) {
            JOptionPane.showMessageDialog(this, "Add at least one Product and one Customer first!",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!Validator.isValidInteger(quantityField.getText()) || Integer.parseInt(quantityField.getText().trim()) <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be a positive whole number!",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantity = Integer.parseInt(quantityField.getText().trim());
        String payment = cashRadio.isSelected() ? "Cash" : "Card";

        try {
            salesManager.recordSale(product.getId(), customer.getId(), quantity, payment);
            refreshTable();
            refreshCombos();
            quantityField.setText("");
            JOptionPane.showMessageDialog(this, "Sale recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Complete Sale", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshTable() {
        String keyword = searchField != null ? searchField.getText().trim() : "";
        List<Sale> data = keyword.isEmpty() ? salesManager.getAll() : salesManager.searchByKeyword(keyword);
        tableModel.setData(data);
        revenueLabel.setText(String.format("Total Revenue: %.2f", salesManager.getTotalRevenue()));
    }

    private static class SalesTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Product", "Customer", "Qty", "Unit Price", "Total", "Payment", "Date"};
        private List<Sale> data;

        SalesTableModel(List<Sale> data) { this.data = data; }
        void setData(List<Sale> data) { this.data = data; fireTableDataChanged(); }
        int getIdAt(int row) { return data.get(row).getId(); }

        public int getRowCount() { return data.size(); }
        public int getColumnCount() { return columns.length; }
        public String getColumnName(int col) { return columns[col]; }

        public Object getValueAt(int row, int col) {
            Sale s = data.get(row);
            switch (col) {
                case 0: return s.getId();
                case 1: return s.getProductName();
                case 2: return s.getCustomerName();
                case 3: return s.getQuantity();
                case 4: return String.format("%.2f", s.getUnitPrice());
                case 5: return String.format("%.2f", s.getTotalAmount());
                case 6: return s.getPaymentMethod();
                case 7: return s.getFormattedDate();
                default: return "";
            }
        }
    }
}

