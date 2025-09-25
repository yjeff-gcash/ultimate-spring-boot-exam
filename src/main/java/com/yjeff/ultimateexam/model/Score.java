package com.yjeff.ultimateexam.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Score {
    private String name;
    private LocalDateTime date;
    private int score;
    private int totalQuestions;
}
