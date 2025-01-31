package com.example.mywhatsapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mywhatsapp.Adapter.ChatAdapter;
import com.example.mywhatsapp.Models.MessageModel;
import com.example.mywhatsapp.databinding.ActivityChatDetailBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class ChatDetail extends AppCompatActivity {
    ActivityChatDetailBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Hide the toolbar
        getSupportActionBar().hide();

        // Initialize Firebase database and authentication
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get user data from intent extras
        final String senderId = auth.getUid();
        String receiverId = getIntent().getStringExtra("userId");
        String userName = getIntent().getStringExtra("userName");
       // String profilePic = getIntent().getStringExtra("profilePic");

        // Add null checks to prevent crashes if any data is missing
        if (receiverId == null) {
            Toast.makeText(this, "receiverId", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity to prevent further errors
            return;
        }
        if (userName == null) {
            Toast.makeText(this, "UserName", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity to prevent further errors
            return;
        }
        if (senderId == null) {
            Toast.makeText(this, "SenderId", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity to prevent further errors
            return;
        }
//        if (profilePic == null) {
//            Toast.makeText(this, "profilePic", Toast.LENGTH_SHORT).show();
//            finish(); // Close the activity to prevent further errors
//            return;
//        }

        // Set up user name and profile picture
        binding.username.setText(userName);
       // Picasso.get().load(profilePic).placeholder(R.drawable.avatar1).into(binding.profileImage);

        // Set the back arrow button functionality
        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatDetail.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        // Initialize message model list and adapter
        final ArrayList<MessageModel> messageModels = new ArrayList<>();
        final ChatAdapter chatAdapter = new ChatAdapter(messageModels, this, receiverId);

        binding.chatRecyclerView.setAdapter(chatAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        // Define sender and receiver rooms
        final String senderRoom = senderId + receiverId;
        final String receiverRoom = receiverId + senderId;

        // Add listener to load chat messages
        database.getReference().child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageModels.clear(); // Clear old messages before adding new ones
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            MessageModel model = snapshot1.getValue(MessageModel.class);
                            if (model != null) {
                                model.setMessageId(snapshot1.getKey());
                                messageModels.add(model);
                            }
                        }
                        chatAdapter.notifyDataSetChanged(); // Notify adapter about the data change
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseError", "Error reading chat data: " + error.getMessage());
                        Toast.makeText(ChatDetail.this, "Failed to load chat data", Toast.LENGTH_SHORT).show();
                    }
                });

        // Send message when the send button is clicked
        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = binding.entermsg.getText().toString().trim();
                if (message.isEmpty()) {
                    Toast.makeText(ChatDetail.this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
                    return; // Prevent sending empty messages
                }

                final MessageModel model = new MessageModel(senderId, message);
                model.setTimestamp(new Date().getTime()); // Set the message timestamp
                binding.entermsg.setText(""); // Clear the message input after sending

                // Send message to the sender's room
                database.getReference().child("chats")
                        .child(senderRoom).push()
                        .setValue(model)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                // Send message to the receiver's room after successful send to sender room
                                database.getReference().child("chats")
                                        .child(receiverRoom)
                                        .push()
                                        .setValue(model)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                // Handle success for receiver room if needed
                                            }
                                        });
                            }
                        });
            }
        });
    }
}
