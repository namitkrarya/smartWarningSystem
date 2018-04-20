package io.github.nitinkedia7.smartwarningsystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "Login";
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private EditText mEmailField, mPasswordField;
    private ProgressBar mProgressBar;
    private Button mSignupButton, mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get Firebase auth instance
        mFirebaseAuth = FirebaseAuth.getInstance();
        // If user is already logged go to next activity
        if(mFirebaseAuth.getCurrentUser() != null){
            Intent intent = new Intent(LoginActivity.this, StudentActivity.class);
            startActivity(intent);
            finish();
        }
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        // set the view now
        setContentView(R.layout.activity_login);
        // Assign all UI elements (text-box, buttons) to variables
        mEmailField = (EditText) findViewById(R.id.email);
        mPasswordField = (EditText) findViewById(R.id.password);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mSignupButton = (Button) findViewById(R.id.btn_signup);
        mLoginButton = (Button) findViewById(R.id.btn_login);

        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Eliminates leading and trailing spaces from input strings
                String email = mEmailField.getText().toString().trim();
                String password = mPasswordField.getText().toString().trim();
                // Validate- input email and password must be nonempty
                if (TextUtils.isEmpty(email)) {
                    mEmailField.setError("Required.");
                    return;
                } else {
                    mEmailField.setError(null);
                }

                if (TextUtils.isEmpty(password)) {
                    mPasswordField.setError("Required.");
                    return;
                } else {
                    mPasswordField.setError(null);
                }
                // Show authentication progress to user.
                mProgressBar.setVisibility(View.VISIBLE);
                //authenticate user
                mFirebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // Authorisation process complete, awaiting result toast, hide progessbar
                            mProgressBar.setVisibility(View.GONE);

                            if (!task.isSuccessful()) {
                                // Sign in unsuccessful, display message to user
                                Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                            } else {
                                // fetch user object from firebase and device token (for notifications)
                                final FirebaseUser user = mFirebaseAuth.getCurrentUser();
                                final String refreshedToken = FirebaseInstanceId.getInstance().getToken();

                                // Determine user type and redirect to appropriate dashboard, also update token in database
                                mDatabaseReference.child("Professors").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()) {
                                            // user is a student
                                            mDatabaseReference.child("Professors").child(user.getUid()).removeEventListener(this);
                                            mDatabaseReference.child("Professors").child(user.getUid()).child("token").setValue(refreshedToken);
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.d(TAG, String.valueOf(databaseError.getCode()));
                                    }
                                });
                                mDatabaseReference.child("Students").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()) {
                                            // user is a student
                                            mDatabaseReference.child("Students").child(user.getUid()).removeEventListener(this);
                                            mDatabaseReference.child("Students").child(user.getUid()).child("token").setValue(refreshedToken);
                                            Intent intent = new Intent(LoginActivity.this, StudentActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.d(TAG, String.valueOf(databaseError.getCode()));
                                    }
                                });
                            }
                        }
                    });
            }
        });
    }
}