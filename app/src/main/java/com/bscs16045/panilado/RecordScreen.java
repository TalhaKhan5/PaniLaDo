package com.bscs16045.panilado;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RecordScreen extends AppCompatActivity {

    FirebaseDatabase firebaseDatabase;
    FirebaseUser currentUser;
    DatabaseReference ordersRef;

    ListView listView;
    ArrayList<DataSnapshot> dataSnapshotArrayList;

    TextView order_rec,status_rec,size_rec,rate_rec,qty_rec,total_rec;
    TextView consumer_rec,supplier_rec;
    TextView time_rec,date_rec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_screen);
        firebaseDatabase = FirebaseDatabase.getInstance();
        listView = findViewById(R.id.listView);

        dataSnapshotArrayList = new ArrayList<>();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null)
        {
            ordersRef = firebaseDatabase.getReference().child("Users").child(currentUser.getUid()).child("Orders");

            ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot order: dataSnapshot.getChildren())
                    {
                        dataSnapshotArrayList.add(order);
                    }

                    //apply adapter here
                    CustomAdapter ca = new CustomAdapter();
                    listView.setAdapter(ca);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    class CustomAdapter extends BaseAdapter{

        public CustomAdapter() {

        }

        @Override
        public int getCount() {
            if(dataSnapshotArrayList != null)
                return dataSnapshotArrayList.size();
            else
                return 0;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @SuppressLint({"ViewHolder", "InflateParams"})
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.cardlayout,null);
            order_rec = view.findViewById(R.id.order_rec);
            status_rec = view.findViewById(R.id.status_rec);
            size_rec = view.findViewById(R.id.size_rec);
            rate_rec = view.findViewById(R.id.rate_rec);
            qty_rec = view.findViewById(R.id.qty_rec);
            total_rec = view.findViewById(R.id.total_rec);
            consumer_rec = view.findViewById(R.id.consumer_rec);
            supplier_rec = view.findViewById(R.id.supplier_rec);
            time_rec = view.findViewById(R.id.time_rec);
            date_rec = view.findViewById(R.id.date_rec);

            order_rec.setText("Od.ID:\n"+dataSnapshotArrayList.get(i).child("Order Number").getValue(String.class));
            status_rec.setText("Status:\n"+dataSnapshotArrayList.get(i).child("Active_Status").getValue(String.class));
            size_rec.setText("Size:\n"+dataSnapshotArrayList.get(i).child("Size").getValue(String.class));
            rate_rec.setText("Rate:\n"+dataSnapshotArrayList.get(i).child("Rate").getValue(String.class));
            qty_rec.setText("Qty:\n"+dataSnapshotArrayList.get(i).child("Quantity").getValue(String.class));
            total_rec.setText("Total:\n"+dataSnapshotArrayList.get(i).child("Amount").getValue(String.class));
            consumer_rec.setText("Consumer:\n"+dataSnapshotArrayList.get(i).child("Consumer Email").getValue(String.class));
            supplier_rec.setText("Supplier:\n"+dataSnapshotArrayList.get(i).child("supplier").getValue(String.class));
            time_rec.setText("Time:\n"+dataSnapshotArrayList.get(i).child("Time").getValue(String.class));
            date_rec.setText("Date:\n"+dataSnapshotArrayList.get(i).child("Date").getValue(String.class));

            return view;
        }
    }
}
