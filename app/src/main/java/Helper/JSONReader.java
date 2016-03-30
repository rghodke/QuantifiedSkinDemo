package Helper;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Retrieves the JSON data from an URL. It works by retrieving the JSON data
 * in a buffered reader and passing the output to a StringBuilder. The StringBuilder then passing
 * that string into a JSONArray (or JSONObject) to create the returning object.
 */
public class JSONReader {

    //Empty constructor
    public JSONReader() {

    }

    /*
    Returns a JSONObject object based on the data found at jsonURL.

    @param imageURL  The url of the image
    @return         The JSONObject located at specified url
     */
    public JSONObject readObject(String imageURL) {
        InputStream inputStream = null;
        String result = null;
        HttpURLConnection urlConnection;
        String jsonURL = "https://faceplusplus-faceplusplus.p.mashape.com/detection/detect?attribute=gender&url="+imageURL;
        try {
            //Form a url from the string passed in
            URL url = new URL(jsonURL);
            //Get the text input from the url
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("X-Mashape-Key", "oE931Xl6kLmshWPXhGr3MJIJj2djp13CuyNjsnm7fNQKvdP528");
            inputStream = new BufferedInputStream(urlConnection.getInputStream());
            // read the json data that is UTF-8 by default
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, "UTF-8"), 8);
            //Store the result into a string
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            result = sb.toString();
            //Disconnect the connection
            urlConnection.disconnect();

        } catch (IOException ioe) {
            Log.e("URL ERROR", "getWebInfo: ", ioe);
        } finally { //Close the inputStream
            try {
                if (inputStream != null) inputStream.close();
            } catch (IOException ioe) {
                Log.e("inputStream ERROR", "getWebInfo: ", ioe);
            }
        }
        try {
            //Return the JSONArray generated from the string retrieved
            JSONObject jObject = new JSONObject(result);
            return jObject;
        } catch (JSONException je) {
            Log.e("ERROR", "getJsonInfo: ", je);
            je.printStackTrace();
        }
        return null;
    }

}
