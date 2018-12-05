package icm.entities;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.sql.Time;
import java.util.ArrayList;

import icm.greencities.Discount;
import icm.greencities.QRCodeGenerator;
import icm.greencities.R;
import icm.greencities.ResultsMain;

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
        Button btnSwitch;

        public DataObjectHolder(View itemView) {
            super(itemView);
            activity = (TextView) itemView.findViewById(R.id.textAtivityType);
            distance = (TextView) itemView.findViewById(R.id.textDistance);
            time = (TextView) itemView.findViewById(R.id.textTime);
            date = (TextView) itemView.findViewById(R.id.textDate);
            actIcon = (ImageView) itemView.findViewById(R.id.imageViewActivity);
            btnSwitch = (Button) itemView.findViewById(R.id.button);
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
        long time_aux = (long)mDataset.get(position).getTime();
        int minutes, hours, seconds;
        hours = (int) (time_aux/3600);
        minutes = (int) (time_aux- (hours * 3600)) / 60;
        seconds = (int) time_aux - (hours * 3600) - (minutes * 60);

        String min,h,sec;
        if(hours<10)
            h = "0" + hours;
        else
            h = hours+"";
        if(minutes<10)
            min = "0" + minutes;
        else
            min = minutes + "";
        if(seconds<10)
            sec = "0" + seconds;
        else
            sec = seconds + "";

        holder.activity.setText(mDataset.get(position).getActivity());
        holder.distance.setText((double) mDataset.get(position).getDistance() + " m");
        holder.time.setText(h + ":" + min+":"+sec);
        holder.date.setText("December 5th 2018");
        mDataset.get(position).getDate();

        if (mDataset.get(position).getActivity() == "Cycling")
            holder.actIcon.setImageResource(R.drawable.ic_on_bicycle);
        if (mDataset.get(position).getActivity() == "Running")
            holder.actIcon.setImageResource(R.drawable.ic_running);
        if (mDataset.get(position).getActivity() == "Walking")
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
