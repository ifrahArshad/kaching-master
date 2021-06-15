package com.devclear.kaching;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private ArrayList<SearchItem> userList = new ArrayList<>();
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();
        //loadAllUsers();
        loadFollowedUsers();

        recyclerView = view.findViewById(R.id.home_recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar = view.findViewById(R.id.home_progressBar);
    }

    private void loadAllUsers() {
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
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
                            }
                            Collections.shuffle(userList);
                            setAdapter();
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });
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
    private void setAdapter() {
        SearchAdapter adapter = new SearchAdapter(userList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickedListener(new SearchAdapter.OnItemClickedListener() {
            @Override
            public void onItemClick(int position) {
                openUserProfile(position);
            }
        });
        progressBar.setVisibility(View.GONE);
    }
    private void openUserProfile(int position) {
        String userID = userList.get(position).getuserID();
        Intent intent = new Intent(getActivity(), ViewProfile.class);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }
}
