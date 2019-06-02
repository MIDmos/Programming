package server;

public class QueryStorage {
    public static final String FIND_USER_BY_LOGIN = "SELECT * FROM users WHERE login = ?";
    public static final String DELETE_NOISE_BY_NAME = "DELETE FROM noises WHERE owner_id = ? AND name = ?";
    public static final String SELECT_NOISES = "SELECT * FROM noises";
    public static final String CHECK_PASSWORD = "SELECT * FROM users WHERE login = ? AND password = ?";
    public static final String ADD_USER= "INSERT INTO users (email, login, password) VALUES (?, ?, ?)";
    public static final String ADD_NOISE= "INSERT INTO noises VALUES (?, ?, ?, ?, ?)"; //owner_id, name, sound, distance, creation_time
}
