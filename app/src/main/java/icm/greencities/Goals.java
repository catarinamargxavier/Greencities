package icm.greencities;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import icm.entities.Discount;
import icm.entities.MyRecyclerViewAdapter;

public class Goals extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private static String LOG_TAG = "CardViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

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

    private ArrayList<Discount> getDataSet() {
        ArrayList results = new ArrayList<Discount>();
        Discount discount = new Discount(312,"30% Desconto na FNAC", "Numa compra superior a 100€, oferta de 30% de desconto.", "Fnac", "500 points");
        results.add(0, discount);
        Discount discount1 = new Discount(22,"5€ em Sapatilhas na SportZone", "Desconto de 5€ numa compra superior a 40€", "SportZone", "300 points");
        results.add(1, discount1);
        Discount discount2 = new Discount(921,"Menu Grande ao preço Menu Normal", "McMenu Grande com desconto Normal", "Mac", "50 points");
        results.add(2, discount2);
        Discount discount3 = new Discount(421,"Rodizio Pizza a 8,5€", "Desconto no Rodizio de Pizza", "Pizza Hut", "75 points");
        results.add(3, discount3);
        results.add(4, discount1);
        results.add(5, discount2);
        return results;
    }
}
