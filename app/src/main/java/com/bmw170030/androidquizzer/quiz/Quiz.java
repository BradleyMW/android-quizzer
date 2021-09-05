/**
 * Quiz.java
 *
 * Written by Bradley Wersterfer for CS4301.001, Assignment 3, starting March 9, 2021.
 * Originally written by Bradley Wersterfer for CS4301.001, Assignment 2, starting January 23, 2021.
 * NetID: bmw170030
 *
 * This is a generic quiz interface to allow for polymorphic quiz handling and creation. A concrete
 * implementation of a quiz should always implement this interface to ensure compatibility. It requires
 * the user to implement methods to get a quiz name or topic, a question at a specific index, the current
 * score, and the total number of questions.
 */

package com.bmw170030.androidquizzer.quiz;

public interface Quiz
{
    /**
     * Uses the topic of the quiz as a string representation for display.
     * @return The name of this multiple choice quiz.
     */
    String getTopic();

    /**
     * Setter for the topic.
     * @param topic String to set as this quiz's topic.
     */
    void setTopic(String topic);

    /**
     * Return a reference to a specific question in this quiz.
     * @param index The number of the question to return.
     * @return An object implementing the Question interface.
     */
    Question getQuestion(int index);

    /**
     * Retrieve the number of questions that have been correctly answered.
     * @return The integer score for this quiz.
     */
    int getScore();

    /**
     * Easy interface for retrieving the number of questions in this quiz.
     * @return The total number of questions in this quiz.
     */
    int numQuestions();
}
