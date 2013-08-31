package com.cosmicrover.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ArrayMap.Keys;
import com.cosmicrover.core.assets.LevelData;

public class LevelManager<L extends LevelData> {
	/// Maximum number of levels supported per group
	public static final int MAX_LEVELS = 1000;
	
	/// Directory where each levle can be found
	public static final String LEVEL_DIRECTORY = "/";
	
	/// Filename for each group file
	public static final String LEVEL_FILENAME = "level_";
	
	/// File extension for each group file
	public static final String LEVEL_EXTENSION = ".ldf";

	/// Filename for the custom group category
	public static final String CUSTOM_LEVEL_FILENAME =
			GroupManager.GROUP_DIRECTORY + GroupManager.MAX_GROUPS + LEVEL_DIRECTORY +
			LEVEL_FILENAME + MAX_LEVELS + LEVEL_EXTENSION;
	
	/// Map of levelNames to LevelData derived classes
	private final ArrayMap<String, L> levelMap;
	
	public LevelManager() {
		levelMap = new ArrayMap<String, L>();
	}
	
	/**
	 * Clears the list of levels registered
	 */
	public void clear() {
		levelMap.clear();
	}

	/**
	 * Provides the group data filename for the id specified which can be used
	 * to discover the list of groups available for a particular game. The
	 * custom group is special and provided as a constant above.
	 * @param groupId to use as part of the filename path
	 * @param levelId to use as part of the filename
	 * @return group data filename for the id specified.
	 */
	public static String getFilename(int groupId, int levelId) {
		return GroupManager.GROUP_DIRECTORY + groupId + LEVEL_DIRECTORY +
			   LEVEL_FILENAME + levelId + LEVEL_EXTENSION;
	}

	/**
	 * Returns to the caller the number of levels currently registered
	 * @return number of levels currently registered
	 */
	public int getSize() {
		return levelMap.size;
	}
	
	/**
	 * Register the screenId for the screen object provided. This will be used
	 * later for handling screen transitions.
	 * @param screenId to use for the screen
	 * @param screen object that inherits from AbstractScreen
	 */
	public void registerLevel(L levelData) {
		String levelName = levelData.getFilename();
		if(!levelMap.containsKey(levelName)) {
			levelMap.put(levelName, levelData);
		} else {
			Gdx.app.error("GroupManager:registerGroup", "Group '"+levelName+"' already exists");
		}
	}

	/**
	 * Unregister a previously registered screen object using its screenId to
	 * identify the screen object.
	 * @param screenId to unregister
	 */
	public void unregisterLevel(String levelName) {
		levelMap.removeKey(levelName);
	}
	
	/**
	 * Unregister a previously registered screen object using its original
	 * screen object address to identify the screen object.
	 * @param screen object to unregister
	 */
	public void unregisterLevel(L levelData) {
		levelMap.removeValue(levelData, true);
	}
	
	/**
	 * Returns the screenId for the Screen provided or 0 if the Screen provided
	 * was not found.
	 * @param screen to find screenId for
	 * @return screenId or 0 if none was found
	 */
	public String getLevelName(L levelData) {
		String anLevelName = "";
		if(levelMap.containsValue(levelData, true)) {
			anLevelName = levelMap.getKey(levelData, true);
		}
		return anLevelName;
	}

	/**
	 * Returns the first LevelData object registered
	 * @return first LevelData object in level map
	 */
	public L getFirstLevel() {
		return levelMap.firstValue();
	}
	
	/**
	 * Returns the screen object for the screenId provided.
	 * @param screenId to lookup
	 * @return screen object or null if screenId can't be found
	 */
	public L getLevel(String levelName) {
		return levelMap.get(levelName);
	}

	/**
	 * Returns an array of all the keys of the levels registered in this LevelManager.
	 * @return all the Keys for each level registered
	 */
	public Keys<String> getLevels() {
		return levelMap.keys();
	}
}
