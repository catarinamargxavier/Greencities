package icm.greencities;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import icm.entities.Activity;
import icm.others.MyCallback;
import icm.others.MyRecyclerViewAdapterResults;

public class ResultsMainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private static String LOG_TAG = "CardViewActivity";
    private Map<Integer, Activity> dados = new HashMap<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results_main);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view_results);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        getData();
        presentToUser();
    }


    @Override
    protected void onResume() {
        super.onResume();
        ((MyRecyclerViewAdapterResults) mAdapter).setOnItemClickListener(new MyRecyclerViewAdapterResults
                .MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Log.i(LOG_TAG, " Clicked on Item " + position);

            }
        });
    }


    private void getData () {
        readData(new MyCallback() {
            @Override
            public void onCallback(Object value) {
                dados = (Map<Integer, Activity>) value;
                presentToUser();
            }
        });
    }


    private void presentToUser() {
        mAdapter = new MyRecyclerViewAdapterResults(getDataSet());
        mRecyclerView.setAdapter(mAdapter);
    }


    private ArrayList<Activity> getDataSet() {
        ArrayList <Activity> results = new ArrayList<>();
        if (dados.size() != 0) {
            for (Map.Entry<Integer, Activity> i : dados.entrySet()) {
                results.add(i.getValue());
            }
        }
        return results;
    }


    private void readData (final MyCallback myCallback) {
        db.collection("users").document(user.getEmail()).collection("activities")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<Integer, Activity> atividades = new HashMap<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Activity atividade = document.toObject(Activity.class);
                                atividades.put(Integer.parseInt(document.getId()),atividade);
                            }

                            myCallback.onCallback(atividades);
                        } else {
                            Log.d("error", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

}
