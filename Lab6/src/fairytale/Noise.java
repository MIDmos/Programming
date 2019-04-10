package fairytale;

import java.io.Serializable;
import java.util.Random;

public class Noise implements SoundChanger,Comparable<Noise>, Serializable {//Можно сравнить звук с другими

    private static Random random=new Random();//Для случайного выбора правила
    private  boolean wasActive;//Флаг активности
    private String sound;//звук-основа для правила
    private final String NAME;//Sound name
    private final int DISTANCE;//Sound name
    private ChangingRule rule;//Парвило изменения
    private int size;
    private final long TIME_STAMP;

    public Noise(String name, String sound){
        NAME=name;
        this.sound=sound;
        createRuleForSound();
        wasActive=false;
        size=(NAME+sound).length();
        DISTANCE=random.nextInt(101)-50;
        TIME_STAMP=System.currentTimeMillis();
    }

    public static String toJson(Noise noise){//Записывает объект в json
        if(noise==null)
            return "\"null\":\"null\"";
        return '"'+noise.getNAME().replaceAll("\"","\\\\\"")+"\": \""+ noise.getSound().replaceAll("\"","\\\\\"")+'"';
    }
    public static Noise fromJson(String arg1,String arg2){//Возвращает объект, описанный в json
        if(arg1.equals("null")&&arg2.equals("null"))
            return null;
        else return new Noise(arg1,arg2);
    }

    @Override
    public int compareTo(Noise anotherNoise) {//Сравнение по алфавиту
        int i =NAME.compareTo(anotherNoise.NAME);
        if(i==0)
            i=sound.compareTo(anotherNoise.sound);
        return i;
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
        return NAME+" со звуком \""+ sound +'"';
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
    public long getTIME_STAMP() {
        return TIME_STAMP;
    }

    @Override
    public boolean equals(Object obj) {//Объекты равны, если имеют одинаковые имена
        if (this == obj) return true;
        if (!(obj instanceof Noise)) return false;
        Noise other = (Noise) obj;
        return NAME.equals(other.NAME);
    }

    @Override
    public int hashCode() {//Хэш-функция зависит только от имени
        return NAME.hashCode();
    }
}
