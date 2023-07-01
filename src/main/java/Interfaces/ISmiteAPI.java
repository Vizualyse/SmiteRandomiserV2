package Interfaces;

import Enums.GodType;
import Helpers.ResourceHelper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface ISmiteAPI
{
    ArrayList<String> GetGodNames();
    ArrayList<String> GetGodWikiLinks();
    ArrayList<String> GetGodImageLinks();
    ArrayList<String> GetItemNames();
    ArrayList<String> GetItemWikiLinks();
    ArrayList<String> GetItemImageLinks();
    GodType GetGodType(String godName);

    /**
     * Given the name of a .txt file, reads from the file into a list separating by commas
     * @param fileName name of a file to read from
     * @return
     */
    default ArrayList<String> ReadStringDataFromFile(String fileName)
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

    /**
     * Given a file name and a list of text, creates a .txt file with the data in comma seperated fashion
     * @param fileName name of file to create
     * @param data list of text
     */
    default void WriteStringDataToFile(String fileName, ArrayList<String> data)
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

    /**
     * Given a URL, downloads image to file with .jpg file format
     * @param url link to image
     * @param godImageLinks list of links to find index - length should match god names
     * @param constants an implementation of the IConstants interface
     */
    default void DownloadImageDataToFile(ArrayList<String> fileNames, ArrayList<String> links, String folder)
    {
        try
        {
            Files.createDirectories(Paths.get(folder));
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
        fileNames.forEach(x ->
        {
            try
            {
                String link = links.get(fileNames.indexOf(x));

                //if the link is blank use missing link
                if (link.isBlank())
                {
                    link = ResourceHelper.GetMissingTextureUrl();
                }
                //if the link hasn't been downloaded
                if (!link.equals("DOWNLOADED")) {
                    Connection.Response resultImageResponse = Jsoup.connect(link).ignoreContentType(true).execute();

                    FileOutputStream out = (new FileOutputStream(folder + x.replaceAll("[^\\sa-zA-Z0-9]", "_").replace(" ", "_") + ".jpg"));
                    out.write(resultImageResponse.bodyAsBytes());
                    out.close();
                }
            }
            catch (IOException e)
            {
                System.out.println(e);
            }
        });
    }

    /**
     * Tries to find images matching the god names
     * Note: Doesn't guarantee a file for each god
     * @return an ArrayList of local URLs
     * @param constants an implementation of the IConstants interface
     */
    default ArrayList<String> PullImagesFromFile(ArrayList<String> fileNames, String folder)
    {
        ArrayList result = new ArrayList();
        File imgLocation = new File(folder);
        if (imgLocation.isDirectory())
        {
            fileNames.forEach(x ->
            {
                File godImage = new File(folder + x.replaceAll("[^\\sa-zA-Z0-9]", "_").replace(" ", "_") + ".jpg");
                try
                {
                    if (godImage.exists())
                    {
                        result.add(godImage.toURI().toURL().toString());
                    }
                }
                catch (MalformedURLException e)
                {
                    System.out.println(e);
                }
            });
        }
        return result;
    }
}
