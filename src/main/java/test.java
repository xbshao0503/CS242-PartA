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
        String input = "C:\\Users\\Xinbo Shao\\Desktop\\lucenecore\\src\\main\\resources\\data1.json";
        JSONParser parser = new JSONParser();

        try {

            Object obj = parser.parse(new FileReader(input));

            JSONObject jsonObject = (JSONObject) obj;
            System.out.println(jsonObject);

            String text = (String) jsonObject.get("text");
            System.out.println("show text:");
            System.out.println(text);

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

