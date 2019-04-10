package fairytale;
//Создан для работы с SoundChanger
public interface SoundListener {
    String listen(String sound, Iterable<? extends SoundChanger> soundChanger);//Слушать
    void playSound();//Make sound
}