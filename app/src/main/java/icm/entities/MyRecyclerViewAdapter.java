package icm.entities;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.Resource;

import icm.greencities.R;

public class MyRecyclerViewAdapter extends RecyclerView
        .Adapter<MyRecyclerViewAdapter
        .DataObjectHolder> {
    private static String LOG_TAG = "MyRecyclerViewAdapter";
    private ArrayList<Discount> mDataset;
    private static MyClickListener myClickListener;

    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        TextView title;
        TextView description, points;
        ImageView store;

        public DataObjectHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.textView);
            description = (TextView) itemView.findViewById(R.id.textView2);
            store = (ImageView) itemView.findViewById(R.id.storeLogo);
            points = (TextView) itemView.findViewById(R.id.textPoints);
            Log.i(LOG_TAG, "Adding Listener");
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public MyRecyclerViewAdapter(ArrayList<Discount> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_row, parent, false);

        DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        holder.title.setText(mDataset.get(position).getTitle());
        holder.description.setText(mDataset.get(position).getDescription());
        holder.points.setText(mDataset.get(position).getValue());

        if (mDataset.get(position).getStoreName() == "Pizza Hut")
            holder.store.setImageResource(R.drawable.pizzahut);
        if (mDataset.get(position).getStoreName() == "Fnac")
            holder.store.setImageResource(R.drawable.fnac_logo);
        if (mDataset.get(position).getStoreName() == "SportZone")
            holder.store.setImageResource(R.drawable.sportzone);
        if (mDataset.get(position).getStoreName() == "Mac")
            holder.store.setImageResource(R.drawable.mac);


    }

    public void addItem(Discount discountObj, int index) {
        mDataset.add(index, discountObj);
        notifyItemInserted(index);
    }

    public void deleteItem(int index) {
        mDataset.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface MyClickListener {
        public void onItemClick(int position, View v);
    }
}
