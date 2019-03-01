import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;

public class ControlLogic {
    private ViewPanel LP, RP;
    private int controlSize;       //axis size of control points
    private int controlTotal;       //total number of control points 10x10 =100
    private Point LEFTcontrolPointArray[];  //hold point data for left render panel
    private Point RIGHTcontrolPointArray[]; //hold point data for right render panel
    private BufferedImage image1,image2;
    private File leftFile, rightFile;
    private int dotSize= 9;  //present for easier tweaking of control point dot (representing where user can click to count as interaction with a point)
    private int centerOffset = 2; //easier tweaking of adjusting center of interaction with a point
    private int xDimL=550; //default xDimension for left panel
    private int yDimL=550;//default yDimension for left panel
    private int xDimR=550;//default xDimension for right panel
    private int yDimR=550;//default yDimension for right panel
    private double xScaleL,yScaleL,xScaleR,yScaleR;  //decimal size spacing of control point (while be converted to int later when exact pixel center needed)
    private int[] selectionList;  //list of cps selected by bulk selection function
    private JMorph parentClass;
    private previewWindow testWarpWindow;
    private RenderWindow renderWindow;
    private boolean openPreview = false;  //whether preview window is currently open
    private boolean openRender = false;  //whether render window is currently open
    RescaleOp rescale;
    float lBright =0f;
    float rBright = 0f;



    public ControlLogic(int points, JMorph parent){
        parentClass = parent;
        controlSize = points + 2;  //2 extra points added to make rendering border (non-interactable control points easier)
        xScaleL = (double)xDimL/(points+1);  //spacing between points aka number of line segments 1 less than number of points,
        // for whatever reason I decided to base on passed points so points + 2 -1 = points +1
        yScaleL = (double)yDimL/(points+1);
        xScaleR = (double)xDimR/(points+1);
        yScaleR = (double)yDimR/(points+1);
        controlTotal = controlSize*controlSize;  //precalculate since it will be used more than once
        selectionList = new int[controlTotal];
        LEFTcontrolPointArray = new Point[controlTotal];  //define the blank array of points based on number of points needed
        RIGHTcontrolPointArray = new Point[controlTotal];
        LP = new ViewPanel(controlSize);
        RP = new ViewPanel(controlSize);
        initCPArray(2);  //fill base coord information select 0 does left array, select 1 does right, anything else does both, so 2 is just shorthand for both
        // I used alot of paired functions and I likely should have done this style with more of my functions
        repaint();
    }

    //variant only for left panel
    public void LEFTreInitialize(int points, int newX, int newY){//contains all of the major defintions from constructor, allowing easy redimensioning of structures
        controlSize = points + 2;
        xDimL = newX;
        yDimL = newY;
        LP.reInitialize(controlSize,xDimL,yDimL);
        xScaleL = (double)xDimL/(points+1);
        yScaleL = (double)yDimL/(points+1);
        controlTotal = controlSize*controlSize;
        selectionList = new int[controlTotal];
        LEFTcontrolPointArray = new Point[controlTotal];
        initCPArray(0);
        repaint();
    }

    //variant for both panels at the same time
    public  void reInitalizePoints(int points){//contains all of the major defintions from constructor, allowing easy redimensioning of structures
        controlSize = points + 2;
        LP.reInitialize(controlSize,xDimL,yDimL);
        RP.reInitialize(controlSize,xDimR,yDimR);
        xScaleL = (double)xDimL/(points+1);
        yScaleL = (double)yDimL/(points+1);
        xScaleR = (double)xDimR/(points+1);
        yScaleR = (double)yDimR/(points+1);
        controlTotal = controlSize*controlSize;
        selectionList = new int[controlTotal];
        LEFTcontrolPointArray = new Point[controlTotal];
        RIGHTcontrolPointArray = new Point[controlTotal];
        initCPArray(2);
        repaint();
    }

    //variant for right panel only
    public void RIGHTreInitialize(int points, int newX, int newY){//contains all of the major defintions from constructor, allowing easy redimensioning of structures
        controlSize = points + 2;
        xDimR = newX;
        yDimR = newY;
        RP.reInitialize(controlSize,xDimL,yDimL);
        xScaleR = (double)xDimR/(points+1);
        yScaleR = (double)yDimR/(points+1);
        controlTotal = controlSize*controlSize;
        selectionList = new int[controlTotal];
        RIGHTcontrolPointArray = new Point[controlTotal];
        initCPArray(1);
        repaint();
    }

    public ViewPanel retLPanel(){return LP.retPanel();}  //middle man for passing viewpanel back to jmorph

    public ViewPanel retRPanel(){return RP.retPanel();}

    public void openPreview(){  //if preview button from main view is clicked
        if(!openPreview) { //as long as preview window is not currently open, open a new one
            //pass both CPAs, number of CPAs, and largest x and y dimension, this max is likely unneeded in the greater context but for now it solves an edge case
            testWarpWindow = new previewWindow(LEFTcontrolPointArray,RIGHTcontrolPointArray,controlSize,max(xDimL,xDimR),max(yDimL,yDimR));
            openPreview = true;
        }
        testWarpWindow.addWindowListener(
                new WindowAdapter(){
                    public void windowClosing(WindowEvent e) {
                        openPreview = false;}});//when window closes, update bool tracking if window is open
    }

    public void openRender(){  //if preview button from main view is clicked
        if(!openRender) { //as long as preview window is not currently open, open a new one
            //pass both CPAs, number of CPAs, and largest x and y dimension, this max is likely unneeded in the greater context but for now it solves an edge case
            if(leftFile == null || rightFile == null){
                JOptionPane.showMessageDialog(null, "File selection needed for both images", "Error: Missing Image", JOptionPane.INFORMATION_MESSAGE);
            }
            else if (yDimL != yDimR || xDimL != xDimR){
                JOptionPane.showMessageDialog(null, "Images need to be the same size", "Error: Image Size", JOptionPane.INFORMATION_MESSAGE);
            }
            else {
                renderWindow = new RenderWindow(LEFTcontrolPointArray, RIGHTcontrolPointArray, controlSize, max(xDimL, xDimR), max(yDimL, yDimR), image1, image2);
                openRender = true;
                renderWindow.addWindowListener(
                        new WindowAdapter(){
                            public void windowClosing(WindowEvent e) {
                                openRender = false;}});//when window closes, update bool tracking if window is open
            }
        }
    }

    public int max(int val1, int val2){ //pick larger value
        if(val1 > val2)
            return val1;
        else
            return val2;
    }

    public void areaSelect(Point start, Point end, int select){  //send off click location and current dragging position to appropriate panel for rendering
        if(select == 0 )
            LP.setSelection(start, end);
        else
            RP.setSelection(start, end);
    }

    public void endSelection(Point start, Point end, int select){  //when mouse is released, define which CPs were captured in region
        LP.setSelection(new Point(), new Point()); //disable the highlighting animation (red rectangular box showing selection area)
        RP.setSelection(new Point(), new Point());
        int x=Math.min(start.x, end.x);  //determine up left corner x coord
        int y=Math.min(start.y, end.y); //determine up left corern y coord
        int width=Math.abs(start.x - end.x);  //determine width of rectangle
        int height=Math.abs(start.y - end.y);  //determine height of rectangle
        Rectangle border = new Rectangle (x,y,width,height);  //define rectangle with this information
        selectionList = new int[controlTotal];  //throw away old selection list and make new
        for (int i =1; i<controlSize-1; i++) {
            for (int j = 1; j < controlSize - 1; j++) {
                int k = i*controlSize + j;  //special index to only check interior control point (ignore fake border control point)
                if(select == 0) {  //select choices whether to look at LEFT cpa or right
                    if (border.contains(LEFTcontrolPointArray[k])) {  //if that CP is inside of the rectangle, then add it to the list, (spatially encoded by index)
                        selectionList[k] = 1;
                    }
                }
                else{
                    if (border.contains(RIGHTcontrolPointArray[k])) {
                        selectionList[k] = 1;
                    }
                }
            }
        }
        LP.setGlow(selectionList);
        RP.setGlow(selectionList);
    }

    private void initCPArray(int select){  //generate base equispaced coords
        for(int i =0; i < controlSize; i++){
            for(int j=0; j < controlSize; j++){
                if(select!=1) //shortcut for running left init when select is 0 or 2 (though realistically, anything not 0 or 1 will run both)
                    LEFTcontrolPointArray[i * controlSize + j] = new Point((int)((j) * xScaleL), (int)((i) * yScaleL));
                if(select!=0)//shortcut for running right init when select is 1 or 2 (though realistically, anything not 0 or 1 will run both)
                    RIGHTcontrolPointArray[i * controlSize + j] = new Point((int)((j) * xScaleR), (int)((i) * yScaleR));
            }
        }
    }

    public void setBright(float value,int select){  //takes brightness value and selector, either left or right image (0 or 1)
        if(select == 0){
            lBright = value;
            if(leftFile != null) {  //if no file has been opened yet it does nothing
                try {
                    image1 = ImageIO.read(leftFile);  //reopen the file
                } catch (IOException e1) {
                };
                rescale = new RescaleOp(1.0f, lBright, null);  //apply brightness transform
                rescale.filter(image1, image1);  //apply to image
                LP.setImage(image1);  //bim in panel is now this one.
            }
        }
        else{  //same thing but for right image
            rBright = value;
            if(rightFile != null) {
                try {
                    image2 = ImageIO.read(rightFile);
                } catch (IOException e1) {
                };
                rescale = new RescaleOp(1.0f, rBright, null);
                rescale.filter(image2, image2);
                RP.setImage(image2);
            }
        }

    }

    public void LEFTopenImage (File imageFile){ //file from main view is opened and size of control point frame is redimensioned to fit image size
        leftFile = imageFile;
        try {
            image1 = ImageIO.read(imageFile);
        } catch (IOException e1){};
        rescale = new RescaleOp(1.0f, lBright, null);
        rescale.filter(image1, image1);
        LP.setImage(image1);
        LEFTreInitialize(controlSize-2,image1.getWidth(),image1.getHeight());
        parentClass.pack();
    }

    public void RIGHTopenImage (File imageFile){//file from main view is opened and size of control point frame is redimensioned to fit image size
        rightFile = imageFile;
        try {
            image2 = ImageIO.read(imageFile);
        } catch (IOException e1){};
        rescale = new RescaleOp(1.0f, rBright, null);
        rescale.filter(image2, image2);
        RP.setImage(image2);
        RIGHTreInitialize(controlSize-2,image2.getWidth(),image2.getHeight());
        parentClass.pack();
    }

    public void Glow(int index){  //passed index results in both render panels having that element highlighted
        if(selectionList[index] == 0) {  //to allow mass glowing, structure changed to be an array of elements
            selectionList = new int[controlTotal];
            int[] glowArray = new int[controlTotal];
            for (int i = 0; i < controlTotal; i++) {  //i was concerned about values so i manually ensured index was 1 and the rest were 0
                if (i == index)
                    glowArray[i] = 1;
                else
                    glowArray[i] = 0;
            }
            LP.setGlow(glowArray);
            RP.setGlow(glowArray);
            repaint();
        }
    }
    
    public boolean testBounds (Point cpArray[], int index){
        Line2D SLines[] = new Line2D.Double[6];  //create an array of lines to hold lines emitted from CP being moved
        Line2D BLines[] = new Line2D.Double[6];
        //The 6 lines from CP being moved to the neighbor CPs
        SLines[0] = new Line2D.Double(cpArray[index],cpArray[index-controlSize-1]);
        SLines[1] = new Line2D.Double(cpArray[index],cpArray[index-controlSize]);
        SLines[2] = new Line2D.Double(cpArray[index],cpArray[index+1]);
        SLines[3] = new Line2D.Double(cpArray[index],cpArray[index+controlSize+1]);
        SLines[4] = new Line2D.Double(cpArray[index],cpArray[index+controlSize]);
        SLines[5] = new Line2D.Double(cpArray[index],cpArray[index-1]);

        //The 6 lines from the 6 neighbors around the CP being moved that act as boundaries, manually defining 6 sided polygon surrounding moving cp 1 line at a time
        BLines[0] = new Line2D.Double(cpArray[index-1],cpArray[index-controlSize-1]);
        BLines[1] = new Line2D.Double(cpArray[index-controlSize-1],cpArray[index-controlSize]);
        BLines[2] = new Line2D.Double(cpArray[index-controlSize],cpArray[index+1]);
        BLines[3] = new Line2D.Double(cpArray[index+1],cpArray[index+controlSize+1]);
        BLines[4] = new Line2D.Double(cpArray[index+controlSize+1],cpArray[index+controlSize]);
        BLines[5] = new Line2D.Double(cpArray[index+controlSize],cpArray[index-1]);
        boolean test = true;  //test is set default to successful or true
        for (int i =0; i < 6; i++){ //for each line emitted from moving CP
            for (int j =i+2; j < i+6; j++){//check all encircling lines for intersection other than lines that share a vertex
                if(SLines[i].intersectsLine(BLines[j%6])) // if i=0 will check 2,3,4,5,     if i=3 will check 4,5,0,1
                    test=false;  //if they intersect test fails
            }
        }
        return test;
    }

    public void copyCPA(Point[] source, Point[] dest){  //ensuring copy does what I want
        for(int i = 0; i < controlTotal; i++){
            dest[i] = source[i];
        }
    }

    public void LEFTsetCPElement(int index, Point newVal){  //Point[index] is assigned new coords, with bounding restrictions)
        int x = newVal.x-centerOffset;  //adjust dragging coords to make render center with mouse tip
        int y = newVal.y-centerOffset;
        int xoffset = x - LEFTcontrolPointArray[index].x;  //when applying movement of selection cp to a group of cps, the pixel offset is needed to apply translation to other points
        int yoffset = y - LEFTcontrolPointArray[index].y;
        Point[] tempCPA = new Point[controlTotal];  //allocating a temp CP array
        copyCPA(LEFTcontrolPointArray,tempCPA);  //making a copy of LEFT so that only when all moves in a drag are valid will LEFT be overwritten and repainted
        boolean success = true;
        if(selectionList[index] == 1){  //if clicked cp is in the selection
            for (int i=0; i < controlTotal; i++){  //look at all elements
                if(selectionList[i] == 1){  //if current inspected element is in the selection
                    index = i;  //variable copy to allow me to reuse already written code
                    x = LEFTcontrolPointArray[index].x + xoffset;  //location of that point + offset
                    y = LEFTcontrolPointArray[index].y + yoffset;
                    tempCPA[index] = new Point(x, y);
                    if (testBounds(tempCPA, index))  //if bounds of this set are legal
                        tempCPA[index] = new Point(x, y);  //set point, redundant!
                    else  //if not legal
                        success = false;  // this move is not a success
                }
                if(!success)  //if move is not a success
                    break;  //dont check the rest
            }
            if(success) //if loop completed successfully
                copyCPA(tempCPA,LEFTcontrolPointArray);  //copy temp into the LEFT cpa
            repaint();
        }
        else {
            Point old = LEFTcontrolPointArray[index];
            LEFTcontrolPointArray[index] = new Point(x, y);
            if (testBounds(LEFTcontrolPointArray, index)) {
                LEFTcontrolPointArray[index] = new Point(x, y);
                repaint();
            } else {
                LEFTcontrolPointArray[index] = new Point(old);
            }
        }
    }
    public void RIGHTsetCPElement(int index, Point newVal){  //same as left variant
        int x = newVal.x-centerOffset;
        int y = newVal.y-centerOffset;
        int xoffset = x - RIGHTcontrolPointArray[index].x;
        int yoffset = y - RIGHTcontrolPointArray[index].y;
        Point[] tempCPA = new Point[controlTotal];
        copyCPA(RIGHTcontrolPointArray,tempCPA);
        boolean success = true;
        if(selectionList[index] == 1) {
            for (int i=0; i < controlTotal; i++){
                if(selectionList[i] == 1){
                    index = i;
                    x = RIGHTcontrolPointArray[index].x + xoffset;
                    y = RIGHTcontrolPointArray[index].y + yoffset;
                    tempCPA[index] = new Point(x, y);
                    if (testBounds(tempCPA, index))
                        tempCPA[index] = new Point(x, y);
                    else
                        success = false;
                }
                if(!success)
                    break;
            }
            if(success)
                copyCPA(tempCPA,RIGHTcontrolPointArray);
            repaint();
        }
        else {
            Point old = RIGHTcontrolPointArray[index];
            RIGHTcontrolPointArray[index] = new Point(x, y);
            if (testBounds(RIGHTcontrolPointArray, index)) {
                RIGHTcontrolPointArray[index] = new Point(x, y);
                repaint();
            } else {
                RIGHTcontrolPointArray[index] = new Point(old);
            }
        }
    }

    public int LEFTclickInControl(Point click){  //check distance from control points of click, return point index if point is found, otherwise return -1
        for (int i =1; i<controlSize-1; i++) {
            for (int j = 1; j < controlSize - 1; j++) {
                int k = i*controlSize + j;  //special index to only check interior control point (ignore fake border control point)
                double distance = Math.sqrt(Math.pow(LEFTcontrolPointArray[k].getX() - click.getX(), 2) + Math.pow(LEFTcontrolPointArray[k].getY() - click.getY(), 2));
                if (distance <= dotSize)  //if offset is within dot
                    return k;
            }
        }
        return -1;  //interacting with none of them
    }

    public int RIGHTclickInControl(Point click){  //same as left variant
        for (int i =1; i<(controlSize-1); i++) {
            for (int j = 1; j < controlSize - 1; j++) {
                int k = i*controlSize + j;
                double distance = Math.sqrt(Math.pow(RIGHTcontrolPointArray[k].getX() - click.getX(), 2) + Math.pow(RIGHTcontrolPointArray[k].getY() - click.getY(), 2));
                if (distance <= dotSize)
                    return k;
            }
        }
        return -1;  //interacting with none of them
    }

    private void repaint(){ //update CPA variables for both render panels
        LP.setCP(LEFTcontrolPointArray);
        RP.setCP(RIGHTcontrolPointArray);
    }
}