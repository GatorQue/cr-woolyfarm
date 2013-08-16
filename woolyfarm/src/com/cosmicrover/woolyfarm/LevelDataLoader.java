package com.cosmicrover.woolyfarm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.cosmicrover.core.screens.LoadingScreen;

public class LevelDataLoader extends AsynchronousAssetLoader<LevelData, LevelDataLoader.Parameters> {
	public LevelDataLoader() {
		super(new InternalFileHandleResolver());
	}

	/** Creates loader
	 * 
	 * @param resolver */
	public LevelDataLoader (FileHandleResolver resolver) {
		super(resolver);
	}

    /**
     * This method is responsible for saving the game data previously created
     * or restored above.
     * @param filename and path to where the data will be saved.
     */
	public static final void save(LevelData levelData, String filename) {
    	if(levelData != null) {
        	Gdx.app.log( "LevelDataLoader:save", "Saving level to '" + filename + "'" );

            // create the JSON utility object
            Json json = new Json();

            // create the handle for the profile data file
            FileHandle levelDataFile = Gdx.files.external(filename);

            // Attempt to compress and save the game data
            try {
                // Retrieve data and encode as Base64 and write it to a file
                levelDataFile.writeString(Base64Coder.encodeString(json.toJson(levelData)), false);
    		} catch (Exception e) {
                // log the exception
            	Gdx.app.error( "LevelDataLoader:save", "Unable to save level file '" + filename + "'", e);
    		}
    	} else {
        	Gdx.app.error( "LevelDataLoader:save", "LevelData provided is null");
    	}
    }

	@Override
	public void loadAsync(AssetManager manager, String fileName, Parameters parameter) {
		// Make sure a gameData object was provided for us to load into
		if(parameter.levelData != null) {
			FileHandle levelDataFile = resolve(fileName);
	
	        // check if the game data file exists
	        if( levelDataFile.exists() ) {
	        	Gdx.app.log( "LevelDataLoader:loadAsync", "Retrieving level from '" + fileName + "'" );
	
	            // load the game data from the file
	            try {
	            	// Read data and convert it from Base64 back to text and parse
	            	parameter.levelData.read(new Json(), new JsonReader().parse(
	            			Base64Coder.decodeString(levelDataFile.readString())));
	            } catch( Exception e ) {
	                // log the exception
	            	Gdx.app.error("LevelDataLoader:loadAsync",	"Error while parsing data from '" + fileName + "'", e);

	            	// Mark this level locked
	            	parameter.levelData.locked = true;

	            	// Explicitly unload this asset
		        	manager.unload(fileName);
		        	
		        	// Switch to LevelSelectScreen
		        	LoadingScreen.setNextScreenId(PlayerData.LEVEL_SELECT_SCREEN);
	            }
	        } else {
	        	// Indicate that the game data file doesn't exist
	        	Gdx.app.error("LevelDataLoader:loadAsync", "Level file '" + fileName + "' doesn't exist");
	        	
            	// TODO: Create an empty level if the file doesn't exist
	        	parameter.levelData.createEmpty(11, 11);
	        	
//            	// Mark this level locked
//            	parameter.levelData.locked = true;
//
//	        	// Explicitly unload this asset
//	        	manager.unload(fileName);
//	        	
	        	// Switch to LevelSelectScreen
	        	LoadingScreen.setNextScreenId(PlayerData.LEVEL_EDITOR_SCREEN);
	        }
		} else {
			Gdx.app.error("LevelDataLoader:loadAsync", "LevelData parameter missing");

			// Explicitly unload this asset
        	manager.unload(fileName);
        	
        	// Switch to LevelSelectScreen
        	LoadingScreen.setNextScreenId(PlayerData.LEVEL_SELECT_SCREEN);
		}
	}

	@Override
	public LevelData loadSync(AssetManager manager, String fileName, Parameters parameter) {
		return parameter.levelData;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies(String fileName,
			Parameters parameter) {
		// TODO: Replace this with a list of maps, textures, or other asset
		// resources that need to be loaded too
//		Array<AssetDescriptor> dependencies = new Array<AssetDescriptor>();
//		try {
//			FileHandle tmxFile = resolve(fileName);
//			root = xml.parse(tmxFile);
//			boolean generateMipMaps = (parameter != null ? parameter.generateMipMaps : false);
//			TextureLoader.TextureParameter texParams = new TextureParameter();
//			texParams.genMipMaps = generateMipMaps;
//			if (parameter != null) {
//				texParams.minFilter = parameter.textureMinFilter;
//				texParams.magFilter = parameter.textureMagFilter;
//			}
//			for (FileHandle image : loadTilesets(root, tmxFile)) {
//				dependencies.add(new AssetDescriptor(image.path(), Texture.class, texParams));
//			}
//			return dependencies;
//		} catch (IOException e) {
//			throw new GdxRuntimeException("Couldn't load tilemap '" + fileName + "'", e);
//		}
		return null;
	}

	public static class Parameters extends AssetLoaderParameters<LevelData> {
		public LevelData levelData = null;
		public Parameters(LevelData levelData) {
			this.levelData = levelData;
		}
	}
}
