# Ultimate Spring Boot Exam

## Overview

Ready to level up your Spring Boot skills? The **Ultimate Spring Boot Exam** is a command-line challenge that puts your knowledge to the test. It's a no-nonsense, interactive quiz that gives you real-time feedback and saves your scores. Think of it as your personal training ground for mastering Spring.

This project isn't just an exam; it's a deep dive into some seriously cool tech:

* **Spring Boot:** The engine that makes everything run.

* **JPA & H2:** Your data's personal bodyguard, keeping questions safe and sound.

* **Apache POI:** The magic that turns your Excel spreadsheet into a dynamic exam.

* **Lombok:** Less code, more fun. Say goodbye to boring getters and setters.

* **AOP:** The behind-the-scenes logging ninja, keeping tabs on your progress.

* **CLI:** Because who needs a fancy UI when you've got pure, unadulterated console power?

## Features

* **Dynamic Question Loading:** Questions, choices, correct answers, and explanations are loaded from a single `questions.xlsx` file.

* **Flexible Exam Types:** Choose from a variety of exams based on question classification: `easy`, `medium`, `hard`, `random` (a mix of all three), or `dump` (its a surprise!).

* **Intelligent UI:**

    * Clears the screen for each new question to reduce distractions.
  
    * Handles long questions by wrapping the text to a readable width.

* **Flexible Answer Validation:** Supports both single and multiple-choice answers, validating user input regardless of case or the order of multiple answers.

* **Score Management:** Saves and displays user scores, sorted by date, to a `exam_results.csv` file.

## Getting Started

### Prerequisites

* Java 17 or higher

* Apache Maven

### Setup and Running the Application

1. **Clone the Repository:**

```
git clone <your-repository-url>
cd ultimate-exam
```

2. **Build and Run:**

* Open a terminal in the project's root directory.

* Execute the following Maven command to build and run the application:

```
./mvnw spring-boot:run
```

## Usage

* The application will start and present a main menu in your console.

* **1. Start a New Exam:** Select this option and follow the prompts to choose your exam type.

* **2. View Saved Scores:** Displays a list of all saved scores, sorted by the date they were recorded.

* **3. Exit:** Closes the application.