
package com.bizvpm.dps.client;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.bizvpm.dps.client package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ProcessException_QNAME = new QName("http://service.dps.bizvpm.com/", "ProcessException");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.bizvpm.dps.client
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ProcessFault }
     * 
     */
    public ProcessFault createProcessFault() {
        return new ProcessFault();
    }

    /**
     * Create an instance of {@link Result }
     * 
     */
    public Result createResult() {
        return new Result();
    }

    /**
     * Create an instance of {@link Parameter }
     * 
     */
    public Parameter createParameter() {
        return new Parameter();
    }

    /**
     * Create an instance of {@link PersistableTask }
     * 
     */
    public PersistableTask createPersistableTask() {
        return new PersistableTask();
    }

    /**
     * Create an instance of {@link PersistableProcessor }
     * 
     */
    public PersistableProcessor createPersistableProcessor() {
        return new PersistableProcessor();
    }

    /**
     * Create an instance of {@link ProcessorList }
     * 
     */
    public ProcessorList createProcessorList() {
        return new ProcessorList();
    }

    /**
     * Create an instance of {@link KeyValuePair }
     * 
     */
    public KeyValuePair createKeyValuePair() {
        return new KeyValuePair();
    }

    /**
     * Create an instance of {@link Task }
     * 
     */
    public Task createTask() {
        return new Task();
    }

    /**
     * Create an instance of {@link ParameterList }
     * 
     */
    public ParameterList createParameterList() {
        return new ParameterList();
    }

    /**
     * Create an instance of {@link ProcessorState }
     * 
     */
    public ProcessorState createProcessorState() {
        return new ProcessorState();
    }

    /**
     * Create an instance of {@link DataObject }
     * 
     */
    public DataObject createDataObject() {
        return new DataObject();
    }

    /**
     * Create an instance of {@link DataSet }
     * 
     */
    public DataSet createDataSet() {
        return new DataSet();
    }

    /**
     * Create an instance of {@link Base64Binary }
     * 
     */
    public Base64Binary createBase64Binary() {
        return new Base64Binary();
    }

    /**
     * Create an instance of {@link HexBinary }
     * 
     */
    public HexBinary createHexBinary() {
        return new HexBinary();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProcessFault }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.dps.bizvpm.com/", name = "ProcessException")
    public JAXBElement<ProcessFault> createProcessException(ProcessFault value) {
        return new JAXBElement<ProcessFault>(_ProcessException_QNAME, ProcessFault.class, null, value);
    }

}
