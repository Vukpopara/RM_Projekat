package Projekat;

import javax.swing.*;
import java.awt.*;

public class AdminDashboardFrame extends JFrame {

    private String username;
    private ClientNetwork network;

    private JTextArea txtTicketsArea;
    private JTextField txtTicketId;
    private JTextField txtReply;
    private JButton btnSendReply;
    private JButton btnCloseTicket;
    private JButton btnRefresh;

    public AdminDashboardFrame(String username, ClientNetwork network) {
        this.username = username;
        this.network = network;

        setTitle("Ticketing Sistem - Admin Panel (" + username + ")");
        setSize(750, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        if (this.network != null) {
            this.network.setOnMessageReceived(this::handleServerResponse);
        }

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Pristigli tiketi i sistemske poruke:"));
        btnRefresh = new JButton("Osvježi listu");
        topPanel.add(btnRefresh);

        txtTicketsArea = new JTextArea();
        txtTicketsArea.setEditable(false);
        txtTicketsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(txtTicketsArea);

        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Upravljanje tiketima"));

        JPanel replyInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        txtTicketId = new JTextField(5);
        txtReply = new JTextField(20);
        btnSendReply = new JButton("Pošalji odgovor");
        btnCloseTicket = new JButton("Zatvori tiket");

        replyInputPanel.add(new JLabel("ID Tiketa:"));
        replyInputPanel.add(txtTicketId);
        replyInputPanel.add(new JLabel("Odgovor:"));
        replyInputPanel.add(txtReply);
        replyInputPanel.add(btnSendReply);
        replyInputPanel.add(btnCloseTicket);

        bottomPanel.add(replyInputPanel);

        setLayout(new BorderLayout(5, 5));
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        btnSendReply.addActionListener(e -> sendAdminReply());
        btnCloseTicket.addActionListener(e -> closeTicket());
        btnRefresh.addActionListener(e -> refreshTickets());

        refreshTickets();
    }

    private void refreshTickets() {
        if (network != null) {
            network.sendCommand("GET_TIKETI");
        }
    }

    private void sendAdminReply() {
        String ticketId = txtTicketId.getText().trim();
        String reply = txtReply.getText().trim();

        if (ticketId.isEmpty() || reply.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Popunite i ID tiketa i tekst odgovora!",
                    "Upozorenje",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (network != null) {
            network.sendCommand("ADMIN_ODGOVOR:" + ticketId + ":" + reply);
            txtTicketsArea.append("[JA -> TIKET " + ticketId + "]: " + reply + "\n");
            txtTicketId.setText("");
            txtReply.setText("");
        }
    }

<<<<<<< HEAD

=======
>>>>>>> ee784ef8a8e78111a62a3c857b0bc720d7b91d7d
    private void closeTicket() {
        String ticketId = txtTicketId.getText().trim();

        if (ticketId.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unesite ID tiketa koji želite da zatvorite!",
                    "Upozorenje",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (network != null) {
            network.sendCommand("ZATVORI_TIKET:" + ticketId);
            txtTicketId.setText("");
            txtReply.setText("");
        }
    }

    private void handleServerResponse(String response) {
        SwingUtilities.invokeLater(() -> {
            txtTicketsArea.append(response + "\n");
        });
    }
}