/**
 * FragCorrectness.java
 *
 * Written by Bradley Wersterfer for CS4301.001, Assignment 3, starting March 9, 2021.
 * Originally written by Bradley Wersterfer for CS4301.001, Assignment 2, starting January 23, 2021.
 * NetID: bmw170030
 *
 * This is a fragment for displaying the correctness of a guess. It is implemented as its own
 * click listener, which calls a corresponding function from the activity that houses it to change
 * it out for a fragment that can confirm the next question.
 */

package com.bmw170030.androidquizzer.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bmw170030.androidquizzer.R;
import com.bmw170030.androidquizzer.activities.TakeQuiz;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragCorrectness#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragCorrectness extends Fragment implements View.OnClickListener {

    // The parent TakeQuiz Activity and the button inside of this fragment.
    TakeQuiz quizzer;
    TextView text;
    private static boolean CORRECT;
    private static char CORRECT_ANSWER;

    // Required empty public constructor.
    public FragCorrectness() { }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param correct Boolean representation of whether the guess was correct or not.
     * @param correct_answer The symbol of the answer that should have been chosen.
     * @return A new instance of fragment FragCorrectness.
     */
    public static FragCorrectness newInstance(boolean correct, char correct_answer) {
        FragCorrectness fragment = new FragCorrectness();
        Bundle args = new Bundle();

        // Whether this answer was correct or not and the actual correct answer are passed in
        // to this factory method.
        CORRECT = correct;
        CORRECT_ANSWER = correct_answer;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * The onCreateView is called when this fragment needs to be inflated. It will check to see which
     * display should be used for the user, along with updating itself to display the correct answer
     * if necessary.
     *
     * @param inflater Object to inflate the corresponding display_correctness layout.
     * @param container ViewGroup container as an object
     * @param savedInstanceState The bundled instance state
     * @return The finished view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_display_correctness, container, false);

        // Set up this fragment's XML resources. This class is used as its own click listener.
        quizzer = (TakeQuiz) getActivity();
        text = view.findViewById(R.id.correctness);
        text.setOnClickListener(this);

        // Check to see whether the user was correct, or if the correct answer should be displayed.
        // Also sets the color of the display text to match the result.
        if(CORRECT) {
            text.setText(R.string.correct_answer);
            text.setTextColor(quizzer.getColor(R.color.green));
        }
        else {
            String inform = getString(R.string.wrong_answer) + " " + CORRECT_ANSWER + ".";
            text.setText(inform);
            text.setTextColor(quizzer.getColor(R.color.red));
        }
        return view;
    }

    /**
     * This click listener will call the quizzer to move on to the next question when the fragment
     * is pressed.
     *
     * @param view The results text that was clicked.
     */
    @Override
    public void onClick(View view)
    {
        // Simply instruct the quizzer to move on to the next question. This will change fragments.
        quizzer.nextQuestion();
    }
}