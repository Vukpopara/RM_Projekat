package Projekat;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private Room currentRoom;
    private boolean admin = false;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public String getUsername() {
        return username;
    }

    public void sendMessage(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            while (username == null) {
                String initialMessage = in.readLine();
                if (initialMessage == null) {
                    socket.close();
                    return;
                }

                if (initialMessage.startsWith("REGISTER ")) {
                    String[] dijelovi = initialMessage.split(" ", 3);
                    if (dijelovi.length == 3) {
                        String u = dijelovi[1];
                        String p = dijelovi[2];

                        if (ProvjeraKorisnika.postojiKorisnik(u)) {
                            sendMessage("Korisnik je već registrovan pod ovim imenom.");
                        } else {
                            boolean uspjeh = ProvjeraKorisnika.registrujKorisnika(u, p);
                            if (uspjeh) {
                                sendMessage("Uspješna registracija! Sada se možete prijaviti.");
                            } else {
                                sendMessage("Greška prilikom čuvanja korisnika.");
                            }
                        }
                    } else {
                        sendMessage("Neispravan format registracije.");
                    }
                    continue;
                }

                if (initialMessage.startsWith("LOGIN ")) {
                    String[] dijelovi = initialMessage.split(" ", 3);
                    if (dijelovi.length == 3) {
                        String u = dijelovi[1];
                        String p = dijelovi[2];

                        if (!ProvjeraKorisnika.postojiKorisnik(u)) {
                            sendMessage("Korisnik nije registrovan.");
                        } else if (!ProvjeraKorisnika.ispravnaPrijava(u, p)) {
                            sendMessage("Pogrešna lozinka.");
                        } else {
                            this.username = u;

                            if (ProvjeraKorisnika.isAdmin(u)) {
                                this.admin = true;
                                sendMessage("Uspjesna prijava (ADMIN)! Dobrodosao " + username);
                            } else {
                                this.admin = false;
                                sendMessage("Uspjesna prijava! Dobrodosao " + username);
                            }
                            break;
                        }
                    } else {
                        sendMessage("Neispravan format prijave.");
                    }
                    continue;
                }

                sendMessage("Molimo prijavite se ili registrujte.");
            }

            String message;
            while ((message = in.readLine()) != null) {

                if (message.equals("/izlaz")) {
                    break;
                }

                if (message.equals("KUCANJE:START")) {
                    if (currentRoom != null) {
                        currentRoom.broadcast(
                                username + " kuca..."
                        );
                    }
                    continue;
                }

                if (message.startsWith("KREIRAJ_KANAL:")) {
                    String naziv = message.substring(14).trim();
                    if (!naziv.isEmpty()) {
                        Server.sobe.computeIfAbsent(naziv, Room::new);
                        sendMessage("[SISTEM] Kanal '" + naziv + "' je uspješno kreiran.");
                    } else {
                        sendMessage("[SISTEM] Naziv kanala ne može biti prazan.");
                    }
                    continue;
                }

                if (message.equals("GET_KANALI")) {
                    StringBuilder sb = new StringBuilder("KANALI_LISTA:");
                    for (String imeKanala : Server.sobe.keySet()) {
                        sb.append(imeKanala).append(",");
                    }
                    sendMessage(sb.toString());
                    continue;
                }

                if (message.startsWith("KANAL:")) {
                    String param = message.substring(6).trim();
                    String nazivSobe = "";

                    if (param.equals("1") || param.equalsIgnoreCase("Programiranje")) {
                        nazivSobe = "Programiranje";
                    } else if (param.equals("2") || param.equalsIgnoreCase("Dizajn")) {
                        nazivSobe = "Dizajn";
                    } else if (param.equals("3") || param.equalsIgnoreCase("Opšte teme")) {
                        nazivSobe = "Opšte teme";
                    } else {
                        nazivSobe = param;
                    }

                    if (currentRoom != null) {
                        currentRoom.broadcast("[SISTEM] Korisnik " + username + " je napustio kanal: " + currentRoom.getName());
                        currentRoom.removeClient(this);
                    }

                    Room room = Server.sobe.computeIfAbsent(nazivSobe, Room::new);
                    currentRoom = room;
                    currentRoom.addClient(this);

                    currentRoom.broadcast("[SISTEM] Korisnik " + username + " je ušao u kanal: " + currentRoom.getName());
                    continue;
                }

                if (message.equals("/izadji")) {
                    if (currentRoom != null) {
                        String imeStarog = currentRoom.getName();
                        currentRoom.broadcast("[SISTEM] Korisnik " + username + " je napustio kanal: " + imeStarog);
                        currentRoom.removeClient(this);
                        currentRoom = null;
                    } else {
                        sendMessage("[SISTEM] Niste ni u jednom kanalu.");
                    }
                    continue;
                }

                if (message.startsWith("NOVI_TIKET:") || message.startsWith("OTVORI_TIKET:")) {
                    String opis = "";
                    if (message.startsWith("NOVI_TIKET:")) {
                        String[] d = message.split(":", 3);
                        opis = (d.length == 3) ? d[2] : message;
                    } else {
                        opis = message.substring(14);
                    }

                    Ticket ticket = new Ticket(Server.tickets.size() + 1, username, opis);
                    Server.tickets.add(ticket);
                    sendMessage("[SISTEM] Tiket je kreiran sa ID brojem: " + ticket.getId());
                    continue;
                }

                if (message.equals("GET_KORISNICI")) {
                    StringBuilder lista = new StringBuilder("Aktivni korisnici: ");
                    for (ClientHandler client : Server.clients) {
                        lista.append(client.username).append(" ");
                    }
                    sendMessage(lista.toString());
                    continue;
                }

                if (message.equals("GET_TIKETI")) {
                    if (!admin) {
                        sendMessage("Nemate administratorska prava.");
                        continue;
                    }
                    if (Server.tickets.isEmpty()) {
                        sendMessage("Nema aktivnih tiketa u sistemu.");
                        continue;
                    }

                    for (Ticket ticket : Server.tickets) {
                        String status = ticket.isZatvoren() ? "ZATVOREN" : "OTVOREN";
                        sendMessage("ID: " + ticket.getId() + " | Korisnik: " + ticket.getKorisnik() + " | Opis: " + ticket.getOpis() + " | Status: " + status + " | Odgovori: " + ticket.getSviOdgovoriFormatirano());
                    }
                    continue;
                }

                if (message.startsWith("PREUZMI_TIKET:")) {
                    if (!admin) {
                        sendMessage("Nemate administratorska prava.");
                        continue;
                    }

                    try {
                        int id = Integer.parseInt(message.substring(15).trim());
                        boolean pronadjen = false;

                        for (Ticket ticket : Server.tickets) {
                            if (ticket.getId() == id) {
                                ticket.dodajAdministratora(username);
                                sendMessage("[ADMIN] Uspešno ste preuzeli tiket ID: " + id);
                                pronadjen = true;
                                break;
                            }
                        }

                        if (!pronadjen) {
                            sendMessage("Tiket sa zadatim ID-om nije pronađen.");
                        }
                    } catch (NumberFormatException e) {
                        sendMessage("Neispravan ID tiketa.");
                    }
                    continue;
                }

                if (message.startsWith("ADMIN_ODGOVOR:")) {
                    if (!admin) {
                        sendMessage("Nemate administratorska prava.");
                        continue;
                    }

                    String[] dijelovi = message.split(":", 3);
                    if (dijelovi.length == 3) {
                        try {
                            int id = Integer.parseInt(dijelovi[1].trim());
                            String odgovor = dijelovi[2].trim();
                            boolean pronadjen = false;

                            for (Ticket ticket : Server.tickets) {
                                if (ticket.getId() == id) {
                                    pronadjen = true;
                                    ticket.dodajOdgovor(odgovor);
                                    ticket.osvjeziAktivnost();

                                    for (ClientHandler client : Server.clients) {
                                        if (client.username.equalsIgnoreCase(ticket.getKorisnik())) {
                                            client.sendMessage("[ODGOVOR NA TIKET #" + id + "]: " + odgovor);
                                        }
                                    }
                                    sendMessage("[SISTEM] Odgovor uspješno poslat korisniku " + ticket.getKorisnik());
                                    break;
                                }
                            }

                            if (!pronadjen) {
                                sendMessage("Tiket sa ID " + id + " nije pronađen.");
                            }

                        } catch (NumberFormatException e) {
                            sendMessage("Neispravan ID tiketa.");
                        }
                    } else {
                        sendMessage("Neispravan format odgovora na tiket.");
                    }
                    continue;
                }

                if (message.startsWith("ZATVORI_TIKET:")) {
                    if (!admin) {
                        sendMessage("Nemate administratorska prava.");
                        continue;
                    }

                    try {
                        int id = Integer.parseInt(message.substring(14).trim());
                        boolean pronadjen = false;

                        for (Ticket ticket : Server.tickets) {
                            if (ticket.getId() == id) {
                                ticket.zatvori();
                                ticket.osvjeziAktivnost();

                                String imeFajla = "izvjestaj_tiket_" + id + ".txt";
                                try (BufferedWriter writer = new BufferedWriter(new FileWriter(imeFajla))) {
                                    writer.write("=== IZVJEŠTAJ O ZATVORENOM TIKETU ===");
                                    writer.newLine();
                                    writer.write("ID Tiketa: #" + ticket.getId());
                                    writer.newLine();
                                    writer.write("Korisnik: " + ticket.getKorisnik());
                                    writer.newLine();
                                    writer.write("Dodijeljeni Admin: " + ticket.getDodijeljeniAdmin());
                                    writer.newLine();
                                    writer.write("Opis problema: " + ticket.getOpis());
                                    writer.newLine();
                                    writer.write("------------------------------------");
                                    writer.newLine();
                                    writer.write("ISTORIJA ODGOVORA:");
                                    writer.write(ticket.getSviOdgovoriFormatirano());
                                    writer.newLine();
                                    writer.write("------------------------------------");
                                    writer.newLine();
                                    writer.write("Status: ZATVOREN");
                                    writer.newLine();
                                } catch (IOException e) {
                                    System.out.println("Greška pri upisu izvještaja: " + e.getMessage());
                                }
                                sendMessage("[ADMIN] Tiket ID #" + id + " je zatvoren i izvještaj je sačuvan (" + imeFajla + ").");

                                for (ClientHandler client : Server.clients) {
                                    if (client.username.equalsIgnoreCase(ticket.getKorisnik())) {
                                        client.sendMessage("[SISTEM] Vaš tiket #" + id + " je zatvoren. Generisan je izvještaj.");
                                    }
                                }

                                pronadjen = true;
                                break;
                            }
                        }

                        if (!pronadjen) {
                            sendMessage("Tiket sa ID " + id + " nije pronađen.");
                        }
                    } catch (NumberFormatException e) {
                        sendMessage("Neispravan ID tiketa.");
                    }
                    continue;
                }

                if (currentRoom == null) {
                    sendMessage("Prvo izaberite kanal.");
                } else {
                    currentRoom.broadcast("[" + currentRoom.getName() + "] " + username + ": " + message);
                }
            }
        } catch (IOException e) {
            System.out.println("Korisnik odspojen: " + username);
        } finally {
            if (currentRoom != null) {
                currentRoom.broadcast("[SISTEM] Korisnik " + username + " je napustio kanal: " + currentRoom.getName());
                currentRoom.removeClient(this);
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}