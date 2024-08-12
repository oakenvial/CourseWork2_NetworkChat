package org.example.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Client {
    private static final String propertiesFile = "/client_settings.txt";
    private final int serverPort;
    private final Logger logger;

    public Client(int id) {
        // Read properties
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream(propertiesFile));
        } catch (IOException e) {
            System.out.println("Error while reading client properties: " + e.getMessage());
        }
        this.serverPort = Integer.parseInt(properties.getProperty("SERVER_PORT"));

        // Instantiate logging
        String logfileName = properties.getProperty("LOG_FILE_NAME") + "_" + id + properties.getProperty("LOG_FILE_EXT");
        this.logger = Logger.getLogger("Client_" + id + "_Log");
        try {
            FileHandler fileHandler = new FileHandler(logfileName);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            System.out.println("Error while logging to file " + logfileName + ": " + e.getMessage());
        }
    }

    public void connectTo(String host) {
        try (Socket socket = new Socket(host, serverPort);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            logger.info("New connection to server " + host + ":" + serverPort);
            Thread thread = new Thread(printMessagesRunnable(socket));
            thread.start();

            Scanner scanner = new Scanner(System.in);
            String message;
            while ((message = scanner.nextLine()) != null) {
                if (message.equals("/exit")) {
                    logger.info("Disconnecting and exiting chat...");
                    break;
                } else {
                    writer.println(message);
                    logger.info("Message sent to server: " + message);
                }
            }
        } catch (IOException e) {
            logger.severe("Error while processing messages: " + e.getMessage());
        }
    }

    private Runnable printMessagesRunnable(Socket socket) {
        return () -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String inputMessage;
                while ((inputMessage = reader.readLine()) != null) {
                    logger.info("Message received: " + inputMessage);
                    System.out.println(inputMessage);
                }
            } catch (IOException e) {
                logger.severe("Error while reading messages: " + e.getMessage());
            }
        };
    }
}
