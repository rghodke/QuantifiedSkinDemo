package helper;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Parses the JSON file to create the Questions and return them back in an ArrayList
 */
public class JSONParser {

    //Empty constructor
    public JSONParser() {

    }

    /*
    Goes through the JSONObject and returns relevant data
    @param JSONObject   The JSONObject that is to be parsed for data
     */
    public String parseJSONfile(JSONObject jsonObject) throws JSONException {
        String faceGenderValue = null;
        JSONArray faceDetails = jsonObject.getJSONArray("face");
        if (faceDetails.length() == 0 || faceDetails.length() > 1) {
            faceGenderValue = "NoFace";
            return faceGenderValue;
        }
        JSONObject faceInfo = faceDetails.getJSONObject(0);
        JSONObject faceAttribute = faceInfo.getJSONObject("attribute");
        JSONObject faceGender = faceAttribute.getJSONObject("gender");
        faceGenderValue = faceGender.getString("value");
        return faceGenderValue;
    }

    /*
    Goes through the JSONObject and returns relevant data
    @param JSONObject   The JSONObject that is to be parsed for data
    @return String[]    All of the quotes
     */
    public String[] parseJSONQuotes(JSONObject jsonObject) throws JSONException {

        JSONArray quotesArray = jsonObject.getJSONArray("self-love");
        String[] quotes = new String[quotesArray.length()];
        for (int i = 0; i < quotesArray.length(); i++) {
            quotes[i] = quotesArray.getString(i);
        }
        return quotes;
    }


}
