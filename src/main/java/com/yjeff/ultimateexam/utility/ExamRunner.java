package com.yjeff.ultimateexam.utility;

import com.yjeff.ultimateexam.model.Question;
import com.yjeff.ultimateexam.model.Score;
import com.yjeff.ultimateexam.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Order(2)
public class ExamRunner implements CommandLineRunner {

    private final QuestionRepository questionRepository;
    private final Scanner scanner = new Scanner(System.in);
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private final int MAX_LINE_WIDTH = 80;

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
            clearScreen();
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
        clearScreen();
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
        int currentQuestionNumber = 1;

        for (Question question : questions) {
            clearScreen();
            System.out.println("Question " + currentQuestionNumber++ + " of " + totalQuestions + "\n");

            askQuestion(question);

            System.out.print("Your answer: ");
            String userAnswer = scanner.nextLine().trim();

            boolean isCorrect = processAnswer(question, userAnswer);

            if (isCorrect) {
                System.out.println("\nCorrect! Well done.");
                score++;
            } else {
                System.out.println("\nIncorrect.");
                System.out.println("The correct answer is: " + question.getCorrectAnswer());
                System.out.println("Why it's correct: " + question.getExplanation());
            }

            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }

        clearScreen();
        System.out.println("\n=========================================");
        System.out.println("Exam finished! You scored " + score + " out of " + totalQuestions + ".");
        System.out.println("=========================================");

        System.out.print("\nEnter your name to save your score: ");
        String userName = scanner.nextLine().trim();
        saveScoreToFile(userName, score, totalQuestions);
    }

    private void viewScores() {
        clearScreen();
        System.out.println("\n=========================================");
        System.out.println("            Saved Scores (Sorted by Date)");
        System.out.println("=========================================");
        String fileName = "exam_results.csv";
        File file = new File(fileName);

        if (!file.exists()) {
            System.out.println("No saved scores found. Take an exam first!");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
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
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }

        scores.sort(Comparator.comparing(Score::getDate).reversed());

        System.out.printf("%-20s %-25s %-10s %-15s%n", "Name", "Date", "Score", "Total Qs");
        System.out.println("-----------------------------------------------------------------");
        scores.forEach(s -> System.out.printf("%-20s %-25s %-10d %-15d%n",
                s.getName(), dtf.format(s.getDate()), s.getScore(), s.getTotalQuestions()));
        System.out.println("\nPress Enter to return to the main menu...");
        scanner.nextLine();
    }

    private void clearScreen() {
        for (int i = 0; i < 50; ++i) System.out.println();
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
        Set<Question> uniqueQuestions = new HashSet<>();

        switch (choice) {
            case "1", "easy":
                uniqueQuestions.addAll(questionRepository.findByClassification("Easy"));
                break;
            case "2", "medium":
                uniqueQuestions.addAll(questionRepository.findByClassification("Medium"));
                break;
            case "3", "hard":
                uniqueQuestions.addAll(questionRepository.findByClassification("Hard"));
                break;
            case "4", "random":
                uniqueQuestions.addAll(questionRepository.findByClassification("Easy"));
                uniqueQuestions.addAll(questionRepository.findByClassification("medium"));
                uniqueQuestions.addAll(questionRepository.findByClassification("Hard"));
                break;
            case "5", "dump":
                uniqueQuestions.addAll(questionRepository.findByClassification("Dump"));
                break;
            default:
                System.out.println("Invalid choice. Running a full exam.");
                uniqueQuestions.addAll(questionRepository.findAll());
                break;
        }

        List<Question> questions = new ArrayList<>(uniqueQuestions);
        Collections.shuffle(questions);

        int limit = Math.min(questions.size(), 60);

        return questions.subList(0, limit);
    }

    private void askQuestion(Question question) {
        String[] codeBlockParts = question.getQuestionText().split("```");
        for (int i = 0; i < codeBlockParts.length; i++) {
            if (i % 2 == 0) {
                System.out.println(wordWrap(codeBlockParts[i], MAX_LINE_WIDTH));
            } else {
                System.out.println();
                String[] lines = codeBlockParts[i].split("\n");
                for (String line : lines) {
                    System.out.println("    " + line.trim());
                }
                System.out.println();
            }
        }

        String[] choices = question.getChoices().split("\\|");
        for (int i = 0; i < choices.length; i++) {
            System.out.println("  " + (char)('A' + i) + ") " + choices[i].trim());
        }
    }

    private boolean processAnswer(Question question, String userAnswer) {
        List<String> correctAnswers = Stream.of(question.getCorrectAnswer().replace(" ", "").split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        List<String> userAnswers = Stream.of(userAnswer.replace(" ", "").split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        return correctAnswers.size() == userAnswers.size() && correctAnswers.containsAll(userAnswers);
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

    private String wordWrap(String text, int maxWidth) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 <= maxWidth) {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                result.append(currentLine).append("\n");
                currentLine = new StringBuilder(word);
            }
        }
        result.append(currentLine);
        return result.toString();
    }
}