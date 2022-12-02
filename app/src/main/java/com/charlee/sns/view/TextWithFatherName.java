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
import com.charlee.sns.model.SnsUser;


/**
 */
public class TextWithFatherName extends SpannableString {
    private static final String TAG_MARK = "@";
    private final Context context;

    public static TextWithFatherName create(@NonNull Context context, @NonNull SnsUser fatherUser,
                                            @NonNull CharSequence content) {
        String toShow = TAG_MARK + String.format(context.getString(R.string.comment_content_format_with_father),
                fatherUser.getNickName(), content.toString());
        return new TextWithFatherName(context, fatherUser, toShow);
    }

    private TextWithFatherName(@NonNull Context context, @NonNull SnsUser fatherUser, @NonNull String fullText) {
        super(fullText);
        this.context = context;
        spanText(fatherUser, fullText, TAG_MARK);
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
