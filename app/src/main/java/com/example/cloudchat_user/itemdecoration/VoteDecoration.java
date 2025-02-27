package com.example.cloudchat_user.itemdecoration;


import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class VoteDecoration extends RecyclerView.ItemDecoration {

    private final int space;

    public VoteDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        // 确保每个 item 的间距一致
        outRect.left = space;
        outRect.right = space;
        outRect.top = space;
        outRect.bottom = space;
//        if (parent.getChildAdapterPosition(view) == 0) {
//            outRect.top = space;
//        }

    }
}
