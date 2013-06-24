/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author YangC
 */
public class StaticLib {
    
    public static Set<String> tagSet = null;
    
    public static String extractorRulesPath = "extractorRules.xml";
    
    public static void initialTagSet(){
        tagSet = new HashSet<String>();
        tagSet.add("li");
        tagSet.add("ol");
        tagSet.add("dl");
        tagSet.add("dt");
        tagSet.add("dd");
        tagSet.add("tr");
        tagSet.add("td");
        tagSet.add("th");
    }
    
    
}
