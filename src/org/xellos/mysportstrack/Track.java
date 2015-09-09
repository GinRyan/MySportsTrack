package org.xellos.mysportstrack;

import java.util.ArrayList;
import java.util.List;

import com.lidroid.xutils.db.annotation.Id;

/**
 * πÏº£∂‘œÛ
 * 
 * @author Liang
 *
 */
public class Track {
	@Id
	public long _id;
	public String name;
	public List<WalkingPointOnMap> walkingPointOnMaps = new ArrayList<WalkingPointOnMap>();

	public long get_id() {
		return _id;
	}

	public void set_id(long _id) {
		this._id = _id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<WalkingPointOnMap> getWalkingPointOnMaps() {
		return walkingPointOnMaps;
	}

	public void setWalkingPointOnMaps(List<WalkingPointOnMap> walkingPointOnMaps) {
		this.walkingPointOnMaps = walkingPointOnMaps;
	}

	@Override
	public String toString() {
		return "Track [_id=" + _id + ", name=" + name + ", walkingPointOnMaps="
				+ walkingPointOnMaps + "]";
	}

}
