package com.cosmicrover.woolyfarm.screens;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.cosmicrover.core.GameEnvironment.Platform;
import com.cosmicrover.core.GameManager;
import com.cosmicrover.core.screens.AbstractLoadingScreen;
import com.cosmicrover.core.screens.AbstractScreen;
import com.cosmicrover.woolyfarm.LevelData;
import com.cosmicrover.woolyfarm.LevelData.Sprites;
import com.cosmicrover.woolyfarm.LevelDataLoader;
import com.cosmicrover.woolyfarm.PlayerData;

public class LevelPlayScreen extends AbstractScreen {
	/// Scene2d used by this Screen
	private Stage stage = null;
	private Image background = null;
	private Table stageTable = null;
	private Label fencesLabel = null;
	private Label dogsLabel = null;
	private Label levelNameLabel = null;
	private Table groundTable = null;
	private Table actionTable = null;
	private Table fenceHorizontalTable = null;
	private Table fenceVerticalTable = null;
	private Music music = null;
	private ScrollPane levelScrollPane = null;
	private Button restartButton = null;
	private Button backButton = null;
	private ButtonListener buttonListener = null;
	private LevelData levelData = null;

	private enum FenceDirection {
		Horizontal,
		Vertical
	};
	
	/// Maps region name to AtlasRegion information to texture
	private HashMap<String, AtlasRegion> spriteRegions;

	/// TextureAtlas that can carve up the sprite texture to show the correct texture
	private TextureAtlas spriteTextureAtlas;
	
	public LevelPlayScreen(GameManager gameManager, int screenId) {
		super("LevelPlayScreen", gameManager, screenId);
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

		// Retrieve the level data for this game
		levelData = gameManager.getData(PlayerData.class).getCurrentLevel();

		// Check to see if our level data has been loaded yet
		if(!levelData.loaded) {
			// Add this level file to our assetMaanger to load next
	    	gameManager.getAssetManager().load(
	    			LevelData.LEVEL_DIRECTORY+LevelData.LEVEL_NAME+levelData.id+LevelData.LEVEL_EXT,
	    			LevelData.class,
	    			new LevelDataLoader.Parameters(levelData));
	    	
	    	// Tell the AssetLoadingScreen to switch back to us when its done
	    	AbstractLoadingScreen.setNextScreenId(PlayerData.LEVEL_PLAY_SCREEN);
	    	
	    	// Switch to the AssetLoadingScreen
	    	gameManager.setScreen(PlayerData.ASSET_LOADING_SCREEN);
		}
		else {
			music = Gdx.audio.newMusic(Gdx.files.internal("music/level_play.mp3"));
			music.setLooping(true);
			music.play();
			
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
			resetLevel();
	
			// Add our Scene2d as an input processor
			gameManager.getInputMultiplexer().addProcessor(stage);
		}
	}

	@Override
	public void hide() {
		// Stop our music from playing and dispose it
		if(music != null) {
			music.stop();
			music.dispose();
			music = null;
		}
		
		// TODO: Disable or remove entities created by show method above.

		// Remove our Scene2d as an input processor
		gameManager.getInputMultiplexer().removeProcessor(stage);
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
	
	private void createStage() {
		// Create our stage object and buttons
		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true, gameManager.getSpriteBatch());
		
		// Create a background image and have it fill the screen/stage
		background = new Image();
		background.setFillParent(true);

		// Create a single layout table and have it fill the screen/stage
        stageTable = new Table();
        stageTable.setFillParent(true);
        stage.addActor(background);
        stage.addActor(stageTable);
        
        // Retrieve the font we will use for text messages
		Texture fontTexture = new Texture(Gdx.files.internal("textures/font_normal.png"));
		fontTexture.setFilter(TextureFilter.Linear, TextureFilter.MipMapLinearLinear);
		TextureRegion fontRegion = new TextureRegion(fontTexture);
		BitmapFont labelFont = new BitmapFont(Gdx.files.internal("fonts/normal.fnt"), fontRegion, false);
		BitmapFont buttonFont = new BitmapFont(Gdx.files.internal("fonts/normal.fnt"), fontRegion, false);
		buttonFont.setUseIntegerPositions(false);

		// Create our LabelStyle
		LabelStyle labelStyle = new LabelStyle();
		labelStyle.font = labelFont;
		labelStyle.fontColor = Color.CYAN;

		// Create our TextButtonStyle
		TextButtonStyle buttonStyle = new TextButtonStyle();
		buttonStyle.font = buttonFont;
		buttonStyle.fontColor = Color.WHITE;
		buttonStyle.overFontColor = Color.YELLOW;
		
		// Create our actor listeners
		buttonListener = new ButtonListener();
		
        // Create our information bar starting by gathering texture regions for each icon in our information bar
        TextureRegionDrawable fencesIcon = new TextureRegionDrawable(spriteRegions.get("fences_icon"));
        TextureRegionDrawable dogsIcon = new TextureRegionDrawable(spriteRegions.get("dogs_icon"));
        TextureRegionDrawable backIcon = new TextureRegionDrawable(spriteRegions.get("back_icon"));
        //TextureRegionDrawable nextIcon = new TextureRegionDrawable(spriteRegions.get("next_icon"));
        TextureRegionDrawable resetIcon = new TextureRegionDrawable(spriteRegions.get("restart_icon"));

		// Add back button on left (for non mobile platforms)
		if(Platform.Android != gameManager.getEnvironment().getPlatform() &&
		   Platform.iOS != gameManager.getEnvironment().getPlatform()) {
			backButton = new Button(backIcon);
			backButton.addListener(buttonListener);
			stageTable.add(backButton).left().spaceRight(5.0f).fillY();
		}

		// Add fences icon and number of fences remaining label
		stageTable.add(new Image(fencesIcon)).fillY().right(); //.width(64.0f).height(64.0f);
		fencesLabel = new Label(""+levelData.numFences, labelStyle);
		stageTable.add(fencesLabel).left().spaceLeft(5.0f).spaceRight(5.0f);
		
		// Add dogs icon and number of dogs remaining label
		stageTable.add(new Image(dogsIcon)).fillY().right().spaceLeft(5.0f); //.width(64.0f).height(64.0f).spaceLeft(5.0f);
		dogsLabel = new Label(""+levelData.numDogs, labelStyle);
		stageTable.add(dogsLabel).left().spaceLeft(5.0f);
		
		// Add level name
		levelNameLabel = new Label(levelData.name, labelStyle);
		stageTable.add(levelNameLabel).center().spaceLeft(5.0f).spaceRight(5.0f);

		// Add our restart button last of all
		restartButton = new Button(resetIcon);
		restartButton.addListener(buttonListener);
		stageTable.add(restartButton).right().fillY();
		stageTable.row();

		// Create a table to hold the background images
		groundTable = new Table();
		//groundTable.debug();
		
		// Create a table to hold the animals and fence post images
		actionTable = new Table();
		//actionTable.debug();

		// Create a table to hold the fences and fence post images
		fenceHorizontalTable = new Table();
		//fenceHorizontalTable.debug();
		fenceVerticalTable = new Table();
		//fenceVerticalTable.debug();

		// Create a stack to hold our two tables above
		Stack scrollStack = new Stack();
		scrollStack.add(groundTable);
		scrollStack.add(fenceHorizontalTable);
		scrollStack.add(fenceVerticalTable);
		scrollStack.add(actionTable);
		
		// Create a scroll pane for the list of level buttons
		levelScrollPane = new ScrollPane(scrollStack);
		stageTable.add(levelScrollPane).colspan(7).expand().fill();
		stageTable.row();

		// Enable table debug lines
		//stageTable.debug();
	}
	
	private void resetLevel() {
		// Reset the level
		
		// Update our level map
		updateLevelMap();
		
		// Update our fences label
		fencesLabel.setText("" + levelData.numFences);
		
		// Update our dogs label
		dogsLabel.setText("" + levelData.numDogs);
	}
	
	private void toggleAnimal(int row, int col) {
		Gdx.app.log("toggleAnimal", "A:row="+row+",col="+col+",value="+levelData.mapAnimals[row][col]);
		switch(levelData.mapAnimals[row][col]) {
		case AnimalDuck:
			levelData.mapAnimals[row][col] = Sprites.AnimalGoat;
			break;
		case AnimalGoat:
			levelData.mapAnimals[row][col] = Sprites.AnimalPig;
			break;
		case AnimalPig:
			levelData.mapAnimals[row][col] = Sprites.AnimalSheep;
			break;
		case AnimalSheep:
			levelData.mapAnimals[row][col] = Sprites.AnimalWolf;
			break;
		case AnimalWolf:
			levelData.mapAnimals[row][col] = Sprites.AnimalNone;
			break;
		default:
			Gdx.app.error("toggleAnimal", "Unknown animal type");
		case AnimalNone:
			levelData.mapAnimals[row][col] = Sprites.AnimalDuck;
			break;
		}

		// Update our level map
		updateLevelMap();
	}
	
	private void toggleFence(int row, int col, FenceDirection fenceDirection) {
		// Even row? must be a horizontal fence to be placed
		if(FenceDirection.Horizontal == fenceDirection) {
			Gdx.app.log("toggleFence", "H:row="+row+",col="+col+",value="+levelData.mapFenceHorizontal[row][col]);
			// Toggle the fence type at the location indicated
			if(Sprites.FenceHorizontalEmpty == levelData.mapFenceHorizontal[row][col] && levelData.numFences > 0) {
				levelData.mapFenceHorizontal[row][col] = Sprites.FenceHorizontal;
				levelData.numFences--;
			}
			else if(Sprites.FenceHorizontal == levelData.mapFenceHorizontal[row][col]) {
				levelData.mapFenceHorizontal[row][col] = Sprites.FenceHorizontalEmpty;
				levelData.numFences++;
			}
		}
		// Odd row? must be a vertical fence to be placed
		else {
			Gdx.app.log("toggleFence", "V:row="+row+",col="+col+",value="+levelData.mapFenceVertical[row][col]);
			// Toggle the fence type at the location indicated
			if(Sprites.FenceVerticalEmpty == levelData.mapFenceVertical[row][col] && levelData.numFences > 0) {
				levelData.mapFenceVertical[row][col] = Sprites.FenceVertical;
				levelData.numFences--;
			}
			else if(Sprites.FenceVertical == levelData.mapFenceVertical[row][col]) {
				levelData.mapFenceVertical[row][col] = Sprites.FenceVerticalEmpty;
				levelData.numFences++;
			}
		}
		
		// Update our level map
		updateLevelMap();
		
		// Update our fence count label
		fencesLabel.setText(""+levelData.numFences);
	}
	
	private void updateLevelMap() {
		// First clear our ground, animal, and fence tables of all their images
		groundTable.clear();
		actionTable.clear();
		fenceHorizontalTable.clear();
		fenceVerticalTable.clear();
		
		// Create a single TextureRegion for each type of graphic to be displayed
        TextureRegionDrawable dirt1 = new TextureRegionDrawable(spriteRegions.get(Sprites.GroundDirt1.toString()));
        TextureRegionDrawable dirt2 = new TextureRegionDrawable(spriteRegions.get(Sprites.GroundDirt2.toString()));
        TextureRegionDrawable grass1 = new TextureRegionDrawable(spriteRegions.get(Sprites.GroundGrass1.toString()));
        TextureRegionDrawable grass2 = new TextureRegionDrawable(spriteRegions.get(Sprites.GroundGrass2.toString()));
        TextureRegionDrawable rock1 = new TextureRegionDrawable(spriteRegions.get(Sprites.GroundRock1.toString()));
        TextureRegionDrawable rock2 = new TextureRegionDrawable(spriteRegions.get(Sprites.GroundRock2.toString()));
        TextureRegionDrawable water1 = new TextureRegionDrawable(spriteRegions.get(Sprites.GroundWater1.toString()));
        TextureRegionDrawable water2 = new TextureRegionDrawable(spriteRegions.get(Sprites.GroundWater2.toString()));
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
        TextureRegionDrawable animalDuck = new TextureRegionDrawable(spriteRegions.get(Sprites.AnimalDuck.toString()));
        TextureRegionDrawable animalGoat = new TextureRegionDrawable(spriteRegions.get(Sprites.AnimalGoat.toString()));
        TextureRegionDrawable animalNone = new TextureRegionDrawable(spriteRegions.get(Sprites.AnimalNone.toString()));
        TextureRegionDrawable animalPig = new TextureRegionDrawable(spriteRegions.get(Sprites.AnimalPig.toString()));
        TextureRegionDrawable animalSheep = new TextureRegionDrawable(spriteRegions.get(Sprites.AnimalSheep.toString()));
        TextureRegionDrawable animalWolf = new TextureRegionDrawable(spriteRegions.get(Sprites.AnimalWolf.toString()));
        
		// Now create the animal map data
		for(int row=0;row<levelData.mapRows; row++) {
			for(int col=0; col<levelData.mapCols; col++) {
				// Select alternating grass image
				Button anGroundButton;
				switch(levelData.origGround[row][col]) {
				case GroundDirt1:
					anGroundButton = new Button(dirt1);
					break;
				case GroundDirt2:
					anGroundButton = new Button(dirt2);
					break;
				case GroundGrass1:
					anGroundButton = new Button(grass1);
					break;
				case GroundGrass2:
					anGroundButton = new Button(grass2);
					break;
				case GroundRock1:
					anGroundButton = new Button(rock1);
					break;
				case GroundRock2:
					anGroundButton = new Button(rock2);
					break;
				case GroundWater1:
					anGroundButton = new Button(water1);
					break;
				case GroundWater2:
					anGroundButton = new Button(water2);
					break;
				default:
					Gdx.app.error("LevelPlayScreen:updateLevelMap", "Unknown background type");
					anGroundButton = new Button();
					break;
				}

				// Add the button to the table
				groundTable.add(anGroundButton);
			}
			groundTable.row();
		}

		// Create animals and fence post layer
		for(int row=0;row<levelData.mapRows+1; row++) {
			// First row? then add fence post spacers and horizontal spacers to vertical fence table
			if(row == 0) {
				for(int col=0; col<levelData.mapCols; col++) {
					fenceVerticalTable.add(new Image(postSpacer));
					fenceVerticalTable.add(new Image(horizontalSpacer));
				}
				// Add final fence post spacer to fence table
				fenceVerticalTable.add(new Image(postSpacer));
				fenceVerticalTable.row();
			}
			
			// Add fence posts and horizontal spacers to animal table
			for(int col=0; col<levelData.mapCols; col++) {
				// Add initial fence post and horizontal spacer to the animal table
				actionTable.add(new Image(fencePost));
				Button anButtonHorizontal = new Button(horizontalSpacer);
				anButtonHorizontal.addListener(buttonListener);
				anButtonHorizontal.setName("hf"+((row*levelData.fenceCols) + col));
				actionTable.add(anButtonHorizontal);

				if(col == 0) {
					// Add horizontal post spacer to fence table
					fenceHorizontalTable.add(new Image(horizontalPostSpacer));
				}
				// Add horizontal fence
				Image anFenceHorizontal;
				switch(levelData.mapFenceHorizontal[row][col]) {
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
				anFenceHorizontal.setName("hf"+((row*levelData.fenceCols) + col));
				fenceHorizontalTable.add(anFenceHorizontal);
			}
			// Add final fence post to the animal table
			actionTable.add(new Image(fencePost));
			actionTable.row();
			// Add final fence post spacer to fence table
			fenceHorizontalTable.add(new Image(horizontalPostSpacer));
			fenceHorizontalTable.row();

			// Skip the last row of vertical spacers and animals
			if(row < levelData.mapRows) {
				// Add vertical spacers and animals to animal table
				for(int col=0; col<levelData.mapCols; col++) {
					// Add vertical edge button to action table
					Button anButtonVertical = new Button(verticalSpacer);
					anButtonVertical.addListener(buttonListener);
					anButtonVertical.setName("vf"+((row*levelData.fenceCols) + col));
					actionTable.add(anButtonVertical);

					// Add map square button to action table
					Button anButtonSquare = new Button(horizontalSpacer);
					anButtonSquare.addListener(buttonListener);
					anButtonSquare.setName("ac"+((row*levelData.fenceCols) + col));
					actionTable.add(anButtonSquare);

					// Add vertical fence
					Image anFenceVertical;
					switch(levelData.mapFenceVertical[row][col]) {
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
					anFenceVertical.setName("vf"+((row*levelData.fenceCols) + col));
					fenceVerticalTable.add(anFenceVertical);
					
					Image anMapSquare;
					switch(levelData.mapAnimals[row][col]) {
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
					anMapSquare.setName("ac"+((row*levelData.fenceCols) + col));
					fenceVerticalTable.add(anMapSquare);
				}
				// Add final vertical spacer on animal table
				Button anButtonVertical = new Button(verticalSpacer);
				anButtonVertical.addListener(buttonListener);
				anButtonVertical.setName("vf"+((row*levelData.fenceCols) + levelData.mapCols));
				actionTable.add(anButtonVertical);
				actionTable.row();
				// Add final vertical spacer to horizontal fence table
				fenceHorizontalTable.add(new Image(verticalSpacer)).colspan(levelData.mapCols*2);
				fenceHorizontalTable.row();

				// Add final vertical fence
				Image anFenceVertical;
				switch(levelData.mapFenceVertical[row][levelData.mapCols]) {
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
				anFenceVertical.setName("vf"+((row*levelData.fenceCols) + levelData.mapCols));
				fenceVerticalTable.add(anFenceVertical);
				fenceVerticalTable.row();
			}
		}
		// Add final row of spacers to vertical fence table
		for(int col=0; col<levelData.mapCols; col++) {
			fenceVerticalTable.add(new Image(postSpacer));
			fenceVerticalTable.add(new Image(horizontalSpacer));
		}
		// Add final fence post spacer to fence table
		fenceVerticalTable.add(new Image(postSpacer));
		fenceVerticalTable.row();
	}

	/**
	 * ButtonListener is a general purpose button listener for all the buttons
	 * in the MainMenuScreen.
	 */
	private class ButtonListener extends ChangeListener {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			if(actor.equals(restartButton)) {
				Gdx.app.log("LevelPlayScreen:ButtonListener", "Restart Level");
				resetLevel();
			} else if(actor.equals(backButton)) {
				Gdx.app.log("LevelPlayScreen:ButtonListener", "Back");
				gameManager.setScreen(getBackScreenId());
			} else {
				// Is this a vertical fence button?
				if(actor.getName().startsWith("vf")) {
					// Find the actor in our fence table
					Actor anButton = fenceVerticalTable.findActor(actor.getName());
					if(anButton != null) {
						// Toggle the fence at the location specified in the name (minus the vf characters)
						int location = Integer.parseInt(anButton.getName().substring(2));
						int row = location / (levelData.fenceCols);
						int col = location % (levelData.fenceCols);
						toggleFence(row, col, FenceDirection.Vertical);
					}
				} else if(actor.getName().startsWith("hf")) {
					// Find the actor in our fence table
					Actor anButton = fenceHorizontalTable.findActor(actor.getName());
					if(anButton != null) {
						// Toggle the fence at the location specified in the name (minus the hf characters)
						int location = Integer.parseInt(anButton.getName().substring(2));
						int row = location / (levelData.fenceCols);
						int col = location % (levelData.fenceCols);
						toggleFence(row, col, FenceDirection.Horizontal);
					}
				} else if(actor.getName().startsWith("ac")) {
					// Find the actor in our vertical fence and animal table
					Actor anButton = fenceVerticalTable.findActor(actor.getName());
					if(anButton != null) {
						// Toggle the fence at the location specified in the name (minus the ac characters)
						int location = Integer.parseInt(anButton.getName().substring(2));
						int row = location / (levelData.fenceCols);
						int col = location % (levelData.fenceCols);
						toggleAnimal(row, col);
					}
				}
			}
		}
	}
}
