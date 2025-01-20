package com.example.cloudchat_user.ui.science_workshop;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.cloudchat_user.MainActivity;
import com.example.cloudchat_user.R;
import com.example.cloudchat_user.databinding.FragmentWorkshopBinding;

public class WorkshopFragment extends Fragment {

    private FragmentWorkshopBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        WorkshopViewModel workshopViewModel =
                new ViewModelProvider(this).get(WorkshopViewModel.class);

        binding = FragmentWorkshopBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        final Button button_vote = binding.voteButton;
        final Button button_lecture = binding.lectureReplayButton;
        final ImageView imageView_live = binding.liveImageview;



        button_vote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowVoteDialog(v);
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}