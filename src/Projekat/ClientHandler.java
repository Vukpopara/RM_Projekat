package Projekat;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private Room currentRoom;

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

            out.println("Unesite korisnicko ime:");
            username = in.readLine();

            out.println("Dobrodosao " + username);

            String message;

            while ((message = in.readLine()) != null) {

                if (message.startsWith("/join ")) {

                    String roomname = message.substring(6);

                    Room room = Server.sobe.computeIfAbsent(roomname, Room::new);

                    if (currentRoom != null) {
                        currentRoom.removeClient(this);
                    }

                    currentRoom = room;
                    currentRoom.addClient(this);

                    currentRoom.broadcast(username + " se pridruzio sobi.");

                    continue;
                }

                if (currentRoom != null) {
                    currentRoom.broadcast(username + ": " + message);
                } else {
                    out.println("Prvo se pridruzite sobi.");
                }
            }

        } catch (Exception e) {
            System.out.println("Klijent se odjavio");
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