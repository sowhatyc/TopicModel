/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package topicmodel;

import org.jsoup.nodes.Attributes;
import org.jsoup.parser.Tag;

/**
 *
 * @author YangC
 */
public class TextNodeUnit {
    private Attributes attributes;
    private String text;


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
}
