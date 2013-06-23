/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package topicmodel;

import java.util.List;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

/**
 *
 * @author YangC
 */
public class FreqElementAttr {
    
    private int componentSize;
    private int continualNum;
    private List<Tag> tagList;


    /**
     * @return the componentSize
     */
    public int getComponentSize() {
        return componentSize;
    }

    /**
     * @param componentSize the componentSize to set
     */
    public void setComponentSize(int componentSize) {
        this.componentSize = componentSize;
    }

    /**
     * @return the continualNum
     */
    public int getContinualNum() {
        return continualNum;
    }

    /**
     * @param continualNum the continualNum to set
     */
    public void setContinualNum(int continualNum) {
        this.continualNum = continualNum;
    }

    /**
     * @return the tagList
     */
    public List<Tag> getTagList() {
        return tagList;
    }

    /**
     * @param tagList the tagList to set
     */
    public void setTagList(List<Tag> tagList) {
        this.tagList = tagList;
    }

    
}
