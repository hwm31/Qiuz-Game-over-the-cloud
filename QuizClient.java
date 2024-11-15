import java.io.*;
import java.net.*;
import java.util.Scanner;

public class QuizClient {

    public static void main(String[] args) {
        BufferedReader in = null;         // Reads messages from the server
        BufferedReader userIn = null;     // Reads user input from the console
        BufferedWriter out = null;        // Sends messages to the server
        Socket socket = null;

        try {
            // Connect to the server running on localhost at port 8888
            socket = new Socket("localhost", 8888);
            System.out.println("Connected to the server. Type your message or answer:");

            // Initialize input and output streams
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            userIn = new BufferedReader(new InputStreamReader(System.in));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String userMessage;
            while (true) {
                // Read user input from console
                userMessage = userIn.readLine();

                // If the user types "bye", send it to the server and break the loop
                if (userMessage.equalsIgnoreCase("bye")) {
                    out.write(userMessage + "\n");
                    out.flush();
                    break;
                }

                // Send the user message to the server
                out.write("Client> " + userMessage + "\n");
                out.flush();

                // Receive and print the server's response
                String serverMessage = in.readLine();
                System.out.println("Server: " + serverMessage);
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
