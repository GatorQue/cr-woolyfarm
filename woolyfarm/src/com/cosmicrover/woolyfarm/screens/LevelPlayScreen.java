package com.cosmicrover.woolyfarm.screens;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.cosmicrover.core.GameEnvironment.Platform;
import com.cosmicrover.core.GameManager;
import com.cosmicrover.core.screens.AbstractScreen;
import com.cosmicrover.woolyfarm.LevelData;
import com.cosmicrover.woolyfarm.LevelData.Backgrounds;
import com.cosmicrover.woolyfarm.LevelData.Fence;
import com.cosmicrover.woolyfarm.PlayerData;
import com.esotericsoftware.tablelayout.Cell;

public class LevelPlayScreen extends AbstractScreen {
	/// Scene2d used by this Screen
	private Stage stage = null;
	private Image background = null;
	private Table stageTable = null;
	private Label fencesLabel = null;
	private Label dogsLabel = null;
	private Label levelNameLabel = null;
	private Table animalTable = null;
	private Table fenceTable = null;
	private ScrollPane levelScrollPane = null;
	private TextButton restartButton = null;
	private TextButton backButton = null;
	private ButtonListener buttonListener = null;
	private LevelData levelData = null;

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
		
		// Update our level map
		updateLevelMap();

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
		
		// Add our Scene2d as an input processor
		gameManager.getInputMultiplexer().addProcessor(stage);
	}

	@Override
	public void hide() {
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
		
		// Create our button listener
		buttonListener = new ButtonListener();
		
		// Create our information bar

		// Add back button on left
		if(Platform.Android != gameManager.getEnvironment().getPlatform() &&
		   Platform.iOS != gameManager.getEnvironment().getPlatform()) {
			backButton = new TextButton("B", buttonStyle);
			backButton.addListener(buttonListener);
			stageTable.add(backButton).left().spaceRight(5.0f);
		}

		// Add fences icon and number of fences remaining label
		stageTable.add(new Image(spriteRegions.get("fences_icon"))).right().width(32.0f);
		fencesLabel = new Label(""+levelData.numFences, labelStyle);
		stageTable.add(fencesLabel).left().spaceRight(5.0f);
		
		// Add dogs icon and number of dogs remaining label
		stageTable.add(new Image(spriteRegions.get("dogs_icon"))).right().width(32.0f).spaceLeft(5.0f);
		dogsLabel = new Label(""+levelData.numDogs, labelStyle);
		stageTable.add(dogsLabel).left();
		
		// Add level name
		levelNameLabel = new Label(levelData.name, labelStyle);
		stageTable.add(levelNameLabel).center().spaceLeft(5.0f).spaceRight(5.0f).expandX();

		// Add our restart button last of all
		restartButton = new TextButton("R", buttonStyle);
		restartButton.addListener(buttonListener);
		stageTable.add(restartButton).right();
		stageTable.row();

		// Create a table to hold the animals and background images
		animalTable = new Table();
		animalTable.debug();
		
		// Create a table to hold the fences and fence post images
		fenceTable = new Table();
		fenceTable.debug();

		// Create a stack to hold our two tables above
		Stack scrollStack = new Stack();
		scrollStack.add(animalTable);
		scrollStack.add(fenceTable);
		
		// Create a scroll pane for the list of level buttons
		levelScrollPane = new ScrollPane(scrollStack);
		stageTable.add(levelScrollPane).colspan(7).expandY().fill();
		stageTable.row();

		// Enable table debug lines
		stageTable.debug();
	}
	
	private void updateLevelMap() {
		// First clear our animal and fence tables of all its images
		animalTable.clear();
		fenceTable.clear();
		
		// Create a single TextureRegion for each type of graphic to be displayed
        TextureRegionDrawable dirt1 = new TextureRegionDrawable(spriteRegions.get(Backgrounds.Dirt1.toString()));
        TextureRegionDrawable dirt2 = new TextureRegionDrawable(spriteRegions.get(Backgrounds.Dirt2.toString()));
        TextureRegionDrawable grass1 = new TextureRegionDrawable(spriteRegions.get(Backgrounds.Grass1.toString()));
        TextureRegionDrawable grass2 = new TextureRegionDrawable(spriteRegions.get(Backgrounds.Grass2.toString()));
        TextureRegionDrawable rock1 = new TextureRegionDrawable(spriteRegions.get(Backgrounds.Rock1.toString()));
        TextureRegionDrawable rock2 = new TextureRegionDrawable(spriteRegions.get(Backgrounds.Rock2.toString()));
        TextureRegionDrawable water1 = new TextureRegionDrawable(spriteRegions.get(Backgrounds.Water1.toString()));
        TextureRegionDrawable water2 = new TextureRegionDrawable(spriteRegions.get(Backgrounds.Water2.toString()));
        TextureRegionDrawable fencePost = new TextureRegionDrawable(spriteRegions.get("fence_post"));
        TextureRegionDrawable fenceHorizontal = new TextureRegionDrawable(spriteRegions.get(Fence.Horizontal.toString()));
        TextureRegionDrawable fenceVertical = new TextureRegionDrawable(spriteRegions.get(Fence.Vertical.toString()));
        TextureRegionDrawable fenceEmptyHorizontal = new TextureRegionDrawable(spriteRegions.get(Fence.EmptyHorizontal.toString()));
        TextureRegionDrawable fenceEmptyVertical = new TextureRegionDrawable(spriteRegions.get(Fence.EmptyVertical.toString()));
        TextureRegionDrawable fenceBrokenHorizontal = new TextureRegionDrawable(spriteRegions.get(Fence.BrokenHorizontal.toString()));
        TextureRegionDrawable fenceBrokenVertical = new TextureRegionDrawable(spriteRegions.get(Fence.BrokenVertical.toString()));
        
		// Now create the animal map data
		for(int row=0;row<levelData.rows; row++) {
			for(int col=0; col<levelData.cols; col++) {
				// Select alternating grass image
				Button anAnimalButton;
				//System.out.println(levelData.mapGround[row][col]);
				switch(levelData.mapGround[row][col]) {
				case Dirt1:
					anAnimalButton = new Button(dirt1);
					break;
				case Dirt2:
					anAnimalButton = new Button(dirt2);
					break;
				case Grass1:
					anAnimalButton = new Button(grass1);
					break;
				case Grass2:
					anAnimalButton = new Button(grass2);
					break;
				case Rock1:
					anAnimalButton = new Button(rock1);
					break;
				case Rock2:
					anAnimalButton = new Button(rock2);
					break;
				case Water1:
					anAnimalButton = new Button(water1);
					break;
				case Water2:
					anAnimalButton = new Button(water2);
					break;
				default:
					//System.out.println("Unknown background type");
					anAnimalButton = new Button();
					break;
				}

				// Add the button to the table
				animalTable.add(anAnimalButton);
			}
			animalTable.row();
		}

		// Now create the fence post map data
		for(int row=0;row<levelData.rows*2+1; row++) {
			for(int col=0; col<levelData.cols; col++) {
				// Even rows gets posts and fences
				if(row % 2 == 0) {
					// Add initial fence post to the fence table
					fenceTable.add(new Image(fencePost));
					// Add horizontal fence
					Button anFenceHorizontal;
					//System.out.println("row=" + row + ",col=" + col + ",fence=" + levelData.mapFences[row][col]);
					switch(levelData.mapFences[row][col]) {
					case BrokenHorizontal:
						anFenceHorizontal = new Button(fenceBrokenHorizontal);
						break;
					case EmptyHorizontal:
						anFenceHorizontal = new Button(fenceEmptyHorizontal);
						break;
					case Horizontal:
						anFenceHorizontal = new Button(fenceHorizontal);
						break;
					default:
						//System.out.println("Unknown horizontal fence type");
						anFenceHorizontal = new Button();
						break;
					}
					fenceTable.add(anFenceHorizontal);
				}
				// Odd rows get vertical fences
				else {
					// Add initial vertical fence
					Button anFenceVertical;
					//System.out.println("row=" + row + ",col=" + col + ",fence=" + levelData.mapFences[row][col]);
					switch(levelData.mapFences[row][col]) {
					case BrokenVertical:
						anFenceVertical = new Button(fenceBrokenVertical);
						break;
					case EmptyVertical:
						anFenceVertical = new Button(fenceEmptyVertical);
						break;
					case Vertical:
						anFenceVertical = new Button(fenceVertical);
						break;
					default:
						//System.out.println("Unknown vertical fence type");
						anFenceVertical = new Button();
						break;
					}
					fenceTable.add(anFenceVertical);
					// Add an empty square
					fenceTable.add();
				}
			}
			// Even rows get final fence post
			if(row % 2 == 0) {
				// Add initial fence post to the fence table
				fenceTable.add(new Image(fencePost));
			}
			// Odd rows get final vertical fence post
			else {
				// Add initial vertical fence
				Button anFenceVertical;
				//System.out.println("Special row=" + row + ",col=" + levelData.cols + ",fence=" + levelData.mapFences[row][levelData.cols]);
				switch(levelData.mapFences[row][levelData.cols]) {
				case BrokenVertical:
					anFenceVertical = new Button(fenceBrokenVertical);
					break;
				case EmptyVertical:
					anFenceVertical = new Button(fenceEmptyVertical);
					break;
				case Vertical:
					anFenceVertical = new Button(fenceVertical);
					break;
				default:
					//System.out.println("Unknown vertical fence type");
					anFenceVertical = new Button();
					break;
				}
				fenceTable.add(anFenceVertical);
			}
			fenceTable.row();
		}
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
				gameManager.getData().resetGame();
			} else if(actor.equals(backButton)) {
				Gdx.app.log("LevelPlayScreen:ButtonListener", "Back");
				gameManager.setScreen(getBackScreenId());
			}
		}
	}
}
