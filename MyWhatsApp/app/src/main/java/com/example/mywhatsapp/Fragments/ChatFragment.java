package com.example.mywhatsapp.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.mywhatsapp.Adapter.UserAdapter;
import com.example.mywhatsapp.Models.UsersDatabase;
import com.example.mywhatsapp.R;
import com.example.mywhatsapp.databinding.FragmentChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatFragment extends Fragment {

    public ChatFragment() {
    // empty condtructor
    }

    FragmentChatBinding binding;
    ArrayList <UsersDatabase> list = new ArrayList<>();
    FirebaseDatabase database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        database = FirebaseDatabase.getInstance();

        UserAdapter adapter = new UserAdapter(list, getContext());
        binding.chatRecyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        database.getReference().child("UsersDatabase").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    UsersDatabase users = dataSnapshot.getValue(UsersDatabase.class);
                    assert users != null;
                    users.setUserId(dataSnapshot.getKey());
                    if(!users.getUserId().equals(FirebaseAuth.getInstance().getUid())){
                        list.add(users);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Inflate the layout for this fragment
        return binding.getRoot();
    }
}