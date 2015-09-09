package org.xellos.mysportstrack;

/**
 * 位置感知接口
 * 
 * @author Liang
 */
public interface LocationChangedObserver {
	void onChanged(Object location);

	void onDirectionChanged(float x);
}
