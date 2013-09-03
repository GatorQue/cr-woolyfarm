package com.cosmicrover.woolyfarm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.cosmicrover.core.GameEnvironment.Platform;
import com.cosmicrover.core.assets.GameData;
import com.cosmicrover.core.GameManager;
import com.cosmicrover.woolyfarm.assets.MapData.Sprites;
import com.cosmicrover.woolyfarm.assets.WoolyGroupData;
import com.cosmicrover.woolyfarm.assets.WoolyLevelData;

public class LevelPlayScreen extends LevelScreen<WoolyGroupData> {
	protected Label fencesLabel = null;
	protected Label dogsLabel = null;
	protected Label levelNameLabel = null;
	protected Button hintButton = null;

	public LevelPlayScreen(GameManager<WoolyLevelData, WoolyGroupData> gameManager, int screenId) {
		super("LevelPlayScreen", GameData.LEVEL_PLAY_SCREEN, gameManager, screenId);
	}
	
	@Override
	protected Music createMusic() {
		// Retrieve our level play music track
		return Gdx.audio.newMusic(Gdx.files.internal("music/level_play.mp3"));
	}
	
	@Override
	protected void createInfoBar() {
        // Create our information bar starting by gathering texture regions for each icon in our information bar
        TextureRegionDrawable fencesIcon = new TextureRegionDrawable(spriteRegions.get("fences_icon"));
        TextureRegionDrawable dogsIcon = new TextureRegionDrawable(spriteRegions.get("dogs_icon"));
        TextureRegionDrawable backIcon = new TextureRegionDrawable(spriteRegions.get("level_select_icon"));
        //TextureRegionDrawable nextIcon = new TextureRegionDrawable(spriteRegions.get("next_icon"));
        TextureRegionDrawable resetIcon = new TextureRegionDrawable(spriteRegions.get("restart_icon"));
        TextureRegionDrawable hintIcon = new TextureRegionDrawable(spriteRegions.get("hint_icon"));

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
		
		// Add back button on left (for non mobile platforms)
		Table leftTable = new Table();
		if(Platform.Android != gameManager.getEnvironment().getPlatform() &&
		   Platform.iOS != gameManager.getEnvironment().getPlatform()) {
			backButton = new Button(backIcon);
			backButton.addListener(buttonListener);
			leftTable.add(backButton).spaceRight(5.0f).fillY();
		}

		// Add fences icon and number of fences remaining label
		leftTable.add(new Image(fencesIcon));
		fencesLabel = new Label(""+levelData.current.numFences, labelStyle);
		leftTable.add(fencesLabel).width(32.0f).spaceRight(5.0f);
		
		// Add dogs icon and number of dogs remaining label
		leftTable.add(new Image(dogsIcon));
		dogsLabel = new Label(""+levelData.current.numDogs, labelStyle);
		leftTable.add(dogsLabel).width(32.0f);
		stageTable.add(leftTable).left().expandX();
		
		// Add level name
		levelNameLabel = new Label(levelData.name, labelStyle);
		stageTable.add(levelNameLabel).fill().expandX().spaceRight(5.0f);

		// Add our hint button next
		hintButton = new Button(hintIcon);
		hintButton.addListener(buttonListener);
		stageTable.add(hintButton).right().fillY();

		// Add our restart button last of all
		restartButton = new Button(resetIcon);
		restartButton.addListener(buttonListener);
		stageTable.add(restartButton).right().fillY();
		stageTable.row();
	}
	
	@Override
	protected void onMapSquareClick(int row, int col) {
		// TODO: Add dog movement

		// Update our level map
		updateLevelMap();
	}

	@Override
	protected void onMapEdgeClick(int row, int col, MapEdge fenceDirection) {
		// Even row? must be a horizontal fence to be placed
		if(MapEdge.Horizontal == fenceDirection) {
			// Place a horizontal fence at the location specified if empty
			if(Sprites.FenceHorizontalEmpty == levelData.current.horizontal[row][col] && levelData.current.numFences > 0) {
				levelData.current.horizontal[row][col] = Sprites.FenceHorizontal;
				levelData.current.numFences--;
			}
		}
		// Odd row? must be a vertical fence to be placed
		else {
			// Place a vertical fence at the location specified if empty
			if(Sprites.FenceVerticalEmpty == levelData.current.vertical[row][col] && levelData.current.numFences > 0) {
				levelData.current.vertical[row][col] = Sprites.FenceVertical;
				levelData.current.numFences--;
			}
		}
		
		// Update our level map
		updateLevelMap();
		
		// Update our fence count label
		fencesLabel.setText(""+levelData.current.numFences);
		
		// Are we done playing this level?
		if(levelData.current.isLevelDone() || (levelData.current.numFences == 0 && levelData.current.numDogs == 0)) {
			System.out.println("End of level reached!");
			// Switch to end level screen
			gameManager.setScreen(GameData.LEVEL_END_SCREEN);
		}
	}

	@Override
	protected void onResetClick() {
		// Call our superclass
		super.onResetClick();
		
		// Remove all fences from the level
		levelData.current.removeFences();
		
		// Update our level map
		updateLevelMap();
		
		// Update our fences label
		fencesLabel.setText("" + levelData.current.numFences);
		
		// Update our dogs label
		dogsLabel.setText("" + levelData.current.numDogs);
	}


	@Override
	protected void onOtherClick(Actor actor) {
		// Handle Hint button
		if(actor.equals(hintButton)) {
			// TODO: Provide hint implementation
		}
	}
	
}
