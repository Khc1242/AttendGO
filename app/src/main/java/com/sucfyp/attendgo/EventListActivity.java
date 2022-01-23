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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class EventListActivity extends AppCompatActivity {
    FirebaseFirestore fStore;
    FirebaseAuth mAuth;
    private ImageView eventListClose;
    private RecyclerView eventList;
    private FirestoreRecyclerAdapter eventAdapter;
    String userID, userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);
        getSupportActionBar().hide();

        eventListClose = findViewById(R.id.eventListCloseIV);
        eventListClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        eventList = findViewById(R.id.evenListRV);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        userName = mAuth.getCurrentUser().getDisplayName();

        // Query
        fStore = FirebaseFirestore.getInstance();
        CollectionReference eventsRef = fStore.collection("Event");
        Query creatorQuery = eventsRef.whereEqualTo("CreatorID", userID);
        // RecyclerOptions
        FirestoreRecyclerOptions<EventsModel> options = new FirestoreRecyclerOptions.Builder<EventsModel>()
                .setQuery(creatorQuery, EventsModel.class)
                .build();

        eventAdapter = new FirestoreRecyclerAdapter<EventsModel, EventsViewHolder>(options) {
            @NonNull
            @Override
            public EventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
                return new EventsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull EventsViewHolder holder, int position, @NonNull EventsModel evModel) {
                holder.bind(evModel);
            }
        };

        LinearLayoutManager eventLayout = new LinearLayoutManager(this);
        eventLayout.setStackFromEnd(true);
        eventLayout.setReverseLayout(true);
        eventList.setHasFixedSize(true);
        eventList.setLayoutManager(eventLayout);
        eventList.setAdapter(eventAdapter);
        eventList.setItemAnimator(null);
    }

    private static class EventsViewHolder extends RecyclerView.ViewHolder{
        private final TextView item_eventName;
        private final TextView item_host;
        EventsModel evModel;

        public EventsViewHolder(@NonNull View itemView) {
            super(itemView);
            item_eventName = itemView.findViewById(R.id.item_eventName);
            item_host = itemView.findViewById(R.id.item_host);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(itemView.getContext(),EventDetailsActivity.class);
                    intent.putExtra("model", evModel);
                    itemView.getContext().startActivity(intent);
                }
            });
        }

        public void bind(EventsModel eventsModel) {
            evModel = eventsModel;
            item_eventName.setText(eventsModel.getEventName());
            item_host.setText(eventsModel.getHostName());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        eventAdapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        eventAdapter.startListening();
    }
}