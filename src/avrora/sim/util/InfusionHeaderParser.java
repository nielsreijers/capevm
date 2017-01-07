package avrora.sim.util;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.util.HashMap;

public class InfusionHeaderParser {
	public static String basedir = "BASE DIR NOT SET";

	private static HashMap<String, InfusionHeaderParser> parsers = new HashMap<String, InfusionHeaderParser>();
	public static InfusionHeaderParser getParser(String infusion) {
		if (parsers.containsKey(infusion)) {
			return parsers.get(infusion);
		}

		String headerPath = basedir + "/infusion-" + infusion + "/" + infusion + ".dih";
		InfusionHeaderParser parser = new InfusionHeaderParser(headerPath);
		parsers.put(infusion, parser);
		return parser;
	}


	private Element infusion;
	private NodeList methodImpl_nList;
	private NodeList methodDef_nList;

	public InfusionHeaderParser(String path) {
		try {
			File fXmlFile = new File(path);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			NodeList infusion_nList = doc.getElementsByTagName("infusion");
			this.infusion = (Element)infusion_nList.item(0);

			NodeList methodImplList_nList = infusion.getElementsByTagName("methodimpllist");
			Element methodImplList = (Element)methodImplList_nList.item(0);
			this.methodImpl_nList = methodImplList.getElementsByTagName("methodimpl");

			NodeList methodDefList_nList = infusion.getElementsByTagName("methoddeflist");
			Element methodDefList = (Element)methodDefList_nList.item(0);
			this.methodDef_nList = methodDefList.getElementsByTagName("methoddef");
		} catch (Exception ex) {
			System.err.println(ex.toString());
			System.exit(1);
		}
	}

	public String getMethodImpl_MethodDefId (int methodImplId) {
		for (int implIdx = 0; implIdx < methodImpl_nList.getLength(); implIdx++) {
			Element e = (Element)methodImpl_nList.item(implIdx);
			if (e.getAttribute("entity_id").equals(Integer.toString(methodImplId))) {
				return e.getAttribute("methoddef.entity_id");
			}
		}
		return null;			
	}

	public String getMethodImpl_MethodDefInfusion (int methodImplId) {
		Element methodImpl = null;
		for (int implIdx = 0; implIdx < methodImpl_nList.getLength(); implIdx++) {
			Element e = (Element)methodImpl_nList.item(implIdx);
			if (e.getAttribute("entity_id").equals(Integer.toString(methodImplId))) {
				return e.getAttribute("methoddef.infusion");
			}
		}
		return null;			
	}

	public String getMethodDef_name (String methodDefId) {
		for (int defIdx = 0; defIdx < methodDef_nList.getLength(); defIdx++) {
			Element e = (Element)methodDef_nList.item(defIdx);
			if (e.getAttribute("entity_id").equals(methodDefId)) {
				return e.getAttribute("name");
			}
		}
		return null;
	}

	public String getMethodDef_signature (String methodDefId) {
		for (int defIdx = 0; defIdx < methodDef_nList.getLength(); defIdx++) {
			Element e = (Element)methodDef_nList.item(defIdx);
			if (e.getAttribute("entity_id").equals(methodDefId)) {
				return e.getAttribute("signature");
			}
		}
		return null;
	}
}