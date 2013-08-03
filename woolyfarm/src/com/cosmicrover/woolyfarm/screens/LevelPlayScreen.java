package com.cosmicrover.woolyfarm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
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
import com.cosmicrover.woolyfarm.PlayerData;

public class LevelPlayScreen extends AbstractScreen {
	/// Scene2d used by this Screen
	private Stage stage = null;
	private Image background = null;
	private Table stageTable = null;
	private Label fencesLabel = null;
	private Label dogsLabel = null;
	private Label levelNameLabel = null;
	private Table levelTable = null;
	private ScrollPane levelScrollPane = null;
	private TextButton restartButton = null;
	private TextButton backButton = null;
	private ButtonListener buttonListener = null;
	private LevelData levelData = null;

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
		
		if(isFirstTime()) {
			// Create our stage objects for the first time
			createStage();
			
			// Clear our first time flag
			clearFirstTime();
		} else {
			// TODO: Enable existing entities used only for this Screen
		}
		
		// Create our level map
		createLevelMap();

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
        
        // Retrieve our PlayerData object for obtaining textures
        PlayerData playerData = gameManager.getData(PlayerData.class);
        
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
			stageTable.add(backButton).left().padRight(5.0f).fillX();
		}

		// Add fences icon and number of fences remaining label
		stageTable.add(new Image(playerData.getSpriteTexture("fences_icon"))).right().fillX();
		fencesLabel = new Label(""+levelData.fences, labelStyle);
		stageTable.add(fencesLabel).left().fillX();
		
		// Add dogs icon and number of dogs remaining label
		stageTable.add(new Image(playerData.getSpriteTexture("dogs_icon"))).right().padLeft(5.0f).fillX();
		dogsLabel = new Label(""+levelData.dogs, labelStyle);
		stageTable.add(dogsLabel).left().fillX();
		
		// Add level name
		levelNameLabel = new Label(levelData.name, labelStyle);
		stageTable.add(levelNameLabel).center().padLeft(5.0f).padRight(5.0f).fillX();

		// Add our restart button last of all
		restartButton = new TextButton("R", buttonStyle);
		restartButton.addListener(buttonListener);
		stageTable.add(restartButton).right().fillX();
		stageTable.row();

		// Create a table to hold the level data and put it into our ScrollPane
		levelTable = new Table();
		
		// Create a scroll pane for the list of level buttons
		levelScrollPane = new ScrollPane(levelTable);
		stageTable.add(levelScrollPane).colspan(7).expand().fill();
		stageTable.row();

		// Enable table debug lines
		stageTable.debug();
	}
	
	private void createLevelMap() {
		// First clear our level table of all its images
		levelTable.clear();
		
        // Retrieve our PlayerData object for obtaining textures
        PlayerData playerData = gameManager.getData(PlayerData.class);
        
        boolean grassStyle = true;
        TextureRegionDrawable grass1 = new TextureRegionDrawable(playerData.getSpriteTexture("grass1"));
        TextureRegionDrawable grass2 = new TextureRegionDrawable(playerData.getSpriteTexture("grass2"));
        TextureRegionDrawable postTopLeft = new TextureRegionDrawable(playerData.getSpriteTexture("fence_post_nw"));
        TextureRegionDrawable postTopRight = new TextureRegionDrawable(playerData.getSpriteTexture("fence_post_ne"));
        TextureRegionDrawable postBottomLeft = new TextureRegionDrawable(playerData.getSpriteTexture("fence_post_sw"));
        TextureRegionDrawable postBottomRight = new TextureRegionDrawable(playerData.getSpriteTexture("fence_post_se"));
        
		// Now create the level map data
		for(int row=0;row<levelData.rows; row++) {
			for(int col=0; col<levelData.cols; col++) {
				// Select alternating grass image
				Button anSquareButton;
				if(grassStyle) {
					anSquareButton = new Button(grass1);
				} else {
					anSquareButton = new Button(grass2);
				}
				grassStyle = !grassStyle;
				
				// Add other sub-buttons to the parent button
				anSquareButton.add(new Button(postTopLeft)).top().left();
				anSquareButton.add(new Image());
				anSquareButton.add(new Button(postTopRight)).top().right();
				anSquareButton.row();
				anSquareButton.add(new Image());
				anSquareButton.add(new Image()).expand(); // Sheep goes here
				anSquareButton.add(new Image());
				anSquareButton.row();
				anSquareButton.add(new Button(postBottomLeft)).bottom().left();
				anSquareButton.add(new Image());
				anSquareButton.add(new Button(postBottomRight)).bottom().right();
				anSquareButton.row();
				
				// Add the button to the table
				levelTable.add(anSquareButton);
				
				// Enable debug for button
				anSquareButton.debug();
			}
			// Force alternating grass styles if columns is even
			if(levelData.cols % 2 == 0) {
				grassStyle = !grassStyle;
			}
			levelTable.row();
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
