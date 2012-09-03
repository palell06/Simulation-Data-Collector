package collector;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class WeatherParser {
	
	public static WeatherData getWeatherData(String country,String county,String municipiality, String city)
	{
		WeatherData data = null;
		
		try {
			URL url = new URL("www.yr.no/"+country+"/"+"/"+county+"/"+municipiality+"/varsel.xml");
		
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(url.openStream()));
			
			DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
			Document document = xmlBuilder.parse(url.openStream());
			
			data = new WeatherData();
			
			NodeList rootNode = document.getDocumentElement().getElementsByTagName("weatherdata");
			System.out.println("-------------------------");
			
			NodeList locationNode = rootNode.item(0).getChildNodes();
			
			Element nameElement = (Element)locationNode.item(0);
			
			String name = getTagValue("name", nameElement);
			
			Element typeElement = (Element)locationNode.item(1);
			
			String type = getTagValue("type",typeElement);
			
			Element countryElement = (Element)locationNode.item(2);
			
			String countryName = getTagValue("country",countryElement);
			
			Element timezoneElement = (Element)locationNode.item(3);
			
			String timeZone_id = timezoneElement.getAttribute("id");
			String utcoffsetMinutes = timezoneElement.getAttribute("utfcoffsetMinutes");
			
			Element location = (Element)locationNode.item(4);
			
			String altitude = location.getAttribute("altitude");
			String latitude = location.getAttribute("latitude");
			String longitude = location.getAttribute("longitude");
			String geobase = location.getAttribute("geobase");
			String geobaseid = location.getAttribute("geobaseid");
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return data;
	}
	
	
	
	
	private static String getTagValue(String tag, Element element)
	{
		NodeList list = element.getElementsByTagName(tag).item(0).getChildNodes();
	
		Node value = (Node)list.item(0);
		return value.getNodeValue();
	}
}
