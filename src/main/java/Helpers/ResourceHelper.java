package Helpers;

import com.sun.tools.javac.Main;

import java.io.InputStream;

public class ResourceHelper
{
    public static InputStream GetResourceFromFile(String fileName)
    {
        return Main.class.getClassLoader().getResourceAsStream(fileName);
    }
}
