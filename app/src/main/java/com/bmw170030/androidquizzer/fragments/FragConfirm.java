/**
 * FragConfirm.java
 *
 * Written by Bradley Wersterfer for CS4301.001, Assignment 3, starting March 9, 2021.
 * Originally written by Bradley Wersterfer for CS4301.001, Assignment 2, starting January 23, 2021.
 * NetID: bmw170030
 *
 * This is a fragment for confirming an answer choice. It is implemented as its own click listener,
 * which calls a corresponding function from the activity that houses it to change it out for a
 * fragment that displays the result.
 */

package com.bmw170030.androidquizzer.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bmw170030.androidquizzer.R;
import com.bmw170030.androidquizzer.activities.TakeQuiz;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragConfirm#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragConfirm extends Fragment implements View.OnClickListener {

    // The parent TakeQuiz Activity and the button inside of this fragment.
    TakeQuiz quizzer;
    Button confirm;

    // Required empty public constructor
    public FragConfirm() { }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragConfirm.
     */
    public static FragConfirm newInstance() {
        FragConfirm fragment = new FragConfirm();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * The onCreateView is called when this fragment needs to be inflated. It simply finds its confirm
     * button and sets the onClickListener to this class, which can then call the corresponding
     * parent activity method.
     *
     * @param inflater Object to inflate the corresponding display_correctness layout.
     * @param container ViewGroup container as an object
     * @param savedInstanceState The bundled instance state
     * @return The finished view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_confirm_selection, container, false);

        // Set up this view and find corresponding information.
        quizzer = (TakeQuiz) getActivity();
        confirm = (Button) view.findViewById(R.id.confirm_button);
        confirm.setOnClickListener(this);

        // Return the inflated view
        return view;
    }

    /**
     * This click listener will call the quizzer to lock in the most recently selected answer.
     * @param view The confirmation button that was clicked.
     */
    @Override
    public void onClick(View view)
    {
        // Simply instruct the quizzer to confirm the answer.
        quizzer.confirmAnswer();
    }
}