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
                addSystemMessage("Error reading configuration file. Using default settings.");
            }
        } else {
            addSystemMessage("Configuration file not found. Using default settings.");
        }

        addSystemMessage("Connecting to server at " + serverIP + ":" + serverPort);

        // Connect to the server
        try {
            socket = new Socket(serverIP, serverPort);
            addSystemMessage("Connected to the server.");

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // Start a new thread to listen for server messages
            new Thread(this::receiveMessages).start();

        } catch (IOException e) {
            addSystemMessage("Error connecting to server: " + e.getMessage());
        }
    }

    private void sendMessage() {
        String message = textField.getText().trim();
        if (message.isEmpty()) {
            return; // Ignore empty messages
        }

        addChatMessage("You: " + message, true); // Show the user's input on the right

        try {
            out.write(message + "\n");
            out.flush();

            // If the user types "bye", disable further input
            if (message.equalsIgnoreCase("bye")) {
                addSystemMessage("You exited the quiz. Waiting for server's final message...");
                textField.setEnabled(false); // Disable further input
                sendButton.setEnabled(false);
            }

        } catch (IOException e) {
            addSystemMessage("Error sending message: " + e.getMessage());
        }

        textField.setText("");
    }

    private void receiveMessages() {
        try {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                // Handle server messages
                if (serverMessage.contains("Your final score is:")) {
                    addSystemMessage(serverMessage); // Display the final score as a system message
                    textField.setEnabled(false);
                    sendButton.setEnabled(false);
                    break;
                } else if (serverMessage.contains("Quiz Over") || serverMessage.contains("Goodbye")) {
                    addSystemMessage(serverMessage); // Quiz completion message
                    textField.setEnabled(false);
                    sendButton.setEnabled(false);
                    break;
                } else {
                    addChatMessage("Server: " + serverMessage, false); // Regular server messages
                }
            }
        } catch (IOException e) {
            addSystemMessage("Connection closed by the server.");
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                addSystemMessage("Error closing connection: " + e.getMessage());
            }
        }
    }

    private void addChatMessage(String message, boolean isClient) {
        SwingUtilities.invokeLater(() -> {
            JPanel messagePanel = new JPanel();
            messagePanel.setLayout(new FlowLayout(isClient ? FlowLayout.RIGHT : FlowLayout.LEFT));

            JLabel messageLabel = new JLabel(message);
            messageLabel.setOpaque(true);
            messageLabel.setBackground(isClient ? Color.CYAN : Color.LIGHT_GRAY);
            messageLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            messagePanel.add(messageLabel);

            chatPanel.add(messagePanel);
            chatPanel.revalidate();
            chatPanel.repaint();

            // Scroll to the bottom automatically
            chatPanel.scrollRectToVisible(new Rectangle(0, chatPanel.getHeight(), 1, 1));
        });
    }

    private void addSystemMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            JPanel messagePanel = new JPanel();
            messagePanel.setLayout(new FlowLayout(FlowLayout.CENTER));

            JLabel messageLabel = new JLabel(message);
            messageLabel.setOpaque(true);
            messageLabel.setBackground(Color.WHITE);
            messageLabel.setForeground(Color.BLACK);
            messageLabel.setFont(messageLabel.getFont().deriveFont(Font.BOLD)); // Bold text for system messages
            messageLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            messagePanel.add(messageLabel);

            chatPanel.add(messagePanel);
            chatPanel.revalidate();
            chatPanel.repaint();

            // Scroll to the bottom automatically
            chatPanel.scrollRectToVisible(new Rectangle(0, chatPanel.getHeight(), 1, 1));
        });
    }
}