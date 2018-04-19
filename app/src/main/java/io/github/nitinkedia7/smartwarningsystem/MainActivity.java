package io.github.nitinkedia7.smartwarningsystem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
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
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private String course_name = "";
    private String session_password = "";

    private Button mCreateSessionButton;
    private Button mEndSessionButton;
    private Button mClassStatusButton;
    private Button mClassReviewButton;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mSessionDatabaseReference;
    private DatabaseReference mProfessorDatabaseReference;
    private DatabaseReference mStudentDatabaseReference;

    private FirebaseAuth mFirebaseAuth;

    private ChildEventListener mSessionEventListener;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        mFirebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mFirebaseAuth.getCurrentUser();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mSessionDatabaseReference = mFirebaseDatabase.getReference().child("Sessions");
        mProfessorDatabaseReference = mDatabaseReference.child("Professors");
        mStudentDatabaseReference = mDatabaseReference.child("Students");

        mCreateSessionButton = (Button) findViewById(R.id.createSessionButton);
        mEndSessionButton = (Button) findViewById(R.id.endSessionButton);
        mClassStatusButton = (Button) findViewById(R.id.classStatusButton);
        mClassReviewButton = (Button) findViewById(R.id.classReviewButton);

        mProfessorDatabaseReference.child(user.getUid()).child("currentCourse").addListenerForSingleValueEvent(
            new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
//                    mProfessorDatabaseReference.child(user.getUid()).child("currentCourse").removeEventListener(this);
                    course_name = dataSnapshot.getValue().toString();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //handle databaseError
                }
            });

        mCreateSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            mProfessorDatabaseReference.child(user.getUid()).child("isEngaged").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(!Boolean.valueOf(dataSnapshot.getValue().toString())) {
                            mProfessorDatabaseReference.child(user.getUid()).child("isEngaged").removeEventListener(this);
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
                            builder.setTitle("Enter Course Details");
                            View viewInflated = getLayoutInflater().inflate(R.layout.create_session_dialog, (ViewGroup) null, false);
                            final EditText courseName = (EditText) viewInflated.findViewById(R.id.courseName);
                            final EditText sessionPassword = (EditText) viewInflated.findViewById(R.id.sessionPassword);
                            builder.setView(viewInflated);
                            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    session_password = sessionPassword.getText().toString();
                                    course_name = courseName.getText().toString();
                                    if(TextUtils.isEmpty(course_name)){
                                        Toast.makeText(MainActivity.this, "Please enter a course name", Toast.LENGTH_LONG).show();
                                    }
                                    else if(TextUtils.isEmpty(session_password)){
                                        Toast.makeText(MainActivity.this, "Please enter a session password", Toast.LENGTH_LONG).show();
                                    }
                                    else{
                                        mSessionDatabaseReference.child(course_name).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                mSessionDatabaseReference.child(course_name).removeEventListener(this);
                                                if(dataSnapshot.exists()){
                                                    Toast.makeText(MainActivity.this, "Session already running!", Toast.LENGTH_LONG).show();
                                                }
                                                else{
                                                    saveSessionDetails(user.getUid(), course_name, session_password);
                                                }
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
//                                        saveSessionDetails(user.getUid(), course_name, session_password);
                                    }
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
                            Toast.makeText(MainActivity.this, "Current Course is Active", Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });
            }
        });

        mEndSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(course_name.equals("None")){
                Toast.makeText(MainActivity.this, "You are not running any course :(", Toast.LENGTH_LONG).show();
            }
            else {
                mSessionDatabaseReference.child(course_name).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
//                                mSessionDatabaseReference.child(course_name).removeEventListener(this);
                                mSessionDatabaseReference.child(course_name).addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(Boolean.valueOf(dataSnapshot.child("isUserJoined").getValue().toString())) {
                                                disengageStudents((Map<String, Object>) dataSnapshot.child("joinedUsers").getValue());
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            //handle databaseError
                                        }
                                    });
                                mProfessorDatabaseReference.child(user.getUid()).child("isEngaged").setValue("false");
                                mProfessorDatabaseReference.child(user.getUid()).child("currentCourse").setValue("None");
                                mSessionDatabaseReference.child(course_name).removeEventListener(this);
                                mSessionDatabaseReference.child(course_name).child("isActive").setValue(false);
                                Toast.makeText(MainActivity.this, "Session ended successfully!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this, "No course created", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            //handle databaseError
                        }
                    });
            }
            }
        });

        mClassStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(course_name.equals("None")){
                Toast.makeText(MainActivity.this, "You are not running any course :(", Toast.LENGTH_LONG).show();
            }
            else {
                mSessionDatabaseReference.child(course_name).child("isUserJoined").addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (Boolean.valueOf(dataSnapshot.getValue().toString())) {
                                mSessionDatabaseReference.child(course_name).child("isUserJoined").removeEventListener(this);
                                Toast.makeText(MainActivity.this, "Showing class status!", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(MainActivity.this, ClassStatusActivity.class);
                                intent.putExtra("course_name", course_name);
                                MainActivity.this.startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "No Students Joined!", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            //handle databaseError
                        }
                    });
            }
            }
        });

        mClassReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(course_name.equals("None")){
                Toast.makeText(MainActivity.this, "You are not running any course :(", Toast.LENGTH_LONG).show();
            }
            else {
                mSessionDatabaseReference.child(course_name).child("isUserJoined").addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (Boolean.valueOf(dataSnapshot.getValue().toString())) {
                                mSessionDatabaseReference.child(course_name).child("isUserJoined").removeEventListener(this);
                                Toast.makeText(MainActivity.this, "Showing class review!", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(MainActivity.this, ClassReviewActivity.class);
                                intent.putExtra("course_name", course_name);
                                MainActivity.this.startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "No Students Joined!", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            //handle databaseError
                        }
                    });
            }
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
                MainActivity.this.startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void disengageStudents(Map<String,Object> students) {

        for (Map.Entry<String, Object> entry : students.entrySet()){
            //Get user map
            String uid = entry.getKey();
            mStudentDatabaseReference.child(uid).child("isEngaged").setValue("false");
            mStudentDatabaseReference.child(uid).child("currentCourse").setValue("None");
        }

    }

    private void saveSessionDetails(final String uid, final String course_name, final String session_password){
        mProfessorDatabaseReference.child(uid).child("isEngaged").setValue("true");
        mProfessorDatabaseReference.child(uid).child("currentCourse").setValue(course_name);

        mSessionDatabaseReference.child(course_name).child("isActive").setValue(true);
        mSessionDatabaseReference.child(course_name).child("isUserJoined").setValue(false);
        mSessionDatabaseReference.child(course_name).child("sessionPassword").setValue(session_password);
        mSessionDatabaseReference.child(course_name).child("alerts").setValue("None");
        mSessionDatabaseReference.child(course_name).child("joinedUsers").setValue("None");
        Toast.makeText(MainActivity.this, "Successfully created session!", Toast.LENGTH_LONG).show();
    }

}
