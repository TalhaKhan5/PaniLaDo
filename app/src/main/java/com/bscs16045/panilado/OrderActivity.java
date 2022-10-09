package com.bscs16045.panilado;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class OrderActivity extends AppCompatActivity {

    FirebaseUser currentUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userReference;
    DatabaseReference ordersRef;
    HashMap<String,String> order_map;
    long orderNumber;

    DataSnapshot userData;
    String usertype;

    Button order;
    EditText quantityView;
    TextView rateView;
    Spinner spinner;
    List<String> bottleSizeList;
    ArrayAdapter spnAdapter;
    int quantity;
    int rate;
    int price;
    String size;

    LocationManager lm;
    double lat;
    double lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //region Initialization

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        if(currentUser != null)
        {
            userReference = firebaseDatabase.getReference().child("Users").child(currentUser.getUid());
            Log.d("***","***DatabaseReference"+ userReference.getKey());
        }

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userData = dataSnapshot;
                Log.d("***","***DataSnapshot"+userData);
                usertype = userData.child("UserType").getValue().toString();

                if(!usertype.isEmpty())
                {
                    if(usertype.equals("Consumer"))
                    {
                        setContentView(R.layout.activity_order_send);

                        rateView = findViewById(R.id.rateText);
                        quantityView = findViewById(R.id.qtyText);
                        order = findViewById(R.id.order_btn);

                        spinner = findViewById(R.id.spinner);
                        bottleSizeList = new ArrayList<>();
                        bottleSizeList.add("Size");
                        bottleSizeList.add("500 ml");
                        bottleSizeList.add("1.5 litres");
                        bottleSizeList.add("19 litres");

                        spnAdapter = new ArrayAdapter(OrderActivity.this, android.R.layout.simple_spinner_item, bottleSizeList);
                        spnAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(spnAdapter);
                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                                switch (position)
                                {
                                    case 0:
                                        rate = 0;
                                        size = "0";
                                        break;
                                    case 1:
                                        rate = 40;
                                        size = "500 ml";
                                        break;
                                    case 2:
                                        rate = 70;
                                        size = "1.5 litres";
                                        break;
                                    case 3:
                                        rate = 100;
                                        size = "19 litres";
                                        break;
                                }

                                CalculationAndSet();
                                //Toast.makeText(OrderActivity.this, "Item Selected", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });

                        quantityView.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                CalculationAndSet();
                            }
                        });

                        order.setEnabled(true);
                        order.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                // Prepare Order to send

                                int p = ContextCompat.checkSelfPermission(
                                        OrderActivity.this,
                                        Manifest.permission.ACCESS_FINE_LOCATION);


                                if (p!= PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(
                                            OrderActivity.this,
                                            new String[]
                                                    {Manifest.permission.ACCESS_FINE_LOCATION},
                                            1
                                    );
                                }
                                else
                                {
                                    lm  = (LocationManager)getSystemService(LOCATION_SERVICE);
                                    assert lm != null;
                                    Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                    Log.d("***","***Location:"+loc);
                                    if(loc != null) {
                                        lat = loc.getLatitude();
                                        lng = loc.getLongitude();
                                        userReference.child("Lat").setValue(lat);
                                        userReference.child("Lng").setValue(lng);
                                    }

                                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                            3000,
                                            1,
                                            new LocationListener() {
                                                @Override
                                                public void onLocationChanged(Location location) {
                                                    lat = location.getLatitude();
                                                    lng = location.getLongitude();

                                                    userReference.child("Lat").setValue(lat);
                                                    userReference.child("Lng").setValue(lng);
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

                                    if(rate == 0)
                                    {
                                        Toast.makeText(OrderActivity.this, "Select Size", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        SendOrder(quantity, rate, price);
                                    }
                                }
                            }
                        });
                    }
                    else
                    {
                        setContentView(R.layout.activity_order_receive);
                        String name = userData.child("Name").getValue().toString();
                        TextView nameView = findViewById(R.id.textView2);
                        nameView.setText("Hi, "+name);
                        Button search_btn = findViewById(R.id.search_btn);
                        search_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(OrderActivity.this, MapsActivity.class);
                                startActivity(intent);
                            }
                        });
                    }
                }
                else
                    Log.d("---","--- ERROR setting layout");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //endregion

    }

    protected void SendOrder(final int qty, final int rate, final int price)
    {
        orderNumber = userData.child("Orders").getChildrenCount()+1;

        Log.d("***","Sending Order: Total:"+orderNumber);

        order_map = new HashMap<>();

        if(lat == 0 || lng == 0)
        {
            int p = ContextCompat.checkSelfPermission(
                    OrderActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION);

            if(p == PackageManager.PERMISSION_GRANTED)
            {
                FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(OrderActivity.this);
                LocationRequest locationRequest = LocationRequest.create().setNumUpdates(3).setInterval(1000).setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                fusedLocationProviderClient.requestLocationUpdates(locationRequest,new LocationCallback(),getMainLooper());

                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if(task.isSuccessful())
                        {
                            if(task.getResult() != null)
                            {
                                lat = task.getResult().getLatitude();
                                lng = task.getResult().getLongitude();
                                Log.d("***","***LAT"+lat);
                                Log.d("***","***LNG"+lng);
                                order_map.put("cLat",""+lat);
                                order_map.put("cLng",""+lng);
                                order_map.put("Active_Status","on");
                                order_map.put("supplier","");

                                order_map.put("Order Number", ""+orderNumber);
                                order_map.put("Consumer Email",userData.child("Email").getValue().toString());
                                order_map.put("Consumer Name",userData.child("Name").getValue().toString());

                                order_map.put("Size",size);
                                order_map.put("Quantity",""+qty);
                                order_map.put("Rate",""+rate);
                                order_map.put("Amount",""+price);
                                Calendar c = Calendar.getInstance();
                                String date = new SimpleDateFormat("dd-MMM-yyyy").format(c.getTime());
                                order_map.put("Date",date);
                                String time = new SimpleDateFormat("hh:mm:ss").format(c.getTime());
                                order_map.put("Time",time);

                                userReference.child("Orders").child(""+orderNumber).setValue(order_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful())
                                        {
                                            ordersRef = firebaseDatabase.getReference().child("All_Orders").child(userData.getKey()+"+"+orderNumber);
                                            ordersRef.setValue(order_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        Toast.makeText(OrderActivity.this, "Order placed", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(OrderActivity.this, OrderProgress.class);
                                                        intent.putExtra("OrderData",order_map);
                                                        intent.putExtra("OrderNumber",orderNumber);
                                                        startActivity(intent);
                                                        order.setEnabled(false);
                                                        order.setTextColor(Color.BLACK);
                                                    }
                                                }
                                            });
                                        }

                                    }
                                });
                            }
                            else
                            {
                                Toast.makeText(OrderActivity.this, "Use different location provider", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                });
            }

        }
        else
        {
            Log.d("***","***LAT"+lat);
            Log.d("***","***LNG"+lng);
            order_map.put("cLat",""+lat);
            order_map.put("cLng",""+lng);
            order_map.put("Active_Status","on");
            order_map.put("supplier","");

            order_map.put("Order Number", ""+orderNumber);
            order_map.put("Consumer Email",userData.child("Email").getValue().toString());
            order_map.put("Consumer Name",userData.child("Name").getValue().toString());

            order_map.put("Size",size);
            order_map.put("Quantity",""+qty);
            order_map.put("Rate",""+rate);
            order_map.put("Amount",""+price);
            Calendar c = Calendar.getInstance();
            String date = new SimpleDateFormat("dd-MMM-yyyy").format(c.getTime());
            order_map.put("Date",date);
            String time = new SimpleDateFormat("hh:mm:ss").format(c.getTime());
            order_map.put("Time",time);

            userReference.child("Orders").child(""+orderNumber).setValue(order_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful())
                    {
                        ordersRef = firebaseDatabase.getReference().child("All_Orders").child(userData.getKey()+"+"+orderNumber);
                        ordersRef.setValue(order_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    Toast.makeText(OrderActivity.this, "Order placed", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(OrderActivity.this, OrderProgress.class);
                                    intent.putExtra("OrderData",order_map);
                                    intent.putExtra("OrderNumber",orderNumber);
                                    startActivity(intent);
                                    order.setEnabled(false);
                                    order.setTextColor(Color.BLACK);
                                }
                            }
                        });
                    }

                }
            });
        }


    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        if(FirebaseAuth.getInstance().getCurrentUser() == null)
//        {
//            firebaseDatabase = FirebaseDatabase.getInstance();
//            if(currentUser != null)
//            {
//                currentUser = FirebaseAuth.getInstance().getCurrentUser();
//                userReference = firebaseDatabase.getReference().child("Users").child(currentUser.getUid());
//                Log.d("***","***DatabaseReference"+ userReference.getKey());
//            }
//        }
//
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mf = getMenuInflater();
        mf.inflate(R.menu.consumer_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.signout_btn:
                // Show Alert for SignOut
                AlertDialog.Builder builder = new AlertDialog.Builder(OrderActivity.this);
                builder.setTitle("Are You Sure?");
                builder.setMessage("Do you want to sign out?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //sign out logic here
                        Log.d("***","***SIGNING OUT");

                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(OrderActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                builder.create().show();
                return true;
            case R.id.record_btn:
                // Go TO Record Activity
                Intent intent = new Intent(getApplicationContext(),RecordScreen.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void CalculationAndSet()
    {
        String s = quantityView.getText().toString();
        if(!s.isEmpty())
            quantity = Integer.parseInt(s);
        else
            quantity = 0;

        price = quantity * rate;
        rateView.setText("Rate: "+rate+"\nAmount: "+price);
    }

    @Override
    protected void onStart() {
        super.onStart();

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userData = dataSnapshot;

                if(userData.child("UserType").getValue().toString().equals("Consumer"))
                {
                    long LastOrder = userData.child("Orders").getChildrenCount();
                    if(LastOrder != 0)
                    {
                        String stat = userData.child("Orders").child(LastOrder+"").child("Active_Status").getValue().toString();
                        if(stat.equals("on") || stat.equals("ongoing"))
                        {
                            Intent intent = new Intent(OrderActivity.this, OrderProgress.class);
                            intent.putExtra("OrderNumber",LastOrder);
                            startActivity(intent);
                            //finish();
                        }

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
