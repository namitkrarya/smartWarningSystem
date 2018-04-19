package io.github.nitinkedia7.smartwarningsystem;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
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
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
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
import java.util.Random;

public class NotificationActivity extends AppCompatActivity {
    private List<notification> notificationList = new ArrayList<>();
    private RecyclerView recyclerView;
    private NotificationAdapter mAdapter;
    private static final String TAG = "NotificationActivity";

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mSessionReference;
    private FirebaseAuth mFirebaseAuth;
    private ChildEventListener mChildEventListener;
    private boolean status;
    private String fullName, isBlacklisted;
    private String comment, state, sessionName;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sessionName = getIntent().getStringExtra("session_name");
        fullName = getIntent().getStringExtra("fullName");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mSessionReference = mDatabaseReference.child("Sessions").child(sessionName);

        mFirebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mFirebaseAuth.getCurrentUser();
        // Attach a listener to read the data at our posts reference

        mSessionReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                status = Boolean.valueOf(dataSnapshot.child("isActive").getValue().toString());
                if(!status){
                    mSessionReference.removeEventListener(this);
                    Toast.makeText(getApplicationContext(), "Session has been terminated", Toast.LENGTH_SHORT).show();
                    finish();
                }
                isBlacklisted = dataSnapshot.child("joinedUsers").child(user.getUid()).child("isBlacklisted").getValue().toString();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
//                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        timer = new CountDownTimer(300000, 1000) {
            public void onTick(long millisUntilFinished) {
                Long remainingSec = millisUntilFinished/1000;
                if (remainingSec % 10 == 0 && status) {
                    Random rand = new Random();
                    Integer randState = rand.nextInt(10)+1;

                    mSessionReference.child("joinedUsers").child(user.getUid()).child("state").setValue(randState.toString());
                    if(mChildEventListener == null) {
                        mChildEventListener = new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                notification notification = dataSnapshot.getValue(notification.class);
                                prepareAlertData(notification);
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        };
                        mSessionReference.child("alerts").child(user.getUid()).child("sentAlerts").addChildEventListener(mChildEventListener);
                    }
                }
            }
            public void onFinish() {

            }
        };
        timer.start();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mAdapter = new NotificationAdapter(notificationList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);


        recyclerView.addOnItemTouchListener(new NotificationTouchListener(getApplicationContext(), recyclerView, new NotificationTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {

                notification notification = notificationList.get(position);
                if(notification.getStatus().equals("Enabled")) {
                    Toast.makeText(getApplicationContext(), "Recorded your response", Toast.LENGTH_SHORT).show();
                    notification.setStatus("Disabled");
                    notification.setTime("Recorded your response");
                    recyclerView.setAdapter(mAdapter);
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    private void prepareAlertData(final notification notification) {
        notificationList.add(notification);
        final FirebaseUser user = mFirebaseAuth.getCurrentUser();
        timer = new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {
                Long remainingSec = millisUntilFinished/1000;
                if(notification.getStatus().equals("Disabled")){
                    timer.cancel();
                } else{
                    notification.setTime(Long.toString(remainingSec));
                    recyclerView.setAdapter(mAdapter);
                }

            }
            public void onFinish() {
                notification.setTime("Time Up");
                notification.setStatus("Disabled");
                mSessionReference.child("alerts").child(user.getUid()).child("unresponsiveAlerts").push().setValue(notification);
                mSessionReference.child("joinedUsers").child(user.getUid()).child("isBlacklisted").setValue("Blacklisted");
                mSessionReference.child("joinedUsers").child(user.getUid()).child("blacklistedState").setValue(Integer.valueOf(notification.getState()));
            }
        };
        timer.start();
        mAdapter.notifyDataSetChanged();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(NotificationActivity.this, StudentActivity.class);
                NotificationActivity.this.startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}