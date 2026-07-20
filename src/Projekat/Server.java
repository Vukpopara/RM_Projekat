package Projekat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private static final int PORT = 8080;

    public static List<ClientHandler> clients = new ArrayList<>();

    public static java.util.Map<String, Room> sobe = new java.util.concurrent.ConcurrentHashMap<>();

    public static List<Ticket> tickets = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("Server slusa na portu: " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();

                System.out.println("Novi klijent: " +
                        clientSocket.getRemoteSocketAddress());

                ClientHandler handler = new ClientHandler(clientSocket);

                clients.add(handler);

                Thread thread = new Thread(handler);
                thread.start();
            }

        } catch (IOException e) {
            System.err.println("Greska: " + e.getMessage());
        }
    }

    public static void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}