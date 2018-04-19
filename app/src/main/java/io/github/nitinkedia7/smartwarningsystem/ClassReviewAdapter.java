package io.github.nitinkedia7.smartwarningsystem;

import android.support.v7.widget.RecyclerView;

//import android.app.Notification;
import android.graphics.Movie;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class ClassReviewAdapter extends RecyclerView.Adapter<ClassReviewAdapter.MyViewHolder> {

    private List<StudentState> studentList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView state, name, status, review;

        public MyViewHolder(View view) {
            super(view);
            status = (TextView) view.findViewById(R.id.status);
            state = (TextView) view.findViewById(R.id.state);
            name = (TextView) view.findViewById(R.id.name);
            review = (TextView) view.findViewById(R.id.review);
        }
    }


    public ClassReviewAdapter(List<StudentState> studentList) {
        this.studentList = studentList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.review_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        StudentState class_review = studentList.get(position);
        holder.status.setText(class_review.getIsBlacklisted());
        holder.state.setText("State : " + class_review.getBlacklistedState());
        holder.name.setText(class_review.getName());
        holder.review.setText(class_review.getReview());
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }
}