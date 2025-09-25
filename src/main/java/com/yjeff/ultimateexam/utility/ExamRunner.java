package com.yjeff.ultimateexam.utility;

import com.yjeff.ultimateexam.model.Question;
import com.yjeff.ultimateexam.model.Score;
import com.yjeff.ultimateexam.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ExamRunner implements CommandLineRunner {

    private final QuestionRepository questionRepository;
    private final Scanner scanner = new Scanner(System.in);
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    @Autowired
    public ExamRunner(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public void run(String... args) {
        showMainMenu();
    }

    private void showMainMenu() {
        boolean exit = false;
        while (!exit) {
            System.out.println("\n=========================================");
            System.out.println("            Ultimate Exam Menu");
            System.out.println("=========================================");
            System.out.println("1. Start a New Exam");
            System.out.println("2. View Saved Scores");
            System.out.println("3. Exit");
            System.out.print("Please enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    runExam();
                    break;
                case "2":
                    viewScores();
                    break;
                case "3":
                    System.out.println("Thank you for using the Ultimate Exam. Goodbye!");
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }
        }
    }

    public void runExam() {
        System.out.println("\n=========================================");
        System.out.println("  Starting a New Exam...");
        System.out.println("=========================================");

        List<Question> questions = getQuestionsBasedOnUserChoice();

        if (questions.isEmpty()) {
            System.out.println("No questions found for the selected classification.");
            return;
        }

        int score = 0;
        int totalQuestions = questions.size();

        for (Question question : questions) {
            System.out.println("\n-----------------------------------------");

            askQuestion(question);

            System.out.print("Your answer: ");
            String userAnswer = scanner.nextLine().trim();

            boolean isCorrect = processAnswer(question, userAnswer);

            if (isCorrect) {
                System.out.println("Correct! Well done.");
                score++;
            } else {
                System.out.println("Incorrect.");
                System.out.println("The correct answer is: " + question.getCorrectAnswer());
                System.out.println("Why it's correct: " + question.getExplanation());
            }
        }

        System.out.println("\n=========================================");
        System.out.println("Exam finished! You scored " + score + " out of " + totalQuestions + ".");
        System.out.println("=========================================");

        System.out.print("\nEnter your name to save your score: ");
        String userName = scanner.nextLine().trim();
        saveScoreToFile(userName, score, totalQuestions);
    }

    private void viewScores() {
        System.out.println("\n=========================================");
        System.out.println("            Saved Scores (Sorted by Date)");
        System.out.println("=========================================");
        String fileName = "exam_results.csv";
        File file = new File(fileName);

        if (!file.exists()) {
            System.out.println("No saved scores found. Take an exam first!");
            return;
        }

        List<Score> scores = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            if (reader.ready()) {
                reader.readLine();
            }

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 4) {
                    try {
                        String name = data[0];
                        LocalDateTime date = LocalDateTime.parse(data[1], dtf);
                        int score = Integer.parseInt(data[2]);
                        int totalQuestions = Integer.parseInt(data[3]);
                        scores.add(new Score(name, date, score, totalQuestions));
                    } catch (Exception e) {
                        System.err.println("Skipping malformed score record: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("An error occurred while reading the scores: " + e.getMessage());
        }

        if (scores.isEmpty()) {
            System.out.println("The score file is empty.");
            return;
        }

        scores.sort(Comparator.comparing(Score::getDate).reversed());

        System.out.printf("%-20s %-25s %-10s %-15s%n", "Name", "Date", "Score", "Total Qs");
        System.out.println("-----------------------------------------------------------------");
        scores.forEach(s -> System.out.printf("%-20s %-25s %-10d %-15d%n",
                s.getName(), dtf.format(s.getDate()), s.getScore(), s.getTotalQuestions()));
    }


    private List<Question> getQuestionsBasedOnUserChoice() {
        System.out.println("\nPlease choose your exam type:");
        System.out.println("  1) Easy");
        System.out.println("  2) Medium");
        System.out.println("  3) Hard");
        System.out.println("  4) Random (Mix of Easy, Medium, Hard)");
        System.out.println("  5) Dump (Close to the real exam)");
        System.out.print("Enter your choice: ");

        String choice = scanner.nextLine().trim().toLowerCase();
        List<Question> questions;

        switch (choice) {
            case "1", "easy":
                questions = questionRepository.findByClassification("Easy");
                break;
            case "2", "medium":
                questions = questionRepository.findByClassification("Medium");
                break;
            case "3", "hard":
                questions = questionRepository.findByClassification("Hard");
                break;
            case "4", "random":
                List<Question> easyQuestions = questionRepository.findByClassification("Easy");
                List<Question> mediumQuestions = questionRepository.findByClassification("Medium");
                List<Question> hardQuestions = questionRepository.findByClassification("Hard");

                questions = Stream.of(easyQuestions, mediumQuestions, hardQuestions)
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
                break;
            case "5", "dump":
                questions = questionRepository.findByClassification("Dump");
                break;
            default:
                System.out.println("Invalid choice. Running a full exam.");
                questions = questionRepository.findAll();
                break;
        }

        Collections.shuffle(questions);

        int limit = Math.min(questions.size(), 60);

        return questions.subList(0, limit);
    }

    private void askQuestion(Question question) {
        System.out.println("Question: " + question.getQuestionText());
        String[] choices = question.getChoices().split(",");
        for (int i = 0; i < choices.length; i++) {
            System.out.println("  " + (char)('A' + i) + ") " + choices[i].trim());
        }
    }

    private boolean processAnswer(Question question, String userAnswer) {
        return userAnswer.equalsIgnoreCase(question.getCorrectAnswer().trim());
    }

    private void saveScoreToFile(String userName, int score, int totalQuestions) {
        String fileName = "exam_results.csv";
        File file = new File(fileName);
        boolean fileExists = file.exists();

        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
            if (!fileExists) {
                writer.println("Name,Date,Score,Total Questions");
            }

            LocalDateTime now = LocalDateTime.now();
            String date = dtf.format(now);

            writer.printf("%s,%s,%d,%d%n", userName, date, score, totalQuestions);
            System.out.println("Your score has been saved to " + fileName);
        } catch (IOException e) {
            System.err.println("An error occurred while saving the score: " + e.getMessage());
        }
    }
}