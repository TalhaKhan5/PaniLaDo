package com.bscs16045.panilado;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class OrderProgress extends AppCompatActivity {

    FirebaseUser currentUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userReference;
    DatabaseReference ordersRef;
    DatabaseReference supplierReference;

    //    HashMap order_map;
    long orderNumber;

    Button complete_btn, cancel_btn, cancel_btn2;
    EditText sEmail;
    TextView waitText;
    ViewGroup buttonLayout;

    String supplierID;
    long count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_progress);
        firebaseDatabase = FirebaseDatabase.getInstance();

        complete_btn = findViewById(R.id.complete_btn);
        cancel_btn = findViewById(R.id.cancel_btn);
        cancel_btn2 = findViewById(R.id.cancel_btn2);
        sEmail = findViewById(R.id.sEmail);
        waitText = findViewById(R.id.waitingText);
        buttonLayout = findViewById(R.id.buttonsLayout);

        waitText.setVisibility(View.VISIBLE);
        cancel_btn2.setVisibility(View.VISIBLE);
        buttonLayout.setVisibility(View.GONE);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null)
            userReference = firebaseDatabase.getReference().child("Users").child(currentUser.getUid());
        else
        {
            Toast.makeText(this, "Session Expired", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(OrderProgress.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        ordersRef = firebaseDatabase.getReference().child("All_Orders");

        //order_map = (HashMap) getIntent().getExtras().get("OrderData");
        orderNumber = getIntent().getExtras().getLong("OrderNumber");

//        Log.d("***","***ReadingOrder"+order_map.get("cLat").toString());

        ordersRef.child(currentUser.getUid()+"+"+orderNumber).child("supplier").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null)
                {
                    if(!dataSnapshot.getValue().toString().isEmpty())
                    {
                        waitText.setVisibility(View.GONE);
                        cancel_btn2.setVisibility(View.GONE);
                        buttonLayout.setVisibility(View.VISIBLE);

                        userReference.child("Orders").child(orderNumber+"").child("Active_Status").setValue("ongoing");
                        userReference.child("Orders").child(orderNumber+"").child("supplier").setValue(dataSnapshot.getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //region ButtonsCode

        cancel_btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ordersRef.child(currentUser.getUid()+"+"+orderNumber).removeValue();
                Log.d("***","Order to be removed"+currentUser.getUid()+"+"+orderNumber);

                firebaseDatabase.getReference().child("Users").child(currentUser.getUid()).child("Orders").child(""+orderNumber).child("Active_Status").setValue("cancelled");

                cancel_btn2.setEnabled(false);
                cancel_btn2.setTextColor(Color.BLACK);
                Toast.makeText(OrderProgress.this, "Order Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ordersRef.child(currentUser.getUid()+"+"+orderNumber).removeValue();
                Log.d("***","Order to be removed"+currentUser.getUid()+"+"+orderNumber);

                firebaseDatabase.getReference().child("Users").child(currentUser.getUid()).child("Orders").child(""+orderNumber).child("Active_Status").setValue("cancelled");

                complete_btn.setEnabled(false);
                cancel_btn.setEnabled(false);
                complete_btn.setTextColor(Color.BLACK);
                cancel_btn.setTextColor(Color.BLACK);
                Toast.makeText(OrderProgress.this, "Order Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        complete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!sEmail.getText().toString().isEmpty() && Patterns.EMAIL_ADDRESS.matcher(sEmail.getText().toString()).matches())
                {
                    userReference = firebaseDatabase.getReference().child("Users");
                    userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot uniqueUserSnapshot : dataSnapshot.getChildren()) {
                                Log.d("---","-$-"+uniqueUserSnapshot.getKey());

                                if(uniqueUserSnapshot.child("Email").getValue().toString().equals(sEmail.getText().toString()))
                                {
                                    if(uniqueUserSnapshot.child("UserType").getValue().toString().equals("Supplier"))
                                    {

                                    count = uniqueUserSnapshot.child("Orders").getChildrenCount()+1;
                                    Log.d("***","Count: "+count);

                                    supplierID = uniqueUserSnapshot.getKey();
                                    Log.d("***","***SplierID: "+supplierID);
                                    supplierReference = firebaseDatabase.getReference().child("Users").child(supplierID);
                                    ordersRef = firebaseDatabase.getReference().child("All_Orders").child(currentUser.getUid()+"+"+orderNumber);
                                    ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Calendar c = Calendar.getInstance();
                                            String date = new SimpleDateFormat("dd-MMM-yyyy").format(c.getTime());
                                            supplierReference.child("Orders").child(""+count).child("Date").setValue(date);
                                            //supplierReference.child("Orders").child(""+count).child("Delivered Date").setValue(date);
                                            String time = new SimpleDateFormat("hh:mm:ss").format(c.getTime());
                                            supplierReference.child("Orders").child(""+count).child("Time").setValue(time);
                                            //supplierReference.child("Orders").child(""+count).child("Delivered Time").setValue(time);


                                            supplierReference.child("Orders").child(""+count).setValue(dataSnapshot.getValue());
                                            supplierReference.child("Orders").child(""+count).child("Active_Status").setValue("delivered");

                                            ordersRef.removeValue();
                                            firebaseDatabase.getReference().child("Users").child(currentUser.getUid()).child("Orders").child(""+orderNumber).child("Active_Status").setValue("completed");
                                            firebaseDatabase.getReference().child("Users").child(currentUser.getUid()).child("Orders").child(""+orderNumber).child("Date").setValue(date);
                                            firebaseDatabase.getReference().child("Users").child(currentUser.getUid()).child("Orders").child(""+orderNumber).child("Time").setValue(time);
                                            //firebaseDatabase.getReference().child("Users").child(currentUser.getUid()).child("Orders").child(""+orderNumber).child("Delivered Date").setValue(time);
                                            //firebaseDatabase.getReference().child("Users").child(currentUser.getUid()).child("Orders").child(""+orderNumber).child("Delivered Time").setValue(time);

                                            complete_btn.setEnabled(false);
                                            cancel_btn.setEnabled(false);
                                            complete_btn.setTextColor(Color.BLACK);
                                            cancel_btn.setTextColor(Color.BLACK);
                                            Toast.makeText(OrderProgress.this, "Order Completed", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                    //child(""+count).child("Active_Status").setValue("delivered");


                                    }
                                    else
                                    {
                                        Toast.makeText(OrderProgress.this, "Not a supplier", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(OrderProgress.this, "Database Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else
                {
                    Toast.makeText(OrderProgress.this, "Enter Valid Email", Toast.LENGTH_SHORT).show();
                }

            }
        });
        //endregion
    }

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
                AlertDialog.Builder builder = new AlertDialog.Builder(OrderProgress.this);
                builder.setTitle("Are You Sure?");
                builder.setMessage("Do you want to sign out?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //sign out logic here
                        Log.d("***","***SIGNING OUT");

                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(OrderProgress.this, MainActivity.class);
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
}
