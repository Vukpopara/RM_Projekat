package Projekat;

import javax.swing.*;
import java.awt.*;

public class UserDashboardFrame extends JFrame {
    private String username;
    private ClientNetwork network;

    private JTextArea txtChatArea;
    private JTextField txtMessage;
    private JButton btnSend;
    private JComboBox<String> comboChannels;
    private JButton btnSelectChannel;

    public UserDashboardFrame(String username, ClientNetwork network) {
        this.username = username;
        this.network = network;

        setTitle("Ticketing Sistem - Korisnički Panel (" + username + ")");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        if (this.network != null) {
            this.network.setOnMessageReceived(this::handleServerResponse);
        }

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.add(new JLabel("Izaberite kanal:"));

        comboChannels = new JComboBox<>(new String[]{"1. Programiranje", "2. Dizajn", "3. Opšte teme"});
        topPanel.add(comboChannels);

        btnSelectChannel = new JButton("Pridruži se");
        topPanel.add(btnSelectChannel);

        txtChatArea = new JTextArea();
        txtChatArea.setEditable(false);
        txtChatArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(txtChatArea);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        txtMessage = new JTextField();
        btnSend = new JButton("Pošalji");

        bottomPanel.add(txtMessage, BorderLayout.CENTER);
        bottomPanel.add(btnSend, BorderLayout.EAST);

        setLayout(new BorderLayout(5, 5));
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        btnSelectChannel.addActionListener(e -> selectChannel());
        btnSend.addActionListener(e -> sendMessage());
        txtMessage.addActionListener(e -> sendMessage());
    }

    private void selectChannel() {
        int selectedIndex = comboChannels.getSelectedIndex();
        if (selectedIndex != -1) {
            String command = "KANAL:" + (selectedIndex + 1);
            if (network != null) {
                network.sendCommand(command);
            }
            txtChatArea.append(">>> Zahtjev za ulazak u kanal: " + comboChannels.getSelectedItem() + "\n");
        }
    }

    private void sendMessage() {
        String msg = txtMessage.getText().trim();
        if (!msg.isEmpty()) {
            if (network != null) {
                network.sendCommand(msg);
            }
            txtChatArea.append("Ja: " + msg + "\n");
            txtMessage.setText("");
        }
    }

    private void handleServerResponse(String response) {
        SwingUtilities.invokeLater(() -> {
            txtChatArea.append("Server: " + response + "\n");
        });
    }
}