package com.rdc.p2p.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rdc.p2p.R;
import com.rdc.p2p.base.BaseRecyclerViewAdapter;
import com.rdc.p2p.bean.GroupBean;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.util.ImageUtil;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;
import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

public class GroupListRvAdapter extends BaseRecyclerViewAdapter<GroupBean>{

    /**
     * 添加一个Item
     *
     * @param groupBean
     */
    public void addItem(GroupBean groupBean) {
        mDataList.add(groupBean);
        notifyItemRangeChanged(mDataList.size() - 1, 1);
    }

    public void clearItemBadge(int index) {
        if (index != -1) {
            GroupBean group = getDataList().get(index);
            group.setMsgNum(0);
            notifyItemChanged(index);
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_peer_list, parent, false);
        return new GroupListRvAdapter.ItemHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((GroupListRvAdapter.ItemHolder) holder).bindView(mDataList.get(position));
    }

    class ItemHolder extends BaseRvHolder {

        @BindView(R.id.tv_nickname_item_peer_list)
        TextView mTvNickname;
        @BindView(R.id.civ_user_image_item_peer_list)
        CircleImageView mCivUserImage;
        @BindView(R.id.tv_recent_message_item_peer_list)
        TextView mTvRecentMessage;
        @BindView(R.id.tv_time_item_peer_list)
        TextView mTvTime;
        // 通知小红点
        Badge badge;

        public ItemHolder(View itemView, Context context) {
            super(itemView);
            badge = new QBadgeView(context).bindTarget(itemView.findViewById(R.id.civ_user_image_item_peer_list));
            badge.setBadgeNumber(0).setGravityOffset(0, 0, true).setBadgeGravity(Gravity.END | Gravity.TOP).setBadgeTextSize(10, true);
        }

        @Override
        protected void bindView(GroupBean groupBean) {
            mTvNickname.setText(groupBean.getNickName());
            Glide.with(itemView.getContext())
                    .load(ImageUtil.getImageResId(groupBean.getGroupImageId())).into(mCivUserImage);
            mTvRecentMessage.setText(groupBean.getRecentMessage());
            mTvTime.setText(groupBean.getTime());
            badge.setBadgeNumber(groupBean.getMsgNum());
        }
    }
}
