import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.nio.file.*;

/**
 * Core class to intialize the runnable program and operate the JFrame window
 */
public class Main extends JPanel {

  /**
   * Default method to run the program. No arguments neccessary for now
   * @param String[] args
   */
  public static void main(String[] args) {
    // Allows for a runnable program
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        runGUI();
      }
    });
  }

  /**
   * Initializes the JFrame window along with all internal components
   */
  public static void runGUI() {
    JFrame frame = new JFrame("FrameIt");
    frame.setBackground(new Color(190, 190, 190));
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Initializes the size of the window to be 90% of the total screen width and height
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int startWidth = (int)(screenSize.width*0.90);
    int startHeight = (int)(screenSize.height*0.90);
    frame.setBounds(0, 0, startWidth, startHeight);

    // Initializes the JPanel containing all of the application's components
    JComponent contPane = new Main(frame, startWidth, startHeight);
    contPane.setOpaque(true);
    frame.setContentPane(contPane);

    frame.pack();
    frame.setVisible(true);
  }

  private JFrame frame;               // JFrame of the application
  private float sidePanelScale;       // Scale to use for the two side panels
  private float imageEditorScale;     // Total scale of the entire image editor
  private ImageMarker editor;         // The image editor component
  private FileExplorer fileExplorer;  // The file explorer component
  private Path workDir;             // The working directory of the program
  private Path imgDir;              // The directory of the images
  private Path saveDir;             // The directory to save files to

  /**
   * Initializes all components of the application
   * @param  JFrame frame         The JFrame of the entire application
   * @param  int    width         Entire width of the window
   * @param  int    height        Entire height of the window
   * @return        Returns the JPanel containing all the application components to add the the JFrame
   */
  public Main(JFrame frame, int width, int height) {
    super(new BorderLayout());
    this.sidePanelScale = (float)1/6;
    this.imageEditorScale = (float)5/6;
    this.frame = frame;

    // Checks for name of operating System
    String osName = System.getProperty("os.name").toLowerCase();

    // By default, sets the working, image, and save directories based on operating system
    if (osName.indexOf("nix") >= 0 || osName.indexOf("nux") >= 0 || osName.indexOf("aix") >= 0) {
      this.workDir = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().getFile());
      // Replaces any "%20" (representing a space) with and actual space so Java.nio.File can successfully read the file path in linux
      if (this.workDir.toString().contains("%20"))
        this.workDir = Paths.get(this.workDir.toString().replace("%20", " "));
      if (!this.workDir.toFile().isDirectory())
        this.workDir = this.workDir.getParent();
    } else if (osName.indexOf("mac") >= 0 || osName.indexOf("win") >= 0)
      this.workDir = Paths.get(System.getProperty("user.dir"));

    this.imgDir = this.workDir.resolve("images");
    this.saveDir = this.workDir.resolve("saves");

    // Initializes the image editor and the file explorer to their respective sizes
    this.editor = new ImageMarker(this.saveDir, this.imgDir, (int)(width*this.imageEditorScale), height, (int)(width*this.sidePanelScale));
    this.fileExplorer = new FileExplorer(this.editor, this.imgDir, this.saveDir, (int)(width*this.sidePanelScale), height);
    this.editor.setFileExplorer(this.fileExplorer);

    // Adds the file explorer to the west side and the editor to the east side of the JPanel
    add(fileExplorer, BorderLayout.WEST);
    add(editor, BorderLayout.EAST);

    // Creates a component listener to respond to the resizing of the window
    ComponentAdapter resizeListener = new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
          resizePanel();
        }};

    // Adds the resizing listener to the JFrame
    this.frame.addComponentListener(resizeListener);
  }

  /**
   * Resizes the JPanel components to match the new size of the window
   */
  public void resizePanel() {
    Rectangle frameSize = frame.getBounds();
    editor.resizeEditor((int)(frameSize.width*this.imageEditorScale), frameSize.height, (int)(frameSize.width*this.sidePanelScale));
    fileExplorer.resizeExplorer((int)(frameSize.width*this.sidePanelScale), frameSize.height);
    repaint();
  }
}
