package Projekat;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) {
        System.out.println("Pokretanje klijenta i povezivanje na server...");

        try (Socket socket = new Socket(SERVER_ADDRESS, PORT)) {
            System.out.println("Uspješno povezivanje sa serverom na adresi: " + socket.getRemoteSocketAddress());
            System.out.println("Veza sa serverom je aktivna. Zatvaranje klijenta...");

        } catch (UnknownHostException e) {
            System.err.println("Server nije pronađen na adresi: " + SERVER_ADDRESS + " (" + e.getMessage() + ")");
        } catch (IOException e) {
            System.err.println("Greška prilikom komunikacije sa serverom: " + e.getMessage());
        }
    }
}