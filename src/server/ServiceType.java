package server;


public enum ServiceType {
    GET,
    SET_CONFIGURATION,
    SEND_FILE,;

    public static final String SERVER_PATH = "http://www.przem94.ayz.pl/dijkstra/";

    public static String getURL(ServiceType serviceType){
        switch(serviceType){

            case GET:
                return SERVER_PATH + "testData.txt";
            case SET_CONFIGURATION:
                return SERVER_PATH + "getData.php";
            case SEND_FILE:
                return SERVER_PATH + "uploadToServer.php";

        }
        return "Service path is invalid";
    }
}
