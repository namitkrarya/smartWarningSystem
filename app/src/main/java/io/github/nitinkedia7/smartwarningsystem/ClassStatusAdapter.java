package io.github.nitinkedia7.smartwarningsystem;

import android.support.v7.widget.RecyclerView;

//import android.app.Notification;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ClassStatusAdapter extends RecyclerView.Adapter<ClassStatusAdapter.MyViewHolder> {

    private List<Student> studentList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView state, name, status;

        public MyViewHolder(View view) {
            super(view);
            status = (TextView) view.findViewById(R.id.status);
            state = (TextView) view.findViewById(R.id.state);
            name = (TextView) view.findViewById(R.id.name);
        }
    }


    public ClassStatusAdapter(List<Student> studentList) {
        this.studentList = studentList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.status_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
       Student class_status = studentList.get(position);
        holder.status.setText(class_status.getIsBlacklisted());
        holder.state.setText("State : " + class_status.getState());
        holder.name.setText(class_status.getName());
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }
}