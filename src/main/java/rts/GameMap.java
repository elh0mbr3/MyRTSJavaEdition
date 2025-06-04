package rts;
import java.util.Random;
class GameMap {
    private Tile[][] tiles;

    public GameMap(int width, int height) {
        tiles = new Tile[height][width];
        for(int row = 0; row < height; row++) {
            for(int col = 0; col < width; col++) {
                tiles[row][col] = Tile.GRASS;
            }
        }
        generateSeashore();
        generateLakes();
        generateRiverWithBridges();
    }

    private void generateSeashore() {
        int w = getWidth();
        int h = getHeight();
        for(int x = 0; x < w; x++) {
            tiles[0][x] = Tile.WATER;
            tiles[h-1][x] = Tile.WATER;
        }
        for(int y = 0; y < h; y++) {
            tiles[y][0] = Tile.WATER;
            tiles[y][w-1] = Tile.WATER;
        }
    }

    private void generateLakes() {
        Random rand = new Random();
        int lakeCount = 2 + rand.nextInt(2); // 2-3 lakes
        int w = getWidth();
        int h = getHeight();
        for(int i = 0; i < lakeCount; i++) {
            int cx = 2 + rand.nextInt(w - 4);
            int cy = 2 + rand.nextInt(h - 4);
            int radius = 2 + rand.nextInt(2);
            for(int y = -radius; y <= radius; y++) {
                for(int x = -radius; x <= radius; x++) {
                    int dx = cx + x;
                    int dy = cy + y;
                    if(dx >= 1 && dy >= 1 && dx < w-1 && dy < h-1) {
                        if(x*x + y*y <= radius*radius) {
                            tiles[dy][dx] = Tile.WATER;
                        }
                    }
                }
            }
        }
    }

    private void generateRiverWithBridges() {
        Random rand = new Random();
        boolean vertical = rand.nextBoolean();
        int w = getWidth();
        int h = getHeight();
        if(vertical) {
            int x = 3 + rand.nextInt(w - 6);
            for(int y = 0; y < h; y++) {
                tiles[y][x] = Tile.WATER;
            }
            for(int i=0;i<2;i++) {
                int by = 1 + rand.nextInt(h-2);
                tiles[by][x] = Tile.BRIDGE;
            }
        } else {
            int y = 3 + rand.nextInt(h - 6);
            for(int x = 0; x < w; x++) {
                tiles[y][x] = Tile.WATER;
            }
            for(int i=0;i<2;i++) {
                int bx = 1 + rand.nextInt(w-2);
                tiles[y][bx] = Tile.BRIDGE;
            }
        }
    }

    public Tile getTile(int x, int y) {
        return tiles[y][x];
    }

    public void setTile(int x, int y, Tile tile) {
        tiles[y][x] = tile;
    }

    public int getWidth() { return tiles[0].length; }
    public int getHeight() { return tiles.length; }
}
