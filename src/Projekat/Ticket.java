package Projekat;

import java.util.ArrayList;
import java.util.List;

public class Ticket {

    private int id;
    private String korisnik;
    private String opis;
    private boolean zatvoren;
    private List<String> odgovori;
    private String dodijeljeniAdmin;
    private long zadnjaAktivnost;

    public Ticket(int id, String korisnik, String opis) {
        this.id = id;
        this.korisnik = korisnik;
        this.opis = opis;
        this.zatvoren = false;
        this.odgovori = new ArrayList<>();
        this.dodijeljeniAdmin = "Nije dodijeljen";
        this.zadnjaAktivnost = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public String getKorisnik() {
        return korisnik;
    }

    public String getOpis() {
        return opis;
    }

    public boolean isZatvoren() {
        return zatvoren;
    }

    public void zatvori() {
        this.zatvoren = true;
    }

    public void dodajAdministratora(String admin) {
        this.dodijeljeniAdmin = admin;
    }

    public String getDodijeljeniAdmin() {
        return dodijeljeniAdmin;
    }

    public void dodajOdgovor(String odgovor) {
        this.odgovori.add(odgovor);
    }

    public List<String> getOdgovori() {
        return odgovori;
    }

    public String getSviOdgovoriFormatirano() {
        if (odgovori.isEmpty()) {
            return "Nema odgovora.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < odgovori.size(); i++) {
            sb.append("\n   [" + (i + 1) + "] ").append(odgovori.get(i));
        }
        return sb.toString();
    }

    public void osvjeziAktivnost() {
        this.zadnjaAktivnost = System.currentTimeMillis();
    }

    public long getZadnjaAktivnost() {
        return zadnjaAktivnost;
    }
}