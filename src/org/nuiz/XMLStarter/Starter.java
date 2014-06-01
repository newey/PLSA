package org.nuiz.XMLStarter;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.nuiz.guessUserMean.GuessZero;
import org.nuiz.parallelPLSA.PLSA;
import org.nuiz.parallelRecommend.*;
import org.nuiz.slopeOne.SlopeOne;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Starter to read in an XML file formed in accordance to LocalRec.xsd. Then runs al the 
 * tests specified there.
 *
 * @author Robert Newey
 */
public class Starter {

	
	/**
	 * @param args First element must a path to the xml configuraiton file.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		String fname = args[0];
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder(); 
		Document doc = db.parse(new File(fname));
		
		configParser(doc.getChildNodes().item(0));
		
		GlobalSettings.getExecutor().shutdown();
	}
	
	private static void configParser(Node config) throws IOException {
		NodeList nl = config.getChildNodes();
		
		GlobalSettings.parseGlobalSettings(nl.item(1));
		
		for (int i = 2; i < nl.getLength(); i++) {
			String nodeName = nl.item(i).getNodeName();
			if (nodeName.equals("holdOut") || 
					nodeName.equals("crossVal") || 
					nodeName.equals("examineUser")) {
				runnableParser(nl.item(i));
			}
		}
	}

	private static void runnableParser(Node runnableNode) throws IOException {
		NodeList nl = runnableNode.getChildNodes();
		String outFname = nl.item(1).getTextContent();
		DataList dataList = dataListParser(nl.item(3));
		Model model = modelParser(nl.item(5));
		
		OutputStream os = outFname.equals("-") ? System.out : new FileOutputStream(outFname);
		
		if (runnableNode.getNodeName().equals("holdOut")){
			holdOutParser(os, dataList, model, runnableNode);
		} else if (runnableNode.getNodeName().equals("crossVal")){
			crossValParser(os, dataList, model, runnableNode);
		} else if (runnableNode.getNodeName().equals("examineUser")){
			examineUserParser(os, dataList, model, runnableNode);
		} else {
			os.close();
			throw new IllegalArgumentException();
		}
		
		os.close();
	}
	
	private static void holdOutParser(OutputStream outStream, DataList dataList, Model model, Node node) throws IOException {
		int randomSeed = Integer.parseInt(node.getChildNodes().item(7).getTextContent());
		int testSize = Integer.parseInt((node.getChildNodes().item(9).getTextContent()));
		System.out.printf("holdout: %d %d\n", randomSeed, testSize);
		
		new HoldoutRunner(dataList, model, randomSeed, testSize, outStream);
	}
	
	private static void crossValParser(OutputStream outStream, DataList dataList, Model model, Node node) {
		int randomSeed = Integer.parseInt(node.getChildNodes().item(7).getTextContent());
		int folds = Integer.parseInt((node.getChildNodes().item(9).getTextContent()));
		System.out.printf("crossVal: %d %d\n", randomSeed, folds);
		new CVRunner(dataList, model, randomSeed, folds, outStream);
	}	
	
	private static void examineUserParser(OutputStream outStream, DataList dataList, Model model, Node node) throws IOException {
		int userNumber = Integer.parseInt(node.getChildNodes().item(7).getTextContent());
		String moviesFile = node.getChildNodes().item(9).getTextContent();
		String separator = node.getChildNodes().item(11).getTextContent();
		System.out.printf("examineUser: %d\n", userNumber);
		new UserExaminer(dataList, model, userNumber, moviesFile, separator, outStream);
	}
	
	private static DataList dataListParser(Node dataListNode) throws IOException {
		if (dataListNode.getNodeName().equals("fileDatalist")) {
			return fileDataListParser(dataListNode);
		} else if (dataListNode.getNodeName().equals("binaryFileDatalist")) {
			return binaryFileDataListParser(dataListNode);
		} else if (dataListNode.getNodeName().equals("normaliseDataList")) {
			return normaliseDataListParser(dataListNode);
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	private static Model modelParser(Node modelNode) {
		if (modelNode.getNodeName().equals("plsa")) {
			return plsaParser(modelNode);
		} else if (modelNode.getNodeName().equals("slopeOne")) {
			return new SlopeOne();
		} else if (modelNode.getNodeName().equals("guessZero")) {
			return new GuessZero();
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	private static DataList fileDataListParser(Node dataListNode) throws IOException {
		NodeList nl = dataListNode.getChildNodes();
		String inFname = nl.item(1).getTextContent();
		String sep = nl.item(3).getTextContent();
		
		return new FileDataList(inFname, sep);
	}
	
	private static DataList binaryFileDataListParser(Node dataListNode) throws IOException {
		NodeList nl = dataListNode.getChildNodes();
		String inFname = nl.item(1).getTextContent();
		
		return new BinaryFileDataList(inFname);
	}
	
	private static DataList normaliseDataListParser(Node dataListNode) throws IOException {
		NodeList nl = dataListNode.getChildNodes();
		Double normFactor = Double.parseDouble((nl.item(1).getTextContent()));
		DataList dl = dataListParser(nl.item(3));
		
		NormaliseData core = new NormaliseData(dl, normFactor);
		
		return new NormalisedDataList(dl, core);
	}
	
	private static Model plsaParser(Node node){
		int steps = Integer.parseInt(node.getChildNodes().item(1).getTextContent());
		int classes = Integer.parseInt((node.getChildNodes().item(3).getTextContent()));
		int holdoutCases = Integer.parseInt((node.getChildNodes().item(5).getTextContent()));
		System.out.printf("PLSA: %d %d %d\n", steps, classes, holdoutCases);
		
		return new PLSA(steps, classes);
	}
}
