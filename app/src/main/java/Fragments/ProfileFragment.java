package Fragments;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;


import com.example.ridebook.AddpostActivity;
import com.example.ridebook.MainActivity;
import com.example.ridebook.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import adapters.AdapterPosts;
import models.ModelPost;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    String storagePath = "Users_Profile_Cover_Imgs/";
    ImageView avatar, Cover;
    TextView nameTv, emailTv, phoneTv;
    FloatingActionButton fab;
    RecyclerView postsRecyclerView;
    ProgressDialog pd;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    String cameraPermissions[];
    String storagePermissions[];

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    Uri image_uri;

    String profileOrCoverPhoto;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        avatar = view.findViewById(R.id.Avatar);
        Cover = view.findViewById(R.id.coverTv);
        nameTv = view.findViewById(R.id.NameTv);
        emailTv = view.findViewById(R.id.EmailTv);
        phoneTv = view.findViewById(R.id.PhoneTv);
        fab = view.findViewById(R.id.fab);
        postsRecyclerView = view.findViewById(R.id.recyclerview_posts);
        pd = new ProgressDialog(getActivity());

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds: snapshot.getChildren()){
                    String name = ""+ ds.child("name").getValue();
                    String email = ""+ ds.child("email").getValue();
                    String phone = ""+ ds.child("phone").getValue();
                    String image = ""+ ds.child("image").getValue();
                    String cover = ""+ ds.child("cover").getValue();



                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);

                    try{
                        Picasso.get().load(image).into(avatar);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default_img_white).into(avatar);
                    }

                    try{
                        Picasso.get().load(cover).into(Cover);
                    }
                    catch (Exception e){
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        postList = new ArrayList<>();
        checkUserStatus();
        loadmyPosts();
        return view;
    }

    private void loadmyPosts() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postsRecyclerView.setLayoutManager(layoutManager);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);
                    postList.add(myPosts);

                    adapterPosts = new AdapterPosts(getActivity(),postList);
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void searchmyPosts(final String searchQuery) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postsRecyclerView.setLayoutManager(layoutManager);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    if(myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase())){
                        postList.add(myPosts);
                    }


                    adapterPosts = new AdapterPosts(getActivity(),postList);
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission(){
       requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }


    private boolean checkCameraPermission(){

        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission(){
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog() {
        String options[] = {"Edit Profile Picture", "Edit Cover Photo", "Edit Name","Edit Phone","Change Password"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Action");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {

                    pd.setMessage("Updating Profile Picture");
                    profileOrCoverPhoto = "image";
                    showImagePicDialog();

                } else if (which == 1) {
                    pd.setMessage("Updating Cover Photo");
                    profileOrCoverPhoto = "cover";
                    showImagePicDialog();


                } else if (which == 2) {
                    pd.setMessage("Updating Name");
                    showNamePhoneUpdateDialog("name");


                } else if (which == 3) {
                    pd.setMessage("Updating Phone");
                    showNamePhoneUpdateDialog("phone");


                }
                else if (which == 4) {
                    pd.setMessage("Change Password");
                    showChangePasswordDialog();


                }
            }
        });

        builder.create().show();
    }

    private void showChangePasswordDialog() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_update_password, null);
        EditText passwordEt = view.findViewById(R.id.passwordET);
        EditText newpasswordEt = view.findViewById(R.id.newpasswordET);
        Button updatePasswordBtn = view.findViewById(R.id.updatepasswordbtn);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();

        updatePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = passwordEt.getText().toString().trim();
                String newPassword = newpasswordEt.getText().toString().trim();
                if(TextUtils.isEmpty(oldPassword)){
                    Toast.makeText(getActivity(), "Enter your current password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(newPassword.length()<8){
                    Toast.makeText(getActivity(), "Password length must atleast 8 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                updatePassword(oldPassword, newPassword);
            }
        });

    }

    private void updatePassword(String oldPassword, String newPassword) {
        pd.show();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
        user.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        pd.dismiss();
                                        Toast.makeText(getActivity(), "Password Updated", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showNamePhoneUpdateDialog(String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+ key);
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        EditText editText = new EditText(getActivity());
        editText.setHint("Enter " +key );
        linearLayout.addView(editText);

        builder.setView(linearLayout);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String  value = editText.getText().toString().trim();

                if(!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                    if(key.equals("name")){
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds: snapshot.getChildren()){
                                    String child = ds.getKey();
                                    snapshot.getRef().child(child).child("uName").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds: snapshot.getChildren()){
                                    String child = ds.getKey();
                                        if (snapshot.child(child).hasChild("Comments")){
                                            String child1 = ""+snapshot.child(child).getKey();
                                            Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid")
                                                    .equalTo(uid);
                                            child2.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    for (DataSnapshot ds: snapshot.getChildren()){
                                                        String child = ds.getKey();
                                                        snapshot.getRef().child(child).child("uName").setValue(value);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
                else {
                    Toast.makeText(getActivity(), "Please Enter"+key +"", Toast.LENGTH_SHORT).show();

                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

    private void showImagePicDialog() {
        String options[] = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromCamera();
                    }
                } else if (which == 1) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });

        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length >0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageAccepted){
                        pickFromCamera();
                    }

                    else {
                        Toast.makeText(getActivity(), "Please enable camere & storage permission", Toast.LENGTH_SHORT).show();
                    }

                }
            }
            break;
            case STORAGE_REQUEST_CODE:{

                if(grantResults.length >0){
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted){
                        pickFromGallery();
                    }

                    else {
                        Toast.makeText(getActivity(), "Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }

                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){

                image_uri = data.getData();

                uploadProfileCoverPhoto(image_uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                
                uploadProfileCoverPhoto(image_uri);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri uri) {

        pd.show();

        String filePathAndName = storagePath+ "" + profileOrCoverPhoto + "_" + user.getUid();

        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();

                        if (uriTask.isSuccessful()){
                            HashMap<String, Object> result = new HashMap<>();
                            result.put(profileOrCoverPhoto, downloadUri.toString());
                            databaseReference.child(user.getUid()).updateChildren(result)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Image Updated...", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Error Updating Image...", Toast.LENGTH_SHORT).show();

                                        }
                                    });

                            if(profileOrCoverPhoto.equals("image")){
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                Query query = ref.orderByChild("uid").equalTo(uid);
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for(DataSnapshot ds: snapshot.getChildren()){
                                            String child = ds.getKey();
                                            snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds: snapshot.getChildren()){
                                            String child = ds.getKey();
                                            if (snapshot.child(child).hasChild("Comments")){
                                                String child1 = ""+snapshot.child(child).getKey();
                                                Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid")
                                                        .equalTo(uid);
                                                child2.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        for (DataSnapshot ds: snapshot.getChildren()){
                                                            String child = ds.getKey();
                                                            snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                        }
                        else {
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Some error occured", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult (cameraIntent, IMAGE_PICK_CAMERA_CODE);

    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //mProfileTv.setText(user.getEmail());

            uid = user.getUid();
        }
        else {
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }





    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!TextUtils.isEmpty(query)){
                    searchmyPosts(query);
                }
                else {
                    loadmyPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if(!TextUtils.isEmpty(newText)){
                    searchmyPosts(newText);
                }
                else {
                    loadmyPosts();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_post){
            startActivity(new Intent(getActivity(), AddpostActivity.class));
        }

        if (id == R.id.action_set){
            startActivity(new Intent(getActivity(), AddpostActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}