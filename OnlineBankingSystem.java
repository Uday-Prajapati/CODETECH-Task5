/*  TASK FIVE: ONLINE BANKING SYSTEM
Create a comprehensive Java program for an online banking system. This system should  allow users to create accounts, 
deposit and withdraw funds, transfer money between accounts, view transaction history, and manage personal information. */

/* MySQL CODE:
-- Create the database
CREATE DATABASE IF NOT EXISTS banking;

-- Use the database
USE banking;

-- Create the users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    balance DOUBLE DEFAULT 0.0
);

-- Create the transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    amount DOUBLE NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

select*from users;
select*from transactions; 
*/
/* ------------------------------------------------------------------------------------------------------------------------------- */
/* Java Code */
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class OnlineBankingSystem {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/banking";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Uday@2609";

    private JFrame frame;
    private JTextField emailField;
    private JPasswordField passwordField;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new OnlineBankingSystem().initialize();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void initialize() {
        frame = new JFrame("Online Banking System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        JButton createAccountButton = new JButton("Create Account");

        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(createAccountButton);

        frame.add(panel, BorderLayout.CENTER);

        loginButton.addActionListener(e -> loginAction());
        createAccountButton.addActionListener(e -> createAccountAction());

        frame.setVisible(true);
    }

    private void loginAction() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String findUserQuery = "SELECT id, name FROM users WHERE email = ? AND password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(findUserQuery)) {
                preparedStatement.setString(1, email);
                preparedStatement.setString(2, password);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    int userId = resultSet.getInt("id");
                    String userName = resultSet.getString("name");
                    JOptionPane.showMessageDialog(frame, "Welcome, " + userName + "!");
                    openUserMenu(userId);
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid email or password.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Database connection error.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createAccountAction() {
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
    
        Object[] fields = {
            "Name:", nameField,
            "Email:", emailField,
            "Password:", passwordField
        };
    
        int option = JOptionPane.showConfirmDialog(frame, fields, "Create Account", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
    
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                // Check if email already exists
                String checkEmailQuery = "SELECT id FROM users WHERE email = ?";
                try (PreparedStatement checkEmailStmt = connection.prepareStatement(checkEmailQuery)) {
                    checkEmailStmt.setString(1, email);
                    ResultSet resultSet = checkEmailStmt.executeQuery();
    
                    if (resultSet.next()) {
                        JOptionPane.showMessageDialog(frame, "Email already registered. Please use a different email.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
    
                // Insert new user
                String insertUserQuery = "INSERT INTO users (name, email, password, balance) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertUserStmt = connection.prepareStatement(insertUserQuery)) {
                    insertUserStmt.setString(1, name);
                    insertUserStmt.setString(2, email);
                    insertUserStmt.setString(3, password);
                    insertUserStmt.setDouble(4, 0.0); // Initial balance set to 0
                    insertUserStmt.executeUpdate();
    
                    JOptionPane.showMessageDialog(frame, "Account created successfully! You can now log in.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error creating account. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void openUserMenu(int userId) {
        JFrame userFrame = new JFrame("User Menu");
        userFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        userFrame.setSize(400, 300);
        userFrame.setLayout(new GridLayout(5, 1));

        JButton depositButton = new JButton("Deposit Funds");
        JButton withdrawButton = new JButton("Withdraw Funds");
        JButton transferButton = new JButton("Transfer Funds");
        JButton historyButton = new JButton("View Transaction History");
        JButton logoutButton = new JButton("Logout");

        userFrame.add(depositButton);
        userFrame.add(withdrawButton);
        userFrame.add(transferButton);
        userFrame.add(historyButton);
        userFrame.add(logoutButton);

        depositButton.addActionListener(e -> depositFunds(userId));
        withdrawButton.addActionListener(e -> withdrawFunds(userId));
        transferButton.addActionListener(e -> transferFunds(userId));
        historyButton.addActionListener(e -> viewTransactionHistory(userId));
        logoutButton.addActionListener(e -> userFrame.dispose());

        userFrame.setVisible(true);
    }

    private void depositFunds(int userId) {
        String amountStr = JOptionPane.showInputDialog(frame, "Enter amount to deposit:");
        if (amountStr != null && !amountStr.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount > 0) {
                    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                        String updateBalanceQuery = "UPDATE users SET balance = balance + ? WHERE id = ?";
                        try (PreparedStatement preparedStatement = connection.prepareStatement(updateBalanceQuery)) {
                            preparedStatement.setDouble(1, amount);
                            preparedStatement.setInt(2, userId);
                            preparedStatement.executeUpdate();
                        }
                        recordTransaction(userId, "Deposit", amount);
                        JOptionPane.showMessageDialog(frame, "Deposit successful!");
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Amount must be greater than zero.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException | SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Invalid amount or database error.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void withdrawFunds(int userId) {
        String amountStr = JOptionPane.showInputDialog(frame, "Enter amount to withdraw:");
        if (amountStr != null && !amountStr.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount > 0) {
                    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                        String checkBalanceQuery = "SELECT balance FROM users WHERE id = ?";
                        try (PreparedStatement checkBalanceStmt = connection.prepareStatement(checkBalanceQuery)) {
                            checkBalanceStmt.setInt(1, userId);
                            ResultSet resultSet = checkBalanceStmt.executeQuery();

                            if (resultSet.next() && resultSet.getDouble("balance") >= amount) {
                                String updateBalanceQuery = "UPDATE users SET balance = balance - ? WHERE id = ?";
                                try (PreparedStatement preparedStatement = connection.prepareStatement(updateBalanceQuery)) {
                                    preparedStatement.setDouble(1, amount);
                                    preparedStatement.setInt(2, userId);
                                    preparedStatement.executeUpdate();
                                }
                                recordTransaction(userId, "Withdrawal", amount);
                                JOptionPane.showMessageDialog(frame, "Withdrawal successful!");
                            } else {
                                JOptionPane.showMessageDialog(frame, "Insufficient balance.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Amount must be greater than zero.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException | SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Invalid amount or database error.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void transferFunds(int userId) {
        JTextField recipientField = new JTextField();
        JTextField amountField = new JTextField();

        Object[] fields = {
            "Recipient Email:", recipientField,
            "Amount:", amountField
        };

        int option = JOptionPane.showConfirmDialog(frame, fields, "Transfer Funds", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String recipientEmail = recipientField.getText();
            String amountStr = amountField.getText();

            try {
                double amount = Double.parseDouble(amountStr);
                if (amount > 0) {
                    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                        String findRecipientQuery = "SELECT id FROM users WHERE email = ?";
                        try (PreparedStatement preparedStatement = connection.prepareStatement(findRecipientQuery)) {
                            preparedStatement.setString(1, recipientEmail);
                            ResultSet resultSet = preparedStatement.executeQuery();

                            if (resultSet.next()) {
                                int recipientId = resultSet.getInt("id");

                                String checkBalanceQuery = "SELECT balance FROM users WHERE id = ?";
                                try (PreparedStatement checkBalanceStmt = connection.prepareStatement(checkBalanceQuery)) {
                                    checkBalanceStmt.setInt(1, userId);
                                    ResultSet balanceResult = checkBalanceStmt.executeQuery();

                                    if (balanceResult.next() && balanceResult.getDouble("balance") >= amount) {
                                        connection.setAutoCommit(false);

                                        String debitQuery = "UPDATE users SET balance = balance - ? WHERE id = ?";
                                        try (PreparedStatement debitStmt = connection.prepareStatement(debitQuery)) {
                                            debitStmt.setDouble(1, amount);
                                            debitStmt.setInt(2, userId);
                                            debitStmt.executeUpdate();
                                        }

                                        String creditQuery = "UPDATE users SET balance = balance + ? WHERE id = ?";
                                        try (PreparedStatement creditStmt = connection.prepareStatement(creditQuery)) {
                                            creditStmt.setDouble(1, amount);
                                            creditStmt.setInt(2, recipientId);
                                            creditStmt.executeUpdate();
                                        }

                                        recordTransaction(userId, "Transfer to " + recipientEmail, amount);
                                        recordTransaction(recipientId, "Transfer from userId: " + userId, amount);

                                        connection.commit();
                                        JOptionPane.showMessageDialog(frame, "Transfer successful!");
                                    } else {
                                        JOptionPane.showMessageDialog(frame, "Insufficient balance.", "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                } catch (SQLException e) {
                                    connection.rollback();
                                    e.printStackTrace();
                                } finally {
                                    connection.setAutoCommit(true);
                                }
                            } else {
                                JOptionPane.showMessageDialog(frame, "Recipient not found.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Amount must be greater than zero.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException | SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Invalid input or database error.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewTransactionHistory(int userId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String transactionHistoryQuery = "SELECT type, amount, timestamp FROM transactions WHERE user_id = ? ORDER BY timestamp DESC";
            try (PreparedStatement preparedStatement = connection.prepareStatement(transactionHistoryQuery)) {
                preparedStatement.setInt(1, userId);
                ResultSet resultSet = preparedStatement.executeQuery();

                StringBuilder history = new StringBuilder("--- Transaction History ---\n");
                while (resultSet.next()) {
                    String type = resultSet.getString("type");
                    double amount = resultSet.getDouble("amount");
                    Timestamp timestamp = resultSet.getTimestamp("timestamp");
                    history.append(String.format("%s | Amount: %.2f | Date: %s\n", type, amount, timestamp));
                }

                JOptionPane.showMessageDialog(frame, history.toString(), "Transaction History", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error retrieving transaction history.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recordTransaction(int userId, String type, double amount) throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String insertTransactionQuery = "INSERT INTO transactions (user_id, type, amount) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertTransactionQuery)) {
                preparedStatement.setInt(1, userId);
                preparedStatement.setString(2, type);
                preparedStatement.setDouble(3, amount);
                preparedStatement.executeUpdate();
            }
        }
    }
}

