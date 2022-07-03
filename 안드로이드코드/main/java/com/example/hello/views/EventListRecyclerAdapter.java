package com.example.hello.views;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import lombok.Getter;
import lombok.Setter;

public class EventListRecyclerAdapter extends RecyclerView.Adapter<EventListRecyclerAdapter.ViewHolder> {
  
  private Context context;
  
  public EventListRecyclerAdapter(Context context) {
    super();
    
    this.context = context;
  }
  
  @Getter
  @Setter
  private List<StorageReference> files = new ArrayList<>();
  
  @FunctionalInterface
  interface ValueCallback<T> extends Consumer<T> {}
  
  @Getter
  @Setter
  private ValueCallback<StorageReference> onItemClicked;
  
  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.user_list_item_fragment, parent, false);
    
    ViewHolder viewHolder = new ViewHolder(view);
    
    view.setOnClickListener(v -> {
      int index = viewHolder.getIndexOfThisItem();
      if (onItemClicked != null) {
        try {
          onItemClicked.accept(files.get(index));
        } catch (ArrayIndexOutOfBoundsException ex) {
          // Don't do anything
        }
      }
    });
    
    return viewHolder;
  }
  
  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    final String TAG = "onBindViewHolder";
    holder.setIndexOfThisItem(position);
    //holder.name.setText("불법 침입 감지됨");
    StorageReference file = files.get(position);
    file.getMetadata().addOnSuccessListener(storageMetadata -> {
      // TODO set entrance date
      long updatedTimeMillis = storageMetadata.getUpdatedTimeMillis();
      TextView textView = holder.mail;
      if (textView != null) {
        Date updatedDate = Date.from(Instant.ofEpochMilli(updatedTimeMillis));
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd aa hh:mm:ss");
        textView.setText(dateFormat.format(updatedDate));
      }
    });
    Glide.with(context)
        .load(file)
        .into(holder.photo);
    
  }
  
  @Override
  public int getItemCount() {
    return files.size();
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

