package io.temasys.skylinksdktimer;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ItemViewHolder> {
    private Context mContext;
    private List<Pair<String, Long>> mData;

    public RecyclerViewAdapter(Context context, List<Pair<String, Long>> data) {
        mContext = context;
        mData = data;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater.from(mContext).inflate(R.layout.recycler_view_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Pair<String, Long> data = mData.get(position);
        holder.mDescriptionTv.setText(data.first);
        holder.mTimeTv.setText(data.second.toString() + " ms");
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    protected static class ItemViewHolder extends RecyclerView.ViewHolder {
        protected TextView mDescriptionTv;
        protected TextView mTimeTv;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            mDescriptionTv = itemView.findViewById(R.id.descriptionTv);
            mTimeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
