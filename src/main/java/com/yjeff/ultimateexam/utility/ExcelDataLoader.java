package com.yjeff.ultimateexam.utility;

import com.yjeff.ultimateexam.model.Question;
import com.yjeff.ultimateexam.repository.QuestionRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Order(1)
public class ExcelDataLoader implements CommandLineRunner {

    private final QuestionRepository questionRepository;

    @Autowired
    public ExcelDataLoader(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        questionRepository.deleteAll();

        DataFormatter dataFormatter = new DataFormatter();

        ClassPathResource resource = new ClassPathResource("questions.xlsx");
        try (InputStream inputStream = resource.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (row.getCell(0) == null) continue;

                String questionText = dataFormatter.formatCellValue(row.getCell(0));

                questionText = questionText.replaceFirst("^\\s*\\d+\\.\\s*", "");

                List<String> choices = new LinkedList<>();
                for (int i = 1; i <= 6; i++) {
                    Cell choiceCell = row.getCell(i);
                    if (choiceCell != null && !dataFormatter.formatCellValue(choiceCell).trim().isEmpty()) {
                        choices.add(dataFormatter.formatCellValue(choiceCell).trim());
                    }
                }
                String choicesString = choices.stream().collect(Collectors.joining("|"));

                String correctAnswer = dataFormatter.formatCellValue(row.getCell(7));
                String explanation = dataFormatter.formatCellValue(row.getCell(8));
                String classification = dataFormatter.formatCellValue(row.getCell(9));

                Question question = new Question(questionText, choicesString, correctAnswer, explanation, classification);
                questionRepository.save(question);
            }
            System.out.println("Successfully loaded " + questionRepository.count() + " questions from Excel.");
        } catch (Exception e) {
            System.err.println("Error loading questions from Excel file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
