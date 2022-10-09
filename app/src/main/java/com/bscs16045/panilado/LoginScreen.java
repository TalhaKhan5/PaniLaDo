package com.bscs16045.panilado;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class LoginScreen extends AppCompatActivity {

    EditText email, password;
    ImageView logo;
    Button login_btn;
    ViewGroup root;

    String email_string;
    String password_string;

    private FirebaseAuth mAuth;
    private DatabaseReference database;


    long DelayMain = 1200;
    long DelayStep = 125;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        email = findViewById(R.id.emailText2);
        password = findViewById(R.id.passwordText2);
        logo = findViewById(R.id.imageView3);
        login_btn = findViewById(R.id.loginButton2);
        root = findViewById(R.id.root2);

        logo.setRotation(0);
        email.setVisibility(View.GONE);
        password.setVisibility(View.GONE);
        login_btn.setVisibility(View.GONE);

        //region Animations
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            root.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGE_APPEARING);
        }

        Handler h = new Handler(getMainLooper());
        h.postDelayed(new Runnable() {
            @Override
            public void run() {

                logo.animate().rotation(-20).setDuration(1000).start();
                email.setVisibility(View.VISIBLE);

            }
        },DelayMain);

        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                password.setVisibility(View.VISIBLE);
            }
        },DelayMain + DelayStep);

        h.postDelayed(new Runnable() {
            @Override
            public void run() {

                login_btn.setVisibility(View.VISIBLE);

            }
        },DelayMain + DelayStep * 3);
        //endregion

        mAuth = FirebaseAuth.getInstance();

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email_string = email.getText().toString().trim();
                password_string = password.getText().toString().trim();

                if(email_string.isEmpty())
                {
                    Toast.makeText(LoginScreen.this,
                            "Enter Email",
                            Toast.LENGTH_SHORT)
                            .show();

                    email.requestFocus();
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(email_string).matches())
                {
                    Toast.makeText(LoginScreen.this,
                            "Not Valid Email",
                            Toast.LENGTH_SHORT)
                            .show();

                    email.requestFocus();
                }
                else if(password_string.isEmpty())
                {
                    Toast.makeText(LoginScreen.this,
                            "Enter Password",
                            Toast.LENGTH_SHORT)
                            .show();

                    password.requestFocus();
                }
                else
                {
                    // firebase sign in - To be tested

                    mAuth.signInWithEmailAndPassword(email_string, password_string).addOnCompleteListener(LoginScreen.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Toast.makeText(LoginScreen.this,
                                        "Logged In Successfully",
                                        Toast.LENGTH_SHORT)
                                        .show();

                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);

                            } else {

                                Toast.makeText(LoginScreen.this,
                                        "Login Error",
                                        Toast.LENGTH_SHORT)
                                        .show();


                            }
                        }
                    });

                }
            }
        });
    }

    private void updateUI(FirebaseUser currentUser) {

        // Go to Order Screen
        Log.d("***","Update UI");

        if(currentUser != null)
        {
            Intent i = new Intent(LoginScreen.this, OrderActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        }

    }
}
