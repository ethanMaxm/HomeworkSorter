import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.*;
/**
 * This class is for making JFrame object
 *
 * @author EthanMaxm
 * @version 09/13/23
 */
public class GUIFrame extends JFrame{
    /**
     * This constructor is for making conformation messages
     * @param message What the caller would like to tell the user
     */
    public GUIFrame(String message){
        JPanel panel = new JPanel();
        panel.add(new JLabel(message));
        setPreferredSize(new Dimension(200,60));
        add(panel);
        setTitle("Conformation message");
        pack();
        setVisible(true);
        setLocationRelativeTo(null);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    /**
     * This constructor is for the frames that the caller can interact with
     * @param frameTitle the title the caller wishes to give the frame
     * @param close if the caller wishes for the entire program to stop or just the one frame
     * @param panel the panel the caller wishes the frame to contain
     */
    public GUIFrame(String frameTitle, boolean close, JPanel panel){

        add(panel);
        pack();
        setTitle(frameTitle);
        setLocationRelativeTo(null);
        if (close) {setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);}
        else{setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);}
        setVisible(true);
    }

}
