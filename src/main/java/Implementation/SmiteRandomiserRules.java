package Implementation;

import Interfaces.IConstants;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SmiteRandomiserRules
{
    IConstants _constants;
    private ArrayList<String> _physRules = new ArrayList();
    private ArrayList<String> _magRules = new ArrayList();

    public SmiteRandomiserRules()
    {
        _constants = new Constants();
        UpdateRulesFromFile();
    }

    public void UpdateRulesFromFile(){
        try
        {
            InputStream is = new FileInputStream("rules.txt");
            List<String> rules = Arrays.stream(new String(is.readAllBytes()).split("\n")).toList();

            rules.forEach(x ->
            {
                if (x.startsWith(_constants.PhysicalTag()))
                    _physRules.add(x.split("]")[1]);
                else
                    _magRules.add(x.split("]")[1]);
            });

            is.close();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }

    public void UpdateRuleFile()
    {
        try
        {
            OutputStream os = new FileOutputStream("rules.txt");

            String rule = "";
            for(String s: _physRules)
            {
                rule += _constants.PhysicalTag() + s + "\n";
            }
            for(String s: _magRules)
            {
                rule += _constants.PhysicalTag() + s + "\n";
            }

            os.write(rule.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
        catch (IOException ex)
        {
            Logger.getLogger(SmiteRandomiserUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void AddRule(SmiteRandomiserRuleType ruleType, String rule)
    {
        switch (ruleType){
            case PHYSICAL -> _physRules.add(rule);
            case MAGICAL -> _magRules.add(rule);
        }
    }

    public void RemoveRule(SmiteRandomiserRuleType ruleType, String rule)
    {
        switch (ruleType){
            case PHYSICAL -> _physRules.remove(rule);
            case MAGICAL -> _magRules.remove(rule);
        }
    }

    public void RemoveRule(SmiteRandomiserRuleType ruleType, int rule)
    {
        switch (ruleType){
            case PHYSICAL -> _physRules.remove(rule);
            case MAGICAL -> _magRules.remove(rule);
        }
    }

    public ArrayList<String> GetRules(SmiteRandomiserRuleType ruleType)
    {
        ArrayList<String> rules;
        switch (ruleType){
            case PHYSICAL -> rules = _physRules;
            case MAGICAL -> rules = _magRules;
            default -> rules = new ArrayList<>();
        }
        return rules;
    }

    public static SmiteRandomiserRuleType GetRuleTypeByIndex(int index){
        return index == 0 ? SmiteRandomiserRuleType.PHYSICAL : SmiteRandomiserRuleType.MAGICAL;
    }
}
