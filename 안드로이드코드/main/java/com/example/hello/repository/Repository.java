package com.example.hello.repository;

import androidx.lifecycle.LiveData;

import java.util.List;

public interface Repository<T> {
  List<T> getData();
  
}
