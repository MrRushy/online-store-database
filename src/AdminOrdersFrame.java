
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

class AdminOrdersFrame extends JFrame {

    private JTable ordersTable;
    private JTable itemsTable;
    private JButton closeButton;
    private JButton updateStatusButton;
    private JButton deleteOrderButton;
    private JComboBox<String> statusCombo;

    private List<Order> orders;

    AdminOrdersFrame() {
        initComponents();
        loadOrders();
        hookListeners();
    }

    private void initComponents() {
        setTitle("Manage Orders (Admin)");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(mainPanel);

        // Orders table
        ordersTable = new JTable();
        ordersTable.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Order ID", "Customer", "Date", "Status", "Total"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        JScrollPane ordersScroll = new JScrollPane(ordersTable);
        ordersScroll.setBorder(BorderFactory.createTitledBorder("Orders"));

        // Items table
        itemsTable = new JTable();
        itemsTable.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Item", "Qty", "Unit Price", "Line Total"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        JScrollPane itemsScroll = new JScrollPane(itemsTable);
        itemsScroll.setBorder(BorderFactory.createTitledBorder("Order Details"));

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, ordersScroll, itemsScroll);
        splitPane.setResizeWeight(0.5);
        splitPane.setOneTouchExpandable(true);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Bottom controls
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftControls.add(new JLabel("New Status:"));
        statusCombo = new JComboBox<>(new String[]{
                "PENDING",
                "PROCESSING",
                "SHIPPED",
                "CANCELLED",
                "COMPLETED"
        });
        leftControls.add(statusCombo);
        updateStatusButton = new JButton("Update Status");
        leftControls.add(updateStatusButton);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        deleteOrderButton = new JButton("Delete Order");
        closeButton = new JButton("Close");
        rightControls.add(deleteOrderButton);
        rightControls.add(closeButton);

        bottomPanel.add(leftControls, BorderLayout.WEST);
        bottomPanel.add(rightControls, BorderLayout.EAST);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setSize(900, 550);
        setLocationRelativeTo(null);
    }

    private void hookListeners() {
        closeButton.addActionListener(e -> dispose());

        ordersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = ordersTable.getSelectedRow();
                if (row >= 0 && orders != null && row < orders.size()) {
                    Order selected = orders.get(row);
                    loadOrderItems(selected.getOrderId());
                }
            }
        });

        updateStatusButton.addActionListener(e -> updateStatusForSelected());
        deleteOrderButton.addActionListener(e -> deleteSelectedOrder());
    }

    private void loadOrders() {
        orders = DBHelper.getAllOrders();
        DefaultTableModel model = (DefaultTableModel) ordersTable.getModel();
        model.setRowCount(0);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        if (orders != null) {
            for (Order o : orders) {
                String dateStr = o.getOrderDate() != null ? o.getOrderDate().format(fmt) : "";
                Object[] row = new Object[]{
                        o.getOrderId(),
                        o.getCustomerName(),
                        dateStr,
                        o.getStatus(),
                        o.getTotalAmount()
                };
                model.addRow(row);
            }
        }
    }

    private void loadOrderItems(int orderId) {
        List<OrderItem> items = DBHelper.getOrderItems(orderId);
        DefaultTableModel model = (DefaultTableModel) itemsTable.getModel();
        model.setRowCount(0);

        if (items != null) {
            for (OrderItem oi : items) {
                Object[] row = new Object[]{
                        oi.getItemName(),
                        oi.getQuantity(),
                        oi.getUnitPrice(),
                        oi.getLineTotal()
                };
                model.addRow(row);
            }
        }
    }

    private void updateStatusForSelected() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an order first.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Order selected = orders.get(row);
        String newStatus = (String) statusCombo.getSelectedItem();

        if (newStatus == null || newStatus.isEmpty()) {
            return;
        }

        boolean success = DBHelper.updateOrderStatus(selected.getOrderId(), newStatus);
        if (success) {
            loadOrders();
            ordersTable.setRowSelectionInterval(row, row); // reselect same row
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to update order status.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void deleteSelectedOrder() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an order to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Order selected = orders.get(row);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete order #" + selected.getOrderId() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean success = DBHelper.deleteOrder(selected.getOrderId());
        if (success) {
            loadOrders();
            DefaultTableModel itemsModel = (DefaultTableModel) itemsTable.getModel();
            itemsModel.setRowCount(0);
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to delete order.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
