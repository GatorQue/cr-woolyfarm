package com.cosmicrover.woolyfarm;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.cosmicrover.core.GameData;
import com.cosmicrover.core.GameManager;
import com.cosmicrover.core.screens.AbstractLoadingScreen;
import com.cosmicrover.core.screens.AbstractScreen;
import com.cosmicrover.core.screens.AssetLoadingScreen;
import com.cosmicrover.woolyfarm.screens.LevelPlayScreen;
import com.cosmicrover.woolyfarm.screens.LevelSelectScreen;
import com.cosmicrover.woolyfarm.screens.MainMenuScreen;
import com.cosmicrover.woolyfarm.screens.OptionsScreen;

public class PlayerData extends GameData {
	/// String name for the Level directory
	private static final String LEVEL_DIRECTORY = "levels/";
	
	/// String name for the Level Group files
	private static final String LEVEL_NAME = "level_";
	
	/// String name for the Level extension
	private static final String LEVEL_EXT = ".lvl";
	
	/// String name for the Level Group extension
	private static final String LEVEL_GROUP_EXT = ".grp";
	
	/// String name for the default level (tutorial)
	private static final String DEFAULT_LEVEL = "Tutorial";
	
	/// List of screenId values to be used by GameManager
	public static final int ASSET_LOADING_SCREEN       = 1;
	public static final int MAIN_MENU_SCREEN           = 2;
	public static final int OPTIONS_SCREEN             = 3;
	public static final int LEVEL_SELECT_SCREEN        = 4;
	public static final int LEVEL_PLAY_SCREEN          = 5;

	/// Data file format version number 
	private static final int DATA_FORMAT_VERSION = 1;

	/// Keep track of our GameManager object provided at init
	private GameManager gameManager = null;
	
	/// Maps region name to AtlasRegion information to texture
	private HashMap<String, AtlasRegion> levelRegions;

	/// TextureAtlas that can carve up the sprite texture to show the correct texture
	private TextureAtlas levelTextureAtlas;

	/// Maps region name to AtlasRegion information to texture
	private HashMap<String, AtlasRegion> spriteRegions;

	/// TextureAtlas that can carve up the sprite texture to show the correct texture
	private TextureAtlas spriteTextureAtlas;
	
	/// Array of level data 
	private ArrayMap<String, LevelData> levels;
	
	/// Name of the current level to be played
	private String currentLevel = DEFAULT_LEVEL;
	
	public PlayerData() {
		levels = new ArrayMap<String, LevelData>();
	}

	@Override
	public AbstractScreen getInitialScreen() {
		return screenManager.getScreen(ASSET_LOADING_SCREEN);
	}

	@Override
	public int getOptionsScreen() {
		return OPTIONS_SCREEN;
	}
	
	public LevelData getCurrentLevel() {
		return levels.get(currentLevel);
	}

	public void setCurrentLevel(String currentLevel) {
		this.currentLevel = currentLevel;
	}

	public TextureRegion getLevelTexture(String name) {
		// First time retrieving a Sprite texture? then initialize it now
		if(levelRegions == null) {
			// Create a hash map for looking up texture regions by string name found in SpriteComponent
			levelRegions = new HashMap<String, AtlasRegion>();
			levelTextureAtlas = gameManager.getAssetManager().get("textures/select.pack");
			// Create a map of each region available in our sprite TextureAtlas
			for (AtlasRegion region : levelTextureAtlas.getRegions()) {
				levelRegions.put(region.name, region);
			}
		}

		// Return the texture requested
		return levelRegions.get(name);
	}
	
	public TextureRegion getSpriteTexture(String name) {
		// First time retrieving a Sprite texture? then initialize it now
		if(spriteRegions == null) {
			// Create a hash map for looking up texture regions by string name found in SpriteComponent
			spriteRegions = new HashMap<String, AtlasRegion>();
			spriteTextureAtlas = gameManager.getAssetManager().get("textures/sprites.pack");
			// Create a map of each region available in our sprite TextureAtlas
			for (AtlasRegion region : spriteTextureAtlas.getRegions()) {
				spriteRegions.put(region.name, region);
			}
		}

		// Return the texture requested
		return spriteRegions.get(name);
	}
	
	public ArrayMap<String, LevelData> getLevels() {
		return levels;
	}

	@Override
	public void init(GameManager gameManager) {
		// Keep track of the GameManager object provided
		this.gameManager = gameManager;

		// Tell our asset manager to load our sprites texture pack
		gameManager.getAssetManager().load("textures/select.pack", TextureAtlas.class);
		gameManager.getAssetManager().load("textures/sprites.pack", TextureAtlas.class);
		
		// Create our screens next
		screenManager.registerScreen(ASSET_LOADING_SCREEN, new AssetLoadingScreen(gameManager, 0.25f));
		screenManager.registerScreen(MAIN_MENU_SCREEN, new MainMenuScreen(gameManager, "Cosmic Rover: Wooly Farm"));
		screenManager.registerScreen(OPTIONS_SCREEN, new OptionsScreen(gameManager, MAIN_MENU_SCREEN));
		screenManager.registerScreen(LEVEL_SELECT_SCREEN, new LevelSelectScreen(gameManager, MAIN_MENU_SCREEN));
		screenManager.registerScreen(LEVEL_PLAY_SCREEN, new LevelPlayScreen(gameManager, LEVEL_SELECT_SCREEN));

		// Set the initial next screenId value for LoadingScreen to the Main Menu
		AbstractLoadingScreen.setNextScreenId(MAIN_MENU_SCREEN);
		
	}

	@Override
	public void newGame() {
		// Clear our new game flag
		setNewGame(false);
		
		// Delete our game data file if it exists
		gameManager.deleteData();

		// Clear our list of levels and discover the levels available
		levels.clear();
		discoverLevels();

		// Set our in progress flag
		setInProgress(true);

		// Switch to the Level Select screen for now
		gameManager.setScreen(LEVEL_SELECT_SCREEN);
	}

	@Override
	public void resumeGame() {
		// Change to the correct screen
		gameManager.setScreen(LEVEL_SELECT_SCREEN);
		
		// Set our in progress flag
		setInProgress(true);
	}

	@Override
	public void resetGame() {
		// Clear our in progress flag
		setInProgress(false);

		// Set our new game flag to true
		setNewGame(true);
	}
	
	@Override
	public void write(Json json) {
		// Write the data format version number value first
		json.writeValue("format_version", DATA_FORMAT_VERSION);
		// Write our our level data information
		writeLevelData(json);
	}
	
	private void writeLevelData(Json json) {
		// Write the array of levels to the file
		json.writeArrayStart("levels");
		for(int i=0, iSize = levels.size; iSize > i; i++) {
			// Retrieve the first level in the list
			LevelData anLevel = levels.getValueAt(i);
			// Write the level data out
			json.writeObjectStart();
			json.writeValue("id", anLevel.id);
			json.writeValue("name", anLevel.name);
			json.writeValue("group", anLevel.group);
			json.writeValue("locked", anLevel.locked);
			json.writeValue("completed", anLevel.completed);
			json.writeObjectEnd();
		}
		json.writeArrayEnd();
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		// Read the data format version number first
		int formatVersion = json.readValue("format_version", Integer.class, jsonData);
	
		// Is this an old version of the data file that needs to be upgraded?
		if(formatVersion < DATA_FORMAT_VERSION) {
			// TODO: Add something for converting from old to new data files as needed
		}

		// Attempt to read our array of maps loaded from our data file
		JsonValue jsonLevels = jsonData.get("levels");
		if(jsonLevels != null) {
			// Read the level data from our file
			readLevelData(formatVersion, json, jsonLevels);
		} else {
			// Clear our current list of levels
			levels.clear();
			// Discover levels available and add them to our list
			discoverLevels();
		}
	}
	
	private void readLevelData(int formatVersion, Json json, JsonValue jsonData) {
		// Clear our list of levels first
		levels.clear();

		// Loop through each entity recorded and create them
		for(int i=0, iSize = jsonData.size; iSize > i; i++) {
			// Loop through and read each map filename into our mapsLoaded array
			JsonValue jsonEntity = jsonData.get(i);
			Integer id = json.readValue("id", Integer.class, jsonEntity);
			String name = json.readValue("name", String.class, jsonEntity);
			String group = json.readValue("group", String.class, jsonEntity);
			boolean locked = json.readValue("locked", Boolean.class, jsonEntity);
			boolean completed = json.readValue("completed", Boolean.class, jsonEntity);
			LevelData levelData = new LevelData(id, name, group, locked, completed);
			addLevel(levelData);
		} // for(int i=0, s=jsonEntities.size; i<s; i++)

		// Is this an old version of the data file that needs to be upgraded?
		if(formatVersion < DATA_FORMAT_VERSION) {
			// Add new levels to this data file
			discoverLevels();
		}
	}
	
	/**
	 * Discover any new levels that have been added and add them to the existing
	 * list of levels available to play.
	 */
	private void discoverLevels() {
		// Look for level group files and load each of them in
		for(int i=0;i<100;i++) {
			String levelGroupFilename = LEVEL_DIRECTORY + LEVEL_NAME + i + LEVEL_GROUP_EXT;

			// Loop through each levelGroup file that exists
			FileHandle levelGroupFile = Gdx.files.internal(levelGroupFilename);
			if(levelGroupFile.exists()) {
				// Read in the group file one line at a time using a scanner
				Scanner scanner = new Scanner(levelGroupFile.read());

				// Loop through each line in the file
				while(scanner.hasNextLine()) {
					// Retrieve the next line from the file
					String levelData = scanner.nextLine();

					// Skip over comment lines that begin with #
					if(levelData == null ||
					   levelData.length() == 0 ||
					   levelData.length() > 0 && levelData.charAt(0) == '#') {
						continue;
					}
					
					// Discover the level information from this line and add it if its missing
					discoverLevel(levelGroupFilename, levelData);
				}

				// Close the scanner after we are done
				scanner.close();
			}
		}

		// If nothing was found, create a bunch of fake levels for debug purposes
		if(levels.size == 0) {
			for(int i=0;i<100;i++) {
				// Create level data object and attempt to add it
				LevelData levelData = new LevelData(i, DEFAULT_LEVEL+i, DEFAULT_LEVEL, i!=0);
				addLevel(levelData);
			}
		}
	}
	
	private void discoverLevel(String levelGroupFilename, String levelStringData) {
		if(levelStringData != null && levelStringData.length() > 0) {
			// Loop through each item in the file using commas and new line characters
			StringTokenizer st = new StringTokenizer(levelStringData, ",");
			while(st.hasMoreTokens()) {
				try {
					Integer id = Integer.parseInt(st.nextToken().trim());
					String name = st.nextToken().trim();
					String group = st.nextToken().trim();
					boolean locked = Boolean.parseBoolean(st.nextToken().trim());
					LevelData levelData = new LevelData(id, name, group, locked);
					addLevel(levelData);
				} catch(NumberFormatException nfe) {
					Gdx.app.error("PlayerData:discoverLevels", "Invalid group file '" + levelGroupFilename + "'", nfe);
				} catch(NoSuchElementException nse) {
					Gdx.app.error("PlayerData:discoverLevels", "Invalid group file '" + levelGroupFilename + "'", nse);
				} catch(Exception e) {
					Gdx.app.error("PlayerData:discoverLevels", "Invalid group file '" + levelGroupFilename + "'", e);
				}
			}
		}
	}
	
	private void addLevel(LevelData levelData) {
		if(!levels.containsKey(levelData.getTag())) {
			Gdx.app.debug("PlayerData:addLevel",
					"Adding id=" + levelData.id + ",name=" + levelData.name + ",group=" + levelData.group + ",locked=" + levelData.locked);
			levels.put(levelData.getTag(), levelData);
		}
	}
}
