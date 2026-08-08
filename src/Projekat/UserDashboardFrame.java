package Projekat;

import javax.swing.*;
import java.awt.*;

public class UserDashboardFrame extends JFrame {
    private String username;
    private ClientNetwork network;

    private JTextArea txtChatArea;
    private JTextField txtMessage;
    private JButton btnSend;
    private JComboBox<String> cbChannels;

    private JTextField txtTicketIssue;
    private JButton btnSendTicket;

    public UserDashboardFrame(String username, ClientNetwork network) {
        this.username = username;
        this.network = network;

        setTitle("Ticketing Sistem - Korisnički Panel (" + username + ")");
        setSize(650, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        if (this.network != null) {
            this.network.setOnMessageReceived(this::handleServerResponse);
        }

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Kanal:"));
        cbChannels = new JComboBox<>(new String[]{"Programiranje", "Dizajn", "Opšte teme"});
        topPanel.add(cbChannels);

        txtChatArea = new JTextArea();
        txtChatArea.setEditable(false);
        txtChatArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(txtChatArea);

        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel chatInputPanel = new JPanel(new BorderLayout(5, 5));
        txtMessage = new JTextField();
        btnSend = new JButton("Pošalji poruku");
        chatInputPanel.add(new JLabel("Poruka: "), BorderLayout.WEST);
        chatInputPanel.add(txtMessage, BorderLayout.CENTER);
        chatInputPanel.add(btnSend, BorderLayout.EAST);

        JPanel ticketInputPanel = new JPanel(new BorderLayout(5, 5));
        txtTicketIssue = new JTextField();
        btnSendTicket = new JButton("Pošalji tiket");
        ticketInputPanel.add(new JLabel("Problem: "), BorderLayout.WEST);
        ticketInputPanel.add(txtTicketIssue, BorderLayout.CENTER);
        ticketInputPanel.add(btnSendTicket, BorderLayout.EAST);

        bottomPanel.add(chatInputPanel);
        bottomPanel.add(ticketInputPanel);

        setLayout(new BorderLayout(5, 5));
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        btnSend.addActionListener(e -> sendMessage());
        btnSendTicket.addActionListener(e -> sendTicket());
    }

    private void sendMessage() {
        String msg = txtMessage.getText().trim();
        if (!msg.isEmpty() && network != null) {
            String selectedChannel = (String) cbChannels.getSelectedItem();
            network.sendCommand("CHAT:" + selectedChannel + ":" + msg);
            txtMessage.setText("");
        }
    }

    private void sendTicket() {
        String issue = txtTicketIssue.getText().trim();
        if (!issue.isEmpty() && network != null) {
            network.sendCommand("TIKET:" + issue);
            txtTicketIssue.setText("");
            JOptionPane.showMessageDialog(this, "Tiket je uspješno poslat podršci!");
        } else {
            JOptionPane.showMessageDialog(this, "Unesite opis problema za tiket.");
        }
    }

    private void handleServerResponse(String response) {
        SwingUtilities.invokeLater(() -> {
            txtChatArea.append(response + "\n");
        });
    }
}