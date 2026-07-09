package Projekat;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) {
        System.out.println("Pokretanje klijenta i povezivanje na server...");

        // Dodali smo PrintWriter i Scanner direktno u try da se sami zatvore na kraju
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Uspješno povezivanje sa serverom na adresi: " + socket.getRemoteSocketAddress());

            // Pozivamo tvoju novu klasu za prijavu
            String korisnickoIme = PrijavaKorisnika.izvrsiPrijavu(out, scanner);

            System.out.println("Veza sa serverom je aktivna. Zatvaranje klijenta...");

        } catch (UnknownHostException e) {
            System.err.println("Server nije pronađen na adresi: " + SERVER_ADDRESS + " (" + e.getMessage() + ")");
        } catch (IOException e) {
            System.err.println("Greška prilikom komunikacije sa serverom: " + e.getMessage());
        }
    }
}