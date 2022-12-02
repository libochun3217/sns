package com.charlee.sns.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.charlee.sns.R;
import com.charlee.sns.model.Card;
import com.charlee.sns.model.IPageableList;


/**
 * 卡片列表的Adapter，显示不同类型的卡片内容
 */
public class CardListAdapter extends HeaderedListAdapter<Card> {

    public CardListAdapter(@NonNull IPageableList<Card> cards, int headerResId,
                           HeaderViewListener headerViewListener) {
        super(cards, headerResId, headerViewListener);
    }

    @Override
    public void onBindBodyViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Card item = super.getItem(position);

        if (holder instanceof FullMessageViewHolder) {
            ((FullMessageViewHolder) holder).bind(item);
        } else if (holder instanceof UsersCardViewHolderWithList) {
            ((UsersCardViewHolderWithList) holder).bind(item);
        } else if (holder instanceof UsersCardViewHolderWithGrid) {
            ((UsersCardViewHolderWithGrid) holder).bind(item);
        }
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        if (holder instanceof FullMessageViewHolder) {
            ((FullMessageViewHolder) holder).recycle();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateBodyViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case Card.NORMAL_MESSAGE:
            case Card.OFFICIAL_MESSAGE:
            case Card.VIDEO_MESSAGE:
                View v = inflater.inflate(R.layout.item_message_full, parent, false);
                int contentWidth = parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight();
                return new FullMessageViewHolder(v, null, contentWidth);
            case Card.RECOMMEND_LIST:
                View listView = inflater.inflate(R.layout.item_recommend_users_with_list, parent, false);
                return new UsersCardViewHolderWithList(listView);
            case Card.RECOMMEND_GRID:
                View gridView = inflater.inflate(R.layout.item_recommend_users_with_grid, parent, false);
                return new UsersCardViewHolderWithGrid(gridView);
            case Card.FOOTER_CARD:
                View footerView = inflater.inflate(R.layout.item_follow_footer_view, parent, false);
                return new FollowFooterCardViewHolder(footerView);
            default:
                return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return TYPE_HEADER;
        }
        position--;
        return itemList.get(position).getType();
    }
}
