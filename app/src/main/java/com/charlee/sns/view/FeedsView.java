package com.charlee.sns.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.charlee.sns.R;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.helper.ObservableArrayList;
import com.charlee.sns.model.Card;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.Publish;
import com.charlee.sns.model.SnsModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import bolts.Continuation;
import bolts.Task;

/**
 * Feed流的自定义视图，包括正在发布的消息列表和已经发布的消息列表
 */
public class FeedsView extends FrameLayout {
    private Context context;

    private CardListView cardListView;

    private ListView publishListView;
    private PublishListAdapter adapter;

    private OnClickListener placeHolderActionClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            NavigationHelper.navigateToPath(getContext(),
                    NavigationHelper.PATH_SNS_HOME_PUBLIC_SQUARE, null);
        }
    };

    private ObservableArrayList<Publish> publishArray = new ObservableArrayList<>();

    public FeedsView(Context context) {
        super(context);
        init(null, 0);
        this.context = context;
    }

    public FeedsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
        this.context = context;
    }

    public void setCardList(@NonNull final IPageableList<Card> cardList) {
        EmptyPlaceholderView.PlaceHolder placeHolder = new EmptyPlaceholderView.PlaceHolder(
                R.string.hint_feeds_shown_here,
                R.drawable.ic_not_login,
                R.string.action_discover,
                0,
                placeHolderActionClickListener);
        cardListView.setEmptyListPlaceHolder(placeHolder);
        cardListView.setCardList(cardList);

        publishArray = SnsModel.getInstance().getPublishArray();
        publishArray.registerObserver(new ObservableArrayList.DataChangeObserver() {
            @Override
            public void onChanged() {
                update();
                cardListView.updateView(publishArray.size() == 0);
            }
        });

        adapter = new PublishListAdapter();
        publishListView.setAdapter(adapter);
    }

    public void update() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void scrollToPosition(int i) {
        cardListView.scrollToPosition(i);
    }

    public void onResume() {
        if (cardListView != null) {
            cardListView.onResume();
        }
    }

    public void onPause() {
        if (cardListView != null) {
            cardListView.onPause();
        }
    }

    public void addOnScollListener(RecyclerView.OnScrollListener onScrollListener) {
        cardListView.addOnScollListener(onScrollListener);
    }

    public void addFloatButtonShowListener(SnsFloatingActionButton.IShowListener showListener) {
        cardListView.addFloatButtonShowListener(showListener);
    }

    private void init(AttributeSet attrs, int defStyle) {
        View.inflate(getContext(), R.layout.view_feeds, this);
        cardListView = (CardListView) findViewById(R.id.card_list);
        publishListView = (ListView) findViewById(R.id.publish_list);

        setCardList(SnsModel.getInstance().getCardList());
    }

    class PublishListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        @Override
        public int getCount() {
            return publishArray.size();
        }

        @Override
        public Publish getItem(int position) {
            return publishArray.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (mInflater == null) {
                mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }

            PublishViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_publish_view, parent, false);
                viewHolder = new PublishViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (PublishViewHolder) convertView.getTag();
            }

            viewHolder.fillHolder(FeedsView.this, getItem(position));

            return convertView;
        }
    }

    static class PublishViewHolder {
        private ImageView ivImage;
        private TextView tvDescription;

        private ImageView ivRetry;
        private ImageView ivDelete;

        private LinearLayout progressLayout;

        private static DisplayImageOptions smallPhotoOptions = new DisplayImageOptions.Builder()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true)
                .build();

        public PublishViewHolder(View v) {
            ivImage = (ImageView) v.findViewById(R.id.iv_image);
            tvDescription = (TextView) v.findViewById(R.id.tv_description);
            ivRetry = (ImageView) v.findViewById(R.id.btn_retry);
            ivDelete = (ImageView) v.findViewById(R.id.btn_delete);
            progressLayout = (LinearLayout) v.findViewById(R.id.progress_layout);
        }

        public void fillHolder(final FeedsView view, final Publish publish) {
            String uri = Uri.parse(publish.getImageUri()).toString();
            ImageLoader.getInstance().displayImage(uri, ivImage, smallPhotoOptions);
            tvDescription.setText(publish.getDescription(tvDescription.getContext()));

            SnsModel.PublishedState state = publish.getState();
            if (state == SnsModel.PublishedState.FAILED || state == SnsModel.PublishedState.PIC_FORBIDDEN
                    || state == SnsModel.PublishedState.USER_FORBIDDEN) {
                ivRetry.setVisibility(state == SnsModel.PublishedState.FAILED ? VISIBLE : INVISIBLE);
                ivDelete.setVisibility(VISIBLE);
                progressLayout.setVisibility(INVISIBLE);

                ivRetry.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        view.publishArray.remove(publish);
                        publishMessage(publish);
                    }
                });

                ivDelete.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        view.publishArray.remove(publish);
                    }
                });
            } else {
                ivRetry.setVisibility(INVISIBLE);
                ivDelete.setVisibility(INVISIBLE);
                progressLayout.setVisibility(VISIBLE);
            }

        }

        private void publishMessage(final Publish publish) {
            SnsModel.getInstance().getLoginUser().postMessage(publish.getContent(), publish.getImageUri(),
                    publish.getVideoPath(), publish.getWidth(), publish.getHeight(), publish.getCampaignIds())
                    .continueWith(
                            new Continuation<Boolean, Object>() {
                                @Override
                                public Object then(Task<Boolean> task) throws Exception {
                                    return null;
                                }
                            }, Task.UI_THREAD_EXECUTOR);
        }

    }


}
