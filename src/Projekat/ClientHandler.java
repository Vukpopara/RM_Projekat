package Projekat;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

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

            String message;

            while ((message = in.readLine()) != null) {

                String fullMessage =
                        socket.getInetAddress() + ": " + message;

                System.out.println(fullMessage);

                Server.broadcast(fullMessage);
            }

        } catch (Exception e) {
            System.out.println("Klijent se odjavio");
        } finally {

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