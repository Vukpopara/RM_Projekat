package Projekat;

import java.util.ArrayList;
import java.util.List;

public class Ticket {

    private int id;
    private String korisnik;
    private String opis;
    private boolean zatvoren;
    private String odgovor;
    private List<String> administrator;

    public Ticket(int id, String korisnik, String opis) {
        this.id = id;
        this.korisnik = korisnik;
        this.opis = opis;
        this.odgovor = "";
        this.administrator = new ArrayList<>();
        this.zatvoren = false;
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

    public String getOdgovor() {
        return odgovor;
    }

    public void dodajOdgovor(String odgovor) {
        this.odgovor = odgovor;
    }

    public boolean isZatvoren() {
        return zatvoren;
    }

    public void zatvori() {
        zatvoren = true;
    }

    public void dodajAdministratora(String admin) {

        if (!administrator.contains(admin)) {
            administrator.add(admin);
        }

    }

    public List<String> getAdministratori() {
        return administrator;
    }
}