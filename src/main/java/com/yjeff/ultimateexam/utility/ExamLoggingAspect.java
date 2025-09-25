package com.yjeff.ultimateexam.utility;

import com.yjeff.ultimateexam.model.Question;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExamLoggingAspect {

    @Pointcut("execution(* com.example.ultimateexam.ExamRunner.runExam(..))")
    public void runExamPointcut() {}

    @Pointcut("execution(* com.example.ultimateexam.ExamRunner.askQuestion(..))")
    public void askQuestionPointcut() {}

    @Pointcut("execution(* com.example.ultimateexam.ExamRunner.processAnswer(..))")
    public void processAnswerPointcut() {}

    @Around("runExamPointcut()")
    public Object logExamRun(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("[AOP] Starting the full exam session.");
        Object result = joinPoint.proceed();
        System.out.println("[AOP] Full exam session finished.");
        return result;
    }

    @Before("askQuestionPointcut()")
    public void logQuestionPresentation(JoinPoint joinPoint) {
        Question question = (Question) joinPoint.getArgs()[0];
        System.out.println("[AOP] Presenting question to user: '" + question.getQuestionText() + "'");
    }

    @AfterReturning(pointcut = "processAnswerPointcut()", returning = "isCorrect")
    public void logAnswerResult(JoinPoint joinPoint, Object isCorrect) {
        Question question = (Question) joinPoint.getArgs()[0];
        String userAnswer = (String) joinPoint.getArgs()[1];
        System.out.println("[AOP] User answered '" + userAnswer + "' for question '" + question.getQuestionText() + "'. Result: " + (boolean)isCorrect);
    }
}
