package uk.co.compendiumdev.challenger.http.completechallenges;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengeMain;
import uk.co.compendiumdev.challenger.http.httpclient.HttpResponseDetails;

import java.util.HashMap;
import java.util.Map;

public class CompleteAllChallengesMultiUserTest extends ChallengeCompleteTest{

    @Override
    public boolean getIsSinglePlayerMode(){
        return false;
    }

    @Override
    public int getNumberOfChallengesToFail(){
        // all challenges work in multi-user mode
        return 0;
    }

    // these tests below will only work in multi user mode

    @Test
    public void restoreChallengeWithPOST() {

        // ensure challenger status file exists
        ChallengeMain.getChallenger().getChallengers().persistChallengerState(challenger);
        // but is not in memory
        ChallengeMain.getChallenger().getChallengers().delete(challenger.getXChallenger());

        // remember x-challenger guid
        String guid = challenger.getXChallenger();

        Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        headers.putAll(x_challenger_header);
        headers.put("Content-Type", "application/json");

        final HttpResponseDetails response =
                http.send("/challenger",
                        "POST", headers,
                        "");

        Assertions.assertEquals(200, response.statusCode);
        challenger = ChallengeMain.getChallenger().getChallengers().getChallenger(guid);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.POST_RESTORE_EXISTING_CHALLENGER));
    }

    @Test
    public void restoreChallengeWithGET() {

        // ensure challenger status file exists
        ChallengeMain.getChallenger().getChallengers().persistChallengerState(challenger);
        // but is not in memory
        ChallengeMain.getChallenger().getChallengers().delete(challenger.getXChallenger());

        // remember x-challenger guid
        String guid = challenger.getXChallenger();

        //Map<String, String> x_challenger_header = getXChallengerHeader(challenger.getXChallenger());

        Map<String, String> headers = new HashMap<>();
        //headers.putAll(x_challenger_header);
        headers.put("Content-Type", "application/json");

        final HttpResponseDetails response =
                http.send("/challenger/" + guid,
                        "GET", headers,
                        "");

        Assertions.assertEquals(204, response.statusCode);
        challenger = ChallengeMain.getChallenger().getChallengers().getChallenger(guid);
        Assertions.assertTrue(challenger.statusOfChallenge(CHALLENGE.GET_RESTORE_EXISTING_CHALLENGER));
    }

}