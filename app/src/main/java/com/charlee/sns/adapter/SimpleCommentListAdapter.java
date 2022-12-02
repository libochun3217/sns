package com.charlee.sns.adapter;

import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.charlee.sns.R;
import com.charlee.sns.helper.NavigationHelper;
import com.charlee.sns.helper.ReportHelper;
import com.charlee.sns.model.Comment;
import com.charlee.sns.model.ICollectionObserver;
import com.charlee.sns.model.SnsUser;
import com.charlee.sns.model.UserMessage;
import com.charlee.sns.view.TextWithName;

import java.util.HashMap;
import java.util.List;

/**
 * 用于图片详情列表中的用户评论列表
 */
public class SimpleCommentListAdapter
        extends PageableListAdapter<Comment, SimpleCommentListAdapter.SimpleCommentViewHolder> {

    private static final int MAX_COMMENTS = 3; // 最多显示三条评论
    private static final int TEXTVIEW_MAX_LINES = 2;

    private UserMessage userMessage;

    private int displayNum;

    public static class SimpleCommentViewHolder extends RecyclerView.ViewHolder {

        private TextView txtComment;
        private Comment comment;

        public SimpleCommentViewHolder(View itemView) {
            super(itemView);
            txtComment = (TextView) itemView.findViewById(R.id.txt_comment);
            txtComment.setMovementMethod(LinkMovementMethod.getInstance());
            txtComment.setMaxLines(TEXTVIEW_MAX_LINES);
        }

        public void bind(Comment comment, final UserMessage userMessage) {
            this.comment = comment;
            setText(txtComment, comment.getUser(), comment.getContent(), true);
            txtComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HashMap<String, String> queries = new HashMap<>();
                    userMessage.getNavQuery(queries);
                    NavigationHelper.navigateToPath(v.getContext(),
                            NavigationHelper.PATH_BROWSE_MESSAGE_COMMENT, queries);

                    ReportHelper.showCommentPage(txtComment.getContext(), userMessage.getId());
                }
            });
        }

        private void setText(@NonNull final TextView textView, @NonNull final SnsUser user,
                             @NonNull final String content, boolean needCheckEllipsize) {
            final TextWithName text = TextWithName.create(this.txtComment.getContext(), user, content,
                    comment.getFatherUser());
            textView.setText(text);
            if (needCheckEllipsize) {
                textView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        textView.getViewTreeObserver().removeOnPreDrawListener(this);
                        if (TEXTVIEW_MAX_LINES <= 1) {
                            return true;
                        }
                        int lineCount = textView.getLineCount();
                        if (lineCount > TEXTVIEW_MAX_LINES) {
                            if (textView.getLayout().getLineWidth(TEXTVIEW_MAX_LINES - 1) * 2
                                    < textView.getWidth()) {
                                return true;
                            }

                            // emoji表情占4个字节，所以当用字符串"..."截断的时候，需要以四字节对齐，否则会有乱码
                            int lineEndIndex = textView.getLayout().getLineEnd(TEXTVIEW_MAX_LINES - 1);
                            lineEndIndex -= 3;
                            lineEndIndex -= 1;

                            String textString = text.toString();
                            int indexCommentStart = textString.indexOf(content);
                            if (indexCommentStart > -1
                                    && content.length() > 4
                                    && indexCommentStart < lineEndIndex
                                    && (lineEndIndex + 4) < textString.length()) {
                                String commentModified = textString.substring(indexCommentStart, lineEndIndex) + "...";
                                setText(textView, user, commentModified, false);
                                textView.invalidate();
                            }
                        }
                        return true;
                    }
                });
            }
        }
    }

    public SimpleCommentListAdapter(UserMessage userMessage) {
        super(userMessage.getComments());
        this.userMessage = userMessage;
        initDisplayNum();
    }

    private void initDisplayNum() {
        displayNum = itemList.size();
        if (displayNum > MAX_COMMENTS) {
            displayNum = MAX_COMMENTS;
        }
    }

    @Override
    public void onBindViewHolder(final SimpleCommentViewHolder holder, int position) {
        Comment item = itemList.get(position);
        holder.bind(item, userMessage);
    }

    @Override
    public int getItemCount() {
        return displayNum;
    }

    @Override
    protected boolean onListChanged(final ICollectionObserver.Action action,
                                    final Comment item, final List<Object> range) {
        initDisplayNum();
        // 总是全部刷新，以保证状态正确。否则RecyclerView可能抛出异常：
        //       IndexOutOfBoundsException: Inconsistency detected
        notifyDataSetChanged();
        return true;
    }

    @Override
    public SimpleCommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_simple, parent, false);
        return new SimpleCommentViewHolder(v);
    }
}
