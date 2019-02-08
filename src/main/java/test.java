import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
public class test {
    public static void main(String[] args){
        String input = "C:\\Users\\Xinbo Shao\\Desktop\\lucenecore\\src\\main\\resources\\data2.json";
        JSONParser parser = new JSONParser();

        try {

            Object obj = parser.parse(new FileReader(input));
            JSONArray jsonArray = (JSONArray) obj;
//            int i = jsonArray.size();
//            System.out.println(i);
//            System.out.println(jsonArray.get(0));
            for (int i=0;i<jsonArray.size();i++){
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
//                System.out.println(jsonObject);
                String text = (String) jsonObject.get("text");
                System.out.println("show text:");
                System.out.println(text);
//                JSONObject user_info = (JSONObject) jsonObject.get("user");
//                String info = (String) user_info.get("utc_offset");
//                System.out.println(info);
//                JSONObject entities = (JSONObject) jsonObject.get("entities");
//                JSONArray hashtags = (JSONArray) entities.get("hashtags");
//                String hash_text = null;
//                if (hashtags.size() != 0){
//                    JSONObject hash_info = (JSONObject) hashtags.get(0);
//                    hash_text = (String) hash_info.get("text") ;
//                }
//                System.out.println(hash_text);
//                JSONObject entities = (JSONObject) jsonObject.get("entities");
//                JSONArray hashtags = (JSONArray) entities.get("urls");
//                JSONObject hash_info = (JSONObject) hashtags.get(0);
//                String url = (String) hash_info.get("url");

                JSONObject coordinates = (JSONObject) jsonObject.get("coordinates");
                JSONArray geolocation = (JSONArray) coordinates.get("coordinates");
                String geo_location = (String) (geolocation.get(0) + "," + geolocation.get(1));

                System.out.println(geo_location);







            }
//            JSONObject jsonObject = (JSONObject) jsonArray.get(0);
//            System.out.println(jsonObject);
//            String text = (String) jsonObject.get("text");
//            System.out.println("show text:");
//            System.out.println(text);



//            JSONObject jsonObject = (JSONObject) obj;
//
//            System.out.println(jsonObject);
//
//            String text = (String) jsonObject.get("text");
//            System.out.println("show text:");
//            System.out.println(text);

//            long age = (Long) jsonObject.get("age");
//            System.out.println(age);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }





    }

