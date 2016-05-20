// CMAppLockDataService.aidl
package com.cmcm.applock.service;

import java.util.List;

import android.content.Intent;

import com.cmcm.applock.service.CMAppLockDataChangeObserver;
/**
 *@hide
 */
interface CMAppLockDataService {
    /**
     * 检查指定应用当前是否需要加锁，可使用 handleTheLockedApp 替代
     *   @param packageName 需要判断的应用的包名
     *   @return true为需要显示锁；false 为该应用不需要加锁
     */
    boolean needLockTheApp(String packageName);

    /**
     * 通知Apps lock处理指定加锁应用
     *   @param packageName 需要处理的应用的包名
     *   @param startIntent 启动该应用的 intent 数据，如果只是判断，可传null @see needLockTheApp
     *   @return true 表示 Apps lock 处理后续逻辑；false 表示 Apps lock 不处理此应用，framework应正常处理后续逻辑
     */
    boolean handleTheLockedApp(String packageName, in Intent startIntent);

    /**
     * 获取当前需要加锁的应用列表
     *   @return 返回当前需要加锁的应用列表包名
     */
    List<String> getLockedPackages();

    /**
     * 注册数据变化的监听器
     *   @param observer 当加锁应用列表发生改变时会通过该接口通知
     */
    void registerObserver(in CMAppLockDataChangeObserver observer);

    /**
     * 当 Activity 产生 onResume 事件时调用，通知 Apps lock 处理
     *    @param packageName 当前Activity所在的包名
     *    @param className 当前Activity类名
     */
    void onActivityResume(String packageName, String className);

    /**
     * 当 Activity 产生 onPause 事件时调用，通知 Apps lock 处理
     *    @param packageName 当前Activity所在的包名
     *    @param className 当前Activity类名
     */
    void onActivityPause(String packageName, String className);
}
