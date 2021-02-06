package com.example.uploadimage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private EditText email,password;
    private Button login;
    private FirebaseAuth fauth;
    private boolean isemail,ispassword;
    private TextInputLayout emailError,passwordError;
    private TextView signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.L_email);
        password = findViewById(R.id.L_password);
        login = findViewById(R.id.L_login);
        fauth = FirebaseAuth.getInstance();
        emailError = findViewById(R.id.L_erroremail);
        passwordError = findViewById(R.id.L_errorpassword);
        signup = findViewById(R.id.create_one);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,SignupActivity.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email_txt = email.getText().toString();
                String password_txt = password.getText().toString();

                if(email_txt.isEmpty())
                {
                    emailError.setError(getResources().getString(R.string.email_error));
                    isemail = false;
                }
                else
                {
                    isemail = true;
                    emailError.setErrorEnabled(false);
                }


                if(password_txt.isEmpty())
                {
                    passwordError.setError(getResources().getString(R.string.password_error));
                    ispassword = false;
                }
                else
                {
                    ispassword = true;
                    passwordError.setErrorEnabled(false);
                }

                if(isemail && ispassword)
                {
                    loginuser(email_txt,password_txt);
                }
            }
        });
    }

    private void loginuser(String email_txt, String password_txt) {

        fauth.signInWithEmailAndPassword(email_txt,password_txt)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText(LoginActivity.this,"You are successfully logged in",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this,HomeActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivity.this,"Error : "+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }


}