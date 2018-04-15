package io.github.nitinkedia7.smartwarningsystem;

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

    private static final String TAG = "MainActivity";
    public static final String ANONYMOUS = "anonymous";
    public static final int RC_SIGN_IN = 1;
    private String status;
    private String password, name;

    private Button mJoinSessionButton;

    private String displayName;
    private String mUsername;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;
    private DatabaseReference mSessionDatabaseReference;
    private DatabaseReference mAlertsDatabaseReference;
    private FirebaseAuth mFirebaseAuth;

    private ChildEventListener mSessionEventListener;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);
        mFirebaseAuth = FirebaseAuth.getInstance();

        mUsername = ANONYMOUS;

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mSessionDatabaseReference = mFirebaseDatabase.getReference().child("session");
        mUsersDatabaseReference = mSessionDatabaseReference.child("joinedUsers");
        mAlertsDatabaseReference = mSessionDatabaseReference.child("alerts");

        mJoinSessionButton = (Button) findViewById(R.id.joinSessionButton);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    mDatabaseReference.child("additionalUserData").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue() != null) {
                                displayName = dataSnapshot.child("fullName").getValue().toString();
                                onSignedInIntialize(displayName);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
//                          System.out.println("The read failed: " + databaseError.getCode());
                        }
                    });

                } else {
                    onSignedOutCleanup();
                    startActivity(new Intent(StudentActivity.this, LoginActivity.class));
                }
            }
        };

        mJoinSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FirebaseUser user = mFirebaseAuth.getCurrentUser();
                final String uid = user.getUid();

                mDatabaseReference.child("session").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        status = dataSnapshot.child("state").getValue().toString();
                        name = dataSnapshot.child("courseName").getValue().toString();
                        password = dataSnapshot.child("sessionPassword").getValue().toString();
                        if(status.equals("INACTIVE")){
                            Toast.makeText(StudentActivity.this, "Session is INACTIVE", Toast.LENGTH_SHORT).show();
                        }
                        else {

                            final StudentState studentState = new StudentState(mUsername, 10, "Not Blacklisted", "None", user.getUid());

                            AlertDialog.Builder builder = new AlertDialog.Builder(StudentActivity.this);
                            builder.setTitle("Enter Session Details");
                            View viewInflated = getLayoutInflater().inflate(R.layout.join_session_dialog, (ViewGroup) null, false);
                            // Set up the input
                            final EditText courseName = (EditText) viewInflated.findViewById(R.id.courseToJoin);
                            final EditText sessionPassword = (EditText) viewInflated.findViewById(R.id.sessionPassword);

                            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                            builder.setView(viewInflated);

                            // Set up the buttons
//                            mDatabaseReference.child("session").addValueEventListener(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(DataSnapshot dataSnapshot) {
//
//                                }
//
//                                @Override
//                                public void onCancelled(DatabaseError databaseError) {
////                              System.out.println("The read failed: " + databaseError.getCode());
//                                }
//                            });

                            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if (!name.equals(courseName.getText().toString()) || !password.equals(sessionPassword.getText().toString())) {
                                        Toast.makeText(StudentActivity.this, "Incorrect Details!", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    } else {
                                        mUsersDatabaseReference.child(uid).setValue(studentState);
                                        mSessionDatabaseReference.child("isUserJoined").setValue("true");
                                        mAlertsDatabaseReference.child(uid).child("sentAlerts").setValue("None");
                                        mAlertsDatabaseReference.child(uid).child("unresponsiveAlerts").setValue("None");
                                        Toast.makeText(StudentActivity.this, "Successfully Joined Session!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(StudentActivity.this, NotificationActivity.class);
                                        StudentActivity.this.startActivity(intent);
                                        dialog.dismiss();
                                    }
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
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
//                      System.out.println("The read failed: " + databaseError.getCode());
                    }
                });

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled Signed in!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    private void onSignedInIntialize(java.lang.String username) {
        mUsername = username;
    }
    private void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
    }
}
