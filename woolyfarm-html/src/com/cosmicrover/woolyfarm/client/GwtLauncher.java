package com.cosmicrover.woolyfarm.client;

import com.cosmicrover.core.GameEnvironment;
import com.cosmicrover.core.GameEnvironment.Platform;
import com.cosmicrover.woolyfarm.WoolyFarmGame;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;

public class GwtLauncher extends GwtApplication {
	@Override
	public GwtApplicationConfiguration getConfig () {
		GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(480, 320);
		return cfg;
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return new WoolyFarmGame(new GameEnvironment(Platform.HTML));
	}
}