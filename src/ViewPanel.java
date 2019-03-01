import java.awt.*;
import javax.swing.*;
import java.awt.Point;
import java.awt.image.*;

public class ViewPanel extends JPanel {
    private int controlSize;
    private int controlTotal;
    private Point controlPointArray[];
    private int glow[];
    private int dotSize= 9;
    private int xDim=550;
    private int yDim=550;
    private int centerOffset = 4;
    private BufferedImage bim=null;
    private Point selectionStart, selectionEnd;

    public ViewPanel(int points){
        controlSize = points;
        controlTotal = controlSize*controlSize;
        glow = new int[controlTotal];
        this.setPreferredSize(new Dimension(xDim+10,yDim+10)); //panel is slightly larger than content
    }

    public void reInitialize(int points, int newX, int newY){  //if size or number of points need to change, updates
        xDim = newX;
        yDim = newY;
        controlSize = points;
        controlTotal = controlSize*controlSize;
        glow = new int[controlTotal];
        this.setPreferredSize(new Dimension(xDim+10,yDim+10));
    }

    public void setCP(Point CPArray[]){  //passed in CPA is stored and repainted
        controlPointArray = CPArray;
        repaint();
    }

    public void setSelection(Point start, Point end){
        selectionStart = start;
        selectionEnd = end;
        repaint();
    }

    public ViewPanel retPanel(){
        return ViewPanel.this;
    }

    public void setGlow(int[] newGlow){
        for (int i = 0; i < controlTotal; i++){
            glow[i] = newGlow[i];
        }
        repaint();
    }

    public void setImage(BufferedImage img) {  //if file image is passed in, buffer image and update render area
        if (img == null) return;
        bim = img;
        setPreferredSize(new Dimension(bim.getWidth()+10, bim.getHeight()+10));
        this.repaint();
    }


    public void paintComponent(Graphics g) {  //each paint draw lines based on CPA
        super.paintComponent(g);
        Graphics2D big = (Graphics2D) g;
        big.drawImage(bim, 0, 0, this);
        for(int i =0; i < controlTotal; i++){
            if((i+1)%controlSize !=0){ //draw all valid horizontal lines
                g.drawLine((int)controlPointArray[i].getX(),(int)controlPointArray[i].getY(),(int)controlPointArray[i+1].getX(),(int)controlPointArray[i+1].getY());
            }
            if(i< controlTotal-controlSize){  //draw all valid vertical lines
                g.drawLine((int)controlPointArray[i].getX(),(int)controlPointArray[i].getY(),(int)controlPointArray[i+controlSize].getX(),(int)controlPointArray[i+controlSize].getY());
            }
            if(i< controlTotal-controlSize-1 && (i+1)%controlSize!=0){ //draw all valid diagonal lines
                g.drawLine((int)controlPointArray[i].getX(),(int)controlPointArray[i].getY(),(int)controlPointArray[i+controlSize+1].getX(),(int)controlPointArray[i+controlSize+1].getY());
            }
            if(glow[i] == 1) //if selected (and just glow will be = i)
                g.setColor(Color.RED);  //make bubble red
            if(i>=controlSize && i< controlTotal-controlSize && i%controlSize!=0 && (i+1)%controlSize!=0) {  //for all interior points
                g.fillOval((int) controlPointArray[i].getX() - centerOffset, (int) controlPointArray[i].getY() - centerOffset, dotSize, dotSize);  //outer circle (black or red)
                g.setColor(Color.WHITE);
                g.fillOval((int) controlPointArray[i].getX() - centerOffset+2, (int) controlPointArray[i].getY() - centerOffset+2, dotSize-4, dotSize-4); //inner white circle
            }
            g.setColor(Color.BLACK);
            if(!(selectionStart == null || selectionEnd == null)){  //as long as both points are defined
                g.setColor(Color.RED);
                int x=Math.min(selectionStart.x, selectionEnd.x);
                int y=Math.min(selectionStart.y, selectionEnd.y);
                int width=Math.abs(selectionStart.x - selectionEnd.x);
                int height=Math.abs(selectionStart.y - selectionEnd.y);
                g.drawRect(x,y,width,height);  //draw a red rectangle around the area the user is right click and drag selecting
                g.setColor(Color.BLACK);
            }
        }
    }
}