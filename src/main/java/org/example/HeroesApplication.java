package org.example;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;

import javax.servlet.http.HttpServletResponse;

import java.io.UnsupportedEncodingException;

import static spark.Spark.*;

public class HeroesApplication {

    private Logger log = LoggerFactory.getLogger(HeroesApplication.class);
    private static final HeroesRepository INSTANCE = new HeroesRepositoryImpl();

    private final HeroesRepository repository;

    public HeroesApplication(HeroesRepository repository) {
        this.repository = repository;
    }

    public HeroesApplication() {
        this(INSTANCE);
    }

    // rest api
    public void serveApi() {

        final String ALLOWED_ORIGINS = "http://localhost:4200";
        final String ALLOWED_METHODS = "GET, POST, PUT, DELETE, HEAD, OPTIONS";
        final String EXPOSED_HEADERS = "X-Correlation-ID";

        port(8080);
        enableCORS(ALLOWED_ORIGINS, ALLOWED_METHODS, EXPOSED_HEADERS);

        path("/api", () -> {
            before("/*", (q, a) -> {
                log.info("API call");
//                if(q.pathInfo().equals("/api/heroes/:id")) {
//                    a.redirect("/api/heroes");
//                }
            });
            get("/heroes", ((request, response) -> new Gson().toJson(repository.fetchHeroes())));
            post("/heroes", "application/json", (request, response) -> {
                repository.createHero(new Gson().fromJson(request.body(), Hero.class));
//                log.info(requestInfoToString(request));
//                response.body("{}");
//                response.status(201);
                return "{}";
            });
            get("/heroes/:id", ((request, response) -> new Gson().toJson(repository.fetchHero(request.params(":id")))));

            put("/heroes/:id","application/json" , ((request, response) -> {
//                        repository.updateHero(request.params(":id"), new Gson().fromJson(request.body(), Hero.class));
                repository.updateHero(request);
                response.redirect("/api/heroes");
//                response.body();
//                response.status(200);
                return "{}";
            }));
            after((q, a) -> log.info(requestInfoToString(q)));
            delete("/heroes/:id", ((request, response) -> {
                repository.deleteHero(request.params(":id"));
                response.body("{}");
                response.status(204);
                return response;
            }));
        });

//        path("/api", () -> {
//            put("/heroes/:heroId","application/json" , ((request, response) -> {
////                        repository.updateHero(request.params(":id"), new Gson().fromJson(request.body(), Hero.class));
//                repository.updateHero(request);
//                response.body();
//                response.status(200);
//                return "{}";
//            }));
//        });
//        after((request, response) -> log.info(requestAndResponseInfoToString(request, response)));
    }

    // Enables CORS on requests. This method is an initialization method and should be called once.
    private static void enableCORS(final String origin, final String methods, final String headers) {
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Request-Method", methods);
            response.header("Access-Control-Allow-Headers", headers);
            // Note: this may or may not be necessary in your particular application
            response.type("application/json");
        });
    }

    private static String requestInfoToString(Request request) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.requestMethod());
        sb.append(" " + request.url());
        sb.append(" " + request.body());
        return sb.toString();
    }

    private static String requestAndResponseInfoToString(Request request, Response response) {

        StringBuilder sb = new StringBuilder();
        sb.append(request.requestMethod());
        sb.append(" " + request.url());
        sb.append(" " + request.body());
        HttpServletResponse raw = response.raw();
        sb.append(" Response: " + raw.getStatus());
        sb.append(" | Content-type: " + raw.getContentType());
        sb.append(" | Response body: " + response.body());
        try {
            sb.append(" body size in bytes: " + response.body().getBytes(raw.getCharacterEncoding()).length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    class JsonTransformer implements ResponseTransformer {
        private Gson gson = new Gson();

        @Override
        public String render(Object model) throws Exception {
            return gson.toJson(model);
        }
    }


}
