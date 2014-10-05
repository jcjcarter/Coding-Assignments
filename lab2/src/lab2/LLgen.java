package lab2;

import java.io.File;
import java.util.HashSet;
//70 minutes
// start 1:24 PM
public class LLgen {

	public static void main(String[] args) {

		// String[] cmdLine = args;
		String[] cmdLine = {"-h", "w", "u"};
		
		//Reads the command line and sets up parameters
		readCommandLine(cmdLine);

		System.out.println("//Finished.");
	}
	
	/**
	 * Finds the file and reads command line parameters for
	 * set-up.
	 * @param cmdLine: command line for the program
	 */
	public static void readCommandLine(String[] cmdLine){
		String filePath = "";
		int cmdLineLen = cmdLine.length;
		int fileCount = 0;

		// Set of the acceptable commands in command line
		HashSet<String> okayCmds = new HashSet<String>();
		okayCmds.add("-h");
		okayCmds.add("-t");
		okayCmds.add("-s");
		//This is only valid if you have an implementation that removes left-recursion
		//okayCmds.add("-r"); 

		// Check the command line to see if there are any invalid commands
		for (int i = 0; i < cmdLineLen; i++) {
			// Check to see if there is a dash which indicates that it is a command
			if (cmdLine[i].contains("-")) {
				
				//Prints out the acceptable command line arguments
				if(cmdLine[i].contains("-h")){
					cmdLinePrintMessages();
					System.exit(0);
				}
				//Terminates program if invalid command is found
				if (!okayCmds.contains(cmdLine[i])) {
					System.out.println("Invalid command.");
					System.exit(0);
				}
			} else {
				filePath = cmdLine[i];
				fileCount++;
			}
		}
		
		//Check to see if more than one file was found
		if (fileCount != 1) {
			System.out.println("Invalid file input. Found "+fileCount+" different file locations.");
			System.exit(0);
		}
		
		File f = new File(filePath);
	    if (!f.exists() || f.isDirectory()) {
	      System.out.println("Failed to open '" + filePath + "' as the input file.");
	      System.exit(0);
	    }
	}
	
	/**
	 * Prints to the console the command line messages.
	 */
 	public static void cmdLinePrintMessages(){
		System.out.println("-h \t prints this message. \n");
		
		System.out.println("-t \t Produces the LL(1) table in YAML format in stdout. If the input grammar contains errors,"
				+ " the –t flag prints an informative error message to stderr. If the input grammar does not have the LL(1) property, prints an informative error"
				+ " message to stderr in lieu of printing the LL(1) table to stdout. The error message states which productions"
				+ " in the grammar created the problem. \n");
		
		System.out.println("-s \t Prints out in a human readable form to stdout the following:"
				+ " the productions, as recognized by the parser, the FIRST sets for each grammar symbol"
				+ "the FOLLOW sets for each nonterminal, and the FIRST+ sets for each production. \n");
	}

}