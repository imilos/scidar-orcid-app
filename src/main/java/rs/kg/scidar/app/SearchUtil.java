package rs.kg.scidar.app;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class SearchUtil
{

    protected static final Logger log = Logger.getLogger(SearchUtil.class);

    public static final String ORCID_ID_SYNTAX = "\\d{4}-\\d{4}-\\d{4}-(\\d{3}X|\\d{4})";

    public static boolean isValidOrcid(String text) {
        return StringUtils.isNotBlank(text) && text.matches(SearchUtil.ORCID_ID_SYNTAX);
    }

    public static String replaceChars(String term)
    {
        String tmp =  term.replace("š", "s").replace("đ", "d").replace("č", "c").replace("ć", "c").replace("ž", "z").
                replace("Š", "S").replace("Đ", "D").replace("Č", "C").replace("Ć", "C").replace("Ž", "Z").
                replace(".","").replace(",","");

        return tmp.toLowerCase();
    }

    public static String makeSearchIndex(String author)
    {
        String tmp = replaceChars(author);
        String[] names = StringUtils.split(tmp.trim());
        if (names.length == 2)
        {
            String[] tmpNames = new String[2];
            tmpNames[0] = StringUtils.capitalize(names[0]) + " " + StringUtils.capitalize(names[1]);
            tmpNames[1] = StringUtils.capitalize(names[0]) + " " + StringUtils.capitalize(names[1].substring(0,1));
            return String.join("#", tmpNames) + "#";
        }
        else if (names.length == 3)
        {
            String[] tmpNames = new String[8];
            tmpNames[0] = StringUtils.capitalize(names[0]) + " " + StringUtils.capitalize(names[1]) + " " + StringUtils.capitalize(names[2]);
            tmpNames[1] = StringUtils.capitalize(names[0]) + "-" + StringUtils.capitalize(names[1]) + " " + StringUtils.capitalize(names[2]);

            tmpNames[2] = StringUtils.capitalize(names[0]) + " " + StringUtils.capitalize(names[1]) + " " + StringUtils.capitalize(names[2].substring(0,1));
            tmpNames[3] = StringUtils.capitalize(names[0]) + "-" + StringUtils.capitalize(names[1]) + " " + StringUtils.capitalize(names[2].substring(0,1));

            tmpNames[4] = StringUtils.capitalize(names[1]) + " " + StringUtils.capitalize(names[0]) + " " + StringUtils.capitalize(names[2]);
            tmpNames[5] = StringUtils.capitalize(names[1]) + "-" + StringUtils.capitalize(names[0]) + " " + StringUtils.capitalize(names[2]);

            tmpNames[6] = StringUtils.capitalize(names[1]) + " " + StringUtils.capitalize(names[0]) + " " + StringUtils.capitalize(names[2].substring(0,1));
            tmpNames[7] = StringUtils.capitalize(names[1]) + "-" + StringUtils.capitalize(names[0]) + " " + StringUtils.capitalize(names[2].substring(0,1));

            return String.join("#", tmpNames) + "#";
        }
        return StringUtils.capitalize(tmp);
    }

    public static String buildOrcidSearchTerm(String term)
    {
        String[] names = StringUtils.split(term.trim());
        String tmp = String.join(" ", names);

        if (tmp.contains(","))
        {
            tmp = "\""+ tmp.replace(", ", "\"AND\"") + "\"";
        }
        else if (term.contains(" "))
        {
            String[] n = StringUtils.split(term);
            tmp = "\"" + String.join("\"AND\"", n) + "\"";
        }
        return tmp;
    }

    public static String buildSolrSearchTerm(String term)
    {
        String tmp = replaceChars(term);
        String[] names = StringUtils.split(tmp.trim());
        tmp = String.join(" ", names);
        return StringUtils.capitaliseAllWords(String.join(" ", tmp));
    }
    public static String buildFailSafeSolrSearchTerm(String term)
    {
        String tmp = replaceChars(term);
        String[] names = StringUtils.split(tmp.trim());
        if (names.length > 0)
        {
            for (int i = 0; i < names.length; i++)
            {
                names[i] = StringUtils.capitalize(names[i].trim());
            }
            if (names.length > 1)
            {
                names[names.length - 1] = names[names.length - 1].substring(0,1);
            }
            return String.join(" ", names);
        }
        return StringUtils.capitalize(tmp);
    }
}
