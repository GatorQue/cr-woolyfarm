package com.cosmicrover.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ArrayMap;
import com.cosmicrover.core.assets.GroupData;
import com.cosmicrover.core.assets.LevelData;
import com.cosmicrover.core.screens.AbstractScreen;

public class ScreenManager<L extends LevelData, G extends GroupData<L>> {
	private final ArrayMap<Integer, AbstractScreen<L,G>> screenMap;
	
	public ScreenManager() {
		screenMap = new ArrayMap<Integer, AbstractScreen<L,G>>();
	}
	
	/**
	 * Register the screenId for the screen object provided. This will be used
	 * later for handling screen transitions.
	 * @param screenId to use for the screen
	 * @param screen object that inherits from AbstractScreen
	 */
	public void registerScreen(int screenId, AbstractScreen<L,G> screen) {
		if(!screenMap.containsKey(screenId)) {
			screenMap.put(screenId, screen);
		} else {
			Gdx.app.error("ScreenManager:registerScreen", "Screen id '"+screenId+"' already exists");
		}
	}

	/**
	 * Unregister a previously registered screen object using its screenId to
	 * identify the screen object.
	 * @param screenId to unregister
	 */
	public void unregisterScreen(int screenId) {
		screenMap.removeKey(screenId);
	}
	
	/**
	 * Unregister a previously registered screen object using its original
	 * screen object address to identify the screen object.
	 * @param screen object to unregister
	 */
	public void unregisterScreen(AbstractScreen<L,G> screen) {
		screenMap.removeValue(screen, true);
	}
	
	/**
	 * Returns the screenId for the Screen provided or 0 if the Screen provided
	 * was not found.
	 * @param screen to find screenId for
	 * @return screenId or 0 if none was found
	 */
	public int getScreenId(AbstractScreen<L,G> screen) {
		int anScreenId = 0;
		if(screenMap.containsValue(screen, true)) {
			anScreenId = screenMap.getKey(screen, true);
		}
		return anScreenId;
	}

	/**
	 * Returns the screen object for the screenId provided.
	 * @param screenId to lookup
	 * @return screen object or null if screenId can't be found
	 */
	public AbstractScreen<L,G> getScreen(int screenId) {
		return screenMap.get(screenId);
	}
}
