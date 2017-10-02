import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Graph extends JFrame {

    private void initUI(){                  //initializing the User Interface
        final surface surface = new surface();
        add(surface);

        setTitle("THESIS!!");
        setSize(1000, 1000);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public Graph(){
        GraphLoader graphLoader = new GraphLoader();
        try {
            graphLoader.read_label();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        initUI();
    }

    class Node{
        String label;
        int nxaxis;
        int nyaxis;
        int numNodes;
        Neighbor adjList;
        boolean selected;

        Node(){
            this.nyaxis = 0;
            this.nxaxis = 0;
            this.label = "NULL";
            this.adjList = null;
            selected  = false;
        }

        Node(String label,Neighbor neighbors){
            this.nxaxis = 0;
            this.nyaxis = 0;
            this.label = label;
            this.adjList = neighbors;
            selected = false;
        }

        public void getaxis (int xaxis,int yaxis){
            nxaxis=xaxis;
            nyaxis=yaxis;
        }

        public void add_label (String s){
            label = s;
        }

        public void get_labels(){
            System.out.println("Label Stored "+label);
            System.out.println("Neighbor Stored : "+adjList);
        }

        public int return_xaxis(){
            return nxaxis;
        }

        public int return_yaxis(){
            return nyaxis;
        }

        public void numNodes (int number){
            this.numNodes=number;
        }
    }

    class Neighbor{
        public int nodeNum;
        public Neighbor next;

        public Neighbor(int nnum, Neighbor nbr) {
            this.nodeNum = nnum;
            next = nbr;
        }
    }

    class surface extends JPanel implements MouseListener {
        private int mStartX, mStartY;   //for last mouse position
        private Node[] n = new Node[5];
        private boolean selected =false;

        public surface(){
            addMouseListener(this);
            mStartX = 0;
            mStartY = 0;
            for (int k=0;k<n.length;k++)    //intializing shape
                n[k]= new Node(null,null);   //always instantiate array to avoid null exception cases
        }

        private void nodeFilling(){ //for equal distances x y axis
            int distance = 0;
            int w = getWidth();
            int h = getHeight();
            Random r = new Random();
            n[0].getaxis(Math.abs(r.nextInt() % w), Math.abs(r.nextInt() % h));
            //node filling algorithm
            for (int i = 1; i<n.length; i++){
                n[i].getaxis(Math.abs(r.nextInt() % w), Math.abs(r.nextInt() % h)); //second node stored in a first node array
                for(int j=0; j<i;j++) {
                    distance = getDistance(n[j].return_xaxis(), n[j].return_yaxis(), n[i].return_xaxis(), n[i].return_yaxis());
                    //System.out.println("Distance"+" "+distance);
                    if ( distance > 100 && distance < 400 ) {   //if you want to cater for more nodes increase the bracket for distance
                        // means node will be added to the list and wont go to the else part of the loop
                    } else{
                        //means distance condition is false and therefore the node is overwritten.
                        i--;
                        break;
                    }
                }
            }
        }

        private void doDrawing (Graphics g){        //Separate method for drawing graphics object override
            Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(Color.RED);
            nodeFilling();  //separate method for checking equal distribution of nodes on the canvas

            for (int i = 0; i < n.length ; i++){    //printing nodes values
                g2d.draw(new Ellipse2D.Double(n[i].return_xaxis(), n[i].return_yaxis(), 20,20));
            }
        }

   /* private void doEdges (Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        g2d.setPaint(Color.RED);
        for (int i = 0; i < n.length ; i++){

            g2d.drawLine();
        }

    }*/

        private void selectDrawing(Graphics g){
            Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(Color.RED);
            for (int i=0;i<n.length;i++){
                if(n[i].selected){
                    g2d.fill(new Ellipse2D.Double(n[i].return_xaxis(), n[i].return_yaxis(), 20,20));    //code for the selected node goes here
                    g2d.drawString("Test Label",n[i].return_xaxis()-15, n[i].return_yaxis()+35);
                    n[i].selected=false;    //to unselected the first node
                } else {
                    g2d.draw(new Ellipse2D.Double(n[i].return_xaxis(), n[i].return_yaxis(), 20,20));
                }
            }

        }

        @Override
        public void paintComponent (Graphics g){
            super.paintComponent(g);
            if(!selected)   //meaning only called once
                doDrawing(g);
            selectDrawing(g);
        }

        public int getDistance(int x1, int y1, int x2, int y2){ //eficient method for distance calculation
            int dx = x2-x1;
            int dy = y2-y1;
            return (int)Math.sqrt(dx*dx+dy*dy);
        }

        @Override
        public void mouseClicked(MouseEvent e) {    //for node selection
            int x = e.getX();
            int y = e.getY();
            int d2 = 0;
            for (int  i = 0; i < n.length ; i++ ){
                d2 = getDistance(n[i].return_xaxis(),n[i].return_yaxis(),x,y);
                if (d2<=50){ //distance between the mouse clicked pointer and the center of the circle
                    n[i].selected = true;
                    selected = true;
                    break;
                } else
                    n[i].selected = false;
            }
            repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

    public class GraphLoader {

        private JFileChooser chooser = new JFileChooser();
        private File selected_file = null;
        private String label = null;
        private ArrayList<String > node_list = new ArrayList<>();
        private int num_nodes = 0;  //number of nodes on top of text file
        Node [] nodes;

        public GraphLoader(){
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "Text File", "txt");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showOpenDialog(null);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                System.out.println("You chose to open this file: " +
                        chooser.getSelectedFile().getAbsolutePath());
            }
            selected_file = chooser.getSelectedFile();
        }

        public File return_selectedfile(){
            return selected_file;
        }

        public void read_label() throws FileNotFoundException, ArrayIndexOutOfBoundsException {

            Scanner s = new Scanner(return_selectedfile());
            num_nodes = s.nextInt();//saving number of nodes on top of the text file
            s.nextLine();

            //LinkedList<String> labels [] = new LinkedList[num_nodes];
            nodes = new Node[num_nodes];

            //read labels
            for (int i = 0; i <nodes.length; i++) {    //adding labels to graph
                nodes[i] = new Node(s.next(),null);
                nodes[i].numNodes(num_nodes);
            }

            //read edges
            while(s.hasNext()) {
                try {
                    int n1 = nameIndex(s.next()); // start node
                    int n2 = nameIndex(s.next()); // end node
                    nodes[n1].adjList = new Neighbor(n2, nodes[n1].adjList);
                    nodes[n2].adjList = new Neighbor(n1, nodes[n2].adjList);
                } catch (Exception e){  //in case of number of nodes are not equal to number of elements stated in the text file
                    System.out.println("exception occured");}
            }
            System.out.println("You have selected => "+selected_file.getAbsolutePath());
            print();
        }

        int nameIndex(String name) {
            for (int n=0; n < nodes.length; n++) {
                if (nodes[n].label.equals(name)) {
                    return n;
                }
            }
            return -1;
        }
        public void print() {
            System.out.println();
            for (int v=0; v < nodes.length; v++) {
                System.out.print(nodes[v].label);
                for (Neighbor nbr=nodes[v].adjList; nbr != null;nbr=nbr.next) {
                    System.out.print(" --> " + nodes[nbr.nodeNum].label);
                }
                System.out.println("\n");
            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException {

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Graph ex = new Graph();
                ex.setVisible(true);
            }
        });
    }

}
