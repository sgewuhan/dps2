
package com.bizvpm.dps.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for processorState complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="processorState">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="max" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="running" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="score" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="waiting" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "processorState", propOrder = {
    "max",
    "running",
    "score",
    "waiting"
})
public class ProcessorState {

    protected int max;
    protected int running;
    protected double score;
    protected int waiting;

    /**
     * Gets the value of the max property.
     * 
     */
    public int getMax() {
        return max;
    }

    /**
     * Sets the value of the max property.
     * 
     */
    public void setMax(int value) {
        this.max = value;
    }

    /**
     * Gets the value of the running property.
     * 
     */
    public int getRunning() {
        return running;
    }

    /**
     * Sets the value of the running property.
     * 
     */
    public void setRunning(int value) {
        this.running = value;
    }

    /**
     * Gets the value of the score property.
     * 
     */
    public double getScore() {
        return score;
    }

    /**
     * Sets the value of the score property.
     * 
     */
    public void setScore(double value) {
        this.score = value;
    }

    /**
     * Gets the value of the waiting property.
     * 
     */
    public int getWaiting() {
        return waiting;
    }

    /**
     * Sets the value of the waiting property.
     * 
     */
    public void setWaiting(int value) {
        this.waiting = value;
    }

}
