package com.bscs16045.panilado;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    EditText email, password, name;
    ImageView logo;
    Button login_btn;
    RadioGroup radioGroup;
    ViewGroup root;
    TextView gotoLogin;

    String usertype;
    String email_string;
    String password_string;

    long DelayMain = 1200;
    long DelayStep = 125;

    private FirebaseAuth mAuth;
    private DatabaseReference database;
    FirebaseUser currentuser;
    HashMap<String,String> usermap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = findViewById(R.id.nameText);
        email = findViewById(R.id.emailText);
        password = findViewById(R.id.passwordText);
        logo = findViewById(R.id.imageView);
        login_btn = findViewById(R.id.loginButton);
        radioGroup = findViewById(R.id.radioGroup1);
        gotoLogin = findViewById(R.id.gotoLogin);
        root = findViewById(R.id.root);

        logo.setRotation(0);
        name.setVisibility(View.GONE);
        email.setVisibility(View.GONE);
        password.setVisibility(View.GONE);
        login_btn.setVisibility(View.GONE);
        radioGroup.setVisibility(View.GONE);
        gotoLogin.setAlpha(0);

        //region ANIMATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            root.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGE_APPEARING);
        }

        Handler h = new Handler(getMainLooper());
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                logo.animate().rotation(-20).setDuration(1000).start();
                name.setVisibility(View.VISIBLE);
            }
        },DelayMain );
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                email.setVisibility(View.VISIBLE);
            }
        },DelayMain + DelayStep);

        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                password.setVisibility(View.VISIBLE);
            }
        },DelayMain + DelayStep * 2);

        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                radioGroup.setVisibility(View.VISIBLE);
            }
        },DelayMain + DelayStep * 3);
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                login_btn.setVisibility(View.VISIBLE);

                gotoLogin.animate().alpha(1).setDuration(1000).setStartDelay(1000).start();
            }
        },DelayMain + DelayStep * 4);

        //endregion

        //region Login Logic

        gotoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginScreen.class);
                startActivity(intent);
            }
        });

        mAuth = FirebaseAuth.getInstance();

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                email_string = email.getText().toString().trim();
                password_string = password.getText().toString().trim();
                int selectedId = radioGroup.getCheckedRadioButtonId();

                int p = ContextCompat.checkSelfPermission(
                        MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

                if(name.getText().toString().isEmpty())
                {
                    Toast.makeText(MainActivity.this,
                            "Enter Name",
                            Toast.LENGTH_SHORT)
                            .show();

                    name.requestFocus();
                }
                else if(email_string.isEmpty())
                {
                    Toast.makeText(MainActivity.this,
                            "Enter Email",
                            Toast.LENGTH_SHORT)
                            .show();

                    email.requestFocus();
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(email_string).matches())
                {
                    Toast.makeText(MainActivity.this,
                            "Not Valid Email",
                            Toast.LENGTH_SHORT)
                            .show();

                    email.requestFocus();
                }
                else if(password_string.isEmpty())
                {
                    Toast.makeText(MainActivity.this,
                            "Enter Password",
                            Toast.LENGTH_SHORT)
                            .show();

                    password.requestFocus();
                }
                else if (selectedId == -1) {
                    Toast.makeText(MainActivity.this,
                            "No user type has been selected",
                            Toast.LENGTH_SHORT)
                            .show();
                }
                else if(p != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]
                                    {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                            1
                    );
                }
                else {

                    RadioButton radioButton
                            = radioGroup
                            .findViewById(selectedId);

                    usertype = radioButton.getText().toString();

                    LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

                    if(lm != null)
                    {
                        if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
                        {
//                            Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

                            LocationRequest locationRequest = LocationRequest.create().setNumUpdates(2).setInterval(2000);
                            fusedLocationProviderClient.requestLocationUpdates(locationRequest,new LocationCallback(),getMainLooper());

                            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                                @Override
                                public void onComplete(@NonNull Task<Location> task) {
                                    if(task.isSuccessful())
                                    {
                                        Location loc = task.getResult();

                                        if(loc != null)
                                        {
                                            //HashMap is used to add user informaton its an easy method

                                            usermap = new HashMap<>();
                                            usermap.put("Email",email_string);
                                            usermap.put("Name", name.getText().toString());
                                            usermap.put("UserType",usertype);
                                            usermap.put("Lat",String.valueOf(loc.getLatitude()));
                                            usermap.put("Lng",String.valueOf(loc.getLongitude()));

                                            mAuth.createUserWithEmailAndPassword(email_string, password_string).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        //new
                                                        Toast.makeText(MainActivity.this,
                                                                "Registered Successfully",
                                                                Toast.LENGTH_SHORT)
                                                                .show();

                                                        currentuser = FirebaseAuth.getInstance().getCurrentUser();
                                                        String userid = currentuser.getUid();
                                                        database = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);

                                                        database.setValue(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if(task.isSuccessful())
                                                                {
                                                                    updateUI(currentuser);
                                                                }

                                                            }
                                                        });

                                                    }
                                                    else
                                                    {
                                                        Toast.makeText(MainActivity.this,
                                                                "Registration Error",
                                                                Toast.LENGTH_SHORT)
                                                                .show();
                                                    }

                                                }
                                            });
                                        }
                                        else
                                        {
                                            Toast.makeText(MainActivity.this, "Location Not Available", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else
                                    {
                                        Toast.makeText(MainActivity.this, "Could not get location", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(MainActivity.this, "Enable Location", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    }

                    // DO firebase here


                    //region OldCode
                    /*mAuth.signInWithEmailAndPassword(email_string, password_string)
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Toast.makeText(MainActivity.this,
                                                "Logged In Successfully",
                                                Toast.LENGTH_SHORT)
                                                .show();

                                        FirebaseUser user = mAuth.getCurrentUser();
                                        updateUI(user);

                                    } else {
                                        // If sign in fails, display a message to the user.

                                        mAuth.createUserWithEmailAndPassword(email_string, password_string).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if(task.isSuccessful())
                                                {
                                                    Toast.makeText(MainActivity.this,
                                                            "Registered Successfully",
                                                            Toast.LENGTH_SHORT)
                                                            .show();


                                                    final FirebaseUser currentuser=FirebaseAuth.getInstance().getCurrentUser();
                                                    String userid = currentuser.getUid();
                                                    database = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);

                                                    //HashMap is used to add user informaton its an easy method
                                                    HashMap<String,String> usermap = new HashMap<>();
                                                    usermap.put("Email",email_string);
                                                    usermap.put("UserType",usertype);

                                                    database.setValue(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            if(task.isSuccessful())
                                                            {
                                                                updateUI(currentuser);
                                                            }

                                                        }
                                                    });

//                                                    FirebaseUser user = mAuth.getCurrentUser();
//                                                    updateUI(user);
                                                }
                                                else
                                                {
                                                    Toast.makeText(MainActivity.this,
                                                            "Error Signing in",
                                                            Toast.LENGTH_SHORT)
                                                            .show();
                                                }
                                            }
                                        });
                                    }
                                }
                            });*/
                            //endregion

                }



            }
        });



        //endregion

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

    }

    private void updateUI(FirebaseUser currentUser) {

        // Go to Order Screen
        Log.d("***","Update UI");

        if(currentUser != null)
        {
            Intent i = new Intent(MainActivity.this, OrderActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("UserType",usertype);
            startActivity(i);
            finish();
        }

    }
}
