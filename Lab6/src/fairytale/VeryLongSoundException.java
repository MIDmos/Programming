package fairytale;

public class VeryLongSoundException extends Exception {
    public static final int CRITICAL_LENGTH=75;
    @Override
    public String toString() {
        return "Звук слишком длинный.";
    }
}
