package com.example.uploadimage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import static androidx.core.content.ContextCompat.checkSelfPermission;

public class HomeActivity extends AppCompatActivity {
    private ImageView image;
    private Button logout,upload;
    private FirebaseAuth fauth;
    private FirebaseFirestore fstore;
    private String timestamp,downloadurl;
    private Uri imageuri;
    private StorageReference imageref;
    private String id;
    private static final int Gallery_pick = 1000;
    private static final int Permission_code = 1001;
    private FirebaseUser user;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        image = findViewById(R.id.image);
        logout= findViewById(R.id.logout);
        upload = findViewById(R.id.upload);
        fstore = FirebaseFirestore.getInstance();
        fauth = FirebaseAuth.getInstance();
        imageref = FirebaseStorage.getInstance().getReference("Images");
        timestamp = String.valueOf(System.currentTimeMillis());
        id = fauth.getCurrentUser().getUid();
        user = fauth.getCurrentUser();
        pd = new ProgressDialog(this);

        if(user==null)
        {
            startActivity(new Intent(HomeActivity.this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
        }

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setMessage("Are you sure you want to logout ?");
                builder.setCancelable(false);
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fauth.signOut();
                        Toast.makeText(HomeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(HomeActivity.this,MainActivity.class));
                        finish();
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(HomeActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED){
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissions,Permission_code);
                    }else{
                        pickImageFromGallery();
                    }

                }else{
                    pickImageFromGallery();
                }
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageuri.toString().length()==0)
                {
                    Toast.makeText(HomeActivity.this,"No image is selected",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    pd.setMessage("Uploading Image");
                    pd.show();
                    pd.setCancelable(false);

                    Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                    byte[] data = baos.toByteArray();

                    StorageReference filePath = imageref.child(imageuri.getLastPathSegment()+"_"+timestamp+".jpeg");


                    filePath.putBytes(data)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                    while(!uriTask.isSuccessful());

                                    downloadurl = uriTask.getResult().toString();
                                    if(uriTask.isSuccessful())
                                    {
                                        HashMap<String,Object> doc = new HashMap<>();

                                        doc.put("Image",downloadurl);
                                        doc.put("Id",id);
                                        doc.put("pid",id + timestamp);
                                        doc.put("time",timestamp);

                                        fstore.collection("Image").document(id+timestamp)
                                                .set(doc)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        pd.dismiss();
                                                        Toast.makeText(HomeActivity.this,"image is Uploaded",Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        pd.dismiss();
                                                        Toast.makeText(HomeActivity.this,"Error : "+e.getMessage(),Toast.LENGTH_SHORT).show();

                                                    }
                                                });
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(HomeActivity.this,"Error : "+e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            });


                }

            }
        });



    }

    private void pickImageFromGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK);
        gallery.setType("image/*");
        startActivityForResult(gallery,Gallery_pick);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case Permission_code:{
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    pickImageFromGallery();
                }
                else
                {
                    Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == Gallery_pick && data != null) {
            imageuri = data.getData();
            image.setImageURI(imageuri);

        }
    }



}