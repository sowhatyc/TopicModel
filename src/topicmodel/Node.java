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
    private Attributes attributes;
    private String text;
    private String ownText;
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
     * @return the attributes
     */
    public Attributes getAttributes() {
        return attributes;
    }

    /**
     * @param attributes the attributes to set
     */
    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
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
     * @return the ownText
     */
    public String getOwnText() {
        return ownText;
    }

    /**
     * @param ownText the ownText to set
     */
    public void setOwnText(String ownText) {
        this.ownText = ownText;
    }
    
}
