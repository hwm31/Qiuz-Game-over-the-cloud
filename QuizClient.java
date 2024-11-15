import java.io.*;
import java.net.*;

public class QuizClient {

    public static void main(String[] args) {
        BufferedReader in = null;         // Reads messages from the server
        BufferedReader userIn = null;     // Reads user input from the console
        BufferedWriter out = null;        // Sends messages to the server
        Socket socket = null;

        try {
            // Connect to the server running on localhost at port 8888
            socket = new Socket("localhost", 8888);
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
                    // Wait for the final score from the server before exiting
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println("Server> " + serverMessage);
                        if (serverMessage.contains("Your final score is:")) {
                            break; // Exit after displaying the final score
                        }
                    }
                    break;
                }

                // Receive and print the server's response
                while ((serverMessage = in.readLine()) != null) {
                    System.out.println("Server> " + serverMessage);

                    // If server sends a new question or ends the quiz, break to allow input
                    if (serverMessage.startsWith("Question") || serverMessage.contains("Quiz Over")) {
                        break;
                    }

                    // If final score is sent, break and display it
                    if (serverMessage.contains("Your final score is:")) {
                        break;
                    }
                }

                // If the quiz is over, display the final score and disconnect
                if (serverMessage != null && serverMessage.contains("Quiz Over")) {
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println("Server> " + serverMessage);
                        if (serverMessage.contains("Your final score is:")) {
                            break;
                        }
                    }
                    break;
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