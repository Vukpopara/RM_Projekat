package Projekat;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegister;
    private ClientNetwork network;

    public LoginFrame() {
        setTitle("Ticketing Sistem - Prijava");
        setSize(350, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        network = new ClientNetwork();
        network.setOnMessageReceived(this::handleServerResponse);
        network.connect("localhost", 8080);

        JPanel mainPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        mainPanel.add(new JLabel("Korisničko ime:"));
        txtUsername = new JTextField();
        mainPanel.add(txtUsername);

        mainPanel.add(new JLabel("Lozinka:"));
        txtPassword = new JPasswordField();
        mainPanel.add(txtPassword);

        btnLogin = new JButton("Prijava");
        btnRegister = new JButton("Registracija");
        mainPanel.add(btnLogin);
        mainPanel.add(btnRegister);

        add(mainPanel);

        btnLogin.addActionListener(e -> performLogin());
        btnRegister.addActionListener(e -> performRegister());
    }

    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Molimo unesite korisničko ime i lozinku.", "Greška", JOptionPane.WARNING_MESSAGE);
            return;
        }

        network.sendCommand("LOGIN " + username + " " + password);
    }

    private void performRegister() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Molimo unesite korisničko ime i lozinku.", "Greška", JOptionPane.WARNING_MESSAGE);
            return;
        }

        network.sendCommand("REGISTER " + username + " " + password);
    }

    private void handleServerResponse(String response) {
        System.out.println("[SERVER ODGOVOR NA LOGIN]: " + response);

        SwingUtilities.invokeLater(() -> {
            String lowerRes = response.toLowerCase();

            if (lowerRes.contains("admin")) {
                JOptionPane.showMessageDialog(this, "Uspješna prijava (ADMIN)!", "Info", JOptionPane.INFORMATION_MESSAGE);

                String username = txtUsername.getText().trim();

                AdminDashboardFrame adminFrame = new AdminDashboardFrame(username, network);
                adminFrame.setVisible(true);
                this.dispose();

            } else if (lowerRes.contains("dobrodosao") || lowerRes.contains("uspesno") || lowerRes.contains("uspjesna prijava")) {
                JOptionPane.showMessageDialog(this, "Uspješna prijava!", "Info", JOptionPane.INFORMATION_MESSAGE);

                String username = txtUsername.getText().trim();

                UserDashboardFrame dashboard = new UserDashboardFrame(username, network);
                dashboard.setVisible(true);
                this.dispose();

            } else {
                JOptionPane.showMessageDialog(this, response, "Obavještenje", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}