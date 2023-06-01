package org.example;

import com.google.gson.Gson;
import jooq.tables.records.HeroesRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import static jooq.tables.Heroes.HEROES;

public class HeroesRepositoryImpl implements HeroesRepository {
    private Logger logger = LoggerFactory.getLogger(HeroesRepositoryImpl.class);
    private static String username = "postgres";
    private static String pass = "simple123";
    private static String url = "jdbc:postgresql://localhost:5432/";

    public List<Hero> fetchHeroes() {
        final List<Hero> heroList = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, pass)) {
            DSLContext dslContext = DSL.using(connection, SQLDialect.POSTGRES);
            Result<Record> result = dslContext.select().from(HEROES).fetch();

            for (Record record : result) {
                Integer id = record.getValue(HEROES.HERO_ID);
                String name = record.getValue(HEROES.HERO_NAME);

                heroList.add(new Hero(id, name));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return heroList;
    }

    public Hero fetchHero(String id) {
        HeroesRecord result = new HeroesRecord();
        try (Connection connection = DriverManager.getConnection(url, username, pass)) {
            DSLContext dslContext = DSL.using(connection, SQLDialect.POSTGRES);
            result = dslContext.fetchOne(HEROES, HEROES.HERO_ID.eq(Integer.valueOf(id)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Hero(result.getHeroId(), result.getHeroName());
    }

    public void createHero(Hero hero) {
        try (Connection connection = DriverManager.getConnection(url, username, pass)) {
            DSLContext dslContext = DSL.using(connection, SQLDialect.POSTGRES);
            HeroesRecord newHero = dslContext.newRecord(HEROES);
            newHero.setHeroId(hero.id());
            newHero.setHeroName(hero.name());
            newHero.store();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateHero(Request req) {
        try (Connection connection = DriverManager.getConnection(url, username, pass)) {
            DSLContext dslContext = DSL.using(connection, SQLDialect.POSTGRES);
            HeroesRecord record = dslContext.fetchOne(HEROES, HEROES.HERO_ID.eq(Integer.valueOf(req.params(":id"))));
            record.setHeroId(Integer.valueOf(req.params(":id")));
            record.setHeroName(new Gson().fromJson(req.body(), Hero.class).name());
            record.store();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteHero(String id) {
        HeroesRecord result = new HeroesRecord();
        try (Connection connection = DriverManager.getConnection(url, username, pass)) {
            DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);
            result = create.fetchOne(HEROES, HEROES.HERO_ID.eq(Integer.valueOf(id)));
            result.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
