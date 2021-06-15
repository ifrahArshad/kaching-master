package com.devclear.kaching;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private ImageView userImage;
    private TextView userName;
    private TextView followerCountView;
    private Button editImage;
    private Button uploadImage;
    private Button editTags;
    private Button logout;
    private Button followedList;
    private Button viewProfile;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private String userID;
    private ProgressBar progressBar;


    private static  final int CHOOSE_IMAGE = 101;
    private int CAMERA_REQUEST = 1888;
    private int CAMERA_PERMISSION_CODE = 100;
    private android.net.Uri profileImageURI;
    private String profileImageDownloadURL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        logout = view.findViewById(R.id.profile_LogOut);
        userName = view.findViewById(R.id.profile_UserName);
        userImage = view.findViewById(R.id.profile_Image);
        progressBar = view.findViewById(R.id.profile_ProgessBar);
        editImage = view.findViewById(R.id.profile_editImageButton2);
        uploadImage = view.findViewById(R.id.profile_UploadImage);
        editTags = view.findViewById(R.id.profile_editTags);
        viewProfile = view.findViewById(R.id.profile_ViewProfile);
        //followedList = view.findViewById(R.id.profile_viewFollowedUsers);
        followerCountView = view.findViewById(R.id.profile_FollowerCount);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadUserInfo();

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageChooser();
            }
        });

        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageChooser();
            }
        });
        /*
        followedList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FollowedList.class);
                startActivity(intent);
            }
        });
        */
        viewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ViewProfile.class);
                intent.putExtra("userID", userID);
                startActivity(intent);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                Objects.requireNonNull(getActivity()).finish();
            }
        });

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditImagesActivity.class);
                startActivity(intent);
            }
        });
        editTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditTagsActivity.class);
                startActivity(intent);
            }
        });
    }

    public void loadUserInfo() {
        userID = firebaseAuth.getCurrentUser().getUid();
        DocumentReference docRef = db.collection("users").document(userID);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                        userName.setText(document.getString("userName"));
                        followerCountView.setText(document.getLong("followerCount").intValue() + " Follower(s)");
                        profileImageDownloadURL = document.getString("userImageURL");

                        Activity activity = getActivity();
                        if(activity != null)
                            if(profileImageDownloadURL != null)
                                Glide.with(activity).load(profileImageDownloadURL).into(userImage);
                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data.getData() != null) {
            profileImageURI = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), profileImageURI);
                userImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            uploadImageToFirebaseStorage();
        }
    }

    private void uploadImageToFirebaseStorage() {
        final StorageReference profileImageReference =
                FirebaseStorage.getInstance().getReference("profilepics/" + userID + ".jpg");

        if(profileImageURI != null) {
            progressBar.setVisibility(View.VISIBLE);
            if (profileImageURI != null) {

                profileImageReference.putFile(profileImageURI)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressBar.setVisibility(View.GONE);

                                profileImageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        profileImageDownloadURL=task.getResult().toString();
                                        //Update FireStore
                                        DocumentReference docRef = db.collection("users").document(userID);
                                        Map<String,Object> user = new HashMap<>();
                                        user.put("userImageURL",profileImageDownloadURL);

                                        docRef.update(user);
                                        Log.i("URL",profileImageDownloadURL);
                                        Toast.makeText(getActivity(), "Image Successfully updated", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getActivity(), "aaa "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    private void showImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), CHOOSE_IMAGE);
    }
}
