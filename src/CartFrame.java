import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;


public class CartFrame extends JFrame {

    private final int customerId;
    private final List<CartItem> cart;

    private JTable cartTable;
    private JLabel totalLabel;
    private JTextField couponField;
    private JButton placeOrderButton;
    private JButton cancelButton;

    public CartFrame() {
        // Used by GUI Designer / any legacy no-arg usages.
        // Delegates to the full constructor so listeners & layout are always wired.
        this(0, new ArrayList<>());
    }


    public CartFrame(int customerId, List<CartItem> cart) {
        this.customerId = customerId;
        this.cart = cart;
        initComponents();
        loadCartIntoTable();
        updateTotalLabel();
        hookListeners();
    }

    private void initComponents() {
        setTitle("Your Cart");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(mainPanel);

        // ===== Cart table =====
        cartTable = new JTable();
        cartTable.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Item", "Price", "Qty", "Line Total"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        JScrollPane scrollPane = new JScrollPane(cartTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Cart Items"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // ===== Bottom section =====
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Left: total + coupon
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        totalLabel = new JLabel("Total: $0.00");
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalPanel.add(totalLabel);

        JPanel couponPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        couponPanel.add(new JLabel("Coupon code (optional): "));
        couponField = new JTextField(12);
        couponPanel.add(couponField);

        leftPanel.add(totalPanel);
        leftPanel.add(couponPanel);

        // Right: buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cancelButton = new JButton("Close");
        placeOrderButton = new JButton("Place Order");
        rightPanel.add(cancelButton);
        rightPanel.add(placeOrderButton);

        bottomPanel.add(leftPanel, BorderLayout.WEST);
        bottomPanel.add(rightPanel, BorderLayout.EAST);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void hookListeners() {
        // Close button: always close the top-level window that owns this button
        cancelButton.addActionListener(e -> {
            java.awt.Window w = SwingUtilities.getWindowAncestor(cancelButton);
            if (w != null) {
                w.dispose();
            }
        });

        placeOrderButton.addActionListener(e -> {
            if (cart == null || cart.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Your cart is empty.",
                        "Cannot place order",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            String couponCode = couponField.getText().trim();
            if (couponCode.isEmpty()) {
                couponCode = null;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Place this order?",
                    "Confirm Order",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            boolean success = DBHelper.placeOrder(customerId, cart, couponCode);

            if (success) {
                JOptionPane.showMessageDialog(
                        this,
                        "Order placed successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
                cart.clear(); // clear shared cart

                java.awt.Window w = SwingUtilities.getWindowAncestor(placeOrderButton);
                if (w != null) {
                    w.dispose();
                }
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "There was a problem placing your order.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }


    private void loadCartIntoTable() {
        DefaultTableModel model = (DefaultTableModel) cartTable.getModel();
        model.setRowCount(0);

        if (cart == null) return;

        for (CartItem ci : cart) {
            Object[] row = new Object[]{
                    ci.getItemName(),
                    ci.getPrice(),
                    ci.getQuantity(),
                    ci.getLineTotal()
            };
            model.addRow(row);
        }
    }

    private void updateTotalLabel() {
        BigDecimal total = BigDecimal.ZERO;
        if (cart != null) {
            for (CartItem ci : cart) {
                total = total.add(ci.getLineTotal());
            }
        }
        totalLabel.setText("Total: $" + total.toPlainString());
    }
}

