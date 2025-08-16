package arcanoid;

public class Brick {
    int x, y, width, height;
    int hits; 
    boolean destroyed = false;

    public Brick(int x, int y, int width, int height, int hits) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hits = hits;
    }

    public void hit() {
        hits--;
        if (hits <= 0) destroyed = true;
    }

    public boolean isDestroyed() {
        return destroyed;
    }
}
