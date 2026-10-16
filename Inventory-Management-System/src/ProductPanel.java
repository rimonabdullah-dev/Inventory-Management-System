import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.table.JTableHeader;

/**
 * UI / View layer for Product. Builds the form + table, calls
 * ProductManager methods on user action. No business logic lives here.
 */
public class ProductPanel extends JPanel {

    private final ProductManager manager;
    private ProductTableModel tableModel;
    private JTable table;

    private JTextField nameField, quantityField, priceField, searchField;
    private JComboBox<String> categoryBox;
    private int selectedId = -1;

    public ProductPanel(ProductManager manager) {
        this.manager = manager;
        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildFormCard(), buildTableCard());
        split.setDividerLocation(300);
        split.setBorder(null);
        split.setBackground(Theme.BACKGROUND);
        add(split, BorderLayout.CENTER);

        refreshTable();
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND);

        JLabel title = new JLabel("Product Inventory");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_DARK);
        panel.add(title, BorderLayout.WEST);

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
        panel.add(searchPanel, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildFormCard() {
        JPanel card = Theme.cardPanel();

        JLabel formTitle = new JLabel("Add / Update Product");
        formTitle.setFont(Theme.FONT_HEADER);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(formTitle);
        card.add(Box.createRigidArea(new Dimension(0, 15)));

        nameField = new JTextField();
        categoryBox = new JComboBox<>(new String[]{"Electronics", "Stationery", "Furniture", "Grocery", "Clothing", "Other"});
        quantityField = new JTextField();
        priceField = new JTextField();

        card.add(Theme.labeledField("Product Name", nameField, Theme.CARD));
        card.add(Theme.labeledField("Category", categoryBox, Theme.CARD));
        card.add(Theme.labeledField("Quantity", quantityField, Theme.CARD));
        card.add(Theme.labeledField("Unit Price", priceField, Theme.CARD));
        card.add(Box.createRigidArea(new Dimension(0, 6)));

        JPanel buttonRow = new JPanel(new GridLayout(2, 2, 8, 8));
        buttonRow.setBackground(Theme.CARD);
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton addBtn = Theme.createButton("Add", Theme.SUCCESS);
        JButton updateBtn = Theme.createButton("Update", Theme.PRIMARY);
        JButton deleteBtn = Theme.createButton("Delete", Theme.DANGER);
        JButton clearBtn = Theme.createButton("Clear", Theme.NEUTRAL);

        addBtn.addActionListener(e -> onAdd());
        updateBtn.addActionListener(e -> onUpdate());
        deleteBtn.addActionListener(e -> onDelete());
        clearBtn.addActionListener(e -> clearForm());

        buttonRow.add(addBtn);
        buttonRow.add(updateBtn);
        buttonRow.add(deleteBtn);
        buttonRow.add(clearBtn);
        card.add(buttonRow);
        card.add(Box.createVerticalGlue());
        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.CARD);
        card.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        tableModel = new ProductTableModel(manager.getAll());
        table = new JTable(tableModel);
        table.setFont(Theme.FONT_TABLE);
        table.setRowHeight(28);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        headerRenderer.setBackground(Theme.SIDEBAR);
        headerRenderer.setForeground(Color.WHITE);
        headerRenderer.setFont(Theme.FONT_TABLE_HEADER);

        table.getTableHeader().setDefaultRenderer(headerRenderer);
        table.setSelectionBackground(new Color(210, 225, 255));
        table.setGridColor(Theme.BORDER);
        table.setDefaultRenderer(Object.class, new LowStockRenderer());
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { populateFormFromSelectedRow(); }
        });

        card.add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel hint = new JLabel("  Rows in red = low stock (below 5 units). Click a row to edit.");
        hint.setFont(Theme.FONT_LABEL);
        hint.setForeground(Theme.TEXT_MUTED);
        hint.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        card.add(hint, BorderLayout.SOUTH);
        return card;
    }

    private void populateFormFromSelectedRow() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        Product p = tableModel.getAt(row);
        selectedId = p.getId();
        nameField.setText(p.getName());
        categoryBox.setSelectedItem(p.getCategory());
        quantityField.setText(String.valueOf(p.getQuantity()));
        priceField.setText(String.valueOf(p.getPrice()));
    }

    private void onAdd() {
        if (!validateForm()) return;
        Product p = new Product(0, nameField.getText().trim(), (String) categoryBox.getSelectedItem(),
                Integer.parseInt(quantityField.getText().trim()), Double.parseDouble(priceField.getText().trim()));
        manager.add(p);
        refreshTable();
        clearForm();
        JOptionPane.showMessageDialog(this, "Product added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onUpdate() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select a row from the table first!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateForm()) return;
        Product p = new Product(selectedId, nameField.getText().trim(), (String) categoryBox.getSelectedItem(),
                Integer.parseInt(quantityField.getText().trim()), Double.parseDouble(priceField.getText().trim()));
        manager.update(selectedId, p);
        refreshTable();
        clearForm();
        JOptionPane.showMessageDialog(this, "Product updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onDelete() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select a row from the table first!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this product permanently?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            manager.delete(selectedId);
            refreshTable();
            clearForm();
        }
    }

    private boolean validateForm() {
        if (Validator.isEmpty(nameField.getText())) {
            JOptionPane.showMessageDialog(this, "Product name cannot be empty!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!Validator.isValidInteger(quantityField.getText()) || Integer.parseInt(quantityField.getText().trim()) < 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be a valid non-negative number!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!Validator.isValidNumber(priceField.getText()) || Double.parseDouble(priceField.getText().trim()) < 0) {
            JOptionPane.showMessageDialog(this, "Price must be a valid non-negative number!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void doSearch() {
        String keyword = searchField.getText().trim();
        tableModel.setData(keyword.isEmpty() ? manager.getAll() : manager.searchByKeyword(keyword));
    }

    private void clearForm() {
        selectedId = -1;
        nameField.setText("");
        categoryBox.setSelectedIndex(0);
        quantityField.setText("");
        priceField.setText("");
        table.clearSelection();
    }

    public void refreshTable() {
        tableModel.setData(manager.getAll());
    }

    /** Custom AbstractTableModel — recommended by the PDF for extra marks. */
    private static class ProductTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Name", "Category", "Quantity", "Price", "Total Value"};
        private List<Product> data;

        ProductTableModel(List<Product> data) { this.data = data; }
        void setData(List<Product> data) { this.data = data; fireTableDataChanged(); }
        Product getAt(int row) { return data.get(row); }

        public int getRowCount() { return data.size(); }
        public int getColumnCount() { return columns.length; }
        public String getColumnName(int col) { return columns[col]; }

        public Object getValueAt(int row, int col) {
            Product p = data.get(row);
            switch (col) {
                case 0: return p.getId();
                case 1: return p.getName();
                case 2: return p.getCategory();
                case 3: return p.getQuantity();
                case 4: return String.format("%.2f", p.getPrice());
                case 5: return String.format("%.2f", p.getTotalValue());
                default: return "";
            }
        }
    }

    /** Highlights low-stock rows in red — a nice touch for the demo. */
    private class LowStockRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (!isSelected) {
                Product p = tableModel.getAt(row);
                c.setBackground(p.isLowStock() ? Theme.LOW_STOCK : Color.WHITE);
            }
            return c;
        }
    }
}
