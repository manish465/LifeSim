package com.mainsh;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.utils.Array;

public class GridWorld implements IndexedGraph<GridNode> {
    private int width;
    private int height;
    private GridNode[][] nodes;
    private int nodeCount;

    // Manhattan distance heuristic for A*
    private ManhattanDistance heuristic = new ManhattanDistance();

    // Pathfinder instance
    private IndexedAStarPathFinder<GridNode> pathfinder;

    public GridWorld(int width, int height) {
        this.width = width;
        this.height = height;
        this.nodeCount = width * height;
        this.nodes = new GridNode[width][height];

        // Initialize all nodes
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Initially, all nodes are walkable
                nodes[x][y] = new GridNode(x, y, index++, true);
            }
        }

        // Connect neighboring nodes
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                GridNode node = nodes[x][y];

                // Connect to adjacent nodes (4-way movement)
                // North
                if (y < height - 1) {
                    connectNodes(node, nodes[x][y + 1]);
                }
                // East
                if (x < width - 1) {
                    connectNodes(node, nodes[x + 1][y]);
                }
                // South
                if (y > 0) {
                    connectNodes(node, nodes[x][y - 1]);
                }
                // West
                if (x > 0) {
                    connectNodes(node, nodes[x - 1][y]);
                }

                // For 8-way movement, add diagonal connections
                // Northeast
                // if (x < width - 1 && y < height - 1) {
                //     connectNodes(node, nodes[x + 1][y + 1]);
                // }
                // Southeast
                // if (x < width - 1 && y > 0) {
                //     connectNodes(node, nodes[x + 1][y - 1]);
                // }
                // Southwest
                // if (x > 0 && y > 0) {
                //     connectNodes(node, nodes[x - 1][y - 1]);
                // }
                // Northwest
                // if (x > 0 && y < height - 1) {
                //     connectNodes(node, nodes[x - 1][y + 1]);
                // }
            }
        }

        // Create the A* pathfinder
        pathfinder = new IndexedAStarPathFinder<GridNode>(this);
    }

    /**
     * Helper method to connect two nodes
     */
    private void connectNodes(GridNode fromNode, GridNode toNode) {
        // Only connect if both nodes are walkable
        if (fromNode.isWalkable() && toNode.isWalkable()) {
            fromNode.addConnection(new GridConnection(fromNode, toNode));
        }
    }

    /**
     * Set whether a node is walkable and update connections
     */
    public void setWalkable(int x, int y, boolean walkable) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return; // Out of bounds
        }

        GridNode node = nodes[x][y];
        node.setWalkable(walkable);

        // Clear existing connections
        node.getConnections().clear();

        // If the node is now walkable, reconnect it to neighbors
        if (walkable) {
            // North
            if (y < height - 1 && nodes[x][y + 1].isWalkable()) {
                connectNodes(node, nodes[x][y + 1]);
                connectNodes(nodes[x][y + 1], node);
            }
            // East
            if (x < width - 1 && nodes[x + 1][y].isWalkable()) {
                connectNodes(node, nodes[x + 1][y]);
                connectNodes(nodes[x + 1][y], node);
            }
            // South
            if (y > 0 && nodes[x][y - 1].isWalkable()) {
                connectNodes(node, nodes[x][y - 1]);
                connectNodes(nodes[x][y - 1], node);
            }
            // West
            if (x > 0 && nodes[x - 1][y].isWalkable()) {
                connectNodes(node, nodes[x - 1][y]);
                connectNodes(nodes[x - 1][y], node);
            }

            // Add diagonals for 8-way movement if needed
        }
    }

    /**
     * Get a node at a specific position
     */
    public GridNode getNode(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return null; // Out of bounds
        }
        return nodes[x][y];
    }

    /**
     * Find path between start and goal positions
     */
    public GraphPath<GridNode> findPath(int startX, int startY, int goalX, int goalY) {
        GridNode startNode = getNode(startX, startY);
        GridNode goalNode = getNode(goalX, goalY);

        if (startNode == null || goalNode == null) {
            return null; // Invalid coordinates
        }

        // Create path object to store the result
        GraphPath<GridNode> path = new DefaultGraphPath<>();

        // Find the path
        boolean pathFound = pathfinder.searchNodePath(startNode, goalNode, heuristic, path);

        return pathFound ? path : null;
    }

    @Override
    public int getIndex(GridNode node) {
        return node.getIndex();
    }

    @Override
    public int getNodeCount() {
        return nodeCount;
    }

    @Override
    public Array<Connection<GridNode>> getConnections(GridNode fromNode) {
        return fromNode.getConnections();
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
