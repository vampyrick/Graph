import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

public class Graph extends JFrame {
    private int num_nodes = 0;
    Node [] nodes;  //global nodes
    private int num_edges = 0;
    Edge [] edges;
    int mX = 0;
    int mY = 0;
    JPanel jp;

    private void initUI(){                  //initializing the User Interface
        final surface surface = new surface();
        add(surface);

        setTitle("Graph");
        setSize(1000, 1000);
        setBackground(Color.RED);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public static void main(String[] args) throws FileNotFoundException {

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Graph ex = new Graph();
                ex.printGraph();
                ex.setVisible(true);
            }
        });
    }

    private void printGraph() {
        System.out.println();
        for (int v=0; v < nodes.length; v++) {
            System.out.print(nodes[v].label);
            for (Neighbor nbr=nodes[v].adjList; nbr != null;nbr=nbr.next) {
                System.out.print(" --> " + nodes[nbr.nodeNum].label);
            }
            System.out.println("\n");
        }
        System.out.println(num_edges);
    }

    private Graph(){
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

    class surface extends JPanel implements MouseListener, MouseMotionListener {
        private int mStartX, mStartY;   //for last mouse position
        private boolean selected =false;

        public surface(){
            addMouseListener(this);
            mStartX = 0;
            mStartY = 0;
        }

        private void nodeFilling(){ //for equal distances x y axis
            int distance = 0;
            int w = getWidth();
            int h = getHeight();
            Random r = new Random();
            nodes[0].getaxis(Math.abs(r.nextInt() % w), Math.abs(r.nextInt() % h));
            //node filling algorithm
            for (int i = 1; i<nodes.length; i++){
                nodes[i].getaxis(Math.abs(r.nextInt() % w), Math.abs(r.nextInt() % h)); //second node stored in a first node array
                for(int j=0; j<i;j++) {
                    distance = getDistance(nodes[j].return_xaxis(), nodes[j].return_yaxis(), nodes[i].return_xaxis(), nodes[i].return_yaxis());
                    if ( distance > 100 && distance < 500 ) {   //if you want to cater for more nodes increase the bracket for distance
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

            for (int i = 0; i < nodes.length ; i++){    //printing nodes values
                g2d.draw(new Ellipse2D.Double(nodes[i].return_xaxis(), nodes[i].return_yaxis(), 20,20));
            }
        }

        private void edgeDrawing (Graphics g){
            Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(Color.RED);
            for(int v=0;v<nodes.length;v++){
                for(Neighbor nbr=nodes[v].adjList; nbr!=null;nbr=nbr.next){
                    g2d.drawLine(nodes[v].return_xaxis()+11,nodes[v].return_yaxis()+11,nodes[nbr.nodeNum].return_xaxis()+11,nodes[nbr.nodeNum].return_yaxis()+11);//half of width and height of ellipse will center the lines
                }
            }
        }

        private void Edgeremoving (Graphics g){
            Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(Color.red);
            for(int v=0;v<nodes.length;v++){
                for(Neighbor nbr=nodes[v].adjList; nbr!=null;nbr=nbr.next){
                    if ((getDistanceEdge(nodes[v].return_xaxis(),nodes[v].return_yaxis(),nodes[nbr.nodeNum].return_xaxis(),nodes[nbr.nodeNum].return_yaxis(),mX,mY)) < 10){
                        System.out.println("Edge selected");
                        break;
                    }
                    System.out.println("Edge not selected");
                    break;
                }
                break;
            }

        }

        private void selectDrawing(Graphics g){
            Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(Color.RED);
            for (int i=0;i<nodes.length;i++){
                if(nodes[i].selected){
                    g2d.fill(new Ellipse2D.Double(nodes[i].return_xaxis(), nodes[i].return_yaxis(), 22,22));    //code for the selected node goes here
                    g2d.drawString(nodes[i].label,nodes[i].return_xaxis()-15, nodes[i].return_yaxis()+35);
                    nodes[i].selected=false;    //to unselected the first node
                } else {
                    g2d.draw(new Ellipse2D.Double(nodes[i].return_xaxis(), nodes[i].return_yaxis(), 20,20));
                }
            }
        }

        @Override
        public void paintComponent (Graphics g){
            super.paintComponent(g);
            topDrawing(g);
            if(!selected)   //meaning only called once
                doDrawing(g);
            selectDrawing(g);
            edgeDrawing(g);
            Edgeremoving(g);
        }

        public void topDrawing(Graphics g){
            g.drawString("A", 25, 25);
            Rectangle label1 = new Rectangle(23,15,22,13);
            g.drawString("B",75,25);
            Rectangle label2 = new Rectangle(73,15,22,13);
            g.drawString("C",125,25);
            Rectangle label3 = new Rectangle(123,15,22,13);
            g.drawString("D",175,25);
            Rectangle label4 = new Rectangle(173,15,22,13);
            g.drawString("F_1",225,25);
            Rectangle label5 = new Rectangle(223,15,22,13);
            g.drawString("F_2",295,25);
            Rectangle label6 = new Rectangle(293,15,22,13);
            g.drawString("F_3",365,25);
            Rectangle label7 = new Rectangle(363,15,22,13);
            g.drawString("F_4",435,25);
            Rectangle label8 = new Rectangle(433,15,22,13);
        }

        public double getDistanceEdge(float x1,float y1,float x2,float y2,float x3,float y3)
        {
            float px=x2-x1;
            float py=y2-y1;
            float temp=(px*px)+(py*py);
            float u=((x3 - x1) * px + (y3 - y1) * py) / (temp);
            if(u>1){
                u=1;
            }
            else if(u<0){
                u=0;
            }
            float x = x1 + u * px;
            float y = y1 + u * py;

            float dx = x - x3;
            float dy = y - y3;
            double dist = Math.sqrt(dx*dx + dy*dy);
            return dist;

        }

        public int getDistance(int x1, int y1, int x2, int y2){ //eficient method for distance calculation
            int dx = x2-x1;
            int dy = y2-y1;
            return (int)Math.sqrt(dx*dx+dy*dy);
        }

        @Override
        public void mouseClicked(MouseEvent e) {    //for node selection
            mX = e.getX();
            mY = e.getY();
            int d2 = 0;
            for (int  i = 0; i < nodes.length ; i++ ){
                d2 = getDistance(nodes[i].return_xaxis(),nodes[i].return_yaxis(),mX,mY);
                if (d2<=20){                    //distance between the mouse clicked pointer and the center of the circle
                    nodes[i].selected = true;
                    selected = true;
                    break;
                } else
                    nodes[i].selected = false;
            }
            repaint();
        }
        @Override
        public void mousePressed(MouseEvent e) {
            int prex = e.getX();
            int prey = e.getY();


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

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }
    }

    class GraphLoader {

        private JFileChooser chooser = new JFileChooser();
        private File selected_file = null;
        private String label = null;

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
            num_nodes = s.nextInt();    //saving number of nodes on top of the text file
            num_edges = s.nextInt();

            s.nextLine();

            nodes = new Node[num_nodes];
            edges = new Edge[num_edges];

            //read labels
            for (int i = 0; i <nodes.length; i++) {    //adding labels to graph
                nodes[i] = new Node(s.next(),null);
                nodes[i].numNodes(num_nodes);
            }

            //read edges
            int i=0;
            while(s.hasNext()) {
                try {
                    int n1 = nameIndex(s.next()); // start node
                    //edges[i].n1 = nodes[n1];
                    int n2 = nameIndex(s.next()); // end node
                    //edges[i].n2 = nodes[n2];
                    nodes[n1].adjList = new Neighbor(n2, nodes[n1].adjList);
                    nodes[n2].adjList = new Neighbor(n1, nodes[n2].adjList);
                    i++;
                } catch (Exception e){          //in case of number of nodes are not equal to number of elements stated in the text file
                    System.out.println("exception occurred");}
            }
            System.out.println("You have selected => "+selected_file.getAbsolutePath());
        }

        int nameIndex(String name) {
            for (int n=0; n < nodes.length; n++) {
                if (nodes[n].label.equals(name)) {
                    return n;
                }
            }
            return -1;
        }

    }

    class Edge{
        private Node n1;
        private Node n2;
        int sxaxis;
        int syaxis;
        int dxaxis;
        int dyaxis;

        Edge(int sx, int sy,int dx,int dy){
            this.sxaxis = sx;
            this.syaxis = sy;
            this.dxaxis = dx;
            this.dyaxis = dy;
        }

        Edge()
        {
            this.sxaxis = 0;
            this.syaxis = 0;
            this.dxaxis = 0;
            this.dyaxis = 0;
            this.n1 = null;
            this.n2 = null;
        }

    }

    class topLabel{

    }

}
