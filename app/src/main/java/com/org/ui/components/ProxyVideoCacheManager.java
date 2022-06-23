package com.org.ui.components;

import android.content.Context;

import com.danikula.videocache.HttpProxyCacheServer;
import com.danikula.videocache.file.DiskUsage;

import java.io.File;
import java.io.IOException;

public class ProxyVideoCacheManager {

    private static HttpProxyCacheServer sharedProxy;

    private ProxyVideoCacheManager() {
    }

    public static HttpProxyCacheServer getProxy(Context context) {
        return sharedProxy == null ? (sharedProxy = newProxy(context)) : sharedProxy;
    }

    private static HttpProxyCacheServer newProxy(Context context) {
        return new HttpProxyCacheServer.Builder(context)
                .maxCacheSize(50 * 1024 * 1024)
                .cacheDirectory(Utils.getVideoCacheDir(context))
                .maxCacheFilesCount(10)
                .build();
    }

}
