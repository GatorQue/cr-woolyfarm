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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.cosmicrover.core.GameManager;
import com.cosmicrover.core.assets.GameData;
import com.cosmicrover.core.assets.GroupData;
import com.cosmicrover.core.screens.AbstractScreen;
import com.cosmicrover.woolyfarm.assets.WoolyLevelData;

public class LevelEndScreen<G extends GroupData<WoolyLevelData>> extends AbstractScreen<WoolyLevelData,G> {
	/// Scene2d used by this Screen
	private Stage stage = null;
	private Table stageTable = null;
	private Image background = null;
	private Label title = null;
	private TextButton nextButton = null;
	private TextButton backButton = null;
	private WoolyLevelData levelData = null;

	/// Maps region name to AtlasRegion information to texture
	private HashMap<String, AtlasRegion> spriteRegions;

	/// TextureAtlas that can carve up the sprite texture to show the correct texture
	private TextureAtlas spriteTextureAtlas;
	
	public LevelEndScreen(GameManager<WoolyLevelData,G> gameManager, int backScreenId) {
		super("LevelEndScreen", GameData.LEVEL_END_SCREEN, gameManager, backScreenId);
		
		// Note the creation of each screen in our debug log
		Gdx.app.debug("LevelEndScreen", "Creating Level End screen");
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
			Gdx.app.log("LevelEndScreen:show()", "screenId="+screenId+",null LevelData");
			gameManager.setScreen(GameData.LEVEL_SELECT_SCREEN);
		}
		// Check to see if our level data has been loaded yet
		else if(!levelData.loaded) {
			Gdx.app.log("LevelEndScreen:show()", "screenId="+screenId+",LevelData not loaded?");
			gameManager.setScreen(GameData.LEVEL_SELECT_SCREEN);
		}
		else {
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

			// Update our stage
			updateStage();
			
			// Add our Scene2d as an input processor
			gameManager.getInputMultiplexer().addProcessor(stage);
		}
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
	}
	
	private void updateStage() {
		// Clear the previous stage table
		stageTable.clear();
		
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
		//style.up = new TextureRegionDrawable(upRegion);
		//style.down = new TextureRegionDrawable(downRegion);
		buttonStyle.font = buttonFont;
		buttonStyle.fontColor = Color.WHITE;
		buttonStyle.overFontColor = Color.YELLOW;
		
		// Create our button listener
		ButtonListener buttonListener = new ButtonListener();

		// Create and add our Title Label
		System.out.println("numFences="+levelData.current.numFences+",numDogs="+levelData.current.numDogs);
		if(levelData.current.isLevelDone()) {
			// Mark level complete
			levelData.completed = true;

			// TODO: Unlock the next level to play
			
			// TODO: Save group information for this level being completed

			// Add title
			title = new Label("Congratulations!", labelStyle);
			stageTable.center().add(title).expandX();
			stageTable.row();

			// Add our next button
			nextButton = new TextButton("Next", buttonStyle);
			nextButton.addListener(buttonListener);
			stageTable.right().add(nextButton).expandY().fill();
			stageTable.row();
		} else {
			title = new Label("Try Again?", labelStyle);
			stageTable.center().add(title).expandX();
			stageTable.row();

			// Add our retry/back button
			backButton = new TextButton("Retry", buttonStyle);
			backButton.addListener(buttonListener);
			stageTable.right().add(backButton).expandY().fill();
			stageTable.row();
		}

		// Enable table debug lines
		stageTable.debug();
	}
	
	@Override
	public void hide() {
		// First time flag has been cleared? then clean up
		if(!isFirstTime()) {
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
	
	/**
	 * ButtonListener is a general purpose button listener for all the buttons
	 * in the MainMenuScreen.
	 */
	private class ButtonListener extends ChangeListener {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			if(actor.equals(nextButton)) {
				Gdx.app.log("LevelEndScreen:ButtonListener", "Next");

				// Retrieve the GroupData for the current group
				G groupData = gameManager.data.getCurrentGroup();
				
				// Get the next levelId value from the current group
				int levelId = groupData.levels.getSize();

				// Did we finish all the levels in the group? switch to GroupSelect screen
				if(levelData.levelId+1 == levelId) {
					// Switch to group select screen
					gameManager.setScreen(GameData.GROUP_SELECT_SCREEN);
				} else {
					// Retrieve the next level filename
					gameManager.data.setCurrentLevel(groupData.getNextLevel(levelData.levelId+1));
					
					// Switch to level play screen
					gameManager.setScreen(GameData.LEVEL_PLAY_SCREEN);
				}
			} else if(actor.equals(backButton)) {
				Gdx.app.log("OptionsScreen:ButtonListener", "Back");
				gameManager.setScreen(getBackScreenId());
			}
		}
	}
}
