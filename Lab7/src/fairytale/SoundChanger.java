package fairytale;
//Специализирован на изменении звука
public interface SoundChanger {
    void createRuleForSound();// Задать правило изменения звука
    String changeSound(String soundToChange);//Change incoming sound
}