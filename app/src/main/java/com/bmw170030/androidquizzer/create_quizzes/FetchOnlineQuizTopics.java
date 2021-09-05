/**
 * FetchOnlineQuizTopics.java
 *
 * Written by Bradley Wersterfer for CS4301.001, Assignment 3, starting March 9, 2021.
 * Adapted from Bradley Wersterfer's CS4301.001 Assignment 2, which started on January 23, 2021.
 * NetID: bmw170030
 *
 * This is the beginning of an alternative implementation of the online functionalities for the
 * AndroidQuizzer program. The current method implemented by MainActivity.java is to use two internal
 * AsyncTask subclasses to handle fetching data from the server to create quiz objects. Because this
 * method is deprecated, I found it useful to begin setting up an alternative method using the more
 * general Java Runnable interface and Threads. Because the Assignment 2, Phase 2 specification
 * requested that we use AsyncTask anyway, this is not currently used by the main logical loop of the
 * AndroidQuizzer, but it is still here as a useful starting point for future improvement.
 */

package com.bmw170030.androidquizzer.create_quizzes;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

/**
 * This class' intent is to retrieve a list of file names from an online repository, then descend into
 * each one and retrieve the first line in order to find the topics for each.
 */
public class FetchOnlineQuizTopics implements Runnable
{
    //! Instance variables for maintaining an online connection
    private String url_str;
    private URL dir_location;
    private HttpsURLConnection connection;

    //! The list of quiz names found at the main URL directory
    private ArrayList<String> file_names;

    /**
     * A public constructor to allow for another process to pass the desired URL to search to this thread.
     * @param url_str A string representation of the URL.
     */
    public FetchOnlineQuizTopics(String url_str)
    {
        // Set instance variables
        this.url_str = url_str;
        file_names = new ArrayList<>();

        // Attempt to set the given URL address and catch any malformed errors
        try {
            dir_location = new URL(url_str);
        }
        catch (MalformedURLException ex) {
            System.out.println("\"" + url_str + "\" is not a properly formed URL!");
            ex.printStackTrace();
        }
    }

    /**
     * This process is run when the thread starts. It will first set up the connection to the main
     * remote repository, then get each
     */
    @Override
    public void run() {
        try {
            // Set up the connection parameters and properties
            connection = (HttpsURLConnection) dir_location.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(100);
            //connection.setRequestProperty();

            // Actually open the connection and make sure a valid response was received
            connection.connect();
            if(connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                InputStream response = connection.getInputStream();
                Scanner input = new Scanner(response);

                // Read in the name of each quiz. If it is a valid file, then add it to the list
                while(input.hasNextLine()) {
                    String line = input.nextLine();
                    if(validFile(line))
                        file_names.add(line);
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
        connection.disconnect();

        /* TODO: For future development of this project (outside of the scope of this Assignment),
         * this will need to descend into each found remote file name and retrieve just the first line
         * as a mapping of quiz topic names to quiz file names. This should then be passed back to
         * the main activity, likely by a Callable<> implementation.
         */
    }

    /**
     * Return the mapping of quiz topics (names) to quiz files stored on the server.
     * @return A HashMap<K, V> where K=quiz topics and V=quiz file names
     */
    public HashMap<String, String> getQuizMapping()
    {
        HashMap<String, String> names_to_files = new HashMap<>();
        for(String file : file_names)
        {
            // If the file is valid, then open a connection for it and get the name
            if(validFile(file)) {
                String full_url = url_str + "/" + file;

                /* TODO: Finishing this alternative constructor is outside the scope of this assignment,
                 * but it could be done relatively quickly by adapting the code from AsyncTask to form
                 * the HashMap as described above in the run() method to-do.
                 */
            }
        }
        return names_to_files;
    }

    /**
     * Accessor for the file names.
     * @return The file names found at the URL location.
     */
    public ArrayList<String> getFileNames()
    {
        return file_names;
    }

    /**
     * Given a file name, determine whether it contains a quiz or not.
     * @param name The name of the file being checked.
     * @return Boolean value for whether file is valid (true) or not (false).
     */
    private boolean validFile(@NonNull String name)
    {
        return name.startsWith("Quiz") && name.endsWith(".txt");
    }
}
