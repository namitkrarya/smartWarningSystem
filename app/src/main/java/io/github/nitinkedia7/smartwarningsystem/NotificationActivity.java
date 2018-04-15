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
    private FirebaseAuth mFirebaseAuth;
    private String status;
    private String fullName, isBlacklisted;
    private String comment, state;
    private ChildEventListener mChildEventListener;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mFirebaseAuth.getCurrentUser();
        // Attach a listener to read the data at our posts reference
        mDatabaseReference.child("session").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                status = dataSnapshot.child("state").getValue().toString();
                if(dataSnapshot.child("joinedUsers").child(user.getUid()).child("isBlacklisted").getValue() != null) {
                    isBlacklisted = dataSnapshot.child("joinedUsers").child(user.getUid()).child("isBlacklisted").getValue().toString();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
//                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        mDatabaseReference.child("additionalUserData").child(user.getUid()).child("fullName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null) {
                    fullName = dataSnapshot.getValue().toString();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
//                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        new CountDownTimer(300000, 1000) {

            public void onTick(long millisUntilFinished) {
                Long remainingSec = millisUntilFinished/1000;

                if (remainingSec % 10 == 0 && status.equals("ACTIVE")) {
                    Random rand = new Random();

                    Integer randState = rand.nextInt(10)+1;
                    StudentState studentState = new StudentState(fullName,randState, isBlacklisted, "none", user.getUid());
                    mDatabaseReference.child("session").child("joinedUsers").child(user.getUid()).setValue(studentState);
                    if(mChildEventListener == null) {
                        mChildEventListener = new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                Alert alert = dataSnapshot.getValue(Alert.class);
                                state = alert.body;
                                comment = alert.title;
                                prepareAlertData(state, comment);
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
                        mDatabaseReference.child("session").child("alerts").child(user.getUid()).child("sentAlerts").addChildEventListener(mChildEventListener);
                    }
                }
            }
            public void onFinish() {

            }
        }.start();

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
                if(notification.getClickable()) {
                    Toast.makeText(getApplicationContext(), "Recorded your response", Toast.LENGTH_SHORT).show();
                    notification.setClickable(false);
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

    private void prepareAlertData(final String state, final String comment) {

        final notification alert = new notification(true, state, comment , "10", "Enabled");
        notificationList.add(alert);
        final FirebaseUser user = mFirebaseAuth.getCurrentUser();
        timer = new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {
                Long remainingSec = millisUntilFinished/1000;
                if(alert.getStatus().equals("Disabled")){
                    timer.cancel();
                }
//                if(remainingSec == 9) {
//                    alert.setTime(Long.toString(remainingSec));
//
//                }
                else{
                    alert.setTime(Long.toString(remainingSec));
                    recyclerView.setAdapter(mAdapter);
                }

            }
            public void onFinish() {
                alert.setTime("Time Up");
                alert.setStatus("Disabled");
                alert.setClickable(false);
                Alert unrespondedAlert = new Alert(alert.getState(), alert.getTime());
                mDatabaseReference.child("session").child("alerts").child(user.getUid()).child("unresponsiveAlerts").push().setValue(unrespondedAlert);
                mDatabaseReference.child("session").child("joinedUsers").child(user.getUid()).child("isBlacklisted").setValue("Blacklisted");


            }
        };
        timer.start();

        mAdapter.notifyDataSetChanged();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // API 5+ solution

                onBackPressed();
                Intent intent = new Intent(NotificationActivity.this, StudentActivity.class);
                NotificationActivity.this.startActivity(intent);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}