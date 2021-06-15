package com.devclear.kaching;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class FollowedList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private SearchAdapter adapter;
    private FirebaseFirestore db;
    private ArrayList<SearchItem> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followed_list);

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.followedList_recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        loadFollowedUsers();
    }

    void loadFollowedUsers() {
        String auth_userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference docRef = db.collection("users").document(auth_userID);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    ArrayList<String> followedUsers = (ArrayList<String>)document.get("followedList");

                    if(followedUsers != null) {
                        for(int i = 0; i < followedUsers.size(); i++) {
                            DocumentReference docRef_v2 = db.collection("users").document(followedUsers.get(i));
                            docRef_v2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    DocumentSnapshot document = task.getResult();
                                    if (task.isSuccessful()) {
                                            String profileImageDownloadURL = document.getString("userImageURL");
                                            String userName = document.getString("userName");
                                            String userID = document.getString("userID");
                                            String tagString = "";

                                            for(int i = 0; i < 4; i++) {
                                                String currTag = document.getString("userTag" + i);
                                                if(currTag != null)
                                                    tagString += "#" + currTag + " ";
                                            }

                                            if(userName != null && userID != null)
                                                userList.add(new SearchItem(profileImageDownloadURL, userName, tagString, userID));
                                            setAdapter();
                                    }
                                    else {
                                        Log.d("TAG", "Error getting documents: ", task.getException());
                                    }
                                }
                            });

                        }
                    }
                }
            }
        });
    }

    void setAdapter() {
        adapter = new SearchAdapter(userList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickedListener(new SearchAdapter.OnItemClickedListener() {
            @Override
            public void onItemClick(int position) {
                openUserProfile(position);
            }
        });
    }

    void openUserProfile(int position) {
        String userID = userList.get(position).getuserID();
        Intent intent = new Intent(FollowedList.this, ViewProfile.class);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }
}
