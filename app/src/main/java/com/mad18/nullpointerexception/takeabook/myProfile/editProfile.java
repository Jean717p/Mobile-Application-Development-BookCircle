package com.mad18.nullpointerexception.takeabook.myProfile;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.mad18.nullpointerexception.takeabook.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class editProfile extends AppCompatActivity {
    private SharedPreferences sharedPref;
    private int editTextBoxesIds[] = new int[]{R.id.edit_profile_Username,R.id.edit_profile_City,
            R.id.edit_profile_mail,R.id.edit_profile_about};
    private Menu menu;
    private final int REQUEST_PICK_IMAGE = 1, REQUEST_IMAGE_CAPTURE = 2;
    private final int REQUEST_PERMISSION_CAMERA = 2, REQUEST_PERMISSION_GALLERY=1;
    private String profileImgName = "profile.jpg";
    private Bitmap profileImg = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getSharedPreferences(getString(R.string.app_name),Context.MODE_PRIVATE);
        setContentView(R.layout.edit_profile);
        Toolbar toolbar = findViewById(R.id.edit_profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.app_name);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if(savedInstanceState == null){
            fillUserData();
        }
        ImageView iw = findViewById(R.id.edit_profile_personalPhoto);
        iw.setClickable(true);
        iw.setOnClickListener(view -> selectUserImg());
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_profile_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.edit_profile_action_save:
                storeUserEditData();
                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                return true;
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void storeUserEditData(){
        EditText text;
        String s; File file;
        SharedPreferences.Editor editor = sharedPref.edit();
        int i=0;
        for(String x: showProfile.sharedUserDataKeys){
            text = findViewById(editTextBoxesIds[i++]);
            editor.putString(x,text.getText().toString());
        }
        if(profileImg!=null){
            editor.putString(profileImgName,saveToInternalStorage(profileImg,profileImgName));
        }
        else{
            s = sharedPref.getString(profileImgName,"");
            if(s.length()>0){
                file = new File(s);
                if(file.exists()){
                    file.delete();
                }
            }
            editor.putString(profileImgName,"");
        }
        editor.apply();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        EditText text;
        for(int i: editTextBoxesIds){
            text = findViewById(i);
            outState.putString(Integer.toString(i),text.getText().toString());
        }
        if(profileImg!=null){
            outState.putString("profileImgPath",saveToInternalStorage(profileImg,"temp_"+profileImgName));
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        EditText text;
        String path = savedInstanceState.getString("profileImgPath");
        for(int i: editTextBoxesIds){
            text = findViewById(i);
            text.setText(savedInstanceState.getString(Integer.toString(i),""));
        }
        if(path!=null) {
            File file = new File(path);
            if (file.exists()) {
                profileImg = loadImageFromStorage(file.getAbsolutePath(), R.id.edit_profile_personalPhoto);
                file.delete();
            }
        }
    }

    private void fillUserData(){
        TextView text;
        String y;
        int i=0;
        for(String x:showProfile.sharedUserDataKeys){
            text = (EditText) findViewById(editTextBoxesIds[i++]);
            y=sharedPref.getString(x,"");
            if(y.length()>0){
                text.setText(y);
            }
        }
        y=sharedPref.getString(profileImgName,"");
        if(y.length()>0){
            profileImg = loadImageFromStorage(sharedPref.getString(profileImgName,""),R.id.edit_profile_personalPhoto);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        ImageView iw;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_IMAGE:
                    if (data != null) {
                        Uri selectedMediaUri = data.getData();
                        try {
                            profileImg = MediaStore.Images.Media.getBitmap(
                                    this.getContentResolver(),selectedMediaUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        iw = findViewById(R.id.edit_profile_personalPhoto);
                        if(iw!=null && profileImg != null) {
                            iw.setImageBitmap(profileImg);
                        }
                    }
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    if (data != null) {
                        profileImg = (Bitmap) data.getExtras().get("data");
                        iw = findViewById(R.id.edit_profile_personalPhoto);
                        if(iw!=null && profileImg != null) {
                            iw.setImageBitmap(profileImg);
                        }
                    }
                    break;
            }
        }
    }

    private void selectUserImg(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        //pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                getString(R.string.photo_from_gallery),
                getString(R.string.photo_from_camera),
                getString(R.string.photo_remove) };
        pictureDialog.setItems(pictureDialogItems,
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            choosePhotoFromGallery();
                            break;
                        case 1:
                            choosePhotoFromCamera();
                            break;
                        case 2:
                            removeUserImg();
                            break;
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallery() {
        if(ActivityCompat.checkSelfPermission(editProfile.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(editProfile.this,new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE }
                    ,REQUEST_PERMISSION_GALLERY);
            return;
        }
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_PICK_IMAGE);
    }

    private void choosePhotoFromCamera() {
        if(ActivityCompat.checkSelfPermission(editProfile.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                ||ActivityCompat.checkSelfPermission(editProfile.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(editProfile.this,new String[]{
                            Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE }
                    ,REQUEST_PERMISSION_CAMERA);
            return;
        }
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
    }

    private void removeUserImg(){
        ImageView iw = findViewById(R.id.edit_profile_personalPhoto);
        if(profileImg!=null){
            iw.setImageResource(R.drawable.ic_account_circle_white_48px);
            profileImg = null;
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage,String filename){
        if(bitmapImage==null){
            return null;
        }
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/appname/app_data/imageDir internal
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        //Environment.getExternalStorageDirectory(); sd
        // Create imageDir
        File file = new File(directory,filename);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            // Use the compress method on the BitMap object to write image to the OutputStream
            if(bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, out)==false){
                //decrementare secondo parametro per compressare
                out.close();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file.getAbsolutePath();
    }

    private Bitmap loadImageFromStorage(String path,int id) {
        if(path==null){
            return null;
        }
        Bitmap b = null;
        File file = new File(path);
        ImageView img = (ImageView) findViewById(id);
        if(file.exists() == false||img==null){
            return null;
        }
        try {
            b = BitmapFactory.decodeStream(new FileInputStream(file));
            img.setImageBitmap(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return b;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_PERMISSION_GALLERY:
                if(grantResults.length>0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        choosePhotoFromGallery();
                    }
                }
                break;
            case REQUEST_PERMISSION_CAMERA:
                if(grantResults.length>0){
                    if(grantResults[0]==PackageManager.PERMISSION_GRANTED
                            && grantResults[1]==PackageManager.PERMISSION_GRANTED){
                        choosePhotoFromCamera();
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}

