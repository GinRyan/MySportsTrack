package org.xellos.mysportstrack;

import com.lidroid.xutils.db.annotation.Foreign;
import com.lidroid.xutils.db.annotation.Id;

/**
 * ��·�����ĵ�
 * 
 * @author Liang
 *
 */
public class WalkingPointOnMap {
	@Id
	private long _id;
	/**
	 * γ��
	 */
	public double latitude;
	/**
	 * ����
	 */
	public double longitude;
	/**
	 * �켣id
	 */
	@Foreign(foreign = "_id")
	public long trackid;

	public long getTrackid() {
		return trackid;
	}

	public void setTrackid(long trackid) {
		this.trackid = trackid;
	}

	public WalkingPointOnMap() {
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public WalkingPointOnMap(double latitude, double longitude) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public long get_id() {
		return _id;
	}

	public void set_id(long _id) {
		this._id = _id;
	}

	@Override
	public boolean equals(Object o) {
		WalkingPointOnMap wp = (WalkingPointOnMap) o;
		if (wp.latitude == latitude && wp.longitude == longitude) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "WalkingPointOnMap [_id=" + _id + ", latitude=" + latitude
				+ ", longitude=" + longitude + ", trackid=" + trackid + "]";
	}
	
	
}