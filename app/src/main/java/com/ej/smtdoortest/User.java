package com.ej.smtdoortest;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    private String idToken; // Firebase Uid (고유 토큰정보)
    private String userName;
    private String emailId; // 이메일 아이디 (Key값으로 사용할 것임)
    private String password; // 비밀번호

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
        // 파이어베이스는 생성자를 직접 만들어 줘야한다. 디폴트 생성자 없음...
    }

    public User(String userName, String email) {
        this.userName = userName;
        this.emailId = email;
    }

    public String getIdToken(){ return idToken; }

    public void setIdToken(String idToken){ this.idToken = idToken; }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmailId() { return emailId; }

    public void setEmailId(String email) {
        this.emailId = email;
    }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", email='" + emailId + '\'' +
                '}';
    }
}