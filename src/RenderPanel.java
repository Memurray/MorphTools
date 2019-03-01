import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class RenderPanel extends JPanel {
    private int xDim;
    private int yDim;
    private BufferedImage bim;

    public RenderPanel(BufferedImage image){  //constructor, given only an image
        bim = image;
        xDim = image.getWidth();
        yDim = image.getHeight();
        this.setPreferredSize(new Dimension(xDim+10,yDim+10));  //small padding around image
    }

    public void setBim(BufferedImage image){ //update image
        bim = image;
        repaint();
    }

    public RenderPanel retPanel(){
        return RenderPanel.this;
    }

    public void paintComponent(Graphics g) {  //each paint
        super.paintComponent(g);
        Graphics2D big = (Graphics2D) g;
        big.drawImage(bim, 0, 0, this);  //draw the image
    }
}