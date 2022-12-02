package com.charlee.sns.helper;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.charlee.sns.R;
import com.charlee.sns.manager.SnsEnvController;
import com.charlee.sns.model.SnsCampaign;
import com.charlee.sns.model.SnsUser;
import com.facebook.animated.gif.BuildConfig;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

/**
 * 负责社区内部的跳转
 * Query除了列表中的定位（pos）外都需要从模型中（通过getNavQuery）获取。
 */
public class NavigationHelper {
    // region Private Fields

    private static final String URI_SCHEME = "photowonder";
    private static final String URI_HOST = "motu.baidu.com";

    // endregion

    // region Public Constants

    public static final String PATH_SNS_HOME = "sns";
    public static final String PATH_SNS_HOME_PUBLIC_SQUARE = PATH_SNS_HOME + "/public_square";
    public static final String PATH_SNS_HOME_FEEDS = PATH_SNS_HOME + "/feeds";
    public static final String PATH_MOTU_HOME = "home";

    /**
     * 通用参数
     */
    public static final String QUERY_POS = "pos"; // 传递列表中的位置（当前项的位置）

    /**
     * 图片详情列表页面（FullMessageListActivity）支持的路径
     */
    // 最新消息。无需对象查询参数。支持QUERY_POS。
    public static final String PATH_SNS_LATEST_MSGS_FULL = "latest_msgs";
    // 热门消息。无需对象查询参数。支持QUERY_POS。
    public static final String PATH_SNS_HOT_MSGS_FULL = "hot_msgs";
    // 用户发布的消息。用SnsUser作为对象查询参数。支持QUERY_POS。
    public static final String PATH_SNS_USER_MSGS = "user_msgs";
    // 一个活动包含的消息，用SnsCampaign作为对象查询参数。支持QUERY_POS
    public static final String PATH_SNS_CAMPAIGN_MSGS = "campaign_msgs";
    // 含有对应标签的消息。用MessageTag作为对象查询参数。支持QUERY_POS。
    public static final String PATH_SNS_TAG_MSGS = "tag_msgs";
    // ！！！！！注意：此路径不可随意修改，需要与服务器轮播图跳转到消息的路径保持一致！！！！！
    public static final String PATH_SNS_MESSAGE_DETAILS = "message_details";

    public static final String PATH_SNS_USER_DETAILS = "user_details";
    public static final String PATH_SNS_USER_BASIC_INFO = "user_basic_info";
    public static final String PATH_SNS_USER_DETAILS_SELF_MODE = "user_details_selfmode";
    public static final String PATH_SNS_NOTIFICATION_CENTER = "user_notification_center";

    public static final String PATH_SNS_LOGIN = "sns_login";

    public static final String PATH_FULL_TAG_LIST = "full_tag_list";
    public static final String PATH_FIND_FRIENDS = "find_friends";

    /**
     * 图片列表页面（SimpleMessageListActivity）支持的路径
     */
    public static final String PATH_TAG_MESSAGE_LIST = "tag_message_list";
    public static final String PATH_LATEST_MESSAGE_LIST = "latest_message_list";

    /**
     * 用户列表页面（UserListActivity）支持的路径
     */
    public static final String PATH_FOLLOWER_USER_LIST = "follower_user_list";
    public static final String PATH_FOLLOWEE_USER_LIST = "followee_user_list";

    /**
     * 运营活动详情页(CampaignDetailsActivity)支持的路径
     */
    public static final String PATH_CAMPAIGN_DETAILS = "campaign_details";

    /**
     * 运营活动历史页面(CampaignHistoryActivity)支持的路径
     */
    public static final String PATH_CAMPAIGN_HISTORY = "campaign_history";

    /**
     * 运营活动选择参加方式页面(SelectParticipateWayActivity)支持的路径
     */
    public static final String PATH_CAMPAIGN_SELECT_PARTICIPATE_WAY = "select_participate_way";
    public static final String BUNDLE_CAMPAIGN_TO_TYPE = "campaign_to_type";
    public static final String BUNDLE_CAMPAIGN_ID = "campaign_id";

    /**
     * 系统公告板页面（SystemNoticeActivity）支持的路径
     */
    public static final String PATH_SYSTEM_NOTICE = "system_notice";

    /**
     * 用户头像编辑页面（）支持的路径
     */
    public static final String PATH_USER_PORTRAIT_EDIT = "user_portrait_edit";
    public static final String BUNDLE_PORTRAIT_URI = "portrait_uri";
    public static final String PATH_USER_NICKNAME_EDIT = "user_nickname_edit";

    public static final String PATH_BROWSE_MESSAGE_COMMENT = "browse_message_comment";
    public static final String PATH_ADD_MESSAGE_COMMENT = "add_message_comment";

    // 跳转到主工程的URL
    public static final String PATH_MAIN_PROJ_IMAGE_PICKER = "jigsaw";
    public static final String PATH_MAIN_PROJ_FILTER_CAMERA = "filtercamera";
    public static final String PATH_MAIN_PROJ_CAMPAIGN_RULES = "campaign_rules";
    public static final String PATH_MAIN_PROJ_EFFECT_VIDEO = "effect_video";

    // 主工程结果页面需要的常量
    public static final String PATH_SETTING_PAGE = "setting_page"; // 设置页面
    public static final String PATH_RESULT_PAGE = "result_page"; // 魔图结果页（分享）
    public static final String BUNDLE_SHARE_URI = "share_uri"; // 对应ResultPageActivity.SHARE_URI
    public static final String BUNDLE_SHARE_URL = "share_url"; // 对应ResultPageActivity.SHARE_URL
    public static final String BUNDLE_SHARE_URL_TITLE = "share_url_title";
    public static final String BUNDLE_SHARE_URL_DESCRIPTION = "share_url_description";
    public static final String BUNDLE_SHARE_CARD_ENABLED = "sns_share_card_enabled";
    public static final String BUNDLE_FROM = "activity_enter";
    public static final int BUNDLE_FROM_SNS = 9;

    // 登录对话框
    public static final String BUNDLE_LOGIN_DIALOG_MEMO = "login_dialog_memo";
    public static final String BUNDLE_ANONYMOUS_DIRECTLY = "login_anonymous_directly";
    public static final String BUNDLE_NEED_LOGIN = "login_need_login";
    public static final String BUNDLE_FROM_HOMEPAGE = "from_homepage";

    // 推送相关的参数
    // UriRouterUtil.EXTRA_IS_FROM_NOTIFICATION
    public static final String EXTRA_IS_FROM_NOTIFICATION = "extra_from_notification";

    // endregion

    // region Request Code

    public static final int LOGIN_REQUEST_CODE = 600;
    public static final int USER_DETAILS_REQUEST_CODE = 700;
    public static final int USER_PUBLISHED_MESSAGE_REQUEST_CODE = 800;
    public static final int USER_BASIC_INFO_CODE = 900;
    public static final int CAMPAIGN_RULES_CODE = 1000;

    // endregion

    // region Public Types

    public static class NavUriParts {
        public Uri uri;                     // Uri
        public String pathNoLeadingSlash;   // 路径去掉开头的'/'，不为null则uri也不为null
    }

    // endregion

    // region Public Static Methods

    /**
     * 取得scheme为URI_SCHEME并且authority为URI_HOST的Uri，并且取得去掉开头'/'的路径
     * @param activity      需要获取Uri的Activity
     * @return              符合条件的Uri
     */
    @NonNull
    public static NavUriParts getValidNavUri(@NonNull Activity activity) {
        NavUriParts parts = new NavUriParts();

        Intent intent = activity.getIntent();
        if (intent == null) {
            return parts;
        }

        String action = intent.getAction();
        if (action == null || !action.equals(Intent.ACTION_VIEW)) {
            return parts;
        }

        Uri uri = intent.getData();
        if (uri != null) {
            if (uri.getScheme().equals(URI_SCHEME) && uri.getAuthority().equals(URI_HOST)) {
                parts.uri = uri;
                String uriPath = uri.getPath();
                if (uriPath != null && !uriPath.isEmpty()) {
                    if (uriPath.startsWith("/")) {
                        uriPath = uriPath.substring(1); // 去掉开头的'/'
                    }

                    parts.pathNoLeadingSlash = uriPath;
                }
            }
        }

        return parts;
    }

    public static void navigateToPath(@NonNull Context context,
                               @NonNull String path,
                               @Nullable Map<String, String> queries) {
        Intent intent = new Intent(Intent.ACTION_VIEW, buildUri(path, queries));
        if (path.equals(PATH_SNS_HOME_FEEDS) || path.equals(PATH_SNS_HOME_PUBLIC_SQUARE)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public static void navigateToPath(@NonNull Context context,
                               @NonNull String path,
                               @Nullable Map<String, String> queries,
                               @Nullable Bundle bundle) {
        Intent intent = new Intent(Intent.ACTION_VIEW, buildUri(path, queries));
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void navigateToPathForResult(@NonNull Context context, int requestCode,
                                        @NonNull String path, @Nullable Map<String, String> queries) {
        Intent intent = new Intent(Intent.ACTION_VIEW, buildUri(path, queries));
        Activity activity = getActivity(context);
        if (activity != null) {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    public static void navigateToPathForResult(@NonNull Context context,  int requestCode,
                                      @NonNull String path,
                                      @Nullable Map<String, String> queries,
                                      @Nullable Bundle bundle) {
        Intent intent = new Intent(Intent.ACTION_VIEW, buildUri(path, queries));
        if (bundle != null) {
            intent.putExtras(bundle);
        }

        Activity activity = getActivity(context);
        if (activity != null) {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    // 以下函数为方便跳转到各个页面的帮助函数
    public static void navigateToUserProfilePage(@NonNull Context context, @NonNull SnsUser snsUser) {
        HashMap<String, String> queries = new HashMap<>();
        snsUser.getNavQuery(queries);
        navigateToPath(context, NavigationHelper.PATH_SNS_USER_DETAILS, queries);
    }

    public static void navigateToFolloweeListPage(@NonNull Context context, @NonNull SnsUser snsUser) {
        HashMap<String, String> queries = new HashMap<>();
        snsUser.getNavQuery(queries);
        navigateToPath(context, NavigationHelper.PATH_FOLLOWEE_USER_LIST, queries);
    }

    public static void navigateToFollowerListPage(@NonNull Context context, @NonNull SnsUser snsUser) {
        HashMap<String, String> queries = new HashMap<>();
        snsUser.getNavQuery(queries);
        navigateToPath(context, NavigationHelper.PATH_FOLLOWER_USER_LIST, queries);
    }

    public static void navigateToUserProfilePageForResult(@NonNull Activity activity, @NonNull SnsUser snsUser) {
        HashMap<String, String> queries = new HashMap<>();
        snsUser.getNavQuery(queries);
        navigateToPathForResult(activity,
                USER_DETAILS_REQUEST_CODE, NavigationHelper.PATH_SNS_USER_DETAILS, queries);
    }

    public static void navigateToUserCenterPage(@NonNull Context context) {
        HashMap<String, String> queries = new HashMap<String, String>();
        navigateToPath(context, NavigationHelper.PATH_SNS_USER_DETAILS_SELF_MODE, queries);
    }

    public static void navigateToUserBasicInfoPageForResult(@NonNull Context context) {
        HashMap<String, String> queries = new HashMap<String, String>();
        navigateToPathForResult(context, NavigationHelper.USER_BASIC_INFO_CODE,
                NavigationHelper.PATH_SNS_USER_BASIC_INFO, queries, null);
    }

    /**
     * 导航到登录对话框
     *
     * @param context 发起导航的Activity，需要实现onActivityResult方法，请求码为LOGIN_REQUEST_CODE
     */
    public static void navigateToLoginPageForResult(@NonNull Context context) {
        navigateToLoginPageForResult(context, false, false);
    }

    public static void navigateToLoginPageForResult(@NonNull Context context, boolean anonymousDirectly,
                                                    boolean needLogin) {
        if (NetworkMonitor.getInstance(context).isNetworkAvailable()) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(BUNDLE_ANONYMOUS_DIRECTLY, anonymousDirectly);
            bundle.putBoolean(BUNDLE_NEED_LOGIN, needLogin);
            bundle.putBoolean(BUNDLE_FROM_HOMEPAGE, isHomepage(context));
            navigateToPathForResult(
                    context,
                    NavigationHelper.LOGIN_REQUEST_CODE,
                    NavigationHelper.PATH_SNS_LOGIN, null, bundle);
        } else {
            Toast.makeText(context, R.string.hint_no_network, Toast.LENGTH_SHORT).show();
        }
    }

    public static void navigateToUserNotificationCenterPage(@NonNull Context context) {
        HashMap<String, String> queries = new HashMap<String, String>();
        navigateToPath(context, NavigationHelper.PATH_SNS_NOTIFICATION_CENTER, queries);
    }

    public static void navigateToShareUrlPage(@NonNull Context context,
                                              @NonNull String urlToShare, @NonNull String imageUri,
                                              @NonNull String shareTitle, @NonNull String shareDesc) {
        HashMap<String, String> queries = new HashMap<>();
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_SHARE_URI, imageUri);
        bundle.putString(BUNDLE_SHARE_URL, urlToShare);
        bundle.putString(BUNDLE_SHARE_URL_TITLE, shareTitle);
        bundle.putString(BUNDLE_SHARE_URL_DESCRIPTION, shareDesc);
        bundle.putBoolean(BUNDLE_SHARE_CARD_ENABLED, false);
        bundle.putInt(BUNDLE_FROM, BUNDLE_FROM_SNS);

        navigateToPath(context, NavigationHelper.PATH_RESULT_PAGE, queries, bundle);
    }

    public static void navigateToSelectCampaignParticipateWayPage(@NonNull Context context,
                                                                  @NonNull String campaignId, int toType) {
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_CAMPAIGN_ID, campaignId);
        bundle.putInt(BUNDLE_CAMPAIGN_TO_TYPE, toType);

        navigateToPath(context, NavigationHelper.PATH_CAMPAIGN_SELECT_PARTICIPATE_WAY, null, bundle);
    }

    public static void navigateToSystemNoticePage(@NonNull Context context, @NonNull String notice) {
        HashMap<String, String> queries = new HashMap<>();
        queries.put("id", notice);

        navigateToPath(context, NavigationHelper.PATH_SYSTEM_NOTICE, queries);
    }

    public static void navigateToCampaignDetailsPage(@NonNull Context context, @NonNull String campaignId) {
        HashMap<String, String> queries = new HashMap<>();
        queries.put("id", campaignId);
        NavigationHelper.navigateToPath(context, NavigationHelper.PATH_CAMPAIGN_DETAILS, queries);
    }

    public static void navigateToPortraitEditPage(@NonNull Context context, @NonNull String uri) {
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_PORTRAIT_URI, uri);

        navigateToPath(context, NavigationHelper.PATH_USER_PORTRAIT_EDIT, null, bundle);
    }

    public static void navigateToMainProjectCameraPage(@NonNull Context context) {
        HashMap<String, String> queries = new HashMap<>();
        Bundle bundle = new Bundle();
        // ResultPageActivity.ACTIVITY_ENTER: ResultPageActivity.ENTER_SNS_CAMERA
        bundle.putInt("activity_enter", 10);
        navigateToPath(context, NavigationHelper.PATH_MAIN_PROJ_FILTER_CAMERA, queries, bundle);
    }

    public static void navigateToMainProjectPickPictureThenBeautify(@NonNull Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, buildUri(NavigationHelper.PATH_MAIN_PROJ_IMAGE_PICKER, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("is_pick_mode", true);
        // ResultPageActivity.ACTIVITY_ENTER: ResultPageActivity.ENTER_SNS_BEAUTIFY
        intent.putExtra("activity_enter", 11);
        context.getApplicationContext().startActivity(intent);
    }

    public static void navigateToMainProjectMV(@NonNull Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, buildUri(NavigationHelper.PATH_MAIN_PROJ_EFFECT_VIDEO, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // ResultPageActivity.ACTIVITY_ENTER: ResultPageActivity.ENTER_SNS_MV
        intent.putExtra("activity_enter", 13);
        context.getApplicationContext().startActivity(intent);
    }

    public static void navigateToMainProjectPickPicturePage(@NonNull Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, buildUri(NavigationHelper.PATH_MAIN_PROJ_IMAGE_PICKER, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // ImagePickerActivity.IS_PICK_MODE
        intent.putExtra("is_pick_mode", true);
        // ImagePickerActivity.CALL_TYPE_PICK_MODE: ImagePickerActivity.CALL_TYPE_PICK_MODE_FOR_SHARE
        intent.putExtra("call_type_pick", -2);
        // ResultPageActivity.ACTIVITY_ENTER: ResultPageActivity.ENTER_SNS_ALBUM
        intent.putExtra("activity_enter", 9);
        context.getApplicationContext().startActivity(intent);
    }

    public static void navigateToMainProjectPickPicturePageForCampaign(@NonNull Context context,
                                                                       @NonNull String campaignId) {
        Intent intent = new Intent(Intent.ACTION_VIEW, buildUri(NavigationHelper.PATH_MAIN_PROJ_IMAGE_PICKER, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // ImagePickerActivity.IS_PICK_MODE
        intent.putExtra("is_pick_mode", true);
        // ImagePickerActivity.CALL_TYPE_PICK_MODE: ImagePickerActivity.CALL_TYPE_PICK_MODE_FOR_SHARE
        intent.putExtra("call_type_pick", -2);
        intent.putExtra(BUNDLE_CAMPAIGN_ID, campaignId);
        // ResultPageActivity.ACTIVITY_ENTER: ResultPageActivity.ENTER_SNS_ALBUM
        intent.putExtra("activity_enter", 9);
        context.getApplicationContext().startActivity(intent);
    }

    public static void navigateToMainProjectPickPicturePageForPortraitEdit(@NonNull Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, buildUri(NavigationHelper.PATH_MAIN_PROJ_IMAGE_PICKER, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // ImagePickerActivity.IS_PICK_MODE
        intent.putExtra("is_pick_mode", true);
        // ImagePickerActivity.CALL_TYPE_PICK_MODE: ImagePickerActivity.CALL_TYPE_PICK_MODE_FOR_PORTRAIT_EDIT
        intent.putExtra("call_type_pick", -3);
        context.getApplicationContext().startActivity(intent);
    }

    public static void navigateToMainProjectRecommandSpPageForResult(@NonNull Activity activity,
                                                                     @NonNull SnsCampaign campaign,
                                                                     boolean isOver) {
        Intent intent = new Intent(Intent.ACTION_VIEW, buildUri(NavigationHelper.PATH_MAIN_PROJ_CAMPAIGN_RULES, null));
        // RecommandSPActivity.EXTRA_SP_RECOMMEND_TITLE
        intent.putExtra("sp_recommend_title", campaign.getTitle());
        // RecommandSPActivity.EXTRA_SP_RECOMMEND_URL
        intent.putExtra("sp_recommand_url", campaign.getUrl());
        // RecommandSPActivity.EXTRA_SP_RECOMMEND_TYPE
        intent.putExtra("sp_recommend_type", 2);
        // RecommandSPActivity.EXTRA_SP_RECOMMEND_CAMPAIGN_ID
        intent.putExtra("sp_recommend_campaign_id", isOver ? null : campaign.getId());
        activity.startActivityForResult(intent, CAMPAIGN_RULES_CODE);
    }

    public static void navigateToHomePage(@NonNull Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, buildUri(PATH_MOTU_HOME, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static void navigateToUserScenario(Context context, int toType, int materialId, String campaignId) {
        if (toType == 0) {
            return;
        }
        SnsEnvController.getInstance().getNavigationBridge().navigateToUserScenario(
                context, toType, materialId, campaignId);
    }

    // endregion

    // region Private Helper Methods

    private static Uri buildUri(@NonNull String path, @Nullable Map<String, String> queries) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(URI_SCHEME).authority(URI_HOST).appendPath(path);
        if (queries != null && queries.size() > 0) {
            for (Map.Entry<String, String> entry : queries.entrySet()) {
                builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }
        }

        return builder.build();
    }

    // 检测是否为首页
    // 为满足一个特殊需求，当从首页的Follow页面发起登录请求后，不刷新Follow页面
    private static boolean isHomepage(Context context) {
        Activity activity = getActivity(context);
        // WelcomeActivity.WELCOME_FRAGMENT_TAG
        if (activity != null && activity instanceof FragmentActivity) {
            return ((FragmentActivity) activity).getSupportFragmentManager().findFragmentByTag("follow_fragment_tag") != null;
        }
        return false;
    }

    // 从Context获取对应的Activity，否则可能会导致ClassCastException:
    // android.support.v7.widget.* cannot be cast to android.app.Activity
    private static Activity getActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }

            context = ((ContextWrapper) context).getBaseContext();
        }

        if (BuildConfig.DEBUG) {
            throw new InvalidParameterException("Cannot get activity from the context!");
        }

        return null;
    }

    // endregion
}
