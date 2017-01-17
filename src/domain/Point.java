package domain;

public class Point  {
    private int id;
    private String name;
    private float xPosition;
    private float yPosition;
    private int floor;
    private boolean isMiddleSource;
    public static double POINT_WIDTH = 8;
    public static double POINT_HEIGHT = 8;


    public Point(int id, String name, float xPosition, float yPosition, int floor, boolean isMiddleSource) {
        this.id = id;
        this.floor = floor;
        this.name = name;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.isMiddleSource = isMiddleSource;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public boolean isMiddleSource() {
        return isMiddleSource;
    }

    public void setMiddleSource(boolean middleSource) {
        isMiddleSource = middleSource;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getXPosition() {
        return xPosition;
    }

    public void setXPosition(float xPosition) {
        this.xPosition = xPosition;
    }

    public float getYPosition() {
        return yPosition;
    }

    public void setYPosition(float yPosition) {
        this.yPosition = yPosition;
    }


}
