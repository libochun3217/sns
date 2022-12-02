package com.charlee.sns.helper;

import java.io.IOException;

import com.charlee.sns.R;
import com.charlee.sns.exception.NeedUpgradeException;
import com.charlee.sns.exception.NotLoggedInException;
import com.charlee.sns.exception.RequestFailedException;
import com.charlee.sns.view.EmptyPlaceholderView;
import com.google.gson.JsonParseException;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import bolts.AggregateException;

/**
 * 通用错误处理函数
 */
public class ErrorHandler {

    public interface Callback {
        void onEvent();
    }

    /**
     * 把错误消息显示在EmptyPlaceholderView上
     *
     * @param exception       错误对应的异常
     * @param placeholderView 占位错误提示
     * @param logTag          Log标签
     */
    public static void handleExceptionWithPlaceholder(@NonNull Activity activity,
                                                      @NonNull Exception exception,
                                                      @NonNull EmptyPlaceholderView placeholderView,
                                                      @NonNull String logTag,
                                                      @Nullable final Callback onRefreshCallback) {
        if (exception instanceof AggregateException) { // 多个异常
            // 简化处理。最后一个异常的处理才会被保留
            AggregateException aggregateException = (AggregateException) exception;
            for (Throwable throwable : aggregateException.getInnerThrowables()) {
                if (throwable instanceof Exception) {
                    if (throwable instanceof AggregateException) {
                        handleExceptionWithPlaceholder(
                                activity, (Exception) throwable, placeholderView, logTag, onRefreshCallback);
                    } else {
                        Exception innerException = (Exception) throwable;
                        handleSingleExceptionWithPlaceholder(
                                activity, innerException, placeholderView, logTag, onRefreshCallback);
                    }
                }
            }
        } else {
            handleSingleExceptionWithPlaceholder(activity, exception, placeholderView, logTag, onRefreshCallback);
        }
    }

    private static void handleSingleExceptionWithPlaceholder(@NonNull Activity activity,
                                                      @NonNull Exception exception,
                                                      @NonNull EmptyPlaceholderView placeholderView,
                                                      @NonNull String logTag,
                                                      @Nullable final Callback onRefreshCallback) {
        // Log.e(logTag, exception.getMessage()); <- 会导致异常
        if (exception instanceof IOException) {
            placeholderView.setHintImage(R.drawable.ic_no_net);
            placeholderView.setHintString(R.string.hint_no_network);
            placeholderView.setActionString(R.string.action_refresh);
            placeholderView.setVisibility(View.VISIBLE);
            placeholderView.setActionClickedListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (onRefreshCallback != null) {
                                onRefreshCallback.onEvent();
                            }
                        }
                    }
            );
        } else if (exception instanceof NotLoggedInException) {
            placeholderView.setHintImage(R.drawable.ic_not_login);
            placeholderView.setHintString(R.string.hint_not_logged_in);
            placeholderView.setActionString(R.string.action_login);
            placeholderView.setVisibility(View.VISIBLE);
            NavigationHelper.navigateToLoginPageForResult(activity);
        } else if (exception instanceof RequestFailedException) {
            RequestFailedException requestFailedException = (RequestFailedException) exception;
            int errCode = requestFailedException.getErrCode();
            switch (errCode) {
                case RequestFailedException.ERR_NOT_FOUND:
                    placeholderView.setHintImage(R.drawable.ic_no_photo);
                    placeholderView.setHintString(R.string.hint_request_failed);
                    placeholderView.setActionString(R.string.action_refresh);
                    placeholderView.setVisibility(View.VISIBLE);
                    placeholderView.setActionClickedListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (onRefreshCallback != null) {
                                        onRefreshCallback.onEvent();
                                    }
                                }
                            }
                    );
                    break;
                default:
                    break;
            }
        } else if (exception instanceof JsonParseException) {
            placeholderView.setHintImage(R.drawable.ic_no_photo);
            placeholderView.setHintString(R.string.hint_request_failed);
            placeholderView.setActionString(R.string.action_refresh);
            placeholderView.setVisibility(View.VISIBLE);
            placeholderView.setActionClickedListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (onRefreshCallback != null) {
                                onRefreshCallback.onEvent();
                            }
                        }
                    }
            );
        }
    }

    /**
     * 以Toast或者对话框形式提示错误信息。
     *
     * @param context
     * @param exception 错误对应的异常
     * @param logTag    Log标签
     *
     * @return 错误消息是否已经提示
     */
    public static boolean showError(@NonNull Context context, @NonNull Exception exception, @NonNull String logTag) {
        // Log.e(logTag, exception.getMessage()); <- 会导致异常
        if (exception instanceof IOException) {
            Toast toast = Toast.makeText(context,
                    R.string.hint_no_network, Toast.LENGTH_SHORT);
            toast.show();
            return true;
        } else if (exception instanceof NotLoggedInException) {
            Toast toast = Toast.makeText(context,
                    R.string.hint_not_logged_in, Toast.LENGTH_SHORT);
            toast.show();
            return true;
        } else if (exception instanceof NeedUpgradeException) {
            Toast toast = Toast.makeText(context,
                    R.string.hint_need_upgrade, Toast.LENGTH_SHORT);
            toast.show();
            return true;
        }

        return false;
    }
}
