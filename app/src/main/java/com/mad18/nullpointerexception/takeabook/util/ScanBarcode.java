package com.mad18.nullpointerexception.takeabook.util;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


/**
 * @apiNote intent.putExtra("toSearch","toSearch") for download only (an already inserted isbn)
 * @apiNote intent.putExtra("justScan","justScan") to just scan the barcode
 */
public class ScanBarcode extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private ZXingScannerView mScannerView;
    private String uid = "";

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        Intent intent = getIntent();
        String x = intent.getStringExtra("toSearch");
        if(x==null) {
            mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
            setContentView(mScannerView);                // Set the scanner view as the content view
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String x = intent.getStringExtra("toSearch");
        if (x == null) {
            mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
            mScannerView.startCamera();          // Start camera on resume
        }
        else{
            downloadJson(x);
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        Intent intent = getIntent();
        String x = intent.getStringExtra("toSearch");
        if (x == null) {
            mScannerView.stopCamera();           // Stop camera on pause
        }
    }


    /**
     * Gestisce il risultato della scansione, chiamando il metodo che permette di scaricare il Json
     * @param rawResult
     */
    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        //Log.v(TAG, rawResult.getText()); // Prints scan results
        //Log.v(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

        Log.d("barcode format", rawResult.getBarcodeFormat().toString());
        Log.d("print scan result", rawResult.getText());
        if(rawResult.getText().length()==0){
            setResult(RESULT_CANCELED);
            finish();
        }
        else if(getIntent().getStringExtra("justScan")!=null){
            Intent intent = new Intent();
            intent.putExtra("isbn",rawResult.getText());
            setResult(RESULT_OK,intent);
            finish();
        }
        else{
            downloadJson(rawResult.getText());
        }
    }

    /**
     * Metodo che permette di scaricare il JSON che descrive il libro a partire dall'ISBN.
     * Crea un nuovo thread da cui viene effettuata la chiamata alla URL delle Google API.
     * Ottenuto il JSON object viene percorso l'albero risultante, controllando l'esistenza di ogni elemento.
     * I dati ottenuti nel JSON vengono inseriti in un oggetto di tipo Bookwrapper, inserito come Bundle
     * dell'intent tramite il metodo putExtra().
     * @param ISBN
     */
    private void downloadJson(String ISBN){
        Intent intent = new Intent();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JsonParser jsonParser = new JsonParser();
                    JSONObject jsonObject = jsonParser.makeHttpRequest(
                            "https://www.googleapis.com/books/v1/volumes?q=isbn:" + ISBN,
                            "GET", new HashMap<String, String>());
                    String title="";
                    List<String> authors=new LinkedList<>();
                    List<String> categories=new LinkedList<>();
                    String publisher="";
                    String thumbnail = "";
                    String category = "";
                    int pages= 0;
                    int editionYear=-1;
                    if(jsonObject.has("items")){
                        JSONObject tmp =  jsonObject.getJSONArray("items").getJSONObject(0);
                        if(tmp.has("volumeInfo")){
                            tmp = tmp.getJSONObject("volumeInfo");
                            if(tmp.has("title")){
                                title = tmp.getString("title");
                            }
                            if(tmp.has("pageCount")){
                                pages = tmp.getInt("pageCount");
                            }
                            if(tmp.has("authors")){
                                JSONArray Jauthors = tmp.getJSONArray("authors");
                                for(int i=0; i<Jauthors.length();i++) {
                                    authors.add(Jauthors.getString(i));
                                }

                            }
                            if(tmp.has("publisher")){
                                publisher =tmp.getString("publisher");
                            }
                            if(tmp.has("publishedDate")){
                                try{
                                    String SeditionYear = tmp.getString("publishedDate");
                                    editionYear = Integer.parseInt(SeditionYear);
                                    if(editionYear<0){
                                        editionYear=0;
                                    }
                                }
                                catch (Exception e){
                                    editionYear = 0;
                                }

                            }
                            if(tmp.has("categories")){
                                JSONArray Jcategories = tmp.getJSONArray("categories");
                                for(int i=0; i<Jcategories.length();i++) {
                                    categories.add(Jcategories.getString(i));
                                }
                            }
                            if(tmp.has("imageLinks")){
                                tmp =  tmp.getJSONObject("imageLinks");
                                if(tmp.has("thumbnail")){
                                    thumbnail = tmp.getString("thumbnail");
                                }
                            }

                        }
                    }
                    FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
                    BookWrapper bookWrapper = new BookWrapper(ISBN,title,authors, publisher,editionYear,
                            thumbnail, categories,"",fbuser.getUid(),0,0, pages,false);
                    intent.putExtra("bookinfo", bookWrapper);
                    setResult(RESULT_OK,intent);
                    finish();
                }
                catch(JSONException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

