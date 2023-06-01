package org.example;

import spark.Request;

import java.util.List;

public interface HeroesRepository {

    List<Hero> fetchHeroes();

    Hero fetchHero(String id);

    void createHero(Hero hero);

    void updateHero(Request req);

    void deleteHero(String id);

}
