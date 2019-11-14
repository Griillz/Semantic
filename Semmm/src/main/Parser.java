package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

public class Parser {

	static String token;
	static String tempNext;
	static File tokenFile = new File("tokens");
	static Scanner sc = null;
	static String[] tokens;
	static int numTok = -1;
	static String lexString = "";
	static int o = 0;
	static Node root;
	//// copied from SEM
	static boolean reject = true;
	static boolean returned = false;
	static int scope = 0; // general scope variable
	static int globalscope = 0; // global scope variable used for global variables declared between functions
	static int functionNum = 1; // Keeps track of the order the functions are seen in
	static String currFun = ""; // current function name
	static String currFunType = ""; // current function type
	static ArrayList<ArrayList<String>> var = new ArrayList<ArrayList<String>>(); // variable table
	static ArrayList<ArrayList<String>> fun = new ArrayList<ArrayList<String>>(); // function table
	static ArrayList<String> funParams = new ArrayList<String>(); // function paramaters
	static String tok = ""; // current token
	static String arrname = "";
	static String expfrom = "";
	static Stack<Integer> argnum = new Stack<>();
	static Stack<String> argfun = new Stack<>();
	static ArrayList<String> amdyolo;
	static boolean argisarray = false;
	static boolean infunc = false;
	static boolean toexp = false;
	static boolean ismain = false; // flag for checking if main is the last function in the program

	public static void main(String args[]) {

		File file = new File(args[0]);

		try {
			new Lexer(file);
			sc = new Scanner(tokenFile);
			while (!lexString.equals("$")) {
				numTok++;
				lexString = sc.nextLine();
			}
			tokens = new String[numTok + 1];
			sc = new Scanner(tokenFile);
			for (int i = 0; i < tokens.length; i++) {
				tokens[i] = sc.nextLine();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
			//new Sem();

		token = tokens[o];

		root = new Node("program");
		root.addChild(declarationList());
		if (token.equals("$"))
			//new Sem();
			System.out.println("ACCEPT");
		else {
			rej();
		}
		
/*
		System.out.println("HERE@@@@@@@@@@@@@@");
		root.printChildren(root);
		System.out.println("END OF PARSE TREE");
		*/
		

	}

	private static void nextToken() {
		o++;
		token = tokens[o];
	}

	private static Node declarationList() {
		System.out.println("DECLIST " + token);
		Node node = new Node("declarationList");
		node.addChild(declaration());
		node.addChild(declarationListP());

		return node;
	}

	private static Node declarationListP() {
		Node node = new Node("declarationListP");
		System.out.println("DECLISTP " + token);
		if (token.equals("K: int") || token.equals("K: void")) {
			node.addChild(declaration());
			node.addChild(declarationListP());
		} else {
			node.addChild(epsilon());
		}

		return node;
	}

	private static Node declaration() {
		Node node = new Node("declaration");
		System.out.println("DEC " + token);
		node.addChild(typeSpecifier());
		if (token.contains("ID: ")) {
			System.out.println("DEC " + token);
			node.addChild(token());
			if(tokens[o+1].equals(";")) {
			arrname = tokens[o];
			addVar("int", arrname, false);
			}
			else if(tokens[o+1].equals("[")) {
				arrname = tokens[o];
				addVar("int", arrname, true);
			}
			nextToken();
		} else {
			rej();
		}

		node.addChild(declarationP());

		return node;
	}

	private static Node declarationP() {
		Node node = new Node("declarationP");
		System.out.println("DECP " + token);
		if (token.equals(";") || token.equals("[")) {
			node.addChild(varDeclarationP());
		} else {
			if (token.equals("(")) {
				node.addChild(functionDeclarationP());
			} else {
				rej();
			}
		}

		return node;
	}

	private static Node varDeclaration() {
		Node node = new Node("varDeclaration");
		System.out.println("VARDEC " + token);
		node.addChild(typeSpecifier());
		System.out.println("VARDEC AFTER TYPE " + token);
		if (token.contains("ID: ")) {
			node.addChild(token());
			nextToken();
			if (token.equals(";")) {
				node.addChild(token());
				arrname = tokens[o-1];
				addVar("int", arrname, false);
				nextToken();
			} else if (token.equals("[")) {
				node.addChild(token());
				arrname = tokens[o-1];
				addVar("int", arrname, true);
				nextToken();
				if (token.contains("NUM: ")) {
					node.addChild(token());
					addArrSize(arrname, tokens[o]);
					nextToken();

					if (token.equals("]")) {
						node.addChild(token());
						nextToken();
						if (token.equals(";")) {
							node.addChild(token());
							nextToken();
						} else {
							rej();
						}
					} else {
						rej();
					}
				} else {
					rej();
				}
			} else {
				rej();
			}
		} else {
			rej();
		}

		return node;

	}

	private static Node varDeclarationP() {
		Node node = new Node("varDeclarationP");
		System.out.println("VARDECP " + token);
		if (token.equals(";")) {
			node.addChild(token());
			nextToken();
		} else if (token.equals("[")) {
			node.addChild(token());
			nextToken();
			if (token.contains("NUM: ")) {
				node.addChild(token());
				nextToken();
				if (token.equals("]")) {
					node.addChild(token());
					nextToken();
					if (token.equals(";")) {
						node.addChild(token());
						nextToken();
					} else {
						rej();
					}
				} else {
					rej();
				}
			} else {
				rej();
			}
		} else {
			rej();
		}

		return node;
	}

	private static Node typeSpecifier() {
		Node node = new Node("typeSpecifier");
		System.out.println(node.getData() + "HELLO");
		System.out.println("TYPESPEC " + token);
		if (token.equals("K: int") || token.equals("K: void")) {
			node.addChild(token());
			nextToken();
		} else {
			rej();
		}

		return node;
	}

	private static Node functionDeclarationP() {
		Node node = new Node("functionDeclarationP");
		if (token.equals("(")) {
			node.addChild(token());
			currFun = tokens[o-1];
			currFunType = tokens[o-2];
			System.out.println("currFun " + currFun);
			System.out.println("currFunType " + currFunType);
			if(currFun.equals("ID: main")) {
				ismain = true;
			}
			nextToken();
			node.addChild(params());
			if (token.equals(")")) {
				node.addChild(token());
				nextToken();
			} else {
				rej();
			}
			node.addChild(compoundStatement());
		} else {
			rej();
		}

		return node;
	}

	private static Node params() {
		Node node = new Node("params");
		System.out.println("PARAMS " + token);
		if (token.equals("K: void")) {
			node.addChild(token());
			nextToken();
			addFunNoParams(currFunType, currFun);
		} else {
			node.addChild(paramsList());
			addFunParams(currFunType, currFun, funParams);
		}

		return node;
	}

	private static Node paramsList() {
		Node node = new Node("paramList");
		System.out.println("PARAMSLIST " + token);
		node.addChild(param());
		node.addChild(paramsListP());

		return node;
	}

	private static Node paramsListP() {
		Node node = new Node("paramListP");
		System.out.println("PARAMSLISTP " + token);
		if (token.equals(",")) {
			node.addChild(token());
			nextToken();
			node.addChild(param());
			node.addChild(paramsListP());
		} else {
			node.addChild(epsilon());
		}

		return node;
	}

	private static Node param() {
		boolean arr = false;
		Node node = new Node("param");
		System.out.println("PARAM " + token);
		node.addChild(typeSpecifier());
		if (token.contains("ID: ")) {
			funParams.add("int");
			funParams.add(token);
			funParams.add("");
			node.addChild(token());
			nextToken();
			if (token.equals("[")) {
				arr = true;
				funParams.add("true");
				funParams.add("");
				node.addChild(token());
				nextToken();
				if (token.equals("]")) {
					node.addChild(token());
					nextToken();
				} else {
					rej();
				}
			}
			if(!arr) {
				funParams.add("false");
				funParams.add("");
			}
			
		} else {
			rej();
		}

		return node;
	}

	private static Node compoundStatement() {
		Node node = new Node("compoundStatement");
		System.out.println("COMPSTMT " + token);
		if (token.equals("{")) {
			if (globalscope == 0) {
				globalscope++;
			}
			scope++;
			node.addChild(token());
			nextToken();
		} else {
			rej();
		}
		node.addChild(localDeclaration());
		node.addChild(statementList());
		System.out.println("COMPSTMT " + token);
		if (token.equals("}")) {
			node.addChild(token());
			delScope(scope);
			scope--;
			if (scope == 0) {
				here(339);
				if(currFunType.equals("K: int") && returned == false) {
					here(341);
					rej();
				}
				currFun = "";
				currFunType = "";
				globalscope++;
				returned = false;
			}
			nextToken();
			if (ismain == true && scope == 0 && !tokens[o].equals("$")) {
				here(351);
				rej();
			}
			if(scope == 0) {
			returned = false;
			}
			// accepts if main it the last thing in the program
			if (tokens[o].equals("$")) {
				if (scope == 0 && ismain == true) {
					System.out.println("ACCEPT");
					//displayVar();
					//displayFun();
					System.exit(0);
				} else {
					here(362);
					rej();
				}
			}
		} else {
			System.out.println("THIS ONE");
			rej();
		}

		return node;
	}

	private static Node localDeclaration() {
		Node node = new Node("localDelcaration");
		System.out.println("LOCDEC " + token);
		if (token.contains("K: int") || token.contains("K: void")) {
			node.addChild(varDeclaration());
			node.addChild(localDeclaration());
		} else {
			node.addChild(epsilon());
		}

		return node;
	}

	private static Node statementList() {
		Node node = new Node("statementList");
		System.out.println("STMTLISTP " + token);
		if (token.contains("K: ") || token.equals(";") || token.contains("NUM: ") || token.equals("(")
				|| token.contains("ID: ") || token.equals("{")) {
			node.addChild(statement());
			node.addChild(statementList());
		} else {
			node.addChild(epsilon());
		}

		return node;
	}

	private static Node statement() {
		Node node = new Node("statement");
		System.out.println("STMT " + token);
		if (token.equals("{"))
			node.addChild(compoundStatement());
		else if (token.equals("K: if"))
			node.addChild(selectionStatement());
		else if (token.equals("K: while"))
			node.addChild(iterationStatement());
		else if (token.equals("K: return"))
			node.addChild(returnStatement());
		else
			node.addChild(expressionStatement());

		return node;

	}

	private static Node expressionStatement() {
		Node node = new Node("expressionStatement");
		System.out.println("EXPSTMT " + token);
		if (token.equals(";")) {
			node.addChild(token());
			nextToken();
		} else {
			node.addChild(expression());
			if (token.equals(";")) {
				node.addChild(token());
				nextToken();
			} else {
				rej();
			}
		}

		return node;
	}

	private static Node selectionStatement() {
		Node node = new Node("selectionStatement");
		System.out.println("SELSTMT " + token);
		if (token.equals("K: if")) {
			node.addChild(token());
			nextToken();
			if (token.equals("(")) {
				node.addChild(token());
				nextToken();
				node.addChild(expression());
				if (token.equals(")")) {
					node.addChild(token());
					nextToken();
					node.addChild(statement());
					if (token.equals("K: else")) {
						node.addChild(token());
						nextToken();
						node.addChild(statement());
					}
				} else {

				}
			} else {
				rej();
			}
		} else {
			rej();
		}

		return node;
	}

	private static Node iterationStatement() {
		Node node = new Node("iterationStatement");
		System.out.println("ITERSTMT " + token);
		if (token.equals("K: while")) {
			node.addChild(token());
			nextToken();
			if (token.equals("(")) {
				node.addChild(token());
				nextToken();
				node.addChild(expression());
				if (token.equals(")")) {
					node.addChild(token());
					nextToken();
					node.addChild(statement());
				} else {
					rej();
				}

			} else {
				rej();
			}
		} else {
			rej();
		}

		return node;
	}

	private static Node returnStatement() {
		toexp = false;
		Node node = new Node("returnStatement");
		System.out.println("RETSTMT " + token);
		if (token.equals("K: return")) {
			node.addChild(token());
			nextToken();
			if (token.equals("(") || token.contains("NUM: ") || token.contains("ID: ")) {
				expfrom = "return";
				node.addChild(expression());
				toexp = true;
			}
			expfrom = "";
			System.out.println(token + "THERTRWEIFEHSJFEWSOIFJESWFSWEGFHFESFGEWSFEWSF");
			if (token.equals(";")) {
				returned = true;
				node.addChild(token());
				if(!currFunType.equals("K: void") && toexp == false) {
					here(521);
					rej();
				}
				else if(currFunType.equals("K: void") && toexp == true) {
					here(550);
					rej();
				}
				nextToken();
			} else{
				here(555);
				rej();
			}
		} else
			rej();

		return node;
	}

	private static Node expression() {
		Node node = new Node("expression");
		System.out.println("EXP " + token);
		node.addChild(additiveExpression());
		if (token.equals("<=") || token.equals("<") || token.equals(">") || token.equals(">=") || token.equals("==")
				|| token.equals("!=")) {
			node.addChild(relop());
			node.addChild(additiveExpression());
		}

		return node;
	}

	private static Node var() {
		boolean first = true;
		Node node = new Node("var");
		System.out.println("VAR " + token);
		if (token.equals("[")) {
			node.addChild(token());
			nextToken();
			node.addChild(expression());
			if (token.equals("]")) {
				node.addChild(token());
				nextToken();
			} else {
				rej();
			}
		}
		

			if (token.equals("=")) {
				first = false;
				node.addChild(token());
				nextToken();
				boolean done = false;
				if (token.equals("[")) {
					node.addChild(token());
					nextToken();
					node.addChild(expression());
					done = true;
					if (token.equals("]")) {
						node.addChild(token());
						nextToken();
					} else {
						rej();
					}
				}
				if (!done) {
					node.addChild(expression());
				}

			} else {
				node.addChild(epsilon());
			}

		
		return node;
	}

	private static Node relop() {
		Node node = new Node("relop");
		System.out.println("RELOP " + token);
		if (token.equals("<=") || token.equals("<") || token.equals(">") || token.equals(">=") || token.equals("==")
				|| token.equals("!=")) {
			node.addChild(token());
			nextToken();
		} else {
			rej();
		}

		return node;
	}

	private static Node additiveExpression() {
		Node node = new Node("additiveExpression");
		System.out.println("ADDEXP " + token);
		node.addChild(term());
		node.addChild(additiveExpressionP());

		return node;
	}

	private static Node additiveExpressionP() {
		Node node = new Node("additiveExpressionP");
		System.out.println("ADDEXPP " + token);
		if (token.equals("+") || token.equals("-")) {
			node.addChild(addop());
			node.addChild(term());
			node.addChild(additiveExpressionP());
		} else {
			node.addChild(epsilon());
		}

		return node;
	}

	private static Node addop() {
		Node node = new Node("addop");
		System.out.println("ADDOP " + token);
		if (token.equals("+") || token.equals("-")) {
			node.addChild(token());
			nextToken();
		} else {
			rej();
		}

		return node;
	}

	private static Node term() {
		Node node = new Node("term");
		System.out.println("TERM " + token);
		node.addChild(factor());
		node.addChild(termP());

		return node;
	}

	private static Node termP() {
		Node node = new Node("termP");
		System.out.println("TERMP " + token);
		if (token.equals("*") || token.equals("/")) {
			node.addChild(mulop());
			node.addChild(factor());
			node.addChild(termP());
		} else {
			node.addChild(epsilon());
		}

		return node;
	}

	private static Node mulop() {
		Node node = new Node("mulop");
		System.out.println("MULOP " + token);
		if (token.equals("*") || token.equals("/")) {
			node.addChild(token());
			nextToken();
		} else {
			rej();
		}

		return node;
	}

	private static Node factor() {
		Node node = new Node("factor");
		System.out.println("FACTOR " + token);
		if (token.equals("(")) {
			node.addChild(token());
			nextToken();
			node.addChild(expression());
			if (token.equals(")")) {
				node.addChild(token());
				nextToken();
			} else {
				rej();
			}
		} else if (token.contains("ID: ") && !tokens[o+1].equals("(")) {
			if(!checkVarExists(currFun, token) && !checkLocalVarExists(currFun, token)) {
				here(718);
				rej();
			}
			if(!tokens[o+1].equals("[") && !(tokens[o+1].equals(";") || tokens[o+1].equals(","))) {
				if ((checkIfArray(currFun, token) || checkIfLocalArray(currFun, token)) != argisarray) {
					if (infunc) {
						rej();
					}
				}
				if(checkIfArray(currFun, token)) {
					here(723);
					rej();
				}
				else if(checkIfLocalArray(currFun, token)) {
				here(727);
				rej();
				}
			}
			else if (tokens[o+1].equals("[")) {
				if (!argisarray && infunc) {
					rej();
				}
			}
			node.addChild(token());
			if (expfrom.equals("return")) {
				System.out.println("MADE IN HERE OR REST?");
				if (tokens[o + 1].equals(";")) {
					if (checkIfArray(currFun, token) && checkIfLocalArray(currFun, token)) {
						here(695);
						rej();
					}
				}
				else if(tokens[o+1].equals("[") ){
					if(!checkIfArray(currFun, token) && !checkIfLocalArray(currFun, token)) {
						here(708);
						rej();
					}
					if(tokens[o+2].contains("ID")) {
							if(!checkVarExists(currFun, tokens[o+2]) && !checkLocalVarExists(currFun, tokens[o+2])) {
								here(718);
								rej();
							}
						}
				}
				/*
				else if(tokens[o+1].equals("[") && !tokens[o+2].contains("NUM")
						|| !tokens[o+2].contains("ID")) {
					System.out.println(token);
					System.out.println(tokens[o+1]);
					System.out.println(tokens[o+2]);
					here(725);
					rej();
				}
				*/
			}
			
			
			
			nextToken();
			node.addChild(factorP());
		}
		else if (token.contains("ID") && tokens[o+1].equals("(")) {
			if (argisarray && infunc) {
				rej();
			}
			if(!checkFunExists(token)) {
				here(772);
				rej();
			}
			if(currFunType.equals("K: int")) {
				if(!checkFunType(token, currFunType).equals("K: int") && expfrom.equals("return")) {
					System.out.println(currFunType + " WHAT THE FUCK IS GOING ON");
					//System.out.println(checkFunType(token));
					here(777);
					rej();
				}
			}
			else if(currFunType.equals("K: void") && expfrom.equals("return")) {
				if(!checkFunType(token, currFunType).equals("K: void")) {
					here(783);
					rej();
				}
			}
			argfun.push(token);
			argnum.push(0);
			nextToken();
			node.addChild(factorP());
			argfun.pop();
			argnum.pop();
		}

		else if (token.contains("NUM: ")) {
			if (argisarray && infunc) {
				rej();
			}
			node.addChild(token());
			if(expfrom.equals("return")) {
				if(!currFunType.equals("K: int")) {
					rej();
				}
			}
			nextToken();
		} else {
			here(718);
			rej();
		}

		return node;
	}

	private static Node factorP() {
		Node node = new Node("factorP");
		System.out.println("FACTORP " + token);
		if (token.equals("(") || token.equals(",")) {
			node.addChild(callP());
		} else
			node.addChild(var());

		return node;
	}

	private static Node callP() {
		Node node = new Node("callP");
		System.out.println("CALLP " + token);
		if (token.equals("(")) {
			node.addChild(token());
			nextToken();
			node.addChild(args());
			if (token.equals(")")) {
				node.addChild(token());
				nextToken();
			} else {
				rej();
			}
		} else if (token.equals(",")) {
			node.addChild(args());
		} else {
			rej();
		}

		return node;
	}

	private static Node args() {
		Node node = new Node("args");
		System.out.println("ARGS " + token);
		
		infunc = true;
		
		amdyolo = null;
		for (ArrayList<String> x : fun) {
			if (x.get(2).equals(argfun.peek())) {
				amdyolo = x;
				break;
			}
		}
		if (amdyolo == null) {
			rej();
		}
		
		if ((token.equals("(") || token.contains("ID: ") || token.contains("NUM: "))) {
			node.addChild(argList());
		}  else {
			if (amdyolo.size() > 3) {
				rej();
			}
			node.addChild(epsilon());
		}
		
		infunc = false;
		return node;
	}

	private static Node argList() {
		Node node = new Node("argList");
		System.out.println("ARGLIST " + token);
		argisarray = amdyolo.get(6).equals("true");
		infunc = true;
		node.addChild(expression());
		argnum.pop();
		argnum.push(8);
		node.addChild(argListP());
		infunc = false;
		return node;
	}

	private static Node argListP() {
		Node node = new Node("argListP");
		System.out.println("ARGLISTP " + token);
		if (token.equals(",")) {
			node.addChild(token());
			nextToken();
			argisarray = amdyolo.get(argnum.peek() + 3).equals("true");
			infunc = true;
			node.addChild(expression());
			int tmp = argnum.pop();
			argnum.push(tmp + 5);
			node.addChild(argListP());
		} else {
			node.addChild(epsilon());
			infunc = false;
		}

		return node;
	}

	private static void rej() {
		System.out.println("REJECT");
		System.exit(0);
	}

	private static Node token() {
		Node node = new Node(token);
		return node;
	}

	private static Node epsilon() {
		Node node = new Node("Epsilon");
		return node;
	}
	
	// FROM SEM
	
	public static boolean checkVarExists(String checkFun, String id) {
		for (int i = 0; i < var.size(); i++) {
			if(var.get(i).get(3).equals(id) && var.get(i).get(1).startsWith("G")) {
				return true;
			}
			else if (var.get(i).get(3).equals(id) && checkFun.equals(var.get(i).get(1))) {
				return true;
			}
		}
		here(815);
		return false;
	}
	
	public static boolean checkLocalVarExists(String checkFun, String id) {
		for (int i = 0; i < fun.size(); i++) {
			if (fun.get(i).get(2).equals(checkFun)) {
				for (int j = 3; j < fun.get(i).size(); j++) {
					if (fun.get(i).get(j).equals(id)) {
						return true;
					}
				}
			}
		}
		return false;
	}
/*
	public static void delScope(int sc) {
		for (int i = 0; i < var.size(); i++) {
			if(var.get(i).get(2).equals(id) && var.get(i).get(0).startsWith("G")) {

			}
			else if ((var.get(i).get(0).substring(0, var.get(i).get(0).length()) 
					&& Integer.parseInt(var.get(i).get(0).substring(0,1)) <= scope) {
				return true;
			}
		}
		here(815);
		return false;
	}
*/	
	public static boolean checkIfArray(String checkFun, String id) {
		for (int i = 0; i < var.size(); i++) {
			if (var.get(i).get(3).equals(id)) {
				if(var.get(i).get(5).equals("true")) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean checkIfLocalArray(String checkFun, String id) {
		System.out.println(checkFun);
		System.out.println(id);
		System.out.println("_________________");
			for (int i = 0; i < fun.size(); i++) {
				System.out.println(fun.get(i).get(1));
				System.out.println("--------");
				if (fun.get(i).get(2).equals(checkFun)) {
					for(int j = 4; j < fun.get(i).size(); j+=5) {
						System.out.println(fun.get(i).get(j));
						if(fun.get(i).get(j).equals(id)) {
							System.out.println(fun.get(i).get(j+2));
							if(fun.get(i).get(j+2).equals("true")) {
								return true;
							}
						}
					}
				}
			}
		return false;
	}

	// checks if a function already exists
	public static boolean checkFunExists(String id) {
		for (int i = 0; i < fun.size(); i++) {
			if (fun.get(i).get(2).equals(id)) {
				return true;
			}
		}
		return false;
	}
	
	public static String checkFunType(String id, String type) {
		for (int i = 0; i < fun.size(); i++) {
			if(fun.get(i).get(2).equals(id)) {
				if (fun.get(i).get(1).equals(type)) {
					return fun.get(i).get(1);
				}
			}
		}
		return "-1";
	}
	
	public static void delScope(int sc) {
		for (int i = 0; i < var.size(); i++) {
			if(var.get(i).get(0).equals(Integer.toString(sc))) {
				var.remove(i);
				i-=1;
			}
		}
	}
	

	// adds a variable to the variable table
	public static void addVar(String type, String id, boolean isArray) {
		if (!checkVarExists(currFun, id)) {
			ArrayList<String> templist = new ArrayList<String>();
			if (scope == 0) {
				templist.add(Integer.toString(globalscope));
				templist.add("G");
			} else {
				templist.add(Integer.toString(scope));
				templist.add(currFun);
			}
			templist.add(type);
			templist.add(id);
			templist.add("");
			templist.add(Boolean.toString(isArray));
			templist.add("");
			var.add(templist);
			displayVar();
		} else {
			if(reject) {
			rej();
			}
		}
	}

	// adds the size of an array to the array 
	public static void addArrSize(String id, String size) {
		// String locscope = getScope(id);
		for (int i = 0; i < var.size(); i++) {
			if (var.get(i).get(4).equals("true") && var.get(i).get(0).equals(scope)) {
				var.get(i).set(5, size);
				displayVar();
			}
		}
	}

	// sets the value of a variable if set
	public static void setVar(String id, int value) {
		if(checkVarExists(currFun, id) || checkLocalVarExists(currFun, id)) {
		for (int i = 0; i < var.size(); i++) {
			if(var.get(i).get(1).startsWith("G") && var.get(i).get(2).equals(id)) {
				var.get(i).set(3, Integer.toString(value));
			}
			else if(!var.get(i).get(1).startsWith("G")) {
			if (Integer.parseInt(var.get(i).get(0).substring(0, 1)) <= scope && var.get(i).get(2).equals(id)) {
				var.get(i).set(3, Integer.toString(value));
			}
			}
		}
		}
	}

	// displays the variable table in the console
	public static void displayVar() {
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
	public static void displayFun() {
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
	public static void addFunParams(String type, String id, ArrayList<String> params) {
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
			displayFun();
		} else {
			if(reject) {
			rej();
			}
		}
	}

	// adds a function with no parameters to the function table
	public static void addFunNoParams(String type, String id) {
		if (!checkFunExists(id)) {
			ArrayList<String> templist = new ArrayList<String>();
			templist.add(Integer.toString(functionNum));
			functionNum++;
			templist.add(type);
			templist.add(id);
			fun.add(templist);
			displayFun();
		}
	}
	
	private static void here(int i) {
		System.out.println("HERE! " + i);
	}

}