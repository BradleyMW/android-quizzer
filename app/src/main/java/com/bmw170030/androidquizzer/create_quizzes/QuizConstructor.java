/**
 * QuizConstructor.java
 *
 * Written by Bradley Wersterfer for CS4301.001, Assignment 3, starting March 9, 2021.
 * NetID: bmw170030
 *
 * This is a factory class that exists to create quiz objects, decoupling the creation process from
 * a specific Activity. It currently only supports creating Multiple Choice Quizzes (the only currently
 * implemented quiz type), but it could be extended or reimplemented by another class to create other
 * types of quizzes in the future. It must be passed a file directory to search for quiz files within,
 * and it supports methods to generate all quizzes within that directory at once.
 */

package com.bmw170030.androidquizzer.create_quizzes;

import androidx.annotation.NonNull;

import com.bmw170030.androidquizzer.quiz.Answer;
import com.bmw170030.androidquizzer.quiz.MultipleChoiceQuestion;
import com.bmw170030.androidquizzer.quiz.MultipleChoiceQuiz;
import com.bmw170030.androidquizzer.quiz.Quiz;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

// Factory builder for Quiz objects. Use the public makeAllQuizzes() function to generate all quizzes
// automatically. Can also build individual multiple choice quizzes with createMultipleChoiceQuiz(file).
public class QuizConstructor
{
    private File directory;                         // The path to the main directory to search within
    private HashMap<String, String> name_to_file;   // Maps a quiz topic to its corresponding file name
    private final String filter = "Quiz.*.txt";     // Regular Expression for Matching Files

    /**
     * Public constructor. Takes the getFilesDir() function to determine the directory that all of the
     * quizzes are stored in for this application.
     * @param directory The internal application directory for data files.
     */
    public QuizConstructor(File directory)
    {
        this.directory = directory;
    }

    /**
     * This method finds all of the quiz names in the given directory. It then maps each quiz name
     * to the corresponding file name so that the two are linked.
     * @return A HashMap<K, V> where K=quiz topic and V=corresponding uiz file name.
     */
    public HashMap<String, String> getQuizNames()
    {
        // Loop through every file in the given directory.
        name_to_file = new HashMap<>();
        for(String name : directory.list()) {
            if(validFile(name)) {
                // Attempt to open the given file. Catch any exceptions that are found.
                try {
                    File quiz_file = new File(directory + "/" + name);
                    Scanner input = new Scanner(quiz_file);

                    // Simply read the first line of the file. Given that all valid files will never
                    // contain errors, we do not need to check past this point.
                    name_to_file.put(input.nextLine(), name);

                    // Always close an input stream
                    input.close();
                }

                // Catch the most common exceptions (with a final catch-all to print the stack trace if the
                // exception was not recognized).
                catch(FileNotFoundException e) {
                    System.out.println("Error! The file \"" + name + "\" could not be opened. Please " +
                            "check to make sure that it is in the file directory.");
                    e.printStackTrace();
                }
                catch(Exception e) {
                    System.out.println("There was an error when reading input from the file.");
                    e.printStackTrace();
                }
            }
        }

        return name_to_file;
    }

    /**
     * Look at the stored internal directory and automatically generate all quizzes.
     * @return An ArrayList of MultipleChoiceQuiz objects.
     */
    public ArrayList<Quiz> makeAllQuizzes()
    {
        // Go through all files listed in this directory. For each one that matches 'Quiz*.txt', create
        // a MultipleChoiceQuiz.
        ArrayList<Quiz> quizzes = new ArrayList<>();
        for(String name : directory.list()) {
            if(validFile(name))
                quizzes.add(createMultipleChoiceQuiz(name));
        }
        return quizzes;
    }

    /**
     * Create a multiple choice quiz from a given serialized quiz. Currently assumes that each question
     * will follow the specific format provided by Assignment 2 (1 or more questions, each with exactly
     * 4 answers).
     *
     * @param quiz_str A representation of the quiz file where each line has already been separated
     *                 into individual elements of a String ArrayList.
     * @return A fully populated Multiple Choice Quiz.
     */
    public MultipleChoiceQuiz createMultipleChoiceQuiz(ArrayList<String> quiz_str)
    {
        /* Multiple Choice Quizzes are guaranteed to follow this format:
         * Line 0   : Topic of the Quiz
         * Line 1   : First Question Text
         * Line 2-5 : Answers 1-4
         * Line 6+  : Subsequent groups of 5 lines may have another question
         */
        String topic = quiz_str.get(0);
        ArrayList<MultipleChoiceQuestion> questions = new ArrayList<>();
        for(int line = 1; line < quiz_str.size(); line++)
        {
            // If there are any completely empty lines, then they should be ignored.
            if(quiz_str.get(line).equals("")) {
                line++;
                continue;
            }

            // Create the MCQ's question text and 4 answers with their given ordering in the
            // question tracked.
            String text = quiz_str.get(line);
            ArrayList<Answer> answers = new ArrayList<>();
            line++;
            int question_num = 0;

            answers.add(new Answer(quiz_str.get(line), question_num));
            line++;
            question_num++;

            answers.add(new Answer(quiz_str.get(line), question_num));
            line++;
            question_num++;

            answers.add(new Answer(quiz_str.get(line), question_num));
            line++;
            question_num++;

            // Final line increment is handled by the for loop; question_num is no longer significant
            answers.add(new Answer(quiz_str.get(line), question_num));

            // Instantiate the question and add it to this quiz
            questions.add(new MultipleChoiceQuestion(text, answers));
        }

        // Finally, build and return the fully constructed MC quiz with its question list and topic
        return new MultipleChoiceQuiz(topic, questions);
    }

    /**
     * Helper function to assist with creating a MCQ from just a file name string. Assumes that a local
     * quiz is being constructed.
     * @param file_name The string name of the file.
     * @return A fully constructed multiple choice quiz.
     */
    public MultipleChoiceQuiz createMultipleChoiceQuiz(String file_name)
    {
        // First, read the given file name into a String ArrayList, then call the above function
        ArrayList<String> quiz_rep = quizFileToString(file_name);
        return createMultipleChoiceQuiz(quiz_rep);
    }

    /**
     * Given a file name, return the contents of that file as a list of strings, one for each line.
     * @param file_name String name of the quiz file
     * @return An ArrayList of individual strings for each line
     */
    public ArrayList<String> quizFileToString(String file_name)
    {
        ArrayList<String> str_representation = new ArrayList<>();

        // Attempt to open the given file. Catch any exceptions that are found.
        try {
            File quiz_file = new File(directory + "/" + file_name);
            Scanner input = new Scanner(quiz_file);

            // Simply convert the file's lines into an ArrayList of strings.
            while(input.hasNextLine())
                str_representation.add(input.nextLine());

            // Always close an input stream
            input.close();
        }

        // Catch the most common exceptions (with a final catch-all to print the stack trace if the
        // exception was not recognized).
        catch(FileNotFoundException e) {
            System.out.println("Error! The file \"" + file_name + "\" could not be opened. Please " +
                    "check to make sure that it is in the file directory.");
            e.printStackTrace();
        }
        catch(Exception e) {
            System.out.println("There was an error when reading input from the file.");
            e.printStackTrace();
        }

        return str_representation;
    }

    /**
     * Given a file name, determine whether it contains a quiz or not.
     * @param name The name of the file being checked.
     * @return Boolean value for whether file is valid (true) or not (false).
     */
    public boolean validFile(@NonNull String name)
    {
        return name.startsWith("Quiz") && name.endsWith(".txt");
    }
}
