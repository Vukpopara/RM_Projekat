package Projekat;

public class Ticket {

    private int id;
    private String korisnik;
    private String opis;
    private boolean zatvoren;

    public Ticket(int id, String korisnik, String opis) {
        this.id = id;
        this.korisnik = korisnik;
        this.opis = opis;
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

    public boolean isZatvoren() {
        return zatvoren;
    }

    public void zatvori() {
        zatvoren = true;
    }
}