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
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            out = new PrintWriter(
                    socket.getOutputStream(), true);

            username = in.readLine();

            out.println("Dobrodosao " + username);

            String message;

            while ((message = in.readLine()) != null) {


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
                    }
                    else if (brojKanala.equals("2")) {
                        nazivSobe = "Dizajn";
                    }
                    else if (brojKanala.equals("3")) {
                        nazivSobe = "Opste teme";
                    }
                    else {
                        out.println("Nepostojeci kanal.");
                        continue;
                    }


                    Room room = Server.sobe.computeIfAbsent(
                            nazivSobe,
                            Room::new);


                    if (currentRoom != null) {
                        currentRoom.removeClient(this);
                    }


                    currentRoom = room;
                    currentRoom.addClient(this);

                    currentRoom.broadcast(
                            username + " se pridruzio sobi.");

                    continue;
                }


                if (message.equals("GET_KORISNICI")) {

                    String lista = "Aktivni korisnici: ";

                    for (ClientHandler client : Server.clients) {
                        lista += client.username + " ";
                    }

                    out.println(lista);

                    continue;
                }


                if (currentRoom != null) {

                    currentRoom.broadcast(
                            username + ": " + message);

                } else {

                    out.println(
                            "Prvo izaberite kanal.");

                }
            }


        } catch (Exception e) {

            System.out.println("Klijent se odjavio");

        } finally {


            if (currentRoom != null) {

                currentRoom.removeClient(this);

                currentRoom.broadcast(
                        username + " je napustio sobu.");

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