package org.xellos.mysportstrack;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * ȫ��Ӧ�ó������
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
