package server;

import fairytale.Noise;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;


public class Response implements Serializable {

    //shows what happened
    public enum State implements Serializable{
        CONNECTED,
        UNKNOWN,
        LOGINED;
    }

    private String doings;
    private long id;
    private String token;
    private State state;
    private LinkedHashSet<Noise> noises;

    public Response(String token, String doings, Set<Noise> noises,State state,long id){
        this.doings=doings;
        setNoises(noises);
        this.token=token;
        this.state=state;
        this.id=id;
    }

    public String getDoings() {
        return doings;
    }

    public void setDoings(String doings) {
        this.doings = doings;
    }

    public LinkedHashSet<Noise> getNoises() {
        return noises;
    }

    /**
     * Задает коллекцию шумов для передачи. Сортирует ее по размеру объектов
     * @param noises Коллекция для передачи
     */
    public void setNoises(Set<Noise> noises) {
        this.noises=Collections.synchronizedSet(noises).stream()
                .sorted(Comparator.comparingInt(Noise::getSize))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
