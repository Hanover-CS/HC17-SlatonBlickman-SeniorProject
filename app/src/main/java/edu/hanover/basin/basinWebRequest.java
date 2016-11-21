package edu.hanover.basin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Slaton on 11/21/2016.
 */

public class basinWebRequest {
    private static final String OPEN_BASINWEB = "http://localhost/basinWeb/v1/index.php/";

    public boolean getUsers(){
        try {
            URL url = new URL(OPEN_BASINWEB + "users");
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                int status = conn.getResponseCode();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line+"\n");
                }
                br.close();
                return true;


            }
            catch(IOException e){
                return false;
            }
        }
        catch(MalformedURLException e){
            return false;
        }


    }

}
