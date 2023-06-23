package Implementation;

import Enums.GodType;
import Interfaces.IConstants;
import Interfaces.ISmiteAPI;
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

public class SmiteAPI implements ISmiteAPI
{
    IConstants _constants;
    private static String url = "https://api.smitegame.com/smiteapi.svc";
    private int _sessionId;

    public SmiteAPI()
    {
        _constants = new Constants();
    }

    public int Connect()
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

            System.out.println(response.body());
            return response.statusCode();
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


    @Override
    public ArrayList<String> GetGodNames()
    {
        return null;
    }

    @Override
    public ArrayList<String> GetGodWikiLinks()
    {
        return null;
    }

    @Override
    public ArrayList<String> GetGodImageLinks()
    {
        return null;
    }

    @Override
    public GodType GetGodType(String godName)
    {
        return null;
    }
}
