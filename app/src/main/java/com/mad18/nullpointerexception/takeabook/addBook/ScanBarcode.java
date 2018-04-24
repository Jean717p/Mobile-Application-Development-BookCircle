package com.mad18.nullpointerexception.takeabook.addBook;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.zxing.Result;
import com.mad18.nullpointerexception.takeabook.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import me.dm7.barcodescanner.zxing.ZXingScannerView;



public class ScanBarcode extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }
    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }
    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }
    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        //Log.v(TAG, rawResult.getText()); // Prints scan results
        //Log.v(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        Log.d("barcode format", rawResult.getBarcodeFormat().toString());
        Log.d("print scan result", rawResult.getText());


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                JsonParser jsonParser = new JsonParser();
                JSONObject jsonObject = jsonParser.makeHttpRequest(
                        "https://www.googleapis.com/books/v1/volumes?q=isbn:" + rawResult.getText(),
                        "GET", new HashMap<String, String>());
                String totalItems = jsonObject.getString("totalItems");
                String id = jsonObject.getJSONArray("items").getJSONObject(0).getString("id");
                Log.d("title",totalItems);
                Log.d("title2",id);
                    final TextView title = (TextView)findViewById(R.id.add_book_title);
                    title.setText(id);
                    //Log.d("title",jsonObject.getJSONObject("items").getJSONArray("volumeInfo").getString(0));


                }
                catch(JSONException e){
                    e.printStackTrace();
                }
            }
        }).start();
        //Use JSONParser to download the JSON

        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);
        Intent intent = new Intent(ScanBarcode.this, AddBook.class);
        startActivity(intent);
    }

}
