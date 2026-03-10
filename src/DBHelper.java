import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;




public class DBHelper {

    // Get a connection to the database
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(Database.URL, Database.USER, Database.PASSWORD);
    }

    // Simple test method to make sure DB connection works
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---- CUSTOMER LOGIN ----
    public static Customer loginCustomer(String username, String password) {
        String sql = "SELECT customer_id, username, password, full_name " +
                "FROM customers WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");

                    // For now, plain-text check
                    if (storedPassword.equals(password)) {
                        Customer c = new Customer();
                        c.setCustomerId(rs.getInt("customer_id"));
                        c.setUsername(rs.getString("username"));
                        c.setFullName(rs.getString("full_name"));
                        return c;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // login failed
    }

    // ---- CREATE CUSTOMER ACCOUNT ----
    public static boolean createCustomer(String username, String password, String fullName,
                                         String addressLine1, String city, String state, String postalCode) {
        String sql = "INSERT INTO customers " +
                "(username, password, full_name, address_line1, city, state, postal_code) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, fullName);
            stmt.setString(4, addressLine1);
            stmt.setString(5, city);
            stmt.setString(6, state);
            stmt.setString(7, postalCode);

            int rows = stmt.executeUpdate();
            return rows == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //---EDIT CUSTOMER PROFILE---
    public static boolean updateCustomerProfile(Customer c) {
        String sql = "UPDATE customers SET username = ?, full_name = ?, address_line1 = ? " +
                "WHERE customer_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getUsername());
            ps.setString(2, c.getFullName());
            ps.setString(3, c.getAddress());
            ps.setInt(4, c.getCustomerId());

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // ---- LIST ACTIVE ITEMS ----
    public static List<Item> getActiveItems() {
        List<Item> items = new ArrayList<>();

        String sql = "SELECT item_id, item_name, description, price, quantity_in_stock " +
                "FROM items WHERE is_active = 1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Item item = new Item();
                item.setItemId(rs.getInt("item_id"));
                item.setItemName(rs.getString("item_name"));
                item.setDescription(rs.getString("description"));
                item.setPrice(rs.getBigDecimal("price"));
                item.setQuantityInStock(rs.getInt("quantity_in_stock"));
                items.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }
    // ---- EMPLOYEE LOGIN ----
    public static Employee loginEmployee(String username, String password) {
        String sql = "SELECT employee_id, username, password, full_name " +
                "FROM employees WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (storedPassword.equals(password)) {
                        Employee e = new Employee();
                        e.setEmployeeId(rs.getInt("employee_id"));
                        e.setUsername(rs.getString("username"));
                        e.setFullName(rs.getString("full_name"));
                        return e;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // login failed
    }

    // ---- ADMIN LOGIN ----
    public static Admin loginAdmin(String username, String password) {
        String sql = "SELECT admin_id, username, password, full_name " +
                "FROM admins WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (storedPassword.equals(password)) {
                        Admin a = new Admin();
                        a.setAdminId(rs.getInt("admin_id"));
                        a.setUsername(rs.getString("username"));
                        a.setFullName(rs.getString("full_name"));
                        return a;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // login failed
    }
   // ----PLACE ORDER-----
    public static boolean placeOrder(int customerId, List<CartItem> cart, String couponCode) {
        if (cart == null || cart.isEmpty()) {
            return false;
        }

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 1) Calculate total
            BigDecimal total = BigDecimal.ZERO;
            for (CartItem ci : cart) {
                total = total.add(ci.getLineTotal());
            }

            // 2) Insert into orders
            String insertOrderSql =
                    "INSERT INTO orders (customer_id, order_date, status, total_amount) " +
                            "VALUES (?, NOW(), ?, ?)";

            int orderId = -1;

            try (PreparedStatement psOrder = conn.prepareStatement(
                    insertOrderSql,
                    Statement.RETURN_GENERATED_KEYS
            )) {
                psOrder.setInt(1, customerId);
                psOrder.setString(2, "PENDING");     // default status
                psOrder.setBigDecimal(3, total);

                psOrder.executeUpdate();

                try (ResultSet rs = psOrder.getGeneratedKeys()) {
                    if (rs.next()) {
                        orderId = rs.getInt(1);
                    }
                }
            }

            if (orderId == -1) {
                conn.rollback();
                return false;
            }

            // 3) Insert each cart item into order_items
            String insertItemSql =
                    "INSERT INTO order_items (order_id, item_id, quantity, unit_price, line_total) " +
                            "VALUES (?, ?, ?, ?, ?)";

            // 4) Update stock for each item
            String updateStockSql =
                    "UPDATE items SET quantity_in_stock = quantity_in_stock - ? WHERE item_id = ?";

            try (PreparedStatement psItem = conn.prepareStatement(insertItemSql);
                 PreparedStatement psStock = conn.prepareStatement(updateStockSql)) {

                for (CartItem ci : cart) {
                    // order_items insert
                    psItem.setInt(1, orderId);
                    psItem.setInt(2, ci.getItemId());
                    psItem.setInt(3, ci.getQuantity());
                    psItem.setBigDecimal(4, ci.getPrice());
                    psItem.setBigDecimal(5, ci.getLineTotal());
                    psItem.addBatch();

                    // stock update
                    psStock.setInt(1, ci.getQuantity());
                    psStock.setInt(2, ci.getItemId());
                    psStock.addBatch();
                }

                psItem.executeBatch();
                psStock.executeBatch();
            }


            // (e.g., update orders set coupon_code = ? where order_id = ?)

            conn.commit();
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }
    //----SHOW CUSTOMER ORDERS------
    public static List<Order> getOrdersForCustomer(int customerId) {
        List<Order> orders = new ArrayList<>();

        String sql = "SELECT order_id, customer_id, order_date, status, total_amount " +
                "FROM orders WHERE customer_id = ? ORDER BY order_date DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");
                    int cid = rs.getInt("customer_id");
                    java.sql.Timestamp ts = rs.getTimestamp("order_date");
                    LocalDateTime orderDate = ts != null ? ts.toLocalDateTime() : null;
                    String status = rs.getString("status");
                    BigDecimal totalAmount = rs.getBigDecimal("total_amount");

                    Order order = new Order(orderId, cid, orderDate, status, totalAmount);
                    orders.add(order);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }
    //-------SHOW ITEMS-------
    public static List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();

        String sql = "SELECT oi.item_id, i.item_name, oi.quantity, oi.unit_price, oi.line_total " +
                "FROM order_items oi " +
                "JOIN items i ON oi.item_id = i.item_id " +
                "WHERE oi.order_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int itemId = rs.getInt("item_id");
                    String itemName = rs.getString("item_name");
                    int quantity = rs.getInt("quantity");
                    BigDecimal unitPrice = rs.getBigDecimal("unit_price");
                    BigDecimal lineTotal = rs.getBigDecimal("line_total");

                    OrderItem item = new OrderItem(itemId, itemName, quantity, unitPrice, lineTotal);
                    items.add(item);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }
   //-------SHOW CUSTOMER (EMPLOYEE)--------
   public static List<Customer> getAllCustomers() {
       List<Customer> customers = new ArrayList<>();

       String sql = "SELECT customer_id, username, full_name FROM customers ORDER BY customer_id";

       try (Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

           while (rs.next()) {
               Customer c = new Customer();
               c.setCustomerId(rs.getInt("customer_id"));
               c.setUsername(rs.getString("username"));
               c.setFullName(rs.getString("full_name"));
               customers.add(c);
           }

       } catch (SQLException e) {
           e.printStackTrace();
       }

       return customers;
   }

   //-------GET ALL ORDERS(EMPLOYEE)------
   public static List<Order> getAllOrders() {
       List<Order> orders = new ArrayList<>();

       String sql = "SELECT o.order_id, o.customer_id, o.order_date, o.status, o.total_amount, " +
               "       c.full_name AS customer_name " +
               "FROM orders o " +
               "JOIN customers c ON o.customer_id = c.customer_id " +
               "ORDER BY customer_name ASC, o.order_date DESC";

       try (Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

           while (rs.next()) {
               int orderId = rs.getInt("order_id");
               int customerId = rs.getInt("customer_id");
               java.sql.Timestamp ts = rs.getTimestamp("order_date");
               LocalDateTime orderDate = ts != null ? ts.toLocalDateTime() : null;
               String status = rs.getString("status");
               BigDecimal totalAmount = rs.getBigDecimal("total_amount");
               String customerName = rs.getString("customer_name");

               Order o = new Order(orderId, customerId, orderDate, status, totalAmount, customerName);
               orders.add(o);
           }

       } catch (SQLException e) {
           e.printStackTrace();
       }

       return orders;
   }
        //----GET ALL ITEM (inactive+active) (EMPLOYEE)------
        public static List<Item> getAllItems() {
            List<Item> items = new ArrayList<>();

            String sql = "SELECT item_id, item_name, description, price, quantity_in_stock, is_active " +
                    "FROM items ORDER BY item_id";

            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Item item = new Item();
                    item.setItemId(rs.getInt("item_id"));
                    item.setItemName(rs.getString("item_name"));
                    item.setDescription(rs.getString("description"));
                    item.setPrice(rs.getBigDecimal("price"));
                    item.setQuantityInStock(rs.getInt("quantity_in_stock"));
                    item.setActive(rs.getBoolean("is_active"));

                    items.add(item);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return items;
        }

    //------ADD NEW ITEMS(EMPLOYEE)-------
    public static boolean insertItem(Item item) {
        String sql = "INSERT INTO items (item_name, description, price, quantity_in_stock, is_active) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getItemName());
            ps.setString(2, item.getDescription());
            ps.setBigDecimal(3, item.getPrice());
            ps.setInt(4, item.getQuantityInStock());
            ps.setBoolean(5, item.isActive());

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
        //-------UPDATE ITEMS (EMPLOYEE)--------
        public static boolean updateItem(Item item) {
            String sql = "UPDATE items SET item_name = ?, description = ?, price = ?, quantity_in_stock = ? " +
                    "WHERE item_id = ?";

            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, item.getItemName());
                ps.setString(2, item.getDescription());
                ps.setBigDecimal(3, item.getPrice());
                ps.setInt(4, item.getQuantityInStock());
                ps.setInt(5, item.getItemId());

                int rows = ps.executeUpdate();
                return rows > 0;

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    //----SET ITEM ACTIVE(EMPLOYEE)-------
    public static boolean setItemActive(int itemId, boolean active) {
        String sql = "UPDATE items SET is_active = ? WHERE item_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, active);
            ps.setInt(2, itemId);

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    //-------GET ALL EMPLOYEES(ADMIN)------
    public static List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();

        String sql = "SELECT employee_id, username, full_name FROM employees ORDER BY employee_id";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Employee e = new Employee();
                e.setEmployeeId(rs.getInt("employee_id"));
                e.setUsername(rs.getString("username"));
                e.setFullName(rs.getString("full_name"));
                employees.add(e);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return employees;
    }
    //---CREATE NEW EMPLOYEE(ADMIN)---
    public static boolean createEmployee(String username, String password, String fullName) {
        String sql = "INSERT INTO employees (username, password, full_name) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, fullName);

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    //----DELETE EMPLOYEE (ADMIN)----
    public static boolean deleteEmployee(int employeeId) {
        String sql = "DELETE FROM employees WHERE employee_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeId);
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //------DELETE CUSTOMER(ADMIN)------
    public static boolean deleteCustomer(int customerId) {
        // If you have foreign keys from orders → customers,
        // consider either disabling delete or handling cascade manually.
        String sql = "DELETE FROM customers WHERE customer_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    //-----UPDATE ORDER STATUS(ADMIN)------
    public static boolean updateOrderStatus(int orderId, String newStatus) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, orderId);

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    //----DELETE ORDER(ADMIN)----
    public static boolean deleteOrder(int orderId) {
        String deleteItemsSql = "DELETE FROM order_items WHERE order_id = ?";
        String deleteOrderSql = "DELETE FROM orders WHERE order_id = ?";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psItems = conn.prepareStatement(deleteItemsSql);
                 PreparedStatement psOrder = conn.prepareStatement(deleteOrderSql)) {

                psItems.setInt(1, orderId);
                psItems.executeUpdate();

                psOrder.setInt(1, orderId);
                int rows = psOrder.executeUpdate();

                conn.commit();
                return rows > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    //------UPDATE CUSTOMER INFO (ADMIN)---------
    public static boolean updateCustomerAsAdmin(int customerId,
                                                String username,
                                                String fullName,
                                                String address,
                                                String password) {
        String sql = "UPDATE customers " +
                "SET username = ?, full_name = ?, address = ?, password = ? " +
                "WHERE customer_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, fullName);
            ps.setString(3, address);
            ps.setString(4, password);
            ps.setInt(5, customerId);

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    //---------GET CUSTOMER DETIALS(ADMIN)---------
    public static CustomerDetails getCustomerDetailsForAdmin(int customerId) {
        String sql = "SELECT customer_id, username, full_name, address, password " +
                "FROM customers WHERE customer_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CustomerDetails d = new CustomerDetails();
                    d.customerId = rs.getInt("customer_id");
                    d.username = rs.getString("username");
                    d.fullName = rs.getString("full_name");
                    d.address = rs.getString("address");
                    d.password = rs.getString("password");
                    return d;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    //---------UPDATE EMPLOYEES---------
    public static boolean updateEmployee(int employeeId,
                                         String username,
                                         String fullName,
                                         String password) {
        String sql = "UPDATE employees SET username = ?, full_name = ?, password = ? " +
                "WHERE employee_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, fullName);
            ps.setString(3, password);
            ps.setInt(4, employeeId);

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }





}
