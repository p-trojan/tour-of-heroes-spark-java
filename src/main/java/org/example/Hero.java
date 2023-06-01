package org.example;

public record Hero(int id, String name) {
    public Hero() {
        this(-1, "unknown");
    }
}

