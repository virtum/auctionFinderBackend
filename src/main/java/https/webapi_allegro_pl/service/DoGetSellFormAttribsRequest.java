
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
 *         &lt;element name="countryId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="webapiKey" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="localVersion" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="catId" type="{http://www.w3.org/2001/XMLSchema}int"/>
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
    "countryId",
    "webapiKey",
    "localVersion",
    "catId"
})
@XmlRootElement(name = "DoGetSellFormAttribsRequest")
public class DoGetSellFormAttribsRequest {

    protected int countryId;
    @XmlElement(required = true)
    protected String webapiKey;
    protected Long localVersion;
    protected int catId;

    /**
     * Gets the value of the countryId property.
     * 
     */
    public int getCountryId() {
        return countryId;
    }

    /**
     * Sets the value of the countryId property.
     * 
     */
    public void setCountryId(int value) {
        this.countryId = value;
    }

    /**
     * Gets the value of the webapiKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWebapiKey() {
        return webapiKey;
    }

    /**
     * Sets the value of the webapiKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWebapiKey(String value) {
        this.webapiKey = value;
    }

    /**
     * Gets the value of the localVersion property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getLocalVersion() {
        return localVersion;
    }

    /**
     * Sets the value of the localVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setLocalVersion(Long value) {
        this.localVersion = value;
    }

    /**
     * Gets the value of the catId property.
     * 
     */
    public int getCatId() {
        return catId;
    }

    /**
     * Sets the value of the catId property.
     * 
     */
    public void setCatId(int value) {
        this.catId = value;
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
