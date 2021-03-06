
import com.sun.javafx.geom.Edge;
import javafx.stage.Stage;

import javax.sound.sampled.Line;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

import static java.awt.Color.BLACK;
import static java.awt.Color.GREEN;
import static java.awt.SystemColor.window;

public class Graph extends JFrame {
    private int num_nodes = 0;
    Node[] nodes;
    private int num_edges = 0;

    ArrayList<Edge> edges=new ArrayList<Edge>();
    ArrayList<Line2D> edges_line = new ArrayList<>();
    private int mX = 0;
    private int mY = 0;


    private void initUI() {                  //initializing the User Interface
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
        for (int v = 0; v < nodes.length; v++) {
            System.out.print(nodes[v].label);
            for (Neighbor nbr = nodes[v].adjList; nbr != null; nbr = nbr.next) {
                System.out.print(" --> " + nodes[nbr.nodeNum].label);
            }
            System.out.println("\n");
        }
        System.out.println(num_edges);
    }

    private Graph() {
        GraphLoader graphLoader = new GraphLoader();
        try {
            graphLoader.read_label();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        initUI();
    }

    class Node {
        boolean labelselected = false;
        String label;
        int nxaxis;
        int nyaxis;
        int numNodes;
        Neighbor adjList;
        boolean selected;
        public Shape shape = new Ellipse2D.Double(nxaxis,nyaxis,20,20);
        boolean a_45,a_135,a_225,a_315;

        Node() {
            this.nyaxis = 0;
            this.nxaxis = 0;
            this.label = "NULL";
            this.adjList = null;
            selected = false;
        }

        Node(String label, Neighbor neighbors) {
            this.nxaxis = 0;
            this.nyaxis = 0;
            this.label = label;
            this.adjList = neighbors;
            selected = false;
        }

        public void getaxis(int xaxis, int yaxis) {
            nxaxis = xaxis;
            nyaxis = yaxis;
        }

        public void add_label(String s) {
            label = s;
        }

        public void get_labels() {
            System.out.println("Label Stored " + label);
            System.out.println("Neighbor Stored : " + adjList);
        }

        public int return_xaxis() {
            return nxaxis;
        }

        public int return_yaxis() {
            return nyaxis;
        }

        public void numNodes(int number) {
            this.numNodes = number;
        }
    }

    class Neighbor {
        public int nodeNum;
        public Neighbor next;

        public Neighbor(int nnum, Neighbor nbr) {
            this.nodeNum = nnum;
            next = nbr;
        }
    }

    class surface extends JPanel implements MouseListener, MouseMotionListener {    //most of UI implemented in surface class
        private int mStartX, mStartY;   //for last mouse position
        private boolean selected = false;
        boolean dragging = false;
        double offsetX, offsetY;
        double x3 =23,y3 =15;
        boolean ellipse = true;
        boolean selected1 = false;
        boolean line = false;
        Shape myRect1, myRect2, myRect3, myRect4, myRect5, myRect6, myRect7, myRect8;//rectangle for the first label A
        //label myRect2;
        int endX=0,endY=0;
        BasicStroke s5;
        private final int ARR_SIZE = 5;
        private static final int BOX_SIZE = 4;
        ArrayList<Line2D> arrowLines = new ArrayList<>();
        ArrayList<label> labels = new ArrayList<>();
        boolean mousedragged = false;
        boolean mousereleased = false;
        boolean intersects = false;
        boolean drawstring = false;


        public surface() {
            addMouseListener(this);
            addMouseMotionListener(this);   //never forget
            mStartX = 0;
            mStartY = 0;
        }

        private void nodeFilling() { //for equal distances x y axis
            int distance = 0;
            int w = getWidth();
            int h = getHeight();
            Random r = new Random();
            nodes[0].getaxis(Math.abs(r.nextInt() % w), Math.abs(r.nextInt() % h));
            //node filling algorithm
            for (int i = 1; i < nodes.length; i++) {
                nodes[i].getaxis(Math.abs(r.nextInt() % w), Math.abs(r.nextInt() % h)); //second node stored in a first node array
                for (int j = 0; j < i; j++) {
                    distance = getDistance(nodes[j].return_xaxis(), nodes[j].return_yaxis(), nodes[i].return_xaxis(), nodes[i].return_yaxis());
                    if ( distance > 100 && distance < 500 ) {   //if you want to cater for more nodes increase the bracket for distance
                        // means node will be added to the list and wont go to the else part of the loop

                    } else {
                        //means distance condition is false and therefore the node is overwritten.
                        i--;
                        break;
                    }
                }
            }
        }

        private void edgeFilling(){
            for (int v = 0; v < nodes.length; v++) {
                for (Neighbor nbr = nodes[v].adjList; nbr != null; nbr = nbr.next) {
                    edges_line.add(new Line2D.Double(nodes[v].return_xaxis() + 11, nodes[v].return_yaxis() + 11, nodes[nbr.nodeNum].return_xaxis() + 11, nodes[nbr.nodeNum].return_yaxis() + 11));
                    edges.add(new Edge(nodes[v].return_xaxis() + 11, nodes[v].return_yaxis() + 11, nodes[nbr.nodeNum].return_xaxis() + 11, nodes[nbr.nodeNum].return_yaxis() + 11));
                }
            }

        }

        private void doDrawing(Graphics g) {        //Separate method for drawing graphics object override
            Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(Color.RED);
            nodeFilling();  //separate method for checking equal distribution of nodes on the canvas
            edgeFilling();
            for (int i = 0; i < nodes.length; i++) {    //printing nodes values
                g2d.draw(new Ellipse2D.Double(nodes[i].return_xaxis(), nodes[i].return_yaxis(), 20, 20));
            }
        }

        private void edgeDrawing(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(BLACK);
            for(Line2D line:edges_line){
                g2d.draw(line);
            }
            //edgeFilling();
            /*for(Edge obj:edges){
                g2d.draw(obj.edge);
            }
            /*Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(Color.BLUE);
            for (int v = 0; v < nodes.length; v++) {
                for (Neighbor nbr = nodes[v].adjList; nbr != null; nbr = nbr.next) {
                    edges_line.add(new Line2D.Double(nodes[v].return_xaxis() + 11, nodes[v].return_yaxis() + 11, nodes[nbr.nodeNum].return_xaxis() + 11, nodes[nbr.nodeNum].return_yaxis() + 11));
                    g2d.drawLine(nodes[v].return_xaxis() + 11, nodes[v].return_yaxis() + 11, nodes[nbr.nodeNum].return_xaxis() + 11, nodes[nbr.nodeNum].return_yaxis() + 11);//half of width and height of ellipse will center the lines
                }
            }*/
        }

        private void Edgeremoving(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(Color.red);
            for (int v = 0; v < nodes.length; v++) {
                for (Neighbor nbr = nodes[v].adjList; nbr != null; nbr = nbr.next) {
                    if ( (getDistanceEdge(nodes[v].return_xaxis(), nodes[v].return_yaxis(), nodes[nbr.nodeNum].return_xaxis(), nodes[nbr.nodeNum].return_yaxis(), mX, mY)) < 10 ) {
                        System.out.println("Edge selected");
                        break;
                    }
                    //System.out.println("Edge not selected");
                    break;
                }
                break;
            }

        }

        private void selectDrawing(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(Color.RED);
            for (int i = 0; i < nodes.length; i++) {
                if ( nodes[i].selected ) {
                    g2d.fill(new Ellipse2D.Double(nodes[i].return_xaxis(), nodes[i].return_yaxis(), 22, 22));    //code for the selected node goes here
                    g2d.drawString(nodes[i].label, nodes[i].return_xaxis() - 15, nodes[i].return_yaxis() + 35);
                    nodes[i].selected = false;    //to unselected the first node
                } else {
                    g2d.draw(new Ellipse2D.Double(nodes[i].return_xaxis(), nodes[i].return_yaxis(), 20, 20));
                }
            }
            selected = true;
        }

        public void topDrawing(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawString("A", 25, 25);
            if(!dragging && ellipse){
                myRect1 = new Rectangle2D.Double(x3, y3, 22, 13);
                g2d.draw(myRect1);
            } else if(!selected1 && ellipse){
                myRect1 = new Rectangle2D.Double(23, 15, 22, 13);
                g2d.draw(myRect1);
            }
            /*g.drawString("B", 75, 25);
            myRect2 = new Rectangle(73, 15, 22, 13);
            g2d.draw(myRect1);
            g.drawString("C", 125, 25);
            g2d.draw(myRect1);
            myRect3 = new Rectangle(123, 15, 22, 13);
            g2d.draw(myRect1);
            g.drawString("D", 175, 25);
            g2d.draw(myRect1);
            myRect4 = new Rectangle(173, 15, 22, 13);
            g2d.draw(myRect1);
            g.drawString("F_1", 225, 25);
            g2d.draw(myRect1);
            myRect5 = new Rectangle(223, 15, 22, 13);
            g2d.draw(myRect1);
            g.drawString("F_2", 295, 25);
            g2d.draw(myRect1);
            myRect6 = new Rectangle(293, 15, 22, 13);
            g2d.draw(myRect1);
            g.drawString("F_3", 365, 25);
            g2d.draw(myRect1);
            myRect7 = new Rectangle(363, 15, 22, 13);
            g.drawString("F_4", 435, 25);
            myRect8 = new Rectangle(433, 15, 22, 13);*/
        }

        public void lineDrawing(Graphics g){
            Graphics2D g2d = (Graphics2D) g;
            for(int i=0;i<nodes.length;i++){
                if(nodes[i].labelselected){
                    if(nodes[i].a_45) {
                        getAngle(nodes[i].nxaxis + 10, nodes[i].nyaxis + 10, 45 + 270, 90);
                        drawArrow(g, nodes[i].nxaxis + 10, nodes[i].nyaxis + 10, endX, endY);
                        arrowLines.add(new Line2D.Double(nodes[i].nxaxis + 10, nodes[i].nyaxis + 10, endX, endY));
                    }
                    if(nodes[i].a_135) {
                        getAngle(nodes[i].nxaxis + 10, nodes[i].nyaxis + 10, 135+270, 90);
                        drawArrow(g, nodes[i].nxaxis + 10, nodes[i].nyaxis + 10, endX, endY);
                        arrowLines.add(new Line2D.Double(nodes[i].nxaxis + 10, nodes[i].nyaxis + 10, endX, endY));
                    }
                    if(nodes[i].a_225){
                        getAngle(nodes[i].nxaxis+10,nodes[i].nyaxis+10,225+270,90);
                        drawArrow(g,nodes[i].nxaxis+10,nodes[i].nyaxis+10,endX,endY);
                        arrowLines.add(new Line2D.Double(nodes[i].nxaxis + 10, nodes[i].nyaxis + 10, endX, endY));
                    }
                    if(nodes[i].a_315){
                        getAngle(nodes[i].nxaxis+10,nodes[i].nyaxis+10,315+270,90);
                        drawArrow(g,nodes[i].nxaxis+10,nodes[i].nyaxis+10,endX,endY);
                        arrowLines.add(new Line2D.Double(nodes[i].nxaxis + 10, nodes[i].nyaxis + 10, endX, endY));
                    }
                    //g2d.drawString("A", nodes[i].return_xaxis() - 3, nodes[i].return_yaxis() + 35);
                    g2d.fill(new Ellipse2D.Double(nodes[i].return_xaxis(), nodes[i].return_yaxis(), 22, 22));    //code for the selected node goes here
                    //nodes[i].labelselected = false;
                }
            }
        }

        public void drawLabel(Graphics g){
            Graphics2D g2d = (Graphics2D) g;
            for (label obj:labels)
                g.drawString(obj.name,obj.x,obj.y);
            drawstring = false;
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if(!selected)//drawing graph only once
                doDrawing(g);
            selectDrawing(g);
            edgeDrawing(g);
            topDrawing(g);
            lineDrawing(g);
            drawLabel(g);
        }

        @Override
        public void mouseClicked(MouseEvent e) {    //for node selection
            mX = e.getX();
            mY = e.getY();
            int d2;
            for (int i = 0; i < nodes.length; i++) {
                d2 = getDistance(nodes[i].return_xaxis(), nodes[i].return_yaxis(), mX, mY);
                if ( d2 <= 20 ) {                    //distance between the mouse clicked pointer and the center of the circle
                    nodes[i].selected = true;
                    selected = true;
                    nodes[i].labelselected = true;
                    alertBox(i);
                    repaint();
                    break;
                } else
                   nodes[i].selected = false;
            }
            getSelectedLine(e.getX(),e.getY());
            //repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            x3 = 23;
            y3 = 15;
            mousereleased = false;
            if ( myRect1.contains(e.getX(), e.getY())){ //&& myRect2.contains(prex, prey) && myRect3.contains(prex, prey) && myRect4.contains(prex, prey) && myRect5.contains(prex, prey)
                    //&& myRect6.contains(prex, prey) && myRect7.contains(prex, prey) && myRect8.contains(prex, prey)   too many internal calls error !!
                offsetX = e.getX() - x3;
                offsetY = e.getY() - y3;
                dragging = false;
                System.out.println("Mouse pressed inside 1");
                repaint();
            } else if ( myRect2.contains(e.getX(),e.getY())){
                offsetX = e.getX() - x3;
                offsetY = e.getY() - y3;
                dragging = false;
                System.out.println("Mouse pressed inside 1");
                repaint();
            } else if ( myRect3.contains(e.getX(),e.getY())){
                offsetX = e.getX() - x3;
                offsetY = e.getY() - y3;
                dragging = false;
                System.out.println("Mouse pressed inside 1");
                repaint();
            } else if ( myRect4.contains(e.getX(),e.getY())){
                offsetX = e.getX() - x3;
                offsetY = e.getY() - y3;
                dragging = false;
                System.out.println("Mouse pressed inside 1");
                repaint();
            } else if ( myRect5.contains(e.getX(),e.getY())){
                offsetX = e.getX() - x3;
                offsetY = e.getY() - y3;
                dragging = false;
                System.out.println("Mouse pressed inside 1");
                repaint();
            } else if ( myRect6.contains(e.getX(),e.getY())){
                offsetX = e.getX() - x3;
                offsetY = e.getY() - y3;
                dragging = false;
                System.out.println("Mouse pressed inside 1");
                repaint();
            } else if ( myRect7.contains(e.getX(),e.getY())){
                offsetX = e.getX() - x3;
                offsetY = e.getY() - y3;
                dragging = false;
                System.out.println("Mouse pressed inside 1");
                repaint();
            } else if ( myRect8.contains(e.getX(),e.getY())){
                offsetX = e.getX() - x3;
                offsetY = e.getY() - y3;
                dragging = false;
                System.out.println("Mouse pressed inside 1");
                repaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            dragging = true;
            line = false;
            mousereleased = true;
            drawstring = false;
            for(Line2D obj:arrowLines){
                if(obj.intersects((Rectangle2D)(myRect1))){
                    System.out.println("Line Intersected with rectangle");
                    System.out.println("X1 = "+obj.getX1());
                    System.out.println("Y1 = "+obj.getY1());
                    int x1 =(int) obj.getX2();
                    int y1 =(int) obj.getY2();
                    drawstring = true;
                    //g2d.drawString("A",x1+20,y1-20);
                    x3 = 23; y3 = 15;
                    intersects = true;
                    labels.add(new label("A",x1+10,y1));
                    repaint();

                } else{
                    System.out.println("Line not Intersected with rectangle");
                    intersects = false;
                    repaint();
                }
            }
            repaint();
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        @Override
        public void mouseDragged(MouseEvent e) {
            mousedragged = true;
            x3 = e.getX() - offsetX;
            y3 = e.getY() - offsetY;
            repaint();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }

        void drawArrow(Graphics g1, int x1, int y1, int x2, int y2) {
            Graphics2D g = (Graphics2D) g1.create();


            double dx = x2 - x1, dy = y2 - y1;
            double angle = Math.atan2(dy, dx);
            int len = (int) Math.sqrt(dx*dx + dy*dy);
            AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
            at.concatenate(AffineTransform.getRotateInstance(angle));
            g.transform(at);

            // Draw horizontal arrow starting in (0, 0)
            s5 = new BasicStroke(2.f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
            g.setStroke(s5);
            g.drawLine(0, 0, len, 0);
            g.fillPolygon(new int[] {len, len-ARR_SIZE, len-ARR_SIZE, len}, new int[] {0, -ARR_SIZE, ARR_SIZE, 0}, 4);
        }


        public void getAngle(int x, int y, int a, int len){
            endX = x + (int)(Math.cos(Math.toRadians(a)) * len);
            endY = y + (int)(Math.sin(Math.toRadians(a)) * len);
        }

        public double getDistanceEdge(float x1, float y1, float x2, float y2, float x3, float y3)        {
            float px = x2 - x1;
            float py = y2 - y1;
            float temp = (px * px) + (py * py);
            float u = ((x3 - x1) * px + (y3 - y1) * py) / (temp);
            if ( u > 1 ) {
                u = 1;
            } else if ( u < 0 ) {
                u = 0;
            }
            float x = x1 + u * px;
            float y = y1 + u * py;

            float dx = x - x3;
            float dy = y - y3;
            double dist = Math.sqrt(dx * dx + dy * dy);
            return dist;

        }

        public int getDistance(int x1, int y1, int x2, int y2) { //eficient method for distance calculation
            int dx = x2 - x1;
            int dy = y2 - y1;
            return (int) Math.sqrt(dx * dx + dy * dy);
        }

        private Shape getSelectedLine(int x,int y){
            int boxX = x - BOX_SIZE / 2;
            int boxY = y - BOX_SIZE / 2;

            int w = BOX_SIZE;
            int h = BOX_SIZE;

            for (Line2D selectedLine:edges_line){
                if(selectedLine.intersects(boxX,boxY,w,h)){
                    System.out.println("line selected / intersects");
                    removeEdge(selectedLine);
                    return selectedLine;
                }

            }
            return null;
        }

        private void removeEdge(Line2D line){
            Iterator<Line2D> it = edges_line.iterator();
            while(it.hasNext()){
                Line2D selectedLine = it.next();
                if(selectedLine.equals(line)){
                    it.remove();
                    repaint();
                }
            }

        }

        public void alertBox(int index){
            Object[] possibilities = {"45", "135", "225","315"};
            String s = (String)JOptionPane.showInputDialog(
                    null,
                    "Choose an Angle",
                    "Angle",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    possibilities,
                    "45");
            if ((s != null) && s == "45") {
                nodes[index].a_45 = true;
            }else  if ((s != null) && s == "135") {
                nodes[index].a_135 = true;
            } else  if ((s != null) && s == "225") {
                nodes[index].a_225 = true;
            } else  if ((s != null) && s == "315") {
                nodes[index].a_315 = true;
            }
        }
    }


    class GraphLoader {

        private JFileChooser chooser = new JFileChooser();
        private File selected_file = null;
        private String label = null;

        public GraphLoader() {
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "Text File", "txt");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showOpenDialog(null);
            if ( returnVal == JFileChooser.APPROVE_OPTION ) {
                System.out.println("You chose to open this file: " +
                        chooser.getSelectedFile().getAbsolutePath());
            }
            selected_file = chooser.getSelectedFile();
        }

        public File return_selectedfile() {
            return selected_file;
        }

        public void read_label() throws FileNotFoundException, ArrayIndexOutOfBoundsException {

            Scanner s = new Scanner(return_selectedfile());
            num_nodes = s.nextInt();    //saving number of nodes on top of the text file
            num_edges = s.nextInt();

            s.nextLine();

            nodes = new Node[num_nodes];
            //edges = new Edge[num_edges];

            //read labels
            for (int i = 0; i < nodes.length; i++) {    //adding labels to graph
                nodes[i] = new Node(s.next(), null);
                nodes[i].numNodes(num_nodes);
            }

            //read edges
            int i = 0;
            while (s.hasNext()) {
                try {
                    int n1 = nameIndex(s.next()); // start node
                    //edges.get(i).n1=nodes[n1];
                    //edges[i].n1 = nodes[n1];
                    int n2 = nameIndex(s.next()); // end node
                    //edges.get(i).n2=nodes[n2];
                    //edges[i].n2 = nodes[n2];
                    nodes[n1].adjList = new Neighbor(n2, nodes[n1].adjList);
                    nodes[n2].adjList = new Neighbor(n1, nodes[n2].adjList);
                    i++;
                } catch (Exception e) {          //in case of number of nodes are not equal to number of elements stated in the text file
                    System.out.println("exception occurred");
                }
            }
            System.out.println("You have selected => " + selected_file.getAbsolutePath());
        }

        int nameIndex(String name) {
            for (int n = 0; n < nodes.length; n++) {
                if ( nodes[n].label.equals(name) ) {
                    return n;
                }
            }
            return -1;
        }

    }

    class Edge {
        private Node n1;
        private Node n2;
        Line2D edge;
        int sxaxis;
        int syaxis;
        int dxaxis;
        int dyaxis;

        Edge(int sx, int sy, int dx, int dy) {
            this.sxaxis = sx;
            this.syaxis = sy;
            this.dxaxis = dx;
            this.dyaxis = dy;
            edge = new Line2D.Double();
            edge.setLine(sxaxis,syaxis,dxaxis,dyaxis);
        }

        Edge() {
            this.sxaxis = 0;
            this.syaxis = 0;
            this.dxaxis = 0;
            this.dyaxis = 0;
            this.n1 = null;
            this.n2 = null;
            edge = new Line2D.Double();
            edge.setLine(sxaxis,syaxis,dxaxis,dyaxis);
        }

        void createEdge(int sX, int sY, int dX, int dY){
            edge.setLine(sX,sY,dX,dY);
        }

    }

    class label{
        String name;
        int x;
        int y;

        label(){
            this.name = null;
            this.x  = 0;
            this.y = 0;
        }

        label(String s, int x1, int y1){
            this.name = s;
            this.x  = x1;
            this.y = y1;
        }

    }
}

