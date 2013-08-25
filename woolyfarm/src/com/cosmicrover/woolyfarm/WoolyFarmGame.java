package com.cosmicrover.woolyfarm;

import com.badlogic.gdx.Game;
import com.cosmicrover.core.GameEnvironment;
import com.cosmicrover.core.GameManager;
import com.cosmicrover.woolyfarm.assets.WoolyGameData;
import com.cosmicrover.woolyfarm.assets.WoolyGroupData;
import com.cosmicrover.woolyfarm.assets.WoolyLevelData;

public class WoolyFarmGame extends Game {
	/// Our persistence service class
	private final GameManager<WoolyLevelData, WoolyGroupData> gameManager;
	
	public WoolyFarmGame(GameEnvironment gameEnvironment) {
		this.gameManager = new GameManager<WoolyLevelData, WoolyGroupData>(this, gameEnvironment);
	}
	
	@Override
	public void create() {
		// Step 1: Add any additional details to our environment (graphics,
		// Gamepad controllers, etc)
		// @TODO: Add detection of game environment information
		// gameManager.getEnvironment().setJoysticks(joystickCount);

		// Step 2: Initialize the game data
		gameManager.initData(new WoolyGameData("wollyfarm.dat"));
		
		// Step 3: Set our first screen object and return to caller
		setScreen(gameManager.data.getInitialScreen());
	}

	@Override
	public void pause() {
		// Call our super class first
		super.pause();

		// Persist our game data settings to disk
		gameManager.saveData();
	}

	@Override
	public void dispose() {
		// Call our super class first
		super.dispose();
		
		// Give our GameManager a chance to dispose itself
		gameManager.dispose();
	}
}
