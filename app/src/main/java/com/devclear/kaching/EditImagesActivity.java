package com.devclear.kaching;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditImagesActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    private static  final int CHOOSE_IMAGE = 101;

    private final int[] imageIds = {R.id.user_image_1, R.id.user_image_2, R.id.user_image_3, R.id.user_image_4, R.id.user_image_5};
    private final int[] imageButtonIds = {R.id.user_image_button_1, R.id.user_image_button_2, R.id.user_image_button_3, R.id.user_image_button_4, R.id.user_image_button_5};

    private ImageView[] images;
    private Button[] imageButtons;

    private ProgressBar progressBar;

    private int selectedImage;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_images);

        progressBar = findViewById(R.id.edit_image_progress_bar);

        images = new ImageView[imageIds.length];
        imageButtons = new Button[imageButtonIds.length];

        for(int i = 0; i < imageIds.length; i++){
            images[i] = findViewById(imageIds[i]);
            imageButtons[i] = findViewById(imageButtonIds[i]);
        }
        for(int i = 0; i < imageButtonIds.length; i++){
            imageButtons[i].setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int id = -1;
                    for(int i = 0; i < imageButtonIds.length; i++){
                        if(v.getId() == imageButtonIds[i])
                            id = i;
                    }
                    showImageChooser(id);
                }
            });
        }
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        loadUserInfo();
    }

    private void showImageChooser(int id) {
        selectedImage = id;
        Intent intent = new Intent();
        intent.putExtra("ID", id);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), CHOOSE_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri profileImageURI = null;
        if(requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data.getData() != null) {
            profileImageURI = data.getData();
            Log.d("YES", Integer.toString(selectedImage));
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), profileImageURI);
                images[selectedImage].setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        uploadImageToFirebaseStorage(profileImageURI, selectedImage);
    }
    private void uploadImageToFirebaseStorage(Uri imageURI, final int id) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        userID = user.getUid();
        final StorageReference portfolioImage =
                FirebaseStorage.getInstance().getReference(id + "/" + userID + ".jpg");

        if(imageURI != null) {
            progressBar.setVisibility(View.VISIBLE);
            Log.i("URL", "HERE");
            portfolioImage.putFile(imageURI)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressBar.setVisibility(View.GONE);

                            portfolioImage.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    String imageDownloadUri=task.getResult().toString();
                                    //Update FireStore
                                    DocumentReference docRef = db.collection("users").document(userID);
                                    Map<String,Object> user = new HashMap<>();
                                    user.put("userPortfolioImage"+id,imageDownloadUri);

                                    docRef.update(user);
                                    Log.i("URL",imageDownloadUri);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        }
    }
    void loadUserInfo() {
        userID = firebaseAuth.getCurrentUser().getUid();
        DocumentReference docRef = db.collection("users").document(userID);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                        String key = "userPortfolioImage";
                        for(int i = 0; i < images.length; i++){
                            String imageUrl = document.getString(key+i);
                            if(imageUrl != null)
                                Glide.with(EditImagesActivity.this).load(imageUrl).into(images[i]);
                        }
                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });
    }
}
