/**
 * QuizTopicAdapter.java
 *
 * Written by Bradley Wersterfer for CS4301.001, Assignment 3, starting March 9, 2021.
 * Originally written by Bradley Wersterfer for CS4301.001, Assignment 2, starting January 23, 2021.
 * NetID: bmw170030
 *
 * This Quiz Adapter extends the actual Adapter class, handling the creation of custom QuizViewHolders
 * that contain a simple TextView for display in a RecyclerView. This provides a way for these views
 * to be bound and recycled, avoiding wasted memory or resources while allowing for the user to scroll
 * through the list and see all of the available quiz topics that they can take. Utilities for handling
 * click events on each quiz topic are also provided here, though they are implemented by the MainActivity.
 */

package com.bmw170030.androidquizzer.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.bmw170030.androidquizzer.R;

import java.util.List;

public class QuizTopicAdapter extends Adapter<ViewHolder>
{
    //! Members of the Adapter for modeling data, inflating it, and handling clicks on items
    private List<String> quiz_lst;
    private LayoutInflater inflater;
    private ClickListener clickListener;
    private int current_selection = -1;

    /**
     * Constructor for the Adapter.
     * @param context The context in which this adapter is being created.
     * @param data The underlying data that should be represented.
     */
    QuizTopicAdapter(Context context, List<String> data) {
        this.inflater = LayoutInflater.from(context);
        this.quiz_lst = data;
    }

    /**
     * Bind a ViewHolder to the associated text. Uses the QuizViewHolder specialized inner class here
     * as a cast to ensure that the method is properly overridden.
     *
     * @param holder The QuizViewHolder object to bind
     * @param position The position in the underlying list to get display text from
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Populate the text of this underlying view.
        String topic = quiz_lst.get(position);
        QuizViewHolder thisHolder = ((QuizViewHolder) holder);
        thisHolder.getView().setText(topic);

        // If this item is the selected one, highlight it. Else, unhighlight it.
        if(position == current_selection) {
            thisHolder.getView().setBackgroundColor(Color.CYAN);
        }
        else {
            thisHolder.getView().setBackgroundColor(Color.WHITE);
        }
    }

    /**
     * Return the number of items in this list.
     * @return The size of the underlying data container.
     */
    @Override
    public int getItemCount() {
        if(quiz_lst == null)
            return 0;
        else
            return quiz_lst.size();
    }

    /**
     * Inflate a ViewHolder containing the quiz topic as a TextView. These will be the rows of this
     * RecyclerView. Returns ViewHolders native to this class.
     *
     * @param parent The Parent ViewGroup for inflation
     * @param viewType the type of view
     * @return A quiz_topics.xml view, used as a row in a RecyclerView
     */
    @NonNull @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.quiz_topics, parent, false);
        return new QuizViewHolder(v);
    }

    //! Personal ViewHolder class for use with this QuizTopicAdapter
    public class QuizViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView text;

        //! Public constructor for this custom ViewHolder.
        public QuizViewHolder(View view) {
            super(view);
            text = (TextView) view.findViewById(R.id.quiz_topic);
            view.setOnClickListener(this);
        }

        //! When clicked, simply call the onItemClick method implemented by the MainActivity.
        //  Also ensures that the click listener was properly set to avoid a NullPtrException.
        @Override
        public void onClick(View view) {
            // Call the onItemClickMethod from the parent activity implementing ClickListener below
            if (clickListener != null)
                clickListener.onItemClick(view, quiz_lst.get(getAdapterPosition()), getAdapterPosition());

            // Highlight the background of the clicked element by notifying Adapter that it has changed
            notifyItemChanged(current_selection);
            current_selection = getAdapterPosition();
            notifyItemChanged(current_selection);
        }

        /**
         * Getter for the held view in this class.
         * @return A TextView holding a Quiz Topic.
         */
        public TextView getView() {
            return text;
        }
    }

    //! A custom click listener interface with an onItemClick method for each item in the ListView
    public interface ClickListener {
        void onItemClick(View view, String quiz_name, int position);
    }

    //! Function to set an item's click listener to this interface (implemented by the parent class).
    void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }
}
