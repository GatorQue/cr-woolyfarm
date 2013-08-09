package com.cosmicrover.woolyfarm;

public class LevelData {
	public enum Backgrounds {
		Grass1,
		Grass2,
		Water1,
		Water2,
		Dirt1,
		Dirt2,
		Rock1,
		Rock2
	};
	
	public enum Animals {
		Duck,
		Goat,
		None,
		Pig,
		Sheep,
		Wolf,
	};

	public enum Fence {
		Horizontal,
		Vertical,
		BrokenHorizontal,
		BrokenVertical,
		EmptyHorizontal,
		EmptyVertical,
	};
	
	public int     id;
	public String  name;
	public String  group;
	public boolean locked;
	public boolean completed;
	public int numDogs = 0;
	public int numFences = 0;
	public int rows = 10;
	public int cols = 10;

	// Array to hold the images to display for each item in the map
	public Backgrounds mapGround[][];
	public Animals mapAnimals[][];
	public Fence mapFences[][];
	
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
		mapGround = new Backgrounds[rows][cols];
		mapAnimals = new Animals[rows][cols];
		mapFences = new Fence[rows*2+1][cols+1];
		boolean grassStyle = false;
		// Create ground and animals
		for(int row=0; row<rows; row++) {
			for(int col=0; col<cols; col++) {
				if(grassStyle) {
					mapGround[row][col] = Backgrounds.Grass1;
				}
				else {
					mapGround[row][col] = Backgrounds.Grass2;
				}
				grassStyle = !grassStyle;
				mapAnimals[row][col] = Animals.None;
			}
			// Force alternating grass styles if columns is even
			if(cols % 2 == 0) {
				grassStyle = !grassStyle;
			}
		}
		// Create fences
		for(int row=0; row<rows*2+1; row++) {
			for(int col=0; col<cols+1; col++) {
				if(row % 2 == 0) {
					mapFences[row][col] = Fence.EmptyHorizontal;
				}
				else {
					mapFences[row][col] = Fence.EmptyVertical;
				}
			}
		}
	}
	
	public String getTag() {
		return createTag(name, group);
	}
	
	public static String createTag(String name, String group) {
		return group + ":" + name;
	}
}
