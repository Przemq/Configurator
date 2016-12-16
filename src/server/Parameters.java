package server;

import java.util.HashMap;


public class Parameters {

   private HashMap<String, String> parameters = new HashMap<String, String>();

    public Parameters addParam(String key, String value){
        parameters.put(key,value);
        return this;
    }

    public HashMap<String, String> getParameters() {
        return parameters;
    }
}
