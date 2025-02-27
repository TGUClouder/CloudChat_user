package com.example.cloudchat_user.ui.science_workshop;


import static android.content.Context.MODE_PRIVATE;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.cloudchat_user.R;
import com.example.cloudchat_user.adapter.VoteAdapter;
import com.example.cloudchat_user.adapter.VoteResultAdapter;
import com.example.cloudchat_user.dao.VotesDao;
import com.example.cloudchat_user.databinding.FragmentWorkshopBinding;
import com.example.cloudchat_user.itemdecoration.VoteDecoration;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class WorkshopFragment extends Fragment {


    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private Boolean VOTABLE;
    private static final String TAG = "WorkshopFragment";

    public String USERNAME = "vote_admin";
    private AlertDialog voteListDialog;

    private FragmentWorkshopBinding binding;
    private  WorkshopViewModel workshopViewModel;
    private final HashMap<String, ArrayList<String>> voteMap = new HashMap<>();
    private VoteAdapter adapter;

    private VoteResultAdapter voteResultAdapter;

    private Boolean renewed = false;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        workshopViewModel =
                new ViewModelProvider(this).get(WorkshopViewModel.class);

        binding = FragmentWorkshopBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        adapter = new VoteAdapter(voteMap, workshopViewModel);
        voteResultAdapter = new VoteResultAdapter(voteMap, workshopViewModel);


        final Button button_vote = binding.voteButton;
        final Button button_lecture = binding.lectureReplayButton;
        final ImageView imageView_live = binding.liveImageview;

        sharedPreferences = getContext().getSharedPreferences("preference", MODE_PRIVATE);
        editor = sharedPreferences.edit();


        VOTABLE = sharedPreferences.getBoolean("votable",  true);



        button_vote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!VOTABLE){ShowVoteResult();}else{ShowVoteDialog(v);}

            }
        });

        button_lecture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowWorkshopDialog(v);
            }
        });

        imageView_live.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowLive(v);
            }
        });


//        final TextView textView = binding.textWorkshop;
//        workshopViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }

    private void ShowLive(View v) {
        Toast.makeText(getContext(), "后续实现跳转直播", Toast.LENGTH_SHORT).show();
    }

    private void ShowWorkshopDialog(View v) {
        final String[] items = {"科学科普", "生活科普", "健康科普", "其他"};
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setIcon(R.mipmap.app_icon)//设置标题的图片
                .setTitle("课程回放（后续实现跳转activity）")//设置对话框的标题
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();

    }

    private int vote_index;
    private void ShowVoteDialog(View v){
        final String[] items = {"选项一", "选项二", "选项三", "选项四"};

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setIcon(R.mipmap.app_icon)//设置标题的图片
                .setTitle("投票（后续由数据库获取）")//设置对话框的标题
                .setSingleChoiceItems(items, 1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        vote_index = which;
                        Toast.makeText(getContext(), items[which]+" 后续弹出对话框显示具体内容", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getContext(), "你选择了"+items[vote_index], Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();

    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // 数据获取
        workshopViewModel.getVoteLiveData().observe(getViewLifecycleOwner(), new Observer<HashMap<String, ArrayList<String>>>() {
            @Override
            public void onChanged(HashMap<String, ArrayList<String>> hashMap) {
                for(String key:hashMap.keySet()){
                    voteMap.put(key,hashMap.get(key));
                    adapter.addRow(key, hashMap.get(key));
                    voteResultAdapter.addRow(key, hashMap.get(key));
                }
            }
        });

        workshopViewModel.getDetail_switch().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean.equals(true)&&voteListDialog!=null){
                    ShowVoteDetail();
                }
            }
        });
    }

    private void ShowLive(View v) {
        editor.putBoolean("votable", true);
        editor.commit();
        Toast.makeText(getContext(), "后续实现跳转直播,已解除投票限制,请重启应用！", Toast.LENGTH_SHORT).show();
    }

    private void ShowWorkshopDialog(View v) {
        final String[] items = {"科学科普", "生活科普", "健康科普", "其他"};
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setIcon(R.mipmap.app_icon)//设置标题的图片
                .setTitle("课程回放（后续实现跳转activity）")//设置对话框的标题
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();

    }

    private void ShowVoteDialog(View v){
        if(voteMap.isEmpty()){
            Toast.makeText(getContext(), "请等待数据加载！", Toast.LENGTH_LONG).show();
            return;
        }
        View voteListLayout = getLayoutInflater().inflate(R.layout.dialog_vote_list, null);

        // 设置 RecyclerView 和适配器
        RecyclerView recyclerView = voteListLayout.findViewById(R.id.recycler_table);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false);
        recyclerView.addItemDecoration(new VoteDecoration(2));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        voteListDialog = new AlertDialog.Builder(getContext())
                .setIcon(R.mipmap.app_icon)
                .setView(voteListLayout)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        voteListDialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String id = workshopViewModel.getVote_id().getValue();
                        new Thread(new Runnable() {
                            String response;
                            @Override
                            public void run() {
                                try {
                                    response = VotesDao.update_pros(id);
                                } catch (IOException e) {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getContext(), "投票失败，请稍后重试！", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    return;
                                }

                                // 在主线程中显示 Toast
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(response.equals("true")){
                                            editor.putBoolean("votable", false);
                                            editor.apply();
                                            VOTABLE = false;
                                            Toast.makeText(getContext(), "投票已完成，谢谢参与！", Toast.LENGTH_SHORT).show();
                                            ShowVoteResult();
                                        }
                                        else if (response.equals("ConnectionFailed")) {Toast.makeText(getContext(), "网络连接出错，请稍后重试！", Toast.LENGTH_SHORT).show();}
                                        else{Toast.makeText(getContext(), "服务器内部出错！", Toast.LENGTH_SHORT).show();}
                                    }
                                });

                            }
                        }).start();
                        voteListDialog.dismiss();
                    }
                })
                .create();

        voteListDialog.show();

    }

    private void ShowVoteDetail(){
        View view = getLayoutInflater().inflate(R.layout.dialog_vote_detail, null);
        TextView topicTV = view.findViewById(R.id.detail_topic);
        TextView creatorTV = view.findViewById(R.id.detail_creator);
        TextView startTV = view.findViewById(R.id.detail_start_time);
        TextView endTV = view.findViewById(R.id.detail_end_time);
        TextView detailTV = view.findViewById(R.id.detail_content);

        if (workshopViewModel.getDetails_list().getValue()!=null){
            topicTV.setText(workshopViewModel.getDetails_list().getValue().get(0));
            creatorTV.setText(workshopViewModel.getDetails_list().getValue().get(1));
            startTV.setText(workshopViewModel.getDetails_list().getValue().get(3));
            endTV.setText(workshopViewModel.getDetails_list().getValue().get(4));
            detailTV.setText(workshopViewModel.getDetails_list().getValue().get(5));}

        AlertDialog voteDetailDialog = new AlertDialog.Builder(getContext())
                .setIcon(R.mipmap.app_icon)
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        workshopViewModel.setDetail_switch(false);
                    }
                })
                .create();
        voteDetailDialog.show();
    }

    private void ShowVoteResult(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    workshopViewModel.setVoteLiveData(VotesDao.get_all_votes());
                    renewed = true;
                } catch (IOException | JSONException e) {
                    renewed = true;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(getContext(),"网络异常，请稍后再查询结果！",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
        while(!renewed);
        View view = getLayoutInflater().inflate(R.layout.dialog_vote_result, null);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_table1);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false);
        recyclerView.addItemDecoration(new VoteDecoration(2));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(voteResultAdapter);
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setIcon(R.mipmap.app_icon)
                .setTitle("投票结果")
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}