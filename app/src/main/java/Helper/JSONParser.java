package Helper;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Parses the JSON file to create the Questions and return them back in an ArrayList
 */
public class JSONParser {

    //Empty constructor
    public JSONParser() {

    }

    /*
    Goes through the JSONObject and returns relevant data
     */
    public String parseJSONfile(JSONObject jsonObject) throws JSONException {
        String gender = null;
        JSONArray faceDetails = jsonObject.getJSONArray("face");
        JSONObject faceInfo = faceDetails.getJSONObject(0);
        JSONObject faceAttribute = faceInfo.getJSONObject("attribute");
        JSONObject faceGender = faceAttribute.getJSONObject("gender");
        String faceGenderValue = faceGender.getString("value");
        return faceGenderValue;
    }

}
