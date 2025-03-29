package com.mainsh;

import com.badlogic.gdx.*;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

public class PathfindingScreen implements Screen {
    private final MyGame game;
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;

    // Grid world dimensions
    private static final int GRID_WIDTH = 20;
    private static final int GRID_HEIGHT = 15;
    private static final int CELL_SIZE = 32;

    // Our grid world
    private GridWorld gridWorld;

    // Start and goal positions
    private int startX = 1;
    private int startY = 1;
    private int goalX = 18;
    private int goalY = 13;

    // The current path
    private GraphPath<GridNode> currentPath;

    // Input handling
    private int selectedX = -1;
    private int selectedY = -1;
    private boolean placeObstacle = false;
    private boolean removeObstacle = false;
    private boolean setStart = false;
    private boolean setGoal = false;

    public PathfindingScreen(final MyGame game) {
        this.game = game;

        // Set up camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, GRID_WIDTH * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);
        camera.update();

        // Create shape renderer
        shapeRenderer = new ShapeRenderer();

        // Initialize the grid world
        gridWorld = new GridWorld(GRID_WIDTH, GRID_HEIGHT);

        // Set up some obstacles for demonstration
        setupObstacles();

        // Calculate initial path
        calculatePath();

        // Set up input handling
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                // Convert screen coordinates to world coordinates
                Vector3 worldCoords = new Vector3(screenX, screenY, 0);
                camera.unproject(worldCoords);

                // Convert to grid coordinates
                int gridX = (int)(worldCoords.x / CELL_SIZE);
                int gridY = (int)(worldCoords.y / CELL_SIZE);

                if (gridX >= 0 && gridX < GRID_WIDTH && gridY >= 0 && gridY < GRID_HEIGHT) {
                    selectedX = gridX;
                    selectedY = gridY;

                    // Handle action based on current mode
                    if (placeObstacle) {
                        gridWorld.setWalkable(gridX, gridY, false);
                        calculatePath();
                    } else if (removeObstacle) {
                        gridWorld.setWalkable(gridX, gridY, true);
                        calculatePath();
                    } else if (setStart) {
                        startX = gridX;
                        startY = gridY;
                        calculatePath();
                        setStart = false;
                    } else if (setGoal) {
                        goalX = gridX;
                        goalY = gridY;
                        calculatePath();
                        setGoal = false;
                    }
                }

                return true;
            }

            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case Input.Keys.O:
                        placeObstacle = true;
                        removeObstacle = false;
                        setStart = false;
                        setGoal = false;
                        Gdx.app.log("PathfindingScreen", "Place Obstacle Mode");
                        break;
                    case Input.Keys.C:
                        placeObstacle = false;
                        removeObstacle = true;
                        setStart = false;
                        setGoal = false;
                        Gdx.app.log("PathfindingScreen", "Clear Obstacle Mode");
                        break;
                    case Input.Keys.S:
                        placeObstacle = false;
                        removeObstacle = false;
                        setStart = true;
                        setGoal = false;
                        Gdx.app.log("PathfindingScreen", "Set Start Mode");
                        break;
                    case Input.Keys.G:
                        placeObstacle = false;
                        removeObstacle = false;
                        setStart = false;
                        setGoal = true;
                        Gdx.app.log("PathfindingScreen", "Set Goal Mode");
                        break;
                    case Input.Keys.R:
                        // Reset the grid
                        resetGrid();
                        break;
                }
                return true;
            }
        });

        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    /**
     * Set up some obstacles in the grid
     */
    private void setupObstacles() {
        // Create a simple maze-like pattern
        for (int x = 5; x < 15; x++) {
            gridWorld.setWalkable(x, 7, false);
        }

        for (int y = 3; y < 12; y++) {
            gridWorld.setWalkable(10, y, false);
        }

        // Add some random obstacles
        for (int i = 0; i < 20; i++) {
            int x = (int)(Math.random() * GRID_WIDTH);
            int y = (int)(Math.random() * GRID_HEIGHT);

            // Don't block start or goal
            if ((x != startX || y != startY) && (x != goalX || y != goalY)) {
                gridWorld.setWalkable(x, y, false);
            }
        }
    }

    /**
     * Reset the grid to its initial state
     */
    private void resetGrid() {
        gridWorld = new GridWorld(GRID_WIDTH, GRID_HEIGHT);
        setupObstacles();
        calculatePath();
        Gdx.app.log("PathfindingScreen", "Grid Reset");
    }

    /**
     * Calculate the path from start to goal
     */
    private void calculatePath() {
        currentPath = gridWorld.findPath(startX, startY, goalX, goalY);

        if (currentPath == null) {
            Gdx.app.log("PathfindingScreen", "No path found!");
        } else {
            Gdx.app.log("PathfindingScreen", "Path found with " + currentPath.getCount() + " nodes");
        }
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update camera
        camera.update();

        // Set projection matrix
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Draw grid
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw cells
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                GridNode node = gridWorld.getNode(x, y);

                if (node != null) {
                    if (!node.isWalkable()) {
                        // Obstacle (red)
                        shapeRenderer.setColor(0.8f, 0.2f, 0.2f, 1);
                    } else {
                        // Normal cell (dark gray)
                        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1);
                    }

                    // Draw cell with a small border
                    shapeRenderer.rect(x * CELL_SIZE + 1, y * CELL_SIZE + 1,
                        CELL_SIZE - 2, CELL_SIZE - 2);
                }
            }
        }

        // Draw the path if it exists
        if (currentPath != null) {
            shapeRenderer.setColor(0.2f, 0.7f, 0.2f, 1); // Green path

            for (int i = 0; i < currentPath.getCount(); i++) {
                GridNode node = currentPath.get(i);
                shapeRenderer.rect(node.getX() * CELL_SIZE + 5, node.getY() * CELL_SIZE + 5,
                    CELL_SIZE - 10, CELL_SIZE - 10);
            }
        }

        // Draw start position
        shapeRenderer.setColor(0.2f, 0.6f, 1.0f, 1); // Blue
        shapeRenderer.rect(startX * CELL_SIZE + 3, startY * CELL_SIZE + 3,
            CELL_SIZE - 6, CELL_SIZE - 6);

        // Draw goal position
        shapeRenderer.setColor(1.0f, 0.8f, 0.2f, 1); // Yellow
        shapeRenderer.rect(goalX * CELL_SIZE + 3, goalY * CELL_SIZE + 3,
            CELL_SIZE - 6, CELL_SIZE - 6);

        shapeRenderer.end();

        // Draw grid lines
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1); // Gray

        for (int x = 0; x <= GRID_WIDTH; x++) {
            shapeRenderer.line(x * CELL_SIZE, 0, x * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);
        }

        for (int y = 0; y <= GRID_HEIGHT; y++) {
            shapeRenderer.line(0, y * CELL_SIZE, GRID_WIDTH * CELL_SIZE, y * CELL_SIZE);
        }

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        // Adjust camera viewport
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
