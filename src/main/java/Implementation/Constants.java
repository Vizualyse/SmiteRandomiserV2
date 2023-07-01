package Implementation;

import Interfaces.IConstants;

public class Constants implements IConstants
{
    @Override
    public String PhysicalTag() { return "[Physical]"; };
    @Override
    public String MagicalTag() { return "[Magical]"; };
    @Override
    public String DataFolder() { return "data/"; };
    @Override
    public String ResourcesFolder() { return "resources/"; };
    @Override
    public String ImagesFolder() { return "resources/images/"; };
    public String ItemImagesFolder() { return "resources/item_images/"; };
    @Override
    public String GodWikiListFileName() { return "GodWikiList.txt"; };
    @Override
    public String GodImageListFileName() { return "GodImageList.txt"; };
    @Override
    public String ItemWikiListFileName() { return "ItemWikiList.txt"; };
    @Override
    public String ItemImageListFileName() { return "ItemImageList.txt"; };
    @Override
    public String RuleListFileName() { return "rules.txt"; };
    @Override
    public String MissingTextureFileName() {return "missingtexture.webp"; }
}
