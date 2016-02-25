package org.xellos.mysportstrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;

public class SDKReceiver extends BroadcastReceiver {
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action
				.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
			// key 验证失败，相应处理
			Toast.makeText(context, "App Key失效，请重新申请", Toast.LENGTH_SHORT)
					.show();
		}
		if (action
				.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
			// 网络出错，相应处理
			Toast.makeText(context, "请检查网络", Toast.LENGTH_SHORT).show();
		}
	}
}