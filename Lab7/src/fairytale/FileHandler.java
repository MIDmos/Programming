package fairytale;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.LinkedHashSet;
import java.util.Set;

public class FileHandler {

    public static LinkedHashSet<Noise> readFile(String fileName){//Чтение из файла
        LinkedHashSet<Noise> set = new LinkedHashSet<>();
        try(FileReader reader = new FileReader(new File(fileName))){
            JSONParser parser = new JSONParser();

            JSONObject collection = (JSONObject) parser.parse(reader);

            JSONArray noises = (JSONArray) collection.get("noises");

            noises.forEach(object ->{

                JSONObject jsonObject = (JSONObject)object;

                long id = (long) jsonObject.get("owner_id");
                String name = (String) jsonObject.get("name");
                String sound = (String) jsonObject.get("sound");

                set.add((new Noise(id,name,sound)));
            });

            System.out.println("Файл "+fileName+ " прочитан");
        } catch (FileNotFoundException e) {
            System.out.println("Файл не найден");
        }catch (IOException e){
            e.printStackTrace();
        }catch (ParseException e){
            System.out.println("Произошла ошибка при парсинге");
        }

        return set;
    }

    public static void writeFile(Set<Noise> set) {

        try (FileOutputStream outputStream = new FileOutputStream(new File("save.json"));){
            outputStream.write(("{" + '\n').getBytes());

            JSONObject collection = new JSONObject();
            JSONArray noises = new JSONArray();

            set.forEach(noise ->{
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("owner_id",noise.getOwnerId());
                jsonObject.put("name",noise.getNAME());
                jsonObject.put("sound",noise.getSound());

                noises.add(jsonObject);
            });
            collection.put("noises",noises);

            try (FileWriter writer = new FileWriter(new File("save.json"))){
                writer.write(collection.toJSONString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Коллекция сохранена");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
