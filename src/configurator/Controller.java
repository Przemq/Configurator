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
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import server.Parameters;
import server.ServerRequest;
import server.ServiceType;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

import static utils.IOHelper.*;

public class Controller implements Initializable {

    @FXML
    public TextField floorEditText;
    public Button buttonAddConnection;
    public TextField pointPrefix;
    public TextField pathToBackgroundFile;
    public ComboBox fromListAC;
    public ComboBox toListAC;
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
    public Button buttonFloorDOWN;
    public Button buttonFloorUP;
    public Button buttonEditConf;
    public Button deleteConnectionButton;
    public Button buttonSaveImages;
    public ListView connectionsList;
    public ListView pointList;

    private List<Connection> connections;
    private LinkedHashMap<String, Point> pointMap;
    private LinkedList<String> clickedPoints = new LinkedList<>();
    private GraphicsContext background;
    private int id = 0;
    private String from;
    private String to;
    private ObservableList<String> items;
    private int floor = 1;
    private boolean isMiddleSource = false;

    private int idName = 0;
    private int maxFloor = 0;
    private boolean allowAddPoints = false;
    private String pointToDelete;
    private String closestPoint;
    private String pointToSave;
    private GraphicsContext gc;
    private String pointToDeleteConnection;
    private boolean canDeleteConnection = false;
    private boolean showInterFloorConnectionInfo = false;
    private HashMap<Integer, String> backgroundSourcePath;
    private float xScale = 0.888f;
    private float yScale = 0.612f;
    private int idToStartDecrement;
    private int totalFloorsNumber = 1;
    private String toTEST;
    private String fromTEST;
    private ObservableList<String> connectionsItems;
    private ObservableList<String> pointsItems;
    private static float Y_CORRECTION_FOR_DISTANCE_TEXT = 3;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = canvas.getGraphicsContext2D();
        backgroundCanvas.toBack();
        initializeTextField();
        initializeIsMiddleSourceList();
        connections = new ArrayList<>();
        pointMap = new LinkedHashMap<>();
        backgroundSourcePath = new HashMap<>();
        backgroundSourcePath.put(0, "");
        floorEditText.setEditable(false);
        setOnStartBackground();
        addListeners();
    }

    private void addListeners() {

        canvas.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                addPoint(event);
            }
            if (event.getButton() == MouseButton.SECONDARY) {
                detectClickedPoint(event);
                addPointsToQueue(closestPoint);
            }
            if (event.getButton() == MouseButton.MIDDLE) {
                detectClickedPoint(event);
                deleteClickedPoint(closestPoint);
                decrementID(idToStartDecrement);
            }
        });

        detailsPointList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                for (Point p : pointMap.values()) {
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
            for (Point p : pointMap.values()) {
                if (p.getName().equals(newValue)) {
                    from = p.getName();
                    fromTEST = newValue.toString();
                    refresh();
                }
            }
        });
        toListAC.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                for (Point p : pointMap.values()) {
                    if (p.getName().equals(newValue)) {
                        to = p.getName();
                        toTEST = newValue.toString();
                        System.out.print("");
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
            }
        });

        pointIsMiddleList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                isMiddleSource = (boolean) newValue;
            }
        });

        buttonAddFloor.setOnAction(event -> {
            addFloor();
            allowAddPoints = false;
        });

        buttonFloorUP.setOnAction(event -> {

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
                showMessage("There is no " + String.valueOf(maxFloor + 1) + "th floor", "Floor error", Alert.AlertType.INFORMATION);
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
                    showMessage("There is no '0' floor ", "Floor error", Alert.AlertType.INFORMATION);
                }
            }
        });
        buttonDeletePoint.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                deletePoint();
                for (Point pkt : pointMap.values()) {
                    if (pkt.getId() >= idToStartDecrement) {
                        pkt.setId(pkt.getId() - 1);
                    }
                }
            }
        });

        buttonSaveAll.setOnAction(event -> {
            refreshConnectionsID();
            checkIfAddedConnectionBetweenFloors();

        });

        buttonEditConf.setOnAction(event -> {
            selectConfigurationFile();
            id = pointMap.size();

        });

        deleteConnectionButton.setOnAction(event -> {
            deleteConnection();
            System.out.println("deleteConection");
        });

        buttonSaveImages.setOnAction(event -> sendImageOnServer());

    }

    public void selectBackgroundFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select background image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.bmp"));

        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            pathToBackgroundFile.setText(selectedFile.toURI().toString());
            String filePath = selectedFile.toURI().toString();
            setCanvasBackground(filePath);
            if (backgroundSourcePath.containsKey(floor)) {
                System.out.println("Coś tu już mam zapisane");
                backgroundSourcePath.replace(floor, filePath);
            } else {
                backgroundSourcePath.put(floor, filePath);
            }
            allowAddPoints = true;
        }
        for (String s : backgroundSourcePath.values()) System.out.println(s);
    }

    private void setCanvasBackground(String filePath) {
        background = backgroundCanvas.getGraphicsContext2D();
        Image image = new Image(filePath);
        background.drawImage(image, 0, 0, backgroundCanvas.getWidth(), backgroundCanvas.getHeight());

    }

    private void addPoint(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            if (allowAddPoints) {
                Point newPoint = new Point(id, pointPrefix.getText() + idName, (float) event.getX(), (float) event.getY(), Integer.valueOf(floorEditText.getText()), false);

                if (!pointMap.isEmpty()) {
                    for (Point p : pointMap.values()) {
                        if (p.getXPosition() == newPoint.getXPosition() && p.getYPosition() == newPoint.getYPosition() && p.getFloor() == newPoint.getFloor()) {
                            showMessage("Point is too close to " + p.getName() + " point", "Can not add point", Alert.AlertType.WARNING);
                            return;
                        }
                    }
                }
                pointMap.put(newPoint.getName(), newPoint);
                refresh();
                id++;
                idName++;
                System.out.println("");
                for (Point p : pointMap.values()) System.out.println(p.getName() + ":" + p.getId());
                System.out.println("");
            } else
                showMessage("Add background image first!", "No background", Alert.AlertType.INFORMATION);
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
        deletePointList.setItems(items);
        detailsPointList.setItems(items);
        fillUILists();

    }

    private void initializeIsMiddleSourceList() {
        ObservableList<Boolean> items = FXCollections.observableArrayList(
                false, true);
        pointIsMiddleList.setItems(items);
    }

    public void savePointChanges() {
        Boolean canModify = false;
        Point tmp = null;
        for (Point p : pointMap.values()) {
            if (p.getName().equals(pointToSave)) {
                System.out.println("Ten pkt edytuje: " + p.getName());
                tmp = p;
                String test = p.getName();
                for (String s : items) {

                    if (s.equals(pointDetailName.getText()) && !s.equals(test)) {
                        System.out.println("Znalazłem tą samą nazwę");
                        showMessage("That point arleady exist!", "Can not change name", Alert.AlertType.INFORMATION);
                        canModify = false;
                        return;
                    } else {
                        canModify = true;
                    }
                }
            }
        }
        if (canModify) {


            pointMap.remove(tmp.getName());
            for (Connection c : connections) {
                if (c.getTo().equals(tmp.getName()))
                    c.setTo(pointDetailName.getText());
                if (c.getFrom().equals(tmp.getName()))
                    c.setFrom(pointDetailName.getText());
            }
            //decrementID(tmp.getId()+1);
            // decrementConnectionsID(tmp.getId()+1);
            tmp.setName(pointDetailName.getText());
            tmp.setMiddleSource(isMiddleSource);
            pointMap.put(tmp.getName(), tmp);
            refresh();

        }

    }

    private void addConnection() {
        if (!Objects.equals(from, to) && !from.equals(pointToDeleteConnection) && !to.equals(pointToDeleteConnection)) {
            Connection newConnection = new Connection(pointMap.get(from).getName(), pointMap.get(to).getName(), pointMap.get(from).getId(), pointMap.get(to).getId(), calculateDistance());
            if (pointMap.get(from).getFloor() != pointMap.get(to).getFloor()) {
                showInterFloorConnectionInfo = true;
            } else {
                showInterFloorConnectionInfo = false;
            }
            if (!connections.isEmpty()) {

                for (Connection c : connections) {
                    if ((Objects.equals(c.getFrom(), newConnection.getFrom()) && Objects.equals(c.getTo(), newConnection.getTo()))
                            || (Objects.equals(c.getFrom(), newConnection.getTo()) && Objects.equals(c.getTo(), newConnection.getFrom()))) {
                        refresh();
                        showMessage("That connection already exist!", "Duplicate connection", Alert.AlertType.INFORMATION);
                        return;
                    }
                }
                connections.add(newConnection);
                if (showInterFloorConnectionInfo) {
                    showMessage("Connection between points on different floors added", "Connection successfully added", Alert.AlertType.INFORMATION);
                }
                distanceText.setText(String.valueOf(calculateDistance()));
                refresh();

            } else {
                connections.add(newConnection);
                if (showInterFloorConnectionInfo) {
                    showMessage("Connection between points on different floors added", "Connection successfully added", Alert.AlertType.INFORMATION);
                }
                distanceText.setText(String.valueOf(calculateDistance()));
                refresh();
            }
        } else {
            if (pointMap.size() > 1)
                showMessage("Source and destination cant't be the same points", "Warning", Alert.AlertType.WARNING);
        }
    }

    private void refresh() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        for (Connection cc : connections) {
            if (pointMap.get(cc.getFrom()).getFloor() == floor && pointMap.get(cc.getTo()).getFloor() == floor) {
                gc.strokeLine(
                        pointMap.get(cc.getFrom()).getXPosition(),
                        pointMap.get(cc.getFrom()).getYPosition(),
                        pointMap.get(cc.getTo()).getXPosition(),
                        pointMap.get(cc.getTo()).getYPosition());
                gc.setFill(Color.GREY);
                gc.fillText(String.valueOf(cc.getDistance()), (pointMap.get(cc.getFrom()).getXPosition() + pointMap.get(cc.getTo()).getXPosition()) / 2
                        , (pointMap.get(cc.getFrom()).getYPosition() + pointMap.get(cc.getTo()).getYPosition()) / 2 - Y_CORRECTION_FOR_DISTANCE_TEXT);
                gc.setFill(Color.BLACK);
            }
        }
        for (Point p : pointMap.values()) {
            if (p.getFloor() == floor) {
                if (p.isMiddleSource()) {
                    gc.setStroke(Color.YELLOWGREEN);
                    gc.setFill(Color.YELLOWGREEN);
                } else if (!p.isMiddleSource()) {
                    gc.setStroke(Color.BLACK);
                    gc.setFill(Color.BLACK);
                }
                if (clickedPoints.size() != 0) {
                    if (p.getName().equals(clickedPoints.get(0)) || p.getName().equals(from)) {
                        gc.setFill(Color.GREEN);
                        gc.fillOval(p.getXPosition() - Point.POINT_WIDTH / 2, p.getYPosition() - Point.POINT_HEIGHT / 2, Point.POINT_HEIGHT, Point.POINT_WIDTH);
                    }
                }
                if (clickedPoints.size() > 1) {
                    if (p.getName().equals(clickedPoints.get(1)) || p.getName().equals(to)) {
                        gc.setFill(Color.RED);
                        gc.fillOval(p.getXPosition() - Point.POINT_WIDTH / 2, p.getYPosition() - Point.POINT_HEIGHT / 2, Point.POINT_HEIGHT, Point.POINT_WIDTH);
                    }
                }

                gc.fillText(p.getName(), p.getXPosition() + 8, p.getYPosition() + 5);
                gc.strokeOval(p.getXPosition() - Point.POINT_WIDTH / 2, p.getYPosition() - Point.POINT_HEIGHT / 2, Point.POINT_HEIGHT, Point.POINT_WIDTH);
            }
        }
        items = FXCollections.observableArrayList(pointMap.keySet());
        fillListViews(items);
    }

    private void showMessage(String message, String headerText, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(alertType.toString());
        alert.setContentText(message);
        alert.setHeaderText(headerText);
        alert.showAndWait();
    }

    private void deletePoint() {
        for (Point p : pointMap.values()) {
            if (p.getName().equals(pointToDelete)) {
                idToStartDecrement = p.getId() + 1;
                pointMap.remove(pointToDelete);
                decrementID(idToStartDecrement);
                //decrementConnectionsID(idToStartDecrement);
                pointToDeleteConnection = p.getName();
                deleteConnectionsAfterDeletePoint();
                refresh();
                break;
            }
        }
    }

    private int calculateDistance() {
        double x1, x2, y1, y2;
        x1 = pointMap.get(from).getXPosition();
        y1 = pointMap.get(from).getYPosition();
        x2 = pointMap.get(to).getXPosition();
        y2 = pointMap.get(to).getYPosition();

        double distance = Math.hypot(x1 - x2, y1 - y2);
        return (int) distance;
    }

    private void deleteConnectionsAfterDeletePoint() {

        for (Iterator<Connection> it = connections.iterator(); it.hasNext(); ) {
            Connection con = it.next();
            if (con.getTo().equals(pointToDeleteConnection) || con.getFrom().equals(pointToDeleteConnection)) {
                it.remove();
            }
        }

    }

    private void deleteConnection() {
        for (Iterator<Connection> it = connections.iterator(); it.hasNext(); ) {
            Connection con = it.next();
            if ((con.getTo().equals(fromTEST) && con.getFrom().equals(toTEST)
                    || (con.getTo().equals(toTEST) && con.getFrom().equals(fromTEST)))) {
                canDeleteConnection = true;
                it.remove();
                System.out.println("usuwam konkretne połaczenie");
                refresh();
                break;
            } else {
                System.out.println("Nie ma takiego połączenia");
                canDeleteConnection = false;
            }

        }
        if (!canDeleteConnection) {
            showMessage("That connection does not exist", "Connection not found", Alert.AlertType.WARNING);
        }
    }

    private void addFloor() {
        floor++;
        totalFloorsNumber++;
        maxFloor = floor;
        pointPrefix.setText(String.valueOf((char) (64 + floor)));
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        if (background != null) background.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        floorEditText.setText(String.valueOf(floor));
    }

    private void detectClickedPoint(MouseEvent event) {
        Point closestPoint = null;
        double distance = canvas.getWidth() * canvas.getHeight();
        for (Point point : pointMap.values()) {
            float tempDistance = (float) Math.hypot(event.getX() - point.getXPosition(), event.getY() - point.getYPosition());
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
        for (Point p : pointMap.values()) {
            if (p.getName().equals(pointName)) {
                pointMap.remove(pointName);
                pointToDeleteConnection = p.getName();
                idToStartDecrement = p.getId() + 1;
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
            to = clickedPoints.get(1);
            from = clickedPoints.get(0);
            addConnection();
            clickedPoints.clear();
        }
    }

    private String createJSONObjectToSave(HashMap<String, Point> pointMap, List<Connection> connectionsList) {
        JSONObject data = new JSONObject();
        JSONArray pointsArray = new JSONArray();
        JSONArray connectionsArray = new JSONArray();
        try {
            for (Point p : pointMap.values()) {
                JSONObject point = new JSONObject();

                point.put("id", p.getId());
                point.put("name", p.getName());
                point.put("xPosition", p.getXPosition() / xScale);
                point.put("yPosition", p.getYPosition() / yScale);
                point.put("floor", p.getFloor());
                point.put("isMiddleSource", p.isMiddleSource());
                pointsArray.put(point);
                data.put("pointsArray", pointsArray);
            }

            for (Connection c : connectionsList) {
                JSONObject connection = new JSONObject();

                connection.put("source", c.getSource());
                connection.put("destination", c.getDestination());
                connection.put("distance", c.getDistance());
                connection.put("from", c.getFrom());
                connection.put("to", c.getTo());
                connectionsArray.put(connection);
            }

            data.put("connectionsArray", connectionsArray);

            JSONArray floors = new JSONArray();
            for (Integer floor : backgroundSourcePath.keySet())
            {
                    if(floor != 0) {
                        floors.put(new File(backgroundSourcePath.get(floor)).getName());
                    }
            }
            JSONObject metaData = new JSONObject();
            metaData.put("idName", idName);
            metaData.put("numberOfFloor", totalFloorsNumber);

            metaData.put("floorsImages",floors);
            data.put("metaData", metaData);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data.toString();
    }

    private void decrementID(int idToStart) {
        for (Point pkt : pointMap.values()) {
            if (pkt.getId() >= idToStart) {
                pkt.setId(pkt.getId() - 1);
            }
        }
        id--;
        System.out.println("");
        for (Point p : pointMap.values()) System.out.println(p.getName() + ":" + p.getId());
        System.out.println("");

    }

    private void parseConfigurationFIle(String json) {
        pointMap.clear();
        connections.clear();
        allowAddPoints = true;
        try {
            System.out.println(json);
            JSONObject receivedData = new JSONObject(json);

            JSONObject metaData = receivedData.getJSONObject("metaData");
            idName = metaData.getInt("idName");
            maxFloor = metaData.getInt("numberOfFloor");
            totalFloorsNumber = metaData.getInt("numberOfFloor");
            JSONArray floorImages = metaData.getJSONArray("floorsImages");
            for (int i = 1; i <= floorImages.length(); i++){
                backgroundSourcePath.put(i,"file:/C:/Users/PrzemekMadzia/Desktop/Grafiki%20inżynierka/"+floorImages.getString(i-1));
            }
            setCanvasBackground(backgroundSourcePath.get(floor));

            JSONArray pointsArray = receivedData.getJSONArray("pointsArray");
            JSONArray connectionsArray = receivedData.getJSONArray("connectionsArray");
            for (int i = 0; i < pointsArray.length(); i++) {
                JSONObject p = pointsArray.getJSONObject(i);
                int id = p.getInt("id");
                String name = p.getString("name");
                float xPosition = (float) p.getDouble("xPosition");
                float yPosition = (float) p.getDouble("yPosition");
                int floor = p.getInt("floor");
                boolean isMiddleSource = p.getBoolean("isMiddleSource");
                Point tmpPoint = new Point(id, name, xPosition * xScale, yPosition * yScale, floor, isMiddleSource);
                pointMap.put(tmpPoint.getName(), tmpPoint);
                System.out.println("idPoint: " + id + "  name: " + name + "  x: " + xPosition + "  y: " + yPosition + "  floor: " + floor + "  isMiddleSource: " + isMiddleSource);
            }
            for (int i = 0; i < connectionsArray.length(); i++) {
                JSONObject c = connectionsArray.getJSONObject(i);
                int source = c.getInt("source");
                int destination = c.getInt("destination");
                int distance = c.getInt("distance");
                String from = c.getString("from");
                String to = c.getString("to");
                Connection tempConnection = new Connection(from, to, source, destination, distance);
                connections.add(tempConnection);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void selectConfigurationFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select configuration file");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            pathToBackgroundFile.setText(selectedFile.toURI().toString());
            String filePath = selectedFile.toString();
            parseConfigurationFIle(readJSONFromFIle(filePath));
            refresh();
        }
    }

    private void saveFileDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select background image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File selectedFile = fileChooser.showSaveDialog(new Stage());
        if (selectedFile != null) {
            saveFile(createJSONObjectToSave(pointMap, connections), selectedFile);
            sendDataOnHosting(createJSONObjectToSave(pointMap, connections));
            showMessage("File saved", "Success", Alert.AlertType.INFORMATION);
        }
    }

    private void checkIfAddedConnectionBetweenFloors() {
        int addedConnecionsBetweenFloors = 0;
        for (Connection c : connections) {
            if ((pointMap.get(c.getFrom()).getFloor() != pointMap.get(c.getTo()).getFloor())) {
                addedConnecionsBetweenFloors ++;
            }
        }
        if (addedConnecionsBetweenFloors >= totalFloorsNumber -1 || totalFloorsNumber == 1) {
            saveFileDialog();
        } else {
            showMessage("You should add connection between floors", "No connection between all floors", Alert.AlertType.INFORMATION);
            System.out.println(addedConnecionsBetweenFloors);
        }

    }

    private void refreshConnectionsID() {
        for (Connection c : connections) {
            c.setSource(pointMap.get(c.getFrom()).getId());
            c.setDestination(pointMap.get(c.getTo()).getId());
        }
    }

    private void sendDataOnHosting(String dataToSend) {
        new ServerRequest(ServiceType.SET_CONFIGURATION, new Parameters().addParam("dane", dataToSend)).start();
    }

    private void sendImageOnServer() {
        String uri;
        if (backgroundSourcePath.size() < 2) {
            showMessage("No file to upload", "No file selected", Alert.AlertType.INFORMATION);
            return;
        }
        for (String s : backgroundSourcePath.values()) {
            if (!s.equals("")) {
                try {
                    uri = Paths.get(new URL(s).toURI()).toFile().toString();
                    new ServerRequest(ServiceType.SEND_FILE, new Parameters().addParam("img", uri)).start();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

            }
        }
        showMessage("Files successfully uploaded to server", "Upload successful", Alert.AlertType.INFORMATION);
    }

    private void setOnStartBackground(){
        background = backgroundCanvas.getGraphicsContext2D();
        Image img = new Image("file:/C:/Users/PrzemekMadzia/Desktop/Grafiki%20inżynierka/logo.png");
        background.drawImage(img, 0, backgroundCanvas.getHeight()/3.5, backgroundCanvas.getWidth(), backgroundCanvas.getHeight()/2.7);
    }

    private void fillUILists(){

        ArrayList connectionsUIList = new ArrayList();
        ArrayList pointsUIList = new ArrayList();

        for (Connection c : connections){
            String tmp = c. getFrom() + "->" + c.getTo();
            connectionsUIList.add(tmp);
        }
        connectionsItems = FXCollections.observableArrayList(connectionsUIList);
        connectionsList.setItems(connectionsItems);

        for (String p : pointMap.keySet()){
            pointsUIList.add(p);
        }

        pointsItems = FXCollections.observableArrayList(pointsUIList);
        pointList.setItems(pointsItems);

    }

}
