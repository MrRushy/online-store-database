
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EmployeeOrdersFrame extends JFrame {

    private JTable ordersTable;
    private JTable orderItemsTable;
    private JButton closeButton;

    private List<Order> orders; // all orders, for lookup

    public EmployeeOrdersFrame() {
        initComponents();
        loadOrders();
        hookListeners();
    }

    private void initComponents() {
        setTitle("All Orders");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(mainPanel);

        // Orders table (top)
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

        // Order items table (bottom)
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

        // Split pane: top = orders, bottom = items
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, ordersScroll, itemsScroll);
        splitPane.setResizeWeight(0.5);          // half / half
        splitPane.setOneTouchExpandable(true);   // optional, gives little arrows

        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Bottom: close button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        closeButton = new JButton("Close");
        bottomPanel.add(closeButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setSize(800, 500);
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
                        o.getCustomerName(),  // <- from JOIN
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

        DefaultTableModel model = (DefaultTableModel) orderItemsTable.getModel();
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
}

