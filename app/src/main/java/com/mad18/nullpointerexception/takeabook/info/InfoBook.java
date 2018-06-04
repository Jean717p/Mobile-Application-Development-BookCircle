package com.mad18.nullpointerexception.takeabook.info;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad18.nullpointerexception.takeabook.GlideApp;
import com.mad18.nullpointerexception.takeabook.util.ImageViewPopUpHelper;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.requestBook.RequestBook;
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.util.BookWrapper;
import com.mad18.nullpointerexception.takeabook.util.User;
import com.mad18.nullpointerexception.takeabook.util.UserWrapper;

import java.util.LinkedList;
import java.util.List;


public class InfoBook extends AppCompatActivity {

    private final int BOOK_EFFECTIVELY_REMOVED = 41;
    private final int REQUEST_BOOK = 7;
    private final int idTextViewIds[] = new int[]{R.id.info_book_title, R.id.info_book_author, R.id.info_book_ISBN,
            R.id.info_book_editionYear, R.id.info_book_publisher, R.id.info_book_categories, R.id.info_book_description,
            R.id.info_book_pages};

    private BookWrapper bookToShowInfoOf;
    private FirebaseAuth mAuth;
    private Menu menu;
    private LinearLayout horizontal_photo_list;
    private View horizontal_photo_list_element;
    private List<String> for_me;
    private User bookOwner;
    private SharedPreferences sharedPref;
    private Context context;
    private AppCompatActivity myActivity;
    private Book myBook;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        myActivity = this;
        setContentView(R.layout.info_book);
        Toolbar toolbar = findViewById(R.id.info_book_toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_activity_info_book);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bookToShowInfoOf = getIntent().getExtras().getParcelable("bookToShow");
        myBook = new Book(bookToShowInfoOf);
        sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        fillInfoBookViews();
    }

    private void fillInfoBookViews() {
        TextView tv;
        tv = findViewById(R.id.info_book_title);
        tv.setText(bookToShowInfoOf.getTitle());
        tv = findViewById(R.id.info_book_author);
        String tmp;
        tmp = bookToShowInfoOf.getAuthors().toString();
        if (tmp.length() > 2) {
            tv.setText(tmp.substring(1, tmp.length() - 1));
        }
        tv = findViewById(R.id.info_book_ISBN);
        tv.setText(bookToShowInfoOf.getISBN());
        tv = findViewById(R.id.info_book_editionYear);
        if (bookToShowInfoOf.getEditionYear() == 0) {
            tv.setText(R.string.add_book_info_not_available);
        } else {
            tv.setText(Integer.toString(bookToShowInfoOf.getEditionYear()));
        }
        tv = findViewById(R.id.info_book_pages);
        if (bookToShowInfoOf.getPages() == 0) {
            tv.setText(R.string.add_book_info_not_available);
        } else {
            tv.setText(Integer.toString(bookToShowInfoOf.getPages()));

        }
        tv = findViewById(R.id.info_book_description);
        if (bookToShowInfoOf.getDescription().length() == 0) {
            tv.setText(getString(R.string.add_book_no_description));
        } else {
            tv.setText(bookToShowInfoOf.getDescription());
        }
        tv = findViewById(R.id.info_book_publisher);
        if (bookToShowInfoOf.getPublisher().length() == 0) {
            tv.setText(getString(R.string.add_book_info_not_available));
        } else {
            tv.setText(bookToShowInfoOf.getPublisher());
        }
        tv = findViewById(R.id.info_book_categories);
        tmp = bookToShowInfoOf.getCategories().toString();
        if (tmp.length() > 2) {
            tv.setText(tmp.substring(1, tmp.length() - 1));
        }
        ImageView iw = findViewById(R.id.info_book_main_image);
        Book book = new Book(bookToShowInfoOf);
        tv = findViewById(R.id.info_book_book_conditions);
        switch (book.getBook_condition()) {
            case 0:
                tv.setText(getString(R.string.add_book_info_not_available));
                break;
            case 1:
                tv.setText(getResources().getStringArray(R.array.book_conditions)[1]);
                break;
            case 2:
                tv.setText(getResources().getStringArray(R.array.book_conditions)[2]);
                break;
            case 3:
                tv.setText(getResources().getStringArray(R.array.book_conditions)[3]);
                break;
        }
        GlideApp.with(this).load(book.getBook_thumbnail_url())
                .placeholder(R.drawable.ic_thumbnail_cover_book).into(iw);
        horizontal_photo_list = (LinearLayout) findViewById(R.id.info_book_list_photo_container);
        for_me = new LinkedList<>(book.getBook_photo_list().keySet());
        for (int i = 0; i < book.getBook_photo_list().size(); i++) {
            horizontal_photo_list_element = getLayoutInflater().inflate(R.layout.cell_in_image_list, null);
            ImageView imageView = (ImageView) horizontal_photo_list_element.findViewById(R.id.image_in_horizontal_list_cell);
            StorageReference mImageRef = FirebaseStorage.getInstance().getReference(for_me.get(i));
            GlideApp.with(context).asDrawable().load(mImageRef).into(new SimpleTarget<Drawable>(SimpleTarget.SIZE_ORIGINAL, SimpleTarget.SIZE_ORIGINAL) {
                private ImageView iwPopUp;
                @Override
                public void onStart() {
                    super.onStart();
                    iwPopUp = imageView;
                }
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    ImageViewPopUpHelper.enablePopUpOnClick(myActivity,iwPopUp,resource);
                }
            });
            GlideApp.with(context).load(mImageRef).placeholder(R.drawable.ic_thumbnail_cover_book).into(imageView);
            horizontal_photo_list.addView(horizontal_photo_list_element);
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference user_doc;
        user_doc = db.collection("users").document(bookToShowInfoOf.getUser_id());
        user_doc.get().addOnCompleteListener(task -> {
            DocumentSnapshot doc = task.getResult();
            //thisUser = doc.toObject(User.class);
            UserWrapper bookOwnerWrapped;
            if(doc==null){
                return;
            }
            bookOwner = doc.toObject(User.class);
            TextView tv2 = findViewById(R.id.info_book_owner);
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            final Button request_button = findViewById(R.id.info_book_request_book_button);
            if(!bookToShowInfoOf.getUser_id().equals(user.getUid())) {
                request_button.setClickable(true);
                request_button.setVisibility(View.VISIBLE);
                request_button.setOnClickListener( (View view) -> checkAlreadyRequested());
                tv2.setText(bookOwner.getUsr_name());
                tv2.setTextColor(Color.BLUE);
                tv2.setClickable(true);
                tv2.setOnClickListener(view -> {
                    Intent toInfoUser = new Intent(InfoBook.this, InfoUser.class);
                    toInfoUser.putExtra("otherUser", new UserWrapper(bookOwner));
                    startActivity(toInfoUser);
                });
            }
            else{
                tv2.setVisibility(View.INVISIBLE);
                tv2.setHeight(0);
                tv2 = findViewById(R.id.info_book_label_owner);
                tv2.setHeight(0);
                tv2.setVisibility(View.INVISIBLE);
                request_button.setVisibility(View.GONE);
            }
        });
    }

    private AlertDialog AskOption()
    {
        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(this)
                //set message, title, and icon
                .setTitle(R.string.info_book_delete_this_book)
                .setMessage(R.string.info_book_delete_want_delete)
                .setIcon(R.drawable.ic_delete_white_24px)
                .setPositiveButton(R.string.info_book_delete_this_book, (dialog, whichButton) -> {
                    //your deleting code
                    DeleteBook(myBook);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
        return myQuittingDialogBox;
    }

    private void DeleteBook(Book bookToDelete) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.document(bookToDelete.getBook_id()).delete().addOnSuccessListener(aVoid -> {
                Log.d("delete","deleted from books");
                DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(bookToDelete.getBook_userid());
                docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User owner = documentSnapshot.toObject(User.class);
                        owner.getUsr_books().remove(bookToDelete.getBook_id());
                        docRef.update("usr_books",owner.getUsr_books(), SetOptions.merge());
                    }
                });
                List<String> x = new LinkedList<String>(bookToDelete.getBook_photo_list().keySet());
                if(bookToDelete.getBook_photo_list().keySet().size() == 0) {
                    Intent bookRemovedintent = new Intent();
                    bookRemovedintent.putExtra("book_removed", new BookWrapper(bookToDelete));
                    setResult(BOOK_EFFECTIVELY_REMOVED,bookRemovedintent);
                    finish();
                }
                for (int i = 0; i < bookToDelete.getBook_photo_list().size(); i++) {
                    StorageReference mImageRef = FirebaseStorage.getInstance().getReference(x.get(i));
                    mImageRef.delete().addOnSuccessListener(aVoid1 -> {
                        // File deleted successfully
                        Log.d("delete", "deleted photo");
                        Intent bookRemovedintent = new Intent();
                        bookRemovedintent.putExtra("book_removed", new BookWrapper(bookToDelete));
                        setResult(BOOK_EFFECTIVELY_REMOVED,bookRemovedintent);
                        finish();
                    });
                }
            });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        TextView tv;
        for (int i : idTextViewIds) {
            tv = findViewById(i);
            outState.putString(Integer.toString(i), tv.getText().toString());
        }
//        /**
//         * da mettere l'immagine thumbnail + le immagini inserite dal proprietario
//         */
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        TextView tv;
        for (int i : idTextViewIds) {
            tv = findViewById(i);
            tv.setText(savedInstanceState.getString(Integer.toString(i), ""));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(bookToShowInfoOf.getUser_id().equals(user.getUid())) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.info_book_menu, menu); //.xml file name
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                return true;
            case R.id.info_book_delete_book:
                //TODO: if isLent == false
                AskOption();
                return true;
            case R.id.info_book_modify_book:
                //TODO: fase di modifica e update del libro
                Toast.makeText(this, "Features in progress, soon available", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkAlreadyRequested(){
        FirebaseFirestore.getInstance().collection("requests")
                .whereEqualTo("bookId",bookToShowInfoOf.getId())
                .whereEqualTo("applicantId",FirebaseAuth.getInstance().getUid())
                //.whereEqualTo("ownerId",bookOwner.getUsr_id())
                .whereEqualTo("endLoanApplicant",null)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().getDocuments().size()>0){
                        Snackbar.make(findViewById(R.id.info_book_owner),
                                R.string.info_book_request_already_done, Snackbar.LENGTH_LONG).show();
                        return;
                    }
                    else{
                        Intent toRequestBook = new Intent(InfoBook.this, RequestBook.class);
                        toRequestBook.putExtra("requested_book", bookToShowInfoOf);
                        toRequestBook.putExtra("otherUser",new UserWrapper(bookOwner));
                        startActivityForResult(toRequestBook,REQUEST_BOOK);
                    }
                }
                else{
                    Snackbar.make(findViewById(R.id.request_book_send),
                            R.string.connecting, Snackbar.LENGTH_LONG).show();
                    checkAlreadyRequested();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_BOOK:
                if(resultCode == RESULT_OK){
                    Snackbar.make(findViewById(R.id.info_book_ISBN),
                            R.string.request_book_sent, Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }
}
