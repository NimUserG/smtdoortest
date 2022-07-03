package com.example.hello.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hello.R;
import com.example.hello.model.firebase.User;
import com.example.hello.model.viewmodel.ListViewModel;
import com.example.hello.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.function.BiConsumer;

public class UserListActivity extends AppCompatActivity {
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    init(this);
    
    loadUserList(this);
  }
  
  private FirebaseListRecyclerAdapter listAdapter;
  private ListViewModel<User> viewModel;
  
  private void init(ComponentActivity context) {
    setContentView(R.layout.user_list);
    if (listAdapter == null) {
      listAdapter = new FirebaseListRecyclerAdapter(context);
      listAdapter.setOnItemClicked((user) -> {
        Intent intent = new Intent(UserListActivity.this, UserEditActivity.class);
        ObjectMapper om = new ObjectMapper();
        try {
          intent.putExtra("editTarget", om.writeValueAsString(user));
          startActivity(intent);
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }
      });
    }
    if (viewModel == null) {
      viewModel = new ViewModelProvider(context).get(ListViewModel.class);
      viewModel.setRepository(new UserRepository());
    }
  }
  
  private void loadUserList(ComponentActivity context) {
    RecyclerView list = findViewById(R.id.userList);
    
    list.setLayoutManager(new LinearLayoutManager(context));
    list.setAdapter(listAdapter);
    observeData(context);
  }
  
  private void observeData(ComponentActivity context) {
    viewModel.fetchData().observe(context, (users) -> {
      listAdapter.setUserList(users);
      listAdapter.notifyDataSetChanged();
    });
  }
  
}
