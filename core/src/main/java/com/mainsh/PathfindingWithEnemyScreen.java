package com.mainsh;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Array;

/**
 * A screen that demonstrates A* pathfinding with an enemy following the cursor
 */
public class PathfindingWithEnemyScreen implements Screen {
    private final MyGame game;
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;

    // Grid world dimensions
    private static final int GRID_WIDTH = 20;
    private static final int GRID_HEIGHT = 15;
    private static final int CELL_SIZE = 32;

    // Our grid world
    private GridWorld gridWorld;

    // Enemy properties
    private Vector2 enemyPosition;
    private float enemyRadius = 10f;
    private float enemySpeed = 120f; // pixels per second
    private int enemyGridX = 1;
    private int enemyGridY = 1;

    // Cursor position (target)
    private Vector2 cursorPosition;
    private int cursorGridX = 10;
    private int cursorGridY = 10;

    // The current path for the enemy to follow
    private GraphPath<GridNode> currentPath;
    private Array<Vector2> pathWaypoints;
    private int currentWaypointIndex = 0;

    // Pathfinding settings
    private long lastPathfindingTime = 0;
    private static final long PATHFINDING_INTERVAL = 500; // ms between path recalculations

    // Input handling
    private int selectedX = -1;
    private int selectedY = -1;
    private boolean placeObstacle = false;
    private boolean removeObstacle = false;

    public PathfindingWithEnemyScreen(final MyGame game) {
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

        // Initialize enemy and cursor positions
        enemyPosition = new Vector2(enemyGridX * CELL_SIZE + CELL_SIZE / 2, enemyGridY * CELL_SIZE + CELL_SIZE / 2);
        cursorPosition = new Vector2(cursorGridX * CELL_SIZE + CELL_SIZE / 2, cursorGridY * CELL_SIZE + CELL_SIZE / 2);

        // Initialize waypoints array
        pathWaypoints = new Array<Vector2>();

        // Initial path calculation
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
                        // Recalculate path immediately when obstacle is placed
                        calculatePath();
                    } else if (removeObstacle) {
                        gridWorld.setWalkable(gridX, gridY, true);
                        // Recalculate path immediately when obstacle is removed
                        calculatePath();
                    }
                }

                return true;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                // Update cursor position
                Vector3 worldCoords = new Vector3(screenX, screenY, 0);
                camera.unproject(worldCoords);
                updateCursorPosition(worldCoords.x, worldCoords.y);
                return true;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                // Update cursor position
                Vector3 worldCoords = new Vector3(screenX, screenY, 0);
                camera.unproject(worldCoords);
                updateCursorPosition(worldCoords.x, worldCoords.y);
                return true;
            }

            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case Keys.O:
                        placeObstacle = true;
                        removeObstacle = false;
                        Gdx.app.log("PathfindingScreen", "Place Obstacle Mode");
                        break;
                    case Keys.C:
                        placeObstacle = false;
                        removeObstacle = true;
                        Gdx.app.log("PathfindingScreen", "Clear Obstacle Mode");
                        break;
                    case Keys.R:
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
     * Update cursor position and grid coordinates
     */
    private void updateCursorPosition(float x, float y) {
        cursorPosition.set(x, y);

        // Update grid coordinates
        cursorGridX = (int)(x / CELL_SIZE);
        cursorGridY = (int)(y / CELL_SIZE);

        // Clamp to grid bounds
        cursorGridX = Math.max(0, Math.min(cursorGridX, GRID_WIDTH - 1));
        cursorGridY = Math.max(0, Math.min(cursorGridY, GRID_HEIGHT - 1));
    }

    /**
     * Set up some obstacles in the grid
     */
    private void setupObstacles() {
        // Create a simple maze-like pattern
        for (int x = 5; x < 15; x++) {
            gridWorld.setWalkable(x, 7, false);
        }

        // Add an opening in the wall
        gridWorld.setWalkable(10, 7, true);

        for (int y = 3; y < 12; y++) {
            if (y != 7) { // Leave an opening at y=7
                gridWorld.setWalkable(10, y, false);
            }
        }

        // Add some random obstacles
        for (int i = 0; i < 15; i++) {
            int x = (int)(Math.random() * GRID_WIDTH);
            int y = (int)(Math.random() * GRID_HEIGHT);

            // Don't block start or goal
            if ((x != enemyGridX || y != enemyGridY) &&
                (x != cursorGridX || y != cursorGridY)) {
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
     * Calculate the path from enemy to cursor
     */
    private void calculatePath() {
        // Update enemy grid position
        enemyGridX = (int)(enemyPosition.x / CELL_SIZE);
        enemyGridY = (int)(enemyPosition.y / CELL_SIZE);

        // Clamp to grid bounds
        enemyGridX = Math.max(0, Math.min(enemyGridX, GRID_WIDTH - 1));
        enemyGridY = Math.max(0, Math.min(enemyGridY, GRID_HEIGHT - 1));

        // Find path from enemy to cursor
        currentPath = gridWorld.findPath(enemyGridX, enemyGridY, cursorGridX, cursorGridY);

        // Update path waypoints
        updatePathWaypoints();

        // Reset waypoint index
        currentWaypointIndex = 0;

        // Update last pathfinding time
        lastPathfindingTime = TimeUtils.millis();

        if (currentPath == null) {
            Gdx.app.log("PathfindingScreen", "No path found!");
        } else {
            Gdx.app.log("PathfindingScreen", "Path found with " + currentPath.getCount() + " nodes");
        }
    }

    /**
     * Convert path nodes to world coordinates for smooth movement
     */
    private void updatePathWaypoints() {
        // Clear existing waypoints
        pathWaypoints.clear();

        if (currentPath != null && currentPath.getCount() > 0) {
            // Add waypoints for each node in the path (converting to world coordinates)
            for (int i = 0; i < currentPath.getCount(); i++) {
                GridNode node = currentPath.get(i);
                // Use center of cell for smoother movement
                pathWaypoints.add(new Vector2(
                    node.getX() * CELL_SIZE + CELL_SIZE / 2,
                    node.getY() * CELL_SIZE + CELL_SIZE / 2
                ));
            }
        }
    }

    /**
     * Update enemy position to follow the path
     */
    private void updateEnemyPosition(float delta) {
        if (pathWaypoints.size == 0) {
            return; // No path to follow
        }

        // Check if we should recalculate the path
        if (TimeUtils.millis() - lastPathfindingTime > PATHFINDING_INTERVAL) {
            calculatePath();
        }

        // Get current waypoint
        if (currentWaypointIndex < pathWaypoints.size) {
            Vector2 currentWaypoint = pathWaypoints.get(currentWaypointIndex);

            // Calculate direction to waypoint
            Vector2 direction = new Vector2(
                currentWaypoint.x - enemyPosition.x,
                currentWaypoint.y - enemyPosition.y
            );

            // Check if we've reached the waypoint (with a small tolerance)
            float distance = direction.len();
            if (distance < 2.0f) {
                // Move to next waypoint
                currentWaypointIndex++;
            } else {
                // Normalize direction and scale by speed
                direction.nor().scl(enemySpeed * delta);

                // Update enemy position
                enemyPosition.add(direction);
            }
        }
    }

    @Override
    public void render(float delta) {
        // Update enemy position
        updateEnemyPosition(delta);

        // Clear the screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update camera
        camera.update();

        // Set projection matrix
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Draw grid
        shapeRenderer.begin(ShapeType.Filled);

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
                shapeRenderer.rect(node.getX() * CELL_SIZE + 8, node.getY() * CELL_SIZE + 8,
                    CELL_SIZE - 16, CELL_SIZE - 16);
            }
        }

        shapeRenderer.end();

        // Draw grid lines
        shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1); // Gray

        for (int x = 0; x <= GRID_WIDTH; x++) {
            shapeRenderer.line(x * CELL_SIZE, 0, x * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);
        }

        for (int y = 0; y <= GRID_HEIGHT; y++) {
            shapeRenderer.line(0, y * CELL_SIZE, GRID_WIDTH * CELL_SIZE, y * CELL_SIZE);
        }

        // Draw waypoints and connections if path exists
        if (pathWaypoints.size > 0) {
            shapeRenderer.setColor(0.0f, 0.8f, 0.8f, 1); // Cyan

            // Draw lines between waypoints
            for (int i = 0; i < pathWaypoints.size - 1; i++) {
                Vector2 current = pathWaypoints.get(i);
                Vector2 next = pathWaypoints.get(i + 1);
                shapeRenderer.line(current, next);
            }
        }

        shapeRenderer.end();

        // Draw enemy and cursor
        shapeRenderer.begin(ShapeType.Filled);

        // Draw enemy (blue circle)
        shapeRenderer.setColor(0.2f, 0.4f, 0.8f, 1);
        shapeRenderer.circle(enemyPosition.x, enemyPosition.y, enemyRadius);

        // Draw cursor (yellow circle)
        shapeRenderer.setColor(1.0f, 0.8f, 0.2f, 1);
        shapeRenderer.circle(cursorPosition.x, cursorPosition.y, 8);

        shapeRenderer.end();

        // Display controls as text
        // (For a complete example, you'd use SpriteBatch and BitmapFont here)
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
