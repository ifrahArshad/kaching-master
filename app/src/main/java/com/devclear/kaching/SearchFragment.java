package com.devclear.kaching;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private SearchAdapter adapter;
    private FirebaseFirestore db;
    private ArrayList<SearchItem> userList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        setHasOptionsMenu(true);

        db = FirebaseFirestore.getInstance();
        loadAllUsers();

        recyclerView = view.findViewById(R.id.search_recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SearchView searchView = view.findViewById(R.id.menu_actionSearch);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Toast.makeText(getActivity(), "YES", Toast.LENGTH_SHORT).show();
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    void loadAllUsers() {
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
                                //Log.d("TAG", document.getId() + " => " + document.getData());
                            }
                            //Collections.shuffle(userList);
                            setAdapter();
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
/*
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu, menu);


    }
*/
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
        Intent intent = new Intent(getActivity(), ViewProfile.class);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }
}
