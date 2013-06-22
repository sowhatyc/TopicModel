/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package topicmodel;

import org.jsoup.nodes.Element;

/**
 *
 * @author YangC
 */
public class LatentFrequentElement {
    
    private Element element;
    private String frequentSequence;
    private int componentSize;
    private int startPosition;
    private int occurenceNum;

    /**
     * @return the element
     */
    public Element getElement() {
        return element;
    }

    /**
     * @param element the element to set
     */
    public void setElement(Element element) {
        this.element = element;
    }

    /**
     * @return the frequentSequence
     */
    public String getFrequentSequence() {
        return frequentSequence;
    }

    /**
     * @param frequentSequence the frequentSequence to set
     */
    public void setFrequentSequence(String frequentSequence) {
        this.frequentSequence = frequentSequence;
    }

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
     * @return the startPosition
     */
    public int getStartPosition() {
        return startPosition;
    }

    /**
     * @param startPosition the startPosition to set
     */
    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    /**
     * @return the occurenceNum
     */
    public int getOccurenceNum() {
        return occurenceNum;
    }

    /**
     * @param occurenceNum the occurenceNum to set
     */
    public void setOccurenceNum(int occurenceNum) {
        this.occurenceNum = occurenceNum;
    }

    
    
    
}
