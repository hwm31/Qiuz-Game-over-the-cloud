import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class QuizClient {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QuizClientChatUI());
    }
}

class QuizClientChatUI extends JFrame {
    private JPanel chatPanel;     // Panel to hold chat messages
    private JTextField textField; // To input user responses
    private JButton sendButton;   // To send responses to the server
    private BufferedReader in;
    private BufferedWriter out;
    private Socket socket;

    public QuizClientChatUI() {
        // Set up the GUI
        setTitle("Quiz Client - Chat Style");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(chatPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        textField = new JTextField();
        sendButton = new JButton("Send");

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(textField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        textField.addActionListener(e -> sendMessage());

        // Connect to the server
        connectToServer();

        // Make the GUI visible
        setVisible(true);
    }

    private void connectToServer() {
        String serverIP = "localhost"; // Default IP
        int serverPort = 1234;         // Default Port

        // Read configuration file
        File configFile = new File("server_info.dat");
        if (configFile.exists()) {
            try (BufferedReader configReader = new BufferedReader(new FileReader(configFile))) {
                String line;
                while ((line = configReader.readLine()) != null) {
                    if (line.startsWith("IP=")) {
                        serverIP = line.substring(3).trim();
                    } else if (line.startsWith("PORT=")) {
                        serverPort = Integer.parseInt(line.substring(5).trim());
                    }
                }
            } catch (IOException | NumberFormatException e) {
                addMessage("Error reading configuration file. Using default settings.", false);
            }
        } else {
            addMessage("Configuration file not found. Using default settings.", false);
        }

        addMessage("Connecting to server at " + serverIP + ":" + serverPort, false);

        // Connect to the server
        try {
            socket = new Socket(serverIP, serverPort);
            addMessage("Connected to the server.", false);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // Start a new thread to listen for server messages
            new Thread(this::receiveMessages).start();

        } catch (IOException e) {
            addMessage("Error connecting to server: " + e.getMessage(), false);
        }
    }

    private void sendMessage() {
        String message = textField.getText().trim();
        if (message.isEmpty()) {
            return; // Ignore empty messages
        }

        addMessage("You: " + message, true); // Show the user's input on the right

        try {
            out.write(message + "\n");
            out.flush();

            // If the user types "bye", disable further input
            if (message.equalsIgnoreCase("bye")) {
                addMessage("You exited the quiz. Waiting for server's final message...", true);
                textField.setEnabled(false); // Disable further input
                sendButton.setEnabled(false);
            }

        } catch (IOException e) {
            addMessage("Error sending message: " + e.getMessage(), false);
        }

        textField.setText("");
    }

    private void receiveMessages() {
        try {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                addMessage("Server: " + serverMessage, false); // Server messages appear on the left

                // If the server sends "Quiz Over" or "Goodbye", allow the user to close the GUI
                if (serverMessage.contains("Quiz Over") || serverMessage.contains("Goodbye")) {
                    addMessage("Server has ended the connection. You can close the window now.", false);
                    textField.setEnabled(false); // Disable input
                    sendButton.setEnabled(false); // Disable button
                    break;
                }
            }
        } catch (IOException e) {
            addMessage("Connection closed by the server.", false);
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                addMessage("Error closing connection: " + e.getMessage(), false);
            }
        }
    }

    private void addMessage(String message, boolean isClient) {
        SwingUtilities.invokeLater(() -> {
            JPanel messagePanel = new JPanel();

            // Check if the message is a system message
            if (!isClient && message.startsWith("System>")) {
                // Center-align for system messages
                messagePanel.setLayout(new FlowLayout(FlowLayout.CENTER));

                JLabel messageLabel = new JLabel(message.replace("System>", "").trim());
                messageLabel.setOpaque(true);
                messageLabel.setBackground(Color.WHITE);
                messageLabel.setForeground(Color.BLACK); // Black text for system messages
                messageLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                messagePanel.add(messageLabel);
            } else {
                // Align left for server messages or right for client messages
                messagePanel.setLayout(new FlowLayout(isClient ? FlowLayout.RIGHT : FlowLayout.LEFT));

                JLabel messageLabel = new JLabel(message);
                messageLabel.setOpaque(true);
                messageLabel.setBackground(isClient ? Color.CYAN : Color.LIGHT_GRAY);
                messageLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                messagePanel.add(messageLabel);
            }

            // Add the message panel to the chat panel
            chatPanel.add(messagePanel);
            chatPanel.revalidate();
            chatPanel.repaint();

            // Scroll to the bottom automatically
            chatPanel.scrollRectToVisible(new Rectangle(0, chatPanel.getHeight(), 1, 1));
        });
    }
}