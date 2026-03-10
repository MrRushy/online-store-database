
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EmployeeCustomersFrame extends JFrame {

    private JTable customersTable;
    private JButton closeButton;

    public EmployeeCustomersFrame() {
        initComponents();
        loadCustomers();
        hookListeners();
    }

    private void initComponents() {
        setTitle("All Customers");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(mainPanel);

        customersTable = new JTable();
        customersTable.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Username", "Full Name"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        JScrollPane scrollPane = new JScrollPane(customersTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Customers"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        closeButton = new JButton("Close");
        bottomPanel.add(closeButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setSize(600, 400);
        setLocationRelativeTo(null);
    }

    private void loadCustomers() {
        List<Customer> customers = DBHelper.getAllCustomers();
        DefaultTableModel model = (DefaultTableModel) customersTable.getModel();
        model.setRowCount(0);

        if (customers != null) {
            for (Customer c : customers) {
                Object[] row = new Object[]{
                        c.getCustomerId(),
                        c.getUsername(),
                        c.getFullName()
                };
                model.addRow(row);
            }
        }
    }

    private void hookListeners() {
        closeButton.addActionListener(e -> dispose());
    }
}
