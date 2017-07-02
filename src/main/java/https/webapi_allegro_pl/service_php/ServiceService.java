
/*
 * 
 */

package https.webapi_allegro_pl.service_php;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.2.9
 * Sun Jul 02 21:58:27 CEST 2017
 * Generated source version: 2.2.9
 * 
 */


@WebServiceClient(name = "serviceService", 
                  wsdlLocation = "file:/C:/Users/Virtum/IdeaProjects/auctionfinderBackend/src/main/resources/test.wsdl",
                  targetNamespace = "https://webapi.allegro.pl/service.php") 
public class ServiceService extends Service {

    public final static URL WSDL_LOCATION;
    public final static QName SERVICE = new QName("https://webapi.allegro.pl/service.php", "serviceService");
    public final static QName ServicePort = new QName("https://webapi.allegro.pl/service.php", "servicePort");
    static {
        URL url = null;
        try {
            url = new URL("file:/C:/Users/Virtum/IdeaProjects/auctionfinderBackend/src/main/resources/test.wsdl");
        } catch (MalformedURLException e) {
            System.err.println("Can not initialize the default wsdl from file:/C:/Users/Virtum/IdeaProjects/auctionfinderBackend/src/main/resources/test.wsdl");
            // e.printStackTrace();
        }
        WSDL_LOCATION = url;
    }

    public ServiceService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public ServiceService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public ServiceService() {
        super(WSDL_LOCATION, SERVICE);
    }
    
    public ServiceService(WebServiceFeature ... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }
    public ServiceService(URL wsdlLocation, WebServiceFeature ... features) {
        super(wsdlLocation, SERVICE, features);
    }

    public ServiceService(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns ServicePort
     */
    @WebEndpoint(name = "servicePort")
    public ServicePort getServicePort() {
        return super.getPort(ServicePort, ServicePort.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns ServicePort
     */
    @WebEndpoint(name = "servicePort")
    public ServicePort getServicePort(WebServiceFeature... features) {
        return super.getPort(ServicePort, ServicePort.class, features);
    }

}
