package Implementation;

import Enums.GodType;
import Interfaces.IConstants;
import Interfaces.ISmiteAPI;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SmiteWebRipAPI implements ISmiteAPI
{
    IConstants _constants;
    private Document _doc;
    private Elements _godTable = new Elements();

    private ArrayList<God> _gods = new ArrayList<>();
    private ArrayList<String> _godImageLinks = new ArrayList();

    public SmiteWebRipAPI()
    {
        _constants = new Constants();
        try
        {
            Files.createDirectories(Paths.get(_constants.ResourcesFolder()));

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
    @Override
    public ArrayList<String> GetGodNames()
    {
        if (!_gods.isEmpty()) { return new ArrayList(_gods.stream().map(x -> x.GodName).toList()); }
        if (_godTable == null) { return new ArrayList<>(); }

        _godTable.forEach(x -> {
            God god = new God();
            god.GodName = x.selectFirst("a").attr("title");
            Element e = x.selectFirst("a:contains(Physical)");
            god.GodType = e == null ? GodType.MAGICAL : GodType.PHYSICAL;
            _gods.add(god);
        });

        return new ArrayList(_gods.stream().map(x -> x.GodName).toList());
    }

    /**
     * @return List of links to the god wiki page
     */
    @Override
    public ArrayList<String> GetGodWikiLinks()
    {
        ArrayList godWikiLinks = new ArrayList(_gods.stream().map(x -> x.GodWikiLink).toList());
        godWikiLinks.removeIf(Objects::isNull);
        if (!godWikiLinks.isEmpty()) { return godWikiLinks; }

        godWikiLinks = ReadStringDataFromFile(_constants.ResourcesFolder() + _constants.GodWikiListFileName());
        ArrayList<String> godNames = GetGodNames();

        if (!godWikiLinks.isEmpty() && godNames.size() == godWikiLinks.size())
        {
            ArrayList<String> finalGodWikiLinks = godWikiLinks;
            finalGodWikiLinks.forEach(x -> _gods.get(finalGodWikiLinks.indexOf(x)).GodWikiLink = x);
            return godWikiLinks;
        }

        if (_godTable == null) { return new ArrayList<>(); }

        String baseUrl = "https://smite.fandom.com";
        _godTable.forEach(x -> _gods.get(_godTable.indexOf(x)).GodWikiLink = baseUrl + x.select("a").first().attr("href"));
        godWikiLinks = new ArrayList(_gods.stream().map(x -> x.GodWikiLink).toList());

        WriteStringDataToFile(_constants.ResourcesFolder() + _constants.GodWikiListFileName(), godWikiLinks);

        return godWikiLinks;
    }

    @Override
    public ArrayList<String> GetGodImageLinks()
    {
        ArrayList<String> godImageLinks = new ArrayList(_gods.stream().map(x -> x.GodImageLink).toList());
        godImageLinks.removeIf(Objects::isNull);
        if (!godImageLinks.isEmpty()) { return godImageLinks; }
        ArrayList<String> godNames = GetGodNames();

        //local links to images
        godImageLinks = PullGodImagesFromFile(_constants);

        if (!godImageLinks.isEmpty() && godImageLinks.size() == godNames.size())
        {
            ArrayList<String> finalGodImageLinks = godImageLinks;
            finalGodImageLinks.forEach(x -> _gods.get(finalGodImageLinks.indexOf(x)).GodImageLink = x);
            return godImageLinks;
        }

        //remote links to images - download to file and pull from there
        godImageLinks = ReadStringDataFromFile(_constants.ResourcesFolder() + _constants.GodImageListFileName());
        if (!godImageLinks.isEmpty() && godNames.size() == _godImageLinks.size())
        {
            ArrayList<String> finalGodImageLinks = godImageLinks;
            finalGodImageLinks.forEach(x -> DownloadImageDataToFile(x, finalGodImageLinks.indexOf(x), _constants));
            godImageLinks = PullGodImagesFromFile(_constants);
            ArrayList<String> finalGodImageLinksFromFile = godImageLinks;
            finalGodImageLinksFromFile.forEach(x -> _gods.get(finalGodImageLinksFromFile.indexOf(x)).GodImageLink = x);
            return godImageLinks;
        }

        //pull new data
        if (_godTable == null) { return new ArrayList<>(); }

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

        godPages.forEach(y ->
        {
            String url = y.select(".image").first().attr("href");
            _gods.get(godPages.indexOf(y)).GodImageLink = url;
            DownloadImageDataToFile(url, godPages.indexOf(y), _constants);
        });

        godImageLinks = new ArrayList(_gods.stream().map(x -> x.GodImageLink).toList());
        WriteStringDataToFile(_constants.ResourcesFolder() + _constants.GodImageListFileName(), godImageLinks);

        return godImageLinks;
    }

    @Override
    public GodType GetGodType(String godName)
    {
        return _gods.stream().filter(x -> x.GodName.equals(godName)).map(x -> x.GodType).findFirst().orElse(null);
    }
}
