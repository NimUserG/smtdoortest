package com.example.hello.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

//import com.example.hello.R;
import com.example.hello.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    Button mBtn_register, mBtn_login;
    EditText mEt_email, mEt_pwd;
    private FirebaseAuth firebaseAuth; // FirebaseAuth 인스턴스 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance(); // onCreate() 메서드에서 FirebaseAuth 인스턴스를 초기화

        mBtn_register = findViewById(R.id.btn_register);
        mBtn_login = findViewById(R.id.btn_login);
        mEt_email = findViewById(R.id.et_email);
        mEt_pwd = findViewById(R.id.et_pwd);

        mBtn_register.setOnClickListener(new View.OnClickListener() {
            // 회원가입 버튼을 눌렀을 때 RegisterActivity로 화면 전환
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        mBtn_login.setOnClickListener(new View.OnClickListener() {
            // 로그인 버튼을 눌렀을 때 MainActivity로 화면 전환
            @Override
            public void onClick(View v) {
                // EditText 텍스트 읽어오기
                String email = mEt_email.getText().toString().trim();      // trim() -> 공백 문자 제거
                String pwd = mEt_pwd.getText().toString().trim();
                // 이메일 주소와 비밀번호를 가져와 유효성을 검사한 후 사용자를 로그인
                firebaseAuth.signInWithEmailAndPassword(email, pwd)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // 로그인 성공하면
                                if (task.isSuccessful()) {
                                    // MainActivity로 이동
                                    Intent intent = new Intent(getApplication(), MainActivity.class);
                                    startActivity(intent);
                                }
                                // 로그인 실패하면
                                else {
                                    // 현재 화면에 토스트 문구 노출
                                    Toast.makeText(LoginActivity.this, "이메일 혹은 비밀번호를 다시 확인해 주세요.", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
            }
        });
    }
}