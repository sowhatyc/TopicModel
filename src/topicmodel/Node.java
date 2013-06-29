/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package topicmodel;

import java.util.List;
import org.jsoup.nodes.Attributes;
import org.jsoup.parser.Tag;

/**
 *
 * @author YangC
 */
public class Node {
    
    private Tag tag;
    private TextNodeUnit textNodeUnit;
    private List<Node> childNode;
    private boolean isAllHave;

    /**
     * @return the tag
     */
    public Tag getTag() {
        return tag;
    }

    /**
     * @param tag the tag to set
     */
    public void setTag(Tag tag) {
        this.tag = tag;
    }

    /**
     * @return the childNode
     */
    public List<Node> getChildNode() {
        return childNode;
    }

    /**
     * @param childNode the childNode to set
     */
    public void setChildNode(List<Node> childNode) {
        this.childNode = childNode;
    }

    /**
     * @return the isAllHave
     */
    public boolean isIsAllHave() {
        return isAllHave;
    }

    /**
     * @param isAllHave the isAllHave to set
     */
    public void setIsAllHave(boolean isAllHave) {
        this.isAllHave = isAllHave;
    }

    /**
     * @return the textNodeUnit
     */
    public TextNodeUnit getTextNodeUnit() {
        return textNodeUnit;
    }

    /**
     * @param textNodeUnit the textNodeUnit to set
     */
    public void setTextNodeUnit(TextNodeUnit textNodeUnit) {
        this.textNodeUnit = textNodeUnit;
    }

    
}
