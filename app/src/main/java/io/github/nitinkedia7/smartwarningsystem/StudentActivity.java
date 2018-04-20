package io.github.nitinkedia7.smartwarningsystem;
// import android, java, Google Firebase libraries

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentActivity extends AppCompatActivity {
    private static final String TAG = "StudentActivity";
    private Button mJoinSessionButton;
    // Database references
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mSessionDatabaseReference;
    private DatabaseReference mStudentDatabaseReference;
    private FirebaseAuth mFirebaseAuth;

    private String mSessionName;
    private String mFullName, mUid;
    private boolean mIsEngaged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the view
        setContentView(R.layout.activity_student_dashboard);

        //Get FirebaseAuth instance
        mFirebaseAuth = FirebaseAuth.getInstance();
        //Get FirebaseDatabase instance
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        //Get reference to Database
        mDatabaseReference = mFirebaseDatabase.getReference();
        mSessionDatabaseReference = mDatabaseReference.child("Sessions");
        mStudentDatabaseReference = mDatabaseReference.child("Students");

        //connect button to the layout
        mJoinSessionButton = (Button) findViewById(R.id.joinSessionButton);

        //Get the current user (final because the user will not change)
        final FirebaseUser user = mFirebaseAuth.getCurrentUser();
        mUid = user.getUid();

        //addValueEventListener triggers whenever the in the given database reference is changed(also triggers when the listener is attached)
        //Get the name of the user using the uid from the database
        mStudentDatabaseReference.child(mUid).addValueEventListener(new ValueEventListener() {
            //OnDataChange triggers when the value in the given data reference changes
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    mFullName = dataSnapshot.child("fullName").getValue().toString();
                    mIsEngaged = Boolean.valueOf(dataSnapshot.child("isEngaged").getValue().toString());
                    mSessionName = dataSnapshot.child("currentSession").getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //handle database error
                Log.d(TAG, String.valueOf(databaseError.getCode()));
            }
        });

        //On click listener triggers when the button is clicked upon
        mJoinSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mIsEngaged) {
                    //Build an pop-up which asks the student to enter the session name to join and its password
                    AlertDialog.Builder builder = new AlertDialog.Builder(StudentActivity.this, R.style.MyDialogTheme);
                    //Title of the pop-up
                    builder.setTitle("Enter Session Details");
                    //set view of AlertDialog
                    View viewInflated = getLayoutInflater().inflate(R.layout.join_session_dialog, (ViewGroup) null, false);
                    // Set up the input(session name and password)
                    final EditText sessionNameField = (EditText) viewInflated.findViewById(R.id.courseToJoin);
                    final EditText sessionPasswordField = (EditText) viewInflated.findViewById(R.id.sessionPassword);
                    builder.setView(viewInflated);
                    // Set up the buttons(ok or cancel)
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        //if the student taps 'OK'
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Get the session name entered by the student
                            final String sessionName = sessionNameField.getText().toString();
                            //Attach a listener where the session exists in the database
                            mSessionDatabaseReference.child(sessionName).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    //Runs if session exists
                                    if (dataSnapshot.exists()) {
                                        //Runs if the session is active
                                        if (Boolean.valueOf(dataSnapshot.child("isActive").getValue().toString())) {
                                            //Get the correct password for the session
                                            String sessionPassword = dataSnapshot.child("sessionPassword").getValue().toString();
                                            //Authorise the student to join the session
                                            if (sessionPassword.equals(sessionPasswordField.getText().toString())) {
                                                //Detach the listener
                                                mSessionDatabaseReference.child(sessionName).removeEventListener(this);
                                                Student student = new Student(mFullName, 10, "Not Blacklisted", "None", user.getUid(), 10);
                                                //Enroll the student in the session
                                                saveUsertoSession(mUid, sessionName, student);
                                                //Fire up the Notification Activity
                                                Intent intent = new Intent(StudentActivity.this, NotificationActivity.class);
                                                //Pass the session name to the next activity
                                                intent.putExtra("sessionName", mSessionName);
                                                StudentActivity.this.startActivity(intent);

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
                                    //handle databse error
                                }
                            });
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        //if the student taps 'CANCEL'
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } else {
                    Toast.makeText(StudentActivity.this, "Already joined a session.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }
    //Create a small menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    //Runs when an option is selected from the main menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            //To sign out
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                StudentActivity.this.startActivity(new Intent(StudentActivity.this, LoginActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Enroll the student in the session
    private void saveUsertoSession(final String uid,final String session_name, final Student student){
        //Add in the joinedUsers list
        mSessionDatabaseReference.child(session_name).child("joinedUsers").child(uid).setValue(student);
        //Declare that the session is not empty anymore
        mSessionDatabaseReference.child(session_name).child("isUserJoined").setValue(true);
        //Add user in the alerts section of the session
        mSessionDatabaseReference.child(session_name).child("alerts").child(uid).child("sentAlerts").setValue("None");
        mSessionDatabaseReference.child(session_name).child("alerts").child(uid).child("unresponsiveAlerts").setValue("None");
        //Make the student "Engaged"
        mStudentDatabaseReference.child(uid).child("isEngaged").setValue("true");
        //Set current course of student as session_name
        mStudentDatabaseReference.child(uid).child("currentSession").setValue(session_name);
        Toast.makeText(StudentActivity.this, "Successfully Joined Session!", Toast.LENGTH_SHORT).show();
    }
}