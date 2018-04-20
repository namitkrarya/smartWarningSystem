package io.github.nitinkedia7.smartwarningsystem;

// import android, java, Google Firebase libraries
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ClassStatusActivity extends AppCompatActivity {
    private static final String TAG = "ClassStatus";
    // an array of objects containing student details
    private List<Student> mStudentList = new ArrayList<>();
    // mAdapter is an object that configures the xml for displaying list
    private RecyclerView recyclerView;
    private RecyclerViewAdapter mAdapter;
    // Database Variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ValueEventListener mValueEventListener;
    // name of current session
    private String mSessionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO- change name of xml file
        setContentView(R.layout.activity_notification);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Session name was passed from dashboard as it will be used in database access
        mSessionName = getIntent().getStringExtra("sessionName");

        // Assigning the studentlist to adapter which inturn displays the xml to display list
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new RecyclerViewAdapter(mStudentList);
        mAdapter.displayReview = false;
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        // connecting to firebase database
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        // Attach a listener to the location of joined students in database to obtain joined student list
        // Path is "/Sessions/{msessionName}/joinedUsers/"
        mValueEventListener = new ValueEventListener() {
            @Override
            // onDataChange runs each time data is changed in above path and returns the whole data in snapshot
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Get map of users in datasnapshot and pass this list to prepareStatusData
                prepareStatusData((Map<String,Object>) dataSnapshot.getValue());
                mDatabaseReference.child("Sessions").child(mSessionName).child("joinedUsers").removeEventListener(mValueEventListener);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                //handle databaseError
                Log.d(TAG, String.valueOf(databaseError.getCode()));
            }
        };
        mDatabaseReference.child("Sessions").child(mSessionName).child("joinedUsers").addValueEventListener(mValueEventListener);
    }

    // this function passes each fetched student object into the adapter for display.
    private void prepareStatusData(Map<String,Object> students) {
        //iterate through each user
        for (Map.Entry<String, Object> entry : students.entrySet()){
            //Get user map and Uid and typecast it to Student object
            Map joinedStudent = (Map) entry.getValue();
            String uid = entry.getKey();
            Student student = new Student(joinedStudent.get("name").toString(),
                    Integer.valueOf(joinedStudent.get("state").toString()),
                    joinedStudent.get("isBlacklisted").toString(),
                    joinedStudent.get("review").toString(), uid,
                    Integer.valueOf(joinedStudent.get("blacklistedState").toString())
            );
            mStudentList.add(student);
        }
        mAdapter.notifyDataSetChanged(); // start loading into xml
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Revert to dashboard when back button is pressed
                Intent intent = new Intent(ClassStatusActivity.this, MainActivity.class);
                ClassStatusActivity.this.startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}