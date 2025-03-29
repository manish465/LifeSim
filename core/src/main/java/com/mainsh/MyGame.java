package com.mainsh;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

public class MyGame extends Game {
    @Override
    public void create() {
        // Set log level
        Gdx.app.setLogLevel(Gdx.app.LOG_DEBUG);

        // Create and set the pathfinding screen with enemy following cursor
        setScreen(new PathfindingWithEnemyScreen(this));

        // Log usage instructions
        Gdx.app.log("MyGame", "A* Pathfinding Demo with Enemy Following Cursor");
        Gdx.app.log("MyGame", "Move your mouse to control the cursor");
        Gdx.app.log("MyGame", "The blue circle (enemy) will follow your cursor using A* pathfinding");
        Gdx.app.log("MyGame", "Press 'O' to place obstacles");
        Gdx.app.log("MyGame", "Press 'C' to clear obstacles");
        Gdx.app.log("MyGame", "Press 'R' to reset the grid");
    }
}
