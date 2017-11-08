package com.uottawa.dutyhelper;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "EmailPassword";
    
    private FirebaseAuth mAuth;
    
    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mLoginButton;
    private Button mSignUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initializing user and password
        mEmailField = (EditText) findViewById(R.id.email_edit_text);
        mPasswordField = (EditText) findViewById(R.id.password_edit_text);
        mLoginButton = (Button) findViewById(R.id.btn_login); 
        mSignUpButton = (Button) findViewById(R.id.btn_sign_up);

        //initializing Onclick listener which is implemented in this class
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpScreen = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(signUpScreen);
            }
        });
        
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

    }


    @Override
    public void onStart() {
        super.onStart();
        // checks if user already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    //need to update this to change activities probably
    private void updateUI(FirebaseUser currentUser) {
        return;
    }

    private void login() {
        Toast.makeText(this, "Logging...", Toast.LENGTH_SHORT).show();
    }
    //sign in method
    public void signIn() {
        //Firebase sign in function
        mAuth.signInWithEmailAndPassword(mEmailField.getText().toString(), mPasswordField.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            mPasswordField.setError("Incorrect E-mail or Password");
                            updateUI(null);
                        }
                    }
                });
    }

    //create an account we should maybe make an intent to a new activity to fill out all user info


    //makes sure the inputed text is correctly inputed
    public  boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }
        return valid;
    }
}
