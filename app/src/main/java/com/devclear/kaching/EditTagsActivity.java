package com.devclear.kaching;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditTagsActivity extends AppCompatActivity {

    private int[] tagIds = {R.id.edit_tags_field_1, R.id.edit_tags_field_2, R.id.edit_tags_field_3, R.id.edit_tags_field_4, R.id.edit_tags_field_5};
    private EditText[] tags;

    private Button submitButton;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tags);

        tags = new EditText[tagIds.length];
        for (int i = 0; i < tagIds.length; i++){
            tags[i] = findViewById(tagIds[i]);
        }

        progressBar = findViewById(R.id.edit_tags_progress_bar);

        submitButton = findViewById(R.id.edit_tags_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveChanges();
            }
        });
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loadUserInfo();
    }

    private void SaveChanges() {
        progressBar.setVisibility(View.VISIBLE);
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference docRef = db.collection("users").document(userID);
        Map<String,Object> user = new HashMap<>();
        for(int i = 0; i < tags.length; i++) {
            String tag = tags[i].getText().toString();
            if(!tag.equals("")) {
                user.put("userTag" + i, tag);
            }
        }
        docRef.update(user);
        progressBar.setVisibility(View.GONE);
        Toast.makeText(EditTagsActivity.this, "Tags Saved",  Toast.LENGTH_LONG).show();
    }

    void loadUserInfo() {

        progressBar.setVisibility(View.VISIBLE);
        String userID = firebaseAuth.getCurrentUser().getUid();
        DocumentReference docRef = db.collection("users").document(userID);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                        String key = "userTag";
                        for(int i = 0; i < tags.length; i++){
                            String tag = document.getString(key+i);
                            tags[i].setText(tag);
                        }
                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });
        progressBar.setVisibility(View.GONE);
    }
}
