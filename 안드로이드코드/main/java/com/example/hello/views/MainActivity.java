package com.example.hello.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hello.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
  
  TextView textView;
  private Button mBtn_user;
  private Button mbuttonLoadHistory;
  private Button mbutton5;
  private Button mbutton2;
//private Button mbutton3;
  
  private Socket clientSocket;
  private BufferedReader socketIn;
  private PrintWriter socketOut;
  private int port = 8888;
  private final String ip ="192.168.137.210";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_main);
  
    try {
      clientSocket = new Socket(ip, port);
      socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      socketOut = new PrintWriter(clientSocket.getOutputStream(), true);
    } catch (Exception e) {
      e.printStackTrace();
    }

  
  
    FirebaseMessaging.getInstance().getToken()
        .addOnCompleteListener(new OnCompleteListener<String>() {
          @Override
          public void onComplete(@NonNull Task<String> task) {
            if (!task.isSuccessful()) {
              Log.w("FCM Log", "getInstanceId faild", task.getException());
              return;
            }
            // Get new FCM registration token
            String token = task.getResult();
            Log.d("FCM Log", "FCM 토근 : " + token);
            //Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
          }
        });
    
    mBtn_user = findViewById(R.id.button6);
    mbuttonLoadHistory = findViewById(R.id.button3);
    mbutton5 = findViewById(R.id.button5);
    mbutton2 = findViewById(R.id.button2);
    textView = findViewById(R.id.textView);
//  mbutton3 =findViewById(R.id.button3);
    
    mbutton2.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        openMessage();
        
      }
    });

    mbuttonLoadHistory.setOnClickListener((v) -> {
      Intent intent = new Intent(getApplication(), entrantrecordActivity.class);
      startActivity(intent);
    });
    
    mBtn_user.setOnClickListener((v) -> {
      Intent intent = new Intent(getApplication(), TestActivity.class);
      startActivity(intent);
    });

//        mbutton3.setOnClickListener((v) -> {
//            Intent intent1 = new Intent(MainActivity.this, UserEditActivity.class);
//            startActivity(intent1);
//        });
    
    mbutton5.setOnClickListener((v) -> {
      Intent intent = new Intent(getApplication(), UserListActivity.class);
      startActivity(intent);
    });
  }
  
  
  public void openMessage() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("안내");
    builder.setMessage("잠금해제 하시겠습니까?");
    builder.setIcon(android.R.drawable.ic_dialog_alert);
    
    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        try {
          Executors.newSingleThreadScheduledExecutor().submit(() -> {
            try (final Socket s = new Socket()) {
              try {
                s.connect(new InetSocketAddress("192.168.137.210", 8888), 2500);
              } catch (SocketTimeoutException e) {
                e.printStackTrace();
                return null;
              }
              try (final InputStream is = s.getInputStream()) {
                try (final OutputStream os = s.getOutputStream()) {
                  os.write("open".getBytes(StandardCharsets.UTF_8));
                }
                byte[] rawResponse = new byte[1024];
                int length = is.read(rawResponse);
                String response = new String(rawResponse, 0, length, StandardCharsets.UTF_8);
                assert response.equals("open");
          
                return response;
              }
            }
          }).get();
        } catch (ExecutionException | InterruptedException e) {
          e.printStackTrace();
        }
        String message = "잠금해제 되었습니다.";
        textView.setText(message);
      }
    });
    
    builder.setNeutralButton("취소", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        String message = "취소 버튼이 눌렸습니다.";
        textView.setText(message);
      }
    });
    AlertDialog dialog = builder.create();
    dialog.show();
  }
  
}


