import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class QuizServer {
    public static void main(String[] args) throws Exception {
        ServerSocket listener = new ServerSocket(8888);
        System.out.println("The quiz server is running...");
        ExecutorService pool = Executors.newFixedThreadPool(20); // Handles up to 20 clients simultaneously
        while (true) {
            Socket sock = listener.accept();
            pool.execute(new QuizHandler(sock));
        }
    }

    private static class QuizHandler implements Runnable {
        private Socket socket;
        private static final List<Question> questions = Arrays.asList(
                new Question("What is the process of moving packets from one network to another called?", "Forwarding"),
                new Question("Which protocol is used to send an email?", "SMTP"),
                new Question("What is the default port number for HTTP?", "80"),
                new Question("Which device is used to forward packets between different networks?", "Router"),
                new Question("What protocol is primarily used for secure communication over the internet?", "HTTPS")
        );

        QuizHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("Connected: " + socket);
            int clientScore = 0;

            try (
                    var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    var out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                // Send welcome message and instructions
                out.println("Welcome to the Quiz Game!");
                out.println("Type 'start' to begin the quiz or 'bye' to exit.");

                // Wait for the client to type 'start' or 'bye'
                String clientMessage = in.readLine();
                if (clientMessage == null || clientMessage.trim().equalsIgnoreCase("bye")) {
                    out.println("Goodbye! Thank you for visiting the quiz.");
                    return;
                } else if (!clientMessage.trim().equalsIgnoreCase("start")) {
                    out.println("Invalid command. Please type 'start' to begin or 'bye' to exit.");
                    return;
                }

                // Start the quiz
                out.println("The quiz is starting! Answer the following questions!");

                // Quiz loop with question counter
                int questionNumber = 1; // Track question number
                for (Question question : questions) {
                    // Send question
                    out.println("Question " + questionNumber + ": " + question.getQuestionText());
                    System.out.println("Sent Question " + questionNumber + ": " + question.getQuestionText());

                    // Wait for client's answer
                    clientMessage = in.readLine();
                    if (clientMessage == null || clientMessage.trim().equalsIgnoreCase("bye")) {
                        out.println("Goodbye! You exited the quiz early.");
                        out.println("Your final score is: " + clientScore + "/50");
                        System.out.println("Client exited quiz early with score: " + clientScore);
                        return;
                    }

                    System.out.println("Received answer for Question " + questionNumber + ": " + clientMessage);

                    // Provide feedback immediately based on case-insensitive comparison
                    if (clientMessage.trim().equalsIgnoreCase(question.getAnswer())) {
                        clientScore += 10;
                        out.println("Correct!");
                    } else {
                        out.println("Incorrect!");
                    }

                    // Increment question number
                    questionNumber++;
                }

                // Send final score and close connection
                out.println("Quiz Over! Your final score is: " + clientScore + "/50");
                System.out.println("Client completed the quiz with score: " + clientScore);

            } catch (IOException e) {
                System.out.println("Error handling client communication: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                    System.out.println("Closed: " + socket);
                } catch (IOException e) {
                    System.out.println("Error closing socket: " + e.getMessage());
                }
            }
        }
    }

    // Class defining a question
    private static class Question {
        private final String questionText;
        private final String answer;

        public Question(String questionText, String answer) {
            this.questionText = questionText;
            this.answer = answer;
        }

        public String getQuestionText() {
            return questionText;
        }

        public String getAnswer() {
            return answer;
        }
    }
}