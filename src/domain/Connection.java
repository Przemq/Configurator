package domain;

/**
 * Created by Przemek on 24.11.2016.
 */
public class Connection {
    private String from;
    private String to;
    private int source;
    private int destination;
    private int distance;

    public Connection(String from, String to,int source,int destination, int distance) {
        this.from = from;
        this.to = to;
        this.source = source;
        this.destination = destination;
        this.distance = distance;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }
}
