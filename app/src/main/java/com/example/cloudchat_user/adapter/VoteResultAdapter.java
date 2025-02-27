package com.example.cloudchat_user.adapter;

import android.animation.ValueAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cloudchat_user.R;
import com.example.cloudchat_user.ui.science_workshop.WorkshopViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VoteResultAdapter extends RecyclerView.Adapter<VoteResultAdapter.ViewHolder> {

    private final HashMap<String, ArrayList<String>> arrayListMap;
    private final List<String> keys;
    private final WorkshopViewModel viewModel;


    public VoteResultAdapter(HashMap<String, ArrayList<String>> hashMap, WorkshopViewModel viewModel) {
        this.arrayListMap = hashMap;
        this.keys = new ArrayList<>(hashMap.keySet());
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_vote_result_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String key = keys.get(position);
        ArrayList<String> row = arrayListMap.get(key);

        int sum=0;

        if (!row.isEmpty()&&row.size()>=6){
            for(String k: this.arrayListMap.keySet()){
                sum += Integer.parseInt(arrayListMap.get(k).get(3));
            }
            holder.topicText.setText("话题："+row.get(0));
            animateVote(holder.percentageText,holder.progressBar,sum,Integer.parseInt(row.get(3)));

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



    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView topicText;
        TextView percentageText;
        ProgressBar progressBar;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            topicText = itemView.findViewById(R.id.result_topic);
            percentageText = itemView.findViewById(R.id.result_percentage);
            progressBar = itemView.findViewById(R.id.result_progress_bar);

        }
    }

    private void animateVote(TextView percentageVotes, ProgressBar progressBar, int voteCount, int voteEach) {
        ValueAnimator votesAnimator = ValueAnimator.ofInt(0, voteEach);
        int time = Math.round(Math.max(3000 *((float) voteEach /voteCount),2000));
        votesAnimator.setDuration(time);
        Log.d("ANIMATION", String.valueOf(time));// Animation duration
        votesAnimator.setInterpolator(new DecelerateInterpolator());
        votesAnimator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            int progress = (int) ((animatedValue / (float) voteCount) * 100);
            percentageVotes.setText(String.valueOf(animatedValue)+"人"+String.valueOf(progress)+"%");
            progressBar.setProgress(progress);
        });

        // Show progress bar and start animation
        progressBar.setVisibility(View.VISIBLE);
        percentageVotes.setVisibility(View.VISIBLE);
        votesAnimator.start();
    }
}
