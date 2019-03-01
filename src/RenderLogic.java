import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class RenderLogic {
    private Timer animationTimer;
    private Point LCPA[],RCPA[],ICPA[];
    private int step = 0;
    private int cp,totalPoints;
    private double stepMax;
    private RenderPanel panel;
    private BufferedImage image1,image2,bim,bim1,bim2;
    private int height, width;
    private MorphTools m;
    private int renderMode;

    public RenderLogic(Point LEFT[], Point RIGHT[],int points,int xDim, int yDim, BufferedImage i1, BufferedImage i2 ) {
        height = yDim;
        width = xDim;
        image1 = i1;
        image2 = i2;
        cp = points;
        totalPoints = points*points;
        LCPA = LEFT;
        RCPA = RIGHT;
        ICPA = new Point[totalPoints];  //intermediate CPA
        panel = new RenderPanel(image1);  //panel starts off by showing image 1
        bim1 = new BufferedImage (width, height,BufferedImage.TYPE_INT_RGB);  //allocate blank bims
        bim2 = new BufferedImage (width, height,BufferedImage.TYPE_INT_RGB);
        m = new MorphTools();  //define object of morphtools

        animationTimer = new Timer(30, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if(step > stepMax) { //if current step exceeds max frame
                    animationTimer.stop(); //stop timer
                    if(renderMode == 1){
                        JOptionPane.showMessageDialog(null, "Rendering to files done.", "Rendering Done", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
                else {
                    double stepping = (double) step / stepMax;  //fractional progress through animation
                    for (int i = 0; i < totalPoints; i++)
                        ICPA[i] = new Point(LCPA[i].x + (int) (stepping * (RCPA[i].x - LCPA[i].x)), LCPA[i].y + (int) (stepping * (RCPA[i].y - LCPA[i].y))); //linear interpolation based on stepping factor, cast to int to assign to nearest pixel
                    morph();  //run the morph function with new intermediate CPA
                    blend(1 - stepping);  //blend together the 2 output bims created by morph
                    panel.setBim(bim);  //draw composite bim
                    if(renderMode == 1) { //if render button was clicked (not preview)
                        try {
                            File outputfile = new File("images/image" + (step+1) + ".jpg"); //write frames to file named image<current step number>.jpg in images folder
                            ImageIO.write(bim, "jpg", outputfile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    step++;
                }
            }
        });
    }

    public Triangle makeTriangle(int row, int col, int top, int select){  //extract triangle from CPA
        Point CPA[];  //new CPA to allow easy selection of 1 of the 3 arrays of interest
        Triangle t;
        int index;
        if(select == 0)  //select LEFT CPA
            CPA = LCPA;
        else if(select == 1)  //select RIGHT CPA
            CPA = RCPA;
        else  //selection intermediate CPA
            CPA = ICPA;
        index = row * cp + col;  //index in CPA defined by this equation (controls out of bounds)
        if(top == 0)  //each start point is considered to refer to 2 different triangles, so selection of which is needed
            t = new Triangle(CPA[index].x,CPA[index].y,CPA[index+1].x,CPA[index+1].y,CPA[index+1+cp].x,CPA[index+1+cp].y);
        else
            t = new Triangle(CPA[index].x,CPA[index].y,CPA[index+cp].x,CPA[index+cp].y,CPA[index+1+cp].x,CPA[index+1+cp].y);
        return t;
    }

    public void morph(){
       for(int i=0; i<(cp-1); i++){  //for all rows -1  ex. (10 CP wide has 12 virtual points, and I want to inspect 11 of them)
           for(int j=0; j<(cp-1); j++){  //for all columns - 1
               m.warpTriangle(image1,bim1,makeTriangle(i,j,0,0),makeTriangle(i,j,0,2),null,null);  //call warp on CP origin triangle top, LEFT vs intermediate, applied to image1
               m.warpTriangle(image1,bim1,makeTriangle(i,j,1,0),makeTriangle(i,j,1,2),null,null);  //call warp on CP origin triangle bottom, LEFT vs intermediate, applied to image1
               m.warpTriangle(image2,bim2,makeTriangle(i,j,0,1),makeTriangle(i,j,0,2),null,null);  //call warp on CP origin triangle top, RIGHT vs intermediate, applied to image2
               m.warpTriangle(image2,bim2,makeTriangle(i,j,1,1),makeTriangle(i,j,1,2),null,null);  //call warp on CP origin triangle bottom, RIGHT vs intermediate, applied to image2
           }
       }
    }

    public JPanel retPanel(){
        return panel.retPanel();
    }

    //This function is a slightly modified version of a function I found online at "http://www.informit.com/articles/article.aspx?p=1245201"
    //I understand what it's doing and if I tried to remake it myself i'd end up basically doing the same thing so i figured it would be better to just acknowledge the source.
    private void blend(double weight){
        bim = new BufferedImage (width, height,BufferedImage.TYPE_INT_RGB);  //blank bim
        int [] rgbim1 = new int [width];  //row of pixel data for bim1
        int [] rgbim2 = new int [width]; //row of pixel data for bim2
        int [] rgbim3 = new int [width];  //row of pixel data for composite bim
        for (int row = 0; row < height; row++){  //for each row
            bim1.getRGB (0, row, width, 1, rgbim1, 0, width);  //save that row's pixel data to array
            bim2.getRGB (0, row, width, 1, rgbim2, 0, width);

            for (int col = 0; col < width; col++){  //for each column (inside the current row)
                int rgb1 = rgbim1 [col];  //grab pixel
                int r1 = (rgb1 >> 16) & 255;  //split out red
                int g1 = (rgb1 >> 8) & 255; //split out green
                int b1 = rgb1 & 255; //split out blue

                int rgb2 = rgbim2 [col];
                int r2 = (rgb2 >> 16) & 255;
                int g2 = (rgb2 >> 8) & 255;
                int b2 = rgb2 & 255;

                int r3 = (int) (r1*weight+r2*(1.0-weight));  //linearly combined r1 and r2 based on current weight
                int g3 = (int) (g1*weight+g2*(1.0-weight)); //linearly combined g1 and g2 based on current weight
                int b3 = (int) (b1*weight+b2*(1.0-weight)); //linearly combined b1 and b2 based on current weight
                rgbim3 [col] = (r3 << 16) | (g3 << 8) | b3;  //build rgb for that pixel
            }
            bim.setRGB (0, row, width, 1, rgbim3, 0, width);  //add completed row to composite bim
        }
    }

    public void previewButtonClicked(int fps, int time) {  //clicking preview button calls this function
        renderMode =0;  //dont save images
        stepMax = fps * time - 1;  //on click save number of frames to render
        animationTimer.setDelay((int) 1000 / fps); //on click assign animation timer tick time
        step = 0;  //start at frame 0 on click
        animationTimer.start();  //start timer
    }

    public void renderButtonClicked(int fps, int time) { //clicking render button calls this function
        renderMode =1;  //save images
        stepMax = fps * time - 1;  //on click save number of frames to render
        animationTimer.setDelay((int) 1000 / fps); //on click assign animation timer tick time
        step = 0;  //start at frame 0 on click
        animationTimer.start();  //start timer
    }
}