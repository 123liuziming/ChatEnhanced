package com.rdc.p2p.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rdc.p2p.R;
import com.rdc.p2p.base.BaseRecyclerViewAdapter;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.util.ImageUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

public class AddGroupChatUserAdapter extends BaseRecyclerViewAdapter<PeerBean> {

    private SparseBooleanArray mSelectedPositions = new SparseBooleanArray();
    private boolean mIsSelectable = false;

    public AddGroupChatUserAdapter(List<PeerBean> userList){
        // 将数据添加
        appendData(userList);
    }

    public SparseBooleanArray getmSelectedPositions() {
        return mSelectedPositions;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_group_list, parent, false);
        return new ItemHolder(view, parent.getContext());
    }

    //设置给定位置条目的选择状态
    private void setItemChecked(int position, boolean isChecked) {
        mSelectedPositions.put(position, isChecked);
    }

    //根据位置判断条目是否选中
    private boolean isItemChecked(int position) {
        return mSelectedPositions.get(position);
    }

    //根据位置判断条目是否可选
    private boolean isSelectable() {
        return mIsSelectable;
    }
    //设置给定位置条目的可选与否的状态
    private void setSelectable(boolean selectable) {
        mIsSelectable = selectable;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        ((ItemHolder) holder).bindView(mDataList.get(position));

        //checkBox的监听
        ((ItemHolder) holder).checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = holder.getAdapterPosition();
                if (isItemChecked(i)) {
                    setItemChecked(i, false);
                } else {
                    setItemChecked(i, true);
                }
            }
        });

        //条目view的监听
        ((ItemHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = holder.getAdapterPosition();
                if (isItemChecked(i)) {
                    setItemChecked(i, false);
                } else {
                    setItemChecked(i, true);
                }
//                notifyItemChanged(i);
            }
        });
    }

    class ItemHolder extends BaseRvHolder {

        @BindView(R.id.tv_recent_message_item_addGroupChat)
        TextView mTvNickname;
        @BindView(R.id.civ_user_image_item_addGroupChat)
        CircleImageView mCivUserImage;
        @BindView(R.id.checkbox_addGroupChat)
        AppCompatCheckBox checkBox;


        public ItemHolder(View itemView, Context context) {
            super(itemView);
        }

        @Override
        protected void bindView(PeerBean peerBean) {
            mTvNickname.setText(peerBean.getNickName());
            Glide.with(itemView.getContext())
                    .load(ImageUtil.getImageResId(peerBean.getUserImageId())).into(mCivUserImage);
        }
    }
}
