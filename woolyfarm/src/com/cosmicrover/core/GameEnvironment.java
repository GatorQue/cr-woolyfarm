package com.cosmicrover.core;

import com.badlogic.gdx.Gdx;

public class GameEnvironment {
	public enum Platform {
		Unknown,
		Android,
		Applet,
		Desktop,
		HTML,
		iOS,
	};
	
	public enum Density {
		Low,     /// 120 dpi (Gdx.graphics.getDensity() = 0.75)
		Medium,  /// 160 dpi (Gdx.graphics.getDensity() = 1.0)
		TV,      /// 213 dpi (Gdx.graphics.getDensity() = 1.33125)
		High,    /// 240 dpi (Gdx.graphics.getDensity() = 1.5)
		XHigh,   /// 320 dpi (Gdx.graphics.getDensity() = 2.0)
		XXHigh,  /// 480 dpi (Gdx.graphics.getDensity() = 3.0)
		XXXHigh, /// 640 dpi (Gdx.graphics.getDensity() = 4.0)
	}
	
	private Platform platform = Platform.Unknown;
	
	public GameEnvironment(Platform platform) {
		this.platform = platform;
	}

	public Platform getPlatform() {
		return platform;
	}
	
	public Density getDensity() {
		Density result = Density.Low;
		if(Gdx.graphics.getDensity() >= 1.0 && Gdx.graphics.getDensity() < 1.33125) {
			result = Density.Medium;
		} else if(Gdx.graphics.getDensity() >= 1.33125 && Gdx.graphics.getDensity() < 1.5) {
			result = Density.TV;
		} else if(Gdx.graphics.getDensity() >= 1.5 && Gdx.graphics.getDensity() < 2.0) {
			result = Density.High;
		} else if(Gdx.graphics.getDensity() >= 2.0 && Gdx.graphics.getDensity() < 3.0) {
			result = Density.XHigh;
		} else if(Gdx.graphics.getDensity() >= 3.0 && Gdx.graphics.getDensity() < 4.0) {
			result = Density.XXHigh;
		} else if(Gdx.graphics.getDensity() >= 4.0) {
			result = Density.XXXHigh;
		}
		
		// Return the density determined above
		return result;
	}
}
