import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class CustomerPanel extends JPanel {

    private final CustomerManager manager;
    private CustomerTableModel tableModel;
    private JTable table;

    private JTextField nameField, phoneField, addressField, emailField, searchField;
    private int selectedId = -1;

    public CustomerPanel(CustomerManager manager) {
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
        JLabel title = new JLabel("Customer Directory");
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
        JLabel formTitle = new JLabel("Add / Update Customer");
        formTitle.setFont(Theme.FONT_HEADER);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(formTitle);
        card.add(Box.createRigidArea(new Dimension(0, 15)));

        nameField = new JTextField();
        phoneField = new JTextField();
        addressField = new JTextField();
        emailField = new JTextField();

        card.add(Theme.labeledField("Full Name", nameField, Theme.CARD));
        card.add(Theme.labeledField("Phone", phoneField, Theme.CARD));
        card.add(Theme.labeledField("Address", addressField, Theme.CARD));
        card.add(Theme.labeledField("Email", emailField, Theme.CARD));
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

        tableModel = new CustomerTableModel(manager.getAll());
        table = new JTable(tableModel);
        table.setFont(Theme.FONT_TABLE);
        table.setRowHeight(28);
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        headerRenderer.setBackground(Theme.SIDEBAR);
        headerRenderer.setForeground(Color.WHITE);
        headerRenderer.setFont(Theme.FONT_TABLE_HEADER);
        table.setSelectionBackground(new Color(210, 225, 255));
        table.setGridColor(Theme.BORDER);
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { populateFormFromSelectedRow(); }
        });

        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private void populateFormFromSelectedRow() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        Customer c = tableModel.getAt(row);
        selectedId = c.getId();
        nameField.setText(c.getName());
        phoneField.setText(c.getPhone());
        addressField.setText(c.getAddress());
        emailField.setText(c.getEmail());
    }

    private void onAdd() {
        if (!validateForm()) return;
        Customer c = new Customer(0, nameField.getText().trim(), phoneField.getText().trim(),
                addressField.getText().trim(), emailField.getText().trim());
        manager.add(c);
        refreshTable();
        clearForm();
        JOptionPane.showMessageDialog(this, "Customer added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onUpdate() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select a row from the table first!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateForm()) return;
        Customer c = new Customer(selectedId, nameField.getText().trim(), phoneField.getText().trim(),
                addressField.getText().trim(), emailField.getText().trim());
        manager.update(selectedId, c);
        refreshTable();
        clearForm();
        JOptionPane.showMessageDialog(this, "Customer updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onDelete() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Select a row from the table first!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this customer permanently?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            manager.delete(selectedId);
            refreshTable();
            clearForm();
        }
    }

    private boolean validateForm() {
        if (Validator.isEmpty(nameField.getText())) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!Validator.isValidPhone(phoneField.getText())) {
            JOptionPane.showMessageDialog(this, "Enter a valid phone number!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (Validator.isEmpty(addressField.getText())) {
            JOptionPane.showMessageDialog(this, "Address cannot be empty!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!Validator.isValidEmail(emailField.getText())) {
            JOptionPane.showMessageDialog(this, "Enter a valid email address!", "Validation Error", JOptionPane.WARNING_MESSAGE);
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
        phoneField.setText("");
        addressField.setText("");
        emailField.setText("");
        table.clearSelection();
    }

    public void refreshTable() {
        tableModel.setData(manager.getAll());
    }

    private static class CustomerTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Name", "Phone", "Address", "Email", "Loyalty Pts"};
        private List<Customer> data;

        CustomerTableModel(List<Customer> data) { this.data = data; }
        void setData(List<Customer> data) { this.data = data; fireTableDataChanged(); }
        Customer getAt(int row) { return data.get(row); }

        public int getRowCount() { return data.size(); }
        public int getColumnCount() { return columns.length; }
        public String getColumnName(int col) { return columns[col]; }

        public Object getValueAt(int row, int col) {
            Customer c = data.get(row);
            switch (col) {
                case 0: return c.getId();
                case 1: return c.getName();
                case 2: return c.getPhone();
                case 3: return c.getAddress();
                case 4: return c.getEmail();
                case 5: return c.getLoyaltyPoints();
                default: return "";
            }
        }
    }
}
