package Projekat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }
    @Override
    public void run() {
        try {
            BufferedReader in =new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message;

            while ((message=in.readLine())!=null) {
                System.out.println(socket.getInetAddress() + ": " + message);
            }
        }catch (Exception e){
            System.out.println("Klijent se odjavio");

        }finally {
            try {
                socket.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
