package com.cosmicrover.core;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.cosmicrover.core.GameEnvironment.Platform;
import com.cosmicrover.core.assets.GameData;
import com.cosmicrover.core.assets.GroupData;
import com.cosmicrover.core.assets.LevelData;
import com.cosmicrover.core.assets.loaders.JsonDataLoader;
import com.cosmicrover.core.screens.AbstractScreen;

public class GameManager<L extends LevelData, G extends GroupData<L>> implements Disposable {
    public GameData<L,G> data = null;
    private final AssetManager assetManager;
	private final Game game;
    private final GameEnvironment gameEnvironment;
	private final InputMultiplexer inputMultiplexer;
	private SpriteBatch spriteBatch = null;
	private ShapeRenderer shapeRenderer = null;
	private OrthogonalTiledMapRenderer mapRenderer = null;
	private BackButtonHandler backButtonHandler = null;
    
    /**
     * Creates the GameManager class that is responsible for providing
     * game data persistence services (save/load of game data information).
     * @param[in] the Game derived parent class needed to change Screens
     * @param[in] the GameEnvironment class used to provide environment information
     */
    public GameManager(Game game, GameEnvironment gameEnvironment) {
    	this.assetManager = new AssetManager();
    	this.game = game;
    	this.gameEnvironment = gameEnvironment;
    	this.inputMultiplexer = new InputMultiplexer();
    }
    
    /**
	 * @return the assetManager
	 */
	public final AssetManager getAssetManager() {
		return assetManager;
	}

	public final InputMultiplexer getInputMultiplexer() {
		return inputMultiplexer;
	}

	/**
	 * @return the spriteBatch
	 */
	public final SpriteBatch getSpriteBatch() {
		if(spriteBatch == null) {
			spriteBatch = new SpriteBatch();
		}
		return spriteBatch;
	}
	
	/**
	 * @return the shapeRenderer
	 */
	public final ShapeRenderer getShapeRenderer() {
		if(shapeRenderer == null) {
			shapeRenderer = new ShapeRenderer();
		}
		return shapeRenderer;
	}
	
	/**
	 * @return the OrthogonalTiledMapRenderer
	 */
	public final OrthogonalTiledMapRenderer getMapRenderer(TiledMap tiledMap) {
		if(tiledMap != null) {
			if(mapRenderer == null) {
				mapRenderer = new OrthogonalTiledMapRenderer(tiledMap, getSpriteBatch());
			} else {
				// Change the tiledMap to use for our renderer
				mapRenderer.setMap(tiledMap);
			}
		}
		return mapRenderer;
	}

	/**
     * Returns the GameEnvironment class provided at construction time.
     * @return GameEnvironment class used to determine the runtime environment
     */
    public final GameEnvironment getEnvironment() {
    	return gameEnvironment;
    }

    /**
     * Changes the current screen being displayed to the screenId specified.
     * @param screenId to change to
     */
    public final void setScreen(int screenId) {
    	// Does the user want to exit the application? then schedule an exit now
    	if(GameData.EXIT_GAME_SCREEN == screenId) {
    		// Log the application exit request and schedule an exit now
        	Gdx.app.debug( "GameManager:setScreen()", "Exit application requested("+screenId+")");
    		Gdx.app.exit();
    	} else {
    		// Retrieve our AbstractScreen base class for the screenId provided
    		AbstractScreen<L,G> anScreen = data.getScreen(screenId);
    		game.setScreen(anScreen);

    		// Log the change of screens event
        	Gdx.app.debug( "GameManager:setScreen()", "Changing to " + anScreen.getName() + "(" + screenId + ")");
    	}
    }
    
    /**
     * Sets the screenId to use when the Back button is pressed.
     * @param screenId to use on Back button
     */
    public final void setBackScreen(int screenId) {
    	if(backButtonHandler != null) {
        	// Does the user want to exit the application? then schedule an exit now
        	if(GameData.EXIT_GAME_SCREEN == screenId) {
        		// Log the application exit request and schedule an exit now
            	Gdx.app.debug( "GameManager:setBackScreen()", "Setting back button to Exit Application");
        	} else {
        		// Retrieve the screen specified and log the back button change
        		AbstractScreen<L,G> anScreen = data.getScreen(screenId);
            	Gdx.app.debug( "GameManager:setBackScreen()", "Setting back button to " + anScreen.getName() + "(" + screenId + ")");
        	}
        	// Tell the BackButtonHandler about the back button change
    		backButtonHandler.setScreenId(screenId);
    	}
    }
    
    /**
     * Sets the back button handling to the value of enabled. Sometimes you
     * want to disable the back button (loading screen, credits, etc) from
     * changing screens. Other times you want to handle the back button on
     * your own. This class allows you to disable the default back button
     * handler.
     * @param enabled for back button handling
     */
    public final void setBackScreenHandling(boolean enabled) {
    	if(backButtonHandler != null) {
    		backButtonHandler.setEnabled(enabled);
    	}
    }

    /**
     * This method is responsible for creating the GameData derived class type
     * specified and calling its initGame() method.
     * 
     * @param[in] type of GameData derived class to create
     */
	@SuppressWarnings("rawtypes")
	public final void initData(GameData<L,G> data) {
    	if( this.data != null ) {
    		Gdx.app.error("GameManager:init", "GameData object already exists, replacing.");
    	}
    	// Update our GameData object
    	this.data = data;

		// Call the init method on this new instance
		data.init(this);
    			
		// Add ourselves as a handler for the GameData class
		assetManager.setLoader(GameData.class, new JsonDataLoader<GameData>());

		// Add the data to our assetMaanger to load next
    	assetManager.load(data.getFilename(), GameData.class,
    			new JsonDataLoader.Parameters<GameData>(data));

        // Make our inputMultiplexer the primary input listener
	    Gdx.input.setInputProcessor(this.inputMultiplexer);

	    // Do we need a back button handler?
    	if(Platform.Android == gameEnvironment.getPlatform() ||
    	   Platform.iOS == gameEnvironment.getPlatform()) {
    		// Register our handler with our InputMultiplexer
    		backButtonHandler = new BackButtonHandler();
    		inputMultiplexer.addProcessor(backButtonHandler);

    		// Make sure we catch the Back key (Android/iOS) input events
    		Gdx.input.setCatchBackKey(true);
    	}
    }
 
    /**
     * This method is responsible for saving the game data previously created
     * or restored above.
     * @param filename and path to where the data will be saved.
     */
    public final void saveData() {
    	JsonDataLoader.save(data);
    }

	@Override
	public void dispose() {
	    assetManager.dispose();
	    if(spriteBatch != null) {
	    	spriteBatch.dispose();
	    }
	    if(shapeRenderer != null) {
			shapeRenderer.dispose();
	    }
	    if(mapRenderer != null) {
			mapRenderer.dispose();
	    }
	    if(data != null) {
	    	data = null;
	    }
	};

	protected final class BackButtonHandler implements InputProcessor {
    	/// The screenId to use when back button is pressed (defaults to exit game)
    	private int screenId = GameData.EXIT_GAME_SCREEN;
    	
    	/// Is the back button response currently enabled?
    	private boolean enabled = true;
    	
    	/**
    	 * Set the screenId to switch to when the back button is pressed (use
    	 * GameData.EXIT_GAME_SCREEN if you want the program to exit instead).
    	 * @param screenId to change to when Back button is pressed
    	 */
    	public void setScreenId(int screenId) {
    		this.screenId = screenId;
    	}

    	/**
    	 * Enable or disable the handling of the back button by this input
    	 * processor class.
    	 * @param enabled value to use
    	 */
    	public void setEnabled(boolean enabled) {
    		this.enabled = enabled;
    	}

    	/////////////////////////////////////////////////////////////////////////
    	// Keyboard inputs
    	/////////////////////////////////////////////////////////////////////////
		@Override
		public boolean keyDown(int keycode) {
			boolean consumed = false;

			if(keycode == Input.Keys.BACK) {
				if(enabled) {
					// Set our screen to the backScreenId set earlier
					setScreen(screenId);
				} else {
					// Log the back button press but do nothing
					Gdx.app.debug("BackButtonHandler", "Back Button pressed, but disabled");
				}
				
				// Set our consumed flag to true
				consumed = true;
			}

			// Return the consumed flag set above
			return consumed;
		}

		@Override
		public boolean keyUp(int keycode) {
			return false;
		}

		@Override
		public boolean keyTyped(char character) {
			return false;
		}

		/////////////////////////////////////////////////////////////////////////
		// Mouse/Touch inputs
		/////////////////////////////////////////////////////////////////////////
		@Override
		public boolean touchDown(int screenX, int screenY, int pointer,
				int button) {
			return false;
		}

		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			return false;
		}

		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {
			return false;
		}

		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			return false;
		}

		@Override
		public boolean scrolled(int amount) {
			return false;
		}
    }
}
