package com.example.hello.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.hello.model.firebase.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UserRepository implements LiveRepository<User> {
  
  final MutableLiveData<List<User>> mutableData = new MutableLiveData<>();
  
  public final LiveData<List<User>> getLiveData() {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Users");;
    myRef.addValueEventListener((new ValueEventListener() {
      @NotNull
      private final List<User> listData = new ArrayList<>();
      
      @NotNull
      public final List<User> getListData() {
        return this.listData;
      }
      
      public void onDataChange(@NotNull DataSnapshot snapshot) {
        if (snapshot.exists()) {
          for (DataSnapshot userSnapshot : snapshot.getChildren()) {
            User getData = userSnapshot.getValue(User.class);
            this.listData.add(getData);
            mutableData.setValue(this.listData);
          }
        }
        
      }
      
      public void onCancelled(@NotNull DatabaseError error) {
        throw new UnsupportedOperationException("An operation is not implemented: ");
      }
    }));
    return mutableData;
  }
  
  @Override
  public List<User> getData() {
    return mutableData.getValue();
  }
}
