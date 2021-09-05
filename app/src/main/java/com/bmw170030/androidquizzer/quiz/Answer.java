/**
 * Answer.java
 *
 * Written by Bradley Wersterfer for CS4301.001, Assignment 3, starting March 9, 2021.
 * Originally written by Bradley Wersterfer for CS4301.001, Assignment 2, starting January 23, 2021.
 * NetID: bmw170030
 *
 * This is a basic Answer class to ensure that a variety of quizzes can implement them. Each answer
 * should know whether it is correct or not and have corresponding text. It can also track its given
 * position by associating a character symbol with that answer (such as A., B., C., ...etc.).
 */

package com.bmw170030.androidquizzer.quiz;

import java.io.Serializable;

public class Answer implements Serializable
{
    //! Private member variables representing this answer
    private String text;
    private boolean correct;
    private char symbol;

    /**
     * Constructor for an answer object.
     * @param text The text for this answer.
     * @param correct Whether this answer is correct or not.
     * @param symbol The letter representing this question choice (A, B, C, etc.)
     */
    public Answer(String text, boolean correct, char symbol)
    {
        this.text = text;
        this.correct = correct;
        this.symbol = symbol;
    }

    /**
     * Constructor for when an entire line is passed as an answer input. Preceded by '*' if correct.
     * @param full_input_line The full string line of an answer
     * @param position The position of this answer within the holding question.
     */
    public Answer(String full_input_line, int position)
    {
        // Check to see if the first character of the string is a special * marking the correct answer
        // Also convert the position of this question into a character symbol
        this.symbol = (char)(position + 'A');
        if(full_input_line.charAt(0) == '*') {
            this.text = full_input_line.substring(1);
            this.correct = true;
        }
        else {
            this.text = full_input_line;
            this.correct = false;
        }
    }

    /**
     * Setter for the text representation of this answer.
     * @param text The text for this answer.
     */
    public void setText(String text)
    {
        this.text = text;
    }

    /**
     * Setter for the symbol representation of this answer.
     * @param symbol The char symbol for this answer.
     */
    public void setSymbol(char symbol)
    {
        this.symbol = symbol;
    }

    /**
     * Setter for whether this answer is correct.
     * @param correct Is this answer correct (true) or wrong (false)
     */
    public void setCorrect(boolean correct)
    {
        this.correct = correct;
    }

    /**
     * Getter for the text representation of this answer.
     * @return The text of this answer.
     */
    public String getText()
    {
        return text;
    }

    /**
     * Getter for the text representation of this answer and corresponding symbol.
     * @return The associated symbol and the answer text.
     */
    public String getFullText()
    {
        return symbol + ". " + text;
    }

    /**
     * Getter for the boolean correctness of this answer.
     * @return Is this answer correct (true) or wrong (false)
     */
    public boolean isCorrect()
    {
        return correct;
    }
}
