package com.org.market

import com.danikula.videocache.HttpProxyCacheServer
import com.org.ui.components.ProxyVideoCacheManager

var videoCacheProxyServer: HttpProxyCacheServer = ProxyVideoCacheManager.getProxy(getApplicationContext())