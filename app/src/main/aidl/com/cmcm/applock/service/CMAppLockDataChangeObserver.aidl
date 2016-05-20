// CMAppLockDataChangeObserver.aidl
package com.cmcm.applock.service;

import java.util.List;

/**
 *@hide
 */
interface CMAppLockDataChangeObserver {
    /**
        @param newPackageList 当前被加锁的应用列表
     */
    void onLockedPackagesChanged(in List<String> newPackageList);
}
