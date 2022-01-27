package com.tanhua.dubbo.api.db;

import com.tanhua.domain.db.Settings;

/**
 * 通知设置服务接口
 */
public interface SettingsApi {
    /**
     * 获取通用设置数据
     * @param userId
     * @return
     */
    Settings findByUserId(Long userId);

    /**
     * 保存通知设置
     * @param settings
     */
    void saveSettings(Settings settings);

    /**
     * 更新通知设置
     * @param settings
     */
    void editSettings(Settings settings);
}
