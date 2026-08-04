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
                            out.println("Dobrodosao " + username);
                            break; // Izlazimo iz login petlje i prelazimo na obradu poruka/kanala
                        }
                    } else {
                        out.println("Neispravan format prijave.");
                    }
                    continue;
                }

                // Ako stigne bilo šta drugo prije login/register
                out.println("Molimo prijavite se ili registrujte.");
            }

            // Petlja za komunikaciju u aplikaciji
            String message;
            while ((message = in.readLine()) != null) {

                if (message.equals("/izlaz")) {
                    break;
                }

                if (message.equals("ADMIN_PRIJAVA")) {
                    admin = true;
                    out.println("Uspjesna admin prijava.");
                    continue;
                }

                if (message.startsWith("KANAL:")) {
                    String brojKanala = message.substring(6);
                    String nazivSobe = "";

                    if (brojKanala.equals("1")) {
                        nazivSobe = "Programiranje";
                    } else if (brojKanala.equals("2")) {
                        nazivSobe = "Dizajn";
                    } else if (brojKanala.equals("3")) {
                        nazivSobe = "Opste teme";
                    } else {
                        out.println("Nepostojeci kanal.");
                        continue;
                    }

                    Room room = Server.sobe.computeIfAbsent(nazivSobe, Room::new);

                    if (currentRoom != null) {
                        currentRoom.removeClient(this);
                    }

                    currentRoom = room;
                    currentRoom.addClient(this);
                    currentRoom.broadcast(username + " se pridruzio sobi.");
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

                if (message.startsWith("OTVORI_TIKET:")) {
                    String opis = message.substring(14);
                    Ticket ticket = new Ticket(Server.tickets.size() + 1, username, opis);
                    Server.tickets.add(ticket);
                    out.println("Tiket uspjesno kreiran. ID: " + ticket.getId());
                    continue;
                }

                if (message.equals("GET_TIKETI")) {
                    if (!admin) {
                        out.println("Nemate administratorska prava.");
                        continue;
                    }
                    if (Server.tickets.isEmpty()) {
                        out.println("Nema aktivnih tiketa.");
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

                    int id = Integer.parseInt(message.substring(15));
                    boolean pronadjen = false;

                    for (Ticket ticket : Server.tickets) {
                        if (ticket.getId() == id) {
                            ticket.dodajAdministratora(username);
                            out.println("Preuzeli ste tiket " + id);
                            pronadjen = true;
                            break;
                        }
                    }

                    if (!pronadjen) {
                        out.println("Tiket nije pronađen.");
                    }
                    continue;
                }

                if (message.startsWith("ADMIN_ODGOVOR:")) {
                    if (!admin) {
                        out.println("Nemate administratorska prava.");
                        continue;
                    }

                    String podaci = message.substring(16);
                    String[] dijelovi = podaci.split(" ", 2);

                    if (dijelovi.length < 2) {
                        out.println("Neispravan format.");
                        continue;
                    }

                    int id = Integer.parseInt(dijelovi[0]);
                    String odgovor = dijelovi[1];
                    boolean pronadjen = false;

                    for (Ticket ticket : Server.tickets) {
                        if (ticket.getId() == id) {
                            ticket.dodajOdgovor(odgovor);
                            ticket.osvjeziAktivnost();

                            for (ClientHandler client : Server.clients) {
                                if (client.username.equals(ticket.getKorisnik())) {
                                    client.sendMessage("Administrator je odgovorio na vas tiket (ID: " + id + "): " + odgovor);
                                    break;
                                }
                            }
                            out.println("Odgovor dodat na tiket " + id);
                            pronadjen = true;
                            break;
                        }
                    }

                    if (!pronadjen) {
                        out.println("Tiket nije pronađen.");
                    }
                    continue;
                }

                if (message.startsWith("ZATVORI_TIKET:")) {
                    if (!admin) {
                        out.println("Nemate administratorska prava.");
                        continue;
                    }

                    int id = Integer.parseInt(message.substring(15));
                    boolean pronadjen = false;

                    for (Ticket ticket : Server.tickets) {
                        if (ticket.getId() == id) {
                            ticket.zatvori();
                            out.println("Tiket " + id + " je zatvoren.");
                            pronadjen = true;
                            break;
                        }
                    }

                    if (!pronadjen) {
                        out.println("Tiket nije pronađen.");
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
            System.out.println("Klijent se odjavio.");
        } finally {
            if (currentRoom != null) {
                currentRoom.removeClient(this);
                currentRoom.broadcast(username + " je napustio sobu.");
            }
            Server.clients.remove(this);
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}