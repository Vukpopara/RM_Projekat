package Projekat;

import java.io.*;

public class ProvjeraKorisnika {

    private static final String FAJL = "korisnici.txt";


    public static boolean postojiKorisnik(String korisnik) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FAJL))) {
            String linija;
            while ((linija = reader.readLine()) != null) {
                String[] dijelovi = linija.split(":");
                if (dijelovi.length > 0 && dijelovi[0].trim().equalsIgnoreCase(korisnik.trim())) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("Greška pri čitanju fajla korisnika: " + e.getMessage());
        }
        return false;
    }


    public static boolean ispravnaPrijava(String korisnik, String lozinka) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FAJL))) {
            String linija;
            while ((linija = reader.readLine()) != null) {
                String[] dijelovi = linija.split(":");
                if (dijelovi.length == 2) {
                    if (dijelovi[0].trim().equalsIgnoreCase(korisnik.trim()) &&
                            dijelovi[1].trim().equals(lozinka.trim())) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Greška pri čitanju fajla korisnika: " + e.getMessage());
        }
        return false;
    }

    public static synchronized boolean registrujKorisnika(String korisnik, String lozinka) {
        if (postojiKorisnik(korisnik)) {
            return false;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FAJL, true))) {
            writer.write(korisnik.trim() + ":" + lozinka.trim());
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("Greška pri upisu korisnika: " + e.getMessage());
            return false;
        }
    }
}