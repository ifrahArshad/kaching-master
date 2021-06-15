package com.devclear.kaching;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewProfile extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private boolean isFollower;
    private String profile_userID;
    private TextView tags;
    private TextView userName;
    private TextView followersCount;
    private ImageView profileImage;
    private ImageView[] portfolioImages;
    private Button followButton;
    private int[] portfolioImageIDs = {R.id.viewProfile_portfolioImage1, R.id.viewProfile_portfolioImage2, R.id.viewProfile_portfolioImage3, R.id.viewProfile_portfolioImage4, R.id.viewProfile_portfolioImage5};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userName = findViewById(R.id.viewProfile_username);
        followersCount = findViewById(R.id.viewProfile_followers);
        tags = findViewById(R.id.viewProfile_tags);
        followButton = findViewById(R.id.viewProfile_followButton);
        profile_userID = getIntent().getStringExtra("userID");
        profileImage =  findViewById(R.id.viewProfile_userImage);
        portfolioImages = new ImageView[portfolioImageIDs.length];

        if(firebaseAuth.getCurrentUser().getUid().equals(profile_userID))
            followButton.setVisibility(View.GONE);

        isFollower = false;
        checkFollower();
        setFollowButton();

        for(int i = 0; i < 5; i++)
            portfolioImages[i] = findViewById(portfolioImageIDs[i]);

        if(profile_userID != null)
            loadUserInfo(profile_userID);


    }

    void loadUserInfo(String userID) {
        DocumentReference docRef = db.collection("users").document(userID);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        int followerCount = document.getLong("followerCount").intValue();
                        followersCount.setText(Integer.toString(followerCount) + " Followers");
                        userName.setText(document.getString("userName"));

                        String profileImageDownloadURL = document.getString("userImageURL");

                        if(profileImageDownloadURL != null)
                            Glide.with(ViewProfile.this).load(profileImageDownloadURL).into(profileImage);

                        for(int i = 0; i < 5; i++) {
                            String portfolioDownloadURL = document.getString("userPortfolioImage" + i);
                            if(portfolioDownloadURL != null) {
                                portfolioImages[i].setVisibility(View.VISIBLE);
                                Glide.with(ViewProfile.this).load(portfolioDownloadURL).into(portfolioImages[i]);
                            }
                        }

                        String tagString = "";
                        for(int i = 0; i < 5; i++) {
                            String currTag = document.getString("userTag" + i);
                            if(currTag != null)
                                tagString += "#" + currTag + " ";

                        }
                        tags.setText(tagString);
                        Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });
    }

    void addFollower() {
        String auth_userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference docRef = db.collection("users").document(auth_userID);
        docRef.update("followedList", FieldValue.arrayUnion(profile_userID));

        DocumentReference docRef_v2 = db.collection("users").document(profile_userID);
        docRef_v2.update("followerCount", FieldValue.increment(1));

        docRef_v2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    int followerCount = document.getLong("followerCount").intValue();
                    followersCount.setText(Integer.toString(followerCount) + " Followers");
                }
            }
        });
    }

    void removeFollower() {
        String auth_userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference docRef = db.collection("users").document(auth_userID);
        docRef.update("followedList", FieldValue.arrayRemove(profile_userID));

        DocumentReference docRef_v2 = db.collection("users").document(profile_userID);
        docRef_v2.update("followerCount", FieldValue.increment(-1));

        docRef_v2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    int followerCount = document.getLong("followerCount").intValue();
                    followersCount.setText(Integer.toString(followerCount) + " Followers");
                }
            }
        });
    }

    public void checkFollower() {
        String auth_userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference docRef = db.collection("users").document(auth_userID);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    ArrayList<String> followedUsers = (ArrayList<String>)document.get("followedList");

                    if(followedUsers !=  null) {
                        if(followedUsers.contains(profile_userID)) {
                            isFollower = true;
                            followButton.setText("Unfollow");
                            Log.d("SMT", "AAAAAAAAAAAAAAAAAAAAAAAA");
                        }
                    }
                }
            }
        });
    }

    public void setFollowButton() {
        if(isFollower == true) {
            followButton.setText("Unfollow");
        }
        else {
            followButton.setText("Follow");
        }
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonText = followButton.getText().toString();
                if(followButton.getText().toString().equals("Follow")) {
                    followButton.setText("Unfollow");
                    addFollower();
                }
                else {
                    followButton.setText("Follow");
                    removeFollower();
                }
            }
        });
    }
}
