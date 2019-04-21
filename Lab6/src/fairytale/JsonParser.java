package fairytale;

import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;


public class JsonParser {
    
    private Set<Noise> set;
    private Scanner scanner;
    private String name,sound,expectedToken;
    private boolean nameIsDone;
    private JsonParser.Tokenizer tokenizer;
    
    public JsonParser(Scanner scanner,Set<Noise> noises){
        this.scanner=scanner;
        set=noises;
        name="";
        sound="";
        expectedToken="";
        nameIsDone=false;
        tokenizer=new JsonParser.Tokenizer();
    }

    private class WrongTokenException extends RuntimeException{
        @Override
        public String toString() {
            return "Ошибка перевода с json";
        }
    }

    private class Tokenizer {
        private String[] array;
        private int index;
        private String nextToken(){
            index++;
            if(index<array.length) {
                return array[index];
            }else return "";
        }
        private void putString(String string){
            index=0;
            array=string.split("\"");
            if(!array[0].equals(""))
                index--;
        }
        private boolean hasMoreTokens(){
            return index<array.length-1;
        }
    }

    /**
     * @throws WrongTokenException когда перевод с json не удается
     */
    public void parse() {
        boolean hasNext = false;
        boolean added=false;
        boolean exception=false;
        String currentLine;
        while (scanner.hasNextLine()) {//Пока есть строки
            currentLine = scanner.nextLine().trim();//Читаем новую строку
            if (!hasNext && currentLine.trim().startsWith("{")) {//Находим начало файла
                currentLine = currentLine.substring(1);
                hasNext = true;
            }
            if (hasNext) {
                if (currentLine.trim().endsWith("}")) {//Если файл кончается на этой строке
                    currentLine = currentLine.substring(0, currentLine.length() - 1);
                    hasNext = false;//Устанавливаем флаг
                }
                try {
                    tokenizer.putString(currentLine);//Разбиваем строку по "
                    while (tokenizer.hasMoreTokens()) {
                        String token = tokenizer.nextToken();//Берем токен
                        if(!expectedToken.equals("")) {
                            token = token.trim();
                            if(expectedToken.equals(token)){
                                expectedToken = "";
                                if(tokenizer.hasMoreTokens()) {
                                    token= tokenizer.nextToken();
                                } else break;
                            }else throw new WrongTokenException();
                        }
                        //Ожидаемый токен отбрасываем
                        if (!nameIsDone) {//Читаем имя
                            name = token;
                            while (name.endsWith("\\")) {
                                name = name.substring(0, name.length() - 1) + '"';
                                name += tokenizer.nextToken();
                            }
                            nameIsDone = true;
                            expectedToken=":";//Ожидаем :
                        } else {
                            sound = token;
                            if (sound.equals("")) throw new WrongTokenException();//Запрет на шумы без звука
                            while (sound.endsWith("\\")) {
                                sound = sound.substring(0, sound.length() - 1) + '"';
                                sound += tokenizer.nextToken();
                            }//Читаем звук
                            set.add(Noise.fromJson(name, sound));
                            added=true;
                            nameIsDone = false;
                            sound = "";
                            expectedToken=",";//Ожидаем ,
                        }
                    }
                    if (!hasNext) {
                        if (nameIsDone && !sound.equals("")) set.add(Noise.fromJson(name, sound));
                    }
                } catch (NoSuchElementException | WrongTokenException e) {
                    exception=true;
                    System.out.println("Ошибка в файле. Рекомендуется проверка.");
                }
            }
        }
        if(!(added||exception)) System.out.println("Ошибка. Пожалуйста, проверьте соответсвие формату json");
    }
}
