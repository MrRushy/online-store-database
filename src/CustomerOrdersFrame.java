import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

class CustomerOrdersFrame extends JFrame {

    private final int customerId;

    private JTable ordersTable;
    private JTable orderItemsTable;
    private JButton closeButton;
    private JButton cancelOrderButton;

    private List<Order> orders; // store for lookup

    CustomerOrdersFrame(int customerId) {
        this.customerId = customerId;
        initComponents();
        loadOrders();
        hookListeners();
    }

    private void initComponents() {
        setTitle("My Orders");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(mainPanel);

        // === Orders table (top) ===
        ordersTable = new JTable();
        ordersTable.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Order ID", "Date", "Status", "Total"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        JScrollPane ordersScroll = new JScrollPane(ordersTable);
        ordersScroll.setBorder(BorderFactory.createTitledBorder("Orders"));

        // === Order items table (bottom) ===
        orderItemsTable = new JTable();
        orderItemsTable.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Item", "Qty", "Unit Price", "Line Total"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        JScrollPane itemsScroll = new JScrollPane(orderItemsTable);
        itemsScroll.setBorder(BorderFactory.createTitledBorder("Order Details"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, ordersScroll, itemsScroll);
        splitPane.setResizeWeight(0.5);
        splitPane.setOneTouchExpandable(true);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        // === Fixed-layout bottom bar ===
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cancelOrderButton = new JButton("Cancel Selected Order");
        closeButton = new JButton("Close");
        rightButtons.add(cancelOrderButton);
        rightButtons.add(closeButton);

        bottomPanel.add(rightButtons, BorderLayout.EAST);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setSize(700, 500);
        setLocationRelativeTo(null);
    }

    private void hookListeners() {
        closeButton.addActionListener(e -> CustomerOrdersFrame.this.dispose());

        cancelOrderButton.addActionListener(e -> cancelSelectedOrder());

        ordersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = ordersTable.getSelectedRow();
                if (row >= 0 && orders != null && row < orders.size()) {
                    Order selectedOrder = orders.get(row);
                    loadOrderItems(selectedOrder.getOrderId());
                }
            }
        });
    }

    private void loadOrders() {
        orders = DBHelper.getOrdersForCustomer(customerId);

        DefaultTableModel model = (DefaultTableModel) ordersTable.getModel();
        model.setRowCount(0);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        if (orders != null) {
            for (Order o : orders) {
                String dateStr = o.getOrderDate() != null ? o.getOrderDate().format(fmt) : "";
                model.addRow(new Object[]{
                        o.getOrderId(),
                        dateStr,
                        o.getStatus(),
                        o.getTotalAmount()
                });
            }
        }
    }

    private void loadOrderItems(int orderId) {
        List<OrderItem> items = DBHelper.getOrderItems(orderId);

        DefaultTableModel model = (DefaultTableModel) orderItemsTable.getModel();
        model.setRowCount(0);

        if (items != null) {
            for (OrderItem oi : items) {
                model.addRow(new Object[]{
                        oi.getItemName(),
                        oi.getQuantity(),
                        oi.getUnitPrice(),
                        oi.getLineTotal()
                });
            }
        }
    }

    private void cancelSelectedOrder() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an order to cancel.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Order selected = orders.get(row);
        String status = selected.getStatus();

        if ("SHIPPED".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(
                    this,
                    "This order can no longer be cancelled.",
                    "Cannot Cancel",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Cancel and delete order #" + selected.getOrderId() + "?",
                "Confirm Cancel",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean success = DBHelper.deleteOrder(selected.getOrderId());
        if (success) {
            JOptionPane.showMessageDialog(
                    this,
                    "Order cancelled.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // Refresh list + clear items
            loadOrders();
            ((DefaultTableModel) orderItemsTable.getModel()).setRowCount(0);

        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to cancel order.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
