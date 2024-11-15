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
                System.out.println("Server: " + serverMessage);

                // Stop reading initial messages if they include the quiz instructions
                if (serverMessage.contains("Type 'start' to begin the quiz or 'bye' to exit.")) {
                    break;
                }
            }

            String userMessage;
            while (true) {
                // Wait for server's question or feedback
                while ((serverMessage = in.readLine()) != null) {
                    System.out.println("Server: " + serverMessage);

                    // If the server sends "Quiz Over" or "Goodbye," break the loop
                    if (serverMessage.contains("Quiz Over") || serverMessage.contains("Goodbye")) {
                        return;
                    }

                    // Prompt user for input after receiving a question
                    if (serverMessage.startsWith("Question:")) {
                        System.out.print("You: ");
                        userMessage = userIn.readLine();

                        // If the user types "bye", send it to the server and exit
                        if (userMessage.equalsIgnoreCase("bye")) {
                            out.write(userMessage + "\n");
                            out.flush();
                            return;
                        }

                        // Send the user message to the server
                        out.write(userMessage + "\n");
                        out.flush();
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