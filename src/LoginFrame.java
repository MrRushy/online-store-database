import javax.swing.*;

public class LoginFrame extends JFrame {
    private JPanel mainPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton loginButton;
    private JButton signUpButton;

    public LoginFrame() {
        setTitle("Online Store - Login");
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // set up role options for combo-box
        if (roleComboBox != null && roleComboBox.getItemCount() == 0) {
            roleComboBox.addItem("Customer");
            roleComboBox.addItem("Employee");
            roleComboBox.addItem("Admin");
        }

        pack();
        setLocationRelativeTo(null); // center on screen

        // Login Button action
        loginButton.addActionListener(e -> handleLogin());
        //SignUp Button action
        signUpButton.addActionListener(e -> {
            CustomerSignUpFrame signUp = new CustomerSignUpFrame();

            JFrame frame = new JFrame("Customer Sign Up (FROM LOGIN)");
            frame.setContentPane(signUp.getMainPanel());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(this);
            frame.setVisible(true);

            dispose(); // closes login
        });







    }



        //method for handling logins for different types of users
        private void handleLogin() {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String role = (String) roleComboBox.getSelectedItem();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter both username and password.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if ("Customer".equals(role)) {
                Customer c = DBHelper.loginCustomer(username, password);
                if (c != null) {

                    CustomerDashboardFrame dashboard = new CustomerDashboardFrame(c);
                    dashboard.setVisible(true);
                    dispose(); // close the login window
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Invalid customer credentials.",
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE);
                }

            } else if ("Employee".equals(role)) {
                Employee emp = DBHelper.loginEmployee(username, password);
                if (emp != null) {


                    EmployeeDashboardFrame dashboard = new EmployeeDashboardFrame(emp);
                    dashboard.setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Invalid employee credentials.",
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE);
                }

            } else if ("Admin".equals(role)) {
                Admin admin = DBHelper.loginAdmin(username, password);
                if (admin != null) {
                    AdminDashboardFrame dashboard = new AdminDashboardFrame(admin);
                    dashboard.setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Invalid admin credentials.",
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

        }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}
