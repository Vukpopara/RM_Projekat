package Projekat;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Scanner;

public class Komunikacija {

    public static void pokreniAplikaciju(BufferedReader in, PrintWriter out, Scanner scanner) {

        String korisnickoIme = PrijavaKorisnika.izvrsiPrijavu(out, scanner);

        System.out.println("\n=== GLAVNI MENI APLIKACIJE ===");
        System.out.println("1. Otvori listu tematskih kanala (Chat)");
        System.out.println("2. Tehnicka podrska (Ticketing System)");
        System.out.print("Izaberite opciju (1 ili 2): ");

        String opcija = scanner.nextLine();

        if (opcija.equals("1")) {
            System.out.println("\n=== DOSTUPNI TEMATSKI KANALI ===");
            System.out.println("1. Programiranje");
            System.out.println("2. Dizajn");
            System.out.println("3. Opste teme");
            System.out.print("Izaberite broj kanala za ulazak: ");

            String izabraniKanal = scanner.nextLine();

            out.println("KANAL:" + izabraniKanal);
            System.out.println("\nUspjesno ste usli u kanal!");
        }

        else if (opcija.equals("2")) {
            out.println("TIKET_SISTEM");
            System.out.println("\nUlazak u sistem tehnicke podrske...");
        }

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