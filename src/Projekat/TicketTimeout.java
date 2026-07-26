package Projekat;

    public class TicketTimeout implements Runnable {

        @Override
        public void run() {

            while (true) {

                try {

                    Thread.sleep(60000);

                    long trenutnoVrijeme = System.currentTimeMillis();

                    for (Ticket ticket : Server.tickets) {

                        if (!ticket.isZatvoren()) {

                            long razlika =
                                    trenutnoVrijeme - ticket.getZadnjaAktivnost();

                            if (razlika >= 300000) {

                                ticket.zatvori();

                                System.out.println(
                                        "Tiket " + ticket.getId()
                                                + " je automatski zatvoren.");

                            }
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

