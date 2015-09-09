package org.xellos.mysportstrack;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

/**
 * ׷���ҵĹ켣����
 * 
 * @author Liang
 */
@SuppressLint("SimpleDateFormat")
@SuppressWarnings("deprecation")
public class TrackingService extends Service implements LocateController {
	String name = "";
	LocationBinder locationBinder = new LocationBinder();
	private LocationChangedObserver locationChangedObserver;
	// ��λ����
	LocationClient mLocClient;
	DbUtils dbUtils = null;
	public MyLocationListenner myListener = new MyLocationListenner();

	SensorManager msm;
	private Sensor defaultSensor;
	private SensorEventListener listener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
				float x = event.values[SensorManager.DATA_X];
				// float y = event.values[SensorManager.DATA_Y];
				// float z = event.values[SensorManager.DATA_Z];
				if (locationChangedObserver != null && x != 0) {
					// Log.d("TrackingDirection", " Direction: " + x);
					locationChangedObserver.onDirectionChanged(x);
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};
	WalkingPointOnMap lastWalkingPointOnMap;

	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (locationChangedObserver != null) {
				locationChangedObserver.onChanged(location);
			}

			WalkingPointOnMap currentPoint = new WalkingPointOnMap(
					location.getLatitude(), location.getLongitude());
			currentPoint.trackid = track._id;
			if (lastWalkingPointOnMap == null
					|| !currentPoint.equals(lastWalkingPointOnMap)) {
				try {
					dbUtils.save(currentPoint);
				} catch (DbException e) {
					e.printStackTrace();
				}
				lastWalkingPointOnMap = currentPoint;
			}

			// Receive Location
			StringBuffer sb = new StringBuffer(256);
			sb.append("time : ");
			sb.append(location.getTime());
			sb.append("\nerror code : ");
			sb.append(location.getLocType());
			sb.append("\nlatitude : ");
			sb.append(location.getLatitude());
			sb.append("\nlontitude : ");
			sb.append(location.getLongitude());
			sb.append("\nradius : ");
			sb.append(location.getRadius());
			if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS��λ���
				sb.append("\nspeed : ");
				sb.append(location.getSpeed());// ��λ������ÿСʱ
				sb.append("\nsatellite : ");
				sb.append(location.getSatelliteNumber());
				sb.append("\nheight : ");
				sb.append(location.getAltitude());// ��λ����
				sb.append("\ndirection : ");
				sb.append(location.getDirection());
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				sb.append("\ndescribe : ");
				sb.append("gps��λ�ɹ�");

			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// ���綨λ���
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				// ��Ӫ����Ϣ
				sb.append("\noperationers : ");
				sb.append(location.getOperators());
				sb.append("\ndescribe : ");
				sb.append("���綨λ�ɹ�");
			} else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// ���߶�λ���
				sb.append("\ndescribe : ");
				sb.append("���߶�λ�ɹ������߶�λ���Ҳ����Ч��");
			} else if (location.getLocType() == BDLocation.TypeServerError) {
				sb.append("\ndescribe : ");
				sb.append("��������綨λʧ�ܣ����Է���IMEI�źʹ��嶨λʱ�䵽loc-bugs@baidu.com��������׷��ԭ��");
			} else if (location.getLocType() == BDLocation.TypeNetWorkException) {
				sb.append("\ndescribe : ");
				sb.append("���粻ͬ���¶�λʧ�ܣ����������Ƿ�ͨ��");
			} else if (location.getLocType() == BDLocation.TypeCriteriaException) {
				sb.append("\ndescribe : ");
				sb.append("�޷���ȡ��Ч��λ���ݵ��¶�λʧ�ܣ�һ���������ֻ���ԭ�򣬴��ڷ���ģʽ��һ���������ֽ�����������������ֻ�");
			}
			sb.append("\nlocationdescribe : ");// λ�����廯��Ϣ
			Log.d("location", sb.toString());
			Log.i("BaiduLocationApiDem", sb.toString());
		}
	}

	public class LocationBinder extends Binder {
		TrackingService getService() {
			return TrackingService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// ��gps
		option.setCoorType("bd09ll"); // ������������
		option.setScanSpan(1000);
		mLocClient.setLocOption(option);
		return locationBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		dbUtils = DbUtils.create(this);
		msm = (SensorManager) getSystemService(SENSOR_SERVICE);
		defaultSensor = msm.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		msm.registerListener(listener, defaultSensor,
				SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		return super.onStartCommand(intent, flags, startId);
	}

	Track track = new Track();

	@Override
	public void startTracking() {
		// ��ʼ��¼�켣
		if (!mLocClient.isStarted()) {
			mLocClient.start();
			Toast.makeText(this, "��ʼ��¼�켣", Toast.LENGTH_SHORT).show();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
					"yyyy��MM��dd��-aa-hh:mm �Ĺ켣");
			name = simpleDateFormat.format(new Date());
			// ����һ���µĹ켣��¼
			try {
				track.name = name;
				dbUtils.save(track);
				track = dbUtils.findFirst(Selector.from(Track.class).where(
						"name", "=", name));
			} catch (DbException e) {
				e.printStackTrace();
			}
		} else {
			Toast.makeText(this, "���ڼ�¼�켣!", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void stopTracking() {
		// ֹͣ��¼�켣
		if (mLocClient.isStarted()) {
			mLocClient.stop();
			Toast.makeText(this, "ֹͣ��¼�켣", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "�Ѿ�ֹͣ��¼�켣!", Toast.LENGTH_SHORT).show();
		}
	}

	public void setLocationObserver(LocationChangedObserver lo) {
		this.locationChangedObserver = lo;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		msm.unregisterListener(listener);
		stopTracking();
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		msm.unregisterListener(listener);
		super.onDestroy();
	}
}
