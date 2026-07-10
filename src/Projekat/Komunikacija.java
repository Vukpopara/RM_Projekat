package Projekat;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Scanner;

public class Komunikacija {

    public static void pokreniAplikaciju(BufferedReader in, PrintWriter out, Scanner scanner) {

        String korisnickoIme = PrijavaKorisnika.izvrsiPrijavu(out, scanner);

        System.out.println("\n=== CHAT JE AKTIVAN ===");
        System.out.println("Mozete poceti sa kucanjem poruka (Za izlaz ukucajte /izlaz)...\n");

        Thread nitZaCitanje = new Thread(() -> {
            try {
                String porukaSaServera;
                while ((porukaSaServera = in.readLine()) != null) {
                    System.out.println(porukaSaServera);
                }
            } catch (Exception e) {
                System.out.println("Veza sa serverom je prekinuta.");
            }
        });
        nitZaCitanje.start();

        while (true) {
            String tekstPoruke = scanner.nextLine();

            if (tekstPoruke.equalsIgnoreCase("/izlaz")) {
                break;
            }

            out.println(korisnickoIme + ": " + tekstPoruke);
        }
    }
}