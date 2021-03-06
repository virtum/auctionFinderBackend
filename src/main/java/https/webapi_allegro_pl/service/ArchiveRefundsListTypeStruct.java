
package https.webapi_allegro_pl.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.cxf.jaxb.JAXBToStringBuilder;
import org.apache.cxf.jaxb.JAXBToStringStyle;


/**
 * <p>Java class for ArchiveRefundsListTypeStruct complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArchiveRefundsListTypeStruct">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="refundId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="buyerId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="buyerLogin" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArchiveRefundsListTypeStruct", propOrder = {

})
public class ArchiveRefundsListTypeStruct {

    protected int refundId;
    protected int buyerId;
    @XmlElement(required = true)
    protected String buyerLogin;

    /**
     * Gets the value of the refundId property.
     * 
     */
    public int getRefundId() {
        return refundId;
    }

    /**
     * Sets the value of the refundId property.
     * 
     */
    public void setRefundId(int value) {
        this.refundId = value;
    }

    /**
     * Gets the value of the buyerId property.
     * 
     */
    public int getBuyerId() {
        return buyerId;
    }

    /**
     * Sets the value of the buyerId property.
     * 
     */
    public void setBuyerId(int value) {
        this.buyerId = value;
    }

    /**
     * Gets the value of the buyerLogin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBuyerLogin() {
        return buyerLogin;
    }

    /**
     * Sets the value of the buyerLogin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBuyerLogin(String value) {
        this.buyerLogin = value;
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
