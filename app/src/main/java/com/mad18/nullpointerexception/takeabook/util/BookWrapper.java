package com.mad18.nullpointerexception.takeabook.util;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.LinkedList;
import java.util.List;

/**
 * La classe BookWrapper si occupa di creare un oggetto in cui possano essere memorizzati i dati
 * relativi ad un libro. La classe implementa l'interfaccia Parcelable in quanto è necessario creare
 * un oggetto di questo tipo affinchè possa essere inserito nel bundle.
 *
 */
public class BookWrapper implements Parcelable {

    /**
     * Si occupa di creare una nuova istanza della classe Parcelable, instanziandola dal Parcel specificato.
     * Il parcel deve essere precedentemente scritto con il metodo WriteToParcel
     */
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
    private String thumbnail;
    private List<String> categories;
    private String user_id="";
    private double lat, longitude;
    private int pages=0;
    private int condition = 0;
    private String id="";
    private boolean status;
    private String description;


    //Constructor
    public BookWrapper(String ISBN,String title,List<String> authors, String publisher, int editionYear, String thumbnail,
                       List<String> categories, String description, String user, double lat, double longitude, int pages, boolean status){
        this.ISBN = ISBN;
        this.title = title;
        this.authors = authors;
        this.publisher = publisher;
        this.editionYear = editionYear;
        this.thumbnail = thumbnail;
        this.categories = categories;
        this.description = description;
        this.user_id = user;
        this.lat = lat;
        this.longitude = longitude;
        photo_list = new LinkedList<>();
        this.pages=pages;
        this.status = status;
        this.id="";
    }

    public BookWrapper(Book book){
        this.ISBN = book.getBook_ISBN();
        this.title = book.getBook_title();
        this.authors = new LinkedList<>(book.getBook_authors().keySet());
        this.publisher = book.getBook_publisher();
        this.editionYear = book.getBook_editionYear();
        this.thumbnail = book.getBook_thumbnail_url();
        this.categories = new LinkedList<>(book.getBook_categories().keySet());
        this.description = book.getBook_description();
        this.user_id = book.getBook_userid();
        this.lat = book.getBook_location().getLatitude();
        this.longitude = book.getBook_location().getLongitude();
        this.photo_list = new LinkedList<>(book.getBook_photo_list().keySet());
        this.pages= book.getBook_pages();
        this.condition = book.getBook_condition();
        this.id=book.getBook_id();
        this.status = book.getBook_status();
    }

    public BookWrapper(Parcel in){
        this.ISBN = in.readString();
        this.title = in.readString();
        this.authors = in.createStringArrayList();
        this.publisher = in.readString();
        this.editionYear = in.readInt();
        this.thumbnail = in.readString();
        this.categories = in.createStringArrayList();
        this.description = in.readString();
        this.user_id = in.readString();
        this.lat = in.readDouble();
        this.longitude = in.readDouble();
        this.photo_list = in.createStringArrayList();
        this.pages = in.readInt();
        this.condition = in.readInt();
        this.id = in.readString();
        this.status = in.readByte() != 0;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Questo metodo si occupa di scrivere nel Parcel di destinazione i dati richiesti.
     * @param dest
     * @param flags
     */

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ISBN);
        dest.writeString(this.title);
        //dest.writeMap(this.authors);
        dest.writeStringList(this.authors);
        dest.writeString(this.publisher);
        dest.writeInt(this.editionYear);
        dest.writeString(this.thumbnail);
        dest.writeStringList(this.categories);
        dest.writeString(this.description);
        dest.writeString(this.user_id);
        dest.writeDouble(this.lat);
        dest.writeDouble(this.longitude);
        dest.writeStringList(this.photo_list);
        dest.writeInt(this.pages);
        dest.writeInt(this.condition);
        dest.writeString(this.id);
        dest.writeByte((byte) (this.status ? 1:0));
    }
    @Override
    public String toString() {
        String Totauthors= ", authors='";
//        for (String key : authors.keySet()) {
//            Totauthors = Totauthors + key + '\'';
//        }
        Totauthors = Totauthors +this.authors.toString()+'\'';
        String allphotolist=", photolist='";
        allphotolist = allphotolist +this.photo_list.toString()+'\'';

        String SeditionYear = Integer.toString(editionYear);
        String Spages = Integer.toString(this.pages);

        return "BookWrapper{"+"ISBN='"+ISBN+'\''+
                ", title='" + title + '\'' +
                Totauthors +
                ", publisher='" + publisher + '\'' +
                ", editionYear='" + SeditionYear + '\'' +
                ", description='" + description + '\'' +
                ", user_id='" + user_id + '\'' +
                ", lat='" + lat + '\'' +
                ", longitude='" + longitude + '\'' +
                allphotolist+
                ", pages='" + Spages + '\'' +
                ", condition='" + condition + '\'' +
                ", id='" + this.id + '\'' +
                ", status='" + this.status + '\'' +
                '}';
    }

    public int getCondition() { return condition; }

    public void setCondition(int condition) { this.condition = condition; }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
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

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getUser_id() {
        return user_id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getPhoto_list() {
        return photo_list;
    }

    public void setPhoto_list(List<String> photo_list) {
        this.photo_list = photo_list;
    }

    private List<String> photo_list;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}