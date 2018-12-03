package icm.entities;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import icm.greencities.R;

public class MyRecyclerViewAdapterResults extends RecyclerView
        .Adapter<MyRecyclerViewAdapterResults
        .DataObjectHolder> {
    private static String LOG_TAG = "MyRecyclerViewAdapter";
    private ArrayList<Activity> mDataset;
    private static MyClickListener myClickListener;

    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        TextView activity, distance, date, time;
        ImageView actIcon;

        public DataObjectHolder(View itemView) {
            super(itemView);
            activity = (TextView) itemView.findViewById(R.id.textAtivityType);
            distance = (TextView) itemView.findViewById(R.id.textDistance);
            time = (TextView) itemView.findViewById(R.id.textTime);
            date = (TextView) itemView.findViewById(R.id.textDate);
            actIcon = (ImageView) itemView.findViewById(R.id.imageViewActivity);
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

    public MyRecyclerViewAdapterResults(ArrayList<Activity> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_row_results, parent, false);

        DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        holder.activity.setText(mDataset.get(position).getActivity());
        holder.distance.setText(mDataset.get(position).getDistance()+"");
        holder.time.setText(mDataset.get(position).getTime()+"");
        holder.date.setText("3 dez 2018");


        if (mDataset.get(position).getActivity() == "bicycling")
            holder.actIcon.setImageResource(R.drawable.ic_on_bicycle);
        if (mDataset.get(position).getActivity() == "running")
            holder.actIcon.setImageResource(R.drawable.ic_running);
        if (mDataset.get(position).getActivity() == "walking")
            holder.actIcon.setImageResource(R.drawable.ic_walking);

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
