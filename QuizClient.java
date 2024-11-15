import java.io.*;
import java.net.*;

public class QuizClient {

    public static void main(String[] args) {
        BufferedReader in = null;         // Reads messages from the server
        BufferedReader userIn = null;     // Reads user input from the console
        BufferedWriter out = null;        // Sends messages to the server
        Socket socket = null;

        String serverIP = "localhost";   // Default server IP
        int serverPort = 1234;           // Default server port

        // Load server connection details from configuration file
        File configFile = new File("server_info.dat");
        if (configFile.exists()) {
            try (BufferedReader configReader = new BufferedReader(new FileReader(configFile))) {
                String line;
                while ((line = configReader.readLine()) != null) {
                    if (line.startsWith("IP=")) {
                        serverIP = line.substring(3).trim(); // Extract IP
                    } else if (line.startsWith("PORT=")) {
                        serverPort = Integer.parseInt(line.substring(5).trim()); // Extract Port
                    }
                }
            } catch (IOException | NumberFormatException e) {
                System.out.println("Error reading configuration file. Using default settings.");
            }
        } else {
            System.out.println("Configuration file not found. Using default settings.");
        }

        System.out.println("Connecting to server at " + serverIP + ":" + serverPort);

        try {
            // Connect to the server using the loaded or default IP and port
            socket = new Socket(serverIP, serverPort);
            System.out.println("Connected to the server.");

            // Initialize input and output streams
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            userIn = new BufferedReader(new InputStreamReader(System.in));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // Display initial messages from the server
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                System.out.println("Server> " + serverMessage);

                // Stop reading initial messages if they include the quiz instructions
                if (serverMessage.contains("Type 'start' to begin the quiz or 'bye' to exit.")) {
                    break;
                }
            }

            String userMessage;
            while (true) {
                // Prompt user for input
                System.out.print("You> ");
                userMessage = userIn.readLine();

                // Send the user message to the server
                out.write(userMessage.trim() + "\n");
                out.flush(); // Ensure the message is sent immediately

                // If the user types "bye", break the loop
                if (userMessage.trim().equalsIgnoreCase("bye")) {
                    break;
                }

                // Receive and print the server's response
                while ((serverMessage = in.readLine()) != null) {
                    System.out.println("Server> " + serverMessage);

                    // If server sends a new question or ends the quiz, break to allow input
                    if (serverMessage.startsWith("Question") || serverMessage.contains("Quiz Over")) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            // Close the socket and release resources
            try {
                if (socket != null) {
                    socket.close();
                }
                System.out.println("Disconnected from the server.");
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
        }
    }
}