
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

class ManageEmployeesFrame extends JFrame {

    private JTable employeesTable;
    private JButton addButton;
    private JButton deleteButton;
    private JButton closeButton;
    private JButton editButton;

    ManageEmployeesFrame() {
        initComponents();
        loadEmployees();
        hookListeners();
    }

    private void initComponents() {
        setTitle("Manage Employees");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(mainPanel);

        employeesTable = new JTable();
        employeesTable.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Username", "Full Name"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        JScrollPane scrollPane = new JScrollPane(employeesTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Employees"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButton = new JButton("Create Employee");
        editButton = new JButton("Edit Selected");
        deleteButton = new JButton("Delete Selected");
        closeButton = new JButton("Close");

        bottomPanel.add(addButton);
        bottomPanel.add(editButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(closeButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setSize(600, 400);
        setLocationRelativeTo(null);
    }

    private void hookListeners() {
        closeButton.addActionListener(e -> dispose());

        addButton.addActionListener(e -> showCreateDialog());
        deleteButton.addActionListener(e -> deleteSelectedEmployee());
        editButton.addActionListener(e -> showEditDialog());

    }

    private void showEditDialog() {
        int row = employeesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an employee to edit.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        DefaultTableModel model = (DefaultTableModel) employeesTable.getModel();
        int employeeId = (int) model.getValueAt(row, 0);
        String currentUsername = model.getValueAt(row, 1).toString();
        String currentFullName = model.getValueAt(row, 2).toString();

        JTextField usernameField = new JTextField(currentUsername, 15);
        JTextField fullNameField = new JTextField(currentFullName, 20);
        JPasswordField passwordField = new JPasswordField(15); // leave blank = must fill

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Full Name:"));
        panel.add(fullNameField);
        panel.add(new JLabel("New Password:"));
        panel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Edit Employee",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String fullName = fullNameField.getText().trim();
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

            boolean success = DBHelper.updateEmployee(employeeId, username, fullName, password);
            if (success) {
                loadEmployees();
                JOptionPane.showMessageDialog(
                        this,
                        "Employee updated.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to update employee.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }


    private void loadEmployees() {
        List<Employee> employees = DBHelper.getAllEmployees();

        DefaultTableModel model = (DefaultTableModel) employeesTable.getModel();
        model.setRowCount(0);

        if (employees != null) {
            for (Employee emp : employees) {
                Object[] row = new Object[]{
                        emp.getEmployeeId(),
                        emp.getUsername(),
                        emp.getFullName()
                };
                model.addRow(row);
            }
        }
    }

    private void showCreateDialog() {
        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JTextField fullNameField = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Full Name:"));
        panel.add(fullNameField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Create Employee",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String fullName = fullNameField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "All fields are required.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            boolean success = DBHelper.createEmployee(username, password, fullName);
            if (success) {
                loadEmployees();
                JOptionPane.showMessageDialog(
                        this,
                        "Employee created.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to create employee.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void deleteSelectedEmployee() {
        int row = employeesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an employee to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        DefaultTableModel model = (DefaultTableModel) employeesTable.getModel();
        int employeeId = (int) model.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this employee?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean success = DBHelper.deleteEmployee(employeeId);
        if (success) {
            loadEmployees();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to delete employee.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
