package uk.co.compendiumdev.challenge;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.routings.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.routings.RoutingStatus;
import uk.co.compendiumdev.thingifier.api.routings.RoutingVerb;
import uk.co.compendiumdev.thingifier.application.ThingifierRestServer;
import uk.co.compendiumdev.thingifier.htmlgui.DefaultGUIHTML;

import java.util.*;

import static spark.Spark.*;


public class ChallengeRouteHandler {
    private final Thingifier thingifier;
    List<RoutingDefinition> routes;
    // todo: create a map of challenges where key is the Challenges guid
    // todo: create a key of 'global' with a Challenges to use as default
    // todo: delete any Challenges which have not been 'accessed' in 15 minutes
    // todo: associate challenges GUID with a session id - let spark manage sessions
    // todo: if session has no challenge guid associated then associate it with "global"

    ChallengeDefinitions challengeDefinitions;
    Challengers challengers;
    private boolean single_player_mode;


    public ChallengeRouteHandler(Thingifier thingifier){
        routes = new ArrayList();
        challengeDefinitions = new ChallengeDefinitions(); //default global challenges
        this.thingifier = thingifier;
        challengers = new Challengers();
        single_player_mode = true;
    }

    public void setToMultiPlayerMode(){
        single_player_mode = false;
        challengers.setMultiPlayerMode();
    }

    public List<RoutingDefinition> getRoutes(){
        return routes;
    }

    public ChallengeRouteHandler configureRoutes() {

        // TODO: create some thingifier helper methods for setting up 405 endpoints with range of verbs
        configureChallengerTrackingRoutes();
        configureChallengesRoutes();
        configureHeartBeatRoutes();
        configureAuthRoutes();

        return this;
    }

    private void configureChallengesRoutes() {
        get("/challenges", (request, result) -> {
            result.status(200);
            result.type("application/json");

            ChallengerAuthData challenger = challengers.getChallenger(request.headers("X-CHALLENGER"));
            result.body(new ChallengesPayload(challengeDefinitions, challenger).getAsJson());
            return "";
        });

        head("/challenges", (request, result) -> {
            result.status(200);
            result.type("application/json");
            return "";
        });

        options("/challenges", (request, result) -> {
            result.status(200);
            result.type("application/json");
            result.header("Allow", "GET, HEAD, OPTIONS");
            return "";
        });

        routes.add(new RoutingDefinition(
                RoutingVerb.GET,
                "/challenges",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Get list of challenges and their completion status"));

        routes.add(new RoutingDefinition(
                RoutingVerb.OPTIONS,
                "/challenges",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Options for list of challenges endpoint"));

        routes.add(new RoutingDefinition(
                RoutingVerb.HEAD,
                "/challenges",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Headers for list of challenges endpoint"));
    }

    private void configureHeartBeatRoutes() {
        get("/heartbeat", (request, result) -> {
            result.status(204);
            return "";
        });

        head("/heartbeat", (request, result) -> {
            result.status(204);
            return "";
        });

        options("/heartbeat", (request, result) -> {
            result.status(204);
            result.header("Allow", "GET, HEAD, OPTIONS");
            return "";
        });

        post("/heartbeat", (request, result) -> {
            result.status(405);
            return "";
        });

        delete("/heartbeat", (request, result) -> {
            result.status(405);
            return "";
        });

        put("/heartbeat", (request, result) -> {
            result.status(405);
            return "";
        });

        patch("/heartbeat", (request, result) -> {
            result.status(500);
            return "";
        });

        trace("/heartbeat", (request, result) -> {
            result.status(501);
            return "";
        });

        routes.add(new RoutingDefinition(
                RoutingVerb.GET,
                "/heartbeat",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Is the server running? YES == 204"));

        routes.add(new RoutingDefinition(
                RoutingVerb.OPTIONS,
                "/heartbeat",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Options for heartbeat endpoint"));

        routes.add(new RoutingDefinition(
                RoutingVerb.HEAD,
                "/heartbeat",
                RoutingStatus.returnedFromCall(),
                null).addDocumentation("Headers for heartbeat endpoint"));
    }

    private void configureChallengerTrackingRoutes() {

        // create a challenger
        post("/challenger", (request, result) -> {

            if(single_player_mode){
                result.header("X-CHALLENGER", challengers.SINGLE_PLAYER.getXChallenger());
                result.status(201);
                return "";
            }

            String xChallengerGuid = request.headers("X-CHALLENGER");
            if(xChallengerGuid == null || xChallengerGuid.trim()==""){
                // create a new challenger
                final ChallengerAuthData challenger = challengers.createNewChallenger();
                result.header("X-CHALLENGER", challenger.getXChallenger());
                result.status(201);
                return "";
            }else {
                ChallengerAuthData challenger = challengers.getChallenger(xChallengerGuid);
                if(challenger==null){
                    // if X-CHALLENGER header exists, and is not a known UUID,
                    // return 410, challenger ID not valid
                    result.header("X-CHALLENGER", "Challenger not found");
                    result.status(422);
                }else{
                    // if X-CHALLENGER header exists, and has a valid UUID, and UUID exists, then return 200
                    result.header("X-CHALLENGER", challenger.getXChallenger());
                    result.status(200);
                }
                // todo: if X-CHALLENGER header exists, and has a valid UUID, and UUID does not exist, then use this to create the challenger, return 201


            }
            result.status(400);
            return "Unknown Challenger State";
        });

        if(!single_player_mode) {
            routes.add(new RoutingDefinition(
                    RoutingVerb.POST,
                    "/challenger",
                    RoutingStatus.returnedFromCall(),
                    null).addDocumentation("Create an X-CHALLENGER guid to allow tracking challenges, use this header in all requests for multi-user tracking"));
        }
    }

    private void configureAuthRoutes() {
        // authentication and authorisation
        // - create a 'secret' note which can be stored against session using an auth token

        // POST /secret/token with basic auth or {username, password} payload to get a secret/token
        post("/secret/token", (request, result) -> {

            BasicAuthHeader basicAuth = new BasicAuthHeader(request.headers("Authorization"));

            // admin/password as default username:password
            if(!basicAuth.matches("admin","password")){
                result.header("WWW-Authenticate","Basic realm=\"User Visible Realm\"");
                result.status(401);
                return "";
            }

            ChallengerAuthData challenger = challengers.getChallenger(request.headers("X-CHALLENGER"));

            if(challenger==null){
                result.status(401);
                result.header("X-CHALLENGER", "Challenger not recognised");
            }

            // if no header X-AUTH-TOKEN then grant one
            result.header("X-AUTH-TOKEN", challenger.getXAuthToken());
            result.status(201);
            return "";
        });

        // GET /secret/token returns the secret token or 401 if not authenticated
        // POST /secret/note GET /secret/note - limit note to 100 chars
        // no auth token will receive a 403
        // auth token which does not match the session will receive a 401
        // header X-AUTH-TOKEN: token given - if token not found (then) 401

        get("/secret/note", (request, result) -> {

            final String authToken = request.headers("X-AUTH-TOKEN");
            if(authToken==null || authToken.length()==0){
                result.status(401);
                return "";
            }

            ChallengerAuthData challenger = challengers.getChallenger(request.headers("X-CHALLENGER"));

            if(challenger==null){
                result.status(401);
                result.header("X-CHALLENGER", "Challenger not recognised");
            }

            if(!authToken.contentEquals(challenger.getXAuthToken())){
                result.status(403); // given token is not allowed to access anything
                return "";
            }

            result.status(200);
            result.header("Content-Type", "application/json");
            final JsonObject note = new JsonObject();
            note.addProperty("note", challenger.getNote());
            return new Gson().toJson(note);
        });

        post("/secret/note", (request, result) -> {

            final String authToken = request.headers("X-AUTH-TOKEN");
            if(authToken==null || authToken.length()==0){
                result.status(401);
                return "";
            }

            ChallengerAuthData challenger = challengers.getChallenger(request.headers("X-CHALLENGER"));

            if(challenger==null){
                result.status(401);
                result.header("X-CHALLENGER", "Challenger not recognised");
            }

            if(!authToken.contentEquals(challenger.getXAuthToken())){
                result.status(403); // given token is not allowed to access anything
                return "";
            }

            try{
                final HashMap body = new Gson().fromJson(request.body(), HashMap.class);
                if(body.containsKey("note")){
                    challenger.setNote((String)body.get("note"));
                }

                result.status(200);
                result.header("Content-Type", "application/json");
                final JsonObject note = new JsonObject();
                note.addProperty("note", challenger.getNote());
                return new Gson().toJson(note);

            }catch(Exception e){
                result.status(400);
                return "";
            }

        });


    }



    public void addHooks(final ThingifierRestServer restServer) {

        restServer.registerPreRequestHook(new ChallengerSparkHTTPRequestHook(challengers));
        restServer.registerPostResponseHook(new ChallengerSparkHTTPResponseHook(challengers));
        restServer.registerHttpApiRequestHook(new ChallengerApiRequestHook(challengers));
        restServer.registerHttpApiResponseHook(new ChallengerApiResponseHook(challengers, thingifier));
    }

    public void setupGui(DefaultGUIHTML guiManagement) {
        guiManagement.addMenuItem("Challenges", "/gui/challenges");

        guiManagement.setHomePageContent("    <h2 id=\"challenges\">Challenges</h2>\n" +
                "    <p>The challenges can be completed by issuing API requests to the API.</p>\n" +
                "    <p>e.g. <code>GET http://localhost:4567/todos</code> would complete the challenge to &quot;GET the list of todos&quot;</p>\n" +
                "    <p>You can also <code>GET http://localhost:4567/challenges</code> to get the list of challenges and their status as an API call. </p>\n"
                );

        get("/", (request, result) -> {
            result.redirect("/gui");
            return "";
        });

        // single user / default session
        get("/gui/challenges", (request, result) -> {
            result.type("text/html");
            result.status(200);

            StringBuilder html = new StringBuilder();
            html.append(guiManagement.getPageStart("Challenges"));
            html.append(guiManagement.getMenuAsHTML());

            // todo explain challenges - single user mode

            List<ChallengeData> reportOn = new ArrayList<>();

            if(single_player_mode){
                reportOn = new ChallengesPayload(challengeDefinitions, challengers.SINGLE_PLAYER).getAsChallenges();
            }else{
                html.append("<p><strong>Unknown Challenger ID</strong></p>");
                reportOn = new ChallengesPayload(challengeDefinitions, challengers.DEFAULT_PLAYER_DATA).getAsChallenges();
            }

            html.append(renderChallengeData(reportOn));

            html.append(guiManagement.getPageFooter());
            html.append(guiManagement.getPageEnd());
            return html.toString();
        });

        // multi user
        get("/gui/challenges/*", (request, result) -> {
            result.type("text/html");
            result.status(200);

            StringBuilder html = new StringBuilder();
            html.append(guiManagement.getPageStart("Challenges"));
            html.append(guiManagement.getMenuAsHTML());

            // todo explain challenges - multi user mode

            List<ChallengeData> reportOn = null;

            String xChallenger = request.splat()[0];
            final ChallengerAuthData challenger = challengers.getChallenger(xChallenger);
            if(challenger==null){
                html.append("<p><strong>Unknown Challenger ID</strong></p>");
                reportOn = new ChallengesPayload(challengeDefinitions, challengers.DEFAULT_PLAYER_DATA).getAsChallenges();
            }else{
                reportOn = new ChallengesPayload(challengeDefinitions, challenger).getAsChallenges();
            }

            html.append(renderChallengeData(reportOn));

            html.append(guiManagement.getPageFooter());
            html.append(guiManagement.getPageEnd());
            return html.toString();
        });
    }


    private String renderChallengeData(final List<ChallengeData> reportOn) {
        StringBuilder html = new StringBuilder();

        html.append("<table>");
        html.append("<thead>");
        html.append("<tr>");

        html.append("<th>Challenge</th>");
        html.append("<th>Done</th>");
        html.append("<th>Description</th>");
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");

        for(ChallengeData challenge : reportOn){
            html.append("<tr>");
            html.append(String.format("<td>%s</td>", challenge.name));
            html.append(String.format("<td>%b</td>", challenge.status));
            html.append(String.format("<td>%s</td>", challenge.description));
            html.append("</tr>");
        }

        html.append("</tbody>");
        html.append("</table>");

        return html.toString();
    }
}
