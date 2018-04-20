package io.github.nitinkedia7.smartwarningsystem;

// import android and java libraries
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {
    // List of student to be displayed
    private List<Student> mStudentList;
    // Flag indication whether to display review
    public Boolean displayReview;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        // Attributes of each student are assigned to corresponding xml components
        public TextView state, name, status, review;
        public MyViewHolder(View view) {
            super(view);
            status = (TextView) view.findViewById(R.id.status);
            state = (TextView) view.findViewById(R.id.state);
            name = (TextView) view.findViewById(R.id.name);
            if (displayReview) review = (TextView) view.findViewById(R.id.review);
        }
    }
    // Set the list (array) of student objects to be displayed
    public RecyclerViewAdapter(List<Student> mStudentList) {
        this.mStudentList = mStudentList;
    }
    // Select the type of tile layout to be used
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        // Tile for ClassStatus and ClassReview show similar but different attributes
        // So choose tile layout accordingly
        if (displayReview) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.review_list_row, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.status_list_row, parent, false);
        }
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // Give labels to attributes of each student
        Student joinedStudent = mStudentList.get(position);
        holder.status.setText(joinedStudent.getIsBlacklisted());
        if (this.displayReview) holder.state.setText("Last Blacklisted State: " + joinedStudent.getBlacklistedState());
        else holder.state.setText("Current State: " + joinedStudent.getState());
        holder.name.setText(joinedStudent.getName());
        if (this.displayReview) holder.review.setText("Review: " + joinedStudent.getReview());
    }

    @Override
    public int getItemCount() {
        return mStudentList.size(); // get number of joined students
    }
}