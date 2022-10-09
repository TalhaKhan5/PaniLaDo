package com.bscs16045.panilado;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    DataSnapshot userData;
    FirebaseUser currentUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userReference;
    DatabaseReference ordersRef;
    HashMap<String,String> order_map;

    LocationManager lm;
    double lat;
    double lng;
    float distanceThresholdMeters = 3000;

    Button pickOrderBtn;
    String selectedOrder;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //region MyCode

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        if(currentUser != null)
        {
            userReference = firebaseDatabase.getReference().child("Users").child(currentUser.getUid());
            ordersRef = firebaseDatabase.getReference().child("All_Orders");
            Log.d("***","***DatabaseReference"+ userReference.getKey());
        }

        int p= ContextCompat.checkSelfPermission(
                MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);


        if (p!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MapsActivity.this,
                    new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    1
            );
        }
        else {
            lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            assert lm != null;
            Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Log.d("***", "***Location:" + loc);
            if (loc != null) {
                lat = loc.getLatitude();
                lng = loc.getLongitude();
//                userReference.child("Lat").setValue(lat);
//                userReference.child("Lng").setValue(lng);
            }

            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    3000,
                    1,
                    new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            lat = location.getLatitude();
                            lng = location.getLongitude();

//                            userReference.child("Lat").setValue(lat);
//                            userReference.child("Lng").setValue(lng);
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {

                        }

                        @Override
                        public void onProviderEnabled(String provider) {

                        }

                        @Override
                        public void onProviderDisabled(String provider) {

                        }
                    });
            }
        }

        //endregion


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        pickOrderBtn = findViewById(R.id.pick_btn);
        pickOrderBtn.setVisibility(View.GONE);

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userData = dataSnapshot;
                lat = Double.parseDouble(userData.child("Lat").getValue().toString());
                lng = Double.parseDouble(userData.child("Lng").getValue().toString());
                final LatLng myLoc = new LatLng(lat, lng);

                ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot uniqueOrderSnapshot : dataSnapshot.getChildren()) {
                            Log.d("---","---"+uniqueOrderSnapshot.getKey());
                            double sLat, sLng;
                            sLat = Double.parseDouble(uniqueOrderSnapshot.child("cLat").getValue().toString());
                            sLng = Double.parseDouble(uniqueOrderSnapshot.child("cLng").getValue().toString());
                            Location loc1 = new Location("");
                            loc1.setLatitude(sLat);
                            loc1.setLongitude(sLng);
                            Location loc2 = new Location("");
                            loc2.setLatitude(lat);
                            loc2.setLongitude(lng);

                            if(loc1.distanceTo(loc2) < distanceThresholdMeters && uniqueOrderSnapshot.child("Active_Status").getValue().toString().equals("on"))
                            {
                                String title = uniqueOrderSnapshot.child("Consumer Name").getValue().toString();
                                String snippet = "Qty: "+uniqueOrderSnapshot.child("Quantity").getValue().toString();
                                snippet = snippet + " - Size: "+uniqueOrderSnapshot.child("Size").getValue().toString();
                                mMap.addMarker(new MarkerOptions().position(new LatLng(sLat,sLng)).title(title).snippet(snippet)).setTag(uniqueOrderSnapshot.getKey());
                                Log.d("***","***MARKER TAG:"+uniqueOrderSnapshot.getKey());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                mMap.addMarker(new MarkerOptions().position(myLoc).title("That's Me")).setTag(0);
                mMap.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(myLoc, 15));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if(marker.getTag() instanceof String)
                {
                    pickOrderBtn.setVisibility(View.VISIBLE);
                    selectedOrder = marker.getTag().toString();

                    Log.d("***","***Selected Marker Tag: "+marker.getTag());
                }
                else
                {
                    pickOrderBtn.setVisibility(View.GONE);
                    selectedOrder = "";

                }

                Log.d("***","***Marker CLicked");

                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                pickOrderBtn.setVisibility(View.GONE);
            }
        });

        pickOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                long LastOrder = userData.child("Orders").getChildrenCount();

                ordersRef.child(selectedOrder).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        double destLat = Double.parseDouble(dataSnapshot.child("cLat").getValue().toString());
                        double destLng = Double.parseDouble(dataSnapshot.child("cLng").getValue().toString());

                        //userReference.child("Orders").child(""+LastOrder).child("Active_Status").setValue("ongoing");
                        ordersRef.child(selectedOrder).child("supplier").setValue(currentUser.getEmail());
                        ordersRef.child(selectedOrder).child("Active_Status").setValue("ongoing");
                        Toast.makeText(MapsActivity.this, "Order Started", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="+destLat+","+destLng));
                        intent.setPackage("com.google.android.apps.maps");
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

    }
}
