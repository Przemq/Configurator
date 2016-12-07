package domain;

public class Point  {


    private int floor;
    private boolean isMiddleSource;
    private String name;
    private float xPosition;
    private float yPosition;
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

    private int id;

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

    public float getxPosition() {
        return xPosition;
    }

    public void setxPosition(float xPosition) {
        this.xPosition = xPosition;
    }

    public float getyPosition() {
        return yPosition;
    }

    public void setyPosition(float yPosition) {
        this.yPosition = yPosition;
    }


}
