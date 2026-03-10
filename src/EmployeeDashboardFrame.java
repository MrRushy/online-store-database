
import javax.swing.*;
import java.awt.*;

public class EmployeeDashboardFrame extends JFrame {

    private final Employee employee;

    private JLabel welcomeLabel;
    private JButton viewCustomersButton;
    private JButton viewOrdersButton;
    private JButton manageItemsButton;
    private JButton logoutButton;

    public EmployeeDashboardFrame(Employee employee) {
        this.employee = employee;
        initComponents();
        hookListeners();
    }

    private void initComponents() {
        setTitle("Employee Dashboard");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(mainPanel);

        // Top: welcome label
        welcomeLabel = new JLabel("Employee Dashboard");
        if (employee != null && employee.getFullName() != null) {
            welcomeLabel.setText("Employee Dashboard – Welcome, " + employee.getFullName());
        }
        welcomeLabel.setFont(welcomeLabel.getFont().deriveFont(Font.BOLD, 16f));
        mainPanel.add(welcomeLabel, BorderLayout.NORTH);

        // Center: buttons
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(4, 1, 10, 10));

        viewCustomersButton = new JButton("View Customers");
        viewOrdersButton = new JButton("View Orders");
        manageItemsButton = new JButton("Manage Items");
        logoutButton = new JButton("Logout");

        centerPanel.add(viewCustomersButton);
        centerPanel.add(viewOrdersButton);
        centerPanel.add(manageItemsButton);
        centerPanel.add(logoutButton);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        setSize(400, 300);
        setLocationRelativeTo(null);
    }

    private void hookListeners() {
        // Phase 2.2: View Customers
        viewCustomersButton.addActionListener(e -> {
            EmployeeCustomersFrame f = new EmployeeCustomersFrame();
            f.setLocationRelativeTo(this);
            f.setVisible(true);
        });

        viewOrdersButton.addActionListener(e -> {
            EmployeeOrdersFrame f = new EmployeeOrdersFrame();
            f.setLocationRelativeTo(this);
            f.setVisible(true);
        });


        manageItemsButton.addActionListener(e -> {
            ManageItemsFrame f = new ManageItemsFrame();
            f.setLocationRelativeTo(this);
            f.setVisible(true);
        });


        // Logout: close and reopen LoginFrame
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }
}

