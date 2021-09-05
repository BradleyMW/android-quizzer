/**
 * MainActivity.java
 *
 * Written by Bradley Wersterfer for CS4301.001, Assignment 4, starting April 4, 2021.
 * Adapted from Bradley Wersterfer's CS4301.001 Assignment 2, which started on January 23, 2021.
 * NetID: bmw170030
 *
 * This is the main activity for this quiz program. It reads the internal directory for this application
 * for any matching quiz files using the QuizConstructor and maintains a list of all quizzes in the
 * system. These are displayed using a RecyclerView, which the user can select an item from to then
 * move on to the TakeQuiz Activity with the selected quiz questions.
 *
 * This class also contains two AsyncTask subclasses in order to easily facilitate running a background
 * process to connect to a remote URL address and select quizzes from there. The Quizzer program lets
 * users choose to either have these tasks run in the background via an ONLINE QUIZ button, or to use
 * the normal Assignment 2, Phase 1 specification of just compiling a single local quiz on the main
 * thread by choosing the LOCAL QUIZ button. The latter is enabled by default.
 *
 * This activity also supports special user permissions for when the user enters the name "professor"
 * (ignoring any capitalization). When this is done, two invisible buttons for either creating a new
 * quiz or modifying an existing selection from the local list will become visible. This lets the user
 * then move on to the CreateQuiz activity, potentially passing in an existing MultipleChoiceQuiz object
 * that they want to modify. Per the project specifications, only local quizzes can be modified currently.
 */

package com.bmw170030.androidquizzer.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bmw170030.androidquizzer.R;
import com.bmw170030.androidquizzer.create_quizzes.FetchOnlineQuizTopics;
import com.bmw170030.androidquizzer.create_quizzes.QuizConstructor;
import com.bmw170030.androidquizzer.quiz.MultipleChoiceQuiz;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import static java.net.Proxy.Type.HTTP;

public class MainActivity extends AppCompatActivity implements QuizTopicAdapter.ClickListener, View.OnFocusChangeListener {

    // Elements for letting the user enter their name
    EditText input_name;
    TextView err_text;

    // UI elements for displaying and selecting quiz topics
    QuizTopicAdapter adapter;
    RecyclerView quiz_topics;
    Button begin_quiz;

    // UI elements for creating or modifying quizzes. Only visible when username=Professor
    Button create_quiz;
    Button edit_quiz;
    private static final String CREATE_PRIVILEGES = "professor";

    // Representation of the quizzes
    QuizConstructor factory;
    FetchOnlineQuizTopics async_factory;
    HashMap<String, String> name_to_file;
    String quiz_file_name;
    boolean remote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect each of the XML elements by their id
        input_name = (EditText) findViewById(R.id.login_text);
        quiz_topics = (RecyclerView) findViewById(R.id.topic_list);
        begin_quiz = (Button) findViewById(R.id.begin_quiz_btn);
        create_quiz = (Button) findViewById(R.id.create_quiz);
        edit_quiz = (Button) findViewById(R.id.edit_quiz);
        err_text = (TextView) findViewById(R.id.error_message);

        // The assignment specification notes that the program start should default to display local
        // quizzes, so immediately set up the local QuizConstructor. To be more efficient and because
        // no UI element is actually being pressed and it is not used, null can safely be passed.
        setLocal(null);

        // The TAKE QUIZ button MUST be enabled when a quiz is selected
        begin_quiz.setEnabled(false);

        // If the username is not professor, then modifying quizzes should be disabled.
        create_quiz.setEnabled(false);
        create_quiz.setVisibility(View.INVISIBLE);
        edit_quiz.setEnabled(false);
        edit_quiz.setVisibility(View.INVISIBLE);

        // Set up a focus change listener for the input_name EditText. When focus leaves it, check
        // to see if the name is professor (if so, then the above buttons will be re-enabled).
        input_name.setOnFocusChangeListener(this);
    }

    /**
     * Called whenever the user clicks the TAKE QUIZ button (enabled after an element or topic from
     * the RecyclerView has been selected). This will initiate the next activity.
     * @param view The Button initiating the procedure.
     */
    public void takeQuiz(View view)
    {
        // Will only start the quiz if one has been selected and there is a valid name
        if(quiz_file_name != null && !input_name.getText().toString().equals(""))
        {
            // Checks to see if the quiz should be collected from the remote repository or not
            MultipleChoiceQuiz mcq;
            if(remote) {
                // Actually create the online quiz that the user selected on a background thread.
                // Once complete, this task will automatically move to the next activity.
                GetOnlineQuizTask get_quiz_rep = new GetOnlineQuizTask();
                String quiz_url = getString(R.string.remote_repo).replace("Quizzes.txt", quiz_file_name);
                get_quiz_rep.execute(quiz_url);
            }
            else {
                // Create the chosen local quiz. Because this is internal to the app, it does not need
                // to use a different thread.
                ArrayList<String> quiz_rep = factory.quizFileToString(quiz_file_name);
                mcq = factory.createMultipleChoiceQuiz(quiz_rep);

                // Once the quiz has been constructed, immediately move to the next activity
                moveToTakeQuiz(mcq);
            }
        }
        // If no name was provided, then make a quick comment about the issue to the user and hold
        // up the program from moving forward (per TA's request)
        else if(input_name.getText().toString().equals("")) {
            Toast t = Toast.makeText(getApplicationContext(), getString(R.string.no_name), Toast.LENGTH_LONG);
            t.show();
        }
    }

    /**
     * Try to enter an activity to modify the currently selected quiz. Only available when input_name
     * contains "Professor" (ignoring case), a quiz has been selected, and the program is in local mode.
     * @param view The Button initiating the procedure.
     */
    public void editQuiz(View view) {
        // Editing remote repository quizzes is not possible currently. Inform the user as much if they
        // attempt to change an online quiz.
        if(remote) {
            Toast t = Toast.makeText(getApplicationContext(), "Editing online quizzes is not supported!", Toast.LENGTH_LONG);
            t.show();
        }
        // Will only start the quiz if one has been selected and there is a valid name
        else if(quiz_file_name != null && input_name.getText().toString().compareToIgnoreCase(CREATE_PRIVILEGES) == 0) {
            // Construct the quiz object from the local file so that it can be edited
            ArrayList<String> quiz_rep = factory.quizFileToString(quiz_file_name);
            MultipleChoiceQuiz mcq = factory.createMultipleChoiceQuiz(quiz_rep);

            // Once the quiz has been constructed, immediately move to the next activity
            Intent edit = new Intent(this, CreateQuiz.class);
            edit.putExtra("File", quiz_file_name);
            edit.putExtra("Quiz", mcq);
            startActivity(edit);
        }
        // If the professor name is not supplied, then editing quizzes should not be possible!
        else if(input_name.getText().toString().compareToIgnoreCase(CREATE_PRIVILEGES) != 0) {
            Toast t = Toast.makeText(getApplicationContext(), getString(R.string.not_professor), Toast.LENGTH_LONG);
            t.show();
        }
    }

    /**
     * Try to enter an activity to create a new quiz. Only available when input_name contains
     * "Professor" (ignoring case) and the program is in local mode.
     * @param view The Button initiating the procedure.
     */
    public void createQuiz(View view) {
        // Creating quizzes in a remote repository is not supported currently. Inform the user as
        // much if they  attempt to create an online quiz.
        if(remote) {
            Toast t = Toast.makeText(getApplicationContext(), "Editing online quizzes is not supported!", Toast.LENGTH_LONG);
            t.show();
        }
        // If the professor name is not supplied, then creating quizzes should not be possible!
        else if(input_name.getText().toString().compareToIgnoreCase(CREATE_PRIVILEGES) != 0) {
            Toast t = Toast.makeText(getApplicationContext(), getString(R.string.not_professor), Toast.LENGTH_LONG);
            t.show();
        }
        // A new quiz is being constructed, so no other conditions need to be checked
        else {
            // Pass null to signify there is no preexisting quiz and immediately move to the next activity
            Intent create = new Intent(this, CreateQuiz.class);
            create.putExtra("File", (Bundle) null);
            create.putExtra("Quiz", (Bundle) null);
            startActivity(create);
        }
    }

    /**
     * This is a consistent way to move on to the second activity of the program, TakeQuiz, from any
     * process that can generate and provide the given MultipleChoiceQuiz. The username of the user
     * is automatically provided by the local EditText for this activity.
     * @param mcq The fully constructed MultipleChoiceQuiz to use in the TakeQuiz activity.
     */
    public void moveToTakeQuiz(MultipleChoiceQuiz mcq)
    {
        // Sets up the intent and passes the serializable quiz to the next activity
        Intent quiz = new Intent(this, TakeQuiz.class);
        quiz.putExtra("Username", input_name.getText().toString());
        quiz.putExtra("Quiz", mcq);
        startActivity(quiz);
    }

    /**
     * This is called whenever the user clicks the LOCAL QUIZ button to check the internal directory
     * from this application for any quiz files. This is also set by default. Also updates RecyclerView.
     * @param view The Button initiating the procedure. Unused, so it can be null.
     */
    public void setLocal(View view)
    {
        // Set up the quiz factory with the local directory for this application, then mark all quizzes.
        remote = false;
        factory = new QuizConstructor(getFilesDir());
        name_to_file = factory.getQuizNames();
        updateRecyclerView();
    }

    /**
     * This is called whenever the user clicks the ONLINE QUIZ button to check the remote directory
     * from this assignment for any quiz files. Also updates the RecyclerView.
     * @param view The Button initiating the procedure.
     */
    public void setOnline(View view)
    {
        // Because this might take a while, manually disable moving forward while the task loads.
        begin_quiz.setEnabled(false);
        edit_quiz.setEnabled(false);
        remote = true;

        // Set up the quiz factory with the remote directory for this application, then mark all quizzes.
        // The remote directory is tracked as a string resource. Note that the AsyncTask for fetching
        // quiz names will automatically update the RecyclerView upon completion.
        GetOnlineQuizFilesTask task = new GetOnlineQuizFilesTask();
        String url = getString(R.string.remote_repo);
        task.execute(url);
    }

    /**
     * This updates the RecyclerView by creating a new QuizTopicAdapter with the quiz topics as items
     * in the list and a corresponding mapped file name for each topic. The adapter uses this MainActivity
     * class as its click listener and LinearLayoutManager, and also disables the TAKE QUIZ button
     * until the user selects an item from the newly updated RecyclerView.
     */
    public void updateRecyclerView()
    {
        // If no quizzes were found, then display an error message to the user notifying them such.
        // They can still select a different button, refreshing the RV and potentially finding quizzes.
        if(name_to_file.size() == 0)
            err_text.setVisibility(View.VISIBLE);

        // Set the recycler view to display only those quiz topics from the internal HashMap.
        // Gets key values from instance variable to display as an array.
        adapter = new QuizTopicAdapter(this, new ArrayList<>(name_to_file.keySet()));
        adapter.setClickListener(this);
        quiz_topics.setAdapter(adapter);
        quiz_topics.setLayoutManager(new LinearLayoutManager(this));

        // Also disable the TAKE and EDIT QUIZ buttons so that a selection must be made
        begin_quiz.setEnabled(false);
        edit_quiz.setEnabled(false);
    }

    /**
     * Handles click events from within the adapter by implementing that interface. When a row of the
     * Recycler View is clicked, the corresponding quiz must be chosen and the button for proceeding
     * to the next quiz should be enabled.
     *
     * @param view The clicked list element.
     * @param quiz_name The topic of the quiz that was just clicked (text value of View in list).
     * @param position The index of the clicked item.
     */
    @Override
    public void onItemClick(View view, String quiz_name, int position)
    {
        // Retrieve the quiz file name from the internal HashMap mapping to topics
        quiz_file_name = name_to_file.get(quiz_name);

        // Re-enable starting a quiz and editing a quiz if the username has create privileges
        begin_quiz.setEnabled(true);
        if(input_name.getText().toString().compareToIgnoreCase(CREATE_PRIVILEGES) == 0) {
            edit_quiz.setEnabled(true);
        }
    }

    /**
     * This focus listener will monitor the input_name attribute to see if the username is changed
     * to a role with special permissions. Currently, the only one is "Professor."
     *
     * @param v The clicked view element.
     * @param hasFocus Whether the element has a focus or not.
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(!hasFocus) {
            EditText view = (EditText) v;
            String username = view.getText().toString();
            if(username.compareToIgnoreCase(CREATE_PRIVILEGES) == 0) {
                // Ensure that the new buttons are visible.
                create_quiz.setVisibility(View.VISIBLE);
                create_quiz.setEnabled(true);
                edit_quiz.setVisibility(View.VISIBLE);

                // Also refresh the RecyclerView in case this user has additional permissions.
                updateRecyclerView();

                // Notify the user of their new permissions.
                Toast t = Toast.makeText(getApplicationContext(), getString(R.string.is_professor), Toast.LENGTH_LONG);
                t.show();
            }
        }
    }

    /**
     * This AsyncTask subclass automatically handles multithreading in Java. When created, it should
     * be passed the main directory URL from the String resources, and once executed, it will loop
     * through these lines to fetch the names (and topics) of each quiz file listed. This then populates
     * the main activities' internal map and automatically updates the RecyclerView.
     */
    public class GetOnlineQuizFilesTask extends AsyncTask<String, Integer, HashMap<String, String>>
    {
        // The connection and files described at that location
        ArrayList<String> quiz_files;
        HashMap<String, String> names_to_files;
        URL url;
        HttpsURLConnection connection;

        /**
         * This method automatically runs in the background when this task's execute() function is called.
         * @param urls The URL of the directory listing quiz file names.
         * @return A HashMap of file names (keys) to quiz topics (values).
         */
        @Override
        protected HashMap<String, String> doInBackground(String... urls) {
            quiz_files = new ArrayList<>();
            names_to_files = new HashMap<>();
            try {
                // Set up the connection parameters and properties
                url = new URL(urls[0]);
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(1000);
                connection.setRequestProperty(HTTP.name(), getString(R.string.app_name));

                // Actually open the connection and make sure a valid response was received
                connection.connect();
                if(connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    InputStream response = connection.getInputStream();
                    Scanner input = new Scanner(response);

                    // Read in the name of each file. If it is a valid file, then add it to the list
                    while(input.hasNextLine()) {
                        String line = input.nextLine();
                        if(factory.validFile(line))
                            quiz_files.add(line);
                    }

                    // Close the streams
                    input.close();
                    response.close();
                }
                else {
                    // If the response code is not OK, then the connection should be aborted
                    System.out.println("A valid connection could not be secured. Disconnecting now.");
                }
            }
            // openConnection() or the Scanner could throw an IOException
            catch (IOException ex) {
                System.out.println("The connection experienced an exception when handling input:");
                ex.printStackTrace();
            }
            // Make sure that the connection has properly ended
            connection.disconnect();

            // Now try to open up each individual quiz file to get the topics
            connection.disconnect();
            for(String name : quiz_files) {
                // Check to make sure that the task hasn't been cancelled before each connection attempt
                if(!isCancelled()) {
                    try {
                        // Try opening the connection for this specific quiz file
                        String dir = url.toString().replace("Quizzes.txt", name);
                        URL quiz_url = new URL(dir);
                        connection = (HttpsURLConnection) quiz_url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(1000);
                        connection.setRequestProperty(HTTP.name(), getString(R.string.app_name));

                        // Actually open the connection and make sure that a valid response was received
                        connection.connect();
                        if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                            InputStream response = connection.getInputStream();
                            Scanner input = new Scanner(response);

                            // Since only valid files are being checked, just read the first line of input
                            names_to_files.put(input.nextLine(), name);

                            // Close the streams
                            input.close();
                            response.close();
                        } else {
                            // If the response code is not OK, then the connection should be aborted
                            System.out.println("A valid connection could not be secured. Disconnecting now.");
                        }
                        // Disconnect that quiz connection before moving on to the next quiz file
                        connection.disconnect();
                    }
                    // openConnection() or the Scanner can throw an IOException
                    catch (IOException ex) {
                        System.out.println("A quiz connection experienced an exception when handling input:");
                        ex.printStackTrace();
                    }
                }
            }
            return names_to_files;
        }

        /**
         * This wraps up the execution of the online task for gathering quiz names. Also updates RV.
         * @param result The final mapping of quiz topics to their file names on the server.
         */
        @Override
        protected void onPostExecute(HashMap<String, String> result)
        {
            // Check to make sure that the task wasn't cancelled (if it was, then it shouldn't finish!)
            if(!isCancelled()) {
                name_to_file = result;
                updateRecyclerView();
            }
        }
    }

    /**
     * This AsyncTask subclass will go to a given remote repository and convert the text on that page
     * into a quiz using this activities' existing QuizConstructor. Once complete, it will automatically
     * serialize this quiz and load it into an intent to start the next activity. This task should only
     * be used to move forward to take a quiz that is located at a remote repository.
     */
    public class GetOnlineQuizTask extends AsyncTask<String, Integer, ArrayList<String>>
    {
        //! Instance variables to track the state of the current connection
        URL url;
        HttpsURLConnection connection;

        //! Separation of the quiz into a list of lines of text
        ArrayList<String> quiz_rep;

        /**
         * This is automatically called when this task is executed. It will go to the given URL address
         * and create a compatible serialized quiz ArrayList of strings from that location.
         * @param urls The URL of the quiz file to fetch
         * @return An ArrayList of each line of the quiz at that location
         */
        @Override
        protected ArrayList<String> doInBackground(String... urls) {
            quiz_rep = new ArrayList<>();
            try {
                // Try opening the connection for this specific quiz file
                url = new URL(urls[0]);
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(1000);
                connection.setRequestProperty(HTTP.name(), getString(R.string.app_name));

                // Actually open the connection and make sure a valid response was received
                connection.connect();
                if(connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    InputStream response = connection.getInputStream();
                    Scanner input = new Scanner(response);

                    // Read in each line of the given quiz to the list.
                    while(input.hasNextLine()) {
                        // Check to make sure that the task hasn't been cancelled, in which case
                        // extraneous work would be done
                        if(!isCancelled()) {
                            String line = input.nextLine();
                            quiz_rep.add(line);
                        }
                    }

                    // Close the streams and end the connection
                    input.close();
                    response.close();
                    connection.disconnect();
                }
                else {
                    // If the response code is not OK, then the connection should be aborted
                    System.out.println("A valid connection could not be secured. Disconnecting now.");
                }
            }
            // Catch any exceptions thrown from the try block relating to the HTTPS connection. Display
            // the message and stack for debugging any error.
            catch (ProtocolException e) {
                System.out.println("The request method was not valid:");
                e.printStackTrace();
            } catch (MalformedURLException e) {
                System.out.println(urls[0] + " is malformed!");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("There was an error when reading the input:");
                e.printStackTrace();
            }

            return quiz_rep;
        }

        /**
         * This method is automatically called once the task has finished execution in the background.
         * It passes the ArrayList of quiz lines that was found to the dedicated QuizConstructor to
         * form a serializable MultipleChoiceQuiz, then passes that to the next activity.
         * @param result The decomposition of the quiz file into a compatible list of lines of text
         */
        @Override
        protected void onPostExecute(ArrayList<String> result) {
            // Check to make sure that the task wasn't cancelled (if it was, then it shouldn't finish!)
            if(!isCancelled()) {
                // Creates the MCQ automatically once this thread has finished execution and then moves on.
                MultipleChoiceQuiz mcq = factory.createMultipleChoiceQuiz(result);
                moveToTakeQuiz(mcq);
            }
        }
    }
}