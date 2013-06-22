/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package topicmodel;

import Utils.StaticLib;
import Utils.WebCrawler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

/**
 *
 * @author YangC
 */
public class Test {
    
    public static void main(String args[]){
//        System.out.println(new WebCrawler().getContent("http://bbs.tianya.cn/list-develop-1.shtml"));
//        System.out.println(new WebCrawler().getContentMethod2("http://bbs.tianya.cn/list-develop-1.shtml"));
//        String url = "http://bbs.tianya.cn/list-develop-1.shtml";
        String url = "http://bbs.tianya.cn/post-develop-1347012-1.shtml";
        String retVal[] = new WebCrawler().getContent(url);
        long start = System.currentTimeMillis();
        Document doc = Jsoup.parse(retVal[1], url);
//        Elements elements = doc.getElementsByTag("body");
//        for(Element element : elements){
//            if(element.)
//        }
        
        Element element = doc.body();
        cleanTree(element);
        Map<String, ArrayList<Element>> sequenceMap = getSequenceMap(element, 2);
        Map<Element, ArrayList<String>> parentStructualMap = getParentStructualMap(sequenceMap);
        parentStructualMap = unionParentStructualMap(parentStructualMap, element);
        Iterator<Element> keyIter = parentStructualMap.keySet().iterator();
        while(keyIter.hasNext()){
            Element ele = keyIter.next();
            System.out.println("Parent Sequence : " + getHierarchicalSequence(ele, 2, true));
            System.out.println("Son Element Sequence : ");
            for(String sequence : parentStructualMap.get(ele)){
                System.out.println(sequence + "  " + sequenceMap.get(sequence).size());
            }
            System.out.println("*********************************");
        }
        System.out.println(System.currentTimeMillis() - start);
        
//        Iterator<String> keyIter = sequenceMap.keySet().iterator();
//        while(keyIter.hasNext()){
//            String key = keyIter.next();
//            System.out.println(key + "   :  " + sequenceMap.get(key).size());
//        }
//        List<Element> sortedEleList = sortElementBySize(element);
//        for(Element ele: sortedEleList){
//            printElement(ele, 0, 2);
//            System.out.println("*************************************");
//            System.out.println("*************************************");
//            System.out.println("*************************************");
//        }
        
//        Element elementMax = element;
//        elementMax = findMaxChildEle(element, elementMax);
//        printElement(elementMax, 0);
//        System.out.println("*************************************");
//        System.out.println("*************************************");
//        System.out.println("*************************************");
//        printElement(element, 0);
//        for(Node node : nodes){
//            printNode(node, 0);
//        }
//        Elements elements = doc.getElementsByTag("a");
//        for(Element ele : elements){
//            System.out.print(ele.attr("abs:href") + "    ");
//            System.out.println(ele.text());
//        }
    }
    
//    public static List<String> getOrderedSequenceList(Map<String, ArrayList<Element>> sequenceMap){
//        if(sequenceMap.isEmpty() || sequenceMap == null){
//            return null;
//        }
//        List<String> orderedSequenceList = new ArrayList<>();
//        Iterator<String> iter = sequenceMap.keySet().iterator();
//        while(iter.hasNext()){
//            orderedSequenceList.add(iter.next());
//        }
//        Collections.sort(orderedSequenceList, new Comparator<String>() {
//
//            @Override
//            public int compare(String o1, String o2) {
////                throw new UnsupportedOperationException("Not supported yet.");
//                return sequenceMap.get(o2).size() - sequenceMap.get(o1).size();
//            }
//        });
//    }
    
    
    
    public static Map<Element, ArrayList<String>> getParentStructualMap(Map<String, ArrayList<Element>> sequenceMap){
        if(sequenceMap.isEmpty() || sequenceMap == null){
            return null;
        }
        Map<Element, ArrayList<String>> parentStructualMap = new HashMap<>();
        Iterator<String> iter = sequenceMap.keySet().iterator();
        while(iter.hasNext()){
            String sequence = iter.next();
            Element parentEle = getCoownerParent(sequenceMap.get(sequence));
            if(parentStructualMap.containsKey(parentEle)){
                parentStructualMap.get(parentEle).add(sequence);
            }else{
                ArrayList<String> sequenceList = new ArrayList<>();
                sequenceList.add(sequence);
                parentStructualMap.put(parentEle, sequenceList);
            }
        }
        return parentStructualMap;
    }
    
    public static Map<Element, ArrayList<String>> unionParentStructualMap(Map<Element, ArrayList<String>> parentStructualMap, Element root){
        
        while(true){
            Set<Element> eleSet = parentStructualMap.keySet();
            boolean updated = false;
            Iterator<Element> iter = eleSet.iterator();
            while(iter.hasNext()){
                Element ele = iter.next();
                if(ele == root){
                    continue;
                }
                for(Element parentEle : ele.parents()){
                    if(parentEle == root){
                        break;
                    }else if(eleSet.contains(parentEle)){
                        parentStructualMap.get(parentEle).addAll(parentStructualMap.get(ele));
                        parentStructualMap.remove(ele);
                        updated = true;
                        break;
                    }
                }
                if(updated){
                    break;
                }
            }
            if(updated){
                continue;
            }else{
                break;
            }
        }
        return parentStructualMap;
    }
    
    public static Element getCoownerParent(ArrayList<Element> elementList){
        if(elementList.isEmpty() || elementList == null){
            return null;
        }
//        ArrayList<Elements> parentsList = new ArrayList<>();
//        int minParentsCount = Integer.MAX_VALUE;
//        int minIndex = -1;
//        for(int i=0; i<elementList.size(); i++){
//            parentsList.add(elementList.get(i).parents());
//            if(elementList.get(i).parents().size() < minParentsCount){
//                minParentsCount = elementList.get(i).parents().size();
//                minIndex = i;
//            }
//        }
//        Elements minimalParents = parentsList.get(minIndex);
//        Element coownerParent = null;
//        for(Element pEle : minimalParents){
//            boolean isParent = true;
//            for(Elements eles : parentsList){
//                if(!eles.contains(pEle)){
//                    isParent = false;
//                    break;
//                }
//            }
//            if(isParent){
//               coownerParent = pEle;
//               break;
//            }
//        }
//        ****************************method 3**********************
//        int left = 0;
//        int right = minimalParents.size();
//        Element coownerParent = null;
//        while(left < right){
//            int mid = (left + right) / 2;
//            Element pEle = minimalParents.get(mid);
//            boolean isParent = true;
//            for(Elements eles : parentsList){
//                if(!eles.contains(pEle)){
//                    isParent = false;
//                    break;
//                }
//            }
//            if(isParent){
//                coownerParent = pEle;
//                right = mid;
//            }else{
//                left = mid + 1;
//            }
//        }
//        ********************** method 2 ********************
        Elements parentEles = elementList.get(0).parents();
        Element coownerParent = null;
        for(Element parentEle : parentEles){
            boolean isParent = true;
            for(Element ele : elementList){
                if(!ele.parents().contains(parentEle)){
                    isParent = false;
                    break;
                }
            }
            if(isParent){
               coownerParent = parentEle;
               break;
            }
        }
        return coownerParent;
    }
    
    
    public static boolean getLatentFreqSeq(Element element, boolean latentNodeChild){
        int continualOccourence;
        if(latentNodeChild){
            continualOccourence = 2;
        }else{
            continualOccourence = 5;
        }
        Elements childElements = element.children();
        if(childElements.size() < continualOccourence){
            return false;
        }
        List<String> childEleSequence = new ArrayList<>();
        for(Element ele : childElements){
            String sequence = getHierarchicalSequence(ele, 2, false);
            childEleSequence.add(sequence.replaceAll("@null", ""));
        }
        int maxOccourence = 0;
        for(int componetSize=1; componetSize<=childElements.size()/continualOccourence; componetSize++){
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
                        occourence++;
                    }else{
                        if(occourence > maxOccourence){
                            maxOccourence = occourence;
                        }
                        occourence = 1; 
                        currentSequence = nextSequence;
                    }
                    currentIndex += componetSize;
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
            return true;
        }else{
            return false;
        }
    }
    
    public static Map<String, ArrayList<Element>> getSequenceMap(Element eleRoot, int level){
        List<Element> elementList = getElementList(eleRoot);
        Map<String, ArrayList<Element>> sequenceMap = new HashMap<>();
        for(Element ele : elementList){
            if(ele == eleRoot){
                continue;
            }
            String sequence = getVerifiedSequence(ele, level, false);
            if(sequence == null){
                continue;
            }
            if(sequenceMap.containsKey(sequence)){
                sequenceMap.get(sequence).add(ele);
            }else{
                ArrayList<Element> eleList = new ArrayList<>();
                eleList.add(ele);
                sequenceMap.put(sequence, eleList);
            }
        }
        return sequenceMap;
    }
    
    public static String getVerifiedSequence(Element element, int level, boolean includeAttVal){
        String sequence = getHierarchicalSequence(element, level, false);
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
    
    public static String getHierarchicalSequence(Element element, int level, boolean includeAttributes){
        if(level <= 0){
            return "";
        }
        String sequence = "<" + element.tagName();
        if(includeAttributes){
            Attributes attributes = element.attributes();
            for(Attribute attr : attributes){
                sequence += " " + attr.getKey() + "=\"" + attr.getValue() + "\"";
            }
        }
        sequence += ">";
        if(level > 1 && element.children().isEmpty()){
            sequence += "@null";
        }
        for(Element ele : element.children()){
            sequence += getHierarchicalSequence(ele, level-1, includeAttributes);
        }
        sequence += "</" + element.tagName() + ">";
        return sequence;
    }
    
    public static List<Element> sortElementBySize(Element root){
        List<Element> elementList = getElementList(root);
        Collections.sort(elementList, new Comparator<Element>(){

            @Override
            public int compare(Element o1, Element o2) {
//                throw new UnsupportedOperationException("Not supported yet.");
                return o2.children().size() - o1.children().size();
            }
            
        });
        return elementList;
    }
    
    public static List<Element> getElementList(Element root){
        List<Element> elementList = new ArrayList<Element>();
        elementList.add(root);
        for(Element ele : root.children()){
            elementList.addAll(getElementList(ele));
        }
        return elementList;
    }
    
    public static Element findMaxChildEle(Element elementRoot, Element retEle){
        if(elementRoot.children().size() > retEle.children().size() ){
            retEle = elementRoot;
        }
        for(Element ele : elementRoot.children()){
//            Element childMax = findMaxChildEle(ele, retEle);
//            if(childMax.children().size() > retEle.children().size()){
//                retEle = childMax;
//            }
            retEle = findMaxChildEle(ele, retEle);
        }
        return retEle;
    }
    
    public static void cleanTree(Element element){
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
    

    
    public static void printElement(Element element, int count, int level){
        if(level > 0 && count >= level){
            return;
        }
        for(int i=0; i<count; i++){
            System.out.print(" ");
        }
        System.out.print(element.nodeName() + " ");
        Attributes attributes = element.attributes();
        for(Attribute attr : attributes){
            System.out.print(attr.getKey() + "=" + attr.getValue() + " ");
        }
        System.out.println();
        for(Element ele : element.children()){
            printElement(ele, count+1, level);
        }
    }
    
    public static void printNode(Node node, int count){
//        if(node instanceof TextNode || node instanceof DataNode){
//            return;
//        }
        for(int i=0; i<count; i++){
            System.out.print(" ");
        }
//        System.out.print(node.toString());
        System.out.print(node.nodeName() + " ");
        Attributes attributes = node.attributes();
        for(Attribute attr : attributes){
            System.out.print(attr.getKey() + "=" + attr.getValue() + " ");
        }
        System.out.println();
        for(Node nd : node.childNodes()){
            printNode(nd, count+1);
        }
    }
}
