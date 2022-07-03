package com.example.hello.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hello.R;
import com.example.hello.model.firebase.User;
import com.example.hello.model.viewmodel.ListViewModel;
import com.example.hello.repository.EventRepository;
import com.example.hello.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.List;

public class entrantrecordActivity extends AppCompatActivity {
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    init(this);
    
    loadUserList(this);
  }
  
  private EventListRecyclerAdapter listAdapter;
  private ListViewModel<StorageReference> viewModel;
  
  private void init(ComponentActivity context) {
    setContentView(R.layout.user_list);
    if (listAdapter == null) {
      listAdapter = new EventListRecyclerAdapter(context);
    }
    if (viewModel == null) {
      viewModel = new ViewModelProvider(context).get(ListViewModel.class);
      viewModel.setRepository(new EventRepository());
    }
  }
  
  private void loadUserList(ComponentActivity context) {
    RecyclerView list = findViewById(R.id.userList);
    
    list.setLayoutManager(new LinearLayoutManager(context));
    list.setAdapter(listAdapter);
    observeData(context);
  }
  
  private void observeData(ComponentActivity context) {
    viewModel.fetchData().observe(context, (files) -> {
      listAdapter.setFiles(files);
      listAdapter.notifyDataSetChanged();
    });
  }


}
