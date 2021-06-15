package com.devclear.kaching;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    EditText mName;
    EditText mEmail;
    EditText mPassword;
    EditText mConfirmedPassword;
    TextView mLoginButton;
    ProgressBar mProgressBar;
    Button signupButton;
    Button signupButtonOverlay;
    Button signupButtonOverlayOverlay;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore fireStore;
    String userID;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mName = findViewById(R.id.signup_name);
        mEmail = findViewById(R.id.signup_email);
        mPassword = findViewById(R.id.signup_password);
        mConfirmedPassword = findViewById(R.id.signup_password_confirm);
        mLoginButton = findViewById(R.id.signup_log_in);
        mProgressBar = findViewById(R.id.signup_progress_bar);

        signupButton = findViewById(R.id.signup_button);
        signupButtonOverlay = findViewById(R.id.signup_button_overlay);
        signupButtonOverlayOverlay = findViewById(R.id.signup_button_overlay_overlay);

        firebaseAuth = FirebaseAuth.getInstance();
        fireStore = FirebaseFirestore.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(intent);
        }

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        signupButton.setOnTouchListener(new View.OnTouchListener() {
            int x;
            int y;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                x = (int) event.getX();
                y = (int) event.getY();
                int finalRadius = Math.max(signupButtonOverlay.getWidth(), signupButtonOverlay.getHeight());
                signupButtonOverlay.setSelected(true);
                Animator anim = ViewAnimationUtils.createCircularReveal(signupButtonOverlay,
                        x, y,0, finalRadius);
                anim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        int finalRadius = Math.max(signupButtonOverlayOverlay.getWidth(), signupButtonOverlayOverlay.getHeight());
                        Animator anim = ViewAnimationUtils.createCircularReveal(signupButtonOverlayOverlay,
                                x, y,0, finalRadius);
                        anim.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                signupButtonOverlay.setVisibility(View.INVISIBLE);
                                signupButtonOverlayOverlay.setVisibility(View.INVISIBLE);
                                signUpUser();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                        signupButtonOverlayOverlay.setVisibility(View.VISIBLE);
                        anim.start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                signupButtonOverlay.setVisibility(View.VISIBLE);
                anim.start();
                signUpUser();
                return false;
            }
        });
    }

    protected void signUpUser(){
        final String email = mEmail.getText().toString();
        final String userName = mName.getText().toString();
        String password = mPassword.getText().toString();
        String confirmedPassword = mConfirmedPassword.getText().toString();

        if(TextUtils.isEmpty(userName)) {
            mName.setError("Username is Required");
            return;
        }

        if(TextUtils.isEmpty(email)) {
            mEmail.setError("Email is Required");
            return;
        }

        if(TextUtils.isEmpty(password)) {
            mPassword.setError("Password is Required");
            return;
        }
        if(password.length() < 6) {
            mPassword.setError("Weak Password");
            return;
        }
        if(!confirmedPassword.equals(password)) {
            mConfirmedPassword.setError("Password not Matched");
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);

        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //Add user info to DB
                            userID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

                            addUsertoDB(userID, userName, email);
                            Toast.makeText(SignUpActivity.this, "Sign Up Successful",  Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(SignUpActivity.this, SuccessfulSignUp.class);
                            startActivity(intent);

                        } else {
                            Toast.makeText(SignUpActivity.this, "Unable to create user",  Toast.LENGTH_LONG).show();
                            mProgressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    void addUsertoDB(String userID,String userName, String email) {
        //TEMPLATE START----------------------------------------------------
        Map<String,Object> user = new HashMap<>();
        user.put("userID", userID);
        user.put("userName",userName);
        user.put("email",email);
        user.put("followerCount", 0);

        fireStore.collection("users").document(userID).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.w("TAG", "Added Document to DB with ID");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Error adding document", e);
                    }
                });
        //TEMPLATE END------------------------------------------------------
    }
}
