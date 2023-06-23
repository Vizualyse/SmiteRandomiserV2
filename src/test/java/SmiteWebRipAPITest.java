import Implementation.SmiteWebRipAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class SmiteWebRipAPITest
{
    SmiteWebRipAPI smiteWebRipAPI;

    @BeforeEach
    void setUp()
    {
        smiteWebRipAPI = new SmiteWebRipAPI();
    }

    @AfterEach
    void tearDown()
    {
    }

    @Test
    void main()
    {
    }

    @Test
    void getGodNames()
    {
        ArrayList<String> names = smiteWebRipAPI.GetGodNames();

        assert(names.size() > 0);
        assert(names.stream().allMatch(x -> x.length() > 0));
        assert(names.stream().allMatch(x -> x.length() < 32));
    }

    @Test
    void getGodWikiLinks()
    {
        ArrayList<String> text = smiteWebRipAPI.GetGodWikiLinks();
        ArrayList<String> godNames = smiteWebRipAPI.GetGodNames();

        assert(text.size() > 0);
        assert(text.stream().allMatch(x -> x.length() > 0));
        assert(text.stream().allMatch(x -> x.length() < 100));
        assert(text.size() == godNames.size());
    }

    @Test
    void getGodImageLinks()
    {
        ArrayList<String> text = smiteWebRipAPI.GetGodImageLinks();
        ArrayList<String> godNames = smiteWebRipAPI.GetGodNames();

        assert(text.size() > 0);
        assert(text.stream().allMatch(x -> x.length() > 0));
        assert(text.stream().allMatch(x -> x.length() < 200));
        assert(text.size() == godNames.size());
    }
}