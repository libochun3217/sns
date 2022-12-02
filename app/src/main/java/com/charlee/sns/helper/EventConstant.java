package com.charlee.sns.helper;

/**
 */
public class EventConstant {

    public static final String SEPARATOR = "-"; // 分隔符

    /**
     * 登录数据&对应转化率
     */
    public static final String EVENT_LOGIN_PAGE = "社区登录面板展示量";
    public static final String EVENT_FACEBOOK_LOGIN = "Facebook登录点击量";
    public static final String EVENT_WECHAT_LOGIN = "WeChat登录点击量";
    public static final String EVENT_QQ_LOGIN = "QQ登录点击量";
    public static final String EVENT_DEBUG_LOGIN = "调试登录点击量";
    public static final String EVENT_ANONYMOUS_LOGIN = "匿名登录点击量";
    public static final String EVENT_KAKAO_LOGIN = "KAKAO登录点击量";
    public static final String EVENT_LOGIN_SUCCESS = "社区登录成功";
    public static final String LABEL_USER_PAGE = "个人页-登录面板";
    public static final String LABEL_NOTIFICATION_PAGE = "通知中心-登录面板";
    public static final String LABEL_RESULT_PAGE = "照片结果页-登录面板";
    public static final String LABEL_COMMENT_PAGE = "评论-登录面板";
    public static final String LABEL_LIKE_PAGE = "赞-登录面板";
    public static final String LABEL_FOLLOW_PAGE = "关注-登录面板";

    /**
     * 图片发送相关
     */
    public static final String EVENT_PHOTO_SOURCE = "社区图片来源";
    public static final String LABEL_CAMERA = "发送按钮-拍照";
    public static final String LABEL_BEAUTY = "发送按钮-美化";
    public static final String LABEL_ALBUM = "发送按钮-相册";
    public static final String LABEL_OTHERS = "发送按钮-其他";
    public static final String LABEL_FLOAT_BTN_CAMERA = "浮动按钮-拍照";
    public static final String LABEL_FLOAT_BTN_BEAUTY = "浮动按钮-视频";
    public static final String LABEL_FLOAT_BTN_ALBUM = "浮动按钮-相册";

    public static final String EVENT_MV_ENTER = "社区视频入口";
    public static final String LABEL_MV_ENTER_SNS = "社区入口";
    public static final String LABEL_MV_ENTER_WELCOME = "首页入口";

    public static final String EVENT_MV_SOURCE = "社区视频来源";
    public static final String LABEL_MV_SEND_SNS = "发送按钮-社区";
    public static final String LABEL_MV_SEND_WELCOME = "发送按钮-首页";

    /**
     * 短视频相关
     */
    public static final String EVENT_MV_UPLOAD = "社区MV上传";
    public static final String LABEL_MV_S3_FAIL = "S3上传失败";
    public static final int LABEL_TIME_1 = 8;
    public static final int LABEL_TIME_2 = 15;
    public static final int LABEL_TIME_3 = 30;
    public static final String LABEL_UPLOAD_TIME = "S内上传完成";
    public static final String LABEL_UPLOAD_TIME_OTHER = "其他时间段上传完成";
    public static final String LABEL_MV_UPLOAD_SUCCESS = "视频发布成功";
    public static final String LABEL_MV_UPLOAD_FAIL = "视频发布失败";

    public static String getUploadLabel(long uploadTime) {
        long seconds = uploadTime / 1000;
        if (seconds < LABEL_TIME_1) {
            return LABEL_TIME_1 + EventConstant.LABEL_UPLOAD_TIME;
        } else if (seconds < LABEL_TIME_2) {
            return LABEL_TIME_1 + EventConstant.LABEL_UPLOAD_TIME;
        } else if (seconds < LABEL_TIME_3) {
            return LABEL_TIME_3 + EventConstant.LABEL_UPLOAD_TIME;
        } else {
            return EventConstant.LABEL_UPLOAD_TIME_OTHER;
        }
    }

    /**
     * 入口点击
     */
    public static final String EVENT_FUNCTION_CLICK = "社区功能按钮点击";
    public static final String LABEL_SNS_HOME_TAB_CLICK = "首页TAB点击";
    public static final String LABEL_SQUARE_MORE = "发现页-更多";
    public static final String LABEL_HOT_TAGS_MORE = "热门标签页-更多";
    public static final String LABEL_HOME_BUTTON = "【首页】按钮点击";

    /**
     * 页面展示量
     */
    public static final String EVENT_PAGE_SHOW = "社区页面展示量";
    public static final String LABEL_SQUARE_PAGE_SHOW = "广场页展示";
    public static final String LABEL_FEEDS_PAGE_SHOW = "关注页展示";

    public static final String EVENT_SNS_TRACE = "社区痕迹跟踪";
    public static final String LABEL_CREATE_IMAGE_BY_LIMITED_SIZE_TRACE = "生成5M大小的图片";
    public static final String LABEL_POST_MESSAGE_FAILED = "发送消息失败: ";

    /**
     * 活动相关
     */
    public static final String EVENT_CAMPAIGN_FUNCTION_CLICK = "社区活动功能点击";
    public static final String LABEL_CAMPAIGN_HISTORY = "历史活动";
    public static final String LABEL_CAMPAIGN_DETAILS = "查看活动详情:";
    public static final String LABEL_CAMPAIGN_RULES = "活动规则:";
    public static final String LABEL_CAMPAIGN_SHARE = "分享:";
    public static final String LABEL_CAMPAIGN_PARTICIPATE = "参加:";
    public static final String LABEL_CAMPAIGN_EDIT = "编辑";
    public static final String LABEL_CAMPAIGN_ABLUM = "相册";

    public static final String EVENT_CAMPAIGN_DETAILS_PV = "社区活动详情PV";
    public static final String LABEL_CAMPAIGN_DETAILS_PV = "活动详情:";

    public static final String EVENT_CAMPAIGN_MESSAGE = "社区活动图片";
    public static final String LABEL_CAMPAIGN_MESSAGE = "图片数量:";

    public static final String EVENT_SNS_BIND_LOGIN = "社区绑定登录";

    /**
     * 社区用户推荐
     */
    public static final String EVENT_USER_RECOMMEND = "社区用户推荐";
    public static final String LABEL_RECOMMEND_LIST_CARD_SHOW = "列表卡片：展示";
    public static final String LABEL_RECOMMEND_GRID_CARD_SHOW = "方格卡片：展示";
    public static final String LABEL_RECOMMEND_LIST_CARD_IN_USER_DETAILS_SHOW = "个人主页列表卡片：展示";
    public static final String LABEL_RECOMMEND_LIST_CARD_CLICK = "列表卡片：点击关注";
    public static final String LABEL_RECOMMEND_GRID_CARD_CLICK = "方格卡片：点击关注";
    public static final String LABEL_RECOMMEND_LIST_CARD_IN_USER_DETAILS_CLICK = "个人主页列表卡片：点击关注";
    public static final String LABEL_FIND_FRIENDS_CLICK = "发现好友：点击关注";

    public static final String EVENT_USER_EDIT = "社区用户编辑";
    public static final String LABEL_USER_EDIT_NICKNAME = "修改名字:";
    public static final String LABEL_USER_EDIT_PORTRAIT = "修改头像";

    /**
     * 失败统计
     */
    public static final String EVENT_IMAGE_FALIURE = "社区图片加载失败";
    public static final String LABEL_IMAGE_DOWNLOAD_FALIURE = "下载失败";
    public static final String LABEL_IMAGE_FALIURE_CONNECTION = "连接失败";
    public static final String LABEL_IMAGE_FALIURE_TIMEOUT = "下载超时";

    /**
     * 推送相关
     */
    public static final String EVENT_PUSH = "消息推送";
    public static final String LABEL_ENTER__SNS_CAMPAIGN = "社区-活动页";
    public static final String LABEL_ENTER_SNS_USER_DETAILS = "社区-用户中心";
    public static final String LABEL_ENTER_SNS_COMMENTS = "社区-评论页";
    public static final String LABEL_ENTER_SNS_MESSAGE = "社区-消息页";
    public static final String LABEL_ENTER_SNS_SYSTEM_NOTICE = "社区-中奖页面";
    public static final String LABEL_ENTER_SNS_NOTIFICATION_CENTER = "社区-通知中心页面";

}
