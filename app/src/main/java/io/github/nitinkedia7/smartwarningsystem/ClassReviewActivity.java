package io.github.nitinkedia7.smartwarningsystem;


import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
//import android.content.DialogInterface;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ClassReviewActivity extends AppCompatActivity {
    private List<StudentState> studentList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ClassReviewAdapter mAdapter;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private String status;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_review);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_review);

        mAdapter = new ClassReviewAdapter(studentList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();


        mDatabaseReference.child("session").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
                        prepareStatusData((Map<String, Object>) dataSnapshot.child("joinedUsers").getValue());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });

//        prepareStatusData();
        recyclerView.addOnItemTouchListener(new NotificationTouchListener(getApplicationContext(), recyclerView, new NotificationTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                final StudentState student = studentList.get(position);
//                Toast.makeText(getApplicationContext(), "Opening Review Dialog", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(ClassReviewActivity.this, R.style.MyDialogTheme);
                builder.setTitle(student.getName());
                // I'm using fragment here so I'm using getView() to provide ViewGroup
                // but you can provide here any other instance of ViewGroup from your Fragment / Activity
                View viewInflated = getLayoutInflater().inflate(R.layout.dialog_review, (ViewGroup) null, false);
                // Set up the input
                final EditText review = (EditText) viewInflated.findViewById(R.id.input);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                builder.setView(viewInflated);
                // Set up the buttons
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
                        mDatabaseReference.child("session").child("joinedUsers").child(student.getUid()).child("review").setValue(review.getText().toString());
                        student.setReview(review.getText().toString());
                        mAdapter.notifyItemChanged(position);
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    private void prepareStatusData(Map<String,Object> students) {
        //iterate through each user, ignoring their UID
        Log.d("Review", Integer.toString(students.size()));
        for (Map.Entry<String, Object> entry : students.entrySet()){

            //Get user map
            Map singleUser = (Map) entry.getValue();
            String uid = entry.getKey();
            if(singleUser.get("isBlacklisted").toString().equals("Blacklisted")) {
                StudentState student = new StudentState(singleUser.get("name").toString(), Integer.valueOf(singleUser.get("state").toString()), singleUser.get("isBlacklisted").toString(), singleUser.get("review").toString(), uid);
                studentList.add(student);
            }
        }
        mAdapter.notifyDataSetChanged();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // API 5+ solution
                onBackPressed();
                Intent intent = new Intent(ClassReviewActivity.this, MainActivity.class);
                ClassReviewActivity.this.startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}