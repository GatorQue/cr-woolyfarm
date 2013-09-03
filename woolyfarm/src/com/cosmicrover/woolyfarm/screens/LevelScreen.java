package com.cosmicrover.woolyfarm.screens;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.cosmicrover.core.GameManager;
import com.cosmicrover.core.assets.GameData;
import com.cosmicrover.core.assets.GroupData;
import com.cosmicrover.core.assets.loaders.JsonDataLoader;
import com.cosmicrover.core.screens.AbstractScreen;
import com.cosmicrover.core.screens.LoadingScreen;
import com.cosmicrover.core.ui.utils.AnimationDrawable;
import com.cosmicrover.woolyfarm.assets.MapData.Sprites;
import com.cosmicrover.woolyfarm.assets.WoolyLevelData;

public abstract class LevelScreen<G extends GroupData<WoolyLevelData>> extends AbstractScreen<WoolyLevelData,G> {
	/// Scene2d used by this Screen
	protected Stage stage = null;
	protected Table stageTable = null;
	private Table groundTable = null;
	private Table actionTable = null;
	private Table horizontalEdges = null;
	private Table verticalEdgesAndSquares = null;
	private Music music = null;
	protected Button restartButton = null;
	protected Button backButton = null;
	protected ButtonListener buttonListener = null;
	protected WoolyLevelData levelData = null;

	protected enum MapEdge {
		Horizontal,
		Vertical
	};
	
	/// Maps region name to AtlasRegion information to texture
	protected HashMap<String, AtlasRegion> spriteRegions;

	/// TextureAtlas that can carve up the sprite texture to show the correct texture
	private TextureAtlas spriteTextureAtlas;
	
	public LevelScreen(String screenName, int screenId, GameManager<WoolyLevelData,G> gameManager, int backScreenId) {
		super(screenName, screenId, gameManager, backScreenId);
	}

	@Override
	public void render(float delta) {
		// Use a basic gray color around the edges of the map
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
		Gdx.gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		// Handle our Stage2d processing here
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
        
        // Draw debug lines for table
        Table.drawDebug(stage);
	}

	@Override
	public void show() {
		// Call our base class implementation (sets our Back button screen)
		super.show();

		// Retrieve the level data and see if its loaded yet
		levelData = gameManager.data.getCurrentLevel();

		// Level never been created? then switch to LevelEditorScreen
		if(levelData == null) {
			// Not level editor? then create a new level and switch to level editor
			if(GameData.LEVEL_EDITOR_SCREEN != screenId) {
				// Retrieve the GroupData for the current group
				G groupData = gameManager.data.getCurrentGroup();
				
				// Get the next levelId value from the current group
				int levelId = groupData.levels.getSize();
				
				// Create a new level for our editor
				WoolyLevelData levelData = groupData.createLevel(levelId);

				// Create an empty level to edit
				levelData.createEmpty();

				// Set our level data flags
				levelData.locked = false;
				levelData.completed = false;
				levelData.loaded = true;
				
				// Create an empty level to edit
				groupData.levels.registerLevel(levelData);
	
				// Register this level as the current level
				gameManager.data.setCurrentLevel(levelData.getFilename());
	
				// Switch to level editor screen to create a new level for this group
				gameManager.setScreen(GameData.LEVEL_EDITOR_SCREEN);
			}
			// Level data was not created, switch back to previous screen
			else {
				Gdx.app.log("LevelScreen:show()", "screenId="+screenId+",null LevelData");
				gameManager.setScreen(getBackScreenId());
			}
		}
		// Check to see if our level data has been loaded yet
		else if(!levelData.loaded) {
			// Add this level file to our assetMaanger to load next
			gameManager.getAssetManager().load(levelData.getFilename(), WoolyLevelData.class,
	    			new JsonDataLoader.Parameters<WoolyLevelData>(levelData));
	    	
	    	// Tell the AssetLoadingScreen to switch back to us when its done
	    	LoadingScreen.setNextScreenId(GameData.LEVEL_PLAY_SCREEN);
	    	
	    	// Switch to the AssetLoadingScreen
	    	gameManager.setScreen(GameData.ASSET_LOADING_SCREEN);
		}
		else {
			// Create music object and start playing it now
			music = createMusic();
			if(music != null) {
				// Set looping flag to true and music volume
				music.setLooping(true);
				music.setVolume(0.5f);
				// Start playing the music track now
				music.play();
			}
			
			if(isFirstTime()) {
				// Create a hash map for looking up texture regions by string name found in SpriteComponent
				spriteRegions = new HashMap<String, AtlasRegion>();
				spriteTextureAtlas = gameManager.getAssetManager().get("textures/sprites.pack");
				// Create a map of each region available in our sprite TextureAtlas
				for (AtlasRegion region : spriteTextureAtlas.getRegions()) {
					spriteRegions.put(region.name, region);
				}
	
				// Create our stage objects for the first time
				createStage();
				
				// Clear our first time flag
				clearFirstTime();
			} else {
				// TODO: Enable existing entities used only for this Screen
			}
			
			// Reset our level
			onResetClick();
	
			// Add our Scene2d as an input processor
			gameManager.getInputMultiplexer().addProcessor(stage);
		}
	}

	@Override
	public void hide() {
		// First time flag has been cleared? then clean up
		if(!isFirstTime()) {
			// Stop our music from playing and dispose it
			if(music != null) {
				music.stop();
				music.dispose();
				music = null;
			}
			
			// Remove our Scene2d as an input processor
			gameManager.getInputMultiplexer().removeProcessor(stage);
		}
	}

	@Override
	public void resize(int width, int height) {
		// Handle any resizing needed here
		stage.setViewport(width, height, true);
	}

	@Override
	public void dispose() {
		// Remove our Scene2d as an input processor
		gameManager.getInputMultiplexer().removeProcessor(stage);
		
		// Give stage a chance to dispose itself
		stage.dispose();
	}

	protected Music createMusic() {
		return null;
	}

	protected void createStage() {
		// Create our stage object and buttons
		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true, gameManager.getSpriteBatch());
		
		// Create a single layout table and have it fill the screen/stage
        stageTable = new Table();
        stageTable.setFillParent(true);
        stage.addActor(stageTable);
        
		// Create our actor listeners
		buttonListener = new ButtonListener();
		
		// Create our information bar
		createInfoBar();
		
		// Create our map
		createMap();
		
		// Enable table debug lines
		//stageTable.debug();
	}
	
	protected abstract void createInfoBar();
	
	protected void createMap() {
		// Create a table to hold the background images
		groundTable = new Table();
		//groundTable.debug();
		
		// Create a table to hold the animals and fence post images
		actionTable = new Table();
		//actionTable.debug();

		// Create a table to hold the fences and fence post images
		horizontalEdges = new Table();
		//fenceHorizontalTable.debug();
		verticalEdgesAndSquares = new Table();
		//fenceVerticalTable.debug();

		// Create a stack to hold our two tables above
		Stack scrollStack = new Stack();
		scrollStack.add(groundTable);
		scrollStack.add(horizontalEdges);
		scrollStack.add(verticalEdgesAndSquares);
		scrollStack.add(actionTable);
		
		// Create a scroll pane for the list of level buttons
		ScrollPane levelScrollPane = new ScrollPane(scrollStack);
		stageTable.add(levelScrollPane).colspan(7).expand().fill();
		stageTable.row();
	}
	
	protected abstract void onOtherClick(Actor actor);
	
	protected abstract void onMapSquareClick(int row, int col);
	
	protected abstract void onMapEdgeClick(int row, int col, MapEdge mapEdge);
	
	protected void onResetClick() {
		// Reset the level
		levelData.resetLevel();
	}

	protected void updateLevelMap() {
		// First clear our ground, animal, and fence tables of all their images
		groundTable.clear();
		actionTable.clear();
		horizontalEdges.clear();
		verticalEdgesAndSquares.clear();
		
		// Create a single TextureRegion for each type of graphic to be displayed
        TextureRegionDrawable dirt1 = new TextureRegionDrawable(spriteRegions.get(Sprites.GroundDirt1.toString()));
        TextureRegionDrawable dirt2 = new TextureRegionDrawable(spriteRegions.get(Sprites.GroundDirt2.toString()));
        TextureRegionDrawable grass1 = new TextureRegionDrawable(spriteRegions.get(Sprites.GroundGrass1.toString()));
        TextureRegionDrawable grass2 = new TextureRegionDrawable(spriteRegions.get(Sprites.GroundGrass2.toString()));
        TextureRegionDrawable rock1 = new TextureRegionDrawable(spriteRegions.get(Sprites.GroundRock1.toString()));
        TextureRegionDrawable rock2 = new TextureRegionDrawable(spriteRegions.get(Sprites.GroundRock2.toString()));
        AnimationDrawable water1 = new AnimationDrawable(new Animation(3.0f, spriteTextureAtlas.findRegions(Sprites.GroundWater1.toString())));
        water1.setPlayMode(Animation.LOOP);
        AnimationDrawable water2 = new AnimationDrawable(new Animation(3.0f, spriteTextureAtlas.findRegions(Sprites.GroundWater2.toString())));
        water2.setPlayMode(Animation.LOOP);
        TextureRegionDrawable fencePost = new TextureRegionDrawable(spriteRegions.get(Sprites.FencePost.toString()));
        TextureRegionDrawable fenceHorizontal = new TextureRegionDrawable(spriteRegions.get(Sprites.FenceHorizontal.toString()));
        TextureRegionDrawable fenceVertical = new TextureRegionDrawable(spriteRegions.get(Sprites.FenceVertical.toString()));
        TextureRegionDrawable fenceEmptyHorizontal = new TextureRegionDrawable(spriteRegions.get(Sprites.FenceHorizontalEmpty.toString()));
        TextureRegionDrawable fenceEmptyVertical = new TextureRegionDrawable(spriteRegions.get(Sprites.FenceVerticalEmpty.toString()));
        TextureRegionDrawable fenceBrokenHorizontal = new TextureRegionDrawable(spriteRegions.get(Sprites.FenceHorizontalBroken.toString()));
        TextureRegionDrawable fenceBrokenVertical = new TextureRegionDrawable(spriteRegions.get(Sprites.FenceVerticalBroken.toString()));
        TextureRegionDrawable horizontalSpacer = new TextureRegionDrawable(spriteRegions.get(Sprites.FenceHorizontalSpacer.toString()));
        TextureRegionDrawable horizontalPostSpacer = new TextureRegionDrawable(spriteRegions.get(Sprites.FencePostHorizontalSpacer.toString()));
        TextureRegionDrawable postSpacer = new TextureRegionDrawable(spriteRegions.get(Sprites.FencePostSpacer.toString()));
        TextureRegionDrawable verticalSpacer = new TextureRegionDrawable(spriteRegions.get(Sprites.FenceVerticalSpacer.toString()));
        TextureRegionDrawable animalNone = new TextureRegionDrawable(spriteRegions.get(Sprites.AnimalNone.toString()));

        // Now create the animal map data
		for(int row=0;row<levelData.current.rows; row++) {
			for(int col=0; col<levelData.current.cols; col++) {
				// Select alternating grass image
				Image anGroundImage;
				switch(levelData.current.ground[row][col]) {
				case GroundDirt1:
					anGroundImage = new Image(dirt1);
					break;
				case GroundDirt2:
					anGroundImage = new Image(dirt2);
					break;
				case GroundGrass1:
					anGroundImage = new Image(grass1);
					break;
				case GroundGrass2:
					anGroundImage = new Image(grass2);
					break;
				case GroundRock1:
					anGroundImage = new Image(rock1);
					break;
				case GroundRock2:
					anGroundImage = new Image(rock2);
					break;
				default:
					Gdx.app.error("LevelScreen:updateLevelMap", "Unknown background type");
				case GroundWater1:
					anGroundImage = new Image(water1);
					break;
				case GroundWater2:
					anGroundImage = new Image(water2);
					break;
				}

				// Add the button to the table
				groundTable.add(anGroundImage);
			}
			groundTable.row();
		}

		// Create animals and fence post layer
		for(int row=0;row<levelData.current.rows+1; row++) {
			// First row? then add fence post spacers and horizontal spacers to vertical fence table
			if(row == 0) {
				for(int col=0; col<levelData.current.cols; col++) {
					verticalEdgesAndSquares.add(new Image(postSpacer));
					verticalEdgesAndSquares.add(new Image(horizontalSpacer));
				}
				// Add final fence post spacer to fence table
				verticalEdgesAndSquares.add(new Image(postSpacer));
				verticalEdgesAndSquares.row();
			}
			
			// Add fence posts and horizontal spacers to animal table
			for(int col=0; col<levelData.current.cols; col++) {
				// Add initial fence post and horizontal spacer to the animal table
				actionTable.add(new Image(fencePost));
				Button anButtonHorizontal = new Button(horizontalSpacer);
				anButtonHorizontal.addListener(buttonListener);
				anButtonHorizontal.setName("he"+((row*(levelData.current.cols+1)) + col));
				actionTable.add(anButtonHorizontal);

				if(col == 0) {
					// Add horizontal post spacer to fence table
					horizontalEdges.add(new Image(horizontalPostSpacer));
				}
				// Add horizontal fence
				Image anFenceHorizontal;
				switch(levelData.current.horizontal[row][col]) {
				case FenceHorizontalBroken:
					anFenceHorizontal = new Image(fenceBrokenHorizontal);
					break;
				case FenceHorizontal:
					anFenceHorizontal = new Image(fenceHorizontal);
					break;
				default:
					Gdx.app.error("LevelPlayScreen:updateLevelMap", "Unknown horizontal fence type");
				case FenceHorizontalEmpty:
					anFenceHorizontal = new Image(fenceEmptyHorizontal);
					break;
				}
				anFenceHorizontal.setName("he"+((row*(levelData.current.cols+1)) + col));
				horizontalEdges.add(anFenceHorizontal);
			}
			// Add final fence post to the animal table
			actionTable.add(new Image(fencePost));
			actionTable.row();
			// Add final fence post spacer to fence table
			horizontalEdges.add(new Image(horizontalPostSpacer));
			horizontalEdges.row();

			// Skip the last row of vertical spacers and animals
			if(row < levelData.current.rows) {
				// Add vertical spacers and animals to animal table
				for(int col=0; col<levelData.current.cols; col++) {
					// Add vertical edge button to action table
					Button anButtonVertical = new Button(verticalSpacer);
					anButtonVertical.addListener(buttonListener);
					anButtonVertical.setName("ve"+((row*(levelData.current.cols+1)) + col));
					actionTable.add(anButtonVertical);

					// Add map square button to action table
					Button anButtonSquare = new Button(horizontalSpacer);
					anButtonSquare.addListener(buttonListener);
					anButtonSquare.setName("ms"+((row*(levelData.current.cols+1)) + col));
					actionTable.add(anButtonSquare);

					// Add vertical fence
					Image anFenceVertical;
					switch(levelData.current.vertical[row][col]) {
					case FenceVerticalBroken:
						anFenceVertical = new Image(fenceBrokenVertical);
						break;
					case FenceVertical:
						anFenceVertical = new Image(fenceVertical);
						break;
					default:
						Gdx.app.error("LevelPlayScreen:updateLevelMap", "Unknown vertical fence type");
					case FenceVerticalEmpty:
						anFenceVertical = new Image(fenceEmptyVertical);
						break;
					}
					anFenceVertical.setName("ve"+((row*(levelData.current.cols+1)) + col));
					verticalEdgesAndSquares.add(anFenceVertical);
					
					// Create animations for each animal type
			        AnimationDrawable animalDuck = new AnimationDrawable(new Animation(0.7f, spriteTextureAtlas.findRegions(Sprites.AnimalDuck.toString())));
			        animalDuck.setPlayMode(Animation.LOOP);
			        animalDuck.setRandomStart();
			        AnimationDrawable animalGoat = new AnimationDrawable(new Animation(0.7f, spriteTextureAtlas.findRegions(Sprites.AnimalGoat.toString())));
			        animalGoat.setPlayMode(Animation.LOOP);
			        animalGoat.setRandomStart();
			        AnimationDrawable animalPig = new AnimationDrawable(new Animation(0.7f, spriteTextureAtlas.findRegions(Sprites.AnimalPig.toString())));
			        animalPig.setPlayMode(Animation.LOOP);
			        animalPig.setRandomStart();
			        AnimationDrawable animalSheep = new AnimationDrawable(new Animation(0.7f, spriteTextureAtlas.findRegions(Sprites.AnimalSheep.toString())));
			        animalSheep.setPlayMode(Animation.LOOP);
			        animalSheep.setRandomStart();
			        AnimationDrawable animalWolf = new AnimationDrawable(new Animation(0.7f, spriteTextureAtlas.findRegions(Sprites.AnimalWolf.toString())));
			        animalWolf.setPlayMode(Animation.LOOP);
			        animalWolf.setRandomStart();
			        
					Image anMapSquare;
					switch(levelData.current.animals[row][col]) {
					case AnimalDuck:
						anMapSquare = new Image(animalDuck);
						break;
					case AnimalGoat:
						anMapSquare = new Image(animalGoat);
						break;
					case AnimalPig:
						anMapSquare = new Image(animalPig);
						break;
					case AnimalSheep:
						anMapSquare = new Image(animalSheep);
						break;
					case AnimalWolf:
						anMapSquare = new Image(animalWolf);
						break;
					default:
						Gdx.app.error("LevelPlayScreen:updateLevelMap", "Unknown animal type");
					case AnimalNone:
						anMapSquare = new Image(animalNone);
						break;
					}
					anMapSquare.setName("ms"+((row*(levelData.current.cols+1)) + col));
					verticalEdgesAndSquares.add(anMapSquare);
				}
				// Add final vertical spacer on animal table
				Button anButtonVertical = new Button(verticalSpacer);
				anButtonVertical.addListener(buttonListener);
				anButtonVertical.setName("ve"+((row*(levelData.current.cols+1)) + levelData.current.cols));
				actionTable.add(anButtonVertical);
				actionTable.row();
				// Add final vertical spacer to horizontal fence table
				horizontalEdges.add(new Image(verticalSpacer)).colspan(levelData.current.cols*2);
				horizontalEdges.row();

				// Add final vertical fence
				Image anFenceVertical;
				switch(levelData.current.vertical[row][levelData.current.cols]) {
				case FenceVerticalBroken:
					anFenceVertical = new Image(fenceBrokenVertical);
					// Don't add listener for broken fences
					break;
				case FenceVertical:
					anFenceVertical = new Image(fenceVertical);
					break;
				default:
					Gdx.app.error("LevelPlayScreen:updateLevelMap", "Unknown vertical fence type");
				case FenceVerticalEmpty:
					anFenceVertical = new Image(fenceEmptyVertical);
					break;
				}
				anFenceVertical.setName("ve"+((row*(levelData.current.cols+1)) + levelData.current.cols));
				verticalEdgesAndSquares.add(anFenceVertical);
				verticalEdgesAndSquares.row();
			}
		}
		// Add final row of spacers to vertical fence table
		for(int col=0; col<levelData.current.cols; col++) {
			verticalEdgesAndSquares.add(new Image(postSpacer));
			verticalEdgesAndSquares.add(new Image(horizontalSpacer));
		}
		// Add final fence post spacer to fence table
		verticalEdgesAndSquares.add(new Image(postSpacer));
		verticalEdgesAndSquares.row();
	}

	/**
	 * ButtonListener is a general purpose button listener for all the buttons
	 * in the MainMenuScreen.
	 */
	private class ButtonListener extends ChangeListener {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			if(actor.equals(restartButton)) {
				Gdx.app.log("LevelScreen:ButtonListener", "Restart");
				onResetClick();
			} else if(actor.equals(backButton)) {
				Gdx.app.log("LevelScreen:ButtonListener", "Back");
				gameManager.setScreen(getBackScreenId());
			} else if(actor.getName() != null) {
				// Is this an map square being clicked?
				if(actor.getName().startsWith("ms")) {
					// Find the actor in our vertical fence and animal table
					Actor anButton = verticalEdgesAndSquares.findActor(actor.getName());
					if(anButton != null) {
						// Toggle the fence at the location specified in the name (minus the ac characters)
						int location = Integer.parseInt(anButton.getName().substring(2));
						int row = location / (levelData.current.cols+1);
						int col = location % (levelData.current.cols+1);
						onMapSquareClick(row, col);
					}
				}
				// Is this a vertical edge being clicked?
				else if(actor.getName().startsWith("ve")) {
					// Find the actor in our fence table
					Actor anButton = verticalEdgesAndSquares.findActor(actor.getName());
					if(anButton != null) {
						// Toggle the fence at the location specified in the name (minus the vf characters)
						int location = Integer.parseInt(anButton.getName().substring(2));
						int row = location / (levelData.current.cols+1);
						int col = location % (levelData.current.cols+1);
						onMapEdgeClick(row, col, MapEdge.Vertical);
					}
				}
				// Is this a horizontal edge being clicked?
				else if(actor.getName().startsWith("he")) {
					// Find the actor in our fence table
					Actor anButton = horizontalEdges.findActor(actor.getName());
					if(anButton != null) {
						// Toggle the fence at the location specified in the name (minus the hf characters)
						int location = Integer.parseInt(anButton.getName().substring(2));
						int row = location / (levelData.current.cols+1);
						int col = location % (levelData.current.cols+1);
						onMapEdgeClick(row, col, MapEdge.Horizontal);
					}
				}
			}
			// Something else we don't know
			else
			{
				onOtherClick(actor);
			}
		}
	}
}
