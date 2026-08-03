
    package Projekat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

    public class ProvjeraKorisnika {

        private static final String FAJL = "korisnici.txt";

        public static boolean postojiKorisnik(String korisnik) {

            try (BufferedReader reader = new BufferedReader(new FileReader(FAJL))) {

                String linija;

                while ((linija = reader.readLine()) != null) {

                    if (linija.trim().equalsIgnoreCase(korisnik.trim())) {
                        return true;
                    }

                }

            } catch (IOException e) {

                System.out.println("Greška pri čitanju fajla korisnika.");

            }

            return false;
        }
    }

