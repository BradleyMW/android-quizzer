/**
 * DisplayResults.java
 *
 * Written by Bradley Wersterfer for CS4301.001, Assignment 3, starting March 9, 2021.
 * Originally written by Bradley Wersterfer for CS4301.001, Assignment 2, starting January 23, 2021.
 * NetID: bmw170030
 *
 * This Activity will display the user's final results for the quiz that they just took. They will
 * see their username, score, and the name of the quiz all displayed, along with an option to restart
 * the program. The majority of this activity's work is done when it is created, as it simply needs
 * to display the final statistics of the quiz that was taken.
 */

package com.bmw170030.androidquizzer.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bmw170030.androidquizzer.R;

public class DisplayResults extends AppCompatActivity {

    // Variables passed in by the intent from the previous activity
    int score;
    int num_questions;

    // Instance variables containing XML UI elements
    TextView name_view;
    TextView score_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_results);

        // Populate messages displaying the results of the previous quiz
        Intent prev = getIntent();
        score = prev.getIntExtra("Score", -1);
        num_questions = prev.getIntExtra("Count", -1);
        String score_msg = getString(R.string.score_display) + score + "/" + num_questions;
        String username = prev.getStringExtra("Username");

        // Compose a message of the user's name, chosen quiz, and score
        String user_msg = "Congratulations, " + username + "! You have taken quiz " +
                prev.getStringExtra("Topic") + ".";

        // Find XML UI elements
        name_view = (TextView) findViewById(R.id.user_msg);
        score_view = (TextView) findViewById(R.id.score);

        // Display the relevant statistics
        name_view.setText(user_msg);
        score_view.setText(score_msg);
    }

    /**
     * This is the onClick method for the button to restart the application.
     * @param view The RESTART button object.
     */
    public void newQuiz(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}