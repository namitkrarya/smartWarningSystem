package io.github.nitinkedia7.smartwarningsystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private EditText mNameField, mEmailField, mPasswordField;
    private RadioGroup mUserTypeOptions;
    private RadioButton mSelectedUserType;
    private Button mSignInButton, mSignUpButton;
    private ProgressBar mProgressBar;
    private String userType, fullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Get Firebase auth instance
        mFirebaseAuth= FirebaseAuth.getInstance();
        // Get Firebase database references
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        // Assign all UI elements (text-box, buttons) to variables
        mSignInButton= (Button) findViewById(R.id.sign_in_button);
        mSignUpButton = (Button) findViewById(R.id.sign_up_button);
        mNameField = (EditText) findViewById(R.id.name);
        mEmailField = (EditText) findViewById(R.id.email);
        mPasswordField = (EditText) findViewById(R.id.password);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mUserTypeOptions = (RadioGroup) findViewById(R.id.userTypeRadioGroup);

        // Since Sign Up activity activity can only be opened from Sign In activity,
        // just finish this activity to go back to Sign In
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // Register user
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Eliminates leading and trailing spaces from input strings
                fullName = mNameField.getText().toString().trim();
                String email = mEmailField.getText().toString().trim();
                String password = mPasswordField.getText().toString().trim();
                // Find user type from selected radio button
                mSelectedUserType = (RadioButton) findViewById(mUserTypeOptions.getCheckedRadioButtonId());
                userType = mSelectedUserType.getText().toString();

                // Validate- input email,password and name must be nonempty
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

                if (TextUtils.isEmpty(fullName)) {
                    mPasswordField.setError("Required.");
                    return;
                } else {
                    mPasswordField.setError(null);
                }
                // Show authentication progress to user.
                mProgressBar.setVisibility(View.VISIBLE);
                // create user
                mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // Show authentication progress to user.
                            mProgressBar.setVisibility(View.GONE);
                            if (!task.isSuccessful()) {
                                // Sign Up unsuccessful, display message to user
                                Toast.makeText(SignupActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                            } else {
                                // Sign Up successful
                                FirebaseUser user = mFirebaseAuth.getCurrentUser();
                                String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                                // Make an userData object with additional user information i.e. Full Name, user type and device token and save it to database
                                AdditionalUserData userData = new AdditionalUserData(fullName, "false", refreshedToken, "None");
                                // Based on usertype redirect to appropriate dashboard
                                if (userType.equals("Professor")) {
                                    mDatabaseReference.child("Professors").child(user.getUid()).setValue(userData);
                                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    mDatabaseReference.child("Students").child(user.getUid()).setValue(userData);
                                    Intent intent = new Intent(SignupActivity.this, StudentActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        }
                    });
            }
        });
    }
}