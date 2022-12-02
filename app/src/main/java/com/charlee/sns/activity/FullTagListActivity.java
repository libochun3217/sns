package com.charlee.sns.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

import com.charlee.sns.R;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.model.ISnsModel;
import com.charlee.sns.model.SnsModel;
import com.charlee.sns.view.FullTagListView;
import com.charlee.sns.view.TopBarLayout;


/**
 * TAG标签列表页面
 */
public class FullTagListActivity extends Activity {

    private ISnsModel model;

    private TopBarLayout topBar;

    private FullTagListView tagListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_full_tag_list);

        model = SnsModel.getInstance();

        initTopBar();

        handleIntent();
    }



    private void initTopBar() {
        tagListView = (FullTagListView) findViewById(R.id.full_tag_list);

        topBar = (TopBarLayout) findViewById(R.id.title_bar);
        topBar.setTitle(R.string.tag_list_page_title);

        topBar.setOnBackClickListener(new TopBarLayout.OnBackClickListener() {
            @Override
            public void onBack() {
                finish();
            }
        });

        topBar.setOnTitleClickListener(new TopBarLayout.OnTitleClickListener() {
            @Override
            public void onTitleClick() {
                // 点击标题栏回到页面顶部
                tagListView.scrollToPosition(0);
            }
        });

    }

    private void handleIntent() {
        NavigationHelper.NavUriParts navUri = NavigationHelper.getValidNavUri(this);
        if (navUri.pathNoLeadingSlash != null) {
            if (navUri.pathNoLeadingSlash.startsWith(NavigationHelper.PATH_FULL_TAG_LIST)) {
                tagListView.setTagList(model.getHotTags());
            }
        }
    }

}
