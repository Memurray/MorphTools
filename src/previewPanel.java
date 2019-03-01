import javax.swing.*;
import java.awt.*;

public class previewPanel extends JPanel {
    private int controlSize;
    private int controlTotal;
    private Point controlPointArray[];
    private int dotSize= 9;
    private int centerOffset = 4;
    private int xDim;
    private int yDim;

    public previewPanel(int points,int x, int y){
        controlSize = points;
        controlTotal = controlSize*controlSize;
        xDim = x;
        yDim = y;
        this.setPreferredSize(new Dimension(xDim+10,yDim+10));
    }

    public void setPoints(Point points[]){
        controlPointArray = points;
        repaint();
    }

    public void paintComponent(Graphics g) {  //each paint
        super.paintComponent(g);
        for(int i =0; i < controlTotal; i++){
            if((i+1)%controlSize !=0){  //render horizontal lines
                g.drawLine((int)controlPointArray[i].getX(),(int)controlPointArray[i].getY(),(int)controlPointArray[i+1].getX(),(int)controlPointArray[i+1].getY());
            }
            if(i< controlTotal-controlSize){ //render vertical lines
                g.drawLine((int)controlPointArray[i].getX(),(int)controlPointArray[i].getY(),(int)controlPointArray[i+controlSize].getX(),(int)controlPointArray[i+controlSize].getY());
            }
            if(i< controlTotal-controlSize-1 && (i+1)%controlSize!=0){  //render diagonal lines
                g.drawLine((int)controlPointArray[i].getX(),(int)controlPointArray[i].getY(),(int)controlPointArray[i+controlSize+1].getX(),(int)controlPointArray[i+controlSize+1].getY());
            }
            if(i>=controlSize && i< controlTotal-controlSize && i%controlSize!=0 && (i+1)%controlSize!=0) {  //render dots (control points)
                g.fillOval((int) controlPointArray[i].getX() - centerOffset, (int) controlPointArray[i].getY() - centerOffset, dotSize, dotSize);
                g.setColor(Color.WHITE);
                g.fillOval((int) controlPointArray[i].getX() - centerOffset+2, (int) controlPointArray[i].getY() - centerOffset+2, dotSize-4, dotSize-4);
            }
            g.setColor(Color.BLACK);
        }
    }
}