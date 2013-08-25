package com.cosmicrover.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ArrayMap.Keys;
import com.cosmicrover.core.assets.GroupData;
import com.cosmicrover.core.assets.LevelData;

public class GroupManager<L extends LevelData, G extends GroupData<L>> {
	/// Maximum number of groups supported
	public static final int MAX_GROUPS = 63;
	
	/// Directory where each group can be found
	public static final String GROUP_DIRECTORY = "groups/";
	
	/// Filename for each group file
	public static final String GROUP_FILENAME = "group_";
	
	/// File extension for each group file
	public static final String GROUP_EXTENSION = ".gdf";

	/// Filename for the custom group category
	public static final String CUSTOM_GROUP_FILENAME =
			GROUP_DIRECTORY + GROUP_FILENAME + MAX_GROUPS + GROUP_EXTENSION;
	
	/// Map of groupNames to GroupData derived classes
	private final ArrayMap<String, G> groupMap;
	
	public GroupManager() {
		groupMap = new ArrayMap<String, G>();
	}
	
	/**
	 * Clear the list of groups registered
	 */
	public void clear() {
		groupMap.clear();
	}
	
	/**
	 * Provides the group data filename for the id specified which can be used
	 * to discover the list of groups available for a particular game. The
	 * custom group is special and provided as a constant above.
	 * @param groupId to use as part of the filename
	 * @return group data filename for the id specified.
	 */
	public static String getFilename(int groupId) {
		return GROUP_DIRECTORY + GROUP_FILENAME + groupId + GROUP_EXTENSION;
	}

	/**
	 * Returns to the caller the number of levels currently registered
	 * @return number of levels currently registered
	 */
	public int getSize() {
		return groupMap.size;
	}
	
	/**
	 * Register the screenId for the screen object provided. This will be used
	 * later for handling screen transitions.
	 * @param screenId to use for the screen
	 * @param screen object that inherits from AbstractScreen
	 */
	public void registerGroup(G groupData) {
		String groupName = groupData.getFilename();
		if(!groupMap.containsKey(groupName)) {
			groupMap.put(groupName, groupData);
		} else {
			Gdx.app.error("GroupManager:registerGroup", "Group '"+groupName+"' already exists");
		}
	}

	/**
	 * Unregister a previously registered screen object using its screenId to
	 * identify the screen object.
	 * @param screenId to unregister
	 */
	public void unregisterGroup(String groupName) {
		groupMap.removeKey(groupName);
	}
	
	/**
	 * Unregister a previously registered screen object using its original
	 * screen object address to identify the screen object.
	 * @param screen object to unregister
	 */
	public void unregisterGroup(G groupData) {
		groupMap.removeValue(groupData, true);
	}
	
	/**
	 * Returns the screenId for the Screen provided or 0 if the Screen provided
	 * was not found.
	 * @param screen to find screenId for
	 * @return screenId or 0 if none was found
	 */
	public String getGroupName(G groupData) {
		String anGroupName = "";
		if(groupMap.containsValue(groupData, true)) {
			anGroupName = groupMap.getKey(groupData, true);
		}
		return anGroupName;
	}

	/**
	 * Returns the first GroupData object registered
	 * @return first GroupData object in group map
	 */
	public G getFirstGroup() {
		return groupMap.firstValue();
	}
	
	/**
	 * Returns the screen object for the screenId provided.
	 * @param screenId to lookup
	 * @return screen object or null if screenId can't be found
	 */
	public G getGroup(String groupName) {
		return groupMap.get(groupName);
	}

	/**
	 * Returns an array of all the keys of the groups registered in this GroupManager.
	 * @return all the Keys for each group registered
	 */
	public Keys<String> getGroups() {
		return groupMap.keys();
	}
}
