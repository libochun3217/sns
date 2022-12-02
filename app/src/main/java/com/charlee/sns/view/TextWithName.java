package com.charlee.sns.view;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.charlee.sns.R;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.model.SnsUser;


/**
 */
public class TextWithName extends SpannableString {
    private static final String TAG_MARK = "@";

    private final Context context;

    public static TextWithName create(@NonNull Context context, @NonNull SnsUser user,
                                      @NonNull CharSequence content, @Nullable SnsUser fatherUser) {
        String userName = user.getNickName();
        String text = content.toString();

        String toShow = null;
        if (fatherUser == null) {
            toShow = String.format(context.getString(R.string.msg_detail_comment_format), userName, text);
        } else {
            toShow = String.format(context.getString(R.string.msg_detail_comment_format_with_father),
                    userName, fatherUser.getNickName(), text);
        }

        return new TextWithName(context, user, fatherUser, toShow);
    }

    private TextWithName(@NonNull Context context, @NonNull SnsUser user, @Nullable SnsUser fatherUser,
                         @NonNull String fullText) {
        super(fullText);
        this.context = context;

        spanText(user, fullText, null);
        if (fatherUser != null) {
            spanText(fatherUser, fullText, TAG_MARK);
        }

    }

    private void spanText(final SnsUser user, @NonNull String fullText, String tagMark) {
        String userName = user.getNickName();
        if (tagMark != null) {
            userName = tagMark + userName;
        }

        int nameStartIndex = fullText.indexOf(userName);
        int nameEndIndex = nameStartIndex + userName.length();

        ClickableSpan clickableSpan = new ClickableSpan() {
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
                if (user.isOfficial()) {
                    ds.setColor(context.getResources().getColor(R.color.sns_pink));
                } else {
                    ds.setColor(context.getResources().getColor(R.color.notify_item_text_name));
                }
                ds.setUnderlineText(false);
            }
        };

        setSpan(clickableSpan, nameStartIndex, nameEndIndex, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }

}
