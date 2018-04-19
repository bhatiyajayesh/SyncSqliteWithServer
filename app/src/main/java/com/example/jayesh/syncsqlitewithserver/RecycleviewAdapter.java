package com.example.jayesh.syncsqlitewithserver;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Jayesh on 19-Apr-18.
 */

public class RecycleviewAdapter extends RecyclerView.Adapter<RecycleviewAdapter.MyViewHolder> {
    ArrayList<Contact> contactArrayList = new ArrayList<>();

    public RecycleviewAdapter(ArrayList<Contact> contacts) {
        this.contactArrayList = contacts;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.tvName.setText(contactArrayList.get(position).getName());
        int sync_status = contactArrayList.get(position).getSync_status();
        if (sync_status == DbContact.SYNC_STATUS_OK) {
            holder.ivSyncStatus.setImageResource(R.drawable.icon_done);
        } else {
            holder.ivSyncStatus.setImageResource(R.drawable.icon_sync);
        }
    }

    @Override
    public int getItemCount() {
        return contactArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView ivSyncStatus;
        TextView tvName;

        public MyViewHolder(View itemView) {
            super(itemView);
            ivSyncStatus = (ImageView) itemView.findViewById(R.id.ivSync);
            tvName = (TextView) itemView.findViewById(R.id.txtName);
        }
    }
}
