package com.mainsh;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

public class MyGame extends Game {
    @Override
    public void create() {
        // Set log level
        Gdx.app.setLogLevel(Gdx.app.LOG_DEBUG);

        // Create and set the pathfinding screen
        setScreen(new PathfindingScreen(this));

        // Log usage instructions
        Gdx.app.log("MyGame", "A* Pathfinding Demo");
        Gdx.app.log("MyGame", "Press 'O' to place obstacles");
        Gdx.app.log("MyGame", "Press 'C' to clear obstacles");
        Gdx.app.log("MyGame", "Press 'S' to set start position");
        Gdx.app.log("MyGame", "Press 'G' to set goal position");
        Gdx.app.log("MyGame", "Press 'R' to reset the grid");
    }
}
