package com.example.hello.repository;

import androidx.lifecycle.LiveData;

import com.example.hello.model.firebase.User;

import java.util.List;

public interface LiveRepository<T> extends Repository<T> {
  
  LiveData<List<T>> getLiveData();
  
}
