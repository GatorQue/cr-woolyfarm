package com.cosmicrover.core.ui.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class AnimationDrawable extends TextureRegionDrawable {
	private Animation animation;
	private float stateTime;

	/** Creates an uninitialized AnimationDrawable. The animation must be set before use. */
	public AnimationDrawable() {
	}

	public AnimationDrawable(Animation animation) {
		setAnimation(animation);
	}

	public AnimationDrawable(AnimationDrawable drawable) {
		super(drawable);
		setAnimation(drawable.animation);
	}

    public void draw(SpriteBatch batch, float x, float y, float width, float height) {
    	stateTime += Gdx.graphics.getDeltaTime();
    	setRegion(animation.getKeyFrame(stateTime));
    	super.draw(batch, x, y, width, height);
	}

	public void setAnimation(Animation animation) {
		this.animation = animation;
		setRegion(animation.getKeyFrame(stateTime));
	}
	
	public void setRandomStart() {
		stateTime = (float)(Math.random()*10.0f);
	}

	public void setPlayMode(int playMode) {
		animation.setPlayMode(playMode);
	}

	public Animation getAnimation() {
		return animation;
	}
}
