
## Online Store Database Application (Solo Project)
Java Swing + MySQL application implementing a role-based online store system with separate Customer, Employee, and Admin workflows.

### Features
**Customer**
- Browse items and view item details
- Add/remove items from cart
- Place orders, view order history, cancel orders
- Update account information

**Employee**
- Manage store items (add/update/remove)
- View customer accounts and orders

**Admin**
- Manage employee and customer accounts
- Control order lifecycle/status updates

### Tech Stack
- Java (IntelliJ) + Swing (GUI)
- MySQL (`online_store_db`)
- SQL queries for CRUD operations and role-based workflows

### Database Design
- Normalized relational schema supporting products/items, users/roles, orders, and order_items
- Supports multi-role access and order lifecycle management

### How to Run
1. Clone the repo
2. Create the database in MySQL:
   - Create schema: `online_store_db`
   - Run the SQL scripts in `/sql` (if included) or follow the schema steps below
3. Update DB credentials in the code:
   - `[point to the exact file/class where connection settings live]`
4. Run the project in IntelliJ:
   - Start from `[your main class name]`

### Notes
- This project was completed as a solo final project for a Database Systems course.
- If you’d like, you can add screenshots in an `/assets` folder and link them here.
