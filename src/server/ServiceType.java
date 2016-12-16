package server;


public enum ServiceType {
    GET,
    CHECK_VERSION,
    ADD_FICHE;

    public static final String SERVER_PATH = "http://www.przem94.ayz.pl/dijkstra/";

    public static String getURL(ServiceType serviceType){
        switch(serviceType){

            case GET:
                return SERVER_PATH + "testData.txt";
            case CHECK_VERSION:
                return SERVER_PATH + "checkVersion.php";
            case ADD_FICHE:
                return SERVER_PATH + "upDateDatabase.php";
        }
        return "Service path is invalid";
    }
}
