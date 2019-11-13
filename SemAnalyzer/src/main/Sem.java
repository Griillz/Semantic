package main;

import java.io.File;
import java.lang.Integer;
import java.util.ArrayList;
import java.lang.Math;
import java.util.regex.*;

public class Sem {
	boolean reject = true;
	boolean returned = false;
	int scope = 0; // general scope variable
	int globalscope = 0; // global scope variable used for global variables declared between functions
	int functionNum = 1; // Keeps track of the order the functions are seen in
	String currFun = ""; // current function name
	String currFunType = ""; // current function type
	static File tokenFile = new File("tokens"); // tokens file made by Lexer
	static ArrayList<ArrayList<String>> var = new ArrayList<ArrayList<String>>(); // variable table
	static ArrayList<ArrayList<String>> fun = new ArrayList<ArrayList<String>>(); // function table
	static String tok = ""; // current token
	static String[] tokens = Parser.tokens; // array of tokens fed in from lexer
	boolean ismain = false; // flag for checking if main is the last function in the program

	public Sem() {
		int i = 0;
		// loop through tokens file
		while (i < tokens.length) {
			String cur = tokens[i];
			//System.out.println(cur);
			reject = true;
			switch (cur) {
			case "K: int":
				String arrname = "";
				// int variable declaration, handles global declarations and function variable delcarations
				if (tokens[i + 1].contains("ID") && tokens[i + 2].equals(";")) {
					arrname = tokens[i + 1].substring(4, tokens[i + 1].length());
					addVar("int", arrname, false);
					i += 3;
				}
				// var array dec with specified size
				else if (tokens[i + 1].equals("[") && !tokens[i + 2].equals("]")) {
					i += 2;
					String tempsize = tokens[i].substring(5, tokens[i].length());
					i += 2;
					arrname = tokens[i].substring(4, tokens[i].length());
					addVar("int", arrname, true);
					addArrSize(arrname, tempsize);
					i += 2;
					if (!tokens[i + 1].equals("]")) {
						// this can be a while loop to check for array indexing with things other than a static integer,
						// this does not need to be done for semantics but will need to be done for code generation,
						// might as well do it here so do not have to worry about it later.
					}
				}
				// array dec without size
				else if (tokens[i + 1].equals("[") && tokens[i + 2].equals("]")) {
					i += 2;
					addVar("int", tokens[i + 1].substring(4, tokens[i + 1].length()), true);
					i += 2;
				}
				// int function dec, does not check for return here, checked in the return case
				else if (tokens[i + 1].contains("ID") && tokens[i + 2].equals("(")) {
					String funName = tokens[i + 1].substring(4, tokens[i + 1].length());
					i += 3;
					if (tokens[i].equals(")")) {
						addFunNoParams(funName, "int");
						i++;
						currFun = funName;
						currFunType = "int";

					} 
					else {						
						ArrayList<String> paramlist = new ArrayList<String>();
						//int testcount = 4;
						while (!tokens[i].equals(")")) {
							paramlist.add(tokens[i].substring(3, tokens[i].length()));
							i++;
							if(tokens[i].equals("[")) {
								i++;
								if(!tokens[i].equals("]")) {
									rej();
								}
								i++;
								paramlist.add(tokens[i].substring(4, tokens[i].length()));
								paramlist.add("");
								paramlist.add("true");
								paramlist.add("");
								i++;
								if(tokens[i].contains("ID")) {
									paramlist.set(1, tokens[i].substring(4, tokens[i].length()));
									i++;
								}
							}
							else {
							paramlist.add(tokens[i].substring(4, tokens[i].length()));
							paramlist.add("");
							paramlist.add("false");
							paramlist.add("");
							i++;

							}
							if (tokens[i].equals(",")) {
								i++;
							}
						}
						addFunParams(funName, "int", paramlist);
						i++;
						currFun = funName;
						currFunType = "int";
					}
				}
				break;
			case "K: void":
				// explicit main function check, will set a flag to indicate that this should be the last function
				// declared in the program
				if (tokens[i + 1].equals("ID: main") && tokens[i + 2].equals("(") && tokens[i + 3].equals("K: void")
						&& tokens[i + 4].equals(")")) {
					i += 4;
					ismain = true;
					currFun = "main";
					currFunType = "void";
					addFunNoParams(currFun, "void");
					
				// general void function declarations with no parameters
				} else if (tokens[i + 1].contains("ID") && tokens[i + 2].equals("(")) {
					String funName = tokens[i + 1].substring(4, tokens[i + 1].length());
					i += 3;
					if (tokens[i].equals(")")) {
						addFunNoParams(funName, "void");
						i++;
						currFun = funName;
						currFunType = "void";

					} 
					// general void function declarations with parameters
					else {						
						ArrayList<String> paramlist = new ArrayList<String>();
						//int testcount = 4;
						while (!tokens[i].equals(")")) {
							paramlist.add(tokens[i].substring(3, tokens[i].length()));
							i++;
							if(tokens[i].equals("[")) {
								i++;
								if(!tokens[i].equals("]")) {
									rej();
								}
								i++;
								paramlist.add(tokens[i].substring(4, tokens[i].length()));
								paramlist.add("");
								paramlist.add("true");
								paramlist.add("");
								i++;
								if(tokens[i].contains("ID")) {
									paramlist.set(1, tokens[i].substring(4, tokens[i].length()));
									i++;
								}
							}
							else {
							paramlist.add(tokens[i].substring(4, tokens[i].length()));
							paramlist.add("");
							paramlist.add("false");
							paramlist.add("");
							i++;

							}
							if (tokens[i].equals(",")) {
								i++;
							}
						}
						addFunParams(funName, "void", paramlist);
						i++;
						currFun = funName;
						currFunType = "void";
					}
				}
				break;
			case "K: return":
				// All checks done in here will check for correct return types within functions,
				// If a function is supposed to return a value and does not, that will be checked when the end of the
				// compound statement is reached, not within here, as this only triggers when the return 
				// keyword is seen
				i++;
				if (tokens[i].equals(";") && !currFunType.equals("void") || !tokens[i].contentEquals(";") && currFunType.equals("void")) {
					rej();
				} else if (tokens[i].contains("NUM") && tokens[i+1].equals(";") && currFunType.equals("int")) {
					i++;
					returned = true;
				} else if (tokens[i].contains("ID") && tokens[i+1].equals(";") && currFunType.equals("int")) {
					if (checkVarExists(currFun, (tokens[i].substring(4, tokens[i].length())))|| checkLocalVarExists(currFun, tokens[i].substring(4, tokens[i].length()))) {
						i++;
						returned = true;
					} else {
						rej();
					}
				}
				else if(tokens[i].contains("ID") || tokens[i].contains("NUM") && tokens[i+1].equals("+") || tokens[i+1].equals("/") || tokens[i+1].equals("*") 
						|| tokens[i+1].equals("-") && currFunType.equals("int"))
						 {
					while(!tokens[i].equals(";")) {
						if(tokens[i].contains("NUM")) {
						//	here(152);
							i++;
						}
						else if(tokens[i].contains("ID")) {
							if(checkVarExists(currFun,(tokens[i].substring(4, tokens[i].length()))) 
									|| checkLocalVarExists(currFun, tokens[i].substring(4, tokens[i].length()))) {
								//here(157);
								i++;
							}
							else {
								rej();
							}
						}
						if(tokens[i].equals("+") || tokens[i].equals("/") || tokens[i].equals("*") || tokens[i].equals("-") || tokens[i].equals(";")) {
						//	here(164);
							if(!tokens[i].equals(";")) {
							i++;
							}
						}
						else {
						//	here(169);
							rej();
						}
					}
					returned = true;
				}
				break;
			case "{":
				// Keeps track of general scope, will have to update to account for undeclared
				// functions within functions, and how to handle if else, as their scope "levels"
				// will be the same, but are unable to use each others local variables
				if (globalscope == 0) {
					globalscope++;
				}
				scope++;
				i++;
				break;
			case "}":
				// decrements the general scope, also keeps track of the "globalscope" counter
				// the globalscope counter is an implementation that accounts for global variables
				// declared in between functions, allowing you to know which functions were seen before
				// the global variable. Any function delcared before the globalscope count will not be able
				// to call that global variable, but any function afterwards will be able to.
				scope--;
				if (scope == 0) {
					if(currFunType.equals("int") && returned == false) {
						rej();
					}
					currFun = "";
					currFunType = "";
					globalscope++;
					returned = false;
				}
				i++;
				// rejects if main is not the last thing in the program
				if (ismain == true && scope == 0 && !tokens[i].equals("$")) {
					rej();
				}
				// accepts if main it the last thing in the program
				if (tokens[i].equals("$")) {
					if (scope == 0 && ismain == true) {
						System.out.println("ACCEPT");
						//displayVar();
						//displayFun();
						System.exit(0);
					} else {
						rej();
					}
				}
				break;
			case "=":
				if(tokens[i-1].contains("ID")) {
					if(tokens[i+1].contains("NUM") || tokens[i+1].contains("ID")) {
						if(tokens[i+1].contains("NUM")) {
						setVar(tokens[i-1].substring(4, tokens[i-1].length()), Integer.parseInt(tokens[i+1].substring(5, tokens[i+1].length())));
						i+=2;
						}
						else if(tokens[i+1].contains("ID") && !tokens[i+2].equals("[")){
							//here(292);
							setVar(tokens[i-1].substring(4, tokens[i-1].length()), Integer.parseInt(getValue(tokens[i+1].substring(4, tokens[i+1].length()))));
							i+=2;
						}
						else if(tokens[i+1].contains("ID") && tokens[i+2].equals("[")) {
							reject = false;
							if(checkIfArray(currFun,tokens[i+1].substring(4, tokens[i+1].length())) 
									|| checkIfLocalArray(currFun,tokens[i+1].substring(4, tokens[i+1].length()))) {
								i+=2;
								i+=paramCycle(i);
							}
							else {
								rej();
							}
							
						}
						else {
						while(!tokens[i].equals(";")) {
							if(tokens[i].contains("NUM")) {
								i++;
							}
							else if(tokens[i].contains("ID")) {
								if(checkVarExists(currFun,(tokens[i].substring(4, tokens[i].length()))) 
										|| checkLocalVarExists(currFun, tokens[i].substring(4, tokens[i].length()))) {
									i++;
								}
								else {
									rej();
								}
							}
							if(tokens[i].equals("+") || tokens[i].equals("/") || tokens[i].equals("*") || tokens[i].equals("-") || tokens[i].equals(";")) {
								if(!tokens[i].equals(";")) {
								i++;
								}
							}
							else {
								rej();
							}
						}
						//System.out.println(Integer.parseInt(tokens[i+1].substring(5, tokens[i+1].length())));
						//here(287);
						//i+=2;
					}
					}
					else if(tokens[i+1].contains("ID")) {
						
					}
				}
				else {
					i++;
				}
				break;
			case "K: if":
				i++;
				while(!tokens[i].equals(")")) {
					i++;
				}
				break;
			default:
				if(cur.matches("ID:.+")) {
					i++;
					if(tokens[i].equals("(")) {
						String funcall = tokens[i-1].substring(4, tokens[i-1].length());
					}
				}
				else {
				i++;
				}
			}
		}
		rej();

	}

	// Method for getting the type of a certain variable, typically only needed for return checks
	public String getType(String id) {
		if (checkVarExists(currFun, id)) {
			for (int i = 0; i < var.size(); i++) {
				if (var.get(i).get(2).equals(id)) {
					return var.get(i).get(1);
				}
			}
		}
		else {
			if(reject) {
			rej();
			}
		}

		return "-1";

	}
	
	public String getValue(String id) {
		if (checkVarExists(currFun, id)) {
			for (int i = 0; i < var.size(); i++) {
				if (var.get(i).get(2).equals(id)) {
					if(!var.get(i).get(3).equals("")) {
					return var.get(i).get(3);
					}
				}
			}
		}
		else {
			if(reject) {
			rej();
			}
		}

		return "9001";

	}
	
	
	// Gets the scope of a certain variable, will need to update this implementation
	/*
	public String getScope(String id) {
		String lowestscope = Integer.toString(scope);
		if (checkVarExists(currFun, id)) {
			for (int i = 0; i < var.size(); i++) {
				if (var.get(i).get(2).equals(id)) {
					if (Integer.parseInt(var.get(i).get(0)) > Integer.parseInt(lowestscope)) {
						lowestscope = var.get(i).get(0);
					}
				}
			}
		} else {
			rej();
		}

		for (int i = 0; i < var.size(); i++) {
			if (var.get(i).get(2).equals(id) && var.get(i).get(0).equals(lowestscope)) {
				return lowestscope;
			}
		}
		return "-1";
	}
	*/

	// Checks if a variable already exists, will have to update to account for scope
	public boolean checkVarExists(String checkFun, String id) {
		for (int i = 0; i < var.size(); i++) {
			if(var.get(i).get(2).equals(id) && var.get(i).get(0).startsWith("G")) {
				return true;
			}
			else if (var.get(i).get(2).equals(id) && checkFun.equals(var.get(i).get(0).substring(1, var.get(i).get(0).length())) 
					&& Integer.parseInt(var.get(i).get(0).substring(0,1)) <= scope) {
				return true;
			}
		}
		return false;
	}
	
	public boolean checkLocalVarExists(String checkFun, String id) {
		for (int i = 0; i < fun.size(); i++) {
			if (fun.get(i).get(1).equals(checkFun)) {
				for (int j = 3; j < fun.get(i).size(); j++) {
					if (fun.get(i).get(j).equals(id)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean checkIfArray(String checkFun, String id) {
		if(checkVarExists(checkFun, id)) {
		for (int i = 0; i < var.size(); i++) {
			if (var.get(i).get(2).equals(id)) {
				if(var.get(i).get(4).equals("true")) {
					return true;
				}
			}
		}
		}
		else {
			if(reject) {
			rej();
			}
		}
		return false;
	}
	
	public boolean checkIfLocalArray(String checkFun, String id) {
		if (checkLocalVarExists(checkFun, id)) {
			for (int i = 0; i < fun.size(); i++) {
				if (fun.get(i).get(1).equals(checkFun)) {
					for(int j = 4; j < fun.get(i).size(); j+=5) {
						if(fun.get(i).get(j).equals(id)) {
							if(fun.get(i).get(j+2).equals("true")) {
								return true;
							}
						}
					}
				}
			}
		} else {
			if(reject) {
			rej();
			}
		}
		return false;
	}

	// checks if a function already exists
	public boolean checkFunExists(String id) {
		for (int i = 0; i < fun.size(); i++) {
			if (fun.get(i).get(1).equals(id)) {
				return true;
			}
		}
		return false;
	}

	// adds a variable to the variable table
	public void addVar(String type, String id, boolean isArray) {
		if (!checkVarExists(currFun, id)) {
			ArrayList<String> templist = new ArrayList<String>();
			if (scope == 0) {
				templist.add("G" + Integer.toString(globalscope));
			} else {
				templist.add(scope + currFun);
			}
			templist.add(type);
			templist.add(id);
			templist.add("");
			templist.add(Boolean.toString(isArray));
			templist.add("");
			var.add(templist);
			//displayVar();
		} else {
			if(reject) {
			rej();
			}
		}
	}

	// adds the size of an array to the array 
	public void addArrSize(String id, String size) {
		// String locscope = getScope(id);
		for (int i = 0; i < var.size(); i++) {
			if (var.get(i).get(4).equals("true") && var.get(i).get(0).equals(scope)) {
				var.get(i).set(5, size);
				displayVar();
			}
		}
	}

	// sets the value of a variable if set
	public void setVar(String id, int value) {
		if(checkVarExists(currFun, id) || checkLocalVarExists(currFun, id)) {
		for (int i = 0; i < var.size(); i++) {
			if(var.get(i).get(0).startsWith("G") && var.get(i).get(2).equals(id)) {
				var.get(i).set(3, Integer.toString(value));
			}
			else if(!var.get(i).get(0).startsWith("G")) {
			if (Integer.parseInt(var.get(i).get(0).substring(0, 1)) <= scope && var.get(i).get(2).equals(id)) {
				var.get(i).set(3, Integer.toString(value));
			}
			}
		}
		}
	}

	// displays the variable table in the console
	public void displayVar() {
		for (int i = 0; i < var.size(); i++) {
			for (int j = 0; j < var.get(i).size(); j++) {
				System.out.print(var.get(i).get(j));
				System.out.print(", ");
			}
			System.out.println();
		}
		System.out.println("END VAR TABLE");
	}

	// displays the function table in the console
	public void displayFun() {
		for (int i = 0; i < fun.size(); i++) {
			if (fun.get(i).size() < 3) {
				System.out.print(fun.get(i).get(0));
				System.out.print(", ");
				System.out.print(fun.get(i).get(1));
				System.out.print(", ");
				System.out.print(fun.get(i).get(2));
				System.out.println();
			} else {
				System.out.print(fun.get(i).get(0));
				System.out.print(", ");
				System.out.print(fun.get(i).get(1));
				System.out.print(", ");
				System.out.print(fun.get(i).get(2));
				System.out.print(", ");
				System.out.println();
				for (int j = 3; j < fun.get(i).size(); j+=5) {
					System.out.print(fun.get(i).get(j));
					System.out.print("_ ");
					System.out.print(fun.get(i).get(j + 1));
					System.out.print("_ ");
					System.out.print(fun.get(i).get(j + 2));
					System.out.print("_ ");
					System.out.print(fun.get(i).get(j + 3));
					System.out.print("_ ");
					System.out.print(fun.get(i).get(j + 4));
					System.out.println("_,");

				}
			}
			System.out.println();
		}
		System.out.println("END FUNCTION TABLE");
	}

	// adds a function with parameters to the function table
	public void addFunParams(String type, String id, ArrayList<String> params) {
		if (!checkFunExists(id)) {
			ArrayList<String> templist = new ArrayList<String>();
			templist.add(Integer.toString(functionNum));
			functionNum++;
			templist.add(type);
			templist.add(id);
			for (int i = 0; i < params.size(); i++) {
				templist.add(params.get(i));
			}

			fun.add(templist);
			//displayFun();
		} else {
			if(reject) {
			rej();
			}
		}
	}

	// adds a function with no parameters to the function table
	public void addFunNoParams(String type, String id) {
		if (!checkFunExists(id)) {
			ArrayList<String> templist = new ArrayList<String>();
			templist.add(Integer.toString(functionNum));
			functionNum++;
			templist.add(type);
			templist.add(id);
			fun.add(templist);
			//displayFun();
		}
	}
	
	public int paramCycle(int index) {
		int count = 0;
		index++;
		count++;
		while(!tokens[index].equals("]")) {
			if(tokens[index].contains("NUM")) {
				index++;
				count++;
			}
			else if(tokens[index].contains("ID") || tokens[index].contains("NUM")) {
				if(checkVarExists(currFun, tokens[index].substring(4, tokens[index].length())) 
						|| checkLocalVarExists(currFun, tokens[index].substring(4, tokens[index].length())) ) {
					index++;
					count++;
					if(tokens[index].equals("[")) {
						if(checkIfArray(currFun, tokens[index-1].substring(4, tokens[index-1].length())) 
								|| checkIfLocalArray(currFun, tokens[index-1].substring(4, tokens[index-1].length()))) {
							//index++;
							count++;
						index += paramCycle(index);
						}
						else {
							rej();
						}
					}
				}
				else {
					rej();
				}
			}
			else {
				rej();
			}
			if(tokens[index].equals("+") || tokens[index].equals("-") || tokens[index].equals("/") || tokens[index].equals("*")
					|| tokens[index].equals("]")) {
				if(!tokens[index].equals("]")) {
				count++;
				index++;
				}
				else {
					return count + 1;
				}
			}
			
		}
		return count;
	}

	// reject function to print reject and exit program
	private static void rej() {
		System.out.println("REJECT");
		System.exit(0);
	}

	// debugging method to know where i am at in the code
	private static void here(int i) {
		System.out.println("HERE! " + i);
	}

}
