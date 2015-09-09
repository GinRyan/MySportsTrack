package org.xellos.mysportstrack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.xellos.mysportstrack.TrackingService.LocationBinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

/**
 * 主要界面内容
 * 
 * @author Liang
 */
public class MainActivity extends Activity implements LocationChangedObserver {
	MapView bmapView;

	Button start_tracking;
	Button stop_tracking;
	SDKReceiver mReceiver = null;
	BaiduMap mBaiduMap = null;
	Button mode;
	DbUtils dbUtils = null;
	ArrayList<LatLng> points = new ArrayList<LatLng>();

	private TrackingService pointService;
	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocationBinder locationBinder = (LocationBinder) service;
			pointService = locationBinder.getService();
			pointService.setLocationObserver(MainActivity.this);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			pointService = null;
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Intent serviceintent = new Intent(this, TrackingService.class);
		startService(serviceintent);
		bindService(serviceintent, conn, Context.BIND_AUTO_CREATE);

		dbUtils = DbUtils.create(this);

		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
		iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);

		mReceiver = new SDKReceiver();
		registerReceiver(mReceiver, iFilter);

		mode = (Button) findViewById(R.id.mode);
		bmapView = (MapView) findViewById(R.id.bmapView);

		mBaiduMap = bmapView.getMap();

		// 定位部分
		mCurrentMode = LocationMode.NORMAL;
		mode.setText("普通");
		mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
				mCurrentMode, true, mCurrentMarker));

		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		// 定位初始化

		OnClickListener btnClickListener = new OnClickListener() {
			public void onClick(View v) {
				switch (mCurrentMode) {
				case NORMAL:
					mode.setText("跟随");
					mCurrentMode = LocationMode.FOLLOWING;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					break;
				case COMPASS:
					mode.setText("普通");
					mCurrentMode = LocationMode.NORMAL;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					break;
				case FOLLOWING:
					mode.setText("罗盘");
					mCurrentMode = LocationMode.COMPASS;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					break;
				}
			}
		};
		mode.setOnClickListener(btnClickListener);
		polylineOptions = new PolylineOptions();
		if (points.size() > 2) {
			mBaiduMap.clear();
			polylineOptions.points(points).color(0xAA000000).width(3);
			mBaiduMap.addOverlay(polylineOptions);
		}

	}

	private LocationMode mCurrentMode;
	BitmapDescriptor mCurrentMarker;
	boolean isFirstLoc = true;// 是否首次定位

	private PolylineOptions polylineOptions;

	private MyLocationData locData;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.start) {
			points.clear();
			pointService.startTracking();
			return true;
		} else if (id == R.id.stop) {
			pointService.stopTracking();
			return true;
		} else if (id == R.id.history) {
			// 画历史轨迹
			try {
				historyTracks = dbUtils.findAll(Track.class);
				String[] title = new String[historyTracks.size()];
				System.out.println("历史轨迹：" + historyTracks);
				for (int i = 0; i < historyTracks.size(); i++) {
					title[i] = historyTracks.get(i).name;
				}
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
				alertDialog.setTitle("选择一条历史轨迹");
				alertDialog.setItems(title,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								int color = 0xAA000000
										+ new Random().nextInt(0xFF0000)
										+ new Random().nextInt(0x00FF00)
										+ new Random().nextInt(0x0000FF);
								long trackid = historyTracks.get(which)._id;
								try {
									List<WalkingPointOnMap> pts = dbUtils
											.findAll(Selector.from(
													WalkingPointOnMap.class)
													.where("trackid", "=",
															trackid));
									points.clear();
									for (int i = 0; i < pts.size(); i++) {
										WalkingPointOnMap wp = pts.get(i);
										points.add(new LatLng(wp.getLatitude(),
												wp.getLongitude()));
									}

									if (points.size() > 2) {
										mBaiduMap.clear();
										polylineOptions.points(points).color(
												color);
										mBaiduMap.addOverlay(polylineOptions);
									}
								} catch (DbException e) {
									e.printStackTrace();
								}

							}
						}).show();
			} catch (DbException e) {
				e.printStackTrace();
			}

			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	BDLocation location;

	private float x;

	private List<Track> historyTracks;

	/**
	 * 当位置发生改变的时候回调
	 */
	@Override
	public void onChanged(Object locationObj) {
		try {
			// 位置发生改变时的回调
			location = (BDLocation) locationObj;
			// map view 销毁后不在处理新接收的位置
			if (location == null || bmapView == null)
				return;
			locData = new MyLocationData.Builder().direction(x)
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);

			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			}

			points.add(new LatLng(location.getLatitude(), location
					.getLongitude()));
			if (points.size() > 2) {
				polylineOptions.points(points);
				mBaiduMap.addOverlay(polylineOptions);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDirectionChanged(float x) {
		this.x = x;
		// map view 销毁后不在处理新接收的位置
		if (location == null || mBaiduMap == null)
			return;
		locData = new MyLocationData.Builder().accuracy(location.getRadius())
				.direction(x)
				// 此处设置开发者获取到的方向信息，顺时针0-360
				.latitude(location.getLatitude())
				.longitude(location.getLongitude()).build();
		try {
			mBaiduMap.setMyLocationData(locData);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		bmapView.onDestroy();
		super.onDestroy();
		unregisterReceiver(mReceiver);
		unbindService(conn);
	}

	@Override
	protected void onResume() {
		bmapView.onResume();
		super.onResume();

	}

	@Override
	protected void onPause() {
		bmapView.onPause();
		super.onPause();
	}
}
