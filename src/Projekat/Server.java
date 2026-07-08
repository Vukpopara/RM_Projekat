package Projekat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server slusa na portu: " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novi klijent: " + clientSocket.getRemoteSocketAddress());
            }
        } catch (IOException e) {
            System.err.println("Greska: " + e.getMessage());
        }
    }
}