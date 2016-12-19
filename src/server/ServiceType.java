package server;


public enum ServiceType {
    GET,
    CHECK_VERSION,
    SET;

    public static final String SERVER_PATH = "http://www.przem94.ayz.pl/dijkstra/";

    public static String getURL(ServiceType serviceType){
        switch(serviceType){

            case GET:
                return SERVER_PATH + "testData.txt";
            case SET:
                return SERVER_PATH + "getData.php ";
            case CHECK_VERSION:
                return SERVER_PATH + "no.txt";

        }
        return "Service path is invalid";
    }
}
