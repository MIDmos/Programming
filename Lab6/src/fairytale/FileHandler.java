package fairytale;

import java.io.*;
import java.util.LinkedHashSet;
import java.util.Scanner;

public class FileHandler {

    public static LinkedHashSet<Noise> readFile(String fileName){//Чтение из файла
        LinkedHashSet<Noise> set = new LinkedHashSet<>();
        try( Scanner scanner = new Scanner(new File(fileName))){
            JsonParser parser=new JsonParser(scanner,set);
            parser.parse();
            System.out.println("Файл "+fileName+ " прочитан");
        } catch (FileNotFoundException e) {
            System.out.println("Файл не найден");
        }
        return set;
    }

    public static void writeFile(LinkedHashSet<Noise> noises) {

        try (FileOutputStream outputStream = new FileOutputStream(new File("save.json"));){
            outputStream.write(("{" + '\n').getBytes());
            int k = 0;
            for (Noise noise : noises) {
                if (++k < noises.size()) outputStream.write(("   " + Noise.toJson(noise) + ",\n").getBytes());
                else outputStream.write(("   " + Noise.toJson(noise) + "\n").getBytes());
            }
            outputStream.write("}".getBytes());
            System.out.println("Коллекция сохранена");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
