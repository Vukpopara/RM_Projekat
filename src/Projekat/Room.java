package Projekat;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Room {
    private String name;
    private List<ClientHandler> currentClients = new CopyOnWriteArrayList<>();

    public Room(String name) {
        this.name = name;
    }

    public void addClient(ClientHandler client) {
        if (!currentClients.contains(client)) {
            currentClients.add(client);
        }
    }

    public void removeClient(ClientHandler client) {
        currentClients.remove(client);
    }

    public void broadcast(String message) {
        for (ClientHandler client : currentClients) {
            client.sendMessage(message);
        }
    }

    public String getName() { return name; }
}