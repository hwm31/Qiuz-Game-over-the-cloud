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
                new Question("What is the largest ocean?", "Pacific"),
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
                    var in = new Scanner(socket.getInputStream());
                    var out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                out.println("Welcome to the Quiz Game!");

                // Send quiz questions and receive client's answers
                for (Question question : questions) {
                    out.println("Question: " + question.getQuestionText());

                    // Wait for client's answer
                    if (in.hasNextLine()) {
                        String clientAnswer = in.nextLine();

                        // Check if the answer is correct and send feedback
                        if (clientAnswer.equalsIgnoreCase(question.getAnswer())) {
                            clientScore = clientScore + 10;
                            out.println("Correct!");
                        } else {
                            out.println("Incorrect!");
                        }
                    } else {
                        out.println("No answer received. Disconnecting.");
                        break;
                    }
                }

                // Send final score
                out.println("Quiz Over! Your final score is: " + clientScore);

            } catch (Exception e) {
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