package com.example.uploadimage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
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

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {
    private EditText email,password,cpassword;
    private FirebaseAuth fauth;
    private Button signup;
    private boolean isemail,ispassword,iscpassword;
    private TextInputLayout emailError,passwordError,cpasswordError;
    private TextView login;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        email = findViewById(R.id.S_email);
        password = findViewById(R.id.S_password);
        cpassword = findViewById(R.id.S_cpassword);
        signup = findViewById(R.id.S_signup);
        fauth = FirebaseAuth.getInstance();
        emailError = findViewById(R.id.S_erroremail);
        passwordError = findViewById(R.id.S_errorpassword);
        cpasswordError = findViewById(R.id.S_errorcpassword);
        login = findViewById(R.id.textlogin);
        pd = new ProgressDialog(this);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this,LoginActivity.class));
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email_txt = email.getText().toString();
                String password_txt = password.getText().toString();
                String cpassword_txt = cpassword.getText().toString();

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
                else if(password_txt.length()<6)
                {

                    passwordError.setError(getResources().getString(R.string.password_error_invalid));
                    ispassword = false;
                }
                else
                {
                    ispassword = true;
                    passwordError.setErrorEnabled(false);
                }


                if(cpassword_txt.isEmpty())
                {
                    cpasswordError.setError(getResources().getString(R.string.cpassword_error));
                    iscpassword = false;
                }
                else if(!password_txt.equals(cpassword_txt))
                {
                    cpasswordError.setError(getResources().getString(R.string.cpassword_error));
                    iscpassword = false;
                }
                else
                {
                    iscpassword = true;
                    cpasswordError.setErrorEnabled(false);
                }


                if(isemail && ispassword && iscpassword)
                {
                    pd.setMessage("Registering Please Wait.");
                    pd.show();
                    pd.setCancelable(false);
                    authenticate(email_txt,password_txt);
                }
            }
        });
    }

    private void authenticate(String email_txt, String password_txt) {
        fauth.createUserWithEmailAndPassword(email_txt,password_txt)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        pd.dismiss();
                        Toast.makeText(SignupActivity.this,"Tou have been successfully registered",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignupActivity.this,HomeActivity.class));

                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SignupActivity.this,"Error : "+e.getMessage(),Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                });
    }


}