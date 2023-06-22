import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SmiteWebRipAPI
{
    private Document _doc;
    private Elements _godTable = new Elements();

    private ArrayList<String> _godNames = new ArrayList();
    private ArrayList<String> _godWikiLinks = new ArrayList();
    private ArrayList<String> _godImageLinks = new ArrayList();

    public static void main(String[] args)
    {
        SmiteWebRipAPI smiteWebRipAPI = new SmiteWebRipAPI();
        smiteWebRipAPI.GetGodNames();
        smiteWebRipAPI.GetGodWikiLinks();
        smiteWebRipAPI.GetGodImageLinks();
    }

    public SmiteWebRipAPI()
    {
        try
        {
            Files.createDirectories(Paths.get(Constants.ResourcesFolder));

            _doc = Jsoup.connect("https://smite.fandom.com/wiki/List_of_gods").get();
            _doc.getElementsByTag("tr").forEach(x -> {
                if (x.select(":contains(Physical)").size() > 0){
                    _godTable.add(x);
                }
                else if (x.select(":contains(Magical)").size() > 0){
                    _godTable.add(x);
                }
            });
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
    }

    /**
     * @return List of God Names
     */
    public ArrayList<String> GetGodNames()
    {
        if (!_godNames.isEmpty()){
            return _godNames;
        }

        if (_godTable == null)
        {
            return new ArrayList<>();
        }

        _godTable.forEach(x -> _godNames.add(x.select("a").first().attr("title")));

        return _godNames;
    }

    /**
     * @return List of links to the god wiki page
     */
    public ArrayList<String> GetGodWikiLinks()
    {
        _godWikiLinks = ReadDataFromFile(Constants.ResourcesFolder + Constants.GodWikiListFileName);
        ArrayList<String> godNames = GetGodNames();

        if (godNames.size() != _godWikiLinks.size()) { _godWikiLinks = new ArrayList<>(); }


        if (!_godWikiLinks.isEmpty()){
            return _godWikiLinks;
        }

        if (_godTable == null)
        {
            return new ArrayList<>();
        }

        String baseUrl = "https://smite.fandom.com";

        _godTable.forEach(x -> _godWikiLinks.add(baseUrl+ x.select("a").first().attr("href")));

        WriteDataToFile(Constants.ResourcesFolder + Constants.GodWikiListFileName, _godWikiLinks);

        return _godWikiLinks;
    }

    public ArrayList<String> GetGodImageLinks()
    {
        _godImageLinks = ReadDataFromFile(Constants.ResourcesFolder + Constants.GodImageListFileName);
        ArrayList<String> godNames = GetGodNames();

        if (godNames.size() != _godImageLinks.size()) { _godImageLinks = new ArrayList<>(); }

        if (!_godImageLinks.isEmpty()){
            return _godImageLinks;
        }

        if (_godTable == null)
        {
            return new ArrayList<>();
        }

        ArrayList<String> wikiLinks = GetGodWikiLinks();
        ArrayList<Document> godPages = new ArrayList<>();

        wikiLinks.forEach(x ->
        {
            try
            {
                godPages.add(Jsoup.connect(x).get());
            }
            catch (IOException e)
            {
                System.out.println(e);
            }
        });

        godPages.forEach(y -> _godImageLinks.add(y.select(".image").first().attr("href")));

        _godImageLinks.forEach(x ->
        {
            try
            {
                Files.createDirectories(Paths.get(Constants.ImagesFolder));
                String fileName = godNames.get(_godImageLinks.indexOf(x));
                Connection.Response resultImageResponse = Jsoup.connect(x).ignoreContentType(true).execute();

                FileOutputStream out = (new FileOutputStream(Constants.ImagesFolder + fileName + ".jpg"));
                out.write(resultImageResponse.bodyAsBytes());
                out.close();
            }
            catch (IOException e)
            {
                System.out.println(e);
            }
        });

        WriteDataToFile(Constants.ResourcesFolder + Constants.GodImageListFileName, _godImageLinks);

        return _godImageLinks;
    }

    private ArrayList<String> ReadDataFromFile(String fileName)
    {
        File f = new File(fileName);
        if(f.exists() && !f.isDirectory())
        {
            try
            {
                InputStream is = new FileInputStream(fileName);
                List<String> input = Arrays.stream(new String(is.readAllBytes()).split(",")).toList();
                is.close();

                return new ArrayList<>(input);
            }
            catch(Exception e)
            {
                System.out.println(e);
                return new ArrayList<>();
            }
        }
        else
        {
            return new ArrayList<>();
        }
    }

    private void WriteDataToFile(String fileName, ArrayList<String> data)
    {
        try
        {
            OutputStream os = new FileOutputStream(fileName);

            String text = "";
            for (String s : data)
            {
                text += s + ",";
            }

            os.write(text.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
    }
}
