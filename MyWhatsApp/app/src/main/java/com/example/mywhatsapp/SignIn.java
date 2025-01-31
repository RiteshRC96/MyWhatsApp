package com.example.mywhatsapp;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mywhatsapp.Models.UsersDatabase;
import com.example.mywhatsapp.databinding.ActivitySignInBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SignIn extends AppCompatActivity {
    ActivitySignInBinding binding;
    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    SharedPreferences sharedPreferences;
    GoogleSignInClient mGoogleSignInClient;

    private static final String PREF_NAME = "MyAppPreferences";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final int RC_SIGN_IN = 65;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Check if the user is already signed in
        if (mAuth.getCurrentUser() != null && sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)) {
            // Redirect to MainActivity
            Intent intent = new Intent(SignIn.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Google Sign-In options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Google Sign-In button
        binding.btnGoogle.setOnClickListener(v -> signIn());

        // Redirect to Signup page
        binding.txtclickSignup.setOnClickListener(v -> {
            Intent intent = new Intent(SignIn.this, Signup.class);
            startActivity(intent);
        });

        // Email Sign-In button
        binding.btnSignin.setOnClickListener(v -> {
            String email = binding.txtmail.getText().toString();
            String password = binding.txtPassword.getText().toString();

            if (!isInternetConnected()) {
                Toast.makeText(SignIn.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            } else if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignIn.this, "Enter Credentials", Toast.LENGTH_SHORT).show();
            } else {
                binding.progressBar.setVisibility(View.VISIBLE);

                // Firebase sign-in with email
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            binding.progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                // Store sign-in status in SharedPreferences
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean(KEY_IS_LOGGED_IN, true);
                                editor.apply();

                                FirebaseUser user = mAuth.getCurrentUser();
                                saveUserToDatabase(user); // Store user in Realtime Database

                                // Redirect to MainActivity
                                Intent intent = new Intent(SignIn.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                String errorMsg = "Authentication Failed.";
                                if (task.getException() != null) {
                                    errorMsg = task.getException().getMessage();
                                }
                                Toast.makeText(SignIn.this, errorMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    // Method to check for internet connection
    private boolean isInternetConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    @Override
    public void onBackPressed() {
        // Close the app when the back button is pressed
        super.onBackPressed();
        finishAffinity();
    }

    // Google Sign-In
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w("SignIn", "Google sign in failed", e);
            }
        }
    }

    // Authenticate with Firebase using Google Sign-In
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUserToDatabase(user); // Store user in Realtime Database

                        Intent intent = new Intent(SignIn.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.w("SignIn", "signInWithCredential:failure", task.getException());
                    }
                });
    }

    // Method to save user to Firebase Realtime Database
    private void saveUserToDatabase(FirebaseUser user) {
        if (user != null) {
            UsersDatabase newUser = new UsersDatabase();
            newUser.setUserId(user.getUid());
            newUser.setUserName(user.getDisplayName() != null ? user.getDisplayName() : "");  // Default empty string if name is null
            newUser.setEmail(user.getEmail());
            newUser.setProfilepic(user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);

            // Store all users under 'Users' node using UID as the key
            firebaseDatabase.getReference().child("Users").child(user.getUid()).setValue(newUser);
        }
    }

}
