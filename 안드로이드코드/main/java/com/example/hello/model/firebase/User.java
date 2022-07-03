package com.example.hello.model.firebase;

import android.media.Image;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.hello.interfaces.FirebaseModel;
import com.example.hello.model.generic.Audio;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@SuperBuilder
@IgnoreExtraProperties
public class User implements FirebaseModel<User> {

    private @NonNull String uid;
    private String nickname;
    private String email;
    private Image portrait;
    private Audio voice;
  
  public static User from(FirebaseUser user) {
    return User.builder()
        .uid(user.getUid())
        .nickname(user.getDisplayName())
        .email(user.getEmail())
        .build();
  }
}
