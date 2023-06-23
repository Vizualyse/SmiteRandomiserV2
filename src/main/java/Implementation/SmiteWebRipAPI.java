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

public class SmiteWebRipAPI implements ISmiteAPI
{
    IConstants _constants;
    private Document _doc;
    private Elements _godTable = new Elements();

    private ArrayList<String> _godNames = new ArrayList();
    private ArrayList<GodType> _godType = new ArrayList();
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
        if (!_godNames.isEmpty()){
            return _godNames;
        }

        if (_godTable == null)
        {
            return new ArrayList<>();
        }

        _godTable.forEach(x -> {
            _godNames.add(x.selectFirst("a").attr("title"));
            Element e = x.selectFirst("a:contains(Physical)");
            boolean b = e == null ? _godType.add(GodType.MAGICAL) : _godType.add(GodType.PHYSICAL);
        });

        return _godNames;
    }

    /**
     * @return List of links to the god wiki page
     */
    @Override
    public ArrayList<String> GetGodWikiLinks()
    {
        if (!_godWikiLinks.isEmpty()){
            return _godWikiLinks;
        }

        _godWikiLinks = ReadStringDataFromFile(_constants.ResourcesFolder() + _constants.GodWikiListFileName());
        ArrayList<String> godNames = GetGodNames();

        if (godNames.size() != _godWikiLinks.size()) { _godWikiLinks = new ArrayList<>(); }

        if (_godTable == null)
        {
            return new ArrayList<>();
        }

        String baseUrl = "https://smite.fandom.com";

        _godTable.forEach(x -> _godWikiLinks.add(baseUrl+ x.select("a").first().attr("href")));

        WriteStringDataToFile(_constants.ResourcesFolder() + _constants.GodWikiListFileName(), _godWikiLinks);

        return _godWikiLinks;
    }

    @Override
    public ArrayList<String> GetGodImageLinks()
    {
        if (!_godImageLinks.isEmpty()){
            return _godImageLinks;
        }
        ArrayList<String> godNames = GetGodNames();

        //local links to images
        _godImageLinks = PullGodImagesFromFile(_constants);

        if (!_godImageLinks.isEmpty() && _godImageLinks.size() == godNames.size()){
            return _godImageLinks;
        }

        //remote links to images
        _godImageLinks = ReadStringDataFromFile(_constants.ResourcesFolder() + _constants.GodImageListFileName());

        if (godNames.size() != _godImageLinks.size()) { _godImageLinks = new ArrayList<>(); }

        //pull new data
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
        _godImageLinks.forEach(x -> DownloadImageDataToFile(x, _godImageLinks, _constants));

        WriteStringDataToFile(_constants.ResourcesFolder() + _constants.GodImageListFileName(), _godImageLinks);

        return _godImageLinks;
    }

    @Override
    public GodType GetGodType(String godName)
    {
        return _godType.get(_godNames.indexOf(godName));
    }
}
