package io.github.nitinkedia7.smartwarningsystem;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;


public class StudentActivity extends AppCompatActivity {

    private String session_name, session_password, fullName, uid;
    private boolean isEngaged;

    private Button mJoinSessionButton;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mSessionDatabaseReference;
    private DatabaseReference mStudentDatabaseReference;
    private DatabaseReference mAlertsDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private ValueEventListener mValueEventListener;
    private ValueEventListener mValueEventListener2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);
        mFirebaseAuth = FirebaseAuth.getInstance();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mSessionDatabaseReference = mDatabaseReference.child("Sessions");
        mStudentDatabaseReference = mDatabaseReference.child("Students");

        mJoinSessionButton = (Button) findViewById(R.id.joinSessionButton);

        final FirebaseUser user = mFirebaseAuth.getCurrentUser();
        uid = user.getUid();
        final boolean eng;

        mStudentDatabaseReference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                fullName = dataSnapshot.child("fullName").getValue().toString();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
//                              System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        mJoinSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mValueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        isEngaged = Boolean.valueOf(dataSnapshot.child("isEngaged").getValue().toString());
                        Log.d("Student", String.valueOf(isEngaged));
                        mStudentDatabaseReference.child(uid).removeEventListener(mValueEventListener);

                        if (!isEngaged) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(StudentActivity.this, R.style.MyDialogTheme);
                            builder.setTitle("Enter Session Details");
                            View viewInflated = getLayoutInflater().inflate(R.layout.join_session_dialog, (ViewGroup) null, false);
                            // Set up the input
                            final EditText courseName = (EditText) viewInflated.findViewById(R.id.courseToJoin);
                            final EditText sessionPassword = (EditText) viewInflated.findViewById(R.id.sessionPassword);
                            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                            builder.setView(viewInflated);


                            // Set up the buttons
                            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    session_name = courseName.getText().toString();
                                    mValueEventListener2 = new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                if (Boolean.valueOf(dataSnapshot.child("isActive").getValue().toString())) {
                                                    session_password = dataSnapshot.child("sessionPassword").getValue().toString();
                                                    if (session_password.equals(sessionPassword.getText().toString())) {
                                                        mSessionDatabaseReference.child(courseName.getText().toString()).removeEventListener(mValueEventListener2);
                                                        StudentState studentState = new StudentState(fullName, 10, "Not Blacklisted", "None", user.getUid());
                                                        mSessionDatabaseReference.child(session_name).child("joinedUsers").child(uid).setValue(studentState);
                                                        mSessionDatabaseReference.child(session_name).child("isUserJoined").setValue(true);
                                                        mSessionDatabaseReference.child(session_name).child("alerts").child(uid).child("sentAlerts").setValue("None");
                                                        mSessionDatabaseReference.child(session_name).child("alerts").child(uid).child("unresponsiveAlerts").setValue("None");
                                                        mStudentDatabaseReference.child(uid).child("isEngaged").setValue("true");
                                                        mStudentDatabaseReference.child(uid).child("currentCourse").setValue(session_name);
                                                        Toast.makeText(StudentActivity.this, "Successfully Joined Session!", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(StudentActivity.this, NotificationActivity.class);
                                                        intent.putExtra("session_name", session_name);
                                                        intent.putExtra("fullName", fullName);
                                                        StudentActivity.this.startActivity(intent);
                                                        finish();
                                                    } else {
                                                        Toast.makeText(StudentActivity.this, "Password doesn't match", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    Toast.makeText(StudentActivity.this, "Session has been ended.", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(StudentActivity.this, "Course Name doesn't exist.", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
//                              System.out.println("The read failed: " + databaseError.getCode());
                                        }
                                    };
                                    mSessionDatabaseReference.child(courseName.getText().toString()).addValueEventListener(mValueEventListener2);
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

                        } else {
//                            mStudentDatabaseReference.child(user.getUid()).addListenerForSingleValueEvent(
//                                    new ValueEventListener() {
//                                        @Override
//                                        public void onDataChange(DataSnapshot dataSnapshot) {
//                                            mSessionDatabaseReference.child(user.getUid()).removeEventListener(this);
//                                            Toast.makeText(StudentActivity.this, "Already joined a session.", Toast.LENGTH_SHORT).show();
//                                            Intent intent = new Intent(StudentActivity.this, NotificationActivity.class);
//                                            intent.putExtra("session_name", dataSnapshot.child("currentCourse").getValue().toString());
//                                            intent.putExtra("fullName", dataSnapshot.child("fullName").getValue().toString());
//                                            StudentActivity.this.startActivity(intent);
//                                            finish();
//                                        }
//
//                                        @Override
//                                        public void onCancelled(DatabaseError databaseError) {
//                                            //handle databaseError
//                                        }
//                                    });
                            Toast.makeText(StudentActivity.this, "Already joined a session.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
//                              System.out.println("The read failed: " + databaseError.getCode());
                    }
                };
                mStudentDatabaseReference.child(uid).addValueEventListener(mValueEventListener);
                Log.d("Activity", String.valueOf(isEngaged));

            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                StudentActivity.this.startActivity(new Intent(StudentActivity.this, LoginActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}