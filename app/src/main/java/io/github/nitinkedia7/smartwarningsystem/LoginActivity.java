package io.github.nitinkedia7.smartwarningsystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ProgressBar progressBar;
    private Button btnSignup, btnLogin;
    public String userType;
    private ValueEventListener mValueEventListener;
    private ValueEventListener mValueEventListener2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        // set the view now
        setContentView(R.layout.activity_login);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnSignup = (Button) findViewById(R.id.btn_signup);
        btnLogin = (Button) findViewById(R.id.btn_login);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //authenticate user
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                progressBar.setVisibility(View.GONE);
                                if (!task.isSuccessful()) {
                                    // there was an error
                                    Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_LONG).show();
                                } else {
                                    final FirebaseUser user = auth.getCurrentUser();
                                    final String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                                    mValueEventListener = new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()) {
                                                    mDatabaseReference.child("Professors").child(user.getUid()).removeEventListener(mValueEventListener);
                                                    mDatabaseReference.child("Professors").child(user.getUid()).child("token").setValue(refreshedToken);
                                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                            }
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            System.out.println("The read failed: " + databaseError.getCode());
                                        }
                                    };
                                    mDatabaseReference.child("Professors").child(user.getUid()).addValueEventListener(mValueEventListener);
                                    mValueEventListener2 = new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()) {
                                                mDatabaseReference.child("Students").child(user.getUid()).removeEventListener(mValueEventListener2);
                                                mDatabaseReference.child("Students").child(user.getUid()).child("token").setValue(refreshedToken);
                                                Intent intent = new Intent(LoginActivity.this, StudentActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            System.out.println("The read failed: " + databaseError.getCode());
                                        }
                                    };
                                    mDatabaseReference.child("Students").child(user.getUid()).addValueEventListener(mValueEventListener2);
                                }
                            }
                        });
            }
        });
    }
}