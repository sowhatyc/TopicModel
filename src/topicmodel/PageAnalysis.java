/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package topicmodel;

import Utils.StaticLib;
import Utils.Storage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private String baseUrl;
    private String type;
    
    public static boolean initialExtractorRule(){
        boolean success = true;
        if(extractorRules == null){
            try {
                File file = new File(StaticLib.extractorRulesPath);
                file.createNewFile();
                Document doc = Jsoup.parse(file, "utf-8"); 
                extractorRules = new HashMap<>();
                Elements domainEles = doc.getElementsByTag("domainname");
                for(Element domainEle : domainEles){
                    String domainName = domainEle.ownText();
                    FreqElementAttr fea = new FreqElementAttr();
                    fea.setComponentSize(Integer.valueOf(domainEle.getElementsByTag("componentSize").first().ownText()));
                    fea.setContinualNum(Integer.valueOf(domainEle.getElementsByTag("continualnum").first().ownText()));
                    fea.setAttrKey(domainEle.getElementsByTag("attrkey").first().ownText());
                    fea.setAttrVal(domainEle.getElementsByTag("attrval").first().ownText());
                    List<String> startElementInfo = new ArrayList<>();
                    Elements startInfoEles = domainEle.getElementsByTag("eleinfo");
                    for(Element infoEle : startInfoEles){
                        startElementInfo.add(infoEle.ownText());
                    }
                    fea.setStartElementsInfo(startElementInfo);
                    extractorRules.put(domainName, fea);
                }
            } catch (IOException ex) {
                Logger.getLogger(PageAnalysis.class.getName()).log(Level.SEVERE, null, ex);
                success = false;
            }
        }
        return success;
    }
//    
    public boolean analyzePage(String content, String url, boolean isListPage){
        boolean success = true;
        if(!initialExtractorRule()){
            System.err.println("Initial Extractor Rules Failed!!!");
            return false;
        }
        String baseUrl = StaticLib.getBaseUrl(url);
        if(baseUrl == null){
            System.err.println("The url is not Correct!!");
            return false;
        }
        String type = "";
        if(isListPage){
            type = "listpage";
        }else{
            type = "contentpage";
        }
        setBaseUrl(baseUrl);
        setType(type);
        if(!extractorRules.containsKey(baseUrl+type)){
            Document doc = Jsoup.parse(content, baseUrl);
            Element element = doc.body();
            cleanTree(element);
            Map<Element, FreqElementAttr> eleInfo = getFreqEleInfo(element);
            if(eleInfo == null){
                return false;
            }
            Iterator<Element> iter = eleInfo.keySet().iterator();
            Element friEle = iter.next();
            FreqElementAttr fea = eleInfo.get(friEle);
            String entry = "<domainName>" + baseUrl + type;
            entry += "<componentSize>" + fea.getComponentSize() + "</componentSize>";
            entry += "<continualNum>" + fea.getContinualNum() + "</continualNum>";
            entry += "<attrKey>" + fea.getAttrKey() + "</attrKey>";
            entry += "<attrVal>" + fea.getAttrVal() + "</attrVal>";
            for(String startEleInfo : fea.getStartElementsInfo()){
                entry += "<eleInfo>" + startEleInfo + "</eleInfo>";
            }
            entry += "</domainName>\n";
            if(!new Storage().saveFile(StaticLib.extractorRulesPath, entry, true)){
                System.err.println("Save File Failed!!!");
                return false;
            }
            extractorRules.put(baseUrl+type, fea);
        }else{
            System.out.println("Already Exsit!!");
        }
        return true;
    }
    
    public List<Elements> getAnalysisiElements(String content, String url, boolean isListPage){
        if(!analyzePage(content, url, isListPage)){
            System.err.println("Analyze Page Failed!!");
            return null;
        }
        String baseUrl = StaticLib.getBaseUrl(url);
        String type = "";
        if(isListPage){
            type = "listpage";
        }else{
            type = "contentpage";
        }
        FreqElementAttr fea = extractorRules.get(baseUrl+type);
        String attrKey = fea.getAttrKey();
        String attrVal = fea.getAttrVal();
        Elements elements = null;
        Document doc = Jsoup.parse(content, baseUrl);
        if(attrVal.equals("@OnlyAttrKey")){
            elements = doc.body().getElementsByAttribute(attrKey);
        }else if(attrVal.equals("@OnlyTagName")){
            elements = doc.body().getElementsByTag(attrKey);
        }else{
            elements = doc.body().getElementsByAttributeValue(attrKey, attrVal);
        }
        List<Elements> elementsList = new ArrayList<>();
        Elements entryEles = new Elements();
        Element priviousEle = elements.get(0).previousElementSibling();
        if(StaticLib.tagSet == null){
            StaticLib.initialTagSet();
        }
        if(fea.getComponentSize() == 1){
            while(priviousEle != null && !priviousEle.tag().formatAsBlock() && !StaticLib.tagSet.contains(priviousEle.tagName())){
                priviousEle = priviousEle.previousElementSibling();
            }
            if(priviousEle != null && (priviousEle.tag().formatAsBlock() || StaticLib.tagSet.contains(priviousEle.tagName()))){
                entryEles.add(priviousEle);
                elementsList.add(entryEles);
            }
            for(int i=0; i<elements.size(); i++){
                for(Element ele : elements.get(i).children()){
                    entryEles = new Elements();
                    entryEles.add(ele);
                    elementsList.add(entryEles);
                }
            }
        }else{
            for(int i=0; i<elements.size(); i++){
                Elements childEles = elements.get(i).children();
                int j = 0;
                int equalCount = 0;
                int[] indexNum = new int[fea.getComponentSize()];
                entryEles = new Elements();
                for(; j<childEles.size(); j++){
                    if(childEles.get(j).tag().formatAsBlock() || StaticLib.tagSet.contains(priviousEle.tagName())){
                        String eleAttr = getVerifiedSequence(childEles.get(j), 1, true, false);
                        eleAttr = "@" + eleAttr.substring(1);
                        eleAttr = eleAttr.substring(0, eleAttr.indexOf("</")-1);
                        if(eleAttr.equals(fea.getStartElementsInfo().get(equalCount))){
                            indexNum[equalCount] = j;
                            if(++equalCount == indexNum.length){
                                for(int k=0; k<indexNum.length; k++){
                                    entryEles.add(childEles.get(indexNum[k]));
                                }
                                elementsList.add(entryEles);
                                entryEles = new Elements();
                                equalCount = 0;
                            }
                        }else{
                            equalCount = 0;
                        }
                    }
                }
            }
        }
        
        return elementsList;
    }
    
    
    
    private Map<Element, FreqElementAttr> getFreqEleInfo(Element cleanTreeRoot){
        Map<Element, FreqElementAttr> feMap = getFreqElement(cleanTreeRoot);
        Map<String, ArrayList<Element>> seqEleMap = getSeqEleMap(feMap.keySet(), 2, false, false);
        Iterator<String> iterStr = seqEleMap.keySet().iterator();
        int maxCount = 0;
        Element maxEle = null;
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
                        attrVal = "@OnlyAttrKey";
                        min = elements.size();
                    }
                } 
            }
            Elements elements = cleanTreeRoot.getElementsByTag(eleList.get(0).tagName());
            if(elements.containsAll(eleList) && elements.size() < min){
                attrKey = eleList.get(0).tagName();
                attrVal = "@OnlyTagName";
            }
            feMap.get(eleList.get(0)).setAttrKey(attrKey);
            feMap.get(eleList.get(0)).setAttrVal(attrVal);
            int size = feMap.get(eleList.get(0)).getContinualNum() * eleList.size();
            feMap.get(eleList.get(0)).setContinualNum(size);
            if(size > maxCount){
                maxCount = size;
                maxEle = eleList.get(0);
            }
            for(int i=1; i<eleList.size(); i++){
                feMap.remove(eleList.get(i));
            }
        }
        List<Element> eleList = new ArrayList<>(feMap.keySet());
        for(Element ele : eleList){
            if(ele != maxEle){
                feMap.remove(ele);
            }
        }
        if(feMap.size() != 1){
            System.err.println("Something Wrong!");
            return null;
        }
        return feMap;
    }
    
    private Map<Element, FreqElementAttr> getFreqElement(Element root){
        Map<Element, FreqElementAttr> feMap  = getLatentFreqElement(root);
        if(feMap.size() > 1){
            feMap = getFilteredFreqElement(feMap, root);
        }
//        List<Element> eleList = new ArrayList<>(feMap.keySet());
//        for(Element ele : eleList){
//            Element previousEle = ele.previousElementSibling();
//            if(!feMap.containsKey(previousEle) && previousEle != null  && feMap.get(ele).getComponentSize() == 1){
//                FreqElementAttr fea = new FreqElementAttr();
//                fea.setComponentSize(1);
//                fea.setContinualNum(1);
//                List<String> startElementInfo = new ArrayList<>();
//                startElementInfo.add("@null");
//                fea.setStartElementsInfo(startElementInfo);
//                feMap.put(previousEle, fea);
//            }
//        }
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
                                info = "@" + eleInfo.tagName();
                                for(Attribute attr : eleInfo.attributes()){
                                    info += " " + attr.getKey();
                                }
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
    
    public List<List<String>> getElementNode(List<Elements> elesList){
        List<ElementNode> eNodeList = new ArrayList<>();
        int count = 0;
        for(Elements eles : elesList){
            ElementNode eNode = new ElementNode();
            List<Node> nodeList = new ArrayList<>();
            for(Element ele : eles){
                nodeList.add(getNodeInfo(ele));
            }
            eNode.setNodes(nodeList);
            eNode.setPositionIndex(count++);
            eNodeList.add(eNode);
        }
        FreqElementAttr fea = extractorRules.get(baseUrl+type);
        int continualNum = fea.getContinualNum();
        int level = 1;
        int maximumNum = Integer.MIN_VALUE;
        Map<String, List<ElementNode>>  seqENodeMap = new HashMap<>();
        do{
            maximumNum = Integer.MIN_VALUE;
            seqENodeMap.clear();
            level++;
            for(ElementNode eNode : eNodeList){
                String seq = getENodeSeq(eNode, true, level);
                if(seq.equals("")){
                    continue;
                }
                if(seqENodeMap.containsKey(seq)){
                    seqENodeMap.get(seq).add(eNode);
                }else{
                    List<ElementNode> eNList = new ArrayList<>();
                    eNList.add(eNode);
                    seqENodeMap.put(seq, eNList);
                }
                if(seqENodeMap.get(seq).size() > maximumNum){
                    maximumNum = seqENodeMap.get(seq).size();
                }
            }
        }while(maximumNum > continualNum);
        
        Iterator<String> iter = seqENodeMap.keySet().iterator();
        eNodeList.clear();
        while(iter.hasNext()){
            List<ElementNode> eNList = seqENodeMap.get(iter.next());
            if(eNList.size() > 1){
                eNList = alignElementNode(eNList);
            }
            eNodeList.addAll(eNList);
        }
        Collections.sort(eNodeList, new Comparator<ElementNode>() {

            @Override
            public int compare(ElementNode o1, ElementNode o2) {
//                throw new UnsupportedOperationException("Not supported yet.");
                return o1.getPositionIndex() - o2.getPositionIndex();
            }
        });
        List<List<String>> entrys = new ArrayList<>();
        for(ElementNode eNode : eNodeList){
            List<TextNode> elementNT = getElementNodeTextSeq(eNode);
            if(elementNT != null && !elementNT.isEmpty()){
                List<String> entry = new ArrayList<>();
                for(TextNode tNode : elementNT){
                    String str = "";
                    for(TextNodeUnit tnu : tNode.getTextNode()){
                        str += "@Tag: " + tnu.getTag().getName() + " ";
                        str += "@Attributes: ";
                        for(Attribute attr : tnu.getAttributes()){
                            str += attr.getKey() + "=\"" + attr.getValue() + "\" ";
                        }
                        str += "@Text: " + tnu.getText() + " ";
                    }
                    entry.add(str);
                }
                entrys.add(entry);
            }
        }
        
        return entrys;
    }
    
    private List<TextNode> getElementNodeTextSeq(ElementNode eNode){
        List<TextNode> eNodeTextSeq = null;
        if(eNode.getNodes() != null){
            eNodeTextSeq = new ArrayList<>();
            for(Node node : eNode.getNodes()){
                eNodeTextSeq.addAll(getNodeTexSeq(node, node.isIsAllHave()));
            }
        }
        return eNodeTextSeq;
    }
    
    private List<TextNode> getNodeTexSeq(Node node, boolean isAlign){
        List<TextNode> nodeTextSeq = new ArrayList<>();
        List<TextNodeUnit> tnuList = new ArrayList<>();
        TextNode tN = new TextNode();
        if(!node.getTextNodeUnit().getText().trim().equals("") || (node.isIsAllHave() && node.getChildNode() == null)){
            tnuList.add(node.getTextNodeUnit());
            tN.setTextNode(tnuList);
            nodeTextSeq.add(tN);
        }
        if(node.getChildNode() != null){
            for(Node childNode : node.getChildNode()){
                if(isAlign && !childNode.isIsAllHave()){
                    List<Node> nodeList = new LinkedList<>();
                    nodeList.add(childNode);
                    List<TextNodeUnit> tnuChildList = new ArrayList<>();
                    while(!nodeList.isEmpty()){
                        Node qNode = nodeList.get(0);
                        if(!qNode.getTextNodeUnit().getText().equals("")){
                            tnuChildList.add(qNode.getTextNodeUnit());
                        }
                        if(qNode.getChildNode() != null){
                            nodeList.addAll(qNode.getChildNode());
                        }
                        nodeList.remove(0);
                    }
                    tnuList.addAll(tnuChildList);
                    if(!tnuList.isEmpty()){
                        int index = nodeTextSeq.indexOf(tN);
                        if(index != -1){
                            nodeTextSeq.get(index).setTextNode(tnuList);
                        }else{
                            tN.setTextNode(tnuList);
                            nodeTextSeq.add(tN);
                        }
                    }
                }else{
                    nodeTextSeq.addAll(getNodeTexSeq(childNode, isAlign));
                }
            }
        }
        return nodeTextSeq;
    }
    
    private List<ElementNode> alignElementNode(List<ElementNode> eNList){
        Map<String, Integer> seqCountMap = new HashMap<>();
        for(ElementNode eNode : eNList){
            if(eNode.getNodes() != null){
                for(int i=0; i<eNode.getNodes().size(); i++){
                    Node node = eNode.getNodes().get(i);
                    if(node.getChildNode() != null){
                        for(int j=0; j<node.getChildNode().size(); j++){
                            List<String> seqList = getSeqList(node.getChildNode().get(j), i + "_" + j);
                            for(String seq : seqList){
                                int count = 0;
                                if(seqCountMap.containsKey(seq)){
                                    count = seqCountMap.get(seq);
                                }
                                seqCountMap.put(seq, count+1);
                            }
                        }
                    }
                }
            }
        }
        Set<String> necessaryNodeSeq = seqCountMap.keySet();
        List<String> seqList = new ArrayList<>(necessaryNodeSeq);
        for(String str : seqList){
            if(seqCountMap.get(str) != eNList.size()){
                necessaryNodeSeq.remove(str);
            }
        }
        List<ElementNode> updatedENlist = new ArrayList<>();
        for(ElementNode eNode : eNList){
            updatedENlist.add(updateElementNode(eNode, necessaryNodeSeq));
        }
        return updatedENlist;
    }
    
    private ElementNode updateElementNode(ElementNode eNode, Set<String> necessaryNodeSeq){
        if(eNode.getNodes() != null){
            List<Node> childNodes = new ArrayList<>();
            for(int i=0; i<eNode.getNodes().size(); i++){
                Node node = updateNode(eNode.getNodes().get(i), necessaryNodeSeq, ""+i);
                node.setIsAllHave(true);
                childNodes.add(node);
            }
            eNode.setNodes(childNodes);
        }
        return eNode;
    }
    
    private Node updateNode(Node node, Set<String> necessaryNodeSeq, String prefix){
        String seq = prefix + "_" + getNodeSeq(node, false, true, 1);
        if(necessaryNodeSeq.contains(seq)){
            node.setIsAllHave(true);
        }
        if(node.getChildNode() != null){
            List<Node> nodeList = new ArrayList<>();
            for(int i=0; i<node.getChildNode().size(); i++){
                nodeList.add(updateNode(node.getChildNode().get(i), necessaryNodeSeq, prefix + "_" + i));
            }
            node.setChildNode(nodeList);
        }
        return node;
    }
    
    private List<String> getSeqList(Node node, String prefix){
        List<String> seqList = new ArrayList<>();
        seqList.add(prefix + "_" + getNodeSeq(node, false, true, 1));
        if(node.getChildNode() != null){
            for(int i=0; i<node.getChildNode().size(); i++){
                seqList.addAll(getSeqList(node.getChildNode().get(i), prefix + "_" + i));
            }
        }
        return seqList;
    }
    
    
    private int getElementNodeSize(ElementNode eNode){
        int count = 0;
        if(eNode.getNodes() != null){
            for(Node childNode : eNode.getNodes()){
                count += getNodeSize(childNode);
            }
        }
        return count;
    }
    
    private int getNodeSize(Node node){
        int count = 0;
        if(node.getChildNode() != null){
            for(Node childNode : node.getChildNode()){
                count += getNodeSize(childNode);
            }
        }
        return count + 1;
    }
    
    public String getENodeSeq(ElementNode eNode, boolean onlyStructrueNode, int level){
        String seq = "";
        if(eNode.getNodes() != null){
            for(Node node : eNode.getNodes()){
                if(node.getChildNode() != null){
                    for(Node childNode : node.getChildNode()){
                        seq += getNodeSeq(childNode, onlyStructrueNode, false, level);
                    }
                }
            }
        }
        return seq;
    }
    
    public String getNodeSeq(Node node, boolean onlyStructrueNode, boolean singleNode, int level){
        if(StaticLib.tagSet == null){
            StaticLib.initialTagSet();
        }
        String seq = "";
        if(level == 0){
            return seq;
        }
        boolean osn = onlyStructrueNode;
        if(osn && (node.getTextNodeUnit().getTag().formatAsBlock() || StaticLib.tagSet.contains(node.getTextNodeUnit().getTag().toString()))){
            osn = !osn;
        }
        if(!osn){
            seq += "<" + node.getTextNodeUnit().getTag().toString();
            for(Attribute attr : node.getTextNodeUnit().getAttributes()){
                seq += " " + attr.getKey();
            }
            seq += ">";
            if(!singleNode && node.getChildNode() != null){
                for(Node childNode : node.getChildNode()){
                    seq += getNodeSeq(childNode, onlyStructrueNode, singleNode, level-1);
                }
            }
            seq += "</" + node.getTextNodeUnit().getTag().toString() + ">";
        }
        return seq;
    }
    
    public Node getNodeInfo(Element ele){
        Node node = new Node();
        TextNodeUnit tNU = new TextNodeUnit();
        tNU.setAttributes(ele.attributes());
        tNU.setText(ele.ownText());
        tNU.setTag(ele.tag());
        node.setTextNodeUnit(tNU);
        List<Node> childNodeList = null;
        if(ele.children().size() != 0){
            childNodeList = new ArrayList<>();
            for(Element childEle: ele.children()){
                childNodeList.add(getNodeInfo(childEle));
            }
        }
        node.setChildNode(childNodeList);
        return node;
    }
    
    public Map<String, ArrayList<Element>> getMinimalGeneralizationSeq(List<Elements> elesList){
        Map<String, ArrayList<Set<String>>> cehs = new HashMap<>();
        Map<String, ArrayList<Element>> cehsEleMap = new HashMap<>();
        Map<String, ArrayList<Element>> minimalGerneralSeq = new HashMap<>();
//        List<Element> eleList = new ArrayList<>();
//        for(Elements eles: elesList){
//            for(Element ele : eles){
//                eleList.add(ele);
//            }
//        }
        for(Elements eles : elesList){
            for(Element ele : eles){
                String seq = getChildEleHierSeq(ele, true, true, true);
                if(seq.equals("")){
                    continue;
                }
                Pattern p = Pattern.compile("\"[^\"]*\"");
                Matcher m = p.matcher(seq);
                String seqCpy = m.replaceAll("");
                if(cehsEleMap.containsKey(seqCpy)){
                    cehsEleMap.get(seqCpy).add(ele);
                }else{
                    ArrayList<Element> eleList = new ArrayList<>();
                    eleList.add(ele);
                    cehsEleMap.put(seqCpy, eleList);
                }
                m = p.matcher(seq);
                int count = 0;
                ArrayList<Set<String>> posVals = new ArrayList<>();
                while(m.find()){
                    String val = m.group();
                    if(cehs.containsKey(seqCpy)){
                        cehs.get(seqCpy).get(count).add(val);
                    }else{
                        Set<String> attrValSet = new HashSet<>();
                        attrValSet.add(val);
                        posVals.add(attrValSet);
                    }
                    count++;
                }
                if(!cehs.containsKey(seqCpy)){
                    cehs.put(seqCpy, posVals);
                }
            }
        }
        Iterator<String> iter = cehs.keySet().iterator();
        while(iter.hasNext()){
            ArrayList<Set<String>> posVals = cehs.get(iter.next());
            for(int i=0; i<posVals.size(); i++){
                if(posVals.get(i).size() != 1){
                    posVals.get(i).clear();
                    posVals.get(i).add("\"\"");
                }
            }
        }
        iter = cehs.keySet().iterator();
        while(iter.hasNext()){
            String seq = iter.next();
            String[] attrVals = seq.split("=");
            String str = "";
            for(int i=0; i<attrVals.length-1; i++){
                Iterator<String> vals = cehs.get(seq).get(i).iterator();
                str += attrVals[i] + "=" + vals.next();
            }
            str += attrVals[attrVals.length-1];
            minimalGerneralSeq.put(str, cehsEleMap.get(seq));
        }
        
        return minimalGerneralSeq;
    }
    
    public String getChildEleHierSeq(Element element, boolean includeAtt, boolean includeAttVal, boolean treeClean){
        Element eleCpy = element.clone();
        if(treeClean){
            cleanTree(eleCpy);
        }
        String seq = getHierarchicalSequence(eleCpy, -1, includeAtt, includeAttVal);
        seq = seq.replaceFirst("<[^/]+?>", "");
        seq = seq.substring(0, seq.lastIndexOf("</"));
        return seq;
    }
    
    private String getHierarchicalSequence(Element element, int level, boolean includeAtt, boolean includeAttVal){
        if(level == 0){
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

    /**
     * @return the baseUrl
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * @param baseUrl the baseUrl to set
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }
    
}
