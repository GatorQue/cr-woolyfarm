package com.cosmicrover.woolyfarm;

public class LevelData {
	public int     id;
	public String  name;
	public String  group;
	public boolean locked;
	public boolean completed;
	public int dogs = 0;
	public int fences = 0;
	public int rows = 10;
	public int cols = 10;
	
	// TODO: Add read methods to load the level details from the level file
	
	// TODO: Add statistics about each level completed
	
	public LevelData(int id, String name, String group, boolean locked) {
		this(id, name, group, locked, false);
	}

	public LevelData(int id, String name, String group, boolean locked, boolean completed) {
		this.id = id;
		this.name = name.trim();
		this.group = group.trim();
		this.locked = locked;
		this.completed = completed;
	}
	
	public String getTag() {
		return createTag(name, group);
	}
	
	public static String createTag(String name, String group) {
		return group + ":" + name;
	}
}
