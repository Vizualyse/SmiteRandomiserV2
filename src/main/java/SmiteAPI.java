import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.MessageFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class SmiteAPI {

    private static String url = "https://api.smitegame.com/smiteapi.svc";

    private SmiteWebRipAPI _webRipAPI;
    private int _sessionId;

    public static void main(String[] args)
    {
        SmiteAPI api = new SmiteAPI();
        api.Connect();
    }

    public SmiteAPI()
    {
        int statusCode = Connect();
        if (statusCode == 0 || statusCode == 200)
        {
            _webRipAPI = new SmiteWebRipAPI();
        }
        else
        {

        }
    }

    private int Connect()
    {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.from(ZoneOffset.UTC));
        String datetime = dtf.format(Instant.now());
        String signature = DigestUtils.md5Hex("3644" + "createsession" + "BA257853E476477CB0C45F006D14BCE2" + datetime);

        try
        {
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(MessageFormat.format("{0}/createSessionJson/3644/{1}/{2}", url, signature, datetime)))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode();
            //System.out.println(response.body());
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
        catch (InterruptedException e)
        {
            System.out.println(e);
        }
        return 0;
    }

    public ArrayList<String> GetGodNames()
    {
        if (_webRipAPI != null)
        {
            return _webRipAPI.GetGodNames();
        }
        else
        {
            return new ArrayList<>();
        }
    }

    public ArrayList<String> GetGodWikiLinks()
    {
        if (_webRipAPI != null)
        {
            return _webRipAPI.GetGodWikiLinks();
        }
        else
        {
            return new ArrayList<>();
        }
    }

    public ArrayList<String> GetGodImageLinks()
    {
        if (_webRipAPI != null)
        {
            return _webRipAPI.GetGodImageLinks();
        }
        else
        {
            return new ArrayList<>();
        }
    }
}
