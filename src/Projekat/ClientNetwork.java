package Projekat;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class ClientNetwork {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Consumer<String> onMessageReceived;
    private boolean isRunning = false;

    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            isRunning = true;

            new Thread(this::listenForServerMessages).start();
            return true;
        } catch (IOException e) {
            System.err.println("Greška pri povezivanju sa serverom: " + e.getMessage());
            return false;
        }
    }

    private void listenForServerMessages() {
        try {
            String serverResponse;
            while (isRunning && (serverResponse = in.readLine()) != null) {
                if (onMessageReceived != null) {
                    onMessageReceived.accept(serverResponse);
                }
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("Konekcija sa serverom je prekinuta.");
            }
        }
    }

    public void setOnMessageReceived(Consumer<String> callback) {
        this.onMessageReceived = callback;
    }

    public void sendCommand(String command) {
        if (out != null) {
            out.println(command);
        }
    }

    public void disconnect() {
        isRunning = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
