// PROBLEMS
// While the program does not crash, it is possible to hit major slow downs when performing group selected
// control point moving where line intersection is detected. The program will recover and will not allow illegal placement
// but the sheer number of calculations being performed and rejected can result in an unresponsive user experience until
// user suggested position is clearly valid and the program has successfully caught up.

// NOTES
//Control points are 2 tone to make readability easier on complex backgrounds (images)
//Dragging is constrained to prevent line intersection, but extremely tiny triangles are still possible
//setting image present in file menu bar at top
//Control Point group selection was implemented by way of right clicking and dragging, no additional user effort is required
//Clicking on a point inside the selection on either image side will move all points that are selected
//Clicking on any control point not in this selection will disable the group selection, or simply right clicking anywhere will disable group
//Since group selection is done by highlighting with a right click drag, a right click on a singular control point will not select it.
//Preview warp window is the same as in project 3, I just show how the control points will move
//The functionality of previewing how the image will change with the morphing is present from the render morph window (opened with the render morph button)
//preview warp window has fps and transition time settings
//these setting are captured when the Preview button is clicked, meaning changing these sliders during render has no effect, this is intentional
//Clicking Preview during animation will restart it
//Clicking the Render Morph button from the main window will popup an error message box if files arent both defined or if they arent the same size
//If valid, render morph window will open
//Render Morph window contains fps and time sliders
//Pressing the preview button inside the render window, will show the morph occuring in realtime
//Pressing the render button inside the render window, will still show the morph and will save the images to jpg files, this can be done in realtime at low fps, but will fall behind at higher fps.
//You will get a popup box when rendering is done, this is to ensure it's obvious when morphing rendering is finished.

// ACKNOWLEDGEMENTS
// MorphTools.java was provided by Dr. Seales and other than adjusting alpha values, nothing was changed
// Triangle.java was provided by Dr. Seales and was not altered
// Blend function in RenderLogic.java is a slightly modified version of a function I found online at "http://www.informit.com/articles/article.aspx?p=1245201"
// Everything else was solely my own product


import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class JMorph extends JFrame {
    private Container c;
    private ControlLogic morphLogic;  //controller class
    private JPanel lPanel, rPanel;  //panel holders for triangle grid
    private int dragNumber;  //which point is being dragged
    private boolean isLDragging = false;  //if a point is currently being dragged
    private boolean isRDragging = false;  //likely could condense to a single bool since there is no way to be dragging both left and right
    private boolean rightClickLDragging =false;
    private boolean rightClickRDragging =false;
    private Point rightClickStartL, rightClickStartR;
    private JButton previewButton, renderButton;
    private JSlider controlPointsSlider, LBrightSlider,RBrightSlider;
    private JLabel controlLabel, LBrightLabel, RBrightLabel;
    private JPanel sliderPanel = new JPanel();  //organization panel for slider element and label
    private JPanel LBrightPanel = new JPanel();
    private JPanel RBrightPanel = new JPanel();
    private JPanel bottomPanel = new JPanel();  //organization panel for combination of button and sliderPanel
    private JPanel bottomMidPanel = new JPanel();


    public JMorph(int points) {
        super("JMorph");
        c = getContentPane();
        morphLogic = new ControlLogic(points,this);  //create the controller object,
        // I think in my current design I'm not using the passed parent reference, but it's there if/when I need it
        lPanel = morphLogic.retLPanel();  //pass back of left render panel
        rPanel = morphLogic.retRPanel();
        previewButton = new JButton("Preview Warp");
        previewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                morphLogic.openPreview();
            } //button click opens preview window
        });

        renderButton = new JButton("Render Morph");
        renderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                morphLogic.openRender();
            } //button click opens render window
        });

        //I saw this in one of the provided programs and liked the cleaner look of constructor by dividing into function blocks
        buildMenus();  //configures top bar menu system
        buildMouseSettings();  //configures mouse interactions
        buildSlider();  //configures the slider bar
        buildBrightnessSliders();
        buildLayout();  //configures adding all the elements to the frame
        this.pack();  //no set size, just fit to content
        setVisible(true);
    }

    private void buildMenus () {
        final JFileChooser fc = new JFileChooser(".");
        JMenuBar bar = new JMenuBar();
        this.setJMenuBar (bar);
        JMenu fileMenu = new JMenu ("File");
        JMenuItem fileLeft = new JMenuItem ("Set Left Image");
        JMenuItem fileRight = new JMenuItem ("Set Right Image");
        JMenuItem fileexit = new JMenuItem ("Exit");

        JMenu helpMenu = new JMenu ("Help");
        JMenuItem tips = new JMenuItem ("Tips");

        tips.addActionListener(  //simple close option in menu
                new ActionListener () {
                    public void actionPerformed (ActionEvent e) {
                        SplashScreen splash = new SplashScreen();
                    }
                }
        );

        fileLeft.addActionListener( //open file chooser and send file name off to controller
                new ActionListener () {
                    public void actionPerformed (ActionEvent e) {
                        int returnVal = fc.showOpenDialog(JMorph.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = fc.getSelectedFile();
                            morphLogic.LEFTopenImage(file);
                        }
                    }
                }
        );
        fileRight.addActionListener(//open file chooser and send file name off to controller
                new ActionListener () {
                    public void actionPerformed (ActionEvent e) {
                        int returnVal = fc.showOpenDialog(JMorph.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = fc.getSelectedFile();
                            morphLogic.RIGHTopenImage(file);
                        }
                    }
                }
        );
        fileexit.addActionListener(  //simple close option in menu
                new ActionListener () {
                    public void actionPerformed (ActionEvent e) {
                        System.exit(0);
                    }
                }
        );
        fileMenu.add(fileLeft);
        fileMenu.add(fileRight);
        fileMenu.add(fileexit);
        helpMenu.add(tips);
        bar.add(fileMenu);
        bar.add(helpMenu);
    }

    private void buildMouseSettings(){
        lPanel.addMouseListener(new MouseListener(){  //track mouse interactions with left render panel
            public void mouseExited(MouseEvent e){}
            public void mouseEntered(MouseEvent e){}
            public void mouseReleased(MouseEvent e){
                isLDragging = false;
                if (rightClickLDragging) {  //if user was rightclick dragging
                    morphLogic.endSelection(rightClickStartL, new Point(e.getPoint()),0);  //call endSelection function with start/end coords
                    rightClickStartL = null;  //disables dragging animation
                    rightClickLDragging = false;  //no longer dragging
                }

            }  //as soon as mouse released, no longer dragging
            public void mousePressed(MouseEvent e){
                if(SwingUtilities.isLeftMouseButton(e)) {   //if click was a left click
                    dragNumber = morphLogic.LEFTclickInControl(e.getPoint());  //requests which element is being interacted with
                    if (dragNumber >= 0) { //if click is on valid element
                        isLDragging = true;  //now dragging
                        morphLogic.Glow(dragNumber);  //function to make this point on both render panels glow (highlighting point of interaction)
                    }
                }
                else if(SwingUtilities.isRightMouseButton(e)){  //if click was a rightclick
                    rightClickStartL = e.getPoint();  //capture location of click
                    rightClickLDragging = true;  //start dragging
                }
            }
            public void mouseClicked(MouseEvent e){}
        });
        lPanel.addMouseMotionListener(new MouseMotionListener(){
            public void mouseDragged(MouseEvent e) {
                if(isLDragging){  //if dragging with left click
                    morphLogic.LEFTsetCPElement(dragNumber,new Point(e.getX(),e.getY()));  //if dragging, constantly repaint with new coords
                }
                else if(rightClickLDragging){ //if dragging with right click (intentional priority given to left click over right)
                    morphLogic.areaSelect(rightClickStartL,new Point(e.getPoint()),0);  //send current location of mouse and originating click location
                }
            }
            public void mouseMoved(MouseEvent e) {}
        });

        rPanel.addMouseListener(new MouseListener(){ //identical logic but now for right render panel
            public void mouseExited(MouseEvent e){}
            public void mouseEntered(MouseEvent e){}
            public void mouseReleased(MouseEvent e){
                isRDragging = false;
                if (rightClickRDragging) {
                    morphLogic.endSelection(rightClickStartR, new Point(e.getPoint()),1);
                    rightClickStartR = null;
                    rightClickRDragging = false;
                }
            }
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    dragNumber = morphLogic.RIGHTclickInControl(e.getPoint());
                    if (dragNumber >= 0) {
                        isRDragging = true;
                        morphLogic.Glow(dragNumber);
                    }
                }
                else if(SwingUtilities.isRightMouseButton(e)){
                    rightClickStartR = e.getPoint();
                    rightClickRDragging = true;
                }

            }
            public void mouseClicked(MouseEvent e){}
        });

        rPanel.addMouseMotionListener(new MouseMotionListener(){
            public void mouseDragged(MouseEvent e) {
                if(isRDragging){
                    morphLogic.RIGHTsetCPElement(dragNumber,new Point(e.getX(),e.getY()));
                }
                else if(rightClickRDragging){
                    morphLogic.areaSelect(rightClickStartR,new Point(e.getPoint()),1);
                }
            }
            public void mouseMoved(MouseEvent e) {}
        });
    }

    private void buildSlider(){ //slider with value text to quickly alter control point density
        controlLabel = new JLabel("Points: 10");
        controlLabel.setHorizontalAlignment(SwingConstants.CENTER);
        controlPointsSlider = new JSlider(SwingConstants.HORIZONTAL, 5, 20, 10 );
        controlPointsSlider.addChangeListener(new ChangeListener(){
            public void stateChanged( ChangeEvent e ){  //when slider changed, update tick time
                controlLabel.setText("Points: " + Integer.toString(controlPointsSlider.getValue()));
                morphLogic.reInitalizePoints(controlPointsSlider.getValue());  //controller must adapt data structure to new size inputs
                repaint();
            }
        }
        );
    }

    private void buildBrightnessSliders(){ //slider with value text to quickly alter control point density
        LBrightLabel = new JLabel("Left Brightness: +0");
        LBrightSlider = new JSlider(SwingConstants.HORIZONTAL, -100, 100, 0 );
        LBrightSlider.setMajorTickSpacing(50);
        LBrightSlider.setPaintTicks(true);
        LBrightSlider.setPaintLabels(true);
        LBrightSlider.addChangeListener(new ChangeListener(){
              public void stateChanged( ChangeEvent e ){  //when slider changed, update tick time
                  if(LBrightSlider.getValue()>=0 )
                      LBrightLabel.setText("Left Brightness: +" + Integer.toString(LBrightSlider.getValue()));
                  else
                      LBrightLabel.setText("Left Brightness: " + Integer.toString(LBrightSlider.getValue()));
                  morphLogic.setBright((float) LBrightSlider.getValue(),0);  //controller must adapt data structure to new size inputs
                  repaint();
              }
          }
        );

        RBrightLabel = new JLabel("Right Brightness: +0");
        RBrightSlider = new JSlider(SwingConstants.HORIZONTAL, -100, 100, 0 );
        RBrightSlider.setMajorTickSpacing(50);
        RBrightSlider.setPaintTicks(true);
        RBrightSlider.setPaintLabels(true);
        RBrightSlider.addChangeListener(new ChangeListener(){
            public void stateChanged( ChangeEvent e ){  //when slider changed, update tick time
                if(RBrightSlider.getValue()>=0 )
                    RBrightLabel.setText("Right Brightness: +" + Integer.toString(RBrightSlider.getValue()));
                else
                    RBrightLabel.setText("Right Brightness: " + Integer.toString(RBrightSlider.getValue()));
                morphLogic.setBright((float) RBrightSlider.getValue(),1);  //controller must adapt data structure to new size inputs
                repaint();
            }
        }
        );
    }

    private void buildLayout(){  //add all the elements to the JFrame
        LBrightPanel.setLayout(new GridLayout(2,1,1,0));
        LBrightLabel.setHorizontalAlignment(SwingConstants.CENTER);
        LBrightPanel.add(LBrightLabel);
        LBrightPanel.add(LBrightSlider);

        RBrightPanel.setLayout(new GridLayout(2,1,1,0));
        RBrightLabel.setHorizontalAlignment(SwingConstants.CENTER);
        RBrightPanel.add(RBrightLabel);
        RBrightPanel.add(RBrightSlider);

        bottomMidPanel.add(previewButton,BorderLayout.EAST);
        bottomMidPanel.add(renderButton,BorderLayout.WEST);

        sliderPanel.setLayout(new GridLayout(3,1,1,3));
        sliderPanel.add(controlLabel);
        sliderPanel.add(controlPointsSlider);
        sliderPanel.add(bottomMidPanel);

        bottomPanel.add(LBrightPanel);
        bottomPanel.add(sliderPanel);
        bottomPanel.add(RBrightPanel);

        c.add(lPanel,BorderLayout.WEST);
        c.add(rPanel,BorderLayout.EAST);
        c.add(bottomPanel,BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        JMorph morphWindow = new JMorph(10); // default size of 10x10 control
        morphWindow.addWindowListener(
                new WindowAdapter(){
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                });
        SplashScreen splash = new SplashScreen();

    }
}