package icm.greencities;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import icm.entities.Discount2;
import icm.entities.MyCallback;
import icm.entities.MyRecyclerViewAdapter;
import icm.entities.Store;

public class Goals extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private static String LOG_TAG = "CardViewActivity";
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        db = FirebaseFirestore.getInstance();

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyRecyclerViewAdapter(getDataSet());
        mRecyclerView.setAdapter(mAdapter);

        // Code to Add an item with default animation
        //((MyRecyclerViewAdapter) mAdapter).addItem(obj, index);

        // Code to remove an item with default animation
        //((MyRecyclerViewAdapter) mAdapter).deleteItem(index);

    }


    @Override
    protected void onResume() {
        super.onResume();
        ((MyRecyclerViewAdapter) mAdapter).setOnItemClickListener(new MyRecyclerViewAdapter
                .MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Log.i(LOG_TAG, " Clicked on Item " + position);
            }
        });
    }


    private ArrayList<Discount2> getDataSet() {

        readData(new MyCallback() {
            @Override
            public void onCallback(Object value) {
                for (Object i :(ArrayList) value) {
                    Log.d("TAG", ((Store) i).getName());
                    Log.d("TAG", Integer.toString(((Store) i).getId()));
                }

            }
        });

        ArrayList results = new ArrayList<Discount2>();
        Discount2 discount = new Discount2(312,"30% Desconto na FNAC", "Numa compra superior a 100€, oferta de 30% de desconto.", "Fnac", "500 points");
        results.add(0, discount);
        Discount2 discount21 = new Discount2(22,"5€ em Sapatilhas na SportZone", "Desconto de 5€ numa compra superior a 40€", "SportZone", "300 points");
        results.add(1, discount21);
        Discount2 discount22 = new Discount2(921,"Menu Grande ao preço Menu Normal", "McMenu Grande com desconto Normal", "Mac", "50 points");
        results.add(2, discount22);
        Discount2 discount23 = new Discount2(421,"Rodizio Pizza a 8,5€", "Desconto no Rodizio de Pizza", "Pizza Hut", "75 points");
        results.add(3, discount23);
        results.add(4, discount21);
        results.add(5, discount22);
        return results;
    }


    private void readData (final MyCallback myCallback) {
        db.collection("stores")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Store> stores = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //Log.d("sucess", document.getId() + " => " + document.getData());
                                Store loja = document.toObject(Store.class);
                                loja.setId(Integer.parseInt(document.getId()));
                                stores.add(loja);
                            }
                            myCallback.onCallback(stores);
                        } else {
                            Log.d("error", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


}
