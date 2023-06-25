package Interfaces;

public interface IConstants
{
    default int MinScreenHeight() { return (int) (MinScreenWidth() * 1.6); };
    default int DefaultScreenHeight() { return (int) (DefaultScreenWidth() * 1.6); };
    default int MaxScreenHeight() { return (int) (MaxScreenWidth() * 1.6); };
    default int MinScreenWidth() { return 450; };
    default int DefaultScreenWidth() { return 450; };
    default int MaxScreenWidth() { return 1000; };
    default String PhysicalTag() { return null; };
    default String MagicalTag() { return null; };
    default String DataFolder() { return null; };
    default String ResourcesFolder() { return null; };
    default String ImagesFolder() { return null; };
    default String GodWikiListFileName() { return null; };
    default String GodImageListFileName() { return null; };
    default String RuleListFileName() { return null; };
}
