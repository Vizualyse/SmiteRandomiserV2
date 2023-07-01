package Helpers;

import Implementation.Constants;
import Interfaces.IConstants;
import com.sun.tools.javac.Main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ResourceHelper
{
    private static final IConstants _constants = new Constants();
    public static InputStream GetResourceFromFile(String fileName)
    {
        return Main.class.getClassLoader().getResourceAsStream(fileName);
    }

    public static String GetMissingTextureUrl()
    {
        return "https://static.wikia.nocookie.net/gmod/images/9/99/The_Missing_textures.png/revision/latest?cb=20210208200840";
    }
    public static String GetMissingTextureLocalUrl()
    {
        File missingTexture = new File(_constants.ResourcesFolder() + _constants.MissingTextureFileName());

        try
        {
            if (missingTexture.exists())
            {
                return missingTexture.toURI().toURL().toString();
            }
            InputStream is = ResourceHelper.GetResourceFromFile(_constants.DataFolder() + _constants.MissingTextureFileName());

            FileOutputStream f = new FileOutputStream(_constants.ResourcesFolder() + _constants.MissingTextureFileName());
            f.write(is.readAllBytes());

            return missingTexture.toURI().toURL().toString();
        }
        catch (Exception e)
        {
            System.out.println(e);
            return null;
        }
    }
}
