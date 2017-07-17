import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.LinkedList;
import java.awt.image.*;
import java.nio.file.*;
import java.util.Scanner;
import java.awt.geom.Rectangle2D;

/**
 * Component class to allow for the marking of an image with bounding boxes
 * The component uses a container to display the image and the drawn boxes, along with the control panel
 */
public class ImageMarker extends JPanel {

  private int width;                        // Total width of the marker frame and control panel
  private int height;                       // Total height of the image marker
  private Path imgDirPath;                  // Path to the image directory
  private Path saveDirPath;                 // Path to the save directory
  private ListItem openedItem;              // Image item currently opened in the editor
  private ImageContainer imageContainer;    // Container for the opened image
  private MarkerControlPanel controlPanel;  // Control panel for the image marker
  private FileExplorer imgList;             // The file explorer connected to the image marker

  /**
   * Intitializes the image marker with no opened image
   * @param  Path saveDir       Directory to save image completions
   * @param  Path imgDir        Directory where the images are located
   * @param  int  w             Pixel width of the entire image marker including the control panel
   * @param  int  h             Pixel height of the image marker
   * @param  int  controlWidth  Pixel width of the entire width to use for the control panel
   */
  public ImageMarker(Path saveDir, Path imgDir, int w, int h, int controlWidth) {
    super(new BorderLayout());
    this.width = w;
    this.height = h;
    setPreferredSize(new Dimension(this.width, this.height));
    this.imgDirPath = imgDir;
    this.saveDirPath = saveDir;
    this.openedItem = null;
    this.imageContainer = new ImageContainer(this.width-controlWidth-1, this.height);
    add(imageContainer, BorderLayout.WEST);
    this.controlPanel = new MarkerControlPanel(this, this.imageContainer, controlWidth, this.height);
    add(controlPanel, BorderLayout.EAST);
    this.imageContainer.addControls(this.controlPanel);
    this.imgList = null;
  }

  /**
   * Method used to resize the entire image marker when the window is resizeEditor
   * @param int w            New width of the component
   * @param int h            New height of the component
   * @param int controlWidth New width of the control panel within the component
   */
  public void resizeEditor(int w, int h, int controlWidth) {
    this.width = w;
    this.height = h;
    setPreferredSize(new Dimension(this.width, this.height));
    this.imageContainer.resizeContainer(this.width-controlWidth-1, this.height);
    this.controlPanel.resizePanel(controlWidth, this.height);
    repaint();
  }

  /**
   * Assigns a file explorer to the image marker
   * @param FileExplorer list File explorer associated with the image marker
   */
  public void setFileExplorer(FileExplorer list) {
    this.imgList = list;
  }

  /**
   * Returns the file explorer associated with the image marker
   * @return File explorer associated with the image marker
   */
  public FileExplorer getFileExplorer() {
    return this.imgList;
  }

  /**
   * Checks to see if an image with the given path is currently opened in the editor
   * @param  Path itemPath      Path to compare to
   * @return      Whether or not an image with the given path is opened in the editor
   */
  public boolean isOpen(Path itemPath) {
    if (this.openedItem != null && this.openedItem.getPath().equals(itemPath))
      return true;
    else
      return false;
  }

  /**
   * Opens the given image item in the editor
   * @param ListItem item Item to open in the editor
   */
  public void openImage(ListItem item) {
    // returns if the current image is already open to make sure the current
    // progress is not overwritten
    if (this.openedItem != null && this.openedItem.getPath().equals(item.getPath()))
      return;

    this.openedItem = item;
    this.imageContainer.openImage(item);
  }

  /**
   * Opens the given image, along with its previously saved information
   * @param ListItem item Item to open and load save information from
   */
  public void openCompletedImage(ListItem item) {
    openImage(item);
    Path savePath = ScrollList.convertToSavePath(this.imgDirPath, this.saveDirPath, this.openedItem.getPath());
    LinkedList<Rectangle2D> rectangles = getSaveRectangles(savePath);
    this.imageContainer.loadRectangles(rectangles);
  }

  /**
   * Loads in the rectangle information from the file at the save path
   * @param  Path savePath      Path to load the save file from
   * @return      List of rectangles previously saved
   */
  public LinkedList<Rectangle2D> getSaveRectangles(Path savePath) {
    LinkedList<Rectangle2D> rects = new LinkedList<Rectangle2D>();
    Scanner scan = null;
    try {
      scan = new Scanner(new FileReader(savePath.toFile()));

      for (int i=0; i < 3; i++)
        scan.nextLine();

      while (scan.hasNextLine()) {
        String line = scan.nextLine();
        String[] parts = line.split(",");
        for (int i = 0; i < parts.length; i++) {
          float x = Float.parseFloat(parts[0]);
          float y = Float.parseFloat(parts[1]);
          float w = Float.parseFloat(parts[2]);
          float h = Float.parseFloat(parts[3]);
          rects.add(new Rectangle2D.Float(x, y, w, h));
        }
      }
    } catch (Exception e) {
      System.out.println("Error!--" + e);
    } finally {
      scan.close();
    }
    return rects;
  }

  /**
   * Saves the progress of the currently opened image to its save path
   */
  public void save() {
    if (this.openedItem == null)
      return;

    File saveDir = this.saveDirPath.toFile();
    if (!saveDir.exists())
      saveDir.mkdir();

    Path savePath = ScrollList.convertToSavePath(this.imgDirPath, this.saveDirPath, this.openedItem.getPath());

    Path saveDirPath = savePath.getParent();
    saveDir = saveDirPath.toFile();
    if (!saveDir.exists())
      saveDir.mkdir();

    Path endImgPath = this.imgDirPath.relativize(this.openedItem.getPath());
    String firstTextLine = endImgPath.toString();

    printSave(savePath, firstTextLine);
    this.imgList.reloadList();
  }

  /**
   * Prints the image's save information to the save path
   * @param Path   savePath  Save path for the file
   * @param String firstLine First line of the save file i.e. "parentFolder/imgName"
   */
  public void printSave(Path savePath, String firstLine) {
    PrintWriter pw = null;
    try {
      pw = new PrintWriter(savePath.toFile());
      pw.println(firstLine);
      BufferedImage orgImg = this.imageContainer.getOrgImg();
      pw.println(orgImg.getWidth() + "," + orgImg.getHeight());
      LinkedList<Rectangle2D> rects = this.imageContainer.getRescaledRectangles();
      pw.println(rects.size());
      for (Rectangle2D rect : rects) {
        String rectLine = rect.getX() + "," + rect.getY() + "," +
            rect.getWidth() + "," + rect.getHeight();
          pw.println(rectLine);
      }

    } catch (Exception e) {
      System.out.println("Error!--" + e);
    } finally {
      pw.close();
    }
  }

  @Override
  public void paintComponent(Graphics g) {
    this.imageContainer.paintComponent(g);
  }
}
