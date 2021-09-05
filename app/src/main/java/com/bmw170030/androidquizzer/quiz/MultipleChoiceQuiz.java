/**
 * MultipleChoiceQuiz.java
 *
 * Written by Bradley Wersterfer for CS4301.001, Assignment 3, starting March 9, 2021.
 * Originally written by Bradley Wersterfer for CS4301.001, Assignment 2, starting January 23, 2021.
 * NetID: bmw170030
 *
 * This is the multiple choice quiz implementation of the generic quiz. It uses an underlying ArrayList
 * that can only store Multiple Choice Questions--if more types of questions are desired, then another
 * concrete quiz class should be used. Beyond the common quiz architecture, this class also provides
 * a method for users to guess a specific answer choice from a multiple choice question, and it
 * automates the process of handling the current question so that the user does not have to progress
 * manually. Once the last question has been answered, getCurrentQuestion() will return null, marking
 * the end of the questions.
 */

package com.bmw170030.androidquizzer.quiz;

import java.io.Serializable;
import java.util.List;

public class MultipleChoiceQuiz implements Quiz, Serializable
{
    //! Variables representing the current state of the quiz
    private String topic;
    private int current_question;
    private int score;

    //! Underlying storage for Quiz Questions
    List<MultipleChoiceQuestion> questions;

    /**
     * Public constructor for creating a new multiple choice quiz. This allows for a set of multiple
     * choice questions to be constructed and held together, along with some overall statistics.
     *
     * @param topic The string topic (or name) of the given quiz.
     * @param questions A list of quiz questions. Must be entirely multiple choice currently.
     */
    public MultipleChoiceQuiz(String topic, List<MultipleChoiceQuestion> questions)
    {
        this.topic = topic;
        this.questions = questions;

        // Always start with these default values, regardless of the exact quiz
        this.current_question = 0;
        this.score = 0;
    }

    /**
     * Make a final guess on a given question in this quiz. If the guess is correct, then the
     * score for this quiz is also incremented. Either way, the quiz will note that the current
     * question is now 1 higher than it was previously.
     *
     * @param guess Position of the answer being chosen.
     * @return Boolean representation of whether that guess was correct or not.
     */
    public boolean guess(int guess)
    {
        // Ensure that the question index is within bounds, then check to see if it was correct.
        // The current question count is incremented accordingly.
        if(current_question >= questions.size() || current_question < 0) {
            throw new IndexOutOfBoundsException("Index \"" + current_question + "\" is not a question!");
        }
        // Check to see if the provided guess for the current question is correct
        else if(getCurrentQuestion().chooseAnswer(guess)) {
            score++;
            current_question++;
            return true;
        }
        else {
            current_question++;
            return false;
        }
    }

    /**
     * Get a reference to the current question that the quiz is on. If there are no questions left,
     * return null instead (this signifies the end of the quiz).
     *
     * @return The next unanswered question or null if there are none.
     */
    public Question getCurrentQuestion() {
        if(current_question >= questions.size()) {
            return null;
        }
        else
            return questions.get(current_question);
    }

    /**
     * First deletes the element at index and then adds the provided object to that spot.
     * @param index The desired question to replace.
     * @param q The question to replace that index with.
     */
    public void overwriteQuestionAt(int index, MultipleChoiceQuestion q) {
        questions.remove(index);
        questions.add(index, q);
    }

    /**
     * Utility function to remove a question from the underlying ArrayList.
     * @param index The int index of the question to remove.
     */
    public void removeQuestion(int index) {
        questions.remove(index);
    }

    /**
     * Uses the topic of the quiz as a string representation for display.
     * @return The name of this multiple choice quiz.
     */
    @Override
    public String getTopic() {
        return topic;
    }

    /**
     * Setter for the topic.
     * @param topic String to set as this quiz's topic.
     */
    @Override
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * Get a reference to a specific question in this quiz.
     * @param index The number of the question to return.
     * @return The MultipleChoiceQuestion as a Question
     */
    @Override
    public Question getQuestion(int index) {
        return questions.get(index);
    }

    /**
     * Wrapper function to add new questions to an existing quiz.
     * @param q The MultipleChoiceQuestion to add.
     * @return Whether the addition was successful or not.
     */
    public boolean addQuestion(MultipleChoiceQuestion q) {
        return questions.add(q);
    }

    /**
     * Get the full list of questions for this quiz.
     * @return ArrayList of MultipleChoiceQuestion objects.
     */
    public List<MultipleChoiceQuestion> getQuestions() {
        return questions;
    }

    /**
     * Retrieve the number of questions that have been correctly answered.
     * @return The integer score for this quiz.
     */
    public int getScore() {
        return score;
    }

    /**
     * Retrieve the total number of questions in this quiz.
     * @return The size of the underlying data container.
     */
    @Override
    public int numQuestions() {
        return questions.size();
    }
}
