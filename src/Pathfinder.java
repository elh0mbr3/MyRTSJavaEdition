import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * A simple Pathfinder class implementing the A* algorithm for grid-based pathfinding.
 */
public class Pathfinder {

    private static class Node {
        int x, y;
        int g, h, f;
        Node parent;

        Node(int x, int y, int g, int h, Node parent) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.h = h;
            this.f = g + h;
            this.parent = parent;
        }
    }

    /**
     * Finds a path from the start to the goal on the provided map. Only GRASS tiles are passable.
     * @param map The game map.
     * @param start The starting tile coordinates.
     * @param goal The goal tile coordinates.
     * @return A list of Points representing the path.
     */
    public static List<Point> findPath(GameMap map, Point start, Point goal) {
        List<Point> path = new ArrayList<>();
        if (start.equals(goal)) return path;

        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        boolean[][] closed = new boolean[map.getHeight()][map.getWidth()];

        Node startNode = new Node(start.x, start.y, 0, manhattan(start, goal), null);
        openList.add(startNode);

        Node current = null;
        while (!openList.isEmpty()) {
            current = openList.poll();
            if (current.x == goal.x && current.y == goal.y) break;
            closed[current.y][current.x] = true;
            int[][] directions = { {0, 1}, {1, 0}, {0, -1}, {-1, 0} };
            for (int[] d : directions) {
                int nx = current.x + d[0];
                int ny = current.y + d[1];
                if (nx < 0 || ny < 0 || nx >= map.getWidth() || ny >= map.getHeight()) continue;
                Tile t = map.getTile(nx, ny);
                if (t != Tile.GRASS && t != Tile.BRIDGE) continue;
                if (closed[ny][nx]) continue;
                int gNew = current.g + 1;
                int hNew = manhattan(new Point(nx, ny), goal);
                Node neighbour = new Node(nx, ny, gNew, hNew, current);
                openList.add(neighbour);
            }
        }
        if (current == null || (current.x != goal.x || current.y != goal.y)) return path;
        while (current != null) {
            path.add(0, new Point(current.x, current.y));
            current = current.parent;
        }
        return path;
    }

    private static int manhattan(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }
}
