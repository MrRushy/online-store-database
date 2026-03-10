import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

 class ManageItemsFrame extends JFrame {

    private JTable itemsTable;
    private JButton addButton;
    private JButton editButton;
    private JButton toggleActiveButton;
    private JButton closeButton;

    public ManageItemsFrame() {
        initComponents();
        loadItems();
        hookListeners();
    }

    private void initComponents() {
        setTitle("Manage Items");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(mainPanel);

        // ----------- TABLE ----------- //
        itemsTable = new JTable();
        itemsTable.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Name", "Description", "Price", "Stock", "Active"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) return Boolean.class; // Active column is checkbox
                return String.class;
            }
        });

        JScrollPane scrollPane = new JScrollPane(itemsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Items"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // ----------- BUTTONS ----------- //
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButton = new JButton("Add Item");
        editButton = new JButton("Edit Selected");
        toggleActiveButton = new JButton("Toggle Active");
        closeButton = new JButton("Close");

        bottomPanel.add(addButton);
        bottomPanel.add(editButton);
        bottomPanel.add(toggleActiveButton);
        bottomPanel.add(closeButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setSize(900, 450);
        setLocationRelativeTo(null);
    }

    private void hookListeners() {

        closeButton.addActionListener(e -> dispose());

        addButton.addActionListener(e -> showAddDialog());

        editButton.addActionListener(e -> showEditDialog());

        toggleActiveButton.addActionListener(e -> toggleActive());
    }

    // --------------------------------------------
    // LOAD ALL ITEMS INTO TABLE
    // --------------------------------------------
    private void loadItems() {
        List<Item> items = DBHelper.getAllItems();
        DefaultTableModel model = (DefaultTableModel) itemsTable.getModel();
        model.setRowCount(0);

        for (Item it : items) {
            Object[] row = new Object[]{
                    it.getItemId(),
                    it.getItemName(),
                    it.getDescription(),
                    it.getPrice(),
                    it.getQuantityInStock(),
                    it.isActive()
            };
            model.addRow(row);
        }
    }

    // --------------------------------------------
    // ADD NEW ITEM
    // --------------------------------------------
    private void showAddDialog() {
        JTextField nameField = new JTextField(20);
        JTextField descField = new JTextField(20);
        JTextField priceField = new JTextField(10);
        JTextField stockField = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        panel.add(new JLabel("Price:"));
        panel.add(priceField);
        panel.add(new JLabel("Stock:"));
        panel.add(stockField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add Item",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                Item item = new Item();
                item.setItemName(nameField.getText().trim());
                item.setDescription(descField.getText().trim());
                item.setPrice(new BigDecimal(priceField.getText().trim()));
                item.setQuantityInStock(Integer.parseInt(stockField.getText().trim()));
                item.setActive(true);

                if (DBHelper.insertItem(item)) {
                    loadItems();
                    JOptionPane.showMessageDialog(this, "Item added.");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add item.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --------------------------------------------
    // EDIT SELECTED ITEM
    // --------------------------------------------
    private void showEditDialog() {
        int row = itemsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an item first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DefaultTableModel model = (DefaultTableModel) itemsTable.getModel();

        int itemId = (int) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);
        String desc = (String) model.getValueAt(row, 2);
        String price = model.getValueAt(row, 3).toString();
        String stock = model.getValueAt(row, 4).toString();

        JTextField nameField = new JTextField(name, 20);
        JTextField descField = new JTextField(desc, 20);
        JTextField priceField = new JTextField(price, 10);
        JTextField stockField = new JTextField(stock, 10);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        panel.add(new JLabel("Price:"));
        panel.add(priceField);
        panel.add(new JLabel("Stock:"));
        panel.add(stockField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Edit Item",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                Item item = new Item();
                item.setItemId(itemId);
                item.setItemName(nameField.getText().trim());
                item.setDescription(descField.getText().trim());
                item.setPrice(new BigDecimal(priceField.getText().trim()));
                item.setQuantityInStock(Integer.parseInt(stockField.getText().trim()));

                if (DBHelper.updateItem(item)) {
                    loadItems();
                    JOptionPane.showMessageDialog(this, "Item updated.");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update item.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --------------------------------------------
    // TOGGLE ACTIVE FIELD
    // --------------------------------------------
    private void toggleActive() {
        int row = itemsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an item first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DefaultTableModel model = (DefaultTableModel) itemsTable.getModel();
        int itemId = (int) model.getValueAt(row, 0);
        boolean active = (boolean) model.getValueAt(row, 5);

        if (DBHelper.setItemActive(itemId, !active)) {
            loadItems();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to change active status.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
