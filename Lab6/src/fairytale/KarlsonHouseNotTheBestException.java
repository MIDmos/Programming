package fairytale;

public class KarlsonHouseNotTheBestException extends RuntimeException{
    @Override
    public String toString() {
        return "Кому-то показалось, что есть домик лучше, чем у Карлсона.";
    }
}