
import javax.swing.*;
import java.awt.*;

class CustomerProfileFrame extends JFrame {

    private final Customer customer;

    private JTextField usernameField;
    private JTextField fullNameField;
    private JTextField addressField;
    private JButton saveButton;
    private JButton cancelButton;

    CustomerProfileFrame(Customer customer) {
        this.customer = customer;
        initComponents();
        loadCustomerData();
        hookListeners();
    }

    private void initComponents() {
        setTitle("Edit Profile");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setLayout(new GridBagLayout());
        setContentPane(mainPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Username
        gbc.gridx = 0; gbc.gridy = row;
        mainPanel.add(new JLabel("Username:"), gbc);
        usernameField = new JTextField(20);
        gbc.gridx = 1;
        mainPanel.add(usernameField, gbc);
        row++;

        // Full Name
        gbc.gridx = 0; gbc.gridy = row;
        mainPanel.add(new JLabel("Full Name:"), gbc);
        fullNameField = new JTextField(20);
        gbc.gridx = 1;
        mainPanel.add(fullNameField, gbc);
        row++;

        // Address
        gbc.gridx = 0; gbc.gridy = row;
        mainPanel.add(new JLabel("Address:"), gbc);
        addressField = new JTextField(20);
        gbc.gridx = 1;
        mainPanel.add(addressField, gbc);
        row++;

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cancelButton = new JButton("Cancel");
        saveButton = new JButton("Save Changes");
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        pack();
        setLocationRelativeTo(null);
    }

    private void loadCustomerData() {
        if (customer == null) return;

        usernameField.setText(customer.getUsername());
        fullNameField.setText(customer.getFullName());
        addressField.setText(customer.getAddress());
    }

    private void hookListeners() {
        cancelButton.addActionListener(e -> dispose());

        saveButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String fullName = fullNameField.getText().trim();
            String address = addressField.getText().trim();

            if (username.isEmpty() || fullName.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Username and full name cannot be empty.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // push updates into the Customer object
            customer.setUsername(username);
            customer.setFullName(fullName);
            customer.setAddress(address);

            boolean success = DBHelper.updateCustomerProfile(customer);
            if (success) {
                JOptionPane.showMessageDialog(
                        this,
                        "Profile updated successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
                dispose();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "There was a problem updating your profile.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
