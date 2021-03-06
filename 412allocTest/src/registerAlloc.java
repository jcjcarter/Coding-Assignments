import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;


/**
 * @author Ace
 *
 */
public class registerAlloc {

	// Holds the value for Operation 2 to keep it from getting overwritten
	private static String operation2PR = "";

	// Mapping to Register's furtherest next use
	private static HashMap<String, String> registerNextU = new HashMap<String, String>();

	// Locations starting at 32768 in data memory are reserved for the register allocator. The
	// allocator will store spilled values into these locations.
	private static int dataMemoryLoc = 32768;

	// Class variable for virtual and physical mappings of registers, KEY = virtural register VALUE
	// =
	// Physical Register
	private static HashMap<String, String> registerVMapped = new HashMap<String, String>();

	// Class variable that keeps track of the physicals that are still live
	private static Stack<String> registerInUse = new Stack<String>();

	// Key is the register, value is the last lined used for the register
	private static HashMap<String, String> registerLinesUsage = new HashMap<String, String>();

	// Line count for program being read
	private static int programLineCount;

	// The program in vector line form
	private static HashMap<Integer, Vector> allocationActions = new HashMap<Integer, Vector>();

	// Class variable that holds the register names and the live ranges, index 0 is the start and
	// index 1 is the end for the array
	private static HashMap<String, int[]> registerList = new HashMap<String, int[]>();

	// Class variable that holds the register names and the live ranges listed out i.e. 0, .., 100
	// in
	// an arrayList
	private static HashMap<String, ArrayList<Integer>> registerRanges =
			new HashMap<String, ArrayList<Integer>>();

	// Class variable that keeps track of X input physical registers available in a set for the
	// bottom-up algorithm
	private static Stack<String> registerAvail = new Stack<String>();

	// Class variable that holds the number of live registers for each line
	private static HashMap<Integer, Integer> maxLiveHash = new HashMap<Integer, Integer>();

	public static void main(String[] args) {

		String[] inputLine = {"8", "/Users/Ace/Downloads/HolderJar/report/report1.i"};
		 //inputLine = args;
		// Check if the file exists
		File f = new File(inputLine[1]);
		if (!f.exists() || f.isDirectory()) {
			System.out.println("Failure to open '" + inputLine[1] + "' as the input file.");
			System.exit(0);
		}

		// Check to see if the parameter -h is present
		if (hFlag(inputLine)) {
			System.exit(0);
		}

		// Look for the number of registers or throw an error if not there
		// String inputRegNumber = args[0];
		if (!generateXRegisters(inputLine[0])) {
			// System.out.println("Will attempt to read from 'stdin'.");
			System.out.println("Cannot allocate with fewer than 2 registers.");
			System.exit(0);
		}

		/**
		 * Test to see if X registers are in the set Iterator<String> allRegistersHere =
		 * registerAvail.iterator(); while(allRegistersHere.hasNext()){
		 * System.out.println(allRegistersHere.next()); }
		 */

		// Opens the file, stores the program, and prints program
		readMicrosyntax(openAndRead(inputLine[1]), programLineCount);

		/**
		 * Iterate through allocationActions, if the OpCode is Empty then skip the line. If
		 * maxLiveHash is greater than the number of registers available then spill a register.
		 * check out a virtual register to a physical register, keep track of mappings using a
		 * HashMap and pop registers from the registerAvail and placed them into the set where
		 * registerUsing. If virtual next used is empty then free the physical and place it back
		 * into registerAvail. print out the result of the vector.
		 * */
		assignPhyRegAndPrintVector();


		System.out.println("//Finished Test.");
	}


	/**
	 * Prints out a spillage to R0 due to lack of available registers.
	 * 
	 * @param physReg: Physical register which is being spilled.
	 */
	public static void printingSpillRegister(String physReg, String virtualRegister) {
		System.out.println("loadI \t" + dataMemoryLoc + "\t  => \t " + "r0"
				+ "\t //Spill (k is minimal)");
		dataMemoryLoc += 4;
		System.out
				.println("store \t" + physReg + "\t  => \t " + "r0" + "\t //Spill (k is minimal) " + virtualRegister + " => ");
	}

	/**
	 * 
	 Finds the current mapping between virtual reg to physical reg and replaces the physical reg
	 * with a memory location
	 * 
	 * @param map: registerVMapped which contains the virtual reg to physical reg maps
	 * @param SwapRegister: physical register that needs to be switched
	 * @param MemoryLocation: memory location that is going to replace the physical register
	 */
	public static void changeVRegisterMappings(HashMap<String, String> map, String SwapRegister,
			String MemoryLocation) {
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> pairs = (Map.Entry) it.next();
			// Finds the current mapping between virtual reg to phyiscal reg and
			// replaces the physical reg with a memory location
			if (pairs.getValue() == SwapRegister) {
				map.put(pairs.getKey(), MemoryLocation);
				break;
			}

		}
	}

	/**
	 * @param map: registerNextU which is hash where Key = Physical Register and Value = Next Line
	 *        number used
	 * @return
	 */
	public static String iterateRegisterNextU(HashMap<String, String> map) {
		// Test to see if all the registers are there.
		// System.out.println("\t Number of Registers here: \t" + map.size());
		int furtherestUse = -100, compareNum = -1000;
		String furtherestReg = "ERROR";
		Iterator it = map.entrySet().iterator();
		// search for a register that has the furtherest next used
		while (it.hasNext()) {
			Map.Entry<String, String> pairs = (Map.Entry) it.next();
			// If a register currently does not have a next use, return that register.
			if (pairs.getValue().contains("Empty") && pairs.getKey().contains("r")) {
				// System.out.println(pairs.getKey() + " = " + pairs.getValue());
				return pairs.getKey();
			}
			// Cannot enumerate an "Empty", just skip to the next iteration.
			if (!pairs.getValue().contains("Empty")) {
				// Keep track of the furtherest register
				compareNum = Integer.parseInt(pairs.getValue());
				if (compareNum > furtherestUse && pairs.getKey().contains("r")) {
					furtherestUse = compareNum;
					furtherestReg = pairs.getKey();
					// System.out.println(pairs.getKey() + " = " + pairs.getValue());
				}
			}
		}
		return furtherestReg;
	}

	/**
	 * Function goes through the all the vectors stored in the allocationActions, assigns physical
	 * registers, and prints to console.
	 */
	public static void assignPhyRegAndPrintVector() {
		String physicalRegister = "", VROP1 = "", VROP2 = "", VROP3 = "", lineOPCode = "";
		int numberOfLines = allocationActions.size();
		for (int i = 0; i < numberOfLines; i++) {
						
			// If there is no opcode skip the line, an indication that it is blank
			if (allocationActions.get(i).getTheOpcode().contains("Empty")) {
				continue;
			}
			
			//Operation Code for the line
			lineOPCode = allocationActions.get(i).getTheOpcode();
			
			//Virtual Register for Operation 1
			VROP1 = allocationActions.get(i).getVROp1();

			//Virtual Register for Operation 2;
			VROP2 = allocationActions.get(i).getVROp2();
			
			//Virtual Register for Operation 3
			VROP3 = allocationActions.get(i).getVROp3();
			
			
			/*if ((maxLiveHash.get(i) - registerInUse.size()) > registerAvail.size()) {
				// Perform allocations for Operations 1,2,3
			}*/

			if (lineOPCode.contains("output")) {
				System.out.println(lineOPCode + "\t "
						+ VROP1);
				continue;
			}

			/************* Operation Code 1 ************/
			if (VROP1.contains("r")) {
				performAllocationOP1(VROP1, i);

			} else {
				// This signifies that the OP1 is doing an operation from memory
				if (VROP2.contains("r")
						&& !registerVMapped.get(VROP1).contains("r")) {
					// Pick the register with the furtherest next use
					physicalRegister = iterateRegisterNextU(registerNextU);

					// Change the mapping for register Change
					changeVRegisterMappings(registerVMapped, physicalRegister,
							Integer.toString(dataMemoryLoc));


					// Spill the contents of the furtherest register to memory
					printingSpillRegister(physicalRegister, VROP1);

					// Change the mappings for the virtual and physical register
					registerVMapped.put(VROP1, physicalRegister);
					allocationActions.get(i).setPROp1(registerVMapped.get(VROP1));


				}
				// loadI is the only thing that shows up in the else

				allocationActions.get(i).setPROp1(VROP1);
			}

			/************* Operation Code 2 ************/
			if (VROP2.contains("r")) {

				performAllocationOP2(VROP2, i);

				// check to see if the virtual register is assigned to physical register
			} else {
				if (VROP2.contains("Empty")) {
					allocationActions.get(i).setPROp2("");
					operation2PR += allocationActions.get(i).getPROp2();
				} else {
					allocationActions.get(i).setPROp2(VROP2);
					operation2PR += allocationActions.get(i).getPROp2();
				}
			}

			/************* Operation Code 3 ************/
			if (VROP3.contains("r")) {
				if (registerVMapped.containsKey(VROP3)) {
					if (!registerVMapped.get(VROP3).contains("r")) {
						// Pick the register with the furtherest next use
						physicalRegister = iterateRegisterNextU(registerNextU);

						// Change the mapping for register Change
						changeVRegisterMappings(registerVMapped, physicalRegister,
								Integer.toString(dataMemoryLoc));

						// System.out.println("\t \t Program never steps into here.");

						// Spill the contents of the furtherest register to memory
						printingSpillRegister(physicalRegister, VROP3);


						// Change the mappings for the virtual and physical register
						registerVMapped.put(VROP3, physicalRegister);
						allocationActions.get(i).setPROp3(registerVMapped.get(VROP3));


					}
				}

				performAllocationOP3(VROP3, i);
				// check to see if the virtual register is assigned to physical register
			} else {

				// Pick the register with the furtherest next use
				physicalRegister = iterateRegisterNextU(registerNextU);

				// Change the mapping for register Change
				changeVRegisterMappings(registerVMapped, physicalRegister,
						Integer.toString(dataMemoryLoc));


				// Spill the contents of the furtherest register to memory
				printingSpillRegister(physicalRegister, VROP3);

				// Change the mappings for the virtual and physical register
				registerVMapped.put(VROP3, physicalRegister);
				allocationActions.get(i).setPROp3(registerVMapped.get(VROP3));


			}

			// Prints out the lines for the standard output 
			// TO operation2PR
			if (operation2PR.equals("")) {
				System.out.println(lineOPCode + "\t "
						+ allocationActions.get(i).getPROp1() + "\t " + operation2PR + " =>" + "\t"
						+ allocationActions.get(i).getPROp3()+ "\t // " + VROP1 + " " + VROP2 + " => " + VROP3);
			} else {
				System.out.println(lineOPCode + "\t "
						+ allocationActions.get(i).getPROp1() + ", " + "\t " + operation2PR + " =>"
						+ "\t" + allocationActions.get(i).getPROp3()+ "\t // " + VROP1 +" "+ VROP2+ " => " + VROP3);
			}
			operation2PR = "";

			/**
			 * Iterator map = registerVMapped.entrySet().iterator(); String tempReg = "";
			 * while(map.hasNext()){ Map.Entry pairs = (Map.Entry) map.next(); if(((String)
			 * pairs.getKey()).contains("r")){ System.out.println(pairs.getKey() + " = " +
			 * pairs.getValue()); } }
			 */

		}
	}

	public static void performAllocationOP3(String vrOP3, int index) {
		String registerChange = "SwitchRegister", physicalRegister = "", regChange = "", memPrev = "";
		boolean checked = false;

		if (registerVMapped.containsKey(vrOP3)) {
			if (!registerVMapped.get(vrOP3).contains("r")) {

				// Pick the register with the furtherest next use
				regChange += iterateRegisterNextU(registerNextU);
				// Remember where the memory came from
				memPrev += registerVMapped.get(vrOP3);
			}
		}

		// check to see if the virtual register is assigned to physical register
		if (registerVMapped.containsKey(vrOP3)) {

			// change the v to p mapping if p is a memory location
			if (!registerVMapped.get(vrOP3).contains("r")) {
				// Pick the register with the furtherest next use
				physicalRegister = iterateRegisterNextU(registerNextU);

				// Change the mapping for register Change
				changeVRegisterMappings(registerVMapped, physicalRegister,
						Integer.toString(dataMemoryLoc));


				// Spill the contents of the furtherest register to memory
				printingSpillRegister(physicalRegister, vrOP3);
				checked = true;

				// Change the mappings for the virtual and physical register
				registerVMapped.put(vrOP3, physicalRegister);
				allocationActions.get(index).setPROp3(registerVMapped.get(vrOP3));


			}

			// write the r to opcode3 location
			allocationActions.get(index).setPROp3(registerVMapped.get(vrOP3));

			// update the next use for the register in the hashmap
			registerNextU.put(registerVMapped.get(vrOP3), allocationActions.get(index)
					.getNUOp3());


		} else {
			// if reg does not have a physical, assign one and write the r to opcode1 location
			if (registerAvail.empty()) {

				// Spill to register

				// Pick the register with the furtherest next use
				registerChange = iterateRegisterNextU(registerNextU);

				// Change the mapping for virtual register
				changeVRegisterMappings(registerVMapped, registerChange,
						Integer.toString(dataMemoryLoc));


				// Spill the contents of the furtherest register to memory
				printingSpillRegister(registerChange, vrOP3);

				checked = true;

				// Change the mappings for the virtual and physical register
				registerVMapped.put(vrOP3, registerChange);
				allocationActions.get(index).setPROp3(registerVMapped.get(vrOP3));

			} else {

				String assignRegister = registerAvail.pop();

				registerInUse.push(assignRegister);

				registerVMapped.put(vrOP3, assignRegister);

				// write the r to opcode1 location
				allocationActions.get(index).setPROp3(registerVMapped.get(vrOP3));

				// Patch for operation 3 having memory locations in the vector
				if (!allocationActions.get(index).getPROp3().contains("r")) {
					
					// Pick the register with the furtherest next use
					registerChange = iterateRegisterNextU(registerNextU);

					// Change the mapping for virtual register
					changeVRegisterMappings(registerVMapped, registerChange,
							Integer.toString(dataMemoryLoc));


					// Spill the contents of the furtherest register to memory
					printingSpillRegister(registerChange, vrOP3);
					checked = true;
					// Change the mappings for the virtual and physical register
					registerVMapped.put(vrOP3, registerChange);
					allocationActions.get(index).setPROp3(registerVMapped.get(vrOP3));
				}

				// update the next use for the register in the hashmap
				registerNextU.put(registerVMapped.get(vrOP3), allocationActions.get(index)
						.getNUOp3());

			}

		}

		if (checked && !memPrev.equals("") && !regChange.equals("")) {
			System.out.println("loadI \t" + memPrev + "\t  => \t " + regChange + "\t //Restoring");
			System.out.println("load \t" + regChange + "\t  => \t " + regChange + "\t //Restoring");
		}

	}


	public static void performAllocationOP2(String vrOP2, int index) {
		String registerChange = "MoveRegister", physicalRegister = "", regChange = "", memPrev = "";
		boolean checked = false;
		if (registerVMapped.containsKey(vrOP2)) {
			if (!registerVMapped.get(vrOP2).contains("r")) {

				// Pick the register with the furtherest next use
				regChange += iterateRegisterNextU(registerNextU);
				// Remember where the memory came from
				memPrev += registerVMapped.get(vrOP2);
			}
		}

		// check to see if the virtual register is assigned to physical register
		if (registerVMapped.containsKey(vrOP2)) {
			
			if (!registerVMapped.get(vrOP2).contains("r")) {

				// Pick the register with the furtherest next use
				physicalRegister = iterateRegisterNextU(registerNextU);

				// Change the mapping for register Change
				changeVRegisterMappings(registerVMapped, physicalRegister,
						Integer.toString(dataMemoryLoc));


				// Spill the contents of the furtherest register to memory
				printingSpillRegister(physicalRegister, vrOP2);
				checked = true;
				// Change the mappings for the virtual and physical register
				registerVMapped.put(vrOP2, physicalRegister);

				/** TRY CREATING A GLOBAL VARIABLE ############################## */
				operation2PR += physicalRegister;

				if (registerVMapped.containsKey(allocationActions.get(index).getVROp1())) {
					if (!registerVMapped.get(allocationActions.get(index).getVROp1()).contains("r")) {
						vrOP2 = allocationActions.get(index).getVROp1();
						// Pick the register with the furtherest next use
						physicalRegister = iterateRegisterNextU(registerNextU);

						// Change the mapping for register Change
						changeVRegisterMappings(registerVMapped, physicalRegister,
								Integer.toString(dataMemoryLoc));


						// Spill the contents of the furtherest register to memory
						printingSpillRegister(physicalRegister, vrOP2);
						checked = true;
						// Change the mappings for the virtual and physical register
						registerVMapped.put(vrOP2, physicalRegister);
						allocationActions.get(index).setPROp1(registerVMapped.get(vrOP2));
					}
				}

			}

			// write the r to opcode1 location
			allocationActions.get(index).setPROp2(registerVMapped.get(vrOP2));

			// Save the phyiscal register for operation 2 for output if not already done
			if (operation2PR.equals("")) {
				operation2PR += allocationActions.get(index).getPROp2();
			}
			// update the next use for the register in the hashmap
			registerNextU.put(registerVMapped.get(vrOP2), allocationActions.get(index)
					.getNUOp2());

			// check to see if there is a next use
			if (allocationActions.get(index).getNUOp2().contains("Empty")) {
				// remove from the mapping
				String freedRegister = registerVMapped.remove(vrOP2);
				// List as available
				registerAvail.push(freedRegister);
				// list as no longer in use
				registerInUse.remove(freedRegister);
			}
		} else {
			// if reg does not have a physical, assign one and write the r to opcode1 location
			// check to see there are available registers
			if (allocationActions.get(index).getVROp2().contains("Empty")) {
				System.out.println("\t\t Nothing here.");
				return;
			}
			if (registerAvail.empty()) {
				// Spill to register

				// Pick the register with the furtherest next use
				registerChange = iterateRegisterNextU(registerNextU);

				// Change the mapping for register Change
				changeVRegisterMappings(registerVMapped, registerChange,
						Integer.toString(dataMemoryLoc));


				// Spill the contents of the furtherest register to memory
				printingSpillRegister(registerChange, vrOP2);
				checked = true;
				// Change the mappings for the virtual and physical register
				registerVMapped.put(vrOP2, registerChange);
				allocationActions.get(index).setPROp2(registerVMapped.get(vrOP2));

				// Save the physical register for output
				operation2PR += allocationActions.get(index).getPROp2();
			} else {
				String assignRegister = registerAvail.pop();
				registerInUse.push(assignRegister);
				registerVMapped.put(vrOP2, assignRegister);

				// write the r to opcode1 location
				allocationActions.get(index).setPROp2(registerVMapped.get(vrOP2));

				// update the next use for the register in the hashmap
				registerNextU.put(registerVMapped.get(vrOP2), allocationActions.get(index)
						.getNUOp2());

				// Save the physical register for output
				operation2PR += allocationActions.get(index).getPROp2();

				if (allocationActions.get(index).getNUOp2().contains("Empty")) {
					// remove from the mapping
					String freedRegister1 = registerVMapped.remove(vrOP2);
					// List as available
					registerAvail.push(freedRegister1);
					// list as no longer in use
					registerInUse.remove(freedRegister1);
				}
			}

		}
		if (checked && !memPrev.equals("") && !regChange.equals("")) {
			System.out.println("loadI \t" + memPrev + "\t  => \t " + regChange + "\t //Restoring");
			System.out.println("load \t" + regChange + "\t  => \t " + regChange + "\t //Restoring");
		}
	}

	/**
	 * @param registerName: Virtual Register for Operation 1 is passed in.
	 * @param index
	 */
	public static void performAllocationOP1(String vrOP1, int index) {
		String registerChange = "OpenRegister", regChange = "", memPrev = "";
		boolean checked = false;
		if (registerVMapped.containsKey(vrOP1)) {
			//Absence of "r" assumes memory location
			if (!registerVMapped.get(vrOP1).contains("r")) {

				// Pick the register with the furtherest next use
				regChange += iterateRegisterNextU(registerNextU);
				// Remember where the memory came from
				memPrev += registerVMapped.get(vrOP1);
			}
		}

		// check to see if the virtual register is assigned to physical register
		if (registerVMapped.containsKey(vrOP1)) {

			// write the r to opcode1 location
			allocationActions.get(index).setPROp1(registerVMapped.get(vrOP1));

			// update the next use for the register in the hashmap
			registerNextU.put(registerVMapped.get(vrOP1), allocationActions.get(index)
					.getNUOp1());

			// Patch applied to finish store having memory locations in the first operation 1
			if (allocationActions.get(index).getTheOpcode().contains("store")
					&& !registerVMapped.get(allocationActions.get(index).getVROp1()).contains("r")) {

				// Pick the register with the furtherest next use
				registerChange = iterateRegisterNextU(registerNextU);

				// Change the mapping for register Change
				changeVRegisterMappings(registerVMapped, registerChange,
						Integer.toString(dataMemoryLoc));


				// Spill the contents of the furtherest register to memory meaning that it is now
				// freed.
				printingSpillRegister(registerChange, vrOP1);
				checked = true;

				// Change the mappings for the virtual and physical register
				registerVMapped.put(vrOP1, registerChange);
				allocationActions.get(index).setPROp1(registerVMapped.get(vrOP1));
			}

			// Patch applied for r being a memory location
			if (!allocationActions.get(index).getPROp1().contains("r")) {
				

				// Pick the register with the furtherest next use
				registerChange = iterateRegisterNextU(registerNextU);
			
				// Change the mapping for register Change
				changeVRegisterMappings(registerVMapped, registerChange,
						Integer.toString(dataMemoryLoc));


				// Spill the contents of the furtherest register to memory
				printingSpillRegister(registerChange, vrOP1);
				checked = true;

				// Change the mappings for the virtual and physical register
				registerVMapped.put(vrOP1, registerChange);
				allocationActions.get(index).setPROp1(registerVMapped.get(vrOP1));
			}


			// check to see if there is a next use
			if (allocationActions.get(index).getNUOp1().contains("Empty")) {
				// remove from the mapping
				String freedRegister = registerVMapped.remove(vrOP1);
				// List as available
				registerAvail.push(freedRegister);
				// list as no longer in use
				registerInUse.remove(freedRegister);
			}
		} else {
			// if reg does not have a physical, assign one and write the r to opcode1 location
			// check to see there are available registers
			if (registerAvail.empty()) {
				// Spill to register

				// Pick the register with the furtherest next use
				registerChange = iterateRegisterNextU(registerNextU);

				// Change the mapping for register Change
				changeVRegisterMappings(registerVMapped, registerChange,
						Integer.toString(dataMemoryLoc));


				// Spill the contents of the furtherest register to memory
				printingSpillRegister(registerChange, vrOP1);
				checked = true;
				// Change the mappings for the virtual and physical register
				registerVMapped.put(vrOP1, registerChange);
				allocationActions.get(index).setPROp1(registerVMapped.get(vrOP1));

			} else {
				String assignRegister = registerAvail.pop();
				registerInUse.push(assignRegister);
				registerVMapped.put(vrOP1, assignRegister);

				// write the r to opcode1 location
				allocationActions.get(index).setPROp1(registerVMapped.get(vrOP1));

				// update the next use for the register in the hashmap
				registerNextU.put(registerVMapped.get(vrOP1), allocationActions.get(index)
						.getNUOp1());

				if (allocationActions.get(index).getNUOp1().contains("Empty")) {
					// remove from the mapping
					String freedRegister1 = registerVMapped.remove(vrOP1);
					// List as available
					registerAvail.push(freedRegister1);
					// list as no longer in use
					registerInUse.remove(freedRegister1);
				}
			}

		}

		if (checked && !memPrev.equals("") && !regChange.equals("")) {
			System.out.println("loadI \t" + memPrev + "\t  => \t " + regChange + "\t //Restoring");
			System.out.println("load \t" + regChange + "\t  => \t " + regChange + "\t //Restoring");
		}

	}

	/**
	 * Fills the arrayList for generating all the numbers between start and end
	 * 
	 * @param regName
	 * @param startIndex
	 * @param endIndex
	 */
	public static void fillInLiveRanges(String regName, int startIndex, int endIndex) {
		ArrayList<Integer> linesLiveRange = new ArrayList<Integer>();
		for (int i = startIndex; i <= endIndex; i++) {
			linesLiveRange.add(i);
			// System.out.print(i +" ");
		}
		// System.out.println(regName);
		registerRanges.put(regName, linesLiveRange);
		registerLinesUsage.put(regName, "Empty");
		return;
	}

	/**
	 * Parses the command line to find the number of registers to produce and places that number of
	 * registers into the class variable Set<String> holding all the registers.
	 * 
	 * @param filePath
	 */
	public static boolean generateXRegisters(String strInt) {
		String regName = "r";
		int numRegisters;
		numRegisters = Integer.parseInt(strInt);
		if (numRegisters < 2) {
			System.out.println("Cannot allocate with fewer than 2 registers.");
			System.exit(0);
		}
		for (int j = 0; j < numRegisters; j++) {
			regName += Integer.toString(j);
			// add to the set
			registerAvail.add(regName);
			registerLinesUsage.put(regName, "Empty");
			registerNextU.put(regName, "Empty");
			regName = "r";
		}
		return true;
	}

	/**
	 * Methods prints out all the available options for parameters.
	 * 
	 * @param commandLine
	 * @param exitProgram
	 * @return
	 */
	public static boolean hFlag(String[] commandLine) {
		int arrayLen = commandLine.length;
		for (int i = 0; i < arrayLen; i++) {
			if (commandLine[i] == "-h") {
				System.out.println(" ");
				System.out
						.println("Command Syntax: "
								+ "\n\t    ./412alloc k filename [-h]\n\n"
								+ "\n Required arguments:"
								+ "\n\t    k     specifies the number of register available"
								+ "\n\t filename  is the pathname (absolute or relative) to the input file\n\n"
								+ "\n Optional flags:" + "\n\t    -h    prints this message" + "");
				return true;
			}
		}
		return false;
	}

	/**
	 * Opens up a text file and prints all the characters in the text.
	 * 
	 * @param filename
	 * @return
	 */
	public static HashMap<Integer, String> openAndRead(String filename) {
		// Structure reverses the file and does not hold comments
		Stack<String> lifo = new Stack<String>();

		// line index from bottom to top
		int stackIndex = 0;

		// line index from top to bottom
		int countToBottom = 0;

		// lines popped from stack
		String ilocLine = "";

		// Used to iterator through the registers found in the program going top down
		Set<String> printRegList;

		// storage of the program
		HashMap<Integer, String> dataStruct = new HashMap<Integer, String>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line;
			// read in each line from the block
			while ((line = reader.readLine()) != null) {

				// Skip pass the comment section of the file
				if (line.contains("/") && line.charAt(0) == '/') {
					// Check to see if the comments are skipped.
					// System.out.println(line);//When I parse the line for registers, I am going to
					// far.
					continue;
				}

				// Put the line in the correct format
				line = reformatLine(line);

				readTopDown(line, countToBottom);
				countToBottom++;

				// Places the lines onto a stack, to prepare for bottom up reading
				lifo.push(line);

			}
			reader.close();
		} catch (Exception e) {
			System.err.format("Exception occurred trying to read '%s'.", filename);
			e.printStackTrace();
		}

		printRegList = registerList.keySet();
		Iterator<String> pass = printRegList.iterator();
		String keyHere;
		while (pass.hasNext()) {
			keyHere = pass.next();
			// System.out.println(keyHere);
			// System.out.println("First appeared: " + registerList.get(keyHere)[0]);
			// System.out.println("Last appeared: " + registerList.get(keyHere)[1]);
			fillInLiveRanges(keyHere, registerList.get(keyHere)[0], registerList.get(keyHere)[1]);
		}

		// Produce MaxLive for Each from the top to bottom read, countToBottom -1 to account for the
		// extra increment
		maxLiveLines(countToBottom - 1);

		while (!lifo.isEmpty()) {
			ilocLine = (String) lifo.pop();

			// Fills the data structure with the ILOC Program
			dataStruct.put(stackIndex, ilocLine);

			// System.out.println(stackIndex);
			programLineCount = stackIndex;
			// increment counter
			stackIndex++;
		}
		
		// System.out.println(programLineCount);
		return dataStruct;
	}


	/**
	 * Takes in the line from the block makes the format into this:
	 * 
	 * Add r2,r3 => r4
	 * 
	 * @param lineIn
	 * @return
	 */
	public static String reformatLine(String lineIn) {

		String lineFormatted = "", buildToken = "";
		int index = 0, strLen = lineIn.length(), commaIndex, slashIndex;
		// System.out.println("Format this: \t" + lineIn + "\t Length: " + strLen);
		if (strLen == 0) {
			return lineFormatted;
		}

		// just return line if output
		if (lineIn.contains("output")) {
			return lineIn.trim();
		}

		// append the operation code
		while (lineIn.charAt(index) < strLen) {
			if (Character.isWhitespace(lineIn.charAt(index))) {
				break;
			}
			lineFormatted += lineIn.charAt(index);
			index++;
		}

		// keep moving to skip the white space
		while (Character.isWhitespace(lineIn.charAt(index))) {
			index++;
		}

		// get the first and second operation
		lineFormatted += " ";
		if (lineIn.contains(",")) {
			commaIndex = lineIn.indexOf(',');
			while (index <= commaIndex) {
				lineFormatted += lineIn.charAt(index);
				index++;
			}
			// go to the second register
			while (lineIn.charAt(index) != 'r') {
				index++;
			}
			// gets the second register
			while (!Character.isWhitespace(lineIn.charAt(index))) {
				lineFormatted += lineIn.charAt(index);
				index++;
			}
			lineFormatted += " ";
			lineFormatted += lineIn.charAt((lineIn.indexOf('=')));
			lineFormatted += lineIn.charAt((lineIn.indexOf('>')));
			lineFormatted += " ";
			while (lineIn.charAt(index) != 'r') {
				index++;
			}
			lineFormatted += lineIn.charAt(index);
			index++;
			while (Character.isDigit(lineIn.charAt(index))) {
				lineFormatted += lineIn.charAt(index);
				if (index + 1 < strLen) {
					index++;
				} else {
					break;
				}
			}
			return lineFormatted;
		}

		// for the other operations like loadI

		if (lineIn.contains("/")) {
			slashIndex = lineIn.indexOf("/");
			while (index < slashIndex) {
				if (lineIn.charAt(index) == '=') {
					lineFormatted += '=';
					index++;
					continue;
				}
				if (lineIn.charAt(index) == '>') {
					lineFormatted += "> ";
					index++;
				}
				if (!Character.isWhitespace(lineIn.charAt(index))) {
					buildToken += lineIn.charAt(index);
				} else {
					lineFormatted += buildToken + " ";
					buildToken = "";
				}
				index++;
			}
			return lineFormatted;
		}

		if (lineIn.contains("=")) {
			slashIndex = lineIn.indexOf("=");
			while (index <= slashIndex + 1) {
				if (lineIn.charAt(index) == '=') {
					lineFormatted += '=';
					index++;
					continue;
				}
				if (lineIn.charAt(index) == '>') {
					lineFormatted += "> ";
					index++;
				}
				if (!Character.isWhitespace(lineIn.charAt(index))) {
					buildToken += lineIn.charAt(index);
				} else {
					lineFormatted += buildToken + " ";
					buildToken = "";
				}
				index++;
			}
			index--;
			while (index < strLen) {
				if (lineIn.charAt(index) == 'r') {
					lineFormatted += 'r';
					index++;
					while (Character.isDigit(lineIn.charAt(index))) {
						lineFormatted += lineIn.charAt(index);
						if (index + 1 < strLen) {
							index++;
						} else {
							break;
						}
					}
					break;
				}
				index++;
			}
			return lineFormatted;
		}

		return lineFormatted;
	}

	/**
	 * The function MaxLive updates a class variable HashMap where the key is the line and value is
	 * number of live registers.
	 * 
	 * @param programLength
	 */
	public static void maxLiveLines(int programLength) {
		int liveRegistersCounted = 0;

		for (int i = programLength; i >= 0; i--) {
			Set<String> listedRegistersSet = registerList.keySet();
			Iterator<String> listedRegister = listedRegistersSet.iterator();
			while (listedRegister.hasNext()) {
				if (registerRanges.get(listedRegister.next()).contains(i)) {
					// System.out.println(i);
					liveRegistersCounted++;
				}
			}
			// System.out.println("Line Number: "+ i);
			// System.out.println("\t Number of live Registers: " + liveRegistersCounted);
			maxLiveHash.put(i, liveRegistersCounted);
			liveRegistersCounted = 0;
		}
		return;
	}

	/**
	 * Will write to HashMap to demonstrate the live range.
	 * 
	 * @param textLine
	 */
	public static void readTopDown(String textLine, int lineIndex) {
		 System.out.println("Reading this Line: \t" + textLine);
		String buildToken = "";
		int strLen = textLine.length();
		// Holds the start
		for (int i = 0; i < strLen; i++) {
			// step through the tokens as they build
			// System.out.println(textLine.charAt(i));
			if (Character.isWhitespace(textLine.charAt(i)) != true) {
				/*
				 * if (textLine.charAt(i) == '=') { if (textLine.charAt(i + 1) == '>') {
				 * System.out.println(textLine.charAt(i + 2)); break; } }
				 */
				buildToken += textLine.charAt(i);
				// System.out.println(textLine.charAt(i));
				/**This if statement passes in the operation 3 virtual register*/
				if (i + 1 == strLen) {
					 System.out.println("Virtual registers for operation 3: " +buildToken);
					liveRanges(buildToken, lineIndex);
				}
				continue;

			}
			/**The call to liveRanges below passes in the operation 1 and operation 2 virtual registers */
			// display the token built
			 System.out.println("Virtual registers for operation 1 and operation 2: " + buildToken);

			// Live ranges for all the registers in the program
			liveRanges(buildToken, lineIndex);

			buildToken = "";
		}
		return;
	}

	/**
	 * Finds the live ranges for the registers in the ILOC program.
	 * 
	 * @param tokenWord
	 * @param lineIndex
	 */
	public static void liveRanges(String tokenWord, int lineIndex) {
		int[] regIndices = new int[2];
		// index of the comma in a operation
		int commaIndex;
		String firstReg = "", secondReg = "";
		if (tokenWord != "") {

			// System.out.println("Token passed in: " + tokenWord);
			if (tokenWord.length() >= 2) {
				if (tokenWord.contains(",")) {
					commaIndex = tokenWord.indexOf(",");
					// parse the string to find the first register
					for (int i = 0; i < commaIndex; i++) {
						firstReg += tokenWord.charAt(i);
					}
					// parse the string to find the second register
					for (int i = commaIndex + 1; i < tokenWord.length(); i++) {
						secondReg += tokenWord.charAt(i);
					}
					/*
					 * Takes care of the first register in updating the live range
					 */
					if (firstReg.charAt(0) == 'r' && testForRegNum(firstReg.charAt(1))) {

						if (!registerList.containsKey(firstReg)) {
							regIndices = new int[2];
							// the first occurrance of the register in the program
							regIndices[0] = lineIndex;
							// the last occurrance of the register in the program
							regIndices[1] = lineIndex;
							registerList.put(firstReg, regIndices);
							System.out.println("First found: " + tokenWord + " at Line: "
									+ lineIndex);
						} else {
							regIndices = new int[2];
							// the first occurrance of the register in the program
							regIndices[0] = registerList.get(firstReg)[0];
							// the last occurrance of the register in the program
							regIndices[1] = lineIndex;
							// update the indices list
							registerList.put(firstReg, regIndices);
							// System.out.println("Updated: "+ buildToken + " at Line: " +
							// lineIndex);
						}
					}

					// Make sure we do not go out of bounds
					if (secondReg.length() > 1) {
						/*
						 * Takes care of the second register in updating the live range
						 */
						if (secondReg.charAt(0) == 'r' && testForRegNum(secondReg.charAt(1))) {

							if (!registerList.containsKey(secondReg)) {
								regIndices = new int[2];
								
								// the first occurrance of the register in the program
								regIndices[0] = lineIndex;
								
								// the last occurrance of the register in the program
								regIndices[1] = lineIndex;
								registerList.put(secondReg, regIndices);
								// System.out.println("First found: "+ tokenWord + " at Line: " +
								// lineIndex);
								return;
							} else {
								regIndices = new int[2];
								
								// the first occurrance of the register in the program
								regIndices[0] = registerList.get(secondReg)[0];
								
								// the last occurrance of the register in the program
								regIndices[1] = lineIndex;
								
								// update the indices list
								registerList.put(secondReg, regIndices);
								
								// System.out.println("Updated: "+ buildToken + " at Line: " +
								// lineIndex);
								return;
							}
						}// end of of rXX if-statement
					}
				}// end of if-statement testing for a comma
			}
		}

		if (tokenWord != "") {
			if (tokenWord.length() >= 2) {
				if (tokenWord.charAt(0) == 'r' && testForRegNum(tokenWord.charAt(1))) {
					if (!registerList.containsKey(tokenWord)) {
						regIndices = new int[2];
						// the first occurrance of the register in the program
						regIndices[0] = lineIndex;
						// the last occurrance of the register in the program
						regIndices[1] = lineIndex;
						registerList.put(tokenWord, regIndices);
						// System.out.println("First found: "+ tokenWord + " at Line: " +
						// lineIndex);
						return;
					} else {
						regIndices = new int[2];
						// the first occurrance of the register in the program
						regIndices[0] = registerList.get(tokenWord)[0];
						// the last occurrance of the register in the program
						regIndices[1] = lineIndex;
						// update the indices list
						registerList.put(tokenWord, regIndices);
						// System.out.println("Updated: "+ buildToken + " at Line: " + lineIndex);
						return;
					}
				}
			}
		}
		return;
	}

	/**
	 * Determines if the token word is a register number
	 * 
	 * @param number
	 * @return
	 */
	public static boolean testForRegNum(char number) {
		char[] alphaNumbers = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
		for (int i = 0; i < alphaNumbers.length; i++) {
			if (number == alphaNumbers[i]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Appends characters to the class variable tokenWord.
	 * 
	 * @param alphabet
	 */
	public static void readMicrosyntax(HashMap<Integer, String> storedData, int numberOfLinesCounted) {// numberOfLinesCounted
																										// is
																										// used
																										// as
																										// the
																										// Key
																										// for
																										// allocationActions
																										// as
																										// lines,
																										// must
																										// be
																										// decremented
		String buildToken = "", textLine = "", previousWord = "";
		Vector lineVectorOP;

		// System.out.println("In the readMirco function: " + numberOfLinesCounted);
		for (int j = 0; j < storedData.size(); j++) {
			// Vector class that holds the opCode, Op1, Op2, and Op3
			lineVectorOP = new Vector();
			textLine = storedData.get(j);
			// Prints out all the lines in the stack from the bottom up
			// System.out.println(textLine);
			for (int i = 0; i < textLine.length(); i++) {

				if (testForCharacters(textLine.charAt(i)) && i + 1 < textLine.length()
						&& !Character.isWhitespace(textLine.charAt(i + 1))) {
					buildToken += textLine.charAt(i);
					continue;
				} else {
					buildToken += textLine.charAt(i);
				}
				if (buildToken != "" && !buildToken.contains("\n") && !buildToken.contains("\t")
						&& !buildToken.contains(" ")) {
					// Prints out all the tokens in the line
					// System.out.println(buildToken);

					/**
					 * Now I need to write functions that will taken in buildToken as a String
					 * parameter and return booleans if that string fits the description. If the
					 * boolean is true then I need fill in the vector for the corresponding
					 * operation. If there is a comma present, write a method for that. Completed.
					 **/
					previousWord =
							sortMicrosyntax(buildToken, previousWord, lineVectorOP,
									numberOfLinesCounted);

				}
				// previousWord = "";
				// previousWord.concat(buildToken);// += buildToken;
				buildToken = "";
			}
			// System.out.println(lineVectorOP.getTheOpcode()+"\t"+
			// lineVectorOP.getVROp1()+"\t Next Used On Line: \t"+lineVectorOP.getNUOp1());
			allocationActions.put(numberOfLinesCounted, lineVectorOP);
			// numberOfLinesCounted is used as the Key for allocationActions as lines, must be
			// decremented
			numberOfLinesCounted--;
			// System.out.println("\tEnd of the line...");
		}
		return;

	}

	/**
	 * Retrieves the second operation from the string.
	 * 
	 * @param second
	 * @return
	 */
	public static String secondOperation(String second) {
		String value = "";
		int index = second.indexOf(',') + 1;
		while (index < second.length()) {
			value += second.charAt(index);
			index++;
		}
		return value;
	}

	/**
	 * Retrieves the first operation from the string.
	 * 
	 * @param first
	 * @return
	 */
	public static String firstOperation(String first) {
		String value = "";
		int index = 0;
		while (first.charAt(index) != ',') {
			value += first.charAt(index);
			index++;
		}
		return value;
	}

	/**
	 * Checks to see if the character corresponds to a microsyntax.
	 * 
	 * @param Character
	 * @return
	 */
	public static boolean testForCharacters(char Character) {
		char[] alphaChars =
				{'s', 't', 'o', 'r', 'e', 'l', 'a', 'd', 'I', ',', 'h', 'i', 'f', 'u', 'b', 'p',
						'n', 'm', '=', '>', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
		for (int i = 0; i < alphaChars.length; i++) {
			if (alphaChars[i] == Character) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks to if the input is a memory location.
	 * 
	 * @param constant
	 * @return
	 */
	public static boolean testForConstants(String constant) {
		int stringLeng = constant.length();
		for (int i = 0; i < stringLeng; i++) {
			if (!Character.isDigit(constant.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Finds the microsyntax for the operation code.
	 * 
	 * @param alphabet
	 */
	public static boolean OpcodeSyntax(String OpcodeSyn) {
		String[] OperationCodes =
				{"load", "loadI", "store", "lshift", "sub", "output", "nop", "add", "mult",
						"rshift"};
		int arrayLength = OperationCodes.length;
		for (int i = 0; i < arrayLength; i++) {
			if (OpcodeSyn.contains(OperationCodes[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Prints out the registers and the lines that they were lasted used.
	 * 
	 * @param mp
	 */
	public static void printMap(HashMap<String, String> mp) {
		Iterator it = mp.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			System.out.println(pairs.getKey() + " = " + pairs.getValue());
		}
	}

	/**
	 * Sorts the microsyntax into the vector.
	 * 
	 * @param alphabet
	 */
	public static String sortMicrosyntax(String buildToken, String previousWord,
			Vector lineVectorOP, int lineNumber) {
		if (OpcodeSyntax(buildToken)) {
			lineVectorOP.setTheOpcode(buildToken);
			// System.out.println(buildToken);
			return buildToken;
		}
		if (testForConstants(buildToken)) {
			lineVectorOP.setVROp1(buildToken);
			return buildToken;
		}
		if (buildToken.contains(",")) {
			/**
			 * Need to mark where I have seen the registers in the code.
			 * */
			lineVectorOP.setVROp1(firstOperation(buildToken));
			// get the line used
			lineVectorOP.setNUOp1(registerLinesUsage.get(firstOperation(buildToken)));
			// set the line used
			registerLinesUsage.put(firstOperation(buildToken), Integer.toString(lineNumber));
			lineVectorOP.setVROp2(secondOperation(buildToken));
			// get the line used
			lineVectorOP.setNUOp2(registerLinesUsage.get(secondOperation(buildToken)));
			// set the line used
			registerLinesUsage.put(secondOperation(buildToken), Integer.toString(lineNumber));
			return buildToken;
		}
		if (buildToken.charAt(0) == 'r') {
			// System.out.println(buildToken);
			// System.out.println(previousWord);
			if (previousWord.contains("=>")) {
				lineVectorOP.setVROp3(buildToken);
				// get the line used
				lineVectorOP.setNUOp3(registerLinesUsage.get(buildToken));
				// set the line used
				registerLinesUsage.put(buildToken, Integer.toString(lineNumber));
			} else {
				lineVectorOP.setVROp1(buildToken);
				// get the line used
				lineVectorOP.setNUOp1(registerLinesUsage.get(buildToken));
				// set the line used
				registerLinesUsage.put(buildToken, Integer.toString(lineNumber));
			}
			return buildToken;
		}
		return buildToken;
	}

}
