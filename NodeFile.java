class NodeFile{
}

class Node extends NodeFile {
    String nodeType;
    Node[] children = new Node[4];
    //only used if treenode
    public Node(String nodeType){
        this.nodeType = nodeType;
    }
    public String toString(){
        return this.nodeType;
    }
}

class SymbolNode extends Node{
    String nodeType;
    public SymbolNode(String nodeType){
        super(nodeType);
    }
}
//terminals

class TreeNode extends Node{
    String nodeType;
    public TreeNode(String nodeType){
        super(nodeType);
    }
}
//variables

class SizeTreeNode extends TreeNode{
    public SizeTreeNode(Node a, Node b, Node c){
        super("size");
        children[0] = a;
        children[1] = b;
        children[2] = c;
    }
}
class CatTreeNode extends TreeNode{
    public CatTreeNode(Node a, Node b, Node c, Node d){
        super("cat");
        children[0] = a;
        children[1] = b;
        children[2] = c;
        children[3] = d;
    }
}
class MouseTreeNode extends TreeNode{
    public MouseTreeNode(Node a, Node b, Node c, Node d){
        super("mouse");
        children[0] = a;
        children[1] = b;
        children[2] = c;
        children[3] = d;
    }
}
class HoleTreeNode extends TreeNode{
    public HoleTreeNode(Node a, Node b){
        super("hole");
        children[0] = a;
        children[1] = b;
    }
}
class SequenceTreeNode extends TreeNode{
    public SequenceTreeNode(Node a, Node b){
        super("sequence");
        children[0] = a;
        children[1] = b;
    }
}
class MoveTreeNode extends TreeNode{
    public MoveTreeNode(Node a, Node b){
        super("move");
        children[0] = a;
        children[1] = b;
    }
}
class ClockwiseTreeNode extends TreeNode{
    public ClockwiseTreeNode(Node a){
        super("clockwise");
        children[0] = a;
    }
}
class RepeatTreeNode extends TreeNode{
    public RepeatTreeNode(Node a, Node b){
        super("repeat");
        children[0] = a;
        children[1] = b;
    }
}
