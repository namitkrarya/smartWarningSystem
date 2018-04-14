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


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final String ANONYMOUS = "anonymous";
    public static final int RC_SIGN_IN = 1;
    private String course_name = "";
    private String status = "";
    private String session_password = "";
    private String password = "", name = "";


    private Button mCreateSessionButton;
    private Button mJoinSessionButton;
    private Button mEndSessionButton;
    private Button mClassStatusButton;
    private Button mClassReviewButton;

    private String displayName;
    private String userType = "";
    private String mUsername;
    private String mUsertype;
    private String usertype;

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
        setContentView(R.layout.activity_dashboard);
        mFirebaseAuth = FirebaseAuth.getInstance();

        mUsername = ANONYMOUS;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mUsertype = extras.getString("mUsertype");
            usertype = extras.getString("mUsertype");
        }

        if(mUsertype == null){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivity(intent);
            finish();
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mSessionDatabaseReference = mFirebaseDatabase.getReference().child("session");
        mUsersDatabaseReference = mSessionDatabaseReference.child("joinedUsers");
        mAlertsDatabaseReference = mSessionDatabaseReference.child("alerts");


        mCreateSessionButton = (Button) findViewById(R.id.createSessionButton);
        mJoinSessionButton = (Button) findViewById(R.id.joinSessionButton);
        mEndSessionButton = (Button) findViewById(R.id.endSessionButton);
        mClassStatusButton = (Button) findViewById(R.id.classStatusButton);
        mClassReviewButton = (Button) findViewById(R.id.classReviewButton);
        Toast.makeText(MainActivity.this, mUsertype, Toast.LENGTH_LONG).show();
        if(usertype.equals("Professor")){
            mJoinSessionButton.setVisibility(View.INVISIBLE);
        }
        else{
            mCreateSessionButton.setVisibility(View.INVISIBLE);
            mClassStatusButton.setVisibility(View.INVISIBLE);
            mEndSessionButton.setVisibility(View.INVISIBLE);
            mClassReviewButton.setVisibility(View.INVISIBLE);

        }

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
                                userType = dataSnapshot.child("userType").getValue().toString();
                                onSignedInIntialize(displayName, userType);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
//                System.out.println("The read failed: " + databaseError.getCode());
                        }
                    });

                } else {
                    onSignedOutCleanup();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }
        };

        mCreateSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
                        mSessionDatabaseReference.child("courseName").setValue(course_name);
                        mSessionDatabaseReference.child("sessionPassword").setValue(session_password);

                        mSessionDatabaseReference.child("state").setValue("ACTIVE");
                        mUsersDatabaseReference.setValue("None");
                        mAlertsDatabaseReference.setValue("None");
                        Toast.makeText(MainActivity.this, "Successfully created session!", Toast.LENGTH_LONG).show();
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
        });
        mEndSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSessionDatabaseReference.child("state").setValue("INACTIVE");
//                mUsersDatabaseReference.setValue("None");
//                mAlertsDatabaseReference.setValue("None");
                Toast.makeText(MainActivity.this, "Session ended successfully!", Toast.LENGTH_LONG).show();
            }
        });

        mJoinSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                final String uid = user.getUid();

                mDatabaseReference.child("session").child("state").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        status = dataSnapshot.getValue().toString();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
//                System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
                if(status.equals("INACTIVE")){
                    Toast.makeText(MainActivity.this, "Session is INACTIVE", Toast.LENGTH_SHORT).show();
                }
                else {

                    final StudentState studentState = new StudentState(mUsername, 10, "Not Blacklisted", "none");

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Enter Session Details");
                    View viewInflated = getLayoutInflater().inflate(R.layout.join_session_dialog, (ViewGroup) null, false);
                    // Set up the input
                    final EditText courseName = (EditText) viewInflated.findViewById(R.id.courseToJoin);
                    final EditText sessionPassword = (EditText) viewInflated.findViewById(R.id.sessionPassword);

                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    builder.setView(viewInflated);

                    // Set up the buttons
                    mDatabaseReference.child("session").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            name = dataSnapshot.child("courseName").getValue().toString();
                            password = dataSnapshot.child("sessionPassword").getValue().toString();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
//                System.out.println("The read failed: " + databaseError.getCode());
                        }
                    });

                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {



                            if (!name.equals(courseName.getText().toString()) || !password.equals(sessionPassword.getText().toString())) {
                                Toast.makeText(MainActivity.this, "Incorrect Details!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                mUsersDatabaseReference.child(uid).setValue(studentState);
                                mAlertsDatabaseReference.child(uid).child("sentAlerts").setValue("None");
                                mAlertsDatabaseReference.child(uid).child("unresponsiveAlerts").setValue("None");
                                Toast.makeText(MainActivity.this, "Successfully Joined Session!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
                                MainActivity.this.startActivity(intent);
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
        });

        mClassStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Showing class status!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, ClassStatusActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        mClassReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Showing class review!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, ClassReviewActivity.class);
                MainActivity.this.startActivity(intent);
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

    private void onSignedInIntialize(java.lang.String username,java.lang.String usertype) {
        mUsername = username;
        mUsertype = usertype;
    }
    private void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
    }
}
