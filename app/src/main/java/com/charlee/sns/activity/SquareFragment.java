package com.charlee.sns.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.charlee.sns.view.PublicSquareView;


/**
 * 关注流页面
 */
public class SquareFragment extends Fragment {
    private static final String PAGE_NAME = "SquareFragment";

    PublicSquareView publicSquareView;

    public static SquareFragment newInstance() {
        SquareFragment fragment = new SquareFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        publicSquareView = new PublicSquareView(getActivity());
        return publicSquareView;
    }

    public void refresh() {
        if (publicSquareView != null) {
            publicSquareView.refresh();
        }
    }

    public void scrollToHead() {
        if (publicSquareView != null) {
            publicSquareView.scrollToPosition(0);
        }
    }

}
