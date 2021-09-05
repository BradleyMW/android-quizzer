/**
 * FragEditQuestion.java
 *
 * Written by Bradley Wersterfer for CS4301.001, Assignment 4, starting April 7, 2021.
 * Extending Bradley Wersterfer's CS4301.001 Assignment 2, which started on January 23, 2021.
 * NetID: bmw170030
 *
 * This fragment allows for quiz questions to be dynamically presented to the user. It accepts a
 * MultipleChoiceQuestion when instantiating a new instance, which it will automatically use to
 * populate the question text and 4 sets of answer text adjacent to a corresponding RadioButton.
 * Exactly one of these RadioButtons will be pressed to denote the correct answer. The calling
 * activity can refresh this fragment whenever it wishes to display a new question.
 *
 * This fragment also supports a saveQuestion() method to facilitate converting the stored information
 * back into a compatible MultipleChoiceQuestion object. Because the user might insert erroneous data
 * into the fragment and try to save it, this method will first validate the data before trying to
 * return a MCQ object. If there is invalid data, it will throw an Exception to the calling function
 * so that the main activity can notify the user what part of their data was invalid.
 */

package com.bmw170030.androidquizzer.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;

import com.bmw170030.androidquizzer.R;
import com.bmw170030.androidquizzer.quiz.Answer;
import com.bmw170030.androidquizzer.quiz.MultipleChoiceQuestion;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragEditQuestion#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragEditQuestion extends Fragment {

    //! Constant name for mapping the passed question value
    private static final String ARG_QUESTION = "question";
    private static final int NUM_ANSWERS = 4;
    private static final int NUM_CORRECT = 1;

    //! Internal representation of the underlying Multiple Choice Question
    private MultipleChoiceQuestion mcq;

    //! UI View elements from this fragment
    EditText topic;
    EditText[] answers = new EditText[NUM_ANSWERS];
    RadioButton[] correct_btns = new RadioButton[NUM_ANSWERS];

    public FragEditQuestion() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using a provided Question object.
     *
     * @param q A serializable MultipleChoiceQuestion to use for population.
     * @return A new instance of fragment FragEditQuestion.
     */
    public static FragEditQuestion newInstance(MultipleChoiceQuestion q) {
        FragEditQuestion fragment = new FragEditQuestion();
        Bundle args = new Bundle();
        args.putSerializable(ARG_QUESTION, q);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Populate this fragment's instance variables.
     * @param savedInstanceState Any saved instance values
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mcq = (MultipleChoiceQuestion) getArguments().getSerializable(ARG_QUESTION);
        }
    }

    /**
     * Inflates this fragment view.
     * @param inflater Used to inflate the XML layout
     * @param container The root group for this fragment
     * @param savedInstanceState Any saved instance values
     * @return An inflated XML view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_frag_edit_question, container, false);
        setDisplays(v);
        return v;
    }

    /**
     * Update this fragment's UI elements to its underlying question representation.
     * @param v The parent view containing individual XML elements. Populate them with the question.
     */
    public void setDisplays(View v) {
        // First set this question's topic text
        topic = v.findViewById(R.id.new_question_text);
        topic.setText(mcq.getText());

        // Find each of the UI answer components of this fragment and their buttons
        correct_btns[0] = v.findViewById(R.id.correct_button1);
        correct_btns[1] = v.findViewById(R.id.correct_button2);
        correct_btns[2] = v.findViewById(R.id.correct_button3);
        correct_btns[3] = v.findViewById(R.id.correct_button4);
        answers[0] = v.findViewById(R.id.edit_answer1);
        answers[1] = v.findViewById(R.id.edit_answer2);
        answers[2] = v.findViewById(R.id.edit_answer3);
        answers[3] = v.findViewById(R.id.edit_answer4);

        // Fetch elements unique to this question
        List<Answer> q_answers = mcq.getAnswers();
        int right_answer = mcq.getRightAnswer();

        // Populate the UI elements with this question information if it already exists
        if(mcq.getAnswers().size() > 0 && !mcq.getText().equals("") && right_answer != -1) {
            for (int ix = 0; ix < NUM_ANSWERS; ix++) {
                answers[ix].setText(mcq.getAnswers().get(ix).getText());

                // Check this answer's radio button (false if wrong; true if correct)
                correct_btns[ix].setChecked(ix == right_answer);
            }
        }
    }

    /**
     * This method attempts to save this display's new values as the question in this multiple
     * choice quiz. If the display contains bad formatting (such as the wrong number of correct
     * answers or an empty text field), then it will raise an exception to avoid outputting
     * bad data. The reason for the exception is contained within.
     *
     * @return A MultipleChoiceQuestion representing this question's display.
     * @throws Exception Invalid data is contained within this quiz. Print the message.
     */
    public MultipleChoiceQuestion saveQuestion() throws Exception {
        // Ensure that the right number of answers is marked as being correct
        if(numRightAnswers() != NUM_CORRECT) {
            throw new Exception("You must have exactly " + NUM_CORRECT + " correct answers!");
        }

        // Check for empty or null text in the topic and each of the answer choices
        if(topic.getText() == null || topic.getText().toString().equals("")) {
            throw new Exception("The topic text is empty!");
        }
        for(EditText t : answers) {
            if(t.getText() == null || t.getText().toString().equals("")) {
                throw new Exception("An answer's text box is currently empty!");
            }
        }

        // Fetch the answers for this question. If it is a new question, begin with empty text.
        List<Answer> answers_ls = mcq.getAnswers();
        if(answers_ls.size() < 1)
            for(int ix = 0; ix < NUM_ANSWERS; ix++)
                answers_ls.add(new Answer("", false, (char)(ix + 'A')));

        // Update the answers for this question
        for(int ix = 0; ix < NUM_ANSWERS; ix++) {
            answers_ls.get(ix).setText(answers[ix].getText().toString());
            answers_ls.get(ix).setCorrect(correct_btns[ix].isChecked());
        }
        return new MultipleChoiceQuestion(topic.getText().toString(), answers_ls);
    }

    /**
     * This function checks how many of the answers for this display have been marked as correct.
     * @return The number of corresponding radio buttons in this fragment that have been checked.
     */
    public int numRightAnswers() {
        // Simply count how many of the radio buttons have been checked
        int count = 0;
        for(int ix = 0; ix < NUM_ANSWERS; ix++) {
            if(correct_btns[ix].isChecked())
                count++;
        }
        return count;
    }

    /**
     * onClick method for when a radio button is checked. Unclick all of the others.
     * @param view The RadioButton that was just clicked.
     */
    public void onRadioClick(View view) {
        for(RadioButton rb : correct_btns) {
            rb.setChecked(false);
        }
        RadioButton clickedRb = (RadioButton) view;
        clickedRb.setChecked(true);
    }
}