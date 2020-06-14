package com.rdc.p2p.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.othershe.combinebitmap.CombineBitmap;
import com.othershe.combinebitmap.layout.WechatLayoutManager;
import com.rdc.p2p.R;
import com.rdc.p2p.base.BaseRecyclerViewAdapter;
import com.rdc.p2p.bean.GroupBean;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.util.ImageUtil;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * 更新个Item的Text
     *
     * @param text
     */
    public GroupBean updateItemText(String text, String peerIp) {
        int index = getIndexByGroupName(peerIp);
        if (index != -1) {
            GroupBean groupBean = getDataList().get(index);
            groupBean.setRecentMessage(text);
            notifyItemChanged(index);
            return groupBean;
        }
        return null;
    }

    /**
     * 根据IP查询该成员在成员列表中的位置
     *
     * @param groupName
     * @return 成员的位置 ,如果找不到则返回 -1
     */
    private int getIndexByGroupName(String groupName) {
        for (int i = 0; i < mDataList.size(); i++) {
            if (mDataList.get(i).getNickName().equals(groupName)) {
                //找到成员的下标
                return i;
            }
        }
        return -1;
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
            List<Integer> imageList= new ArrayList<>();
            for(PeerBean peerBean:groupBean.getPeerBeanList()){
                imageList.add(ImageUtil.getImageResId(peerBean.getUserImageId()));
            }
            int[] imageIds = new int[imageList.size()];
            for(int i=0;i<imageIds.length;i++){
                imageIds[i] = imageList.get(i);
            }
            CombineBitmap.init(itemView.getContext())
                    .setLayoutManager(new WechatLayoutManager()) // 必选， 设置图片的组合形式，支持WechatLayoutManager、DingLayoutManager
                    .setSize(200) // 必选，组合后Bitmap的尺寸，单位dp
                    .setGap(0) // 单个图片之间的距离，单位dp，默认0dp
                    .setResourceIds(imageIds) // 要加载的图片资源id数组
                    .setImageView(mCivUserImage) // 直接设置要显示图片的ImageView
                    // 设置“子图片”的点击事件，需使用setImageView()，index和图片资源数组的索引对应
                    .build();
//            Glide.with(itemView.getContext())
//                    .load(ImageUtil.getImageResId(groupBean.getPeerBeanList().get(0).getUserImageId())).into(mCivUserImage);
            mTvRecentMessage.setText(groupBean.getRecentMessage());
            mTvTime.setText(groupBean.getTime());
            badge.setBadgeNumber(groupBean.getMsgNum());
        }
    }
}
