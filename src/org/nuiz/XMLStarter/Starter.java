package org.nuiz.XMLStarter;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.nuiz.guessUserMean.GuessZero;
import org.nuiz.itemBasedCF.ItemBasedCF;
import org.nuiz.leastSquaresBaseline.LeastSquaresBaseline;
import org.nuiz.parallelPLSA.PLSA;
import org.nuiz.parallelRecommend.*;
import org.nuiz.slopeOne.SlopeOne;
import org.nuiz.svd.SVDModel;
import org.nuiz.userBasedCF.UserBasedCF;
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
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		String fname = args[0];
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder(); 
		Document doc = db.parse(new File(fname));
		
		configParser(doc.getChildNodes().item(0));
		
		GlobalSettings.getExecutor().shutdown();
	}
	
	private static void configParser(Node config) throws Exception {
		NodeList nl = config.getChildNodes();
		
		GlobalSettings.parseGlobalSettings(nl.item(1));
		
		for (int i = 2; i < nl.getLength(); i++) {
			String nodeName = nl.item(i).getNodeName();
			if (nodeName.equals("holdOut") || 
					nodeName.equals("crossVal") || 
					nodeName.equals("examineUser")) {
				runnableParser(nl.item(i));
			} else if (nodeName.equals("multiHoldOut")) {
				multiHoldoutParser(nl.item(i));
			}
		}
	}

	private static void runnableParser(Node runnableNode) throws Exception {
		NodeList nl = runnableNode.getChildNodes();
		String outFname = nl.item(1).getTextContent();
		DataList dataList = dataListParser(nl.item(3));
		Model model = modelParser(nl.item(5));
		
		OutputStream os = outFname.equals("-") ? System.out : new FileOutputStream(outFname, true);
		
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
	
	private static void multiHoldoutParser(Node runnableNode) throws InterruptedException, ExecutionException, IOException {
		NodeList nl = runnableNode.getChildNodes();
		String outFname = nl.item(1).getTextContent();
		DataList dataList = dataListParser(nl.item(3));
		int randomSeed = Integer.parseInt(nl.item(5).getTextContent());
		int testSize = Integer.parseInt((nl.item(7).getTextContent()));
		OutputStream os = outFname.equals("-") ? System.out : new FileOutputStream(outFname, true);
		
		ThreadPoolExecutor ex = GlobalSettings.getExecutor();
		
		class HoldOutRunnable implements Runnable {
			DataList dataList;
			Model model;
			int randomSeed;
			int testSize;
			OutputStream os;
			public HoldOutRunnable (DataList dl, Model m, int rs, int s, OutputStream o) {
				dataList = dl;
				model = m;
				randomSeed = rs;
				testSize = s;
				os = o;
			}
			
			@Override
			public void run() {
				try {
					new HoldoutRunner(dataList, model, randomSeed, testSize, os);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
		Vector<Future<?>> tasks = new Vector<Future<?>>();
		for (int i = 9; i < nl.getLength(); i += 2){
			Model model = modelParser(nl.item(i));
			tasks.add(ex.submit(new HoldOutRunnable(dataList, model, randomSeed, testSize, os)));	
		}
		
		for (Future <?> f : tasks){
			try {
				f.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException();
			} catch (ExecutionException e) {
				e.printStackTrace();
				throw new RuntimeException();
			}
		}
	}
	
	
	
	private static void holdOutParser(OutputStream outStream, DataList dataList, Model model, Node node) throws Exception {
		int randomSeed = Integer.parseInt(node.getChildNodes().item(7).getTextContent());
		int testSize = Integer.parseInt((node.getChildNodes().item(9).getTextContent()));
		//System.out.printf("holdout: %d %d\n", randomSeed, testSize);
		
		new HoldoutRunner(dataList, model, randomSeed, testSize, outStream);
	}
	
	private static void crossValParser(OutputStream outStream, DataList dataList, Model model, Node node) throws Exception {
		int randomSeed = Integer.parseInt(node.getChildNodes().item(7).getTextContent());
		int folds = Integer.parseInt((node.getChildNodes().item(9).getTextContent()));
		//System.out.printf("crossVal: %d %d\n", randomSeed, folds);
		new CVRunner(dataList, model, randomSeed, folds, outStream);
	}
	
	private static void examineUserParser(OutputStream outStream, DataList dataList, Model model, Node node) throws Exception {
		int userNumber = Integer.parseInt(node.getChildNodes().item(7).getTextContent());
		String moviesFile = node.getChildNodes().item(9).getTextContent();
		String separator = node.getChildNodes().item(11).getTextContent();
		//System.out.printf("examineUser: %d\n", userNumber);
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
	
	private static Model modelParser(Node modelNode) throws IOException {
		if (modelNode.getNodeName().equals("plsa")) {
			return plsaParser(modelNode);
		} else if (modelNode.getNodeName().equals("plsaUserData")) {
			return plsaUserDataParser(modelNode);
		} else if (modelNode.getNodeName().equals("slopeOne")) {
			return new SlopeOne();
		} else if (modelNode.getNodeName().equals("guessZero")) {
			return new GuessZero();
		} else if (modelNode.getNodeName().equals("itemBased")) {
			return itemBasedParser(modelNode);
		} else if (modelNode.getNodeName().equals("userBased")) {
			return userBasedParser(modelNode);
		} else if (modelNode.getNodeName().equals("svd")) {
			return svdParser(modelNode);
		} else if (modelNode.getNodeName().equals("leastSquaresBaseline")) {
			return svdppParser(modelNode);
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
		//System.out.printf("PLSA: %d %d %d\n", steps, classes, holdoutCases);
		
		return new PLSA(steps, classes);
	}	
	
	private static Model plsaUserDataParser(Node node) throws IOException{
		int steps = Integer.parseInt(node.getChildNodes().item(1).getTextContent());
		int classes = Integer.parseInt((node.getChildNodes().item(3).getTextContent()));
		String userFileName  = node.getChildNodes().item(7).getTextContent();
		//System.out.printf("PLSA: %d %d %d\n", steps, classes, holdoutCases);
		DataList uData = new UserFileDataList(userFileName, "::");
		
		return new PLSA(steps, classes, uData);
	}
	
	private static Model itemBasedParser(Node node){
		NodeList nl = node.getChildNodes();
		String simType = nl.item(1).getNodeName();
		int hoodSize = Integer.parseInt(node.getChildNodes().item(3).getTextContent());
		ItemBasedCF.SimType st;
		if (simType.equals("cosineSimilarity")) {
			st = ItemBasedCF.SimType.COSINE_SIM;
		} else if (simType.equals("adjustedCosineSimilarity")) {
			st = ItemBasedCF.SimType.ADJUSTED_COSINE_SIM;
		} else if (simType.equals("correlationSimilarity")) {
			st = ItemBasedCF.SimType.CORRELATION_SIM;
		} else {
			throw new IllegalArgumentException();
		}
		
		return new ItemBasedCF(st, hoodSize);
	}
	
	private static Model userBasedParser(Node node){
		NodeList nl = node.getChildNodes();
		String simType = nl.item(1).getNodeName();
		int hoodSize = Integer.parseInt(node.getChildNodes().item(3).getTextContent());
		UserBasedCF.SimType st;
		if (simType.equals("cosineSimilarity")) {
			st = UserBasedCF.SimType.COSINE_SIM;
		} else if (simType.equals("adjustedCosineSimilarity")) {
			st = UserBasedCF.SimType.ADJUSTED_COSINE_SIM;
		} else if (simType.equals("correlationSimilarity")) {
			st = UserBasedCF.SimType.CORRELATION_SIM;
		} else {
			throw new IllegalArgumentException();
		}
		
		return new UserBasedCF(st, hoodSize);
	}
	
	private static Model svdParser(Node node){
		NodeList nl = node.getChildNodes();
		int steps = Integer.parseInt(node.getChildNodes().item(1).getTextContent());
		int classes = Integer.parseInt(node.getChildNodes().item(3).getTextContent());
		Double lambda = Double.parseDouble((nl.item(5).getTextContent()));
		Double gamma = Double.parseDouble((nl.item(7).getTextContent()));
		
		return new SVDModel(steps, classes, lambda, gamma);
	}
	
	private static Model svdppParser(Node node){
		NodeList nl = node.getChildNodes();
		Double lambda1 = Double.parseDouble((nl.item(1).getTextContent()));
		
		return new LeastSquaresBaseline(lambda1);
	}
}
