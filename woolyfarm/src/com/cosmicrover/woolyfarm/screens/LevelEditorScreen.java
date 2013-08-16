package com.cosmicrover.woolyfarm.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.cosmicrover.core.GameManager;
import com.cosmicrover.core.GameEnvironment.Platform;
import com.cosmicrover.woolyfarm.LevelData;
import com.cosmicrover.woolyfarm.LevelData.Sprites;
import com.cosmicrover.woolyfarm.LevelDataLoader;
import com.cosmicrover.woolyfarm.PlayerData;

public class LevelEditorScreen extends LevelScreen {
	protected Label fencesLabel = null;
	protected Label dogsLabel = null;
	protected TextField levelName = null;
	protected Button saveButton = null;

	public LevelEditorScreen(GameManager gameManager, int screenId) {
		super("LevelEditorScreen", gameManager, screenId);
	}

	@Override
	protected void createInfoBar() {
        // Create our information bar starting by gathering texture regions for each icon in our information bar
        TextureRegionDrawable fencesIcon = new TextureRegionDrawable(spriteRegions.get("fences_icon"));
        TextureRegionDrawable dogsIcon = new TextureRegionDrawable(spriteRegions.get("dogs_icon"));
        TextureRegionDrawable backIcon = new TextureRegionDrawable(spriteRegions.get("back_icon"));
        //TextureRegionDrawable nextIcon = new TextureRegionDrawable(spriteRegions.get("next_icon"));
        TextureRegionDrawable resetIcon = new TextureRegionDrawable(spriteRegions.get("restart_icon"));
        TextureRegionDrawable cursorIcon = new TextureRegionDrawable(spriteRegions.get("cursor_icon"));
        TextureRegionDrawable saveIcon = new TextureRegionDrawable(spriteRegions.get("save_icon"));

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
			leftTable.add(backButton).left().spaceRight(5.0f).fillY();
		}

		// Add fences icon and number of fences remaining label
		leftTable.add(new Image(fencesIcon));
		fencesLabel = new Label(""+levelData.numFences, labelStyle);
		leftTable.add(fencesLabel).width(32.0f).spaceRight(5.0f);
		
		// Add dogs icon and number of dogs remaining label
		leftTable.add(new Image(dogsIcon));
		dogsLabel = new Label(""+levelData.numDogs, labelStyle);
		leftTable.add(dogsLabel).width(32.0f);
		stageTable.add(leftTable).left().expandX();
		
		// Create a text field style for our level name
		TextFieldStyle textFieldStyle = new TextFieldStyle();
		textFieldStyle.cursor = cursorIcon;
		textFieldStyle.font = buttonFont;
		textFieldStyle.focusedFontColor = Color.YELLOW;
		textFieldStyle.fontColor = Color.WHITE;
		textFieldStyle.messageFontColor = Color.GRAY;
		
		// Add level name text field
		levelName = new TextField(levelData.name, textFieldStyle);
		levelName.setMessageText("Enter level name");
		levelName.setTextFieldListener(new TextFieldListener() {
			public void keyTyped (TextField textField, char key) {
				if (key == '\r' || key == '\n') {
					// TODO: Remove the old "tag" from our list of levels in PlayerData
					levelData.name = levelName.getText().trim();
					// TODO: Add the new "tag" from our list of levels in PlayerData

					// Disable onscreen keyboard
					textField.getOnscreenKeyboard().show(false);

					// Disable keyboard focus
					stage.setKeyboardFocus(null);
				}
			}
		});
		stageTable.add(levelName).fill().expandX().spaceRight(5.0f);

		// Add our save button next
		saveButton = new Button(saveIcon);
		saveButton.addListener(buttonListener);
		stageTable.add(saveButton).right().fillY();

		// Add our restart button last of all
		restartButton = new Button(resetIcon);
		restartButton.addListener(buttonListener);
		stageTable.add(restartButton).right().fillY();
		stageTable.row();
	}

	@Override
	protected void onMapSquareClick(int row, int col) {
		switch(levelData.mapAnimals[row][col]) {
		default:
			Gdx.app.error("toggleAnimal", "Unknown animal type");
		case AnimalNone:
			levelData.mapAnimals[row][col] = Sprites.AnimalSheep;
			break;
		case AnimalSheep:
			levelData.mapAnimals[row][col] = Sprites.AnimalWolf;
			break;
		case AnimalWolf:
			levelData.mapAnimals[row][col] = Sprites.AnimalGoat;
			break;
		case AnimalGoat:
			levelData.mapAnimals[row][col] = Sprites.AnimalPig;
			break;
		case AnimalPig:
			levelData.mapAnimals[row][col] = Sprites.AnimalDuck;
			break;
		case AnimalDuck:
			levelData.mapAnimals[row][col] = Sprites.AnimalNone;
			break;
		}
		Gdx.app.log("toggleAnimal", "A:row="+row+",col="+col+",value="+levelData.mapAnimals[row][col]);

		// Update our level map
		updateLevelMap();
	}

	@Override
	protected void onMapEdgeClick(int row, int col, MapEdge fenceDirection) {
		// Even row? must be a horizontal fence to be placed
		if(MapEdge.Horizontal == fenceDirection) {
			Gdx.app.log("toggleFence", "H:row="+row+",col="+col+",value="+levelData.mapFenceHorizontal[row][col]);
			switch(levelData.mapFenceHorizontal[row][col]) {
			case FenceHorizontal:
				levelData.mapFenceHorizontal[row][col] = Sprites.FenceHorizontalBroken;
				levelData.numFences--;
				break;
			case FenceHorizontalBroken:
				levelData.mapFenceHorizontal[row][col] = Sprites.FenceHorizontalEmpty;
				break;
			default:
			case FenceHorizontalEmpty:
				levelData.mapFenceHorizontal[row][col] = Sprites.FenceHorizontal;
				levelData.numFences++;
				break;
			}
		}
		// Odd row? must be a vertical fence to be placed
		else {
			Gdx.app.log("toggleFence", "V:row="+row+",col="+col+",value="+levelData.mapFenceVertical[row][col]);
			switch(levelData.mapFenceVertical[row][col]) {
			case FenceVertical:
				levelData.mapFenceVertical[row][col] = Sprites.FenceVerticalBroken;
				levelData.numFences--;
				break;
			case FenceVerticalBroken:
				levelData.mapFenceVertical[row][col] = Sprites.FenceVerticalEmpty;
				break;
			default:
			case FenceVerticalEmpty:
				levelData.mapFenceVertical[row][col] = Sprites.FenceVertical;
				levelData.numFences++;
				break;
			}
		}
		
		// Update our level map
		updateLevelMap();
		
		// Update our fence count label
		fencesLabel.setText(""+levelData.numFences);
	}

	@Override
	protected void onResetClick() {
		// Call our superclass
		super.onResetClick();
		
		// Update our fences label
		fencesLabel.setText("" + levelData.numFences);
		
		// Update our dogs label
		dogsLabel.setText("" + levelData.numDogs);
	}

	@Override
	protected void onOtherClick(Actor actor) {
		// Handle Save button
		if(actor.equals(saveButton)) {
			levelData.updateOriginal();
			// TODO: Replace this with a more elegant solution that can also
			// add the new level to the custom levels group
			LevelDataLoader.save(levelData, GameManager.DATA_DIRECTORY + LevelData.LEVEL_DIRECTORY + LevelData.LEVEL_NAME + levelData.id + LevelData.LEVEL_EXT);
			gameManager.setScreen(PlayerData.LEVEL_SELECT_SCREEN);
		}
	}
}
