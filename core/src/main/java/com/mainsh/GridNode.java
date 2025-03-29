package com.mainsh;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.utils.Array;

public class GridNode {
    private final int x;
    private final int y;

    // Unique index used by the A* algorithm
    private final int index;

    // Whether this node is walkable
    private boolean walkable;

    // Array of connections to neighboring nodes
    private Array<Connection<GridNode>> connections;

    public GridNode(int x, int y, int index, boolean walkable) {
        this.x = x;
        this.y = y;
        this.index = index;
        this.walkable = walkable;
        this.connections = new Array<Connection<GridNode>>();
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getIndex() { return index; }
    public boolean isWalkable() { return walkable; }
    public void setWalkable(boolean walkable) { this.walkable = walkable; }

    public Array<Connection<GridNode>> getConnections() {
        return connections;
    }

    public void addConnection(GridConnection connection) {
        connections.add(connection);
    }
}
