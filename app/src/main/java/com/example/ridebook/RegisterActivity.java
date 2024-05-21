package com.example.ridebook;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    EditText mEmailEt, mPasswordEt, mConfPasswordEt;
    Button mRegisterBtn, License;

    private TextView uploadLicense;


    ProgressDialog progressDialog;

    private FirebaseAuth mAuth;

    private static final int PICK_IMAGE_REQUEST = 1;


    private static final int PICK_IMAGE_REQUEST_LICENSE = 4;




;

    private Uri uriImageLicence;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        mEmailEt = findViewById(R.id.emailET);
        mPasswordEt = findViewById(R.id.passwordET);
        mConfPasswordEt = findViewById(R.id.confpasswordET);
        mRegisterBtn = findViewById(R.id.registerBtn);
        License = findViewById(R.id.button_license);

        uploadLicense = findViewById(R.id.textview_upload_license);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User...");
        mAuth = FirebaseAuth.getInstance();






        License.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openFileChooser(PICK_IMAGE_REQUEST_LICENSE);
            }
        });

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();
                String confpassword = mConfPasswordEt.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your Email", Toast.LENGTH_LONG).show();
                    mEmailEt.setError("Email is required");
                    mEmailEt.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                } else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your password", Toast.LENGTH_LONG).show();
                    mPasswordEt.setError("Password is required");
                    mPasswordEt.requestFocus();
                } else if (password.length() < 8) {
                    Toast.makeText(RegisterActivity.this, "Password should be at least 8 characters", Toast.LENGTH_LONG).show();
                    mPasswordEt.requestFocus();
                } else if (!isAlphaNumeric(password)) { // Check for special characters
                    Toast.makeText(RegisterActivity.this, "Password should contain only letters and numbers", Toast.LENGTH_LONG).show();
                    mPasswordEt.requestFocus();
                } else if (TextUtils.isEmpty(confpassword)) {
                    Toast.makeText(RegisterActivity.this, "Please confirm your password", Toast.LENGTH_LONG).show();
                    mConfPasswordEt.setError("Password is required");
                    mConfPasswordEt.requestFocus();
                } else if (!password.equals(confpassword)) {
                    Toast.makeText(RegisterActivity.this, "Password and confirm password do not match", Toast.LENGTH_LONG).show();
                    mConfPasswordEt.setError("Password confirmation is required");
                    mConfPasswordEt.requestFocus();
                    mConfPasswordEt.clearComposingText();
                    mConfPasswordEt.clearComposingText();

                } else if (uriImageLicence == null) {
                    Toast.makeText(RegisterActivity.this, "Please upload your license.", Toast.LENGTH_LONG).show();
                } else {
                    registerUser(email, password);
                }
            }
        });

    }

    private boolean isAlphaNumeric(String s) {
        return s.matches("[a-zA-Z0-9]+");
    }

    private void openFileChooser(int requestCode) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST_LICENSE && resultCode == RESULT_OK && data != null && data.getData() !=null) {
            uriImageLicence = data.getData();

            String fileName = getFileNameFromUri(uriImageLicence);

            if (fileName != null){
                uploadLicense.setText(fileName);
            }
        }

    }



    private String getFileNameFromUri(Uri uri) {

        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }


    private void registerUser(String email, String password) {
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> emailVerificationTask) {
                                                if (emailVerificationTask.isSuccessful()) {
                                                    Toast.makeText(RegisterActivity.this,
                                                            "User registered successfully. Please verify your email",
                                                            Toast.LENGTH_LONG).show();

                                                    Intent emailIntent = new Intent(Intent.ACTION_MAIN);
                                                    emailIntent.addCategory(Intent.CATEGORY_APP_EMAIL);
                                                    startActivity(emailIntent);

                                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                                            Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(RegisterActivity.this,
                                                            "Failed to send verification email. Please try again later.",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }



                            if (uriImageLicence != null) {
                                uploadLicensePicture(uriImageLicence, user);
                            }


                            String email = user.getEmail();
                            String uid = user.getUid();

                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("onlineStatus", "online");
                            hashMap.put("typingTo", "noOne");
                            hashMap.put("name", "");
                            hashMap.put("phone", "");
                            hashMap.put("image", "");

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference reference = database.getReference("Users");
                            reference.child(uid).setValue(hashMap);

                            Toast.makeText(RegisterActivity.this, ""+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }






    private void uploadLicensePicture(Uri uriImageLicence, FirebaseUser firebaseUser) {
        uploadORCRPicture(uriImageLicence, "License", firebaseUser);
    }



    private void uploadORCRPicture(Uri uriImage, String folderName, FirebaseUser firebaseUser) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(folderName);
        StorageReference fileReference = storageReference.child(firebaseUser.getUid() + "." + getFileExtension(uriImage));

        fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri downloadUri = uri;

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "Failed to upload picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
