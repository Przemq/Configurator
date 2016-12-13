package configurator;

import domain.Connection;
import domain.Point;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {

    @FXML
    public TextField floorEditText;
    public Button buttonAddConnection;
    public TextField pointPrefix;
    public TextField pathToBackgroundFile;
    public ComboBox fromListAC;
    public ComboBox toListAC;
    public ComboBox fromListDC;
    public ComboBox toListDC;
    public Button buttonDeleteConnection;
    public ComboBox deletePointList;
    public Button buttonDeletePoint;
    public Button buttonFindFile;
    public Canvas canvas;
    public ComboBox detailsPointList;
    public TextField pointDetailName;
    public ComboBox pointIsMiddleList;
    public TextField distanceText;
    public Canvas backgroundCanvas;
    public Button buttonSaveDetails;
    public Button buttonAddFloor;
    public Button buttonSaveAll;
    public Button buttonSaveFloor;
    public Button buttonFloorDOWN;
    public Button buttonFloorUP;

    private List<Connection> connections;
    private int id = 0;
    private int floor = 1;
    private int maxFloor = 0;
    private int minFloor = 0;
    private boolean allowAddPoints = true; // w produkcji ma być false
    private String from;
    private String to;
    private String pointToDelete;
    private String closestPoint;
    private String fromConnectionDel;
    private String toConnectionDel;
    private String pointToSave;
    private GraphicsContext gc;
    private GraphicsContext gcBackground;
    private ObservableList<String> items;
    private String pointToDeleteConnection;
    private LinkedHashMap<String, Point> pointListMap;
    private boolean isMiddleSource = false;
    private boolean canDeleteConnection = false;
    private boolean showInterFloorConnectionInfo = false;
    private LinkedList<String> clickedPoints = new LinkedList<>();
    private boolean canColorPoints = false;
    private HashMap<Integer, String> backgroundSourcePath;
    private float xScale = 0.88333f;
    private float yScale = 0.60443f;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = canvas.getGraphicsContext2D();
        backgroundCanvas.toBack();
        initializeTextField();
        initializeIsMiddleSourceList();
        connections = new ArrayList<>();
        pointListMap = new LinkedHashMap<>();
        backgroundSourcePath = new HashMap<>();
        backgroundSourcePath.put(0, "Grafika tymczasowa");
        addListeners();
    }

    private void addListeners() {

        canvas.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    drawPoint(event);
                    System.out.println("Rysuje punkt   "  + event.getX() + ":" + event.getY());
                }
                if (event.getButton() == MouseButton.SECONDARY) {
                    canColorPoints = true;
                    detectClickedPoint(event);
                    addPointsToQueue(closestPoint);
                    System.out.println("Dodaje połączenie");
                }
                if (event.getButton() == MouseButton.MIDDLE) {
                    detectClickedPoint(event);
                    deleteClickedPoint(closestPoint);
                    System.out.println("usuwam punkt");
                }
            }
        });

        detailsPointList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                for (Point p : pointListMap.values()) {
                    if (p.getName() == newValue) {
                        pointDetailName.setText(p.getName());
                        pointToSave = p.getName();
                        break;
                    }
                }
            }
        });

        buttonAddConnection.setOnAction(event -> addConnection());


        fromListAC.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            for (Point p : pointListMap.values()) {
                if (p.getName().equals(newValue)) {
                    from = p.getName();
                    canColorPoints = false;
                    refresh();
                }
            }
        });
        toListAC.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                for (Point p : pointListMap.values()) {
                    if (p.getName().equals(newValue)) {
                        to = p.getName();
                        System.out.print("");
                        canColorPoints = false;
                        refresh();
                    }
                }
            }
        });

        deletePointList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (newValue != null) {
                    pointToDelete = newValue.toString();
                }
                //  System.out.println(deletedPoint);
            }
        });

        fromListDC.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (newValue != null)
                    fromConnectionDel = newValue.toString();
            }
        });

        toListDC.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (newValue != null)
                    toConnectionDel = newValue.toString();
            }
        });

        pointIsMiddleList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                isMiddleSource = (boolean) newValue;
            }
        });

        buttonAddFloor.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                addFloor();
            }
        });

        buttonFloorUP.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (floor < maxFloor) {
                    floor++;
                    pointPrefix.setText(String.valueOf((char) (64 + floor)));
                    floorEditText.setText(String.valueOf(floor));
                    buttonFloorDOWN.setDisable(false);
                    if (backgroundSourcePath.get(floor) != null) {
                        setCanvasBackground(backgroundSourcePath.get(floor));
                        System.out.println(backgroundSourcePath.get(floor));
                    }
                    refresh();
                } else {
                    buttonFloorUP.setDisable(true);
                    showDialogMessage("There is no " + String.valueOf(maxFloor + 1) + "th floor", "Floor error", Alert.AlertType.INFORMATION);
                }


            }
        });

        buttonFloorDOWN.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (floor > 1) {
                    floor--;
                    pointPrefix.setText(String.valueOf((char) (64 + floor)));
                    floorEditText.setText(String.valueOf(floor));
                    buttonFloorUP.setDisable(false);
                    if (backgroundSourcePath.get(floor) != null) {
                        setCanvasBackground(backgroundSourcePath.get(floor));
                        System.out.println(backgroundSourcePath.get(floor));
                    }
                    refresh();
                } else {
                    buttonFloorDOWN.setDisable(true);
                    showDialogMessage("There is no '0' floor ", "Floor error", Alert.AlertType.INFORMATION);
                }
            }
        });
        buttonDeletePoint.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                deletePoint();
            }
        });

        buttonSaveAll.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                for (String s : backgroundSourcePath.values()) System.out.println(s);
                saveDataToFile("testowyJSON", createJSONObjectToSave(pointListMap,connections));

            }
        });

    }

    public void selectBackgroundFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select background image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.bmp"));

        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            pathToBackgroundFile.setText(selectedFile.toURI().toString());
            String filePath = selectedFile.toURI().toString();
            setCanvasBackground(filePath);
            if (backgroundSourcePath.containsKey(floor)) {
                System.out.println("Coś tu już mam zapisane");
                backgroundSourcePath.replace(floor,filePath);
            } else {
                backgroundSourcePath.put(floor, filePath);
            }
            allowAddPoints = true;
        }
    }

    private void setCanvasBackground(String filePath) {
        gcBackground = backgroundCanvas.getGraphicsContext2D();
        Image image = new Image(filePath);
        gcBackground.drawImage(image, 0, 0, backgroundCanvas.getWidth(), backgroundCanvas.getHeight());

    }

    private void drawPoint(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            if (allowAddPoints) {
                Point newPoint = new Point(id, pointPrefix.getText() + id, (float) event.getX(), (float) event.getY(), Integer.valueOf(floorEditText.getText()), false);

                if (!pointListMap.isEmpty()) {
                    for (Point p : pointListMap.values()) {
                        if (p.getxPosition() == newPoint.getxPosition() && p.getyPosition() == newPoint.getyPosition() && p.getFloor() == newPoint.getFloor()) {
                            showDialogMessage("Point is too close to " + p.getName() + " point", "Can not add point", Alert.AlertType.WARNING);
                            return;
                        }
                    }
                }
                pointListMap.put(newPoint.getName(), newPoint);
                refresh();
                id++;
            } else
                showDialogMessage("Add background image first!", "No background", Alert.AlertType.INFORMATION);

        }
    }

    private void initializeTextField() {
        floorEditText.setText(String.valueOf(floor));
        if (pointPrefix.getText().equals("")) {
            pointPrefix.setText("A");
        }
    }

    private void fillListViews(ObservableList<String> items) {
        fromListAC.setItems(items);
        toListAC.setItems(items);
        fromListDC.setItems(items);
        toListDC.setItems(items);
        deletePointList.setItems(items);
        detailsPointList.setItems(items);
    }

    private void initializeIsMiddleSourceList() {
        ObservableList<Boolean> items = FXCollections.observableArrayList(
                false, true);
        pointIsMiddleList.setItems(items);
    }

    public void savePointChanges() {
        Boolean canModify = false;
        Point tmp = null;
        for (Point p : pointListMap.values()) {
            if (p.getName().equals(pointToSave)) {
                System.out.println("Ten pkt edytuje: " + p.getName());
                tmp = p;
                String test = p.getName();
                for (String s : items) {

                    if (s.equals(pointDetailName.getText()) && !s.equals(test)) {
                        System.out.println("Znalazłem tą samą nazwę");
                        showDialogMessage("That point arleady exist!", "Can not change name", Alert.AlertType.INFORMATION);
                        canModify = false;
                        return;
                    } else {
                        //System.out.println("nie znalazłem tej nazwy w liście puktów");
                        canModify = true;
                    }
                }
            } //else
            //System.out.println("Nic ne rób");
        }
        if (canModify) {


            pointListMap.remove(tmp.getName());
            for (Connection c : connections) {
                if (c.getTo().equals(tmp.getName()))
                    c.setTo(pointDetailName.getText());
                if (c.getFrom().equals(tmp.getName()))
                    c.setFrom(pointDetailName.getText());
            }
            tmp.setName(pointDetailName.getText());
            tmp.setMiddleSource(isMiddleSource);
            pointListMap.put(tmp.getName(), tmp);


            // for (Point p : pointListMap.values()) System.out.println(p.getName() + " : " + p.isMiddleSource());
            refresh();
            return;
        }
        System.out.println("można? " + canModify);
    }

    private void addConnection() {
        if (!Objects.equals(from, to) && !from.equals(pointToDeleteConnection) && !to.equals(pointToDeleteConnection) ) {
            System.out.println(from + ":" + to);

            Connection newConnection = new Connection(pointListMap.get(from).getName(), pointListMap.get(to).getName(),pointListMap.get(from).getId(),pointListMap.get(to).getId(), calculateDistance());
            if (pointListMap.get(from).getFloor() != pointListMap.get(to).getFloor()) {
                showInterFloorConnectionInfo = true;
            } else {
                showInterFloorConnectionInfo = false;
            }

            if (!connections.isEmpty()) {

                for (Connection c : connections) {
                    if ((Objects.equals(c.getFrom(), newConnection.getFrom()) && Objects.equals(c.getTo(), newConnection.getTo()))
                            || (Objects.equals(c.getFrom(), newConnection.getTo()) && Objects.equals(c.getTo(), newConnection.getFrom()))) {
                        refresh();
                        showDialogMessage("That connection already exist!", "Info", Alert.AlertType.INFORMATION);
                        return;
                    }
                }
                connections.add(newConnection);
                if (showInterFloorConnectionInfo) {
                    showDialogMessage("Connection between points on different floors added", "Connection successfully added", Alert.AlertType.INFORMATION);
                }
                distanceText.setText(String.valueOf(calculateDistance()));
                refresh();

            } else {
                connections.add(newConnection);
                if (showInterFloorConnectionInfo) {
                    showDialogMessage("Connection between points on different floors added", "Connection successfully added", Alert.AlertType.INFORMATION);
                }
                distanceText.setText(String.valueOf(calculateDistance()));
                refresh();
            }
        } else {
            if(pointListMap.size()>1)
            showDialogMessage("Source and destination cant't be the same points", "Warning", Alert.AlertType.WARNING);
        }
    }

    private void refresh() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        for (Connection cc : connections) {
            if (pointListMap.get(cc.getFrom()).getFloor() == floor && pointListMap.get(cc.getTo()).getFloor() == floor) {
                gc.strokeLine(
                        pointListMap.get(cc.getFrom()).getxPosition() + Point.POINT_WIDTH / 2,
                        pointListMap.get(cc.getFrom()).getyPosition() + Point.POINT_HEIGHT / 2,
                        pointListMap.get(cc.getTo()).getxPosition() + Point.POINT_WIDTH / 2,
                        pointListMap.get(cc.getTo()).getyPosition() + Point.POINT_HEIGHT / 2);
            }
        }


        for (Point p : pointListMap.values()) {
            if (p.getFloor() == floor) {
                if (p.isMiddleSource()) {
                    gc.setStroke(Color.YELLOWGREEN);
                    gc.setFill(Color.YELLOWGREEN);
                } else if(!p.isMiddleSource()){
                    gc.setStroke(Color.BLACK);
                    gc.setFill(Color.BLACK);
                }
                if (clickedPoints.size() != 0) {
                    if (p.getName().equals(clickedPoints.get(0)) || p.getName().equals(from)) {
                        gc.setFill(Color.GREEN);
                        gc.fillOval(p.getxPosition(), p.getyPosition(), Point.POINT_HEIGHT, Point.POINT_WIDTH);
                    }
                }
                if (clickedPoints.size() > 1) {
                    if (p.getName().equals(clickedPoints.get(1)) || p.getName().equals(to)) {
                        gc.setFill(Color.RED);
                        gc.fillOval(p.getxPosition(), p.getyPosition(), Point.POINT_HEIGHT, Point.POINT_WIDTH);
                    }
                }

                gc.fillText(p.getName(), p.getxPosition() + 11, p.getyPosition() + 6);
                gc.strokeOval(p.getxPosition(), p.getyPosition(), Point.POINT_HEIGHT, Point.POINT_WIDTH);
            }
        }
        items = FXCollections.observableArrayList(pointListMap.keySet());
        fillListViews(items);
    }

    private void showDialogMessage(String message, String headerText, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(alertType.toString());
        alert.setContentText(message);
        alert.setHeaderText(headerText);
        alert.showAndWait();
    }

    private void deletePoint() {
        for (Point p : pointListMap.values()) {
            if (p.getName().equals(pointToDelete)) {
                pointListMap.remove(pointToDelete);
                pointToDeleteConnection = p.getName();
                deleteConnectionsAfterDeletePoint();
                refresh();
                break;
            }
        }

    }

    private int calculateDistance() {
        double x1, x2, y1, y2;
        x1 = pointListMap.get(from).getxPosition();
        y1 = pointListMap.get(from).getyPosition();
        x2 = pointListMap.get(to).getxPosition();
        y2 = pointListMap.get(to).getyPosition();

        double distance = Math.hypot(x1 - x2, y1 - y2);
        return (int) distance;
    }

    private void deleteConnectionsAfterDeletePoint() {

        for (Iterator<Connection> it = connections.iterator(); it.hasNext(); ) {
            Connection con = it.next();
            if (con.getTo().equals(pointToDeleteConnection) || con.getFrom().equals(pointToDeleteConnection)) {
                it.remove();
                System.out.println("usuwam połączenia po usunięciu punktu");
            }
        }

    }

    public void deleteConnection() {
        for (Iterator<Connection> it = connections.iterator(); it.hasNext(); ) {
            Connection con = it.next();
            if ((con.getTo().equals(fromConnectionDel) && con.getFrom().equals(toConnectionDel)
                    || (con.getTo().equals(toConnectionDel) && con.getFrom().equals(fromConnectionDel)))) {
                canDeleteConnection = true;
                it.remove();
                System.out.println("usuwam konkretne połaczenie");
                refresh();
            } else {
                System.out.println("Nie ma takiego połączenia");
                canDeleteConnection = false;
            }

        }
        if (!canDeleteConnection) {
            showDialogMessage("That connection does not exist", "Connection not found", Alert.AlertType.WARNING);
        }
    }

    private void addFloor() {
        floor++;
        maxFloor = floor;
        pointPrefix.setText(String.valueOf((char) (64 + floor)));
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        if (gcBackground != null) gcBackground.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        floorEditText.setText(String.valueOf(floor));
    }

    private void detectClickedPoint(MouseEvent event) {
        Point closestPoint = null;
        double distance = canvas.getWidth() * canvas.getHeight();
        for (Point point : pointListMap.values()) {
            float tempDistance = (float) Math.hypot(event.getX() - point.getxPosition(), event.getY() - point.getyPosition());
            if ((tempDistance < distance && point.getFloor() == floor)) {
                closestPoint = point;
                distance = tempDistance;
            }
        }

        if (closestPoint != null) {
            this.closestPoint = closestPoint.getName();
        }
    }

    private void deleteClickedPoint(String pointName) {
        for (Point p : pointListMap.values()) {
            if (p.getName().equals(pointName)) {
                pointListMap.remove(pointName);
                pointToDeleteConnection = p.getName();
                deleteConnectionsAfterDeletePoint();
                refresh();
                System.out.println("");
                break;
            }
        }
    }

    private void addPointsToQueue(String closestPoint) {
        if (closestPoint != null) {
            clickedPoints.add(closestPoint);
            if (clickedPoints.size() > 2) {
                clickedPoints.remove(0);
                to = clickedPoints.get(1);
            }
            from = clickedPoints.get(0);
            refresh();
        }
        if (clickedPoints.size() > 1) {
            //System.out.println(clickedPoints.get(0) + " : " + clickedPoints.get(1));
            to = clickedPoints.get(1);
            from = clickedPoints.get(0);
            addConnection();
            clickedPoints.clear();
        }
    }

    private void saveDataToFile(String fileName, String content) {
        File file = new File("d:/"+fileName+".txt");

        try (FileOutputStream fop = new FileOutputStream(file)) {

            if (!file.exists()) {
                file.createNewFile();
            }
            byte[] contentInBytes = content.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();

            System.out.println("Done");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String createJSONObjectToSave(HashMap<String, Point> pointMap, List<Connection> connectionsList){
        JSONObject data = new JSONObject();
        JSONArray pointsArray = new JSONArray();
        JSONArray connectionsArray = new JSONArray();
        for(Point p : pointMap.values()){
            JSONObject point = new JSONObject();
            try {
                point.put("id",p.getId());
                point.put("name", p.getName());
                point.put("xPosition",p.getxPosition()/xScale);
                point.put("yPosition", p.getyPosition()/yScale);
                point.put("floor",p.getFloor());
                point.put("isMiddleSource", p.isMiddleSource());
                pointsArray.put(point);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        try {
            data.put("pointsArray",pointsArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (Connection c : connectionsList){
            JSONObject connection = new JSONObject();
            try {
                connection.put("from",c.getSource());
                connection.put("to", c.getDestination());
                connection.put("distance", c.getDistance());
                connectionsArray.put(connection);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        try {
            data.put("connectionsArray",connectionsArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data.toString();
    }

}