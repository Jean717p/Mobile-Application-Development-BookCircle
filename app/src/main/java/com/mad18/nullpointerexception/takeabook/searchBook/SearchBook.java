package com.mad18.nullpointerexception.takeabook.searchBook;

import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.mad18.nullpointerexception.takeabook.Book;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.addBook.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mad18.nullpointerexception.takeabook.mainActivity.Main_BorrowedBooks_Fragment;
import com.mad18.nullpointerexception.takeabook.mainActivity.Main_LentBooks_Fragment;
import com.mad18.nullpointerexception.takeabook.mainActivity.Main_MyLibrary_Fragment;
import com.mad18.nullpointerexception.takeabook.mainActivity.Main_TopBooks_Fragment;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;


public class SearchBook extends AppCompatActivity {
    private final String TAG="SearchBook";
    private Toolbar toolbar;
    private String searchBase;
    private Menu menu;
    private MyAtomicCounter booksFoundCounter;
    private List<Book> booksFound;
    private ViewPager viewPager;
    private SearchBookPagerAdapter myAdapter;
    private TabLayout tabLayout;
    private Boolean resultFragmentChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_book);
        setTitle(R.string.title_activity_search_book);
        searchBase = getIntent().getStringExtra("action");
        Toolbar toolbar = findViewById(R.id.search_book_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        booksFound = new LinkedList<>();
        booksFoundCounter = new MyAtomicCounter(0);
        // Create an instance of the tab layout from the view.
        tabLayout = (TabLayout) findViewById(R.id.search_book_tab_layout);
        // Set the text for each tab.
        tabLayout.addTab(tabLayout.newTab().setText("Search"));
        tabLayout.addTab(tabLayout.newTab().setText("Found"));
        // Set the tabs to fill the entire layout.
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        viewPager = (ViewPager) findViewById(R.id.search_book_view_pager);
        myAdapter = new SearchBookPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(myAdapter);
        resultFragmentChanged = true;
        viewPager.addOnPageChangeListener(new
                TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                SearchBookPagerAdapter adapter = (SearchBookPagerAdapter) viewPager.getAdapter();
                switch (tab.getPosition()){
                    case 0:
                        break;
                    case 1:
                        if(resultFragmentChanged){
                            SearchBook_found f = (SearchBook_found) adapter.getRegisteredFragment(viewPager.getCurrentItem());
                            if(f!=null){
                                f.updateView(booksFound);
                            }
                            resultFragmentChanged=false;
                        }
                        break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        booksFoundCounter.setListener(new OnCounterChangeListener() {
            @Override
            public void onCounterReachZero() {
                double user_lat=0,user_long=0;
                Location user_loc;
                resultFragmentChanged=true;
                //Log.d(TAG,"Reached 0");
                Bundle intentExtras = getIntent().getExtras();
                if(booksFound.size()==0){
                    /** change UI - NO books found **/
                    //Call anothre activity/fragment to show books
                }
                else if(booksFound.size()==1){
                    /** change UI **/
                    tabLayout.getTabAt(1).select();
                    return;
                }
                //Sort alfabelito booksfound
                /** Change UI **/
                tabLayout.getTabAt(1).select();
                //Call anothre activity/fragment to show books
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                finish();
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void searchForBook(String flag){
        EditText text = findViewById(R.id.search_book_edit_text);
        String to_find;
        to_find = text.getText().toString();
        getFromGoogleApi(flag,to_find);
        //Startare l'intent della nuova activity o riempire fragment con i risultati

    }

    private void searchBooksOnFireStore(String flag, List<String> inputList){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if(inputList.size()==0){
            booksFoundCounter.getListener().onCounterReachZero();
            return;
        }
        CollectionReference booksRef = db.collection("books");
        booksFoundCounter.set(1);
        for(String x : inputList){
            switch(flag){
                case "Title":
                    Query query_t = booksRef.whereEqualTo("book_title",x);
                    booksFoundCounter.increment();
                    query_t.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<DocumentSnapshot> documents = task.getResult().getDocuments();
                            for(DocumentSnapshot documentSnapshot:documents){
                                if(documentSnapshot!=null){
                                    if(documentSnapshot.exists()){
                                        booksFound.add(documentSnapshot.toObject(Book.class));
                                        break;
                                    }
                                }
                            }
                            booksFoundCounter.decrement();
                        }
                    });
                    break;
                case "Author":
                    Query query_a = booksRef.whereEqualTo("book_authors."+x,true);
                    booksFoundCounter.increment();
                    query_a.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<DocumentSnapshot> documents = task.getResult().getDocuments();
                            for(DocumentSnapshot documentSnapshot:documents){
                                if(documentSnapshot!=null){
                                    if(documentSnapshot.exists()){
                                        booksFound.add(documentSnapshot.toObject(Book.class));
                                        break;
                                    }
                                }
                            }
                            booksFoundCounter.decrement();
                        }
                    });
                    break;
                case "ISBN":
                    Query query_i = booksRef.whereEqualTo("book_ISBN",x);
                    booksFoundCounter.increment();
                    query_i.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<DocumentSnapshot> documents = task.getResult().getDocuments();
                            for(DocumentSnapshot documentSnapshot:documents){
                                if(documentSnapshot!=null){
                                    if(documentSnapshot.exists()){
                                        booksFound.add(documentSnapshot.toObject(Book.class));
                                        break;
                                    }
                                }
                            }
                            booksFoundCounter.decrement();
                        }
                    });
                    break;
                default:
                    return;
            }
        }
        booksFoundCounter.decrement();
        return;
    }
    /**
     * Metodo utilizzato per ricavare le liste dalle googleAPI
     */
    private void getFromGoogleApi(String flag, String text){
        List list = new LinkedList();
        list.add(text);
        switch (flag){
            case "Title":
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int numItems = getNumItemsFromApi(text,flag);

                            int maxCycle = numItems / 40; //Posso prendere solo 40 item alla volta dal json
                            if((numItems%40) != 0) {
                                maxCycle++;
                            }
                            JsonParser jsonParser = new JsonParser();
                            for(int i = 0; i < maxCycle; i++) {
                                String url = "https://www.googleapis.com/books/v1/volumes?maxResults=40&orderBy=relevance&q=intitle:" +
                                        text +
                                        "&fields=items(volumeInfo/title)&startIndex=" + Integer.toString(i*40);
                                JSONObject jsonObject = jsonParser.makeHttpRequest(url,
                                        "GET", new HashMap<String, String>());
                                if(jsonObject.has("items")){
                                    JSONArray tmp =  jsonObject.getJSONArray("items");
                                    for(int j = 0; j < tmp.length(); j++) {
                                        JSONObject item = tmp.getJSONObject(j);
                                        if(item.has("volumeInfo")){
                                            JSONObject vol = item.getJSONObject("volumeInfo");
                                            if(vol.has("title")){
                                                String info = vol.getString("title");
                                                if(list.contains(info) == false) {
                                                    list.add(info);
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                            searchBooksOnFireStore(flag,list);
                        }
                        catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            case "Author":
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int numItems = getNumItemsFromApi(text,flag);
                            int maxCycle = numItems / 40; //Posso prendere solo 40 item alla volta dal json
                            if((numItems%40) != 0) {
                                maxCycle++;
                            }
                            JsonParser jsonParser = new JsonParser();
                            for(int i = 0; i < maxCycle; i++) {
                                JSONObject jsonObject = jsonParser.makeHttpRequest(
                                        "https://www.googleapis.com/books/v1/volumes?maxResults=40&orderBy=relevance&q=inauthor:" +
                                                text +
                                                "&fields=items(volumeInfo/title)&startIndex=" + Integer.toString(i*40) ,
                                        "GET", new HashMap<String, String>());
                                JSONArray tmp =  jsonObject.getJSONArray("items");
                                for(int j = 0; j < tmp.length(); j++) {
                                    JSONObject item = tmp.getJSONObject(j);
                                    if(item.has("volumeInfo")){
                                        JSONObject vol = item.getJSONObject("volumeInfo");
                                        if(vol.has("title")){
                                            String info = vol.getString("title");
                                            if(list.contains(info) == false) {
                                                list.add(info);
                                            }
                                        }
                                    }
                                }
                            }
                            searchBooksOnFireStore(flag,list);
                        }
                        catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                }).start();
            break;
            case "ISBN":

                searchBooksOnFireStore(flag,list);
                break;
        }
        return;
    }

    private int getNumItemsFromApi(String text, String flag) {
        int totItems = 0;
        switch (flag){
            case "Title":
                try {
                    JsonParser jsonParser = new JsonParser();
                    JSONObject jsonObject = jsonParser.makeHttpRequest(
                            "https://www.googleapis.com/books/v1/volumes?q=intitle:" + text + "&fields=totalItems",
                            "GET", new HashMap<String, String>());
                    if (jsonObject.has("totalItems")) {
                        totItems = jsonObject.getInt("totalItems");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }

                break;
            case "Author":
                try {
                    JsonParser jsonParser = new JsonParser();
                    JSONObject jsonObject = jsonParser.makeHttpRequest(
                            "https://www.googleapis.com/books/v1/volumes?q=inauthor:" + text + "&fields=totalItems",
                            "GET", new HashMap<String, String>());
                    if (jsonObject.has("totalItems")) {
                        totItems = jsonObject.getInt("totalItems");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }

                break;
        }
        return totItems;

    }

    private interface OnCounterChangeListener{
        public void onCounterReachZero();
    }

    private class MyAtomicCounter{
        private OnCounterChangeListener listener;
        private AtomicInteger atomicInteger;

        public MyAtomicCounter(int initialValue){
            atomicInteger = new AtomicInteger(initialValue);
        }

        public OnCounterChangeListener getListener() {
            return listener;
        }

        public void setListener(OnCounterChangeListener listener) {
            this.listener = listener;
        }

        public void decrement(){
            int value = atomicInteger.decrementAndGet();
            if(listener!=null){
                if(value ==0){
                    listener.onCounterReachZero();
                }
            }
        }
        public void increment(){
            int value = atomicInteger.incrementAndGet();
            if(listener!=null){
                if(value ==0){
                    listener.onCounterReachZero();
                }
            }
        }
        public void set(int value){
            atomicInteger.set(value);
            if(listener!=null){
                if(value ==0){
                    listener.onCounterReachZero();
                }
            }
        }
    }

    private static class SearchBookPagerAdapter extends FragmentStatePagerAdapter {
        SparseArray<Fragment> registeredFragments = new SparseArray<>();
        private int NUM_ITEMS=2;

        public SearchBookPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }
        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }
        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return SearchBook_search.newInstance(0,"Search a book");
                case 1: // Fragment # 0 - This will show FirstFragment
                    return SearchBook_found.newInstance(1,"Books Found");
                default:
                    return null;
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position,fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment(int position){
            return registeredFragments.get(position);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page "+position;
        }
    }

}

