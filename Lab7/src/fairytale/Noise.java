package fairytale;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.NavigableMap;
import java.util.Random;

public class Noise implements SoundChanger,Comparable<Noise>, Serializable {//Можно сравнить звук с другими

    private static Random random=new Random();//Для случайного выбора правила
    private  boolean wasActive;//Флаг активности
    private String sound;//звук-основа для правила
    private final String NAME;//Sound name
    private final int DISTANCE;//Положение в пространстве
    private ChangingRule rule;//Парвило изменения
    private int size;//Размер
    private long ownerId;
    private final LocalDateTime CREATION_TIME;//Время создания

    public Noise(long ownerId, String name, String sound){
        NAME=name;
        this.sound=sound;
        this.ownerId =ownerId;
        createRuleForSound();
        wasActive=false;
        size=(NAME+sound).length();
        DISTANCE=random.nextInt(101)-50;
        CREATION_TIME =LocalDateTime.now();
    }

    public Noise(ResultSet resultSet)throws SQLException{
        ownerId=resultSet.getInt("owner_id");
        DISTANCE=resultSet.getInt("distance");
        NAME=resultSet.getString("name");
        sound=resultSet.getString("sound");
        CREATION_TIME=resultSet.getTimestamp("creation_time").toLocalDateTime();
    }


    public static LinkedHashSet<Noise> fromJson(String json){
        LinkedHashSet<Noise> set = new LinkedHashSet<>();

        try{
            System.out.println(json);
            JSONParser parser = new JSONParser();

            System.out.println(1);
            JSONObject collection = (JSONObject) parser.parse(json.trim());

            JSONArray noises = (JSONArray) collection.get("noises");

            if(noises!=null) {
                noises.forEach(object -> {
                    JSONObject jsonObject = (JSONObject) object;

                    String name = (String) jsonObject.get("name");
                    String sound = (String) jsonObject.get("sound");

                    set.add((new Noise(0, name, sound)));
                });
            }else{
                String name = (String) collection.get("name");
                String sound = (String) collection.get("sound");
                set.add(new Noise(0,name,sound));
            }
        }catch (ParseException e){
            System.out.println("Произошла ошибка при парсинге");
            e.printStackTrace();
        }

        return set;
    }
    public static LinkedHashSet<Noise> fromJson(long id,String json){
        LinkedHashSet<Noise> noises = fromJson(json);
        noises.forEach(n->n.setOwnerId(id));
        return noises;
    }

    @Override
    public int compareTo(Noise anotherNoise) {//Сравнение по алфавиту (проверяем только имена)
        return NAME.compareTo(anotherNoise.NAME);
    }

    @Override
    public void createRuleForSound() {//Создает правило для звука
        rule= ChangingRule.values()[random.nextInt(ChangingRule.values().length)];
    }

    @Override
    public String changeSound(String soundToChange) {//Changes sound with rule
        wasActive=true;
        switch (rule) {
            case COVER:
                String newSound = "";
                    for (char c : soundToChange.toCharArray()) {
                        if (Math.random() <= 0.15) newSound = newSound + c + sound;
                        else newSound = newSound + c;
                    }
                return newSound;
            case START_END:
                return sound +soundToChange+ sound;
            case MIDDLE:
                return soundToChange.substring(0, soundToChange.length() / 2) + sound +
                        soundToChange.substring(soundToChange.length() / 2);
            case START:
                return sound +soundToChange;
            case END:
                return soundToChange+ sound;
            default:
                return sound;
        }
    }
    public boolean wasActive() {//Возврашает true, если ранее был выполнен метод changeSound(String soundIn)
        return wasActive;
    }
    public void setWasActive(boolean wasActive) {
        this.wasActive = wasActive;
    }
    public String getSound() {
        return sound;
    }
    public void setSound(String sound) {
        this.sound = sound;
        size=(NAME+sound).length();
    }

    @Override
    public String toString() {
        return "Noise{" +
                " ownerId=" + ownerId +
                ", NAME='" + NAME + '\'' +
                ", sound='" + sound + '\'' +
                ", DISTANCE=" + DISTANCE +
                ", CREATION_TIME=" + CREATION_TIME +
                '}';
    }

    public static Random getRandom() {
        return random;
    }

    public static void setRandom(Random random) {
        Noise.random = random;
    }
    public String getNAME() {
        return NAME;
    }

    public ChangingRule getRule() {
        return rule;
    }
    public void setRule(ChangingRule rule) {
        this.rule = rule;
    }
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
    public int getDISTANCE() {
        return DISTANCE;
    }
    public LocalDateTime getCREATION_TIME() {
        return CREATION_TIME;
    }
    public long getOwnerId() {
        return ownerId;
    }

    @Override
    public boolean equals(Object obj) {//Объекты равны, если имеют одинаковые имена
        if (this == obj) return true;
        if (!(obj instanceof Noise)) return false;
        Noise other = (Noise) obj;
        return NAME.equals(other.NAME);
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public int hashCode() {//Хэш-функция зависит только от имени
        return NAME.hashCode();
    }
}
