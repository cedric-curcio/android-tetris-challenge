package com.perso.android.free.tetris.game.event;

/**
 * holding all game events.
 * @author cedric
 *
 */
public class GameEvent {
	public long eventTime;
	public GameEvent() {
		eventTime = System.currentTimeMillis();
	}
}