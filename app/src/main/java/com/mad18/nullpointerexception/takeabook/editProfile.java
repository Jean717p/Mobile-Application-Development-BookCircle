package com.mad18.nullpointerexception.takeabook;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.view.WindowManager;

public class editProfile extends AppCompatActivity {
    private SharedPreferences sharedPref;
    private int editTextBoxesIds[] = new int[]{R.id.Username,R.id.City,
            R.id.profile_about,R.id.profile_mail};
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getSharedPreferences(getString(R.string.app_name),Context.MODE_PRIVATE);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.app_name);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if(savedInstanceState == null){
            fillUserData();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_inbox, menu); //.xml file name
        goToEditMode();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_settings:
                storeUserEditData();
                finish();
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void storeUserEditData(){
        EditText text;
        SharedPreferences.Editor editor = sharedPref.edit();
        for(int i: editTextBoxesIds){
            text = findViewById(i);
            editor.putString(Integer.toString(i),text.getText().toString());
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
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        EditText text;
        for(int i: editTextBoxesIds){
            text = findViewById(i);
            text.setText(savedInstanceState.getString(Integer.toString(i),""));
        }
    }

    private void goToEditMode(){
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        changeIcon(R.drawable.ic_done_white_48dp);
        for(int i: editTextBoxesIds){
            findViewById(i).setEnabled(true);
        }
    }

    private void fillUserData(){
        EditText text;
        for(int i:editTextBoxesIds){
            text = findViewById(i);
            text.setText(sharedPref.getString(Integer.toString(i),""));
        }
    }

    public void changeIcon(int iconID){
        runOnUiThread(() -> {
            if (menu != null) {
                MenuItem item = menu.findItem(R.id.action_settings);
                if (item != null) {
                    item.setIcon(iconID);
                }
            }
        });
    }

    /***** To Do */
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode == RESULT_OK) {
//            if (requestCode == PICK_IMAGE) {
//                Uri selectedMediaUri = data.getData();
//                if (selectedMediaUri.toString().contains("image")) {
//                    //handle image  -- To Do salvare immagine e settarla come immagine
//                }
//            }
//        }
//    }
//
//    private void selectUserImg(){
//        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
//        getIntent.setType("image/*");
//        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        pickIntent.setType("image/*");
//        Intent chooserIntent = Intent.createChooser(getIntent,"Select Image");
//        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
//        startActivityForResult(chooserIntent, PICK_IMAGE);
//    }

}
