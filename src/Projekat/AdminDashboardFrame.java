package Projekat;

import javax.swing.*;
import java.awt.*;

public class AdminDashboardFrame extends JFrame {
    private String adminUsername;
    private ClientNetwork network;

    private JTextArea txtTicketsArea;
    private JTextField txtTicketId;
    private JTextField txtReplyMessage;
    private JButton btnRefresh;
    private JButton btnClaimTicket;
    private JButton btnSendReply;
    private JButton btnCloseTicket;

    public AdminDashboardFrame(String adminUsername, ClientNetwork network) {
        this.adminUsername = adminUsername;
        this.network = network;

        setTitle("Ticketing Sistem - Admin Panel (" + adminUsername + ")");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        if (this.network != null) {
            this.network.setOnMessageReceived(this::handleServerResponse);
        }

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnRefresh = new JButton("Osvježi listu tiketa");
        topPanel.add(btnRefresh);

        topPanel.add(new JLabel("ID Tiketa:"));
        txtTicketId = new JTextField(5);
        topPanel.add(txtTicketId);

        btnClaimTicket = new JButton("Preuzmi tiket");
        btnCloseTicket = new JButton("Zatvori tiket");
        topPanel.add(btnClaimTicket);
        topPanel.add(btnCloseTicket);

        txtTicketsArea = new JTextArea();
        txtTicketsArea.setEditable(false);
        txtTicketsArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(txtTicketsArea);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        txtReplyMessage = new JTextField();
        btnSendReply = new JButton("Pošalji odgovor");

        bottomPanel.add(new JLabel("Odgovor: "), BorderLayout.WEST);
        bottomPanel.add(txtReplyMessage, BorderLayout.CENTER);
        bottomPanel.add(btnSendReply, BorderLayout.EAST);

        setLayout(new BorderLayout(5, 5));
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> refreshTickets());
        btnClaimTicket.addActionListener(e -> claimTicket());
        btnSendReply.addActionListener(e -> sendReply());
        btnCloseTicket.addActionListener(e -> closeTicket());

        refreshTickets();
    }

    private void refreshTickets() {
        if (network != null) {
            network.sendCommand("GET_TIKETI");
        }
    }

    private void claimTicket() {
        String ticketId = txtTicketId.getText().trim();
        if (!ticketId.isEmpty() && network != null) {
            network.sendCommand("PREUZMI_TIKET:" + ticketId);
        } else {
            JOptionPane.showMessageDialog(this, "Unesite ID tiketa koji želite preuzeti.");
        }
    }

    private void sendReply() {
        String ticketId = txtTicketId.getText().trim();
        String reply = txtReplyMessage.getText().trim();
        if (!ticketId.isEmpty() && !reply.isEmpty() && network != null) {
            network.sendCommand("ADMIN_ODGOVOR:" + ticketId + ":" + reply);
            txtReplyMessage.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Unesite ID tiketa i tekst odgovora.");
        }
    }

    private void closeTicket() {
        String ticketId = txtTicketId.getText().trim();
        if (!ticketId.isEmpty() && network != null) {
            network.sendCommand("ZATVORI_TIKET:" + ticketId);
        } else {
            JOptionPane.showMessageDialog(this, "Unesite ID tiketa koji želite zatvoriti.");
        }
    }

    private void handleServerResponse(String response) {
        SwingUtilities.invokeLater(() -> {
            txtTicketsArea.append(response + "\n");
        });
    }
}