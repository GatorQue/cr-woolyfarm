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
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ArrayMap;
import com.cosmicrover.core.GameManager;
import com.cosmicrover.core.GameEnvironment.Platform;
import com.cosmicrover.core.screens.AbstractScreen;
import com.cosmicrover.woolyfarm.LevelData;
import com.cosmicrover.woolyfarm.PlayerData;

public class LevelSelectScreen extends AbstractScreen {
	/// Scene2d used by this Screen
	private Stage stage = null;
	private Image background = null;
	private Table stageTable = null;
	private Label title = null;
	private Table levelTable = null;
	private ScrollPane levelScrollPane = null;
	private TextButton backButton = null;
	private BitmapFont buttonFont = null;
	private ButtonListener buttonListener = null;

	/// Maps region name to AtlasRegion information to texture
	private HashMap<String, AtlasRegion> levelRegions;

	/// TextureAtlas that can carve up the sprite texture to show the correct texture
	private TextureAtlas levelTextureAtlas;

	public LevelSelectScreen(GameManager gameManager, int screenId) {
		super("LevelSelectScreen", gameManager, screenId);
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
		
		if(isFirstTime()) {
			// Create a hash map for looking up texture regions by string name found in SpriteComponent
			levelRegions = new HashMap<String, AtlasRegion>();
			levelTextureAtlas = gameManager.getAssetManager().get("textures/level_select.pack");
			// Create a map of each region available in our sprite TextureAtlas
			for (AtlasRegion region : levelTextureAtlas.getRegions()) {
				levelRegions.put(region.name, region);
			}

			// Create our stage objects on first time
			createStage();
			
			// Clear our first time flag
			clearFirstTime();
		} else {
			// TODO: Enable existing entities used only for this Screen
		}

		// Create our table of level buttons to click on
		createLevelButtons(buttonFont, buttonListener);
		
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
        // Retrieve the font we will use for text messages
		Texture fontTexture = new Texture(Gdx.files.internal("textures/font_normal.png"));
		fontTexture.setFilter(TextureFilter.Linear, TextureFilter.MipMapLinearLinear);
		TextureRegion fontRegion = new TextureRegion(fontTexture);
		BitmapFont labelFont = new BitmapFont(Gdx.files.internal("fonts/normal.fnt"), fontRegion, false);
		labelFont.setUseIntegerPositions(false);
		buttonFont = new BitmapFont(Gdx.files.internal("fonts/normal.fnt"), fontRegion, false);
		buttonFont.setUseIntegerPositions(false);

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
        
		// Create our LabelStyle
		LabelStyle labelStyle = new LabelStyle();
		labelStyle.font = labelFont;
		labelStyle.fontColor = Color.CYAN;

		// Create and add our Title Label
		title = new Label("Level Select", labelStyle);
		stageTable.add(title).center().expandX();
		stageTable.row();

		// Create our TextButtonStyle
		TextButtonStyle textButtonStyle = new TextButtonStyle();
		//style.up = new TextureRegionDrawable(upRegion);
		//style.down = new TextureRegionDrawable(downRegion);
		textButtonStyle.font = buttonFont;
		textButtonStyle.fontColor = Color.WHITE;
		textButtonStyle.overFontColor = Color.YELLOW;
		
		// Create our button listener
		buttonListener = new ButtonListener();

		// Create a table to hold our list of levels to choose from
		levelTable = new Table();
		
		// Create a scroll pane for the list of level buttons
		levelScrollPane = new ScrollPane(levelTable);
		stageTable.add(levelScrollPane).expand().fill();
		stageTable.row();

		if(Platform.Android != gameManager.getEnvironment().getPlatform() &&
				   Platform.iOS != gameManager.getEnvironment().getPlatform()) {
			// Add our back button last of all
			backButton = new TextButton("Back", textButtonStyle);
			backButton.addListener(buttonListener);
			stageTable.add(backButton).expandY().fill();
		}

		// Enable table debug lines
		stageTable.debug();
	}

	private void createLevelButtons(BitmapFont buttonFont, ButtonListener buttonListener) {
		int column = 0;
		int maxColumn = Gdx.graphics.getWidth() / (64 + 20); // TODO: Replace with Texture.getWidth + padding;
		System.out.println("maxColumn="+maxColumn);

		// Retrieve our PlayerData class to obtain textures
		PlayerData playerData = gameManager.getData(PlayerData.class);
		
		// Create our button style
		TextButtonStyle unlockedButtonStyle = new TextButtonStyle();
		unlockedButtonStyle.font = buttonFont;
		unlockedButtonStyle.fontColor = Color.BLACK;
		unlockedButtonStyle.overFontColor = Color.BLUE;
		unlockedButtonStyle.disabled = new TextureRegionDrawable(levelRegions.get("level_locked"));
		unlockedButtonStyle.up = new TextureRegionDrawable(levelRegions.get("level_unlocked_up"));
		unlockedButtonStyle.down = new TextureRegionDrawable(levelRegions.get("level_unlocked_down"));
		unlockedButtonStyle.over = new TextureRegionDrawable(levelRegions.get("level_unlocked_over"));

		TextButtonStyle completedButtonStyle = new TextButtonStyle();
		completedButtonStyle.font = buttonFont;
		completedButtonStyle.fontColor = Color.BLACK;
		completedButtonStyle.overFontColor = Color.BLUE;
		completedButtonStyle.disabled = new TextureRegionDrawable(levelRegions.get("level_locked"));
		completedButtonStyle.up = new TextureRegionDrawable(levelRegions.get("level_completed_up"));
		completedButtonStyle.down = new TextureRegionDrawable(levelRegions.get("level_completed_down"));
		completedButtonStyle.over = new TextureRegionDrawable(levelRegions.get("level_completed_over"));

		// Clear our table of level buttons
		levelTable.clear();
		
		// Loop through each level and create a new button
		ArrayMap<String, LevelData> levels = playerData.getLevels();
		for(int i=0, iSize = levels.size; iSize > i; i++) {
			// Retrieve the level data information
			LevelData anLevel = levels.getValueAt(i);

			// Create a button to represent this level
			TextButton anButton;
			if(anLevel.completed) {
				anButton = new TextButton(""+anLevel.id, completedButtonStyle);
			} else {
				anButton = new TextButton(""+anLevel.id, unlockedButtonStyle);
			}
			anButton.setName(anLevel.getTag());
			anButton.setDisabled(anLevel.locked);
			anButton.addListener(buttonListener);
			anButton.pad(10.0f);

			// Now add the level button to the list to be displayed
			levelTable.add(anButton).center().expand();
			
			// Increment our column count and check against maximum
			if(++column >= maxColumn) {
				column = 0;
				levelTable.row();
			}
		}

		// Enable debug lines
		levelTable.debug();
	}
	
	/**
	 * ButtonListener is a general purpose button listener for all the buttons
	 * in the MainMenuScreen.
	 */
	private class ButtonListener extends ChangeListener {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			if(actor.equals(backButton)) {
				Gdx.app.log("LevelSelectScreen:ButtonListener", "Back");
				gameManager.setScreen(getBackScreenId());
			} else {
				Actor anButton = levelTable.findActor(actor.getName());
				if(anButton != null) {
					Gdx.app.log("LevelSelectScreen:ButtonListener", anButton.getName());
					gameManager.getData(PlayerData.class).setCurrentLevel(anButton.getName());
					gameManager.setScreen(PlayerData.LEVEL_PLAY_SCREEN);
				}
			}
		}
	}
}
