package server;

import fairytale.Noise;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public class Response implements Serializable {
    private String doings;
    private LinkedHashSet<Noise> noises;

    public Response(String doings,LinkedHashSet<Noise> noises){
        this.doings=doings;
        setNoises(noises);
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

    public void setNoises(LinkedHashSet<Noise> noises) {
        this.noises=noises.stream()
                .sorted(Comparator.comparingInt(Noise::getSize))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
