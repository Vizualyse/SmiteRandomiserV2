package Implementation;

import Interfaces.IConstants;

public class Constants implements IConstants
{
    @Override
    public String PhysicalTag() { return "[Physical]"; };
    @Override
    public String MagicalTag() { return "[Magical]"; };
    @Override
    public String ResourcesFolder() { return "resources/"; };
    @Override
    public String ImagesFolder() { return "resources/images/"; };
    @Override
    public String GodWikiListFileName() { return "GodWikiList.txt"; };
    @Override
    public String GodImageListFileName() { return "GodImageList.txt"; };
}
