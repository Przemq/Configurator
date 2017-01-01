package server;

import configurator.Controller;
import javafx.scene.control.Alert;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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
        String serverURL = ServiceType.getURL(serviceType);
        if (serverURL.equals(ServiceType.getURL(ServiceType.SEND_FILE))) {
         new Thread(()->uploadFile(parameters.getParameters().get("img"))).start();
            return null;
        } else{
            try {
                URL url = new URL(serverURL);
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

    public void uploadFile(String sourceFileUri) {
        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        try {

            // open a URL connection to the Servlet
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(ServiceType.getURL(ServiceType.SEND_FILE));

            // Open a HTTP  connection to  the URL
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("uploaded_file", fileName);

            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                    + fileName + "\"" + lineEnd);

            dos.writeBytes(lineEnd);

            // create a buffer of  maximum size
            bytesAvailable = fileInputStream.available();

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);


            while (bytesRead > 0) {

                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            //close the streams //
            fileInputStream.close();
            dos.flush();
            dos.close();
            InputStream in = new BufferedInputStream(conn.getInputStream());
            //System.out.println(ServerRequest.streamToString(in));
        } catch (MalformedURLException ex) {

            ex.printStackTrace();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

}
