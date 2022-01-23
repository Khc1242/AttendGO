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

public class AttendanceRecordActivity extends AppCompatActivity {
    FirebaseFirestore fStore;
    FirebaseAuth mAuth;
    private ImageView attendanceClosePage;
    private RecyclerView attendanceRecord;
    FirestoreRecyclerAdapter attAdapter;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_record);
        getSupportActionBar().hide();

        attendanceClosePage = findViewById(R.id.attendanceRecCloseIV);
        attendanceClosePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        attendanceRecord = findViewById(R.id.attendanceRecRV);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();

        // Query
        fStore = FirebaseFirestore.getInstance();
        Query query = fStore.collection("User").document(userID).collection("AttendanceRecord");
        // RecyclerOptions
        FirestoreRecyclerOptions<AttendanceModel> options = new FirestoreRecyclerOptions.Builder<AttendanceModel>()
                .setQuery(query, AttendanceModel.class)
                .build();

        attAdapter = new FirestoreRecyclerAdapter<AttendanceModel, AttendanceRecordActivity.AttendanceViewHolder>(options) {
            @NonNull
            @Override
            public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance, parent, false);
                return new AttendanceViewHolder(view);
            }
            @Override
            protected void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position, @NonNull AttendanceModel attModel) {
                holder.bind(attModel);
            }
        };

        LinearLayoutManager attendanceLayout = new LinearLayoutManager(this);
        attendanceLayout.setStackFromEnd(true);
        attendanceLayout.setReverseLayout(true);
        attendanceRecord.setHasFixedSize(true);
        attendanceRecord.setLayoutManager(attendanceLayout);
        attendanceRecord.setAdapter(attAdapter);
        attendanceRecord.setItemAnimator(null);
    }

    private static class AttendanceViewHolder extends RecyclerView.ViewHolder{
        private final TextView attendanceDate;
        private final TextView attendanceTime;
        AttendanceModel attModel;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            attendanceDate = itemView.findViewById(R.id.attDate);
            attendanceTime = itemView.findViewById(R.id.attTime);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(itemView.getContext(),AttendanceDetailsActivity.class);
                    intent.putExtra("model", attModel);
                    itemView.getContext().startActivity(intent);
                }
            });
        }

        public void bind(AttendanceModel attendanceModel) {
            attModel = attendanceModel;
            attendanceDate.setText(attendanceModel.getAttendanceDate());
            attendanceTime.setText(attendanceModel.getAttendanceTime());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        attAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        attAdapter.stopListening();
    }
}