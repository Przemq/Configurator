package server;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;


public class ServerRequest extends Thread {

    private ServiceType serviceType;
    private Parameters parameters;
    private ServerRequestListener serverRequestListener;

    public ServerRequest(ServiceType serviceType, Parameters parameters) {
        this.serviceType = serviceType;
        this.parameters = parameters;
    }

    public ServerRequest setServerRequestListener(ServerRequestListener serverRequestListener) {
        this.serverRequestListener = serverRequestListener;
        return this;
    }


    private String doInBackground() {

        try {
            URL url = new URL(ServiceType.getURL(serviceType));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            HashMap<String, String> params = new HashMap<>();
            for (String key : parameters.getParameters().keySet()) {
                params.put(key, parameters.getParameters().get(key));
            }

            OutputStream os = connection.getOutputStream();


            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQuery(params));
            System.out.println("params: " + getQuery(params));
            writer.flush();
            writer.close();
            os.close();

            InputStream in = new BufferedInputStream(connection.getInputStream());
            return streamToString(in);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    private String getQuery(HashMap<String, String> params) {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (String key : params.keySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            try {
                result.append(URLEncoder.encode(key, "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(params.get(key), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return result.toString();
    }


    protected void onPostExecute(String result) {
        if (serverRequestListener != null) {

            serverRequestListener.onSuccess(result);
        }
    }

    public static String streamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    @Override
    public void run() {
        super.run();
        onPostExecute(doInBackground());

    }

    public interface ServerRequestListener {
        void onSuccess(String json);
    }

}
