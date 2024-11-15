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
                new Question("What is the capital of France?", "Paris"),
                new Question("What is 2 + 2?", "4"),
                new Question("What is the largest ocean?", "Pacific")
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
                // Send welcome message and instructions to the client
                out.println("Welcome to the Quiz Game!");
                out.println("Type 'start' to begin the quiz or 'bye' to exit.");

                // Wait for the client to respond with 'start' or 'bye'
                String clientMessage = in.readLine();

                if (clientMessage == null || clientMessage.equalsIgnoreCase("bye")) {
                    out.println("Goodbye! Thank you for visiting the quiz.");
                    return; // End connection if client types 'bye' or disconnects
                } else if (!clientMessage.equalsIgnoreCase("start")) {
                    out.println("Invalid command. Please type 'start' to begin or 'bye' to exit.");
                    return;
                }

                // Start the quiz
                out.println("The quiz is starting! Answer the following questions:");

                // Quiz question-answer interaction
                for (Question question : questions) {
                    // Send question to the client
                    out.println("Question: " + question.getQuestionText());

                    // Wait for the client's answer
                    clientMessage = in.readLine();
                    if (clientMessage == null || clientMessage.equalsIgnoreCase("bye")) {
                        out.println("Goodbye! You exited the quiz early.");
                        out.println("Your final score is: " + clientScore);
                        return; // End connection if client types 'bye' or disconnects
                    }

                    // Check if the answer is correct and provide feedback
                    if (clientMessage.equalsIgnoreCase(question.getAnswer())) {
                        clientScore++;
                        out.println("Correct!");
                    } else {
                        out.println("Incorrect!");
                    }
                }

                // Send the final score
                out.println("Quiz Over! Your final score is: " + clientScore);

            } catch (IOException e) {
                System.out.println("Error: " + socket);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error closing socket: " + e.getMessage());
                }
                System.out.println("Closed: " + socket);
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