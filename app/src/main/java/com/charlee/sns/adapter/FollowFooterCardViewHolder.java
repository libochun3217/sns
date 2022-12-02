package com.charlee.sns.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.charlee.sns.R;
import com.charlee.sns.helper.NavigationHelper;


/**
 * 关注页面的FooterCardViewHolder
 */
public class FollowFooterCardViewHolder extends RecyclerView.ViewHolder {

    public FollowFooterCardViewHolder(@NonNull final View container) {
        super(container);
        container.findViewById(R.id.btn_switch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationHelper.navigateToPath(container.getContext(),
                        NavigationHelper.PATH_SNS_HOME_PUBLIC_SQUARE, null);
            }
        });
    }

}
