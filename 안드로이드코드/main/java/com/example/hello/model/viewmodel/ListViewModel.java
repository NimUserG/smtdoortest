package com.example.hello.model.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.hello.repository.LiveRepository;
import com.example.hello.repository.Repository;

import java.util.List;

import lombok.Setter;

public class ListViewModel<T> extends ViewModel {
  @Setter
  private Repository<T> repository;
  
  public ListViewModel() {}
  
  public ListViewModel(Repository<T> repository) {
    this.repository = repository;
  }
  
  public LiveData<List<T>> fetchData() {
    MutableLiveData<List<T>> mutableData = new MutableLiveData<>();
    if (repository instanceof LiveRepository) {
      ((LiveRepository<T>) repository).getLiveData().observeForever((data) -> {
        mutableData.setValue(data);
      });
    }
    return mutableData;
  }
}
