
package https.webapi_allegro_pl.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.cxf.jaxb.JAXBToStringBuilder;
import org.apache.cxf.jaxb.JAXBToStringStyle;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="postBuyForm" type="{https://webapi.allegro.pl/service.php}PostBuyFormStruct"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "postBuyForm"
})
@XmlRootElement(name = "doSendPostBuyFormResponse")
public class DoSendPostBuyFormResponse {

    @XmlElement(required = true)
    protected PostBuyFormStruct postBuyForm;

    /**
     * Gets the value of the postBuyForm property.
     * 
     * @return
     *     possible object is
     *     {@link PostBuyFormStruct }
     *     
     */
    public PostBuyFormStruct getPostBuyForm() {
        return postBuyForm;
    }

    /**
     * Sets the value of the postBuyForm property.
     * 
     * @param value
     *     allowed object is
     *     {@link PostBuyFormStruct }
     *     
     */
    public void setPostBuyForm(PostBuyFormStruct value) {
        this.postBuyForm = value;
    }

    /**
     * Generates a String representation of the contents of this type.
     * This is an extension method, produced by the 'ts' xjc plugin
     * 
     */
    @Override
    public String toString() {
        return JAXBToStringBuilder.valueOf(this, JAXBToStringStyle.DEFAULT_STYLE);
    }

}
