/**
 * CreateQuiz.java
 *
 * Written by Bradley Wersterfer for CS4301.001, Assignment 4, starting April 7, 2021.
 * Extending Bradley Wersterfer's CS4301.001 Assignment 2, which started on January 23, 2021.
 * NetID: bmw170030
 *
 * This activity supports creating or modifying existing quizzes. It will be passed a MultipleChoiceQuiz
 * object by the MainActivity--if this object is null, then it enters CREATE mode, otherwise it stores
 * the object and maintains it internally with each valid addition, modification, or deletion to one
 * of the questions. While in CREATE mode, the name of the file can be freely set.
 *
 * Clearing the current question text will return the activity to ADD mode, letting users add a new
 * question to the end of the list upon saving it. Any time that a user attempts to change a question,
 * the program will ensure that the file name is valid (including checking to make sure that an older
 * quiz is not being overwritten if using CREATE mode to design a new quiz), the question topic name
 * is not empty, and each answer choice has text along with exactly one RadioButton being pressed to
 * denote a correct answer. Each of these elements representing an individual question (as opposed to
 * the entire quiz) is stored in a FragEditQuestion fragment to facilitate easier refreshing of the
 * info presented on the screen at all times.
 *
 * Once a question from the RecyclerView is pressed (using repurposed QuizTopicAdapter from MainActivity)
 * the program will enter EDIT mode. This allows for the user to modify the question or which answer
 * is correct, delete the entire question to remove it from the file, or make other changes until they
 * press the CLEAR QUESTION button to re-enter ADD mode. The user can continue modifying the quiz until
 * they press the EXIT button to return to the Main Activity, but only valid changes to the quiz will
 * be saved to the Quiz file.
 */

package com.bmw170030.androidquizzer.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bmw170030.androidquizzer.R;
import com.bmw170030.androidquizzer.create_quizzes.QuizWriter;
import com.bmw170030.androidquizzer.fragments.FragEditQuestion;
import com.bmw170030.androidquizzer.quiz.Answer;
import com.bmw170030.androidquizzer.quiz.MultipleChoiceQuestion;
import com.bmw170030.androidquizzer.quiz.MultipleChoiceQuiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class CreateQuiz extends AppCompatActivity implements QuizTopicAdapter.ClickListener {

    // Track what mode this activity is in.
    enum MODE {CREATE, ADD, EDIT};
    private MODE mode;

    // The multiple choice quiz that is either being constructed or edited.
    MultipleChoiceQuiz quiz;

    // Object to store quizzes as files compatible with QuizConstructors
    QuizWriter writer;

    // UI elements for this activity
    EditText file_name;
    EditText quiz_name;
    Button delete_question;

    RecyclerView question_list;
    QuizTopicAdapter adapter;
    int current_selection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz);

        // Connect the UI elements to their instance variables
        file_name = findViewById(R.id.new_file_name);
        quiz_name = findViewById(R.id.new_quiz_topic);
        delete_question = findViewById(R.id.delete_question);
        question_list = (RecyclerView) findViewById(R.id.question_list);

        // Pull in the quiz from the MainActivity that is being edited. If it is null, then
        // enter CREATE MODE.
        Intent intent = getIntent();
        quiz = (MultipleChoiceQuiz) intent.getSerializableExtra("Quiz");
        MultipleChoiceQuestion first_question;
        if(quiz == null) {
            mode = MODE.CREATE;

            // Create a new empty quiz
            quiz = new MultipleChoiceQuiz("", new ArrayList<MultipleChoiceQuestion>());

            // If this quiz is newly created, then start the fragment with an empty new question
            first_question = new MultipleChoiceQuestion("", new ArrayList<Answer>());
        }
        else {
            // Also disable changing the file name when a new quiz is not being created
            String quiz_file_name = intent.getStringExtra("File");
            file_name.setText(quiz_file_name);

            // Enter EDIT MODE and lock the file name.
            mode = MODE.EDIT;
            lockFileName();

            // Pull in the quiz name to the quiz topic field
            String quiz_topic = quiz.getTopic();
            quiz_name.setText(quiz_topic);

            first_question = (MultipleChoiceQuestion) quiz.getQuestion(0);
            current_selection = 0;
        }
        setEditQuestion(first_question);
        updateRecyclerView();

        // Set up the output writer for storing quizzes as files in the internal directory
        writer = new QuizWriter(getFilesDir());
    }

    /**
     * If the file for this quiz already exists, then attempting to change it (either when modifying
     * an existing quiz or after adding further questions to a new one) will result in duplicate and
     * incomplete copies. Prevent this from occurring.
     */
    public void lockFileName() {
        // Also disables the functionality to change the file name once it already exists.
        file_name.setFocusable(false);
        file_name.setEnabled(false);
    }

    /**
     * Update the question fragment display to a new question.
     * @param q The MultipleChoiceQuestion to display in this fragment.
     */
    public void setEditQuestion(MultipleChoiceQuestion q) {
        // Create the new fragment and then replace the old one
        FragEditQuestion frag = FragEditQuestion.newInstance(q);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.question_display_fragment, frag, "frag_edit_question").commit();
    }

    /**
     * onClick handler for exiting the creation activity.
     * @param view The Button initiating the procedure.
     */
    public void returnToMain(View view) {
        Intent exit = new Intent(this, MainActivity.class);
        startActivity(exit);
    }

    /**
     * This utility function simply resets the question display to a new empty question. Can be null.
     * Because the question is reset to a new display, also enter ADD mode.
     * @param view The Button initiating the procedure.
     */
    public void clearEditQuestion(View view) {
        mode = MODE.ADD;
        MultipleChoiceQuestion q = new MultipleChoiceQuestion("", new ArrayList<Answer>());
        setEditQuestion(q);
        updateRecyclerView();
    }

    /**
     * This is a utility function to verify that the input file name is correct, or if not, inform the user.
     * @return Whether the file name is valid or not.
     */
    public boolean verifyFileName() {
        String name = file_name.getText().toString();
        String error_msg = "";
        boolean error = false;

        // Check invalid name structures first, such as empty text or invalid formatting.
        if(name.length() < 1) {
            error_msg = "Please enter a file name!";
            error = true;
        }
        else if(!name.startsWith("Quiz")) {
            error_msg = "The file name must start with \"Quiz\"!";
            error = true;
        }
        else if(!name.endsWith(".txt")) {
            error_msg = "The file name must end with the .txt extension!";
            error = true;
        }

        // Also check to make sure that a quiz with this file name does not already exist
        // Only need to check in CREATE MODE (when this file has not yet been saved before)
        else if(mode == MODE.CREATE) {
            File dir = getFilesDir();
            for (File f : dir.listFiles()) {
                if (name.equals(f.toString())) {
                    error_msg = "There is already a quiz with this file name!";
                    error = true;
                }
            }
        }

        // If there is an error in the file name, let the user know why. Else, return true.
        if(error) {
            Toast t = Toast.makeText(getApplicationContext(), error_msg, Toast.LENGTH_LONG);
            t.show();
            return false;
        }
        else
            return true;
    }

    /**
     * This is a utility function to verify that the input file name is correct, or if not, inform the user.
     * @return Whether the file name is valid or not.
     */
    public boolean verifyQuizName() {
        String topic = quiz_name.getText().toString();
        if(topic.length() > 0)
            return true;
        else
            return false;
    }

    /**
     * onClick method for the SAVE QUESTION button. Attempts to save the quiz in the current fragment;
     * if it cannot, then it notifies the user why.
     * @param view The Button initiating the procedure.
     */
    public void saveQuestion(View view) {
        // First, find the current underlying question fragment.
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragEditQuestion frag = (FragEditQuestion) fragmentManager.findFragmentByTag("frag_edit_question");

        // Ensure that the question fragment was actually found to avoid a null pointer issue
        if(frag != null) {
            // Attempt saving the question. The fragment will throw an exception with an error message if
            // invalid data is attempted to be stored.
            try {
                MultipleChoiceQuestion q = frag.saveQuestion();

                // Check to make sure that the save is fully valid before committing anything.
                if(verifyFileName() && verifyQuizName()) {

                    // If the user is creating a new quiz, automatically repopulate the fragment with empty.
                    // Will also prioritize adding a new question if in ADD MODE.
                    if (mode == MODE.CREATE || mode == MODE.ADD) {
                        quiz.addQuestion(q);
                    } else {
                        // Do not start a new question because the user is in EDIT MODE
                        quiz.overwriteQuestionAt(current_selection, q);
                    }

                    // Regardless of the changes, the entire quiz must be saved.
                    saveQuiz();
                    updateRecyclerView();
                }

            } catch (Exception ex) {
                // If an error in saving occurred, then show the reason to the user and print the stack.
                ex.printStackTrace();
                Toast t = Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG);
                t.show();
            }
        }
        else {
            Toast t = Toast.makeText(getApplicationContext(), "There is no message to save!", Toast.LENGTH_LONG);
            t.show();
        }
    }

    /**
     * onClick for removing a question from the underlying list. Only accessible in EDIT MODE.
     * @param view The Button initiating the procedure.
     */
    public void deleteQuestion(View view) {
        if(verifyFileName() && verifyQuizName() && quiz.getQuestions().size() > 0) {
            quiz.removeQuestion(current_selection);
            saveQuiz();
            clearEditQuestion(null);
            mode = MODE.ADD;
            updateRecyclerView();
        }
        // verifyFileName and verifyQuizName will display errors. Display why questions might be invalid.
        else if (quiz.getQuestions().size() <= 0) {
            Toast t = Toast.makeText(getApplicationContext(), "Quizzes must have at least one question.", Toast.LENGTH_LONG);
            t.show();
        }
    }

    /**
     * Save all changes to the quiz from this activity. Will overwrite the old quiz file if CREATE
     * MODE is not active (to ensure that the user does not accidentally overwrite the wrong quiz).
     */
    public void saveQuiz() {
        String topic = quiz_name.getText().toString();
        if(verifyFileName() && topic.length() > 0 && quiz.getQuestions().size() > 0) {
            // Actually save the quiz by writing the current object to its corresponding file.
            quiz.setTopic(topic);
            writer.write(file_name.getText().toString(), quiz);

            // If the save was successful and the user is in CREATE or ADD mode, initialize a new question
            if(mode == MODE.CREATE || mode == MODE.ADD)
                clearEditQuestion(null);

            // Since file now exists, exit CREATE MODE if it was active. Otherwise, stay in the
            // mode that the user was last in.
            if(mode == MODE.CREATE) {
                mode = MODE.ADD;
                // Toast t = Toast.makeText(getApplicationContext(), "Now that you have saved this file, it cannot be renamed.", Toast.LENGTH_LONG);
                // t.show();
            }

            // If the file name changed now, it would be saved multiple times. Lock in the user's selection.
            // lockFileName();
        }
        // verifyFileName() will display why the save failed if the file name is invalid. Also display
        // the reason if the quiz topic field was invalid.
        else if(quiz_name.getText().toString().length() <= 0){
            Toast t = Toast.makeText(getApplicationContext(), "Please enter a quiz topic!", Toast.LENGTH_LONG);
            t.show();
        }
        // By the earlier project specifications, we know that quizzes are only valid if they have at least
        // one question. Notify the user if their question list is somehow empty.
        else if(quiz.getQuestions().size() <= 0) {
            Toast t = Toast.makeText(getApplicationContext(), "Quizzes must have at least one question!", Toast.LENGTH_LONG);
            t.show();
        }
    }

    /**
     * This updates the RecyclerView by creating a new QuizTopicAdapter with the question topics as
     * items in the list. The adapter uses this activity class as its click listener and
     * LinearLayoutManager.
     */
    public void updateRecyclerView()
    {
        // If no questions were found, then display an error message to the user notifying them such.
        // They can still select a different button, refreshing the RV and potentially finding quizzes.
        ArrayList<String> question_text = new ArrayList<>();
        if(quiz == null || quiz.getQuestions().size() == 0) {
            // There are no questions. Probably in CREATE MODE, unless they deleted everything.
        }
        else {
            // Form the list of question topics to populate the RecyclerView.
            for (MultipleChoiceQuestion q : quiz.getQuestions()) {
                question_text.add(q.getText());
            }
        }

        // Set the recycler view to display only those quiz topics from the internal HashMap.
        // Gets key values from instance variable to display as an array.
        adapter = new QuizTopicAdapter(this, question_text);
        adapter.setClickListener(this);
        question_list.setAdapter(adapter);
        question_list.setLayoutManager(new LinearLayoutManager(this));

        // Also disable the DELETE QUESTION button so that a selection must be made
        delete_question.setEnabled(false);
    }

    /**
     * Handles click events from within the adapter by implementing that interface. When a row of the
     * Recycler View is clicked, the corresponding question must be found and the button for deleting
     * it should be enabled.
     *
     * @param view The clicked list element.
     * @param quiz_topic The text value of the clicked item.
     * @param position The index of the question that was clicked.
     */
    @Override
    public void onItemClick(View view, String quiz_topic, int position)
    {
        // Retrieve the full Question object from the passed RecyclerView position
        MultipleChoiceQuestion question = (MultipleChoiceQuestion) quiz.getQuestion(position);
        setEditQuestion(question);

        // Re-enable deleting a question and enter EDIT MODE
        current_selection = position;
        mode = MODE.EDIT;
        delete_question.setEnabled(true);
    }

    /**
     * Pass onClick events for radio buttons to the interior fragment for handling.
     * @param view The clicked RadioButton.
     */
    public void onRadioClick(View view) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragEditQuestion frag = (FragEditQuestion) fragmentManager.findFragmentByTag("frag_edit_question");
        frag.onRadioClick(view);
    }

}