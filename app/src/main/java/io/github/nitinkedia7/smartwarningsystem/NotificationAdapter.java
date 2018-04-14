package io.github.nitinkedia7.smartwarningsystem;

import android.support.v7.widget.RecyclerView;

//import android.app.Notification;
import android.graphics.Movie;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder> {

    private List<notification> notificationsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView state, time, comment, status;
        public boolean isClickable = true;

        public MyViewHolder(View view) {
            super(view);
            status = (TextView) view.findViewById(R.id.status);
            state = (TextView) view.findViewById(R.id.state);
            comment = (TextView) view.findViewById(R.id.comment);
            time = (TextView) view.findViewById(R.id.time);
        }
    }


    public NotificationAdapter(List<notification> notificationsList) {
        this.notificationsList = notificationsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alert_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        notification notification = notificationsList.get(position);
        holder.status.setText(notification.getStatus());
        holder.state.setText(notification.getState());
        holder.comment.setText(notification.getComment());
        holder.time.setText(notification.getTime());
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }
}