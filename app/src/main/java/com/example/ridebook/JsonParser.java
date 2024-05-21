package com.example.ridebook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
public class JsonParser {
    private HashMap<String,String> parseJsonObject(JSONObject object) {
        HashMap<String, String> datalist = new HashMap<>();
        try {
            String name = object.getString("name");
            String latiude = object.getJSONObject("geometry")
                    .getJSONObject("location").getString("lat");
            String longitude = object.getJSONObject("geometry")
                    .getJSONObject("location").getString("lng");

            datalist.put("name", name);
            datalist.put("lat", latiude);
            datalist.put("long", longitude);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return datalist;

    }

    private List<HashMap<String,String>> parseJsonArray(JSONArray jsonArray){
        List<HashMap<String,String>> datalist = new ArrayList<>();
        for (int i=0; i<jsonArray.length(); i++){
            try {
                HashMap<String,String> data = parseJsonObject((JSONObject) jsonArray.get(i));
                datalist.add(data);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return  datalist;
    }
    public List<HashMap<String,String>> parseResult(JSONObject object){
        JSONArray jsonArray = null;

        try {
            jsonArray =object.getJSONArray("results");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return parseJsonArray(jsonArray);
    }
}
