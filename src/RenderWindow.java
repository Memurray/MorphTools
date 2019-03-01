import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;


public class RenderWindow extends JFrame {
    private Container c;
    private JButton renderButton,previewButton;
    public JSlider fpsSlider, timeSlider;
    private JPanel fpsPanel, timePanel,bottomPanel,panel,buttonPanel;
    private JLabel fpsLabel, timeLabel;
    private RenderLogic renderLogic;


    public RenderWindow(Point LEFT[], Point RIGHT[],int points,int xDim, int yDim, BufferedImage i1, BufferedImage i2 ) {  //passed a ton of information that the window doesnt use
        // because it was easier to have main window call a new window instead of calling the logic function for render
        super("Render");
        c = getContentPane();
        renderLogic = new RenderLogic(LEFT,RIGHT,points,xDim,yDim,i1,i2);  //create the controller object with all the information passed to window constructor
        panel = renderLogic.retPanel();
        fpsPanel = new JPanel();
        timePanel = new JPanel();
        bottomPanel = new JPanel();
        buttonPanel = new JPanel();
        renderButton = new JButton("Render to files");  //button for actually generating the jpg from morph
        renderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                renderLogic.renderButtonClicked(fpsSlider.getValue(),timeSlider.getValue());
            }
        });
        previewButton = new JButton("Preview");  //button to see what the morph looks like without creating the jpgs
        previewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                renderLogic.previewButtonClicked(fpsSlider.getValue(),timeSlider.getValue());
            }
        });


        fpsLabel = new JLabel("  Frame Per Second: 30");
        fpsSlider = new JSlider(SwingConstants.HORIZONTAL, 10, 60, 30 );
        fpsSlider.addChangeListener(new ChangeListener(){
            public void stateChanged( ChangeEvent e ){  //when slider changed, update tick time label (actual tick time updated only when preview button clicked)
                fpsLabel.setText("  Frames Per Second: " + Integer.toString(fpsSlider.getValue()));
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
        buttonPanel.setLayout(new GridLayout(2,1,1,4));
        buttonPanel.add(previewButton);
        buttonPanel.add(renderButton);


        bottomPanel.add(fpsPanel,BorderLayout.WEST);
        bottomPanel.add(timePanel,BorderLayout.EAST);
        bottomPanel.add(buttonPanel,BorderLayout.SOUTH);

        c.add(panel,BorderLayout.CENTER);
        c.add(bottomPanel,BorderLayout.SOUTH);
        this.pack();
        setVisible(true);
    }
}