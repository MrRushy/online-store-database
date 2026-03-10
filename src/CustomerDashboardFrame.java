import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

public class CustomerDashboardFrame extends JFrame {

    private JPanel mainPanel;
    private JLabel welcomeLabel;
    private JButton browseItemsButton;
    private JTable itemsTable;
    private JButton logoutButton;
    private JButton addToCartButton;
    private JButton viewCartButton;
    private JButton viewOrdersButton;
    private JButton editProfileButton;
    private final List<CartItem> cart = new ArrayList<>();


    private Customer loggedInCustomer;

    public CustomerDashboardFrame(Customer customer) {
        this.loggedInCustomer = customer;

        setTitle("Online Store - Customer Dashboard");
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 400);
        setLocationRelativeTo(null);

        // Set the welcome text
        if (loggedInCustomer != null) {
            welcomeLabel.setText("Welcome, " + loggedInCustomer.getFullName() + "!");
        }

        // Set up table model
        setupItemsTable();

        // Button actions
        browseItemsButton.addActionListener(e -> loadItems());

        logoutButton.addActionListener(e -> {
            // Go back to login screen
            new LoginFrame().setVisible(true);
            dispose();
        });

        addToCartButton.addActionListener(e -> addSelectedItemToCart());

        viewCartButton.addActionListener(e -> openCartWindow());

        viewOrdersButton.addActionListener(e -> openCustomerOrdersWindow());

        editProfileButton.addActionListener(e -> openProfileWindow());


    }
    private void openProfileWindow() {

        CustomerProfileFrame profileFrame = new CustomerProfileFrame(loggedInCustomer);
        profileFrame.setLocationRelativeTo(this);
        profileFrame.setVisible(true);
    }


    private void openCustomerOrdersWindow() {
        int customerId = loggedInCustomer.getCustomerId();
        CustomerOrdersFrame ordersFrame = new CustomerOrdersFrame(customerId);
        ordersFrame.setLocationRelativeTo(this);
        ordersFrame.setVisible(true);
    }


    private void openCartWindow() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Your cart is currently empty.",
                    "Cart",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        // Adjust getter name depending on your Customer class
        int customerId = loggedInCustomer.getCustomerId(); // maybe getId()

        CartFrame cartFrame = new CartFrame(customerId, cart);
        cartFrame.setLocationRelativeTo(this);
        cartFrame.setVisible(true);
    }


    private void showCartDebugDialog() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Your cart is currently empty.",
                    "Cart",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Current cart:\n\n");
        for (CartItem ci : cart) {
            sb.append(ci.getQuantity())
                    .append(" x ")
                    .append(ci.getItemName())
                    .append(" @ ")
                    .append(ci.getPrice())
                    .append(" = ")
                    .append(ci.getLineTotal())
                    .append("\n");
        }

        JOptionPane.showMessageDialog(
                this,
                sb.toString(),
                "Cart (debug view)",
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    private void setupItemsTable() {
        // Define the columns
        DefaultTableModel model = new DefaultTableModel(
                new Object[] { "ID", "Name", "Description", "Price", "In Stock" }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only for now
            }
        };

        itemsTable.setModel(model);
    }

    private void loadItems() {
        List<Item> items = DBHelper.getActiveItems();

        DefaultTableModel model = (DefaultTableModel) itemsTable.getModel();
        model.setRowCount(0); // clear previous rows

        for (Item item : items) {
            Object[] row = new Object[]{
                    item.getItemId(),
                    item.getItemName(),
                    item.getDescription(),
                    item.getPrice(),
                    item.getQuantityInStock()
            };
            model.addRow(row);
        }
    }

    private void addSelectedItemToCart() {
        int row = itemsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an item first.",
                    "No item selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        DefaultTableModel model = (DefaultTableModel) itemsTable.getModel();

        // Adjust indexes if your columns differ
        Object idObj = model.getValueAt(row, 0);
        Object nameObj = model.getValueAt(row, 1);
        Object priceObj = model.getValueAt(row, 3); // or whatever column is price

        if (idObj == null || nameObj == null || priceObj == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selected row is missing data.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        int itemId = Integer.parseInt(idObj.toString());
        String itemName = nameObj.toString();

        BigDecimal price;
        if (priceObj instanceof BigDecimal) {
            price = (BigDecimal) priceObj;
        } else {
            try {
                price = new BigDecimal(priceObj.toString());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Price format is invalid for selected item.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
        }

        // Ask for quantity
        String qtyStr = JOptionPane.showInputDialog(
                this,
                "Enter quantity:",
                "Add to Cart",
                JOptionPane.QUESTION_MESSAGE
        );

        if (qtyStr == null) {
            // user cancelled
            return;
        }

        qtyStr = qtyStr.trim();
        if (qtyStr.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Quantity cannot be empty.",
                    "Invalid quantity",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(qtyStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid whole number for quantity.",
                    "Invalid quantity",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (quantity <= 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Quantity must be greater than zero.",
                    "Invalid quantity",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // See if this item is already in the cart
        CartItem existing = null;
        for (CartItem ci : cart) {
            if (ci.getItemId() == itemId) {
                existing = ci;
                break;
            }
        }

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
        } else {
            cart.add(new CartItem(itemId, itemName, price, quantity));
        }

        JOptionPane.showMessageDialog(
                this,
                "Added " + quantity + " x " + itemName + " to cart.",
                "Item added",
                JOptionPane.INFORMATION_MESSAGE
        );
    }


}
