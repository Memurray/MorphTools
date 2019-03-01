import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class previewWindow extends JFrame {
    private Container c;
    private JButton previewButton;
    private JSlider fpsSlider, timeSlider;
    private JPanel fpsPanel, timePanel,bottomPanel;
    private JLabel fpsLabel, timeLabel;
    private Timer animationTimer;
    private Point LCPA[],RCPA[],ICPA[];
    private int step = 0;
    private int totalPoints;
    private double stepMax;
    private previewPanel preview;

    public previewWindow(Point LEFT[], Point RIGHT[],int points,int xDim, int yDim ) {
        super("Preview");
        c = getContentPane();
        totalPoints = points*points;
        LCPA = LEFT;
        RCPA = RIGHT;
        ICPA = new Point[totalPoints];
        fpsPanel = new JPanel();
        timePanel = new JPanel();
        bottomPanel = new JPanel();
        preview = new previewPanel(points,xDim,yDim);
        preview.setPoints(LEFT);
        previewButton = new JButton("Preview");

        previewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stepMax = fpsSlider.getValue()*timeSlider.getValue();  //on click save number of frames to render
                animationTimer.setDelay((int)1000/fpsSlider.getValue()); //on click assign animation timer tick time
                step = 0;  //start at frame 0 on click
                animationTimer.start();  //start timer
            }
        });

        //******************************************
        // Technically this logic would make more sense in a controller class, which I did for the render button
        // but due to the simplicity this is left as is.
        //******************************************
        animationTimer = new Timer(30, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if(step >= stepMax)  //if current step exceeds max frame
                    animationTimer.stop(); //stop timer
                double stepping = (double)step/stepMax;  //fractional progress through animation
                for(int i =0; i< totalPoints; i++)
                    ICPA[i] = new Point(LCPA[i].x + (int)(stepping *( RCPA[i].x-LCPA[i].x)),LCPA[i].y + (int)(stepping *( RCPA[i].y-LCPA[i].y))); //linear interpolation based on stepping factor, cast to int to assign to nearest pixel
                preview.setPoints(ICPA); //update CPA for rendering animation
                step++;
            }
        });
        //*********************************************

        fpsLabel = new JLabel("  Frames Per Second: 30");
        fpsSlider = new JSlider(SwingConstants.HORIZONTAL, 10, 60, 30 );
        fpsSlider.addChangeListener(new ChangeListener(){
              public void stateChanged( ChangeEvent e ){  //when slider changed, update tick time label (actual tick time updated only when preview button clicked)
                  fpsLabel.setText("  Framess Per Second: " + Integer.toString(fpsSlider.getValue()));
                  repaint();
              }
        }
        );

        timeLabel = new JLabel("  Transition time (s): 4");
        timeSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 10, 4 );
        timeSlider.addChangeListener(new ChangeListener(){
            public void stateChanged( ChangeEvent e ){  //when slider changes, update text value
                timeLabel.setText("  Transition time (s): " + Integer.toString(timeSlider.getValue()));
                repaint();
            }
        }
        );

        fpsPanel.setLayout(new GridLayout(2,1,1,1));
        fpsPanel.add(fpsLabel);
        fpsPanel.add(fpsSlider);

        timePanel.setLayout(new GridLayout(2,1,1,1));
        timePanel.add(timeLabel);
        timePanel.add(timeSlider);

        bottomPanel.add(fpsPanel,BorderLayout.WEST);
        bottomPanel.add(timePanel,BorderLayout.EAST);
        bottomPanel.add(previewButton,BorderLayout.SOUTH);

        c.add(preview,BorderLayout.CENTER);
        c.add(bottomPanel,BorderLayout.SOUTH);
        this.pack();
        setVisible(true);
    }
}