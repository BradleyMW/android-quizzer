/**
 * QuizWriter.java
 *
 * Written by Bradley Wersterfer for CS4301.001, Assignment 4, starting April 7, 2021.
 * Extending Bradley Wersterfer's CS4301.001 Assignment 2, which started on January 23, 2021.
 * NetID: bmw170030
 *
 * This is a utility class to allow for outputting quizzes to a file directory. It is designed to store
 * MultipleChoiceQuiz objects in a format that is compatible with reading from QuizConstructor; this
 * means that each quiz will be stored with a Quiz____.txt file name where the quiz topic is the first
 * line and then every subsequent set of 5 lines denotes the next MultipleChoiceQuestion for the quiz.
 *
 * The constructor for this QuizWriter should be passed the desired directory to store quizzes in,
 * similar to how the QuizConstructor is passed the directory that the user wants to search in.
 */

package com.bmw170030.androidquizzer.create_quizzes;

import com.bmw170030.androidquizzer.quiz.Answer;
import com.bmw170030.androidquizzer.quiz.MultipleChoiceQuestion;
import com.bmw170030.androidquizzer.quiz.MultipleChoiceQuiz;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class QuizWriter {

    //! The path to the main directory to store files under
    private File directory;

    /**
     * Public constructor. Takes the getFilesDir() function to determine the directory that all of the
     * quizzes are stored in for this application.
     * @param directory The internal application directory for data files.
     */
    public QuizWriter(File directory) {
        this.directory = directory;
    }

    /**
     * This function will store a given multiple choice quiz to a file name in the directory that it
     * stores internally. This format will match the existing stored quizzes so that the file can
     * then be read again by the compatible QuizConstructor object.
     *
     * @param file_name String name of the file to store this quiz under.
     * @param quiz A fully populated MultipleChoiceQuiz.
     * @return Whether the operation was a success (true) or failure (false).
     */
    public boolean write(String file_name, MultipleChoiceQuiz quiz) {
        try {
            File output_file = new File(directory + "/" + file_name);
            FileWriter out = new FileWriter(output_file);

            // Write this quiz's name and then each of its questions
            out.write(quiz.getTopic() + "\n");
            for(MultipleChoiceQuestion q : quiz.getQuestions()) {

                // Store this question's text and then each of its answers
                out.write(q.getText() + "\n");
                for(Answer a : q.getAnswers()) {

                    // If this answer was correct, then prepend an asterisk denoting it as such
                    String answer = a.getText();
                    if(a.isCorrect()) {
                        answer = "*" + answer;
                    }
                    out.write(answer + "\n");
                }
            }

            // Close the output stream and mark the save as successful
            out.close();
            return true;
        }
        // Check for any expected IOExceptions or unexpected general Exceptions.
        catch (IOException ex) {
            System.out.println("Quiz " + quiz.getTopic() + " could not be written to.");
            ex.printStackTrace();
        }
        catch (Exception ex) {
            System.out.println("An unknown error occurred while writing a file.");
            ex.printStackTrace();
        }

        // This section can only be reached if an error occurred and the save was not successful.
        return false;
    }
}
