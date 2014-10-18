import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

// 70 minutes + 2hrs 36 minutes + 2hrs
// start 12:15 PM - 4:50PM
// start 6:01PM
/************************************* Second Version *********************************/
public class LLgen {

	// location of the input file
	private static String filePath = "";

	// Start symbol of the grammar
	private static String startSym = "";

	// Map key non-Terminal for productions map
	private static String nonTermKey = "";

	// Maps the number of times a non-Terminal appears to determine if a ' is need on RHS
	private static HashMap<String, Integer> nonTerminalCount = new HashMap<String, Integer>();

	// Grammar table for converting strings into symbols
	private static HashMap<String, String> grammarTable = new HashMap<String, String>();

	// HashMap holding the initial lines and the grammar
	private static HashMap<Integer, ArrayList<String>> grammarLines =
			new HashMap<Integer, ArrayList<String>>();

	// Set holding all the non-terminals
	private static Set<String> genNonTerminals = new HashSet<String>();

	// Set holding created productions
	private static Set<String> createdProduction = new HashSet<String>();

	// Index for the final production map
	private static int productionMapIndex = 0;

	// The final production map
	private static HashMap<Integer, ArrayList<String>> finalProduction =
			new HashMap<Integer, ArrayList<String>>();
	
	// Set holding all the terminals
	private static Set<String> genTerminals = new HashSet<String>();
	

	public static void main(String[] args) {

		// String[] cmdLine = args;
		// String[] cmdLine = {"-t", "/Users/Ace/Downloads/lab2/grammars/SN-nonLL1-RR"};
		//String[] cmdLine = {"-t", "/Users/Ace/Downloads/lab2/grammars/CEG-RR"};
		 //String[] cmdLine = {"-t", "/Users/Ace/Downloads/lab2/grammars/Factor-LL1-RR"};
		// String[] cmdLine = {"-t", "/Users/Ace/Downloads/lab2/grammars/Invocation-nonLL1"};
		// String[] cmdLine = {"-t", "/Users/Ace/Downloads/lab2/grammars/LongAlternation"};
		 String[] cmdLine = {"-t", "/Users/Ace/Downloads/lab2/grammars/Parens"};
		// String[] cmdLine = {"-t", "/Users/Ace/Downloads/lab2/grammars/Parens-Alt"}; //If index(x+1) == index(x+2) then is NonFlagProduction
		// String[] cmdLine = {"-t", "/Users/Ace/Downloads/lab2/grammars/SN-RR-LL1"};
		// String[] cmdLine = {"-t", "/Users/Ace/Downloads/lab2/grammars/Useless-LL1"};
		// String[] cmdLine = {"-t", "/Users/Ace/Downloads/lab2/grammars/Useless-nonLL1"};
		// String[] cmdLine = {"-t", "/Users/Ace/Downloads/lab2/grammars/CEG-RR-Simple"};
		// String[] cmdLine = {"-t", "/Users/Ace/Downloads/lab2/grammars/LeftRecursion/CEG-ILR"};
		// String[] cmdLine = {"-t", "/Users/Ace/Downloads/lab2/grammars/LeftRecursion/CEG-LR"};
		// String[] cmdLine = {"-t",
		// "/Users/Ace/Downloads/lab2/grammars/LeftRecursion/ILR-Example1"};
		// String[] cmdLine = {"-t",
		// "/Users/Ace/Downloads/lab2/grammars/LeftRecursion/ILR-Example2"};

		// Reads the command line and sets up parameters
		readCommandLine(cmdLine);

		// Fill the grammar table
		fillGrammarTable();

		// Place the grammar into the data structure
		fillGrammarLines(filePath);

		// Generate Output path for top Yamal
		// printTopOutput(filePath);

		System.out.println("//Finished.");
	}
	
	public static void LLSkeletonParser(){
		Iterator<String> itNonTerm;
		Stack<String> nonTermStack = new Stack<String>();
		itNonTerm = genNonTerminals.iterator();
		String TopOfStack = "";
		while(itNonTerm.hasNext()){
			nonTermStack.add(itNonTerm.next());
		}
		
		nonTermStack.add("EOF");
		TopOfStack = nonTermStack.pop();
		while(true){
			if(TopOfStack.contains("EOF")){
				break;
			}else{
				TopOfStack = nonTermStack.pop();
			}
			
			
		}
		return;
	}

	public static void fillGrammarLines(String inputFile) {
		ArrayList<String> gramLine = new ArrayList<String>();
		String line = "", word = "";
		boolean boStartSymbol = true;

		// KeyIndexCounter for debugging
		int lineCounter = 0;



		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			// read in each line from the block
			while ((line = reader.readLine()) != null) {

				for (int i = 0; i < line.length(); i++) {

					// The end of the line has been reached, create a new line
					if (line.charAt(i) == ';') {
						grammarLines.put(lineCounter, gramLine);
						// Increment the line count
						lineCounter++;
						gramLine = new ArrayList<String>();
					}

					// Build the word
					if (Character.isAlphabetic(line.charAt(i))) {
						word += line.charAt(i);

						// Check the bounds
						if (i + 1 < line.length()) {

							// White space following character indicates a word
							if (Character.isWhitespace(line.charAt(i + 1))
									|| line.charAt(i + 1) == ';') {

								// Add Term, if at line 0 add the start symbol
								gramLine.add(word);
								// The start symbol found
								if(boStartSymbol){
									startSym += word;
									boStartSymbol = false;
								}
								// Clear the word
								word = "";
							}
						} else if (i + 1 == line.length()) {
							// gets terminals at the end of the line
							gramLine.add(word);

						}
					}

					// Add the | symbol to the arrayList
					if (line.charAt(i) == '|') {
						gramLine.add(Character.toString(line.charAt(i)));
					}
				}
				// clear the word
				word = "";


			}
			reader.close();
		} catch (Exception e) {
			System.err.format("Exception occurred trying to read '%s'.", inputFile);
			e.printStackTrace();
		}

		parseTheGrammar();
		
		// Test to see if everything is there // Fill in the Terminals
		for (int i = 0; i < finalProduction.size(); i++) {
			gramLine = finalProduction.get(i);
			for (int j = 0; j < gramLine.size(); j++) {
				if(j != 0 && !genNonTerminals.contains(gramLine.get(j).trim())){
					genTerminals.add(gramLine.get(j).trim());
				}
				//System.out.print(gramLine.get(j) + " " );
			}
			//System.out.println("");
		}
		String strNonTerminals = "[";
		Iterator<String> thisNonTermi = genNonTerminals.iterator();
		while(thisNonTermi.hasNext()){
			strNonTerminals += thisNonTermi.next();
			if(thisNonTermi.hasNext()){
				strNonTerminals += ", ";
			}
		}
		strNonTerminals += "]";
		
		Iterator<String> iterTerminals = genTerminals.iterator();
		String strTerminals = "[", strSym = "";
		while(iterTerminals.hasNext()){
			strSym = grammarsTable(iterTerminals.next());
			if(strSym.equals("")){
				continue;
			}
			strTerminals += strSym;
			if(iterTerminals.hasNext()){
				strTerminals += ", ";
			}
		}
		strTerminals += "]";
		String strProduction = "";
		System.out.println("terminals: "+ strTerminals);
		System.out.println("non-terminals: "+ strNonTerminals);
		System.out.println("eof-maker: <EOF>");
		System.out.println("error-maker: --");
		System.out.println("start-symbol: "+ startSym);
		System.out.println("");
		System.out.println("productions: ");
		for (int i = 0; i < finalProduction.size(); i++) {
			strProduction += "\t"+ Integer.toString(i)+":\t";
			gramLine = finalProduction.get(i);
			for (int j = 0; j < gramLine.size(); j++) {
				if(j == 0){
					strProduction += "{"+gramLine.get(j).trim()+": [";
					continue;
				}
				if(j +1 == gramLine.size()){
					if(gramLine.size() ==2 && grammarsTable(gramLine.get(j).trim()) == ""){
						strProduction += "]}";
						continue;
					}
					if(grammarsTable(gramLine.get(j).trim()).equals("")){
						continue;
					}else{
						strProduction += grammarsTable(gramLine.get(j).trim())+ "]}";
					}
				}else{
					strProduction += grammarsTable(gramLine.get(j).trim())+", ";
				}
			}
			System.out.println(strProduction);
			strProduction = "";
			//System.out.println("");
		}
		
	}

	public static void parseTheGrammar() {
		ArrayList<String> gramLine;
		boolean flag = false, match = false, nonMatch = true;
		String flagWord = "", matchWord = "";
		int flagIndex = -1;
		for (int i = 0; i < grammarLines.size(); i++) {

			flag = false;
			flagWord = "";
			gramLine = grammarLines.get(i);
			for (int j = 0; j < gramLine.size(); j++) {
				// Indicates that flag word has not been found
				if (flag == false) {
					flag = determineIfInGrammarTable(gramLine.get(j));
					flagWord = gramLine.get(j);
					flagIndex = j;
				}

				// Looks for a match between non-Terminal and LR grammar
				if (j != 0 && gramLine.get(0).contains(gramLine.get(j))) {
					matchWord = gramLine.get(j);
					
					match = true;
					
				}

				if (flag && match) {
					// System.out.print("*****Match.***");
					
					buildProductions(gramLine, flagIndex);
					nonMatch = false;
				} 

				// Adds to the collection of non-terminals
				determineIfNonTerm(gramLine.get(j), j);
				// System.out.print(gramLine.get(j) + " ");

				match = false;
			}
			//System.out.println("");
			if(nonMatch){
				buildNonFlagProductions(gramLine);
			}
			nonMatch = true;
		}
	}

	public static void buildNonFlagProductions(ArrayList<String> gramLine) {
		String productionString = "";
		ArrayList<String> productionArrayList = new ArrayList<String>();
		ArrayList<ArrayList<String>> mapOfMaps = new ArrayList<ArrayList<String>>();
		ArrayList<String> mapOfStrings = new ArrayList<String>();

		for (int x = 0; x < gramLine.size(); x++) {
			// Handle cases when "|" appears
			if (gramLine.get(x).contains("|")) {
				mapOfMaps.add(productionArrayList);
				mapOfStrings.add(productionString);
				productionString = "";

				productionArrayList = new ArrayList<String>();
				productionString += gramLine.get(0) + " ";
				productionArrayList.add(gramLine.get(0));
				continue;
			}

			productionString += gramLine.get(x) + " ";
			productionArrayList.add(gramLine.get(x) + " ");

			if (x == 0) {
				if (!genNonTerminals.contains(gramLine.get(0))) {
					genNonTerminals.add(gramLine.get(0));
				}
			}
			
			if (x == gramLine.size() - 1 && !createdProduction.contains(productionString)) {
				 //System.out.println(">>>>>>>>>>>> \t"+productionString);
				 createdProduction.add(productionString);
				 
				finalProduction.put(productionMapIndex, productionArrayList);
				productionMapIndex += 1;
			}
		}
		
		for (int i = 0; i < mapOfStrings.size(); i++) {
			if (!createdProduction.contains(mapOfStrings.get(i))) {
				createdProduction.add(mapOfStrings.get(i));
				
				
				finalProduction.put(productionMapIndex, mapOfMaps.get(i));
				productionMapIndex += 1;
			}
		}

	}

	public static void buildProductions(ArrayList<String> gramLine, int flagIndex) {
		String productionString = "", productionString2 = "";
		ArrayList<String> productionArrayList = new ArrayList<String>();
		ArrayList<String> productionArrayListTop = new ArrayList<String>();

		ArrayList<ArrayList<String>> mapOfMaps = new ArrayList<ArrayList<String>>();
		ArrayList<String> mapOfStrings = new ArrayList<String>();

		ArrayList<ArrayList<String>> mapOfMaps2 = new ArrayList<ArrayList<String>>();
		ArrayList<String> mapOfStrings2 = new ArrayList<String>();

		// Feed the grammar line for production list creation
		for (int x = 0; x < gramLine.size(); x++) {

			// Handle cases when "|" appears
			if (gramLine.get(x).contains("|")) {
				mapOfMaps.add(productionArrayListTop);
				mapOfStrings.add(productionString);
				productionString = "";

				mapOfMaps2.add(productionArrayList);
				mapOfStrings2.add(productionString2);
				productionString2 = "";


				productionArrayList = new ArrayList<String>();
				productionArrayListTop = new ArrayList<String>();

				if (genNonTerminals.contains(gramLine.get(0) + "'")) {
					productionString += gramLine.get(0) + "' ";
					productionString2 += gramLine.get(0) + "' ";

					productionArrayList.add(gramLine.get(0) + "' ");
					productionArrayListTop.add(gramLine.get(0) + "' ");
				} else {
					productionString += gramLine.get(0) + " ";
					productionString2 += gramLine.get(0) + " ";
				}

				continue;
			}
			// Used to test for membership in productions
			if (x != flagIndex) {
				if (gramLine.get(0).contains(gramLine.get(x)) && x != 0) {
					productionString += gramLine.get(x) + "'";
					productionArrayListTop.add(gramLine.get(x) + "'");

				} else {
					productionString += gramLine.get(x) + " ";
					productionArrayListTop.add(gramLine.get(x) + " ");
				}
			}

			// Bottom portion
			if (x == 0 || gramLine.get(0).contains(gramLine.get(x))) {
				productionArrayList.add(gramLine.get(x) + "'");
				productionString2 += gramLine.get(x) + "' ";
				if (!genNonTerminals.contains(gramLine.get(x) + "'")) {
					genNonTerminals.add(gramLine.get(x) + "'");
				}
			} else {
				productionArrayList.add(gramLine.get(x));
				productionString2 += gramLine.get(x) + " ";
			}
			if (x == gramLine.size() - 1 &&!createdProduction.contains(productionString2)) {
				// System.out.println(productionString2);
				createdProduction.add(productionString2);
				finalProduction.put(productionMapIndex, productionArrayList);
				productionMapIndex += 1;
			}
		}
		for (int i = 0; i < mapOfStrings.size(); i++) {
			if (!createdProduction.contains(mapOfStrings.get(i))) {
				createdProduction.add(mapOfStrings.get(i));
				
				
				finalProduction.put(productionMapIndex, mapOfMaps.get(i));
				productionMapIndex += 1;
			}
		}
		for (int j = 0; j < mapOfStrings2.size(); j++) {
			if (!createdProduction.contains(mapOfStrings2.get(j))) {
				createdProduction.add(mapOfStrings2.get(j));
				
				
				finalProduction.put(productionMapIndex, mapOfMaps2.get(j));
				productionMapIndex += 1;
			}
		}
	}

	
	public static boolean determineIfInGrammarTable(String word) {
		if (grammarTable.containsKey(word)) {
			return true;
		}
		return false;
	}

	public static void determineIfNonTerm(String word, int index) {
		if (index == 0) {
			genNonTerminals.add(word);
		}
	}

	/**
	 * Prints the require YAML format for the top output portion.
	 */
	public static void printTopOutput(String inputFile) {
		String line = "", word = "", nonTermStr = "[", termStr = "[";
		HashSet<String> nonTerminals = new HashSet<String>();
		HashSet<String> terminals = new HashSet<String>();
		Iterator<String> iterNonTerms;
		Iterator<String> iterTerms;
		// KeyIndexCounter for debugging
		int colonIndex = -1, lineCounter = 0, keyIndexCounter = 0, productionCounter = 0;

		// Container for the productions
		ArrayList<String> productionLists = new ArrayList<String>();

		String value = "";

		boolean startOfProduction = false, foundStartSym = true;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			// read in each line from the block
			while ((line = reader.readLine()) != null) {

				colonIndex = line.indexOf(":");

				for (int i = 0; i < line.length(); i++) {

					// increment the map key index counter for the productions map
					if (line.charAt(i) == '|' || line.charAt(i) == ';') {
						keyIndexCounter++;
					}

					// Build the word
					if (Character.isAlphabetic(line.charAt(i))) {
						word += line.charAt(i);

						// Check the bound
						if (i + 1 < line.length()) {

							// White space following character indicates a word
							if (Character.isWhitespace(line.charAt(i + 1))
									|| line.charAt(i + 1) == ';') {

								// Add Term, if at line 0 add the start symbol
								if (!nonTerminals.contains(word)) {
									terminals.add(word);
									if (lineCounter == 0 && foundStartSym) {
										startSym = word;
										foundStartSym = false;
									}
								}

								// Add the Non-terminal to the set
								if (i < colonIndex && !nonTerminals.contains(word)) {
									nonTerminals.add(word);
									terminals.remove(word);
									nonTermKey = word;

									// Increments the frequency for the word
									updateNonTerminalCount(word);

									// System.out.println("Non-terminal at line: \t " +
									// keyIndexCounter + "\t" + nonTermKey);
									startOfProduction = true;
								}

								// Fill in the productions
								if (nonTerminals.contains(word)) {
									nonTermKey = word;
									// Increments the frequency for the word
									updateNonTerminalCount(nonTermKey);
								}

								if (startOfProduction) {

									// Increments the frequency for the word
									updateNonTerminalCount(nonTermKey);

									startOfProduction = false;
									value += "{" + nonTermKey + ": " + "[";
								} else if (nonTerminals.contains(word) && !line.contains("|")) {

									// Increments the frequency for the word
									updateNonTerminalCount(nonTermKey);

									if (value.length() == 0) {
										value += "{" + nonTermKey + "'" + ": " + "[";
									}
								} else if (line.contains("|")) {

									// Increments the frequency for the word
									updateNonTerminalCount(nonTermKey);

									// if(value.length() == 0){
									value += "{" + nonTermKey + "'" + ": " + "[";
									// }
								}


								if (i + 2 == line.length()) {

									// Convert the word if needed
									word = grammarsTable(word);

									// Formats the production table correctly
									if (nonTerminalCount.containsKey(word)) {


										if (nonTerminalCount.get(word) > 1) {
											word += "'";
										}
									}
									value += word + "]}";
								} else if (i > colonIndex) {

									// Convert the word if needed
									word = grammarsTable(word);

									// Formats the production table correctly
									if (nonTerminalCount.containsKey(word)) {
										if (nonTerminalCount.get(word) > 1) {
											word += "'";
										}
									}
									value += word + ", ";
								}

								word = "";
							}
						} else if (i + 1 == line.length()) {
							// gets terminals at the end of the line
							if (!nonTerminals.contains(word)) {
								terminals.add(word);
							}
							// System.out.println("The last word: \t " + word);

							// Convert the word if needed
							word = grammarsTable(word);

							// Formats the production table correctly
							if (nonTerminalCount.containsKey(word)) {
								if (nonTerminalCount.get(word) > 1) {
									word += "'";
								}
							}
							value += word + "]}";
						}
					}
				}
				// clear the word
				word = "";

				// Clear the non-terminal count
				nonTerminalCount.clear();

				if (!value.equals("")) {
					// System.out.println("This is value: \t " + value);
					// Add the production for printing
					productionLists.add("\t" + Integer.toString(productionCounter) + ":" + "\t"
							+ value);
					productionCounter += 1;
					// System.out.println(nonTerminalCount.get("Expr"));
				}

				value = "";
				lineCounter++;
				// System.out.println("Line number for map key: \t " + keyIndexCounter);
			}
			reader.close();
		} catch (Exception e) {
			System.err.format("Exception occurred trying to read '%s'.", inputFile);
			e.printStackTrace();
		}

		iterTerms = terminals.iterator();
		iterNonTerms = nonTerminals.iterator();

		// Build string vector for terminals, has nothing to do with productions
		while (iterTerms.hasNext()) {
			// Convert the word if needed
			String theWord = grammarsTable(iterTerms.next());
			termStr += theWord;
			if (iterTerms.hasNext()) {
				if (theWord != "") {
					termStr += ", ";
				}
			}
		}
		termStr += "]";

		// Build string vector for non terminals, has nothing to do with productions
		while (iterNonTerms.hasNext()) {
			nonTermStr += iterNonTerms.next();
			if (iterNonTerms.hasNext()) {
				nonTermStr += ", ";
			}
		}
		nonTermStr += "]";

		// Prints out the header for the scanner
		System.out.println("terminals: " + termStr);
		System.out.println("non-terminals: " + nonTermStr);
		System.out.println("eof-marker: <EOF>");
		System.out.println("error-marker: --");
		System.out.println("start-symbol: " + startSym);
		System.out.println(" ");
		// System.out.println(" ");

		// Prints out all the productions in map form
		System.out.println("productions:");
		for (int i = 0; i < productionLists.size(); i++) {
			System.out.println(productionLists.get(i));
		}
		System.out.println(" ");

		// Prints out the table in map of maps form
		System.out.println("table:");
		iterTerms = terminals.iterator();
		iterNonTerms = nonTerminals.iterator();
		ArrayList<String> theNonTerms = new ArrayList<String>();
		ArrayList<String> theTerms = new ArrayList<String>();
		String mapOfMap = "";
		// Write non-terminals to the arraylist
		while (iterNonTerms.hasNext()) {
			theNonTerms.add(iterNonTerms.next());
		}
		// Write terminals to the arraylist
		while (iterTerms.hasNext()) {
			theTerms.add(grammarsTable(iterTerms.next()));
		}
		for (int i = 0; i < theNonTerms.size(); i++) {
			mapOfMap += "\t" + theNonTerms.get(i) + ": {";
			for (int j = 0; j < theTerms.size(); j++) {
				if (theTerms.get(j) == "") {
					mapOfMap += "<EOF>" + ": --,";
				} else {
					mapOfMap += theTerms.get(j) + ": --,";
				}
			}
			mapOfMap += "}";
			System.out.println(mapOfMap);
			mapOfMap = "";
		}

	}

	public static void fillGrammarTable() {
		grammarTable.put("LP", "(");
		grammarTable.put("RP", ")");
		grammarTable.put("LB", "{");
		grammarTable.put("RB", "}");
		grammarTable.put("TIMES", "*");
		grammarTable.put("PLUS", "+");
		grammarTable.put("MINUS", "-");
		grammarTable.put("DIV", "/");
		grammarTable.put("epsilon", "");
		grammarTable.put("Epsilon", "");
		grammarTable.put("EPSILON", "");
		grammarTable.put("COMMA", ",");
		grammarTable.put("LParen", "(");
		grammarTable.put("RParen", ")");
		grammarTable.put("DIVIDE", "/");
	}

	public static String grammarsTable(String word) {
		if (grammarTable.containsKey(word)) {
			return grammarTable.get(word);
		} else {
			return word;
		}
	}

	public static void updateNonTerminalCount(String nonTerm) {
		int currentCount = 0;
		if (nonTerminalCount.containsKey(nonTerm)) {
			currentCount = nonTerminalCount.get(nonTerm);
			nonTerminalCount.put(nonTerm, currentCount += 1);
		} else {
			nonTerminalCount.put(nonTerm, currentCount);
		}
	}

	/**
	 * Finds the file and reads command line parameters for set-up.
	 * 
	 * @param cmdLine: command line for the program
	 */
	public static void readCommandLine(String[] cmdLine) {
		int numArgs = cmdLine.length;
		int fileCount = 0;

		// Set of the acceptable commands in command line
		HashSet<String> okayCmds = new HashSet<String>();
		okayCmds.add("-h");
		okayCmds.add("-t");
		okayCmds.add("-s");
		// This is only valid if you have an implementation that removes left-recursion
		// okayCmds.add("-r");

		// Check the command line to see if there are any invalid commands
		for (int i = 0; i < numArgs; i++) {
			// Check to see if there is a dash which indicates that it is a command
			if (cmdLine[i].contains("-") && cmdLine[i].length() == 2) {

				// Prints out the acceptable command line arguments
				if (cmdLine[i].contains("-h")) {
					cmdLinePrintMessages();
					System.exit(0);
				}
				// Terminates program if invalid command is found
				if (!okayCmds.contains(cmdLine[i])) {
					System.out.println("Invalid command.");
					System.exit(0);
				}
			} else {
				filePath = cmdLine[i];
				fileCount++;
			}
		}

		// Check to see if more than one file was found
		if (fileCount != 1) {
			System.out.println("Invalid file input. Found " + fileCount
					+ " different file locations.");
			System.exit(0);
		}
		// Check if the file exists
		File f = new File(filePath);
		if (!f.exists() || f.isDirectory()) {
			System.out.println("Failed to open '" + filePath + "' as the input file.");
			System.exit(0);
		}
	}

	/**
	 * Prints to the console the command line messages.
	 */
	public static void cmdLinePrintMessages() {
		System.out.println("-h \t prints this message. \n");

		System.out
				.println("-t \t Produces the LL(1) table in YAML format in stdout. If the input grammar contains errors,"
						+ " the –t flag prints an informative error message to stderr. If the input grammar does not have the LL(1) property, prints an informative error"
						+ " message to stderr in lieu of printing the LL(1) table to stdout. The error message states which productions"
						+ " in the grammar created the problem. \n");

		System.out
				.println("-s \t Prints out in a human readable form to stdout the following:"
						+ " the productions, as recognized by the parser, the FIRST sets for each grammar symbol"
						+ "the FOLLOW sets for each nonterminal, and the FIRST+ sets for each production. \n");
	}

}
