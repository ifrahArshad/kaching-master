package com.devclear.kaching;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Transition;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText mEmail;
    EditText mPassword;
    TextView mSignup;
    TextView mForgotPassword;
    ProgressBar mProgessBar;
    Button loginButton;
    Button loginButtonOverlay;
    Button loginButtonOverlayOverlay;
    FirebaseAuth firebaseAuth;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail = findViewById(R.id.login_email);
        mPassword = findViewById(R.id.login_password);
        mSignup = findViewById(R.id.login_sign_up);
        mForgotPassword = findViewById(R.id.login_forgot_password);
        mProgessBar = findViewById(R.id.login_progress_bar);
        firebaseAuth = FirebaseAuth.getInstance();

        loginButton = findViewById(R.id.login_button);
        loginButtonOverlay = findViewById(R.id.login_button_overlay);
        loginButtonOverlayOverlay = findViewById(R.id.login_button_overlay_overlay);

        if (firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        mSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        loginButton.setOnTouchListener(new View.OnTouchListener() {
            int x;
            int y;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                x = (int) event.getX();
                y = (int) event.getY();
                int finalRadius = Math.max(loginButtonOverlay.getWidth(), loginButtonOverlay.getHeight());
                loginButtonOverlay.setSelected(true);
                Animator anim = ViewAnimationUtils.createCircularReveal(loginButtonOverlay,
                        x, y,0, finalRadius);
                anim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        int finalRadius = Math.max(loginButtonOverlayOverlay.getWidth(), loginButtonOverlayOverlay.getHeight());
                        Animator anim = ViewAnimationUtils.createCircularReveal(loginButtonOverlayOverlay,
                                x, y,0, finalRadius);
                        anim.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                loginButtonOverlay.setVisibility(View.INVISIBLE);
                                loginButtonOverlayOverlay.setVisibility(View.INVISIBLE);
                                loginUser();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                        loginButtonOverlayOverlay.setVisibility(View.VISIBLE);
                        anim.start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                loginButtonOverlay.setVisibility(View.VISIBLE);
                anim.start();
                loginUser();
                return false;
            }
        });
    }

    protected void loginUser(){
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email)) {
            mEmail.setError("Email is Required");
            return;
        }

        if(TextUtils.isEmpty(password)) {
            mPassword.setError("Password is Required");
            return;
        }

        mProgessBar.setVisibility(View.VISIBLE);

        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "Logged In Successfully",  Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Wrong username/password",  Toast.LENGTH_LONG).show();
                            mProgessBar.setVisibility(View.GONE);
                        }
                    }
                });
    }
}
