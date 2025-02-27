package com.example.cloudchat_user.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cloudchat_user.R;
import com.example.cloudchat_user.ui.science_workshop.WorkshopViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VoteAdapter extends RecyclerView.Adapter<VoteAdapter.ViewHolder> {

    private final HashMap<String, ArrayList<String>> arrayListMap;
    private final List<String> keys;
    private final WorkshopViewModel viewModel;


    public VoteAdapter(HashMap<String, ArrayList<String>> hashMap, WorkshopViewModel viewModel) {
        this.arrayListMap = hashMap;
        this.keys = new ArrayList<>(hashMap.keySet());
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_vote_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String key = keys.get(position);
        ArrayList<String> row = arrayListMap.get(key);
        Integer pos = holder.getAdapterPosition();
        Integer now_pos = viewModel.getChecked_position().getValue();


        assert now_pos != null;
        holder.radioButton.setChecked(now_pos.equals(position));

        if (!row.isEmpty()&&row.size()>=6){
            holder.topicText.setText(row.get(0));
            holder.ownerText.setText(row.get(1));
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add(row.get(0));
            arrayList.add(row.get(1));
            arrayList.add(row.get(3));
            arrayList.add(row.get(4));
            arrayList.add(row.get(5));
            arrayList.add(row.get(2));


            holder.detailText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewModel.setDetails_list(arrayList);
                    viewModel.setDetail_switch(true);
                }
            });

            holder.radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewModel.setChecked_position(pos);
                    viewModel.setVote_id(key);
                    notifyDataSetChanged();
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return arrayListMap.keySet().size();
    }

    public void addRow(String key, ArrayList<String> newRow) {
        this.arrayListMap.put(key, newRow); // 向HashMap中添加新行
        this.keys.add(key); // 将新键添加到keys中
        notifyItemInserted(keys.size()-1); // 通知插入新行
    }

    public void removeRow(int position) {
        String keyToRemove = this.keys.get(position); // 获取要删除的键
        this.arrayListMap.remove(keyToRemove); // 从HashMap中删除
        this.keys.remove(position); // 从keys中删除该键
        notifyItemRemoved(position); // 通知删除指定位置的行
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView topicText;
        TextView ownerText;
        TextView detailText;
        RadioButton radioButton;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            topicText = itemView.findViewById(R.id.topic);
            ownerText = itemView.findViewById(R.id.owner);
            detailText = itemView.findViewById(R.id.detail);
            radioButton = itemView.findViewById(R.id.vote_for);

        }
    }
}
