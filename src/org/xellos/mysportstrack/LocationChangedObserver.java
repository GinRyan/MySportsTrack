package org.xellos.mysportstrack;

/**
 * λ�ø�֪�ӿ�
 * 
 * @author Liang
 */
public interface LocationChangedObserver {
	void onChanged(Object location);

	void onDirectionChanged(float x);
}
