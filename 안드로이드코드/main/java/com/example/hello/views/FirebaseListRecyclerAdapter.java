package com.example.hello.views;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.example.hello.R;
import com.example.hello.model.firebase.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import lombok.Getter;
import lombok.Setter;

public class FirebaseListRecyclerAdapter extends RecyclerView.Adapter<FirebaseListRecyclerAdapter.ViewHolder> {
  
  private Context context;
  
  public FirebaseListRecyclerAdapter(Context context) {
    super();
    
    this.context = context;
  }
  
  @Getter
  @Setter
  private List<User> userList = new ArrayList<>();
  
  @FunctionalInterface
  interface ValueCallback<T> extends Consumer<T> {}
  
  @Getter
  @Setter
  private ValueCallback<User> onItemClicked;
  
  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.user_list_item_fragment, parent, false);
  
    ViewHolder viewHolder = new ViewHolder(view);
    
    view.setOnClickListener(v -> {
      int index = viewHolder.getIndexOfThisItem();
      if (onItemClicked != null) {
        try {
          onItemClicked.accept(userList.get(index));
        } catch (ArrayIndexOutOfBoundsException ex) {
          // Don't do anything
        }
      }
    });
    
    return viewHolder;
  }
  
  private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
  private StorageReference storageReference = firebaseStorage.getReference();
  
  private DatabaseReference imageMetaRef = FirebaseDatabase.getInstance().getReference().child("images");
  
  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    final String TAG = "onBindViewHolder";
    holder.setIndexOfThisItem(position);
    User user = userList.get(position);
    holder.name.setText(user.getNickname());
    holder.mail.setText(user.getEmail());
    final String uid = user.getUid();
    Log.d(TAG, "position " + position);
    Log.d(TAG, "uid " + uid);
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
        Log.d(TAG, "got path");
        if (path != null && !path.isEmpty()) {
          Log.d(TAG, "path " + path);
          StorageReference imagesRef = storageReference.child(path);
          if (updated != null) {
          Glide.with(context)
              .load(imagesRef)
              .signature(new ObjectKey(updated))
              .into(holder.photo);
          } else {
            Glide.with(context)
                .load(imagesRef)
                .into(holder.photo);
          }
        }
      });
    });
    new Thread((Runnable) () -> {
      new Thread(updateUserImage).start();
      userProfileRef.child("updated").addValueEventListener(
          new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
              Log.d(TAG, "image updated");
              new Thread(updateUserImage).start();
            }
  
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
    
            }
          }
      );
    }).start();
    
  }
  
  @Override
  public int getItemCount() {
    return userList.size();
  }
  
  class ViewHolder extends RecyclerView.ViewHolder {
  
    TextView name = itemView.findViewById(R.id.itemTitle);
    TextView mail = itemView.findViewById(R.id.itemSubtitle);
    ImageView photo = itemView.findViewById(R.id.itemImage);
    
    @Getter
    @Setter
    private int indexOfThisItem = -1;
  
    public ViewHolder(@NonNull View itemView) {
      super(itemView);
    }
  }
}
