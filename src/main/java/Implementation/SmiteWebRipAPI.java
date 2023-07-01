package Implementation;

import Enums.GodType;
import Helpers.ResourceHelper;
import Interfaces.IConstants;
import Interfaces.ISmiteAPI;
import org.apache.commons.codec.StringEncoderComparator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class SmiteWebRipAPI implements ISmiteAPI
{
    IConstants _constants;
    private Elements _godTable = new Elements();
    private Elements _itemTable = new Elements();

    private ArrayList<God> _gods = new ArrayList<>();
    private ArrayList<Item> _items = new ArrayList<>();

    public SmiteWebRipAPI()
    {
        _constants = new Constants();
        _godTable = GetGodTable();
        _itemTable = GetItemTable();
    }

    public Elements GetGodTable()
    {
        try
        {
            Document doc = Jsoup.connect("https://smite.fandom.com/wiki/List_of_gods").get();
            Elements godTable = new Elements();
            doc.getElementsByTag("tr").forEach(x -> {
                if (x.select(":contains(Physical)").size() > 0){
                    godTable.add(x);
                }
                else if (x.select(":contains(Magical)").size() > 0){
                    godTable.add(x);
                }
            });
            return godTable;
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
        return null;
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
            return finalGodWikiLinks;
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

        System.out.println("Pulling God Images from File");
        //local links to images
        ArrayList<String> godImageLinksOnFile = godImageLinks = PullImagesFromFile(godNames, _constants.ImagesFolder());

        if (!godImageLinks.isEmpty() && godImageLinks.size() == godNames.size())
        {
            ArrayList<String> finalGodImageLinks = godImageLinks;
            finalGodImageLinks.forEach(x -> _gods.get(finalGodImageLinks.indexOf(x)).GodImageLink = x);
            return finalGodImageLinks;
        }

        System.out.println("Fallback");
        System.out.println("Pulling God Image Links from File");
        //remote links to images - download to file and pull from there
        godImageLinks = ReadStringDataFromFile(_constants.ResourcesFolder() + _constants.GodImageListFileName());

        if (!godImageLinks.isEmpty() && godNames.size() == godImageLinks.size() && !(godImageLinks.contains(ResourceHelper.GetMissingTextureUrl()) || godImageLinks.contains(ResourceHelper.GetMissingTextureLocalUrl())))
        {
            godImageLinks = CheckIfImageExistsOnFile(godNames, godImageLinks, godImageLinksOnFile, _constants.ImagesFolder());
            DownloadImageDataToFile(godNames, godImageLinks, _constants.ImagesFolder());
            godImageLinks = PullImagesFromFile(godNames, _constants.ImagesFolder());
            ArrayList<String> finalGodImageLinksFromFile = godImageLinks;
            finalGodImageLinksFromFile.forEach(x -> _gods.get(finalGodImageLinksFromFile.indexOf(x)).GodImageLink = x);
            return godImageLinks;
        }

        //pull new data
        if (_godTable == null) { return new ArrayList<>(); }

        ArrayList<String> wikiLinks = GetGodWikiLinks();
        ArrayList<Document> godPages = new ArrayList<>();

        System.out.println("Fallback");
        System.out.println("Pulling New Wiki Pages for Gods");

        //very readable way of writing download missing images
        godNames.stream().filter(x -> !godImageLinksOnFile.stream().map(y -> y.split(".jpg")[0]).toList().contains(x)).forEach(x ->
        {
            try
            {
                godPages.add(Jsoup.connect(wikiLinks.get(godNames.indexOf(x))).get());
            }
            catch (IOException e)
            {
                System.out.println(e);
            }
        });

        godPages.forEach(y ->
        {
            System.out.println(y);
            String url = y.select("a.image").first().attr("href");
            if (!url.isBlank())
            {
                url = Arrays.stream(url.split("cb")).findFirst().orElse(ResourceHelper.GetMissingTextureUrl());
            }
            _gods.get(godPages.indexOf(y)).GodImageLink = url;
        });

        godImageLinks = new ArrayList(_gods.stream().map(x -> x.GodImageLink).toList());
        WriteStringDataToFile(_constants.ResourcesFolder() + _constants.GodImageListFileName(), godImageLinks);

        godImageLinks = CheckIfImageExistsOnFile(godNames, godImageLinks, godImageLinksOnFile, _constants.ImagesFolder());
        DownloadImageDataToFile(godNames, godImageLinks, _constants.ImagesFolder());
        godImageLinks = PullImagesFromFile(godNames, _constants.ImagesFolder());

        return godImageLinks;
    }

    @Override
    public GodType GetGodType(String godName)
    {
        return _gods.stream().filter(x -> x.GodName.equals(godName)).map(x -> x.GodType).findFirst().orElse(null);
    }

    public Elements GetItemTable()
    {
        try
        {
            Document doc = Jsoup.connect("https://smite.fandom.com/wiki/Items").get();
            String [] list = doc.body().toString().split("List of items</span>");

            if (list.length == 3)
            {
                list = list[2].split("<table class=\"navbox\" cellspacing=\"0\" style=\";\">");
            }

            if (list.length > 0)
            {
                //break into separate list of <div> inners
                list = list[0].split("<div");
                ArrayList<String> items = new ArrayList<>();
                Arrays.stream(list).forEach(x ->
                {
                    Arrays.stream(x.split("</div>")).findFirst().ifPresent(y ->
                    {
                        if (!y.isBlank())
                        {
                            items.add(y);
                        }
                    });
                });

                //break into spans and convert to documents for parsing
                ArrayList<String> spanList = new ArrayList<>(items.stream().map(x ->
                {
                    int i = x.indexOf(">");
                    if (i > -1)
                    {
                        String result = x.substring(i + 1, x.length()).strip();
                        return result.endsWith(")") ? result.substring(0, result.length() - 1) : result;
                    }
                    return null;
                }).toList());
                spanList.removeIf(x -> x == null || !x.endsWith("</span>"));
                Elements spanElements = new Elements();
                spanList.forEach(x -> spanElements.add(Jsoup.parse(x)));

                return new Elements(spanElements.stream().sorted(Comparator.comparing(x -> x.getElementsByTag("a").attr("title"))).toList());
            }
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
        return null;
    }

    @Override
    public ArrayList<String> GetItemNames()
    {
        if (!_items.isEmpty()) { return new ArrayList(_items.stream().map(x -> x.ItemName).toList()); }
        if (_itemTable == null) { return new ArrayList<>(); }

        _itemTable.forEach(x -> {
            Item item = new Item();
            item.ItemName = x.selectFirst("a").attr("title");
            _items.add(item);
        });

        return new ArrayList(_items.stream().map(x -> x.ItemName).toList());
    }

    @Override
    public ArrayList<String> GetItemWikiLinks()
    {
        ArrayList itemWikiLinks = new ArrayList(_items.stream().map(x -> x.ItemWikiLink).toList());
        itemWikiLinks.removeIf(Objects::isNull);
        if (!itemWikiLinks.isEmpty()) { return itemWikiLinks; }

        itemWikiLinks = ReadStringDataFromFile(_constants.ResourcesFolder() + _constants.ItemWikiListFileName());
        ArrayList<String> itemNames = GetItemNames();

        if (!itemWikiLinks.isEmpty() && itemNames.size() == itemWikiLinks.size())
        {
            ArrayList<String> finalItemWikiLinks = itemWikiLinks;
            finalItemWikiLinks.forEach(x -> _items.get(finalItemWikiLinks.indexOf(x)).ItemWikiLink = x);
            return finalItemWikiLinks;
        }

        if (_itemTable == null) { return new ArrayList<>(); }

        String baseUrl = "https://smite.fandom.com";
        _itemTable.forEach(x -> _items.get(_itemTable.indexOf(x)).ItemWikiLink = baseUrl + x.select("a").first().attr("href"));
        itemWikiLinks = new ArrayList(_items.stream().map(x -> x.ItemWikiLink).toList());

        WriteStringDataToFile(_constants.ResourcesFolder() + _constants.ItemWikiListFileName(), itemWikiLinks);

        return itemWikiLinks;
    }

    @Override
    public ArrayList<String> GetItemImageLinks()
    {
        ArrayList<String> itemImageLinks = new ArrayList(_items.stream().map(x -> x.ItemImageLink).toList());
        itemImageLinks.removeIf(Objects::isNull);
        if (!itemImageLinks.isEmpty()) { return itemImageLinks; }
        ArrayList<String> itemNames = GetItemNames();

        System.out.println("Pulling Item Images from File");
        //local links to images
        ArrayList<String> itemImageLinksOnFile = itemImageLinks = PullImagesFromFile(itemNames, _constants.ItemImagesFolder());

        if (!itemImageLinks.isEmpty() && itemImageLinks.size() == itemNames.size())
        {
            ArrayList<String> finalItemImageLinks = itemImageLinks;
            finalItemImageLinks.forEach(x -> _items.get(finalItemImageLinks.indexOf(x)).ItemImageLink = x);
            return finalItemImageLinks;
        }

        System.out.println("Fallback");
        System.out.println("Pulling Item Image Links from File");
        //remote links to images - download to file and pull from there
        itemImageLinks = ReadStringDataFromFile(_constants.ResourcesFolder() + _constants.ItemImageListFileName());
        if (!itemImageLinks.isEmpty() && itemNames.size() == itemImageLinks.size() && !(itemImageLinks.contains(ResourceHelper.GetMissingTextureUrl()) || itemImageLinks.contains(ResourceHelper.GetMissingTextureLocalUrl())))
        {
            itemImageLinks = CheckIfImageExistsOnFile(itemNames, itemImageLinks, itemImageLinksOnFile, _constants.ItemImagesFolder());
            DownloadImageDataToFile(itemNames, itemImageLinks, _constants.ItemImagesFolder());
            itemImageLinks = PullImagesFromFile(itemNames, _constants.ItemImagesFolder());
            ArrayList<String> finalGodImageLinksFromFile = itemImageLinks;
            finalGodImageLinksFromFile.forEach(x -> _items.get(finalGodImageLinksFromFile.indexOf(x)).ItemImageLink = x);
            return itemImageLinks;
        }

        //pull new data
        if (_itemTable == null) { return new ArrayList<>(); }

        System.out.println("Fallback");
        System.out.println("Pulling New Wiki Pages for Items");
        _itemTable.forEach(y ->
        {
            String url = Arrays.stream(y.getElementsByTag("img").attr("data-src").split("/scale")).findFirst().orElse(ResourceHelper.GetMissingTextureUrl());
            if (url.isBlank())
            {
                url = Arrays.stream(y.getElementsByTag("img").attr("src").split("/scale")).findFirst().orElse(ResourceHelper.GetMissingTextureUrl());
            }
            _items.get(_itemTable.indexOf(y)).ItemImageLink = url;

        });
        itemImageLinks = new ArrayList(_items.stream().map(x -> x.ItemImageLink).toList());
        WriteStringDataToFile(_constants.ResourcesFolder() + _constants.ItemImageListFileName(), itemImageLinks);

        itemImageLinks = CheckIfImageExistsOnFile(itemNames, itemImageLinks, itemImageLinksOnFile, _constants.ItemImagesFolder());

        DownloadImageDataToFile(itemNames, itemImageLinks, _constants.ItemImagesFolder());
        itemImageLinks = PullImagesFromFile(itemNames, _constants.ItemImagesFolder());
        return itemImageLinks;
    }

    private ArrayList<String> CheckIfImageExistsOnFile(ArrayList<String> names, ArrayList<String> imageLinks, ArrayList<String> imageLinksOnFile, String folder)
    {
        //awful way to mark existing files as downloaded
        ArrayList<String> result = new ArrayList(imageLinks.stream().map(x -> { if ( imageLinksOnFile.stream().map(y -> y.split(folder)[1]).toList().contains(names.get(imageLinks.indexOf(x)).replaceAll("[^\\sa-zA-Z0-9]", "_").replace(" ", "_") + ".jpg")) { return "DOWNLOADED"; } else { return x; } }).toList());
        System.out.println("Downloading: ");
        System.out.println(result.stream().filter(x -> !x.equals("DOWNLOADED")).toList());
        return result;
    }
}
