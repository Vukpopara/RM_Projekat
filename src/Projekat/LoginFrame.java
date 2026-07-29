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
        setSize(380, 230);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centriranje prozora na ekranu
        setResizable(false);

        network = new ClientNetwork();
        boolean connected = network.connect("localhost", 8080);

        if (!connected) {
            JOptionPane.showMessageDialog(this,
                    "Nije moguće povezivanje sa serverom! Provjerite da li je server pokrenut.",
                    "Greška konekcije",
                    JOptionPane.ERROR_MESSAGE);
        }

        network.setOnMessageReceived(this::handleServerResponse);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Korisničko ime:"), gbc);

        txtUsername = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Lozinka:"), gbc);

        txtPassword = new JPasswordField(15);
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(txtPassword, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnLogin = new JButton("Prijava");
        btnRegister = new JButton("Registracija");

        btnPanel.add(btnRegister);
        btnPanel.add(btnLogin);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

        add(panel);

        btnLogin.addActionListener(e -> performLogin());
        btnRegister.addActionListener(e -> performRegister());
    }

    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Popunite sva polja!", "Upozorenje", JOptionPane.WARNING_MESSAGE);
            return;
        }

        network.sendCommand("LOGIN " + username + " " + password);
    }

    private void performRegister() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Popunite sva polja!", "Upozorenje", JOptionPane.WARNING_MESSAGE);
            return;
        }

        network.sendCommand("REGISTER " + username + " " + password);
    }

    private void handleServerResponse(String response) {
        SwingUtilities.invokeLater(() -> {
            if (response.startsWith("LOGIN_SUCCESS") || response.startsWith("OK_LOGIN")) {
                JOptionPane.showMessageDialog(this, "Uspješna prijava!", "Info", JOptionPane.INFORMATION_MESSAGE);
            } else if (response.startsWith("REGISTER_SUCCESS") || response.startsWith("OK_REGISTER")) {
                JOptionPane.showMessageDialog(this, "Registracija uspješna! Možete se prijaviti.", "Info", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, response, "Odgovor servera", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}