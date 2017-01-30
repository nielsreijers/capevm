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

		InfusionHeaderParser parser = new InfusionHeaderParser(infusion);
		parsers.put(infusion, parser);
		return parser;
	}

	private String name;
	private Element infusion;
	private NodeList methodImpl_nList;
	private NodeList methodDef_nList;
	private NodeList referencedInfusion_nList;

	public InfusionHeaderParser(String name) {
		try {
			if (name==null) {
				System.exit(1);
			}
			this.name = name;
			String headerPath = basedir + "/infusion-" + name + "/" + name + ".dih";

			File fXmlFile = new File(headerPath);
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

			NodeList referencedInfusionList_nList = infusion.getElementsByTagName("infusionlist");
			Element referencedInfusionList = (Element)referencedInfusionList_nList.item(0);
			this.referencedInfusion_nList = referencedInfusionList.getElementsByTagName("infusion");

		} catch (Exception ex) {
			System.err.println(ex.toString());
			System.exit(1);
		}
	}

	public String getReferencedInfusionName (int referencedInfusionId) {
		if (referencedInfusionId==0) {
			return this.name;
		} else {
			referencedInfusionId -= 1;
			if (referencedInfusion_nList.getLength() <= referencedInfusionId) {
				System.err.println("referenced infusion not found: " + referencedInfusionId + " in " + this.name);
			}
			Element e = (Element)referencedInfusion_nList.item(referencedInfusionId);
			NodeList header_nList = e.getElementsByTagName("header");
			Element header = (Element)header_nList.item(0);
			return header.getAttribute("name");
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

	public String getMethodImpl_name_and_signature (int methodImplId) {
		String methodDefId = getMethodImpl_MethodDefId(methodImplId);
		String methodDefInfusion = getMethodImpl_MethodDefInfusion(methodImplId);
		InfusionHeaderParser header = InfusionHeaderParser.getParser(methodDefInfusion);
		return header.getMethodDef_name(methodDefId) + " " + header.getMethodDef_signature(methodDefId);
	}

	public String getMethodDef_name_and_signature (int methodDefId) {
		String methodDefIdString = Integer.toString(methodDefId);
		return getMethodDef_name(methodDefIdString) + " " + getMethodDef_signature(methodDefIdString);
	}

}