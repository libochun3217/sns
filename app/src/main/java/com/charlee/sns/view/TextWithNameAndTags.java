package com.charlee.sns.view;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;


import androidx.annotation.NonNull;

import com.charlee.sns.R;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.model.MessageTag;
import com.charlee.sns.model.SnsUser;

import java.util.HashMap;
import java.util.List;

/**
 * 带可点击标签的文字
 */
public class TextWithNameAndTags extends SpannableString {

    public static final char TAG_MARK = '#';

    private final Context context;
    private final SnsUser user;
    private final List<MessageTag> tagList;

    private ClickableSpan userNameClickableSpan = new ClickableSpan() {
        @Override
        public void onClick(View textView) {
            if (user.isOfficial()) {
                return;
            }
            NavigationHelper.navigateToUserProfilePage(context, user);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
            if (user.isOfficial()) {
                ds.setColor(context.getResources().getColor(R.color.sns_pink));
            } else {
                ds.setColor(context.getResources().getColor(R.color.notify_item_text_name));
            }
        }
    };

    public static TextWithNameAndTags create(@NonNull Context context,
                                             @NonNull SnsUser user,
                                             @NonNull CharSequence content,
                                             @NonNull List<MessageTag> tagList) {
        String userName = user.getNickName();
        String text = content.toString();
        String toShow = String.format(context.getString(R.string.msg_detail_comment_format), userName, text);
        int nameStartIndex = toShow.indexOf(userName);
        int nameEndIndex = nameStartIndex + userName.length();
        return new TextWithNameAndTags(context, user, toShow, nameStartIndex, nameEndIndex, tagList);
    }

    private TextWithNameAndTags(@NonNull Context context,
                                @NonNull SnsUser user,
                                @NonNull CharSequence source,
                                int nameStartIndex,
                                int nameEndIndex,
                                @NonNull List<MessageTag> tagList) {
        super(source);
        this.context = context;
        this.user = user;
        this.tagList = tagList;
        init(source, nameStartIndex, nameEndIndex);
    }

    private void init(@NonNull CharSequence source, int nameStartIndex, int nameEndIndex) {
        String text = source.toString();
        for (final MessageTag tag : tagList) {
            String tagWithMarks = TAG_MARK + tag.getName();
            int tagStartIndex = text.indexOf(tagWithMarks);
            if (tagStartIndex == -1) {
                return;
            }

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    HashMap<String, String> queries = new HashMap<>();
                    tag.getNavQuery(queries);
                    NavigationHelper.navigateToPath(context, NavigationHelper.PATH_TAG_MESSAGE_LIST, queries);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };

            setSpan(clickableSpan, tagStartIndex, tagStartIndex + tagWithMarks.length(),
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE); // 包括两边的"#"
        }

        setSpan(userNameClickableSpan, nameStartIndex, nameEndIndex, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }
}
