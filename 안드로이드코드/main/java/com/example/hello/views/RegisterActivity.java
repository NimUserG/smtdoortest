package com.example.hello.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hello.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    EditText mEt_name, mEt_email, mEt_pwd;
    Button mBtn_register;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // firebase access
        firebaseAuth = FirebaseAuth.getInstance();

        mEt_name = findViewById(R.id.et_nameR);
        mEt_email = findViewById(R.id.et_emailR);
        mEt_pwd = findViewById(R.id.et_pwdR);
        mBtn_register = findViewById(R.id.btn_registerR);

        mBtn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEt_email.getText().toString().trim();
                String pwd = mEt_pwd.getText().toString().trim();

                firebaseAuth.createUserWithEmailAndPassword(email, pwd)
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    FirebaseUser user = firebaseAuth.getCurrentUser();

                                    String nickname = mEt_name.getText().toString().trim();
                                    String email = user.getEmail();
                                    String uid = user.getUid();

                                    //해쉬맵 테이블을 파이어베이스 데이터베이스에 저장
                                    // 해쉬맵을 이용하여 테이블 형식으로 한 번에 집어 넣으면 편리(테이블 형식)
                                    HashMap<Object,String> hashMap = new HashMap<>();

                                    hashMap.put("uid",uid);
                                    hashMap.put("email",email);
                                    hashMap.put("nickname",nickname);

                                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                                    DatabaseReference ref = db.getReference("Users");
                                    ref.child(uid).setValue(hashMap);

                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    Toast.makeText(RegisterActivity.this, "회원 가입 완료", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(RegisterActivity.this, "회원 가입 실패",
                                            Toast.LENGTH_SHORT).show();
                                    return;


                                }
                            }
                        });


            }
        });

    }
    public boolean onSupportNavigateUp () {
        onBackPressed();
        ; // 뒤로가기 버튼이 눌렸을시
        return super.onSupportNavigateUp(); // 뒤로가기 버튼
    }
}
