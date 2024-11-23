import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;

public class Project3 extends NodeFile {

    private static Map<String, ArrayList<String>> symbolTable = new HashMap<>();
    // using a hashmap for the symbol table
    private static String[] symbols = { "begin", "halt", "cat", "mouse", "clockwise", "move", "north", "south", "east",
            "west",
            "hole", "repeat", "size", "end" };
    private static String[] nums = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
    private static String[] lettersAndNums = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n",
            "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8",
            "9" };
    private static String[] findColumn = { "z", "i", "b", "t", ";", "c", "v", "m", "h", "o", "l", "r", "d", "n", "s",
            "e", "w", "$", "P", "L", "S", "D" };
    // important arrays to help identify the type of the tokens from the input file

    private static String[][] parseTable = new String[38][23];
    // empty parse table to hold table from text file

    private static String[] ruleTable = { "P'->P", "P->ziibLt", "L->S;", "L->LS;", "S->cviiD", "S->mviiD", "S->hii",
            "S->ov", "S->ovi", "S->lv", "S->riLd", "D->n", "D->s", "D->e", "D->w" };
    private static int[] ruleNonDetermCountTable = { 1, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    //identifies special cases in the ruleTable, 1 if RHS is a single var and no terminals, 2 if RHS is two vars and no terminals, else 0
    private static String[] rulePrintTable = { "P' → P", "P → size int int begin L halt", "L → S ;", "L → L S ;",
            "S → cat var int int D", "S → mouse var int int D", "S → hole int int", "S → move var", "S → move var int",
            "S → clockwise var", "S → repeat int L end", "D → north", "D → south", "D → east", "D → west" };
    // arrays holding information about the rules of the program

    private static Stack<Node> STStack = new Stack<>(); 
    //stack parallel to LR stack in order to create tree

    private static String[][] grid;
    //grid used for the graphics

    private static ArrayList<String> errorList = new ArrayList<String>();
    //arraylist of all errors at the end of the program

    public static void main(String[] args) throws FileNotFoundException {
        fillParseTable(); // creating the LR parse table
        String fileInput = askForFile();
        ArrayList<String> tokenArrayList = fileToArrayList(fileInput);
        // scanThrough(tokenArrayList); *commented out as we do not need to print
        scanThroughWithoutPrint(tokenArrayList);
        // parseLR(createParseList(tokenArrayList)); // parseLR method after putting the
        // tokenArrayList into necessary form
        treeRecursion(parseLR(createParseList(tokenArrayList)));
        // parseLR now returns a refrence to the head node of the program
        // treeRecursion starts on the head node and moves recursively through the tree
        printGrid();
        // printing out the final grid (graphics)
        printErrors();
        // displaying errors
    }

    private static void printErrors() {
        System.out.println("\n\n\nERRORS: (if any)");
        for (int i = 0; i < errorList.size(); i++) {
            System.out.println(errorList.get(i));
        }
        System.out.print("\n\n\n");
    }
    //prints all errors in a list at the end

    private static void printGrid() {
        for (int i = 0; i < grid.length; i++) {
            for (int k = 0; k < grid[0].length; k++) {
                System.out.print("--");
            }
            System.out.print("-");
            System.out.print("\n");
            for (int j = 0; j < grid[0].length; j++) {
                System.out.print("|" + grid[i][j]);
            }
            System.out.print("|\n");
        }
        for (int a = 0; a < grid[0].length; a++) {
            System.out.print("--");
        }
        System.out.print("-");
    }
    //prints the grid in a clean format

    private static void treeRecursion(Node headNode) {
        String currentType = headNode.nodeType;
        if (currentType.equals("size")) { //size node operations (non-leaf)
            Node child0 = headNode.children[0];
            Node child1 = headNode.children[1];
            Node child2 = headNode.children[2];
            grid = new String[Integer.parseInt(child0.nodeType)][Integer.parseInt(child1.nodeType)];
            for (int i = 0; i < grid.length; i++) {
                for (int j = 0; j < grid[0].length; j++) {
                    grid[i][j] = " ";
                }
            }
            treeRecursion(child2);
        } else if (currentType.equals("cat")) { //cat node operations (leaf)
            Node child0 = headNode.children[0];
            Node child1 = headNode.children[1];
            Node child2 = headNode.children[2];
            Node child3 = headNode.children[3];
            int xCoord = Integer.parseInt(child1.nodeType);
            int yCoord = Integer.parseInt(child2.nodeType);
            if ((xCoord < grid[0].length) && (yCoord < grid.length) && (yCoord > -1) && (xCoord > -1)) {
                grid[yCoord][xCoord] = "C";
                symbolTable.get(child0.nodeType).set(2,
                        child1.nodeType + "." + child2.nodeType + ";" + child3.nodeType);
            } else {
                errorList.add("*Cat " + child0.nodeType + " attempted to be placed outside the bounds of the grid*");
                symbolTable.remove(child0.nodeType);
            }
            //if cat was placed outside the grid display an error message, remove from symbol table
        } else if (currentType.equals("mouse")) { //mouse node operations (leaf)
            Node child0 = headNode.children[0];
            Node child1 = headNode.children[1];
            Node child2 = headNode.children[2];
            Node child3 = headNode.children[3];
            int xCoord = Integer.parseInt(child1.nodeType);
            int yCoord = Integer.parseInt(child2.nodeType);
            if ((xCoord < grid[0].length) && (yCoord < grid.length) && (yCoord > -1) && (xCoord > -1)) {
                grid[yCoord][xCoord] = "M";
                symbolTable.get(child0.nodeType).set(2,
                        child1.nodeType + "." + child2.nodeType + ";" + child3.nodeType);
            } else {
                errorList.add("*Mouse " + child0.nodeType + " attempted to be placed outside the bounds of the grid*");
                symbolTable.remove(child0.nodeType);
            }
            //if mouse was placed outside the grid display an error message, remove from symbol table
        } else if (currentType.equals("hole")) { //hole node operations (leaf)
            Node child0 = headNode.children[0];
            Node child1 = headNode.children[1];
            int xCoord = Integer.parseInt(child0.nodeType);
            int yCoord = Integer.parseInt(child1.nodeType);
            if ((xCoord < grid[0].length) && (yCoord < grid.length) && (yCoord > -1) && (xCoord > -1)) {
                grid[yCoord][xCoord] = "H";
            } else {
                errorList.add("*Hole attempted to be placed outside the bounds of the grid*");
            }
            //if hole was placed outside the grid display an error message
        } else if (currentType.equals("sequence")) { //sequence node operations (non-leaf)
            Node child0 = headNode.children[0];
            Node child1 = headNode.children[1];
            treeRecursion(child0);
            treeRecursion(child1);
        } else if (currentType.equals("move")) { //move node operations (leaf)
            Node child0 = headNode.children[0];
            Node child1 = headNode.children[1];
            boolean cantGoFarther = false;
            if (!symbolTable.containsKey(child0.nodeType)) {
                errorList.add("*" + child0.nodeType + " has been removed and cannot be moved*");
            }
            //if a cat or mouse is referenced that is not in the symbol table, display an error message
            String currentPosData = symbolTable.get(child0.nodeType).get(2);
            int xCoord = Integer.parseInt(currentPosData.substring(0, currentPosData.indexOf(".")));
            int yCoord = Integer
                    .parseInt(currentPosData.substring(currentPosData.indexOf(".") + 1, currentPosData.indexOf(";")));
            String direction = currentPosData.substring(currentPosData.indexOf(";") + 1, currentPosData.length());
            int distance = Integer.parseInt(child1.nodeType);

            if (direction.substring(0, 1).equals("n")) { // north
                if (grid[yCoord][xCoord].equals("C")) { // north operations for cat
                    for (int i = 0; i < distance; i++) {
                        if ((xCoord < grid[0].length) && (yCoord < grid.length) && (yCoord > -1) && (xCoord > -1)) {
                            grid[yCoord][xCoord] = "c";
                            //for each move leave a path of c behind
                            if ((i == 0) && (symbolTable.get(child0.nodeType).get(1).equals("1"))) {
                                grid[yCoord][xCoord] = "H";
                            }
                            //if the cat was on top of a hole, replace the hole again
                            yCoord--;
                        } else {
                            errorList.add(
                                    "*Cat " + child0.nodeType + " attempted to move outside the bounds of the grid*");
                                    symbolTable.remove(child0.nodeType);
                            cantGoFarther = true;

                        }
                        //if the cat is trying to go outside the bounds, provide an error message, remove the cat, stop the loop
                        if (cantGoFarther) {
                            i = distance;
                        }
                        //stopping the loop
                    }
                    if (((xCoord < grid[0].length) && (yCoord < grid.length) && (yCoord > -1) && (xCoord > -1))) {
                        if (grid[yCoord][xCoord].equals("C")) {
                            errorList.add("*Cat " + child0.nodeType
                                    + " attempted to move inside the same square as another cat*");
                            symbolTable.remove(child0.nodeType);
                            //if the cat tries to go in the same square as another cat, provide an error message, remove the second cat
                        } else if (grid[yCoord][xCoord].equals("H")) {
                            symbolTable.get(child0.nodeType).set(1, "1");
                            grid[yCoord][xCoord] = "C";
                            //if the cat is over a hole, identify this in the symbol table, replace hole with a C for now
                        } else {
                            grid[yCoord][xCoord] = "C";
                            //otherwise display the cats current location with a C
                        }
                    }
                } else if (grid[yCoord][xCoord].equals("M") || grid[yCoord][xCoord].equals("H")) { // north operations for mouse
                    for (int i = 0; i < distance; i++) {
                        if ((xCoord < grid[0].length) && (yCoord < grid.length) && (yCoord > -1) && (xCoord > -1)) {
                            grid[yCoord][xCoord] = "m";
                            //for each move leave a path of m behind
                            if ((i == 0) && (symbolTable.get(child0.nodeType).get(1).equals("1"))) {
                                grid[yCoord][xCoord] = "H";
                            }
                            //if the mouse was in a hole, make sure the hole is kept and not replaced by the path
                            yCoord--;
                        } else {
                            errorList.add(
                                    "*Mouse " + child0.nodeType + " attempted to move outside the bounds of the grid*");
                                    symbolTable.remove(child0.nodeType);
                            cantGoFarther = true;
                        }
                        // if the mouse is trying to go out of bounds, provide an error message, remove the mouse, stop the loop
                        if (cantGoFarther) {
                            i = distance;
                        }
                        //stopping the loop
                    }
                    if (((xCoord < grid[0].length) && (yCoord < grid.length) && (yCoord > -1) && (xCoord > -1))) {
                        if (grid[yCoord][xCoord].equals("M")) {
                            errorList.add("*Mouse " + child0.nodeType
                                    + " attempted to move inside the same square as another mouse*");
                            symbolTable.remove(child0.nodeType);
                            //if the mouse tries to go in the same square as another mouse, provide an error message, remove the second cat
                        } else if (grid[yCoord][xCoord].equals("H")) {
                            symbolTable.get(child0.nodeType).set(1, "1");
                            grid[yCoord][xCoord] = "H";
                            //if the mouse is over a hole, identify this in the symbol table, make the square visual be a hole
                        } else if (grid[yCoord][xCoord].equals("C")) {
                            symbolTable.remove(child0.nodeType);
                            //if the mouse is on a cat, the mouse is eaten, remove the mouse
                        }

                        else {
                            grid[yCoord][xCoord] = "M";
                            //otherwise display the mouses current location with a M
                        }
                    }
                }
                if (symbolTable.containsKey(child0.nodeType)) {
                    symbolTable.get(child0.nodeType).set(2, xCoord + "." + yCoord + ";" + direction);
                }
                //enter the new coordinates of the cat or mouse into the symbol table
            } else if (direction.substring(0, 1).equals("e")) { // east
                if (grid[yCoord][xCoord].equals("C")) { // east operations for cat
                    for (int i = 0; i < distance; i++) {
                        if ((xCoord < grid[0].length) && (yCoord < grid.length) && (yCoord > -1) && (xCoord > -1)) {
                            grid[yCoord][xCoord] = "c";
                            if ((i == 0) && (symbolTable.get(child0.nodeType).get(1).equals("1"))) {
                                grid[yCoord][xCoord] = "H";
                            }
                            xCoord++;
                        } else {
                            errorList.add(
                                    "*Cat " + child0.nodeType + " attempted to move outside the bounds of the grid*");
                                    symbolTable.remove(child0.nodeType);
                            cantGoFarther = true;

                        }
                        if (cantGoFarther) {
                            i = distance;
                        }
                    }
                    if (((xCoord < grid[0].length) && (yCoord < grid.length) && (yCoord > -1) && (xCoord > -1))) {
                        if (grid[yCoord][xCoord].equals("C")) {
                            errorList.add("*Cat " + child0.nodeType
                                    + " attempted to move inside the same square as another cat*");
                            symbolTable.remove(child0.nodeType);
                        } else if (grid[yCoord][xCoord].equals("H")) {
                            symbolTable.get(child0.nodeType).set(1, "1");
                            grid[yCoord][xCoord] = "C";
                        } else {
                            grid[yCoord][xCoord] = "C";
                        }
                    }
                } else if (grid[yCoord][xCoord].equals("M") || grid[yCoord][xCoord].equals("H")) { // east operations for mouse
                    for (int i = 0; i < distance; i++) {
                        if ((xCoord < grid[0].length) && (yCoord < grid.length) && (yCoord > -1) && (xCoord > -1)) {
                            grid[yCoord][xCoord] = "m";
                            if ((i == 0) && (symbolTable.get(child0.nodeType).get(1).equals("1"))) {
                                grid[yCoord][xCoord] = "H";
                            }
                            xCoord++;
                        } else {
                            errorList.add(
                                    "*Mouse " + child0.nodeType + " attempted to move outside the bounds of the grid*");
                                    symbolTable.remove(child0.nodeType);
                            cantGoFarther = true;

                        }
                        if (cantGoFarther) {
                            i = distance;
                        }
                    }
                    if (((xCoord < grid[0].length) && (yCoord < grid.length) && (yCoord > -1) && (xCoord > -1))) {
                        if (grid[yCoord][xCoord].equals("M")) {
                            errorList.add("*Mouse " + child0.nodeType
                                    + " attempted to move inside the same square as another mouse*");
                            symbolTable.remove(child0.nodeType);
                        } else if (grid[yCoord][xCoord].equals("H")) {
                            symbolTable.get(child0.nodeType).set(1, "1");
                            grid[yCoord][xCoord] = "H";
                        } else if (grid[yCoord][xCoord].equals("C")) {
                            symbolTable.remove(child0.nodeType);
                        }

                        else {
                            grid[yCoord][xCoord] = "M";
                        }
                    }
                }
                if (symbolTable.containsKey(child0.nodeType)) {
                    symbolTable.get(child0.nodeType).set(2, xCoord + "." + yCoord + ";" + direction);
                }
            } else if (direction.substring(0, 1).equals("s")) { // south
                if (grid[yCoord][xCoord].equals("C")) { // south operations for cat
                    for (int i = 0; i < distance; i++) {
                        if ((xCoord < grid[0].length) && (yCoord < grid.length) && (yCoord > -1) && (xCoord > -1)) {
                            grid[yCoord][xCoord] = "c";
                            if ((i == 0) && (symbolTable.get(child0.nodeType).get(1).equals("1"))) {
                                grid[yCoord][xCoord] = "H";
                            }
                            yCoord++;
                        } else {
                            errorList.add(
                                    "*Cat " + child0.nodeType + " attempted to move outside the bounds of the grid*");
                                    symbolTable.remove(child0.nodeType);
                            cantGoFarther = true;

                        }
                        if (cantGoFarther) {
                            i = distance;
                        }
                    }
                    if (((xCoord < grid[0].length) && (yCoord < grid.length) && (yCoord > -1) && (xCoord > -1))) {
                        if (grid[yCoord][xCoord].equals("C")) {
                            errorList.add("*Cat " + child0.nodeType
                                    + " attempted to move inside the same square as another cat*");
                            symbolTable.remove(child0.nodeType);
                        } else if (grid[yCoord][xCoord].equals("H")) {
                            symbolTable.get(child0.nodeType).set(1, "1");
                            grid[yCoord][xCoord] = "C";
                        } else {
                            grid[yCoord][xCoord] = "C";
                        }
                    }
                } else if (grid[yCoord][xCoord].equals("M") || grid[yCoord][xCoord].equals("H")) { // south operations for mouse
                    for (int i = 0; i < distance; i++) {
                        if ((xCoord < grid[0].length) && (yCoord < grid.length) && (yCoord > -1) && (xCoord > -1)) {
                            grid[yCoord][xCoord] = "m";
                            if ((i == 0) && (symbolTable.get(child0.nodeType).get(1).equals("1"))) {
                                grid[yCoord][xCoord] = "H";
                            }
                            yCoord++;
                        } else {
                            errorList.add(
                                    "*Mouse " + child0.nodeType + " attempted to move outside the bounds of the grid*");
                                    symbolTable.remove(child0.nodeType);
                            cantGoFarther = true;

                        }
                        if (cantGoFarther) {
                            i = distance;
                        }
                    }
                    if (((xCoord < grid[0].length) && (yCoord < grid.length) && (yCoord > -1) && (xCoord > -1))) {
                        if (grid[yCoord][xCoord].equals("M")) {
                            errorList.add("*Mouse " + child0.nodeType
                                    + " attempted to move inside the same square as another mouse*");
                            symbolTable.remove(child0.nodeType);
                        } else if (grid[yCoord][xCoord].equals("H")) {
                            symbolTable.get(child0.nodeType).set(1, "1");
                            grid[yCoord][xCoord] = "H";
                        } else if (grid[yCoord][xCoord].equals("C")) {
                            symbolTable.remove(child0.nodeType);
                        }

                        else {
                            grid[yCoord][xCoord] = "M";
                        }
                    }
                }
                if (symbolTable.containsKey(child0.nodeType)) {
                    symbolTable.get(child0.nodeType).set(2, xCoord + "." + yCoord + ";" + direction);
                }
            } else { // west
                if (grid[yCoord][xCoord].equals("C")) { // west operations for cat
                    for (int i = 0; i < distance; i++) {
                        if ((xCoord < grid[0].length) && (yCoord < grid.length) && (yCoord > -1) && (xCoord > -1)) {
                            grid[yCoord][xCoord] = "c";
                            if ((i == 0) && (symbolTable.get(child0.nodeType).get(1).equals("1"))) {
                                grid[yCoord][xCoord] = "H";
                            }
                            xCoord--;
                        } else {
                            errorList.add(
                                    "*Cat " + child0.nodeType + " attempted to move outside the bounds of the grid*");
                                    symbolTable.remove(child0.nodeType);
                            cantGoFarther = true;

                        }
                        if (cantGoFarther) {
                            i = distance;
                        }
                    }
                    if (((xCoord < grid[0].length) && (yCoord < grid.length) && (yCoord > -1) && (xCoord > -1))) {
                        if (grid[yCoord][xCoord].equals("C")) {
                            errorList.add("*Cat " + child0.nodeType
                                    + " attempted to move inside the same square as another cat*");
                            symbolTable.remove(child0.nodeType);
                        } else if (grid[yCoord][xCoord].equals("H")) {
                            symbolTable.get(child0.nodeType).set(1, "1");
                            grid[yCoord][xCoord] = "C";
                        } else {
                            grid[yCoord][xCoord] = "C";
                        }
                    }
                } else if (grid[yCoord][xCoord].equals("M") || grid[yCoord][xCoord].equals("H")) { // west operations for mouse
                    for (int i = 0; i < distance; i++) {
                        if ((xCoord < grid[0].length) && (yCoord < grid.length) && (yCoord > -1) && (xCoord > -1)) {
                            grid[yCoord][xCoord] = "m";
                            if ((i == 0) && (symbolTable.get(child0.nodeType).get(1).equals("1"))) {
                                grid[yCoord][xCoord] = "H";
                            }
                            xCoord--;
                        } else {
                            errorList.add(
                                    "*Mouse " + child0.nodeType + " attempted to move outside the bounds of the grid*");
                                    symbolTable.remove(child0.nodeType);
                            cantGoFarther = true;

                        }
                        if (cantGoFarther) {
                            i = distance;
                        }
                    }
                    if (((xCoord < grid[0].length) && (yCoord < grid.length) && (yCoord > -1) && (xCoord > -1))) {
                        if (grid[yCoord][xCoord].equals("M")) {
                            errorList.add("*Mouse " + child0.nodeType
                                    + " attempted to move inside the same square as another mouse*");
                            symbolTable.remove(child0.nodeType);
                        } else if (grid[yCoord][xCoord].equals("H")) {
                            symbolTable.get(child0.nodeType).set(1, "1");
                            grid[yCoord][xCoord] = "H";
                        } else if (grid[yCoord][xCoord].equals("C")) {
                            symbolTable.remove(child0.nodeType);
                        }

                        else {
                            grid[yCoord][xCoord] = "M";
                        }
                    }
                }
                if (symbolTable.containsKey(child0.nodeType)) {
                    symbolTable.get(child0.nodeType).set(2, xCoord + "." + yCoord + ";" + direction);
                }
            }

        } else if (currentType.equals("clockwise")) { //clockwise node operations (leaf)
            Node child0 = headNode.children[0];
            if (!symbolTable.containsKey(child0.nodeType)) {
                errorList.add("*" + child0.nodeType + " has been removed and cannot be rotated*");
                //if a cat or mouse is referenced that is not in the symbol table, display an error message
            } else {
                String currentPosData = symbolTable.get(child0.nodeType).get(2);
                int xCoord = Integer.parseInt(currentPosData.substring(0, currentPosData.indexOf(".")));
                int yCoord = Integer
                        .parseInt(
                                currentPosData.substring(currentPosData.indexOf(".") + 1, currentPosData.indexOf(";")));
                String direction = currentPosData.substring(currentPosData.indexOf(";") + 1, currentPosData.length());
                if (direction.equals("north")) {
                    direction = "east";
                } else if (direction.equals("east")) {
                    direction = "south";
                } else if (direction.equals("south")) {
                    direction = "west";
                } else {
                    direction = "north";
                }
                symbolTable.get(child0.nodeType).set(2, xCoord + "." + yCoord + ";" + direction);
            }
        } else if (currentType.equals("repeat")) { //repeat node operations (non-leaf)
            Node child0 = headNode.children[0];
            Node child1 = headNode.children[1];
            int repeatAmount = Integer.parseInt(child0.nodeType);
            for (int i = 1; i <= repeatAmount; i++) {
                treeRecursion(child1);
            }

        } else {
            System.out.println("Node Identification Failure"); //should never get here
            System.exit(0);
        }

    }

    private static ArrayList<String> createParseList(ArrayList<String> tokenArrayList) { // simply formats the
        // tokenArrayList into a form
        // applicable for the LR parser
        ArrayList<String> parseTokenList = new ArrayList<String>();
        for (String element : tokenArrayList) {
            parseTokenList.add(element.substring(0, element.indexOf(" ")));
        }
        return parseTokenList;
    }

    private static void scanThroughWithoutPrint(ArrayList<String> tokenArrayList) { // performs the actions of scanner
        // without the prints
        for (int i = 0; i < tokenArrayList.size(); i++) {
            tokenMethod(tokenArrayList.get(i));
        }
    }

    private static Node parseLR(ArrayList<String> parseTokenList) { // LR parsing method

        Stack<String> ruleStack = new Stack<>();
        ruleStack.push("Z");
        Stack<String> parsingStack = new Stack<>();
        parsingStack.push("0");
        // setting up the necessary stacks

        String lookahead = "";
        String topOfStack = "";
        int row = 0;
        int column = 0;
        // important variables to be used later in determining next operation

        String[] parseTokenArray = new String[parseTokenList.size() + 1];

        for (int i = 0; i < parseTokenArray.length - 1; i++) {
            parseTokenArray[i] = parseTokenList.get(i);
        }
        parseTokenArray[parseTokenList.size()] = "$";
        // creating an array of tokens with the $ at the end to signify we have moved
        // through the entire file

        for (int i = 0; i < parseTokenArray.length; i++) {
            String token = parseTokenArray[i];
            lookahead = token.toLowerCase();                                   
            column = determineColumn(lookahead);
            topOfStack = parsingStack.peek();
            row = Integer.parseInt(topOfStack);
            // the necessary information to determine the next instruction

            String instruction = parseTable[row][column];
            // the next instruction

            if (instruction == null) { // LR parse fail
                System.out.println("\n\nFile NOT ACCEPTED! (it is not syntactically correct)\n\n");
                System.exit(0);

            } else if (instruction.substring(0, 1).equals("a")) { // accept operation
                System.out.println("\n\n      File ACCEPTED!");

            } else if (instruction.substring(0, 1).equals("s")) { // shift operation
                parsingStack.push(findColumn[column]);
                parsingStack.push(instruction.substring(1, instruction.length()));

                if (lookahead.equals("north") || lookahead.equals("east") || lookahead.equals("south")
                        || lookahead.equals("west")) {
                    SymbolNode terminal = new SymbolNode(lookahead);
                    STStack.push(terminal);
                } else if (symbolTable.containsKey(lookahead)) {
                    SymbolNode terminal = new SymbolNode(lookahead);
                    STStack.push(terminal);
                }
                //if the lookahead is in the symbol table, push to STS stack on shift (directions are rare excepetions)


            } else if (instruction.substring(0, 1).equals("r")) {
                // reduce operation
                int ruleNumber = Integer.parseInt(instruction.substring(1, instruction.length()));
                String ruleToUse = ruleTable[ruleNumber];
                ruleStack.push(instruction.substring(1, instruction.length()));
                String rightSide = ruleToUse.substring(ruleToUse.indexOf(">") + 1, ruleToUse.length());

                Node[] possibleChildren = new Node[4];
                Node[] possibleChildrenCO = new Node[4];
                boolean varsZero = false;
                String nodeType = "";
                int numOfRHSVars = ruleNonDetermCountTable[ruleNumber];
                boolean moveAmountProvided = false;
                if (numOfRHSVars == 2) {
                    Node secondTree = STStack.pop();
                    Node firstTree = STStack.pop();
                    TreeNode comboTree = new SequenceTreeNode(firstTree, secondTree);
                    STStack.push(comboTree);
                    //merging two trees together and pushing onto stack
                } else if (numOfRHSVars == 1) {
                    //don't have to do anything
                } else {
                    nodeType = rightSide.substring(0, 1);
                    varsZero = true;
                    if (ruleNumber == 8) {
                        moveAmountProvided = true;
                        //special case for move
                    }
                }

                for (int a = rightSide.length() - 1; a > -1; a--) {
                    parsingStack.pop();
                    String secondPop = parsingStack.pop();

                    if (!(secondPop.equals(rightSide.substring(a, a + 1)))) { // should be equal: LR Parse Fail
                        System.out.println("File NOT ACCEPTED! (it is not syntactically correct)");
                        System.exit(0);
                    }
                }

                int b = 0;
                if (nodeType.equals("z")) {
                    b = 3;
                } else if (nodeType.equals("c")) {
                    b = 4;
                } else if (nodeType.equals("m")) {
                    b = 4;
                } else if (nodeType.equals("h")) {
                    b = 2;
                } else if (nodeType.equals("L")) {
                    b = 2;
                } else if (nodeType.equals("o") && (moveAmountProvided)) {
                    b = 2;
                } else if (nodeType.equals("o") && (!moveAmountProvided)) {
                    b = 1;
                } else if (nodeType.equals("l")) {
                    b = 1;
                } else if (nodeType.equals("n") || nodeType.equals("e") || nodeType.equals("s")
                        || nodeType.equals("w")) {

                } else {
                    b = 2;
                }
                //finding how many nodes need to be popped off STStack

                int k = 0;
                while (b >= 1) {

                    if (varsZero) {
                        possibleChildren[k] = STStack.pop();
                        k++; 
                    }
                    b--;
                    //popping nodes and adding them as children to new parent node

                }
                if (varsZero) {
                    k--;
                    int c = 0;
                    while (k >= 0) {
                        possibleChildrenCO[c] = possibleChildren[k];
                        c++;
                        k--;
                    }
                    //flipping the order of the array for better formatting (stack provides nodes in reverse)
                    Node reduceNode = new Node("");
                    if (nodeType.equals("z")) {
                        reduceNode = new SizeTreeNode(possibleChildrenCO[0], possibleChildrenCO[1],
                                possibleChildrenCO[2]);
                        STStack.push(reduceNode);
                    } else if (nodeType.equals("c")) {
                        reduceNode = new CatTreeNode(possibleChildrenCO[0], possibleChildrenCO[1],
                                possibleChildrenCO[2], possibleChildrenCO[3]);
                        STStack.push(reduceNode);
                    } else if (nodeType.equals("m")) {
                        reduceNode = new MouseTreeNode(possibleChildrenCO[0], possibleChildrenCO[1],
                                possibleChildrenCO[2], possibleChildrenCO[3]);
                        STStack.push(reduceNode);
                    } else if (nodeType.equals("h")) {
                        reduceNode = new HoleTreeNode(possibleChildrenCO[0], possibleChildrenCO[1]);
                        STStack.push(reduceNode);
                    } else if (nodeType.equals("L")) {
                        reduceNode = new SequenceTreeNode(possibleChildrenCO[0], possibleChildrenCO[1]);
                        STStack.push(reduceNode);
                    } else if (nodeType.equals("o") && (moveAmountProvided)) {
                        reduceNode = new MoveTreeNode(possibleChildrenCO[0], possibleChildrenCO[1]);
                        STStack.push(reduceNode);
                    } else if (nodeType.equals("o") && (!moveAmountProvided)) {
                        SymbolNode single = new SymbolNode("1");
                        reduceNode = new MoveTreeNode(possibleChildrenCO[0], single);
                        STStack.push(reduceNode);
                    } else if (nodeType.equals("l")) {
                        reduceNode = new ClockwiseTreeNode(possibleChildrenCO[0]);
                        STStack.push(reduceNode);
                    } else if (nodeType.equals("n") || nodeType.equals("e") || nodeType.equals("s")
                            || nodeType.equals("w")) {

                    } else {
                        reduceNode = new RepeatTreeNode(possibleChildrenCO[0], possibleChildrenCO[1]);
                        STStack.push(reduceNode);
                    }
                    //pushing new parent node to stack with appropriate children
                }
                String nodeAfterLS = parsingStack.peek();
                String leftSide = ruleToUse.substring(0, 1);
                parsingStack.push(leftSide);

                row = Integer.parseInt(nodeAfterLS);
                column = determineColumn(leftSide);
                parsingStack.push(parseTable[row][column]);
                i--; // lookahead should not move since it was a reduce

            } else { // variable shift operation
                parsingStack.push(findColumn[column]);
                parsingStack.push(instruction);
                if (lookahead.equals("north") || lookahead.equals("east") || lookahead.equals("south")
                        || lookahead.equals("west")) {
                    SymbolNode terminal = new SymbolNode(lookahead);
                    STStack.push(terminal);
                } else if (symbolTable.containsKey(lookahead)) {
                    SymbolNode terminal = new SymbolNode(lookahead);
                    STStack.push(terminal);
                }
                //if the lookahead is in the symbol table, push to STS stack on shift (directions are rare excepetions)


            }
        }

        /*System.out.println("");
        System.out.println("          RULES");
        System.out.println("-------------------------");
        System.out.println("");
        while (!ruleStack.peek().equals("Z")) {
            String rule = ruleStack.pop();
            System.out.println(rulePrintTable[Integer.parseInt(rule)]);
        }*/
        // commented out since we do not need to print the rules now

        return (STStack.peek());

        // printing out the rule stack at the end to show the rules throughout the
        // derivation
    }

    private static int determineColumn(String thisLookAhead) { // finding the column number from the parse table using
                                                               // the column's character value
        String lookahead = thisLookAhead.toLowerCase();
        if (symbolTable.containsKey(lookahead)) {
            if (symbolTable.get(lookahead).get(0).equals("integer")) { // checking to see if the lookahead is in the
                                                                       // symbol
                // table, if so, recognize its type
                return 1;
            } else {
                return 6;
            }
        } else {
            String firstLetter = "";
            if (thisLookAhead.equals("size")) { // special case
                firstLetter = "z";
            } else if (thisLookAhead.equals("move")) { // special case
                firstLetter = "o";
            } else if (thisLookAhead.equals("clockwise")) { // special case
                firstLetter = "l";
            } else if (thisLookAhead.equals("end")) { // special case
                firstLetter = "d";
            } else if (thisLookAhead.equals("halt")) { // special case
                firstLetter = "t";
            } else {
                firstLetter = thisLookAhead.substring(0, 1);
            }
            for (int i = 0; i < findColumn.length; i++) { // any of these columns are determined by the first character
                                                          // of the token
                if (firstLetter.equals(findColumn[i])) {
                    return i;
                }
            }
            return 999; // if we can't find the column number, return a value that will be out of bounds
        }
    }

    private static void fillParseTable() throws FileNotFoundException { // method to construct LR parse table from text
                                                                        // file provided
        Scanner scannerParse = new Scanner(new File("parsedata.txt"));
        scannerParse.nextLine();
        int currentLineIndex = 1;
        while (scannerParse.hasNextLine()) {
            currentLineIndex++;
            String currentLine = scannerParse.nextLine();

            if (currentLineIndex < 12) { // case of the first 10 rows (must offset by a two characters at the start)
                int row = currentLineIndex - 2;
                int column = 0;
                for (int i = 2; i < currentLine.length(); i++) {
                    if (currentLine.substring(i, i + 1).equals("&")) {
                        column++;
                    } else {
                        if ((currentLine.length() <= i + 2) || (currentLine.substring(i + 2, i + 3).equals("&"))) {
                            parseTable[row][column] = currentLine.substring(i, i + 2);
                            i = i + 1;
                        } else {
                            parseTable[row][column] = currentLine.substring(i, i + 3);
                            i = i + 2;
                        }
                    }
                }

            } else if (currentLineIndex < 40) { // case of the rest of the 38 rows (must offset by three characters at
                                                // the
                                                // start)
                int row = currentLineIndex - 2;
                int column = 0;
                for (int i = 3; i < currentLine.length(); i++) {
                    if (currentLine.substring(i, i + 1).equals("&")) {
                        column++;
                    } else {
                        if ((currentLine.length() <= i + 2) || (currentLine.substring(i + 2, i + 3).equals("&"))) {
                            parseTable[row][column] = currentLine.substring(i, i + 2);
                            i = i + 1;
                        } else {
                            parseTable[row][column] = currentLine.substring(i, i + 3);
                            i = i + 2;
                        }
                    }
                }

            } else if ((currentLineIndex > 40) && (currentLineIndex < 51)) { // case of the first 10 rows within the
                                                                             // variable section of the table (must
                                                                             // offset by two characters at the
                                                                             // start AND reset the row value)
                int row = currentLineIndex - 41;
                int column = 18;
                for (int i = 2; i < currentLine.length(); i++) {
                    if (currentLine.substring(i, i + 1).equals("&")) {
                        column++;
                    } else {
                        if ((currentLine.length() <= i + 1) || (currentLine.substring(i + 1, i + 2).equals("&"))) {
                            parseTable[row][column] = currentLine.substring(i, i + 1);
                            i = i + 0;
                        } else {
                            parseTable[row][column] = currentLine.substring(i, i + 2);
                            i = i + 1;
                        }
                    }
                }

            } else if (currentLineIndex >= 51) {// case of the first rest of the rows within the
                                                // variable section of the table (must
                                                // offset by three characters at the
                                                // start AND reset the row value)
                int row = currentLineIndex - 41;
                int column = 18;
                for (int i = 3; i < currentLine.length(); i++) {
                    if (currentLine.substring(i, i + 1).equals("&")) {
                        column++;
                    } else {
                        if ((currentLine.length() <= i + 1) || (currentLine.substring(i + 1, i + 2).equals("&"))) {
                            parseTable[row][column] = currentLine.substring(i, i + 1);
                            i = i + 0;
                        } else {
                            parseTable[row][column] = currentLine.substring(i, i + 2);
                            i = i + 1;
                        }
                    }
                }

            } else {

            }
        }
        scannerParse.close();
    }

    private static String askForFile() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please provide a text file to test.");
        String file = scanner.nextLine();
        scanner.close();
        return file;
    }
    // prompts the user for text file

    private static ArrayList<String> fileToArrayList(String inputFile) throws FileNotFoundException {
        File file = new File(inputFile);
        Scanner scanner = new Scanner(file);
        String token = "";
        ArrayList<String> tokenList = new ArrayList<>();
        int currentLineIndex = 0;
        while (scanner.hasNextLine()) {
            String currentLine = scanner.nextLine();
            currentLineIndex++;
            if (!currentLine.equals("")) {
                for (int i = 0; i < currentLine.length(); i++) {
                    if ((currentLine.substring(i, i + 1)).equals(" ")
                            || (currentLine.substring(i, i + 1)).equals("\t")) { // any space or tab means a new token
                        tokenList.add(token + " ." + currentLineIndex);
                        token = "";
                    } else if ((currentLine.substring(i, i + 1)).equals("/")) { // a dash suggests a comment, so we
                                                                                // break to the next line
                        tokenList.add(token + " ." + currentLineIndex);
                        token = "";
                        break;
                    } else {
                        token = token + currentLine.substring(i, i + 1); // anything else we add to the current token
                    }
                }
                tokenList.add(token + " ." + currentLineIndex); // saving location
                token = "";
            }
        }
        // method to enter individual tokens into the arraylist, along with saving the
        // line number after "." in case it needs to be referenced for causing an error
        scanner.close();
        int changingSize = tokenList.size();
        for (int i = 0; i < changingSize; i++) {
            if ((tokenList.get(i).substring(0, 1)).equals(" ") || (tokenList.get(i).substring(0, 1)).equals("\t")) {
                tokenList.remove(i);
                i--;
                changingSize--;
            }
        }
        // removes any extra spaces or tabs that may have been added to the arraylist
        return tokenList;
    }
    // we now have an accurate arraylist of token elements

    private static void scanThrough(ArrayList<String> tokenArrayList) {
        System.out.println("TYPE              CH VALUE            INT VALUE");
        System.out.println("====             ==========          ===========");
        String tokenReference = "";
        for (int i = 0; i < tokenArrayList.size(); i++) {
            tokenReference = tokenMethod(tokenArrayList.get(i));

            // commenting out the printing portion of the scanner as it is not needed for
            // this part of the assignment:

            if (symbolTable.containsKey(tokenReference)) {
                String howManySpaces = "                                    ";
                String currentTokenPrintable = tokenReference;
                if (tokenReference.length() > 10) {
                    currentTokenPrintable = tokenReference.substring(0, 10); // truncate name in case it
                                                                             // is too long
                }
                howManySpaces = howManySpaces.substring(0, 20 - currentTokenPrintable.length()); // adjusting
                                                                                                 // spacing for
                                                                                                 // size
                if (symbolTable.get(tokenReference).equals("integer")) {
                    System.out.println(
                            "" + symbolTable.get(tokenReference) + "          " + currentTokenPrintable + howManySpaces
                                    + currentTokenPrintable);
                } else {
                    System.out.println(
                            "" + symbolTable.get(tokenReference) + "         " + currentTokenPrintable + howManySpaces
                                    + "0"); // all variables have 0 as their int value

                }
            }
            // printing out variables and integers using their reference in the symbol
            // table.
            else {
                System.out.println(tokenReference);
            }
            // printing out symbols and semi-colons
        }
    }

    private static String tokenMethod(String token) {
        String currentTokenData = token.toLowerCase();
        String currentToken = currentTokenData.substring(0, currentTokenData.indexOf(" "));
        boolean isSymb = false;
        boolean isPunct = false;
        boolean isNum = false;
        boolean isVar = false;
        // keeps track of what type token is identified as
        for (int j = 0; j < symbols.length; j++) {
            if (currentToken.equals(symbols[j])) {
                isSymb = true;

                return (currentToken);
            }
        }
        // checks to see if the token is a symbol, if so we can just return it
        if (!isSymb) {
            if (currentToken.equals(";")) {
                isPunct = true;
                return (currentToken);
            }
        }
        // checks to see if the token is a punctuation, if so we can just return it
        int numDigits = 0;
        if (!isPunct && !isSymb && (currentToken.length() < 4)) {
            for (int j = 0; j < currentToken.length(); j++) {
                for (int k = 0; k < nums.length; k++) {
                    if (currentToken.substring(j, j + 1).equals(nums[k])) {
                        numDigits++;
                        break;
                    }
                }
            } // check to make sure it is a valid integer
            if (numDigits == currentToken.length()) {
                if (!symbolTable.containsKey(currentToken)) {
                    ArrayList<String> toPutInt = new ArrayList<String>();
                    toPutInt.add("integer");
                    toPutInt.add(currentToken);
                    toPutInt.add(currentToken);
                    symbolTable.put(currentToken, toPutInt); // entering it into the symbol table
                }
                // checks to see if it was entered into the symbol table
                isNum = true;
                return (currentToken);
                // reference (key) to the tokens location is currentToken
                // token type (value) is "integer", can be referenced by
                // symbolTable.get(currentToken); effectively returning both the reference and
                // the type
                // reference is returned for printing
            }
        }
        int numLettersNums = 0;
        if (!isNum & !isPunct & !isSymb) {
            for (int j = 0; j < currentToken.length(); j++) {
                for (int k = 0; k < lettersAndNums.length; k++) {
                    if (currentToken.substring(j, j + 1).equals(lettersAndNums[k])) {
                        numLettersNums++;
                        break;
                    }
                }
            } // check to make sure it is a valid variable
            if (numLettersNums == currentToken.length()) {
                if (!symbolTable.containsKey(currentToken)) {
                    ArrayList<String> toPutVariable = new ArrayList<String>();
                    toPutVariable.add("variable");
                    toPutVariable.add("0"); // replaced currentToken with 0, should be fine.
                    toPutVariable.add("0");
                    symbolTable.put(currentToken, toPutVariable); // entering it into the symbol table
                }
                // checks to see if it was entered into the symbol table
                isVar = true;
                return (currentToken);
                // reference (key) to the tokens location is currentToken
                // token type (value) is "variable", can be referenced by
                // symbolTable.get(currentToken); effectively returning both the reference and
                // the type
                // reference is returned for printing
            }
        }

        if (!isNum && !isPunct && !isSymb && !isVar) {
            // System.out.println("\nERROR: Token " + currentToken + " on line " +
            // currentTokenData.substring(currentTokenData.indexOf(" ") + 2,
            // currentTokenData.length())
            // + " is not a valid token.");
            // if the token is still not identified, we print an error and the line it was
            // found on
        }
        // commented out as these printed statements are mot needed for this assignment

        return ("");
    }
}
