package com.mad18.nullpointerexception.takeabook.addBook;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class BookWrapper implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public BookWrapper createFromParcel(Parcel in) {
            return new BookWrapper(in);
        }

        public BookWrapper[] newArray(int size) {
            return new BookWrapper[size];
        }
    };
    private String ISBN;
    private String title;
   // private Map<String,Boolean> authors;
    private List<String> authors;
    private String publisher;
    private int editionYear;

    //Constructor
    public BookWrapper(String ISBN,String title,List<String> authors, String publisher, int editionYear){
        this.ISBN = ISBN;
        this.title = title;
        this.authors = authors;
        this.publisher = publisher;
        this.editionYear = editionYear;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

//    public Map<String, Boolean> getAuthors() {
//        return authors;
//    }

//    public void setAuthors(Map<String, Boolean> authors) {
//        this.authors = authors;
//    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getEditionYear() {
        return editionYear;
    }

    public void setEditionYear(int editionYear) {
        this.editionYear = editionYear;
    }
    public BookWrapper(Parcel in){
        this.ISBN = in.readString();
        this.title = in.readString();
        //this.authors = (HashMap<String,Boolean>)in.readSerializable();
        this.authors = in.createStringArrayList();
        this.publisher = in.readString();
        this.editionYear = in.readInt();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ISBN);
        dest.writeString(this.title);
        //dest.writeMap(this.authors);
        dest.writeStringList(this.authors);
        dest.writeString(this.publisher);
        dest.writeInt(this.editionYear);
    }
    @Override
    public String toString() {
        String Totauthors= ", authors='";
//        for (String key : authors.keySet()) {
//            Totauthors = Totauthors + key + '\'';
//        }
        Totauthors = Totauthors +this.authors.toString()+'\'';

        String SeditionYear = Integer.toString(editionYear);

        return "BookWrapper{"+"ISBN='"+ISBN+'\''+
                ", title='" + title + '\'' +
                Totauthors +
                ", publisher='" + publisher + '\'' +
                ", editionYear='" + SeditionYear + '\'' +
                '}';
    }
}