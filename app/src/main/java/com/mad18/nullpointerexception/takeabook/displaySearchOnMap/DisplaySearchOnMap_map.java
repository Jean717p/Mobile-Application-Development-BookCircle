package com.mad18.nullpointerexception.takeabook.displaySearchOnMap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mad18.nullpointerexception.takeabook.R;
import com.mad18.nullpointerexception.takeabook.info.InfoBook;
import com.mad18.nullpointerexception.takeabook.util.Book;
import com.mad18.nullpointerexception.takeabook.util.BookWrapper;

import java.util.ArrayList;
import java.util.List;

public class DisplaySearchOnMap_map extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        SlidingUpPanelLayout.PanelSlideListener, LocationListener, HeaderAdapter.ItemClickListener, GoogleMap.OnMarkerClickListener {

    private static final String ARG_LOCATION = "arg.location";

    // private LockableListView mListView;
    private DisplaySearchOnMap_booklistRV mListView;
    private SlidingUpPanelLayout mSlidingUpPanelLayout;
    private ArrayList<BookWrapper> bookList;
    // ListView stuff
    //private View mTransparentHeaderView;
    //private View mSpaceView;
    private View mTransparentView;
    private View mWhiteSpaceView;

    private HeaderAdapter mHeaderAdapter;

    public static LatLng mLocation;
    private Marker mLocationMarker;

    private SupportMapFragment mMapFragment;

    private GoogleMap bookMap;
    private boolean mIsNeedLocationUpdate = true;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LatLng MELBOURNE;

    public DisplaySearchOnMap_map() {
    }

    public static DisplaySearchOnMap_map newInstance(LatLng location) {
        DisplaySearchOnMap_map f = new DisplaySearchOnMap_map();
        Bundle args = new Bundle();
        args.putParcelable(ARG_LOCATION, location);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_display_search_on_map, container, false);

        mListView = rootView.findViewById(android.R.id.list);
        mListView.setOverScrollMode(ListView.OVER_SCROLL_NEVER);

        mSlidingUpPanelLayout = (SlidingUpPanelLayout) rootView.findViewById(R.id.display_search_on_map_slidingLayout);
        mSlidingUpPanelLayout.setEnableDragViewTouchEvents(true);

        int mapHeight = getResources().getDimensionPixelSize(R.dimen.map_height);
        mSlidingUpPanelLayout.setPanelHeight(mapHeight); // you can use different height here
        mSlidingUpPanelLayout.setScrollableView(mListView, mapHeight);

        mSlidingUpPanelLayout.setPanelSlideListener(this);

        // transparent view at the top of ListView
        mTransparentView = rootView.findViewById(R.id.display_search_on_map_transparentView);
        //mWhiteSpaceView = rootView.findViewById(R.id.display_search_on_map_whiteSpaceView);

        // init header view for ListView
        // mTransparentHeaderView = inflater.inflate(R.layout.transparent_header_view, mListView, false);
        // mSpaceView = mTransparentHeaderView.findViewById(R.id.space);

        //collapseMap();

        mSlidingUpPanelLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mSlidingUpPanelLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                mSlidingUpPanelLayout.onPanelDragged(0);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mLocation = getArguments().getParcelable(ARG_LOCATION);
        if (mLocation == null) {
            mLocation = getLastKnownLocation(false);
        }

        mMapFragment = SupportMapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.display_search_on_map_mapContainer, mMapFragment, "map");
        fragmentTransaction.commit();

        // show white bg if there are not too many items
        // mWhiteSpaceView.setVisibility(View.VISIBLE);

        // ListView approach
        /*mListView.addHeaderView(mTransparentHeaderView);
        mListView.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.simple_list_item, testData));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSlidingUpPanelLayout.collapsePane();
            }
        });*/
        ArrayList<Book> mBooks = new ArrayList<>();
        Bundle bundle = getActivity().getIntent().getExtras();
        if(bundle!=null){
            List<BookWrapper> bookWrappers = bundle.getParcelableArrayList("bookToShow");
            for(BookWrapper bw : bookWrappers){
                mBooks.add(new Book(bw));
            }
        }
        mHeaderAdapter = new HeaderAdapter(getActivity(), mBooks, this);
        mListView.setItemAnimator(null);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mListView.setLayoutManager(layoutManager);
        mListView.setAdapter(mHeaderAdapter);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        setUpMapIfNeeded();
        expandMap();
        mSlidingUpPanelLayout.collapsePane();
    }

    private void setUpMapIfNeeded() {

        // Do a null check to confirm that we have not already instantiated the map.
        if (bookMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap mMap) {
                    bookMap = mMap;
                    /*LatLng b1,b2;
                    b1 = new LatLng(85,-180);
                    b2 = new LatLng(-85,180);
                    LatLngBounds bounds = new LatLngBounds(b2,b1);
                    bookMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,0));
                    */
                    // For showing a move to my location button
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    bookMap.setMyLocationEnabled(true);

                    // For dropping a marker at a point on the Map

                    bookList = new ArrayList<>();
                    Bundle bundle = getActivity().getIntent().getExtras();
                    bookList = bundle.getParcelableArrayList("bookToShow");

                    for (BookWrapper i: bookList){
                        Marker marker = bookMap.addMarker(new MarkerOptions()
                                .position(new LatLng(i.getLat(),i.getLongitude()))
                                .title(i.getTitle())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        );
                        marker.setTag(i);
                    }


                    bookMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            BookWrapper bw = (BookWrapper) marker.getTag();
                            Intent intent = new Intent(getActivity(), InfoBook.class);
                            intent.putExtra("bookToShow",bw);
                            startActivity(intent);
                            return false;
                        }
                        });

                    // For zooming automatically to the location of the marker
                    //CameraPosition cameraPosition = new CameraPosition.Builder().target().zoom(12).build();
                    //bookMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    return;
                }
            });

            // Check if we were successful in obtaining the map.
            if (bookMap != null) {
                bookMap.setMyLocationEnabled(false);
                bookMap.getUiSettings().setCompassEnabled(false);
                bookMap.getUiSettings().setZoomControlsEnabled(false);
                bookMap.getUiSettings().setMyLocationButtonEnabled(false);
                LatLng update = getLastKnownLocation();
                if (update != null) {
                    bookMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(update, 11.0f)));
                }
                bookMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        mIsNeedLocationUpdate = false;
                        moveToLocation(latLng, false);
                    }
                });
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // In case Google Play services has since become available.
        setUpMapIfNeeded();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Connect the client.
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public LatLng getLastKnownLocation() {
        return getLastKnownLocation(true);
    }



    public LatLng getLastKnownLocation(boolean isMoveMarker) {
        LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_LOW);
        String provider = lm.getBestProvider(criteria, true);
        if (provider == null) {
            return null;
        }
        Activity activity = getActivity();
        if (activity == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
        }
        Location loc = lm.getLastKnownLocation(provider);
        if (loc != null) {
            LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
            if (isMoveMarker) {
                moveMarker(latLng);
            }
            if(mHeaderAdapter!=null){
                mHeaderAdapter.notifyDataSetChanged();
            }
            return latLng;
        }
        return null;
    }


    private void moveMarker(LatLng latLng) {
        if (mLocationMarker != null) {
            mLocationMarker.remove();
        }
        //mLocationMarker = bookMap.addMarker(new MarkerOptions()
                //.position(latLng).anchor(0.5f, 0.5f));

    }

    private void moveToLocation(Location location) {
        if (location == null) {
            return;
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        moveToLocation(latLng);
    }

    private void moveToLocation(LatLng latLng) {
        moveToLocation(latLng, true);
    }

    private void moveToLocation(LatLng latLng, final boolean moveCamera) {
        if (latLng == null) {
            return;
        }
        moveMarker(latLng);
        mLocation = latLng;
        mListView.post(new Runnable() {
            @Override
            public void run() {
                if (bookMap != null && moveCamera) {
                    bookMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(mLocation, 11.0f)));
                }
            }
        });
    }

    private void collapseMap() {
        if (mHeaderAdapter != null) {
            //mHeaderAdapter.showSpace();
        }
        //mTransparentView.setVisibility(View.GONE);
        if (bookMap != null && mLocation != null) {
            bookMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLocation, 11f), 1000, null);
        }
        mListView.setScrollingEnabled(true);
    }

    private void expandMap() {
        if (mHeaderAdapter != null) {
            mHeaderAdapter.hideSpace();
        }
        mTransparentView.setVisibility(View.INVISIBLE);
        if (bookMap != null) {
            //bookMap.animateCamera(CameraUpdateFactory.zoomTo(14f), 1000, null);
        }
        mListView.setScrollingEnabled(false);
    }

    @Override
    public void onPanelSlide(View view, float v) {
    }

    @Override
    public void onPanelCollapsed(View view) {
        expandMap();
    }

    @Override
    public void onPanelExpanded(View view) {
        collapseMap();
    }

    @Override
    public void onPanelAnchored(View view) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (mIsNeedLocationUpdate) {
            moveToLocation(location);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setNumUpdates(1);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onItemClicked(LatLng position) {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(position).zoom(12).build();
        bookMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mSlidingUpPanelLayout.collapsePane();
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        if(getActivity().getIntent().getBooleanExtra("isFromInfoBook",false)){
            getActivity().finish();
            getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        }
        BookWrapper bw = (BookWrapper) marker.getTag();
        Intent intent = new Intent(getActivity(), InfoBook.class);
        intent.putExtra("bookToShow",bw);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        return false;
    }


}
