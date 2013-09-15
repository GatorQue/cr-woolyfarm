package com.cosmicrover.core.screens;

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
import com.badlogic.gdx.utils.ArrayMap.Keys;
import com.cosmicrover.core.GameManager;
import com.cosmicrover.core.GameEnvironment.Platform;
import com.cosmicrover.core.LevelManager;
import com.cosmicrover.core.assets.GameData;
import com.cosmicrover.core.assets.GroupData;
import com.cosmicrover.core.assets.LevelData;
import com.cosmicrover.woolyfarm.assets.WoolyGameData;

public class LevelSelectScreen<L extends LevelData, G extends GroupData<L>> extends AbstractScreen<L,G> {
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
	private G groupData = null;

	/// Maps region name to AtlasRegion information to texture
	private HashMap<String, AtlasRegion> levelRegions;

	/// TextureAtlas that can carve up the sprite texture to show the correct texture
	private TextureAtlas levelTextureAtlas;

	public LevelSelectScreen(GameManager<L,G> gameManager, int backScreenId) {
		super("LevelSelectScreen", GameData.LEVEL_SELECT_SCREEN, gameManager, backScreenId);
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

		// Retrieve the groupName and GroupData object for the current group
		groupData = gameManager.data.getCurrentGroup();

		// GroupData doesn't yet exist? then create and register it now
		if(groupData == null) {
			// Assign groupId using current size of groups registered
			int groupId = gameManager.data.groups.getSize();
			
			// Create new GroupData object
			groupData = gameManager.data.createGroup(groupId);

			// Register new GroupData object with gameManager
			gameManager.data.groups.registerGroup(groupData);
		}
		
		// First time? then create our stage object and other UI elements
		if(isFirstTime()) {
			// Create a hash map for looking up texture regions by string name found in SpriteComponent
			levelRegions = new HashMap<String, AtlasRegion>();
			levelTextureAtlas = gameManager.getAssetManager().get("level_select.pack");
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
		// First time flag has been cleared? then clean up
		if(!isFirstTime()) {
			// Clear our table of level buttons
			levelTable.clear();
			
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

	private void createStage() {
        // Retrieve the font we will use for text messages
		Texture fontTexture = new Texture(Gdx.files.internal("fonts/font_normal.png"));
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

		TextButtonStyle createButtonStyle = new TextButtonStyle();
		createButtonStyle.font = buttonFont;
		createButtonStyle.fontColor = Color.BLACK;
		createButtonStyle.overFontColor = Color.BLUE;
		createButtonStyle.disabled = new TextureRegionDrawable(levelRegions.get("level_locked"));
		createButtonStyle.up = new TextureRegionDrawable(levelRegions.get("level_create_up"));
		createButtonStyle.down = new TextureRegionDrawable(levelRegions.get("level_create_down"));
		createButtonStyle.over = new TextureRegionDrawable(levelRegions.get("level_create_over"));
		
		// Clear our table of level buttons
		levelTable.clear();

		// Retrieve a list of levels available
		Keys<String> levels = groupData.levels.getLevels();
		for(String levelName : levels) {
			LevelData levelData = groupData.levels.getLevel(levelName);

			// Create a button to represent this level
			TextButton anButton;
			if(levelData.completed) {
				anButton = new TextButton(""+levelData.levelId, completedButtonStyle);
			} else {
				anButton = new TextButton(""+levelData.levelId, unlockedButtonStyle);
			}
			anButton.setName(levelData.getFilename());
			anButton.setDisabled(levelData.locked);
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
		
		// Add our create new level button
		TextButton anButton = new TextButton("*"+groupData.levels.getSize(), createButtonStyle);
		anButton.setName(LevelManager.getFilename(groupData.groupId, groupData.levels.getSize()));
		anButton.setDisabled(false); // TODO: Replace false with lookup to see if level editor is enabled
		anButton.addListener(buttonListener);
		anButton.pad(10.0f);

		// Now add the level button to the list to be displayed
		levelTable.add(anButton).center().expand();
		
		// Increment our column count and check against maximum
		if(++column >= maxColumn) {
			column = 0;
			levelTable.row();
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
					gameManager.data.setCurrentLevel(anButton.getName());
					gameManager.setScreen(WoolyGameData.LEVEL_PLAY_SCREEN);
				}
			}
		}
	}
}
