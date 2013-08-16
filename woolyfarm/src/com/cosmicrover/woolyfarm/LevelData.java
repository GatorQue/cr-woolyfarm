package com.cosmicrover.woolyfarm;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.JsonValue;

public class LevelData implements Serializable {
	/// String name for the Level directory
	public static final String LEVEL_DIRECTORY = "levels/";
	
	/// String name for the Level Group files
	public static final String LEVEL_NAME = "level_";
	
	/// String name for the Level extension
	public static final String LEVEL_EXT = ".lvl";
	
	/// String name for the Level Group extension
	public static final String LEVEL_GROUP_EXT = ".grp";
	
	public enum Sprites {
		AnimalDuck,
		AnimalGoat,
		AnimalNone,
		AnimalPig,
		AnimalSheep,
		AnimalWolf,
		FencePost,
		FencePostHorizontalSpacer,
		FencePostSpacer,
		FenceHorizontal,
		FenceHorizontalEmpty,
		FenceHorizontalBroken,
		FenceHorizontalSpacer,
		FenceVertical,
		FenceVerticalEmpty,
		FenceVerticalBroken,
		FenceVerticalSpacer,
		GroundGrass1,
		GroundGrass2,
		GroundWater1,
		GroundWater2,
		GroundDirt1,
		GroundDirt2,
		GroundRock1,
		GroundRock2,
	};
	
	public int     id;
	public String  name;
	public String  group;
	public boolean locked;
	public boolean completed;
	public boolean loaded;
	public int numDogs = 0;
	public int maxDogs = 0;
	public int numFences = 0;
	public int maxFences = 0;
	public int mapRows = 0;
	public int mapCols = 0;
	public int fenceRows = 1; // rows*2+1
	public int fenceCols = 1; // cols+1

	// Array to hold the images to display for each item in the map
	public Sprites mapAnimals[][];
	public Sprites mapFenceHorizontal[][];
	public Sprites mapFenceVertical[][];
	public Sprites origGround[][];
	private Sprites origAnimals[][];
	private Sprites origFenceHorizontal[][];
	private Sprites origFenceVertical[][];
	
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
		this.loaded = false;
	}
	
	public String getTag() {
		return createTag(name, group);
	}
	
	public static String createTag(String name, String group) {
		return group + ":" + name;
	}

	public void createEmpty(int rows, int cols) {
		mapRows = rows;
		mapCols = cols;
		fenceRows = mapRows+1;
		fenceCols = mapCols+1;

		// Create our arrays according to the sizes retrieved above
		origGround = new Sprites[mapRows][mapCols];
		mapAnimals = new Sprites[mapRows][mapCols];
		mapFenceHorizontal = new Sprites[fenceRows][fenceCols];
		mapFenceVertical = new Sprites[fenceRows][fenceCols];
		origAnimals = new Sprites[mapRows][mapCols];
		origFenceHorizontal = new Sprites[fenceRows][fenceCols];
		origFenceVertical = new Sprites[fenceRows][fenceCols];
		
		// Set default values for ground and animals
		boolean grassStyle = false;
		for(int row=0; row<mapRows; row++) {
			for(int col=0; col<mapCols; col++) {
				if(col % 5 == 0) {
					if(grassStyle) {
						origGround[row][col] = Sprites.GroundGrass1;
					}
					else {
						origGround[row][col] = Sprites.GroundGrass2;
					}
				} else if(col % 5 == 1) {
					if(grassStyle) {
						origGround[row][col] = Sprites.GroundDirt1;
					}
					else {
						origGround[row][col] = Sprites.GroundDirt2;
					}
				} else if(col % 5 == 2) {
					if(grassStyle) {
						origGround[row][col] = Sprites.GroundWater1;
					}
					else {
						origGround[row][col] = Sprites.GroundWater2;
					}
				} else if(col % 5 == 3) {
					if(grassStyle) {
						origGround[row][col] = Sprites.GroundRock1;
					}
					else {
						origGround[row][col] = Sprites.GroundRock2;
					}
				} else if(col % 5 == 4) {
					if(grassStyle) {
						origGround[row][col] = Sprites.GroundGrass1;
					}
					else {
						origGround[row][col] = Sprites.GroundGrass2;
					}
				}
				grassStyle = !grassStyle;

				origAnimals[row][col] = Sprites.AnimalNone;
			}
			// Force alternating grass styles if columns is even
			if(mapCols % 2 == 0) {
				grassStyle = !grassStyle;
			}
		}

		// Set default values for fences
		for(int row=0; row<fenceRows; row++) {
			for(int col=0; col<fenceCols; col++) {
				origFenceHorizontal[row][col] = Sprites.FenceHorizontalEmpty;
				origFenceVertical[row][col] = Sprites.FenceVerticalEmpty;
			}
		}

		// Reset our level with the information provided
		resetLevel();
		
		// Set our loaded flag to true
		loaded = true;
	}
	
	public void updateOriginal() {
		// Update our number of fences and dogs remaining values
		maxFences = numFences;
		maxDogs = numDogs;
		
		// Update our original animal placement information
		for(int row=0; row<mapRows; row++) {
			for(int col=0; col<mapCols; col++) {
				origAnimals[row][col] = mapAnimals[row][col];
			}
		}

		// Update our original fence placement information
		for(int row=0; row<fenceRows; row++) {
			for(int col=0; col<fenceCols; col++) {
				origFenceHorizontal[row][col] = mapFenceHorizontal[row][col];
				origFenceVertical[row][col] = mapFenceVertical[row][col];
			}
		}
	}

	public void resetLevel() {
		// Reset our number of fences and dogs remaining values
		numFences = maxFences;
		numDogs = maxDogs;
		
		// Reset our animal placement information from original
		for(int row=0; row<mapRows; row++) {
			for(int col=0; col<mapCols; col++) {
				mapAnimals[row][col] = origAnimals[row][col];
			}
		}

		// Reset our fence placement information from original
		for(int row=0; row<fenceRows; row++) {
			for(int col=0; col<fenceCols; col++) {
				mapFenceHorizontal[row][col] = origFenceHorizontal[row][col];
				mapFenceVertical[row][col] = origFenceVertical[row][col];
			}
		}
	}
	
	@Override
	public void read(Json json, JsonValue jsonData) {
		maxFences = json.readValue("maxFences", Integer.class, jsonData);
		maxDogs = json.readValue("maxDogs", Integer.class, jsonData);
		mapRows = json.readValue("mapRows", Integer.class, jsonData);
		mapCols = json.readValue("mapCols", Integer.class, jsonData);
		fenceRows = mapRows+1;
		fenceCols = mapCols+1;

		// Create our arrays according to the sizes retrieved above
		origGround = new Sprites[mapRows][mapCols];
		mapAnimals = new Sprites[mapRows][mapCols];
		mapFenceHorizontal = new Sprites[fenceRows][fenceCols];
		mapFenceVertical = new Sprites[fenceRows][fenceCols];
		origAnimals = new Sprites[mapRows][mapCols];
		origFenceHorizontal = new Sprites[fenceRows][fenceCols];
		origFenceVertical = new Sprites[fenceRows][fenceCols];
		
		// Set default value and read in the actual value for ground and animals
		boolean grassStyle = false;
		for(int row=0; row<mapRows; row++) {
			for(int col=0; col<mapCols; col++) {
				if(grassStyle) {
					origGround[row][col] = Sprites.GroundGrass1;
				}
				else {
					origGround[row][col] = Sprites.GroundGrass2;
				}
				grassStyle = !grassStyle;
				// Retrieve each type using the valueOf method of each Enum class
				try {
					origGround[row][col] = Sprites.valueOf(json.readValue("origGround"+row*mapCols+col, String.class, jsonData));
				} catch(IllegalArgumentException iae) {
					// Accept the default value of None
				}

				origAnimals[row][col] = Sprites.AnimalNone;
				// Retrieve each type using the valueOf method of each Enum class
				try {
					origAnimals[row][col] = Sprites.valueOf(json.readValue("origAnimals"+row*mapCols+col, String.class, jsonData));
				} catch(IllegalArgumentException iae) {
					// Accept the default value of None
				}
			}
			// Force alternating grass styles if columns is even
			if(mapCols % 2 == 0) {
				grassStyle = !grassStyle;
			}
		}

		// Set default value and read in the actual value for fences
		for(int row=0; row<fenceRows; row++) {
			for(int col=0; col<fenceCols; col++) {
				origFenceHorizontal[row][col] = Sprites.FenceHorizontalEmpty;
				origFenceVertical[row][col] = Sprites.FenceVerticalEmpty;

				// Retrieve each type using the valueOf method of each Enum class
				try {
					origFenceHorizontal[row][col] = Sprites.valueOf(json.readValue("origFenceHorizontal"+row*fenceCols+col, String.class, jsonData));
				} catch(IllegalArgumentException iae) {
					// Accept the default value of EmptyHorizontal
				}
				// Retrieve each type using the valueOf method of each Enum class
				try {
					origFenceVertical[row][col] = Sprites.valueOf(json.readValue("origFenceVertical"+row*fenceCols+col, String.class, jsonData));
				} catch(IllegalArgumentException iae) {
					// Accept the default value of EmptyVertical
				}
			}
		}

		// Reset our level with the information provided
		resetLevel();
		
		// Set our loaded flag now
		loaded = true;
	}

	@Override
	public void write(Json json) {
		json.writeObjectStart(this.getClass().getName(), this.getClass(), this.getClass());
    	json.writeValue("maxFences", maxFences);
    	json.writeValue("maxDogs", maxDogs);
    	json.writeValue("mapRows", mapRows);
    	json.writeValue("mapCols", mapCols);

    	// Write out map of ground and animal tiles
    	for(int row=0; row<mapRows; row++) {
    		for(int col=0; col<mapCols; col++) {
    			json.writeValue("origGround"+row*mapCols+col, origGround[row][col].toString());
    			json.writeValue("origAnimals"+row*mapCols+col, origAnimals[row][col].toString());
    		}
    	}

    	// Write out map of fences
    	for(int row=0; row<fenceRows; row++) {
    		for(int col=0; col<fenceCols; col++) {
    			json.writeValue("origFenceHorizontal"+row*fenceCols+col, origFenceHorizontal[row][col].toString());
    			json.writeValue("origFenceVertical"+row*fenceCols+col, origFenceVertical[row][col].toString());
    		}
    	}
    	json.writeObjectEnd();
	}
}
