package com.ej.smtdoortest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mFirevaseAuth; //파이어베이스 인증처리
    private DatabaseReference mDatabaseRef; //실시간 DB
    private EditText mEtEmail, mEtPwd; //회원가입 입력필드
    private Button mBtnRegister; //회원가입 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFirevaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("smtdoortest");

        mEtEmail = findViewById(R.id.et_user_email);
        mEtPwd = findViewById(R.id.et_user_pwd);
        mBtnRegister = findViewById(R.id.btn_register);

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 회원가입 처리 시작
                String strEmail = mEtEmail.getText().toString();
                String strPwd = mEtPwd.getText().toString();

                try{
                    if(strEmail.length() <= 0){
                        Toast.makeText(RegisterActivity.this, "이메일을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (Patterns.EMAIL_ADDRESS.matcher(strEmail).matches()){
                        Toast.makeText(RegisterActivity.this, "이메일 형식을 확인해 주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(strPwd.length() <= 0){
                        Toast.makeText(RegisterActivity.this, "비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (strPwd.length() < 6) {
                        Toast.makeText(RegisterActivity.this, "비밀번호는 6글자 이상 입력해 주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Firebase Auth 진행
                    mFirevaseAuth.createUserWithEmailAndPassword(strEmail, strPwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // 인증처리 완료시(성공시)
                            if (task.isSuccessful()){
                                FirebaseUser firebaseUser = mFirevaseAuth.getCurrentUser();
                                User account = new User();
                                account.setIdToken(firebaseUser.getUid());
                                account.setEmailId(firebaseUser.getEmail()); //정확도를 위해서 getEmail()을 이용해서 값을 가져온다.
                                account.setPassword(strPwd); //정확도 굳이 필요 없어서 쉬운 방법으로

                                // setValue : database에 insert (삽입) 행위
                                mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).setValue(account);

                                Toast.makeText(RegisterActivity.this, "회원가입에 성공하셨습니다.", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(RegisterActivity.this, "회원가입에 실패하셨습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }catch (Exception e){
                    Toast.makeText(RegisterActivity.this, "오류", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}