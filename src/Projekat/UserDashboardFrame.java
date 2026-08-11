package Projekat;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class UserDashboardFrame extends JFrame {

    private String username;
    private ClientNetwork network;

    private JTextArea txtChatArea;
    private JTextField txtMessage;
    private JButton btnSendMessage;
    private JComboBox<String> cbChannels;
    private JButton btnJoinChannel;
    private JButton btnLeaveChannel;
    private JButton btnCreateChannel;

    private JTextField txtTicketIssue;
    private JButton btnSendTicket;

    private boolean isJoinedToChannel = false;

    private Timer typingTimer;
    private boolean trenutnoKuca = false;

    public UserDashboardFrame(String username, ClientNetwork network) {

        this.username = username;
        this.network = network;

        setTitle("Ticketing Sistem - Korisnički Panel (" + username + ")");
        setSize(750, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        if (this.network != null) {
            this.network.setOnMessageReceived(this::handleServerResponse);
        }

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        topPanel.add(new JLabel("Kanal:"));

        cbChannels = new JComboBox<>(
                new String[]{
                        "Programiranje",
                        "Dizajn",
                        "Opšte teme"
                }
        );
        cbChannels.setEditable(false);

        topPanel.add(cbChannels);

        btnJoinChannel = new JButton("Uđi u kanal");
        topPanel.add(btnJoinChannel);

        btnLeaveChannel = new JButton("Napusti kanal");
        topPanel.add(btnLeaveChannel);

        btnCreateChannel = new JButton("Kreiraj kanal");
        topPanel.add(btnCreateChannel);

        txtChatArea = new JTextArea();
        txtChatArea.setEditable(false);
        txtChatArea.setFont(
                new Font("Monospaced", Font.PLAIN, 12)
        );

        JScrollPane scrollPane = new JScrollPane(txtChatArea);

        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel chatInputPanel = new JPanel(new BorderLayout(5, 5));
        txtMessage = new JTextField();
        btnSendMessage = new JButton("Pošalji");

        typingTimer = new Timer(1500, e -> {
            trenutnoKuca = false;
        });
        typingTimer.setRepeats(false);

        txtMessage.getDocument().addDocumentListener(new DocumentListener() {

            private void typing() {
                if (network == null) {
                    return;
                }

                if (!txtMessage.getText().trim().isEmpty()) {
                    if (!trenutnoKuca) {
                        network.sendCommand("KUCANJE:START");
                        trenutnoKuca = true;
                    }
                    typingTimer.restart();
                } else {
                    trenutnoKuca = false;
                    typingTimer.stop();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                typing();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                typing();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                typing();
            }
        });

        chatInputPanel.add(new JLabel("Poruka: "), BorderLayout.WEST);
        chatInputPanel.add(txtMessage, BorderLayout.CENTER);
        chatInputPanel.add(btnSendMessage, BorderLayout.EAST);

        txtMessage.addActionListener(e -> sendMessage());
        btnSendMessage.addActionListener(e -> sendMessage());

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

        btnJoinChannel.addActionListener(e -> joinChannel());
        btnLeaveChannel.addActionListener(e -> leaveChannel());
        btnCreateChannel.addActionListener(e -> createChannel());
        btnSendTicket.addActionListener(e -> sendTicket());
    }

    private void createChannel() {
        String noviKanal = JOptionPane.showInputDialog(
                this,
                "Unesite naziv novog kanala:",
                "Kreiranje kanala",
                JOptionPane.PLAIN_MESSAGE
        );

        if (noviKanal != null && !noviKanal.trim().isEmpty()) {
            noviKanal = noviKanal.trim();

            boolean postoji = false;
            for (int i = 0; i < cbChannels.getItemCount(); i++) {
                if (cbChannels.getItemAt(i).equalsIgnoreCase(noviKanal)) {
                    postoji = true;
                    break;
                }
            }

            if (!postoji) {
                cbChannels.addItem(noviKanal);
            }

            cbChannels.setSelectedItem(noviKanal);

            if (network != null) {
                network.sendCommand("KREIRAJ_KANAL:" + noviKanal);
            }
        }
    }

    private void joinChannel() {
        String izabraniKanal = (String) cbChannels.getSelectedItem();

        if (izabraniKanal != null && network != null) {
            network.sendCommand("KANAL:" + izabraniKanal);
            isJoinedToChannel = true;
        }
    }

    private void leaveChannel() {
        if (!isJoinedToChannel) {
            return;
        }

        if (network != null) {
            network.sendCommand("/izadji");
            isJoinedToChannel = false;
        }
    }

    private void sendMessage() {
        String msg = txtMessage.getText().trim();

        if (msg.isEmpty()) {
            return;
        }

        if (network != null) {
            network.sendCommand(msg);
            txtMessage.setText("");
            trenutnoKuca = false;
            if (typingTimer != null) {
                typingTimer.stop();
            }
        }
    }

    private void sendTicket() {
        if (!isJoinedToChannel) {
            JOptionPane.showMessageDialog(
                    this,
                    "Morate prvo izabrati kanal iz padajućeg menija i kliknuti 'Uđi u kanal'!",
                    "Greška",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String issue = txtTicketIssue.getText().trim();

        if (issue.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unesite opis problema za tiket.",
                    "Upozorenje",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (network != null) {
            network.sendCommand("OTVORI_TIKET:" + issue);
            txtTicketIssue.setText("");
        }
    }

    private void handleServerResponse(String response) {
        SwingUtilities.invokeLater(() -> {
            txtChatArea.append(response + "\n");
        });
    }
}