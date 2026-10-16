# Inventory Management System

A Java Swing desktop application for managing products, suppliers, customers and sales from a single window.

> **University group project** (Team Loop): Introduction to Programming Language II (Java) Lab, Southeast University.
> This repository is my copy of the team project. See [Contributions](#team-and-contributions) for who built what.

![Java](https://img.shields.io/badge/Java-8%2B-orange) ![UI](https://img.shields.io/badge/UI-Swing-blue)

## Features

- Login gate with a 3-attempt limit and Change Password option
- Add, Update, Delete and live Search in every tab (Products, Suppliers, Customers, Sales)
- Dashboard: total inventory value, total revenue, low-stock count, record counts
- Low-stock alert: products with quantity below 5 are shown in red
- Sales automatically reduce stock and add customer loyalty points; editing or deleting a sale reverses those changes
- Input validation (empty fields, phone/email format, non-negative quantity/price)
- Auto-save/auto-load of data as CSV files, plus manual Save/Load from the File menu

## Screenshots

<!-- Add screenshots to a /screenshots folder, then uncomment: -->
<!-- ![Dashboard](screenshots/dashboard.png) -->
<!-- ![Suppliers](screenshots/suppliers.png) -->

## Getting Started

**Requirements:** JDK 8 or newer (tested with JDK 21).

```bash
git clone https://github.com/rimonabdullah-dev/inventory-management-system.git
cd inventory-management-system/src
javac *.java
java Main
```

Or open the project in IntelliJ IDEA, make sure all `.java` files are in the source root, and run `Main.java`.

**Default login:** `admin` / `admin123` (changeable from the Admin menu).

The app creates a `data/` folder in the directory it runs from. On the first run, a few sample records are added.

## Project Structure

The code follows a simple layered design: **Model → Manager → Panel (UI) → Main**.

| Module | Files |
|---|---|
| Product | `Product.java`, `ProductManager.java`, `ProductPanel.java` |
| Supplier | `Supplier.java`, `SupplierManager.java`, `SupplierPanel.java` |
| Customer | `Customer.java`, `CustomerManager.java`, `CustomerPanel.java` |
| Sales | `Sale.java`, `SalesManager.java`, `SalesPanel.java` |
| Entry point | `Main.java` |
| Shared | `Validator.java` (Validator, Manageable, Person), `Theme.java` (Theme, LoginDialog, DashboardPanel), `FileHandler.java` (FileHandler, AdminAccount) |

Files with more than one class use package-private classes, since Java allows only one public class per file.

## OOP Concepts Used

- **Encapsulation:** private fields with getters/setters in the models
- **Inheritance:** `Supplier` and `Customer` extend the abstract `Person`; panels extend `JPanel`
- **Polymorphism:** `getRole()` overridden in `Supplier` and `Customer`; all managers implement `Manageable<T>`
- **Abstraction:** abstract class `Person` and generic interface `Manageable<T>`
- **Other:** generics, collections (`ArrayList`, lambdas with `removeIf`), regex validation, exception handling, file I/O (CSV, `Properties`)

## Team and Contributions

| Member | Contribution |
|---|---|
| Shiarajam Monir Jamil | Product module, Theme, login dialog, Dashboard |
| **Abdullah Al Rimon Khan (Rimon)** | **Supplier module** (model, manager, Swing tab with form, table, search, validation) and shared foundation classes: abstract `Person`, generic `Manageable<T>`, `Validator` |
| Somaya Akter | Customer module, CSV save/load, admin account handling |
| Radoyan Ahmed | Sales module (cross-manager logic), main window, menu bar, start-up flow |

## Note on AI Assistance

I wrote the Supplier module and the shared foundation classes with the help of AI tools. I reviewed and studied the code, and I can explain how it works. I am continuing to improve it by adding features and fixing issues myself.

## Future Improvements

- Replace CSV storage with a database (SQLite/MySQL)
- Hash admin passwords
- Add reports and charts to the Dashboard

## License

Educational project. Add a license of your choice (e.g. MIT) if you plan to reuse it publicly.
