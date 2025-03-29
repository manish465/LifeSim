package com.mainsh;

import com.badlogic.gdx.ai.pfa.Heuristic;

public class ManhattanDistance implements Heuristic<GridNode> {
    @Override
    public float estimate(GridNode node, GridNode goal) {
        // Calculate Manhattan distance: |x1 - x2| + |y1 - y2|
        return Math.abs(node.getX() - goal.getX()) + Math.abs(node.getY() - goal.getY());
    }
}
