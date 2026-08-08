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
                            out.println("Korisnik je već registrovan pod ovim imenom.");
                        } else {
                            boolean uspjeh = ProvjeraKorisnika.registrujKorisnika(u, p);
                            if (uspjeh) {
                                out.println("Uspješna registracija! Sada se možete prijaviti.");
                            } else {
                                out.println("Greška prilikom čuvanja korisnika.");
                            }
                        }
                    } else {
                        out.println("Neispravan format registracije.");
                    }
                    continue;
                }

                if (initialMessage.startsWith("LOGIN ")) {
                    String[] dijelovi = initialMessage.split(" ", 3);
                    if (dijelovi.length == 3) {
                        String u = dijelovi[1];
                        String p = dijelovi[2];

                        if (!ProvjeraKorisnika.postojiKorisnik(u)) {
                            out.println("Korisnik nije registrovan.");
                        } else if (!ProvjeraKorisnika.ispravnaPrijava(u, p)) {
                            out.println("Pogrešna lozinka.");
                        } else {
                            this.username = u;

                            // PROVJERAVAMO DA LI JE KORISNIK ADMIN
                            if (u.toLowerCase().contains("admin")) {
                                this.admin = true;
                                out.println("Uspjesna prijava (ADMIN)! Dobrodosao " + username);
                            } else {
                                out.println("Uspjesna prijava! Dobrodosao " + username);
                            }
                            break;
                        }
                    } else {
                        out.println("Neispravan format prijave.");
                    }
                    continue;
                }

                out.println("Molimo prijavite se ili registrujte.");
            }

            String message;
            while ((message = in.readLine()) != null) {

                if (message.equals("/izlaz")) {
                    break;
                }


                if (message.startsWith("CHAT:")) {
                    String[] dijelovi = message.split(":", 4);
                    if (dijelovi.length == 4) {
                        String nazivSobe = dijelovi[1];
                        String posiljalac = dijelovi[2];
                        String tekstPoruke = dijelovi[3];

                        Room room = Server.sobe.computeIfAbsent(nazivSobe, Room::new);
                        if (currentRoom == null || !currentRoom.equals(room)) {
                            if (currentRoom != null) {
                                currentRoom.removeClient(this);
                            }
                            currentRoom = room;
                            currentRoom.addClient(this);
                        }
                        currentRoom.broadcast("[" + nazivSobe + "] " + posiljalac + ": " + tekstPoruke);
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
                    out.println("[SISTEM] Tiket je kreiran sa ID broj: " + ticket.getId());
                    continue;
                }

                if (message.equals("GET_KORISNICI")) {
                    StringBuilder lista = new StringBuilder("Aktivni korisnici: ");
                    for (ClientHandler client : Server.clients) {
                        lista.append(client.username).append(" ");
                    }
                    out.println(lista.toString());
                    continue;
                }

                if (message.equals("GET_TIKETI")) {
                    if (!admin) {
                        out.println("Nemate administratorska prava.");
                        continue;
                    }
                    if (Server.tickets.isEmpty()) {
                        out.println("Nema aktivnih tiketa u sistemu.");
                        continue;
                    }

                    for (Ticket ticket : Server.tickets) {
                        String status = ticket.isZatvoren() ? "ZATVOREN" : "OTVOREN";
                        out.println("ID: " + ticket.getId() + " | Korisnik: " + ticket.getKorisnik() + " | Opis: " + ticket.getOpis() + " | Status: " + status);
                    }
                    continue;
                }

                if (message.startsWith("PREUZMI_TIKET:")) {
                    if (!admin) {
                        out.println("Nemate administratorska prava.");
                        continue;
                    }

                    try {
                        int id = Integer.parseInt(message.substring(15).trim());
                        boolean pronadjen = false;

                        for (Ticket ticket : Server.tickets) {
                            if (ticket.getId() == id) {
                                ticket.dodajAdministratora(username);
                                out.println("[ADMIN] Uspešno ste preuzeli tiket ID: " + id);
                                pronadjen = true;
                                break;
                            }
                        }

                        if (!pronadjen) {
                            out.println("Tiket sa ID " + id + " nije pronađen.");
                        }
                    } catch (NumberFormatException e) {
                        out.println("Neispravan ID tiketa.");
                    }
                    continue;
                }

                if (message.startsWith("ADMIN_ODGOVOR:")) {
                    if (!admin) {
                        out.println("Nemate administratorska prava.");
                        continue;
                    }

                    String podaci = message.substring(14);
                    String[] dijelovi = podaci.split(":", 2);

                    if (dijelovi.length < 2) {
                        out.println("Neispravan format odgovora.");
                        continue;
                    }

                    try {
                        int id = Integer.parseInt(dijelovi[0].trim());
                        String odgovor = dijelovi[1].trim();
                        boolean pronadjen = false;

                        for (Ticket ticket : Server.tickets) {
                            if (ticket.getId() == id) {
                                ticket.dodajOdgovor(odgovor);
                                ticket.osvjeziAktivnost();

                                for (ClientHandler client : Server.clients) {
                                    if (client.username != null && client.username.equals(ticket.getKorisnik())) {
                                        client.sendMessage("[ODGOVOR NA TIKET ID " + id + "]: " + odgovor);
                                    }
                                }
                                out.println("[ADMIN] Odgovor dodan na tiket ID: " + id);
                                pronadjen = true;
                                break;
                            }
                        }

                        if (!pronadjen) {
                            out.println("Tiket nije pronađen.");
                        }
                    } catch (NumberFormatException e) {
                        out.println("Neispravan ID tiketa.");
                    }
                    continue;
                }

                if (message.startsWith("ZATVORI TIKET:")) {
                    if (!admin) {
                        out.println("Nemate administratorska prava.");
                        continue;
                    }

                    try {
                        int id = Integer.parseInt(message.substring(15).trim());
                        boolean pronadjen = false;

                        for (Ticket ticket : Server.tickets) {
                            if (ticket.getId() == id) {
                                ticket.zatvori();
                                out.println("[SISTEM] Tiket ID " + id + " je zatvoren.");
                                pronadjen = true;
                                break;
                            }
                        }

                        if (!pronadjen) {
                            out.println("Tiket nije pronađen.");
                        }
                    } catch (NumberFormatException e) {
                        out.println("Neispravan ID tiketa.");
                    }
                    continue;
                }


                if (currentRoom != null) {
                    currentRoom.broadcast(username + ": " + message);
                } else {
                    out.println("Prvo izaberite kanal.");
                }
            }

        } catch (Exception e) {
            System.out.println("Klijent (" + (username != null ? username : "nepoznat") + ") se odjavio.");
        } finally {
            if (currentRoom != null) {
                currentRoom.removeClient(this);
            }
            Server.clients.remove(this);
            try {
                socket.close();
            } catch (Exception e) {

            }
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}