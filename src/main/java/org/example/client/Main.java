package org.example.client;

import java.util.Random;

public class Main {
    public static void main(String[] args) {
        String host = "localhost";
        Random random = new Random();
        Client client = new Client(random.nextInt(1000));
        client.connectTo(host);
    }
}
