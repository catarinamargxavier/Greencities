package icm.greencities;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import icm.entities.Discount2;
import icm.entities.Discount;
import icm.others.MyCallback;
import icm.others.MyRecyclerViewAdapter;
import icm.entities.Store;
import icm.entities.User;

public class GoalsActivity extends AppCompatActivity {

    private TextView pointsText;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private static String LOG_TAG = "CardViewActivity";

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private User userData;

    private Map<String, Store> dados = new HashMap<>();
    private List <Discount> aux = new ArrayList<>();

    private int count;
    private String id;
    private int pointsUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        pointsText = (TextView) findViewById(R.id.userPointsAvailable);
        String email = user.getEmail();
        DocumentReference docRef = db.collection("users").document(email);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                userData = documentSnapshot.toObject(User.class);
                pointsText.setText(userData.getPoints() + " points");
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // NEW
        getData();
        // END OF NEW



        // HERE GET DATA
        mAdapter = new MyRecyclerViewAdapter(getDataSet());
        // END OF GET DATA
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
                startActivity(new Intent(GoalsActivity.this, QRCodeGeneratorActivity.class));
            }
        });
    }


    private void getData () {

        readData(new MyCallback() {
            @Override
            public void onCallback(Object value) {
                dados = (Map<String, Store>) value;
                count = 0;
                for (Map.Entry<String, Store> i : dados.entrySet()) {
                    id = i.getKey();
                    //Log.d("Tag4", id);
                    getAuxData();
                    //Log.d("Tag3", "CUCU IT'S ME MARIO");
                }
            }
        });

    }


    private void getAuxData () {

            //Log.d("Tag3", "OHLALA OHLALA");
            final String id2 = id;
            readMoreData(id2, new MyCallback() {
                @Override
                public void onCallback(Object value) {
                    //Log.d("Tag3", Arrays.toString(((List<Discount>) value).toArray()));
                    //Log.d("Tag3", "NLAAAAAAAAAAAAAAAAAA");
                    //List <Discount> aux2 = (List<Discount>) value;
                    //Log.d("Tag3", Integer.toString(aux2.size()));
                    count ++;
                    //Log.d("Tag5", id2);
                    Store j = dados.get(id2);
                    j.setDiscount((List<Discount>) value);
                    dados.put(id2, j);
                    //Log.d("Tag3", Integer.toString(((List<Discount>) value).size()));
                    if (count == dados.size()) {
                        presentToUser();
                    }
                }
            });
    }


    private void presentToUser() {
        DocumentReference docRef = db.collection("users").document(user.getEmail());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                userData = documentSnapshot.toObject(User.class);
                pointsUser = userData.getPoints();
                mAdapter = new MyRecyclerViewAdapter(getDataSet());
                mRecyclerView.setAdapter(mAdapter);
            }
        });
    }


    private ArrayList<Discount2> getDataSet() {

        ArrayList results = new ArrayList<Discount2>();

        if (dados.size() != 0) {
            for (Map.Entry<String, Store> i : dados.entrySet()) {
                for (Discount desconto: i.getValue().getDiscount()) {
                    if (desconto.getValue() <= pointsUser) {
                        Discount2 discount = new Discount2(Integer.parseInt(i.getKey()), desconto.getTitle(), desconto.getDescription(), i.getValue().getName(), Integer.toString(desconto.getValue()) + " points");
                        results.add(discount);
                    }
                }
            }
        }

        return results;
    }


    private void readData (final MyCallback myCallback) {
        db.collection("stores")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Store> stores = new HashMap<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //Log.d("sucess", document.getId() + " => " + document.getData());
                                Store loja = document.toObject(Store.class);
                                //loja.setId(Integer.parseInt(document.getId()));
                                stores.put(document.getId(), loja);
                            }
                            myCallback.onCallback(stores);
                        } else {
                            Log.d("error", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


    private void readMoreData (String id, final MyCallback myCallback) {
        db.collection("stores").document(id).collection("discount")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Discount> discounts = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //Log.d("sucess", document.getId() + " => " + document.getData());
                                Discount desconto = document.toObject(Discount.class);
                                desconto.setId(Integer.parseInt(document.getId()));
                                discounts.add(desconto);
                            }
                            myCallback.onCallback(discounts);
                        } else {
                            Log.d("error", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


}




