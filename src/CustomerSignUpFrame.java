import javax.swing.*;
import java.awt.*;

public class CustomerSignUpFrame {

    private JPanel mainPanel;
    private JTextField fullNameField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField addressField;
    private JTextField cityField;
    private JTextField stateField;
    private JTextField postalCodeField;
    private JButton createAccountButton;
    private JButton cancelButton;

    public CustomerSignUpFrame() {
        initActions();
    }

    private void initActions() {
        cancelButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            closeWindow();
        });

        createAccountButton.addActionListener(e -> handleCreateAccount());
    }

    private void handleCreateAccount() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String address = addressField.getText().trim();
        String city = cityField.getText().trim();
        String state = stateField.getText().trim();
        String postalCode = postalCodeField.getText().trim();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()
                || address.isEmpty() || city.isEmpty() || state.isEmpty() || postalCode.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "Please fill in all fields.");
            return;
        }

        boolean success = DBHelper.createCustomer(
                username, password, fullName,
                address, city, state, postalCode
        );

        if (success) {
            Customer c = DBHelper.loginCustomer(username, password);
            if (c != null) {
                new CustomerDashboardFrame(c).setVisible(true);
                closeWindow();
            } else {
                new LoginFrame().setVisible(true);
                closeWindow();
            }
        } else {
            JOptionPane.showMessageDialog(mainPanel, "Username may already be taken.");
        }
    }

    private void closeWindow() {
        Window w = SwingUtilities.getWindowAncestor(mainPanel);
        System.out.println("Closing window: " + w);
        if (w != null) {
            w.dispose();
        }
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
