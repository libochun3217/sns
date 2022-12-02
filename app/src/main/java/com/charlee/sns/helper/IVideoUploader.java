package com.charlee.sns.helper;

import bolts.Task;

/**
 * 视频上传接口。
 */
public interface IVideoUploader {
    /**
     * 准备上传
     * @return 异步任务对象
     */
    Task<Void> prepare();

    /**
     * 上传视频文件
     * @param localVideoPath 文件本地路径
     * @return 上传的异步任务对象，返回上传后的URL
     */
    Task<String> upload(String localVideoPath);
}
