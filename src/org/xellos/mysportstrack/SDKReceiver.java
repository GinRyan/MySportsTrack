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
			// key ��֤ʧ�ܣ���Ӧ����
			Toast.makeText(context, "App KeyʧЧ������������", Toast.LENGTH_SHORT)
					.show();
		}
		if (action
				.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
			// ���������Ӧ����
			Toast.makeText(context, "��������", Toast.LENGTH_SHORT).show();
		}
	}
}