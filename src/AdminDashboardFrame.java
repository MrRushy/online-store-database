import javax.swing.*;
import java.awt.*;

class AdminDashboardFrame extends JFrame {

    private final Admin admin;

    private JLabel welcomeLabel;
    private JButton manageEmployeesButton;
    private JButton manageCustomersButton;
    private JButton manageOrdersButton;
    private JButton logoutButton;

    AdminDashboardFrame(Admin admin) {
        this.admin = admin;
        initComponents();
        hookListeners();
    }

    private void initComponents() {
        setTitle("Admin Dashboard");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(mainPanel);

        welcomeLabel = new JLabel("Admin Dashboard");
        if (admin != null && admin.getFullName() != null) {
            welcomeLabel.setText("Admin Dashboard – Welcome, " + admin.getFullName());
        }
        welcomeLabel.setFont(welcomeLabel.getFont().deriveFont(Font.BOLD, 16f));
        mainPanel.add(welcomeLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        manageEmployeesButton = new JButton("Manage Employees");
        manageCustomersButton = new JButton("Manage Customers");
        manageOrdersButton = new JButton("Manage Orders");
        logoutButton = new JButton("Logout");

        centerPanel.add(manageEmployeesButton);
        centerPanel.add(manageCustomersButton);
        centerPanel.add(manageOrdersButton);
        centerPanel.add(logoutButton);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        setSize(400, 300);
        setLocationRelativeTo(null);
    }

    private void hookListeners() {
        manageEmployeesButton.addActionListener(e -> {
            ManageEmployeesFrame f = new ManageEmployeesFrame();
            f.setLocationRelativeTo(this);
            f.setVisible(true);
        });

        manageCustomersButton.addActionListener(e -> {
            ManageCustomersFrame f = new ManageCustomersFrame();
            f.setLocationRelativeTo(this);
            f.setVisible(true);
        });

        manageOrdersButton.addActionListener(e -> {
            AdminOrdersFrame f = new AdminOrdersFrame();
            f.setLocationRelativeTo(this);
            f.setVisible(true);
        });

        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }
}
