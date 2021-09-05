/**
 * MultipleChoiceQuestion.java
 *
 * Written by Bradley Wersterfer for CS4301.001, Assignment 4, starting April 7, 2021.
 * Originally written by Bradley Wersterfer for CS4301.001, Assignment 2, starting January 23, 2021.
 * NetID: bmw170030
 *
 * This is an implementation of the Question interface for multiple choice questions. It assumes that
 * there will be 4 answers per question and only 1 of them will be correct, as per the Assignment 2
 * specifications. There is also a method to get the right answer for this question, which is useful
 * for displaying the correct answer when a user chooses a wrong one.
 *
 * As of Phase 4, this class also supports a few more utility methods such as getting the full list
 * of questions, removing an existing question, or overwriting one in-place in the underlying ArrayList.
 * These methods are intended to be used only by the CreateQuiz activity, which can only be accessed
 * by usernames with special creation privileges.
 */

package com.bmw170030.androidquizzer.quiz;

import java.io.Serializable;
import java.util.List;

public class MultipleChoiceQuestion implements Question, Serializable
{
    private String question;
    private List<Answer> answers;
    private int max_answers;

    /**
     * Public constructor for a single multiple choice question. Can take any number of answer objects.
     *
     * @param question The string text for this question.
     * @param answers A list of answers for this question.
     */
    public MultipleChoiceQuestion(String question, List<Answer> answers)
    {
        this.question = question;
        this.answers = answers;
        this.max_answers = answers.size();
    }

    /**
     * Give an index for an answer and check to see if it is correct.
     * @param selection Index for a guessed answer
     * @return Boolean value for whether that answer was correct or not
     */
    @Override
    public boolean chooseAnswer(int selection)
    {
        return answers.get(selection).isCorrect();
    }

    /**
     * Getter for the possible answers.
     * @return A list of the answers
     */
    @Override
    public List<Answer> getAnswers()
    {
        return answers;
    }

    /**
     * Get a specific answer as long as it exists for this question.
     * @param index The number of the question to retrieve.
     * @return The corresponding Answer object, or a dummy variable showing the error.
     */
    @Override
    public Answer getAnswer(int index)
    {
        if(index >= answers.size() || index < 0)
            return new Answer("Index \"" + index + "\" out of bounds!", false, '.');
        else
            return answers.get(index);
    }

    /**
     * Assumes that exactly one answer will be correct per the multiple choice question specification.
     * @return The index of the correct answer to this question.
     */
    @Override
    public int getRightAnswer() {
        for(int i = 0; i < answers.size(); i++)
        {
            if(answers.get(i).isCorrect())
                return i;
        }

        // If this point is reached, then there was no correct answer, and this question was
        // improperly formed.
        return -1;
    }

    /**
     * Getter for the question text being used.
     * @return Textual representation of the question.
     */
    @Override
    public String getText()
    {
        return question;
    }
}
