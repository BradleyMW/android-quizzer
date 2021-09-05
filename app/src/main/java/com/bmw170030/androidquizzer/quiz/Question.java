/**
 * Question.java
 *
 * Written by Bradley Wersterfer for CS4301.001, Assignment 3, starting March 9, 2021.
 * Originally written by Bradley Wersterfer for CS4301.001, Assignment 2, starting January 23, 2021.
 * NetID: bmw170030
 *
 * This is a generic Question interface for any type of quiz question. It enforces an implementation
 * of getText() for the question text, a way to retrieve a list of all associated answers or just a
 * specific one, and ways to get the right answers for this question along with a method for determining
 * whether a specific answer is correct by its position.
 */

package com.bmw170030.androidquizzer.quiz;

import java.util.List;

public interface Question
{
    /**
     * Get the associated question text.
     * @return String describing question.
     */
    public String getText();

    /**
     * Get a list of all of the possible answers for this question.
     * @return List of answer objects.
     */
    public List<Answer> getAnswers();

    /**
     * Get a specific answer for this question.
     * @param index The number of the question to retrieve.
     * @return The corresponding Answer object.
     */
    public Answer getAnswer(int index);

    /**
     * Assumes that exactly one answer will be correct per the multiple choice question specification.
     * @return The index of the correct answer to this question.
     */
    public int getRightAnswer();

    /**
     * Give a guess for an answer and check to see if the guess is correct.
     *
     * @param choice The integer index of the guessed answer.
     * @return Boolean value for whether the answer was correct or not.
     */
    public boolean chooseAnswer(int choice);
}
