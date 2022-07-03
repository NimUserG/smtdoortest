package com.example.hello.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.hello.model.firebase.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EventRepository implements LiveRepository<StorageReference> {
  
  
  final MutableLiveData<List<StorageReference>> mutableData = new MutableLiveData<>();
  
  public final LiveData<List<StorageReference>> getLiveData() {
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference("image_store");
    storageReference.listAll().addOnSuccessListener(listResult -> {
      mutableData.setValue(listResult.getItems());
    });
    return mutableData;
  }
  
  @Override
  public List<StorageReference> getData() {
    return mutableData.getValue();
  }
  
}
