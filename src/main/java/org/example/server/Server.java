package org.example.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Server {
    private static final String propertiesFile = "/server_settings.txt";
    private final int maxClientsCount;
    private final int port;
    private final Logger logger;
    private List<String> newMessages;

    public Server() {
        // Read properties
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream(propertiesFile));
        } catch (IOException e) {
            System.out.println("Error while reading server properties: " + e.getMessage());
        }
        this.port = Integer.parseInt(properties.getProperty("PORT"));
        this.maxClientsCount = Integer.parseInt(properties.getProperty("MAX_CLIENT_COUNT"));

        // Instantiate logging
        String logfileName = properties.getProperty("LOG_FILE");
        this.logger = Logger.getLogger("ServerLog");
        try {
            FileHandler fileHandler = new FileHandler(logfileName);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            System.out.println("Error while logging to file " + logfileName + ": " + e.getMessage());
        }
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server runs on port " + port);
            newMessages = new CopyOnWriteArrayList<>();
            try (ExecutorService threadPool = Executors.newFixedThreadPool(maxClientsCount)) {
                while (true) {
                    Socket socket = serverSocket.accept();
                    logger.info("New connection received on port " + socket.getPort());
                    threadPool.submit(writerRunnable(socket));
                }
            } catch (Exception e) {
                logger.severe("Error while executing task in a thread pool: " + e.getMessage());
            }
        } catch (IOException e) {
            logger.severe("Error while processing connections: " + e.getMessage());
        }
    }

    private Runnable writerRunnable(Socket socket) {
        return () -> {
            try (PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
                writer.println("Welcome to the chat! Send your name in the first message, then send any messages");
                logger.info("Welcome message sent to a new client");
                Thread thread = new Thread(readerRunnable(socket));
                thread.start();

                // Send new messages
                int currentMessageIndex = 0;
                while (true) {
                    Thread.sleep(1000); // Wait before refresh
                    if (currentMessageIndex < newMessages.size()) {
                        for (int i = currentMessageIndex; i < newMessages.size(); i++) {
                            String message = newMessages.get(i);
                            writer.println(message);
                            logger.info("Message " + message + " sent to client");
                            Thread.sleep(300); // CX improvement
                        }
                        currentMessageIndex = newMessages.size();
                    }
                }
            } catch (IOException | InterruptedException e) {
                logger.severe("Error while processing client: " + e.getMessage());
            }
        };
    }

    private Runnable readerRunnable(Socket socket) {
        return () -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                // Await messages from client
                String name = null;
                String inputMessage;
                while ((inputMessage = reader.readLine()) != null) {
                    if (name == null) {
                        name = inputMessage;
                        logger.info("Name received: " + name);
                    } else {
                        logger.info("Message received from " + name + ": " + inputMessage);
                        newMessages.add(formatChatMessage(name, inputMessage));
                    }
                }
            } catch (IOException e) {
                logger.severe("Error while reading messages: " + e.getMessage());
            }
        };
    }

    private String formatChatMessage(String name, String message) {
        return "[" + ZonedDateTime.now() + "] [" + name + "]: " + message;
    }
}
