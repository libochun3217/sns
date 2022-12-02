package com.charlee.sns.adapter;

import android.view.View;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.charlee.sns.R;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.model.Card;
import com.charlee.sns.model.ICollectionObserver;
import com.charlee.sns.model.IObservable;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.ISnsModel;
import com.charlee.sns.model.SnsModel;
import com.charlee.sns.model.SnsUser;
import com.charlee.sns.storage.Storage;
import com.charlee.sns.view.RecommendUsersCardView;

import java.util.List;


/**
 * 卡片ViewHolder
 */
public class UsersCardViewHolderWithList extends RecyclerView.ViewHolder {
    private ISnsModel model;
    private IPageableList<SnsUser> hotUsers;
    private Card card;

    private View container;
    private RecommendUsersCardView cardView;

    private ICollectionObserver observer = new ICollectionObserver() {
        @Override
        public void update(IObservable<ICollectionObserver> observable,
                           Action action, Object item, List<Object> range) {
            IPageableList<SnsUser> userList = (IPageableList<SnsUser>) observable;
            if (userList != null) {
                if (userList.size() == 0) {
                    card.setFiltered();
                }
            }
        }
    };

    public UsersCardViewHolderWithList(@NonNull final View container) {
        super(container);
        this.container = container;
        cardView = (RecommendUsersCardView) container.findViewById(R.id.recommend_users);
        container.setVisibility(View.GONE);

        container.findViewById(R.id.txt_close_card_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                card.setFiltered();
                container.setVisibility(View.GONE);
                Storage.getInstance().setRecommendShowTime(System.currentTimeMillis());
            }
        });
        container.findViewById(R.id.txt_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationHelper.navigateToPath(container.getContext(), NavigationHelper.PATH_FIND_FRIENDS, null);
            }
        });

        model = SnsModel.getInstance();
    }

    public void bind(@NonNull final Card item) {
        card = item;

        hotUsers = model.getHotUsers();
        if (hotUsers.size() == 0) {
            container.setVisibility(View.GONE);
            card.setFiltered();
            refreshData();
            return;
        } else {
            container.setVisibility(View.VISIBLE);
        }

        if (cardView != null) {
            cardView.setSnsUser(hotUsers, card.getType());
            hotUsers.addObserver(observer);
        }
    }

    public void recycle() {

    }

    private void refreshData() {
        try {
            hotUsers.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
