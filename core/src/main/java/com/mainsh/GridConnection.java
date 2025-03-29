package com.mainsh;

import com.badlogic.gdx.ai.pfa.Connection;

public class GridConnection implements Connection<GridNode> {
    private GridNode fromNode;
    private GridNode toNode;
    private float cost;

    public GridConnection(GridNode fromNode, GridNode toNode) {
        this.fromNode = fromNode;
        this.toNode = toNode;

        // Calculate cost (can be customized based on terrain, etc.)
        // For diagonal connections, use 1.414f (sqrt(2)) instead
        this.cost = 1.0f;
    }

    @Override
    public float getCost() {
        return cost;
    }

    @Override
    public GridNode getFromNode() {
        return fromNode;
    }

    @Override
    public GridNode getToNode() {
        return toNode;
    }
}
