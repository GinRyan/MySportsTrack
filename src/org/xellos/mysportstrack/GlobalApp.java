package org.xellos.mysportstrack;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * 全局应用程序对象
 * 
 * @author Liang
 *
 */
public class GlobalApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		SDKInitializer.initialize(getApplicationContext());
	}
}
