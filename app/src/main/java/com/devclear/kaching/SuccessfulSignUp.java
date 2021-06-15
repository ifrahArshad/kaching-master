package com.devclear.kaching;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;

public class SuccessfulSignUp extends AppCompatActivity {

    Button successButton;
    Button successButtonOverlay;
    Button successButtonOverlayOverlay;
    
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_successful_sign_up);
        successButton = findViewById(R.id.success_button);
        successButtonOverlay = findViewById(R.id.success_button_overlay);
        successButtonOverlayOverlay = findViewById(R.id.success_button_overlay_overlay);

        successButton.setOnTouchListener(new View.OnTouchListener() {
            int x;
            int y;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                x = (int) event.getX();
                y = (int) event.getY();
                int finalRadius = Math.max(successButtonOverlay.getWidth(), successButtonOverlay.getHeight());
                successButtonOverlay.setSelected(true);
                Animator anim = ViewAnimationUtils.createCircularReveal(successButtonOverlay,
                        x, y,0, finalRadius);
                anim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        int finalRadius = Math.max(successButtonOverlayOverlay.getWidth(), successButtonOverlayOverlay.getHeight());
                        Animator anim = ViewAnimationUtils.createCircularReveal(successButtonOverlayOverlay,
                                x, y,0, finalRadius);
                        anim.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                successButtonOverlay.setVisibility(View.INVISIBLE);
                                successButtonOverlayOverlay.setVisibility(View.INVISIBLE);
                                //signUpUser();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                        successButtonOverlayOverlay.setVisibility(View.VISIBLE);
                        anim.start();
                        goToMainActivity();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                successButtonOverlay.setVisibility(View.VISIBLE);
                anim.start();
                return false;
            }
        });
    }
    public void goToMainActivity(){
        Intent intent = new Intent(SuccessfulSignUp.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
