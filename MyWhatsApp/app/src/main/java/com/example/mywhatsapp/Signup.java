package com.example.mywhatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mywhatsapp.Models.UsersDatabase;
import com.example.mywhatsapp.databinding.ActivitySignupBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class Signup extends AppCompatActivity {
    ActivitySignupBinding binding;
    private FirebaseAuth mAuth;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        binding.btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = binding.txtusername.getText().toString().trim();
                String email = binding.txtmail.getText().toString().trim();
                String password = binding.txtPassword.getText().toString().trim();

                if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(Signup.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(Signup.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                }else {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    binding.progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        UsersDatabase user = new UsersDatabase(username, email, password);
                                        String id = Objects.requireNonNull(task.getResult().getUser()).getUid();
                                        database.getReference().child("UsersDatabase").child(id).setValue(user);

                                        Toast.makeText(Signup.this, "Account Created", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(Signup.this, SignIn.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        String errorMessage = Objects.requireNonNull(task.getException()).getMessage();
                                        Toast.makeText(Signup.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                                        Log.e("SignupError", errorMessage); // Logging the error
                                    }
                                }
                            });
                }
            }
        });

        binding.txtAlready.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Signup.this, SignIn.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity(); // Close the app when the back button is pressed
    }
}
