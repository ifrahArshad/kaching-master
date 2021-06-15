package com.devclear.kaching;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    Button forgotButton;
    Button forgotButtonOverlay;
    Button forgotButtonOverlayOverlay;

    ProgressBar progressBar;
    EditText emailField;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        forgotButton = findViewById(R.id.forgot_button);
        forgotButtonOverlay = findViewById(R.id.forgot_button_overlay);
        forgotButtonOverlayOverlay = findViewById(R.id.forgot_button_overlay_overlay);

        emailField = findViewById(R.id.forgot_email_field);
        progressBar = findViewById(R.id.forgot_progress_bar);

        forgotButton.setOnTouchListener(new View.OnTouchListener() {
            int x;
            int y;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                x = (int) event.getX();
                y = (int) event.getY();
                int finalRadius = Math.max(forgotButtonOverlay.getWidth(), forgotButtonOverlay.getHeight());
                forgotButtonOverlay.setSelected(true);
                Animator anim = ViewAnimationUtils.createCircularReveal(forgotButtonOverlay,
                        x, y,0, finalRadius);
                anim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        int finalRadius = Math.max(forgotButtonOverlayOverlay.getWidth(), forgotButtonOverlayOverlay.getHeight());
                        Animator anim = ViewAnimationUtils.createCircularReveal(forgotButtonOverlayOverlay,
                                x, y,0, finalRadius);
                        anim.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                forgotButtonOverlay.setVisibility(View.INVISIBLE);
                                forgotButtonOverlayOverlay.setVisibility(View.INVISIBLE);
                                //signUpUser();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                        forgotButtonOverlayOverlay.setVisibility(View.VISIBLE);
                        anim.start();
                        resetPassword();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                forgotButtonOverlay.setVisibility(View.VISIBLE);
                anim.start();
                return false;
            }
        });
    }

    private void resetPassword() {

        progressBar.setVisibility(View.VISIBLE);
        String email = emailField.getText().toString();
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.INVISIBLE);
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this, "Password Reset Successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                        else{
                            Toast.makeText(ForgotPasswordActivity.this, "Password Not Reset", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
