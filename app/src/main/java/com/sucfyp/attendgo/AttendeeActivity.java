package com.sucfyp.attendgo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class AttendeeActivity extends AppCompatActivity {
    FirebaseFirestore fStore;
    FirebaseAuth mAuth;
    private ImageView attendeeClosePage;
    private RecyclerView attendees;
    FirestoreRecyclerAdapter attendeeAdapter;
    String userID, eventID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendee);
        getSupportActionBar().hide();

        Intent intent = getIntent();
        eventID = intent.getStringExtra("EVENT_ID");

        attendeeClosePage = findViewById(R.id.attendeeCloseIV);
        attendeeClosePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        attendees = findViewById(R.id.attendeeRV);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();

        // Query
        fStore = FirebaseFirestore.getInstance();
        Query query = fStore.collection("Event").document(eventID).collection("Attendance");
        // RecyclerOptions
        FirestoreRecyclerOptions<AttendeeModel> options = new FirestoreRecyclerOptions.Builder<AttendeeModel>()
                .setQuery(query, AttendeeModel.class)
                .build();

        attendeeAdapter = new FirestoreRecyclerAdapter<AttendeeModel, AttendeeActivity.AttendeeViewHolder>(options) {
            @NonNull
            @Override
            public AttendeeActivity.AttendeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendee, parent, false);
                return new AttendeeActivity.AttendeeViewHolder(view);
            }
            @Override
            protected void onBindViewHolder(@NonNull AttendeeActivity.AttendeeViewHolder holder, int position, @NonNull AttendeeModel attendeeModel) {
                holder.bind(attendeeModel);
            }
        };

        LinearLayoutManager attendeeLayout = new LinearLayoutManager(this);
        attendeeLayout.setStackFromEnd(true);
        attendeeLayout.setReverseLayout(true);
        attendees.setHasFixedSize(true);
        attendees.setLayoutManager(attendeeLayout);
        attendees.setAdapter(attendeeAdapter);
        attendees.setItemAnimator(null);
    }

    private static class AttendeeViewHolder extends RecyclerView.ViewHolder{
        private final TextView attendeeName;
        private final TextView attendanceDate;
        private final TextView attendanceTime;

        public AttendeeViewHolder(@NonNull View itemView) {
            super(itemView);
            attendeeName = itemView.findViewById(R.id.attendee_Name);
            attendanceDate = itemView.findViewById(R.id.attendance_Date);
            attendanceTime = itemView.findViewById(R.id.attendance_Time);
        }

        public void bind(AttendeeModel attendeeModel) {
            attendeeName.setText(attendeeModel.getAttendeeName());
            attendanceDate.setText(attendeeModel.getAttendanceDate());
            attendanceTime.setText(attendeeModel.getAttendanceTime());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        attendeeAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        attendeeAdapter.stopListening();
    }
}