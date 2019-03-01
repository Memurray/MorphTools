import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SplashScreen extends JFrame {  //class for showing tips images and an ok button
    private Container c;
    private JButton okButton;

    public SplashScreen() {
        super("Tips");
        c = getContentPane();
        ImagePanel panel = new ImagePanel(  //load image into this jpanel
                new ImageIcon("splash.png").getImage());
        okButton = new JButton("OK");  //add ok button that closes just this window
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        c.add(panel,BorderLayout.CENTER);
        c.add(okButton,BorderLayout.SOUTH);
        this.pack();  //no set size, just fit to content
        setVisible(true);
    }
}

class ImagePanel extends JPanel {  //tiny class for loading image file into jpanel
    private Image img;
    public ImagePanel(String img) {
        this(new ImageIcon(img).getImage());
    }

    public ImagePanel(Image img) {
        this.img = img;
        setPreferredSize( new Dimension(img.getWidth(null), img.getHeight(null)));
    }

    public void paintComponent(Graphics g) {
        g.drawImage(img, 0, 0, null);
    }
}
