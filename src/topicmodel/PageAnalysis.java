/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package topicmodel;

import Utils.StaticLib;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author YangC
 */
public class PageAnalysis {
    
//    Map<Element, FreqElementAttr> eleInfo;
    private static Map<String, FreqElementAttr> extractorRules = null;
    
//    public static boolean initialExtractorRule(){
//        boolean success = true;
//        if(extractorRules == null){
//            try {
//                Document doc = Jsoup.parse(new File(StaticLib.extractorRulesPath), "utf-8"); 
//                extractorRules = new HashMap<>();
//                doc.
//            } catch (IOException ex) {
//                Logger.getLogger(PageAnalysis.class.getName()).log(Level.SEVERE, null, ex);
//                success = false;
//            }
//        }
//    }
//    
//    public boolean analyzePage(String content, String baseUrl){
//        boolean success = true;
//        Document doc = Jsoup.parse(content, baseUrl);
//        Element element = doc.body();
//        cleanTree(element);
//        Map<Element, FreqElementAttr> eleInfo = getFreqEleInfo(element);
//        
//    }
    
    private Map<Element, FreqElementAttr> getFreqEleInfo(Element cleanTreeRoot){
        Map<Element, FreqElementAttr> feMap = getFreqElement(cleanTreeRoot);
        Map<String, ArrayList<Element>> seqEleMap = getSeqEleMap(feMap.keySet(), 2, false, false);
        Iterator<String> iterStr = seqEleMap.keySet().iterator();
        while(iterStr.hasNext()){
            ArrayList<Element> eleList = seqEleMap.get(iterStr.next());
            Attributes attrs = eleList.get(0).attributes();
            String attrKey = "";
            String attrVal = "";
            int min = Integer.MAX_VALUE;
            if(attrs.size() != 0){
                for(Attribute attr : attrs){
                    Elements elements = cleanTreeRoot.getElementsByAttributeValue(attr.getKey(), attr.getValue());
                    if(elements.containsAll(eleList) && elements.size() < min){
                        attrKey = attr.getKey();
                        attrVal = attr.getValue();
                        min = elements.size();
                    }
                    elements = cleanTreeRoot.getElementsByAttribute(attr.getKey());
                    if(elements.containsAll(eleList) && elements.size() < min){
                        attrKey = attr.getKey();
                        attrVal = "@OnlyAttrKey@";
                        min = elements.size();
                    }
                } 
            }
            Elements elements = cleanTreeRoot.getElementsByTag(eleList.get(0).tagName());
            if(elements.containsAll(eleList) && elements.size() < min){
                attrKey = eleList.get(0).tagName();
                attrVal = "@OnlyTagName@";
            }
            feMap.get(eleList.get(0)).setAttrKey(attrKey);
            feMap.get(eleList.get(0)).setAttrVal(attrVal);
            for(int i=1; i<eleList.size(); i++){
                feMap.remove(eleList.get(i));
            }
        }
        return feMap;
    }
    
    private Map<Element, FreqElementAttr> getFreqElement(Element root){
        Map<Element, FreqElementAttr> feMap  = getLatentFreqElement(root);
        if(feMap.size() > 1){
            feMap = getFilteredFreqElement(feMap, root);
        }
        List<Element> eleList = new ArrayList<>(feMap.keySet());
        for(Element ele : eleList){
            Element previousEle = ele.previousElementSibling();
            if(!feMap.containsKey(previousEle) && previousEle != null  && feMap.get(ele).getComponentSize() == 1){
                FreqElementAttr fea = new FreqElementAttr();
                fea.setComponentSize(1);
                fea.setContinualNum(1);
                fea.setStartElementsInfo(null);
                feMap.put(previousEle, fea);
            }
        }
        return feMap;
    }
    
    
    private Map<Element, FreqElementAttr> getFilteredFreqElement(Map<Element, FreqElementAttr> feMap, Element root){
        Map<String, ArrayList<Element>> seqEleMap = getSeqEleMap(feMap.keySet(), 2, false, false);
        Set<String> sequenceDone = new HashSet<>();
        Iterator<String> iterSeq = seqEleMap.keySet().iterator();
        while(iterSeq.hasNext()){
            String sequence = iterSeq.next();
            if(sequenceDone.contains(sequence)){
                continue;
            }
            ArrayList<Element> eleList = seqEleMap.get(sequence);
            Element eleParent = eleList.get(0).parent();
            while(eleParent != root){
                if(feMap.containsKey(eleParent)){
                    if(eleList.size() < feMap.get(eleParent).getContinualNum()){
                        for(Element ele : eleList){
                            feMap.remove(ele);
                        }
                    }else{
                        String parentSeq = getVerifiedSequence(eleParent, 2, false, false);
                        sequenceDone.add(parentSeq);
                        feMap.remove(eleParent);
                    }
                    break;
                }
                eleParent = eleParent.parent();
            }
            sequenceDone.add(sequence);
        }
        return feMap;
    }
    
    private Map<String, ArrayList<Element>> getSeqEleMap(Set<Element> elementSet, int level, boolean includeAtt, boolean includeAttVal){
        Map<String, ArrayList<Element>> seqEleMap = new HashMap<>();
        Iterator<Element> iter = elementSet.iterator();
        while(iter.hasNext()){
            Element ele = iter.next();
            String sequence = getVerifiedSequence(ele, level, includeAtt, includeAttVal);
            if(seqEleMap.containsKey(sequence)){
                seqEleMap.get(sequence).add(ele);
            }else{
                ArrayList<Element> eleList = new ArrayList<>();
                eleList.add(ele);
                seqEleMap.put(sequence, eleList);
            }
        }
        return seqEleMap;
    }
    
    private Map<Element, FreqElementAttr> getLatentFreqElement(Element root){
        Map<Element, FreqElementAttr> feMap = new HashMap<>();
        List<Element> elements = new LinkedList<>();
        elements.add(root);
        while(!elements.isEmpty()){
            Element element = elements.get(0);
//            String sequence = getHierarchicalSequence(element, 1, true, true).replaceAll("@null", "");
            FreqElementAttr lfe = isFreqElement(element, feMap.containsKey(element.parent()));
            Elements childEles = element.children();
            for(Element childEle : childEles){
                elements.add(childEle);
            }
            elements.remove(0);
            if(lfe != null){
                feMap.put(element, lfe);
            }
        }
        return feMap;
    }
    
    private FreqElementAttr isFreqElement(Element element, boolean latentNodeChild){
        int continualOccourence;
        if(latentNodeChild){
            continualOccourence = 2;
        }else{
            continualOccourence = 5;
        }
        Elements childElements = element.children();
        if(childElements.size() < continualOccourence){
            return null;
        }
        List<String> childEleSequence = new ArrayList<>();
        for(Element ele : childElements){
            String sequence = getHierarchicalSequence(ele, 2, false, false);
            childEleSequence.add(sequence.replaceAll("@null", ""));
        }
        int maxOccourence = 0;
        int componetSize;
        List<String> startElementsInfo = new ArrayList<>();
        for(componetSize=1; componetSize<=childElements.size()/continualOccourence; componetSize++){
            int occourence = 1;
            maxOccourence = 0;
            for(int i=0; i<componetSize; i++){
                int currentIndex = i;
                String currentSequence = "";
                String nextSequence = "";
                for(int j=currentIndex; j<currentIndex+componetSize; j++){
                    currentSequence += childEleSequence.get(j);
                }
                while(currentIndex+2*componetSize <= childElements.size()){
                    if(occourence == 1 && currentIndex > (childElements.size() - componetSize * continualOccourence)){
                        break;
                    }
                    for(int j=currentIndex+componetSize; j<currentIndex+2*componetSize; j++){
                        nextSequence += childEleSequence.get(j);
                    }
                    if(currentSequence.compareTo(nextSequence) == 0){
                        if(occourence == 1){
                            startElementsInfo.clear();
                            String info = "";
                            for(int k=currentIndex; k<currentIndex+componetSize; k++){
                                Element eleInfo = childElements.get(k);
                                info = "\"" + eleInfo.tagName();
                                for(Attribute attr : eleInfo.attributes()){
                                    info += " " + attr.getKey();
                                }
                                info += "\"";
                            }
                            startElementsInfo.add(info);
                        }
                        occourence++;
                    }else{
                        if(occourence > maxOccourence && isSequenceSingleLevel(currentSequence)){
                            maxOccourence = occourence;
                        }
                        occourence = 1; 
                        currentSequence = nextSequence;
                    }
                    currentIndex += componetSize;
                    nextSequence = "";
                }
                if(occourence > maxOccourence && isSequenceSingleLevel(currentSequence)){
                    maxOccourence = occourence;
                }
                if(maxOccourence >= continualOccourence){
                    break;
                }
            }
            if(maxOccourence >= continualOccourence){
                break;
            }
        }
        if(maxOccourence >= continualOccourence){
            FreqElementAttr lfe = new FreqElementAttr();
            lfe.setComponentSize(componetSize);
            lfe.setContinualNum(maxOccourence);
            lfe.setStartElementsInfo(startElementsInfo);
            return lfe;
        }else{
            return null;
        }
    }
    
    private boolean isSequenceSingleLevel(String sequence){
        Pattern pattern = Pattern.compile("<[^/]+?><[^/]+?>");
        Matcher matcher = pattern.matcher(sequence);
        return matcher.find();
    }
    
    private String getVerifiedSequence(Element element, int level, boolean includeAtt, boolean includeAttVal){
        String sequence = getHierarchicalSequence(element, level, includeAtt, includeAttVal);
        int emptyNodeNum = 0;
        int startIndex = 0;
        int findIndex = -1;
        while((findIndex = sequence.indexOf("@null", startIndex)) != -1){
            emptyNodeNum++;
            startIndex = findIndex + 5;
        }
        if(emptyNodeNum >= element.children().size()){
            return null;
        }else{
            return sequence.replaceAll("@null", "");
        }
    }
    
    private String getHierarchicalSequence(Element element, int level, boolean includeAtt, boolean includeAttVal){
        if(level <= 0){
            return "";
        }
        String sequence = "<" + element.tagName();
        if(includeAtt){
            Attributes attributes = element.attributes();
            for(Attribute attr : attributes){
                sequence += " " + attr.getKey();
                if(includeAttVal){
                    sequence += "=\"" + attr.getValue() + "\"";
                }
            }
        }
        sequence += ">";
        if(level > 1 && element.children().isEmpty()){
            sequence += "@null";
        }
        for(Element ele : element.children()){
            sequence += getHierarchicalSequence(ele, level-1, includeAtt, includeAttVal);
        }
        sequence += "</" + element.tagName() + ">";
        return sequence;
    }
    
    private void cleanTree(Element element){
        if(StaticLib.tagSet == null){
            StaticLib.initialTagSet();
        }
        if(!element.tag().formatAsBlock() && !StaticLib.tagSet.contains(element.tagName())){
            element.remove();
        }else{
            Elements elements = element.children();
            for(Element ele : elements){
                cleanTree(ele);
            }
        }
    }
    
}
