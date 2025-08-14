package arcanoid;

public class Brick {
    int x, y, width, height;
    boolean destroyed;

    Brick(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.destroyed = false;
    }
}