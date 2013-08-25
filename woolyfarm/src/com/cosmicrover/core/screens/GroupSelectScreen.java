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
import com.badlogic.gdx.utils.ArrayMap.Keys;
import com.cosmicrover.core.GameManager;
import com.cosmicrover.core.GroupManager;
import com.cosmicrover.core.GameEnvironment.Platform;
import com.cosmicrover.core.assets.GameData;
import com.cosmicrover.core.assets.GroupData;
import com.cosmicrover.core.assets.LevelData;
import com.cosmicrover.woolyfarm.assets.WoolyGameData;

public class GroupSelectScreen<L extends LevelData, G extends GroupData<L>> extends AbstractScreen<L,G> {
	/// Scene2d used by this Screen
	private Stage stage = null;
	private Image background = null;
	private Table stageTable = null;
	private Label title = null;
	private Table groupTable = null;
	private TextButton backButton = null;
	private BitmapFont buttonFont = null;
	private ButtonListener buttonListener = null;

	/// Maps region name to AtlasRegion information to texture
	private HashMap<String, AtlasRegion> groupRegions;

	/// TextureAtlas that can carve up the sprite texture to show the correct texture
	private TextureAtlas groupTextureAtlas;

	public GroupSelectScreen(GameManager<L,G> gameManager, int backScreenId) {
		super("GroupSelectScreen", GameData.GROUP_SELECT_SCREEN, gameManager, backScreenId);
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

		// First time showing this screen? then create our stage and other UI elements
		if(isFirstTime()) {
			// Create a hash map for looking up texture regions by string name found in SpriteComponent
			groupRegions = new HashMap<String, AtlasRegion>();
			groupTextureAtlas = gameManager.getAssetManager().get("textures/group_select.pack");
			// Create a map of each region available in our sprite TextureAtlas
			for (AtlasRegion region : groupTextureAtlas.getRegions()) {
				groupRegions.put(region.name, region);
			}

			// Create our stage objects on first time
			createStage();
			
			// Clear our first time flag
			clearFirstTime();
		} else {
			// TODO: Enable existing entities used only for this Screen
		}

		// Create our table of group buttons to click on
		createButtons(buttonListener);
		
		// Add our Scene2d as an input processor
		gameManager.getInputMultiplexer().addProcessor(stage);
	}

	@Override
	public void hide() {
		// First time flag has been cleared? then clean up
		if(!isFirstTime()) {
			// Clear our table of group buttons
			groupTable.clear();
		
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
		title = new Label("Group Select", labelStyle);
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

		// Create a table to hold our list of groups to choose from
		groupTable = new Table();
		
		// Create a scroll pane for the list of group buttons
		stageTable.add(new ScrollPane(groupTable)).expand().fill();
		stageTable.row();

		if( Platform.Android != gameManager.getEnvironment().getPlatform() &&
			Platform.iOS != gameManager.getEnvironment().getPlatform()) {
			// Add our back button last of all
			backButton = new TextButton("Back", textButtonStyle);
			backButton.addListener(buttonListener);
			stageTable.add(backButton).expandY().fill();
		}

		// Enable table debug lines
		stageTable.debug();
	}

	private void createButtons(ButtonListener buttonListener) {
		// Clear our table of group buttons
		groupTable.clear();
		
		// Loop through each group and create a new button
		Keys<String> groups = gameManager.data.groups.getGroups();
		for(String groupName : groups) {
			G group = gameManager.data.groups.getGroup(groupName);

			// Create a button to represent this group
			Button anButton = new Button(new TextureRegionDrawable(groupRegions.get("group_"+group.groupId)));
			anButton.setName(groupName);
			anButton.setDisabled(group.locked);
			anButton.addListener(buttonListener);
			anButton.pad(10.0f);

			// Now add the group button to the list to be displayed
			groupTable.add(anButton).center().expand();
		}

		// Add our create new level button
		Button anButton = new Button(new TextureRegionDrawable(groupRegions.get("group_new")));
		anButton.setName(GroupManager.getFilename(gameManager.data.groups.getSize()));
		anButton.setDisabled(false); // TODO: Replace false with lookup to see if group editor is enabled
		anButton.addListener(buttonListener);
		anButton.pad(10.0f);

		// Now add the group button to the list to be displayed
		groupTable.add(anButton).center().expand();

		// Indicate this is the end of the row
		groupTable.row();

		// Enable debug lines
		groupTable.debug();
	}
	
	/**
	 * ButtonListener is a general purpose button listener for all the buttons
	 * in the MainMenuScreen.
	 */
	private class ButtonListener extends ChangeListener {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			if(actor.equals(backButton)) {
				Gdx.app.log("GroupSelectScreen:ButtonListener", "Back");
				gameManager.setScreen(getBackScreenId());
			} else {
				Actor anButton = groupTable.findActor(actor.getName());
				if(anButton != null) {
					Gdx.app.log("GroupSelectScreen:ButtonListener", anButton.getName());
					gameManager.data.setCurrentGroup(anButton.getName());
					gameManager.setScreen(WoolyGameData.LEVEL_SELECT_SCREEN);
				}
			}
		}
	}
}
