package Projekat;

import java.io.PrintWriter;
import java.util.Scanner;

public class PrijavaKorisnika {

    // Metoda prima PrintWriter (za slanje serveru) i Scanner (za citanje iz konzole)
    public static String izvrsiPrijavu(PrintWriter out, Scanner scanner) {
        System.out.println("\n--- PROCES PRIJAVE ---");
        System.out.print("Unesite vaše korisničko ime: ");

        String username = scanner.nextLine();

        // Šaljemo uneseno ime Igorovom serveru
        out.println(username);

        System.out.println("Uspješno ste se prijavili kao: " + username);
        System.out.println("-----------------------\n");

        return username; // Vraćamo ime ako ti zatreba kasnije u klijentu
    }
}