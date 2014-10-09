package lab2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
// 70 minutes + 2hrs 36 minutes
// start 2:32 AM
import java.util.Iterator;

public class LLgen {

	// location of the input file
	private static String filePath = "";
	//Start symbol of the grammar
	private static String startSym = "";
	//Map key non-Terminal for productions map
	private static String nonTermKey = "";

	public static void main(String[] args) {

		// String[] cmdLine = args;
		//String[] cmdLine = {"-t", "/Users/Ace/Downloads/lab2/grammars/SN-nonLL1-RR"};
		String[] cmdLine = {"-t", "/Users/Ace/Downloads/lab2/grammars/CEG-RR"};

		// Reads the command line and sets up parameters
		readCommandLine(cmdLine);

		// Generate Output path for top Yamal
		printTopOutput(filePath);

		System.out.println("//Finished.");
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
		int colonIndex = -1, lineCounter = 0, keyIndexCounter = 0;
		
		String value = "";
		
		boolean startOfProduction = false;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			// read in each line from the block
			while ((line = reader.readLine()) != null) {
				
				colonIndex = line.indexOf(":");
				
				for (int i = 0; i < line.length(); i++) {
					
					//increment the may key index counter for the productions map
					if(line.charAt(i) == '|' || line.charAt(i) == ';'){keyIndexCounter++;}
					
					// Build the word
					if (Character.isAlphabetic(line.charAt(i))) {
						word += line.charAt(i);
						
						//Check the bound
						if (i + 1 < line.length()) {
							
							//White space following character indicates a word
							if (Character.isWhitespace(line.charAt(i + 1))|| line.charAt(i+1) == ';') {
								
								//Add Term, if at line 0 add the start symbol
								if(!nonTerminals.contains(word)){	terminals.add(word); if(lineCounter == 0){startSym += word;}}
																
								// Add the Non-terminal to the set
								if (i < colonIndex && !nonTerminals.contains(word)) {
									nonTerminals.add(word);
									terminals.remove(word);
									nonTermKey = word;
									//System.out.println("Non-terminal at line: \t " + keyIndexCounter + "\t" + nonTermKey);
									startOfProduction = true;
								}
								
								//Fill in the productions
								if(startOfProduction){
									startOfProduction = false;
									value +="{" + nonTermKey + ": " + "[";
								}else if(nonTerminals.contains(word)&&!line.contains("|")){
									value +="{" + nonTermKey+"'" + ": " + "[";
								}//else if(nonTerminals.contains(word)&& i )
								
								
								
								if(i+ 2 == line.length()){value+= word+"]}";
								}else if(i > colonIndex){ value+= word+ ", ";}
								
								word = "";
							}
						}else if(i + 1 == line.length()){
							//gets terminals at the end of the line
							if(!nonTerminals.contains(word)){	terminals.add(word); }
							//System.out.println("The last word: \t " + word);
							value+= word+"]}";
						}
					}
				}
				// clear the word
				word = "";
				//System.out.println(" ");
				System.out.println("This is value: \t "+value);
				//System.out.println(" ");
				value = "";
				lineCounter++;
				//System.out.println("Line number for map key: \t " + keyIndexCounter);
			}
			reader.close();
		} catch (Exception e) {
			System.err.format("Exception occurred trying to read '%s'.", inputFile);
			e.printStackTrace();
		}
		
		 iterTerms= terminals.iterator();
		 iterNonTerms = nonTerminals.iterator();
		 
		 //Build string vector for terminals
		while(iterTerms.hasNext()){
			termStr += iterTerms.next();
			if(iterTerms.hasNext()){
				termStr += ", ";
			}
		}
		termStr += "]";
		
		//Build string vector for non terminals
		while(iterNonTerms.hasNext()){
			nonTermStr += iterNonTerms.next();
			if(iterNonTerms.hasNext()){
				nonTermStr += ", ";
			}
		}
		nonTermStr += "]";
		
		System.out.println("terminals: "+ termStr);
		System.out.println("non-terminals: "+ nonTermStr);
		System.out.println("eof-marker: <EOF>");
		System.out.println("error-marker: --");
		System.out.println("start-symbol: "+ startSym);
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
