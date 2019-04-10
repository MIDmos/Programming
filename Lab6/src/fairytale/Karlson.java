package fairytale;

import java.util.Arrays;
import java.util.Random;

//Plays one sound from array
public class Karlson extends PersonWithSound implements SoundCreator {

    private String[] phrases;

    public Karlson(String name,int pictureCount,int comfort) {
        super(name,pictureCount,comfort);
        setCurrentSound("");
        setPhrases(new String[]
                {"Привет, Малыш!", "Я приведение с мотором!",
                "В каком ухе у меня жужжит?", "В самом расцвете сил."});
    }

    @Override
    public void playSound() {
        System.out.printf("%s сказал: %s\n",getName(),getCurrentSound());
    }

    @Override
    public String createSound() {
        Random random = new Random();
        setCurrentSound(phrases[random.nextInt(phrases.length)]);
        return getCurrentSound();
    }
    @Override
    public int hashCode() {
        return super.hashCode()+ Arrays.hashCode(phrases);
    }
    @Override
    public boolean equals(Object obj) {
        if(!(super.equals(obj)&&(obj instanceof Karlson)))return false;
            Karlson other = (Karlson)obj;
            return phrases.equals(other.getPhrases());
    }
    public String[] getPhrases() {
        return phrases;
    }
    public void setPhrases(String[] phrases) {
        this.phrases = phrases;
    }
}
