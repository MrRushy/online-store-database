
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

class ManageCustomersFrame extends JFrame {

    private JTable customersTable;
    private JButton deleteButton;
    private JButton closeButton;
    private JButton editButton;   // NEW


    ManageCustomersFrame() {
        initComponents();
        loadCustomers();
        hookListeners();
    }

    private void initComponents() {
        setTitle("Manage Customers");
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
        editButton = new JButton("Edit Selected");
        deleteButton = new JButton("Delete Selected");
        closeButton = new JButton("Close");
        bottomPanel.add(editButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(closeButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setSize(600, 400);
        setLocationRelativeTo(null);
    }

    private void hookListeners() {
        closeButton.addActionListener(e -> dispose());
        deleteButton.addActionListener(e -> deleteSelectedCustomer());
        editButton.addActionListener(e -> showEditDialog());

    }

    private void showEditDialog() {
        int row = customersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a customer to edit.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        DefaultTableModel model = (DefaultTableModel) customersTable.getModel();
        int customerId = (int) model.getValueAt(row, 0);
        String currentUsername = model.getValueAt(row, 1).toString();
        String currentFullName = model.getValueAt(row, 2).toString();

        // We need the current address + password from DB.
        // Quick approach: query them in a helper.
        CustomerDetails details = DBHelper.getCustomerDetailsForAdmin(customerId);

        JTextField usernameField = new JTextField(currentUsername, 15);
        JTextField fullNameField = new JTextField(currentFullName, 20);
        JTextField addressField = new JTextField(
                details != null ? details.address : "", 20);
        JPasswordField passwordField = new JPasswordField(
                details != null ? details.password : "", 15);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Full Name:"));
        panel.add(fullNameField);
        panel.add(new JLabel("Address:"));
        panel.add(addressField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Edit Customer",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String fullName = fullNameField.getText().trim();
            String address = addressField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || fullName.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Username, full name, and password cannot be empty.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            boolean success = DBHelper.updateCustomerAsAdmin(
                    customerId, username, fullName, address, password
            );

            if (success) {
                loadCustomers(); // refresh table
                JOptionPane.showMessageDialog(
                        this,
                        "Customer updated.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to update customer.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
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

    private void deleteSelectedCustomer() {
        int row = customersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a customer to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        DefaultTableModel model = (DefaultTableModel) customersTable.getModel();
        int customerId = (int) model.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this customer?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean success = DBHelper.deleteCustomer(customerId);
        if (success) {
            loadCustomers();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to delete customer.\n(If you have foreign keys, consider using an 'active' flag instead.)",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
