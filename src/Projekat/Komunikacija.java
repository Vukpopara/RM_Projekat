package Projekat;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Scanner;

public class Komunikacija {

    public static void pokreniAplikaciju(BufferedReader in, PrintWriter out, Scanner scanner) {

        String korisnickoIme = PrijavaKorisnika.izvrsiPrijavu(out, scanner);

        boolean isAdmin = korisnickoIme.equalsIgnoreCase("admin");

        if (isAdmin) {
            System.out.println("\n=== DOBRODOŠLI, ADMINISTRATORE ===");
            System.out.println("Automatski ste prebačeni u sistem za tehničku podršku.");
            out.println("ADMIN_PRIJAVA");
        } else {

            System.out.println("\n=== GLAVNI MENI APLIKACIJE ===");
            System.out.println("1. Otvori listu tematskih kanala (Chat)");
            System.out.println("2. Tehnička podrška (Ticketing System)");
            System.out.print("Izaberite opciju (1 ili 2): ");

            String opcija = scanner.nextLine();

            if (opcija.equals("1")) {
                System.out.println("\n=== DOSTUPNI TEMATSKI KANALI ===");
                System.out.println("1. Programiranje");
                System.out.println("2. Dizajn");
                System.out.println("3. Opšte teme");
                System.out.print("Izaberite broj kanala za ulazak: ");

                String izabraniKanal = scanner.nextLine();
                out.println("KANAL:" + izabraniKanal);
                System.out.println("\nUspješno ste ušli u kanal!");
            }
            else if (opcija.equals("2")) {
                System.out.println("\n=== KREIRANJE TIKETA ZA TEHNIČKU PODRŠKU ===");
                System.out.print("Opišite vaš problem ukratko: ");
                String opisProblema = scanner.nextLine();

                out.println("OTVORI_TIKET:" + opisProblema);
                System.out.println("\nTiket uspješno kreiran. Povezivanje sa tehničkom podrškom...");
            }
        }

        System.out.println("\n=== KOMUNIKACIJA JE AKTIVNA ===");
        if (isAdmin) {
            System.out.println("Komande: /tiketi (pregled svih), /odgovori [ID] [poruka] (odgovor na tiket), /izlaz\n");
        } else {
            System.out.println("Komande: /korisnici (pregled online korisnika), /izlaz (napuštanje)\n");
        }

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

            if (tekstPoruke.equalsIgnoreCase("/korisnici")) {
                out.println("GET_KORISNICI");
            } else if (tekstPoruke.equalsIgnoreCase("/tiketi") && isAdmin) {
                out.println("GET_TIKETI");
            } else if (tekstPoruke.startsWith("/odgovori ") && isAdmin) {
                out.println("ADMIN_ODGOVOR:" + tekstPoruke.substring(10));
            } else {
                out.println(korisnickoIme + ": " + tekstPoruke);
            }
        }
    }
}