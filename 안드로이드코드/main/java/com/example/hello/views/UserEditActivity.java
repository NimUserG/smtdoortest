package com.example.hello.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.example.hello.R;
import com.example.hello.model.firebase.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UserEditActivity extends AppCompatActivity {
  
  private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
  private DatabaseReference databaseReference = firebaseDatabase.getReference();
  private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
  private StorageReference storageReference = firebaseStorage.getReference();
  
  private Button button;
  private View button1;
  
  private TextView textView_userName;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_edit);
  
    ObjectMapper om = new ObjectMapper();
    String value = getIntent().getStringExtra("editTarget");
    User editTarget = null;
    if (value != null) {
      try {
        editTarget = om.readValue(value, User.class);
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }
    }
    
    button = findViewById(R.id.btn_cancel);
    button1 = findViewById(R.id.btn_load);
    textView_userName = findViewById(R.id.userEditUserName);
    
    button.setOnClickListener((view) -> {
      finish();
      startActivity(new Intent(UserEditActivity.this, MainActivity.class));
    });
    
    button1.setOnClickListener((v) -> {
      startActivity(new Intent(UserEditActivity.this, TestActivity.class));
    });
    
    
    if (editTarget == null) {
      // Edit target was not given, so it must be that we are trying to edit the currently logged in user
      // Try to convert firebase user to our own user model
      FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
      editTarget = User.from(user);
    }
    
    if (editTarget != null) {
      // If an edit target was found, do the work
      String userEmail = editTarget.getEmail();
  
      DatabaseReference users = databaseReference.child("Users");
      Query usersFilteredByEmail =
          users
              .orderByChild("email")
              .equalTo(userEmail)
              .limitToFirst(1);
      usersFilteredByEmail.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
          for (DataSnapshot user : snapshot.getChildren()) {
            DataSnapshot userData = user.child("uid");
            String uid = userData.getValue(String.class);
            DataSnapshot userData2 = user.child("nickname");
            String nickname = userData2.getValue(String.class);
            updateUserProfilePictureByNickname(uid);
            textView_userName.setText(nickname);
          }
        }
    
        @Override
        public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
          // noop
        }
      });
    }
  }
  
  private DatabaseReference imageMetaRef = FirebaseDatabase.getInstance().getReference().child("images");
  
  private void updateUserProfilePictureByNickname(String uid) {
    Context context = UserEditActivity.this;
    DatabaseReference userProfileRef = imageMetaRef.child(uid).child("profile");
    Runnable updateUserImage = ((Runnable) () -> {
      Tasks.whenAllComplete(userProfileRef.child("path").get(), userProfileRef.child("updated").get())
          .addOnSuccessListener(result -> {
            Task getPathTask = result.get(0);
            Task getUpdatedTask = result.get(1);
            final String path = ((DataSnapshot) getPathTask.getResult()).getValue(String.class);
            Long updated;
            try {
              updated =((DataSnapshot) getUpdatedTask.getResult()).getValue(Long.class);
            } catch (NumberFormatException nfe) {
              updated = null;
            }
            if (path != null && !path.isEmpty()) {
              StorageReference imagesRef = storageReference.child(path);
              ImageView target = (ImageView) findViewById(R.id.profileImg);
              if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))
              if (updated != null) {
                Glide.with(context)
                    .load(imagesRef)
                    .signature(new ObjectKey(updated))
                    .into(target);
              } else {
                Glide.with(context)
                    .load(imagesRef)
                    .into(target);
              }
            }
          });
    });
    ((Runnable) () -> {
      userProfileRef.child("updated").addValueEventListener(
          new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
              updateUserImage.run();
            }
          
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            
            }
          }
      );
    }).run();
    updateUserImage.run();
  }
}

