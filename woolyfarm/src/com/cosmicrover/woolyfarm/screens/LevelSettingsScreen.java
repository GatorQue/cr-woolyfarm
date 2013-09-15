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
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.cosmicrover.core.GameManager;
import com.cosmicrover.core.GameEnvironment.Platform;
import com.cosmicrover.core.assets.GameData;
import com.cosmicrover.core.screens.AbstractScreen;
import com.cosmicrover.woolyfarm.assets.WoolyGroupData;
import com.cosmicrover.woolyfarm.assets.WoolyLevelData;

public class LevelSettingsScreen extends AbstractScreen<WoolyLevelData, WoolyGroupData> {
	/// Scene2d used by this Screen
	private Stage stage = null;
	private Image background = null;
	private Table stageTable = null;
	private Label titleLabel = null;
	private Label rowsLabel = null;
	private Label colsLabel = null;
	private TextField levelName = null;
	private TextButton minusRowButton = null;
	private TextButton plusRowButton = null;
	private TextButton minusColButton = null;
	private TextButton plusColButton = null;
	private TextButton editMapButton = null;
	private TextButton backButton = null;
	protected WoolyLevelData levelData = null;

	/// Maps region name to AtlasRegion information to texture
	protected HashMap<String, AtlasRegion> spriteRegions;

	/// TextureAtlas that can carve up the sprite texture to show the correct texture
	private TextureAtlas spriteTextureAtlas;
	
	public LevelSettingsScreen(GameManager<WoolyLevelData, WoolyGroupData> gameManager, int backScreenId) {
		super("LevelSettingsScreen", GameData.LEVEL_SETTINGS_SCREEN, gameManager, backScreenId);
		
		// Note the creation of each screen in our debug log
		Gdx.app.debug("LevelSettingsScreen", "Creating Level Settings screen");
	}

	@Override
	public void render(float delta) {
		// Use a basic gray color around the edges of the map
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.3f, 1);
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
			// Retrieve the GroupData for the current group
			WoolyGroupData groupData = gameManager.data.getCurrentGroup();
			
			// Get the next levelId value from the current group
			int levelId = groupData.levels.getSize();
			
			// Create a new level for our editor
			levelData = groupData.createLevel(levelId);

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
		}

		if(isFirstTime()) {
			// Create a hash map for looking up texture regions by string name found in SpriteComponent
			spriteRegions = new HashMap<String, AtlasRegion>();
			spriteTextureAtlas = gameManager.getAssetManager().get("sprites.pack");
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

		// Update our labels
		updateLabels();
		
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
	
	private void updateLabels() {
		// Update our rows label
		rowsLabel.setText("" + levelData.original.rows);

		// Update our cols label
		colsLabel.setText("" + levelData.original.cols);
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

        // Retrieve our text cursor icon
        TextureRegionDrawable cursorIcon = new TextureRegionDrawable(spriteRegions.get("text_cursor_icon"));
        
        // Retrieve the font we will use for text messages
		Texture fontTexture = new Texture(Gdx.files.internal("fonts/font_normal.png"));
		fontTexture.setFilter(TextureFilter.Linear, TextureFilter.MipMapLinearLinear);
		TextureRegion fontRegion = new TextureRegion(fontTexture);
		BitmapFont labelFont = new BitmapFont(Gdx.files.internal("fonts/normal.fnt"), fontRegion, false);
		BitmapFont buttonFont = new BitmapFont(Gdx.files.internal("fonts/normal.fnt"), fontRegion, false);
		buttonFont.setUseIntegerPositions(false);

		// Create our LabelStyle
		LabelStyle labelStyle = new LabelStyle();
		labelStyle.font = labelFont;
		labelStyle.fontColor = Color.CYAN;

		// Create and add our Title Label
		titleLabel = new Label("Level Editor Settings", labelStyle);
		stageTable.add(titleLabel).center().expandX().colspan(4);
		stageTable.row();

		// Create our TextButtonStyle
		TextButtonStyle buttonStyle = new TextButtonStyle();
		//style.up = new TextureRegionDrawable(upRegion);
		//style.down = new TextureRegionDrawable(downRegion);
		buttonStyle.font = buttonFont;
		buttonStyle.fontColor = Color.WHITE;
		buttonStyle.overFontColor = Color.YELLOW;
		
		// Create our button listener
		ButtonListener buttonListener = new ButtonListener();
		
		// Create a text field style for our level name
		TextFieldStyle textFieldStyle = new TextFieldStyle();
		textFieldStyle.cursor = cursorIcon;
		textFieldStyle.font = buttonFont;
		textFieldStyle.focusedFontColor = Color.YELLOW;
		textFieldStyle.fontColor = Color.WHITE;
		textFieldStyle.messageFontColor = Color.GRAY;
		
		// Add our Name settings elements next
		levelName = new TextField(levelData.name, textFieldStyle);
		levelName.setMessageText("Enter level name");
		levelName.setTextFieldListener(new TextFieldListener() {
			public void keyTyped (TextField textField, char key) {
				if (key == '\r' || key == '\n') {
					// Trim any white space from the text entry
					levelData.name = levelName.getText().trim();

					// Disable onscreen keyboard
					textField.getOnscreenKeyboard().show(false);

					// Disable keyboard focus
					stage.setKeyboardFocus(null);
				}
			}
		});
		stageTable.add(levelName).expand().fill().colspan(4);
		stageTable.row();
		
		// Add our Row setting elements next
		stageTable.add(new Label("Rows", labelStyle)).right();
		minusRowButton = new TextButton("-", buttonStyle);
		minusRowButton.addListener(buttonListener);
		stageTable.add(minusRowButton).right().expandY().fill();
		rowsLabel = new Label(""+levelData.original.rows, labelStyle);
		stageTable.add(rowsLabel).center();
		plusRowButton = new TextButton("+", buttonStyle);
		plusRowButton.addListener(buttonListener);
		stageTable.add(plusRowButton).right().expandY().fill();
		stageTable.row();
		
		// Add our Col setting elements next
		stageTable.add(new Label("Cols", labelStyle)).right();
		minusColButton = new TextButton("-", buttonStyle);
		minusColButton.addListener(buttonListener);
		stageTable.add(minusColButton).right().expandY().fill();
		colsLabel = new Label(""+levelData.original.cols, labelStyle);
		stageTable.add(colsLabel).center();
		plusColButton = new TextButton("+", buttonStyle);
		plusColButton.addListener(buttonListener);
		stageTable.add(plusColButton).right().expandY().fill();
		stageTable.row();

		// Column span value for new button
		int colspan = 4;
		if(Platform.Android != gameManager.getEnvironment().getPlatform() &&
				   Platform.iOS != gameManager.getEnvironment().getPlatform()) {
			// Add our Back button first
			backButton = new TextButton("Back", buttonStyle);
			backButton.addListener(buttonListener);
			stageTable.add(backButton).right().fill();

			// Change our column span value to 3
			colspan = 3;
		}
		
		// Add our New button next
		editMapButton = new TextButton("Edit Map", buttonStyle);
		editMapButton.addListener(buttonListener);
		stageTable.add(editMapButton).right().expandY().fill().colspan(colspan);
		stageTable.row();

		// Enable table debug lines
		stageTable.debug();
	}

	/**
	 * ButtonListener is a general purpose button listener for all the buttons
	 * in the MainMenuScreen.
	 */
	private class ButtonListener extends ChangeListener {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			if(actor.equals(editMapButton)) {
				// Switch to level editor screen to create a new level for this group
				gameManager.setScreen(GameData.LEVEL_EDITOR_SCREEN);
			} else if(actor.equals(minusRowButton)) {
				// Subtract one row and update our row and column labels
				levelData.setRows(levelData.original.rows-1);
				updateLabels();
			} else if(actor.equals(plusRowButton)) {
				// Add one row and update our row and column labels
				levelData.setRows(levelData.original.rows+1);
				updateLabels();
			} else if(actor.equals(minusColButton)) {
				// Subtract one column and update our row and column labels
				levelData.setCols(levelData.original.cols-1);
				updateLabels();
			} else if(actor.equals(plusColButton)) {
				// Add one column and update our row and column labels
				levelData.setCols(levelData.original.cols+1);
				updateLabels();
			} else if(actor.equals(backButton)) {
				gameManager.setScreen(getBackScreenId());
			}
		}
	}
}
