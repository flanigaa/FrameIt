import java.awt.*;
import javax.swing.*;
import java.nio.file.*;
import java.awt.event.*;

/**
 * Class that contains the scrollable list of images and directories along with the scroll bar
 */
public class ScrollBox extends JPanel implements MouseWheelListener {

  private int width;            // Total width of the box
  private int height;           // Total height of the box
  private FileExplorer parent;  // The file explorer that the scroll box is housed in
  private ScrollList list;      // The scroll list containing all items
  private ScrollBar bar;        // The scroll bar used to scroll through the list

  /**
   * Initializes a new scroll box with the given starting directory and its width and height
   * @param  FileExplorer par           File explorer that the scroll list is housed in
   * @param  ImageMarker  editor        Image marker associated with the scroll box
   * @param  Path         imgDir        Path to the image directory to load
   * @param  Path         saveDir       Path to the folder to save the files in
   * @param  int          w             Pixel width of the box
   * @param  int          h             Pixel height of the box
   */
  public ScrollBox(FileExplorer par, ImageMarker editor, Path imgDir, Path saveDir, int w, int h) {
    super(new BorderLayout());
    setBackground(new Color(255, 255, 255));
    this.width = w;
    // Changes the height to round down to fit only full items and no extra space
    this.height = (h/ScrollList.ITEM_HEIGHT)*ScrollList.ITEM_HEIGHT;
    setPreferredSize(new Dimension(this.width, this.height));

    this.parent = par;

    // Initializes the scroll list and bar for the explorer
    this.list = new ScrollList(this, editor, imgDir, saveDir, this.width-ScrollBar.BAR_WIDTH, this.height);
    this.bar = new ScrollBar(this.list, this.height+1);
    // Attaches the scroll bar to the list to link scroll functionality
    this.list.setBar(this.bar);

    // Adds the list to the west of the panel and the bar to the east of the panel
    add(this.list, BorderLayout.WEST);
    add(this.bar, BorderLayout.EAST);

    // Adds the scroll list as mouse listener to the scroll box panel
    addMouseListener(this.list);
    // Adds the panel as a mouse wheel listener to its own panel
    addMouseWheelListener(this);
  }

  /**
   * Triggers the opening of an image to the file explorer
   * @param ListItem selected Currently selected image
   */
  public void triggerImgOpen(ListItem selected) {
    this.parent.openImage(selected);
  }

  /**
   * Resizes the scroll box to fit the defined width and height
   * @param int w New width of the box
   * @param int h New height of the box
   */
  public void resizeBox(int w, int h) {
    this.width = w;
    // Changes the height to round down to fit only full items and no extra space
    this.height = (h/ScrollList.ITEM_HEIGHT)*ScrollList.ITEM_HEIGHT;
    setPreferredSize(new Dimension(this.width, this.height));

    // Resizes the scroll list and scroll bar to match the new dimensions
    this.list.resizeList(this.width-ScrollBar.BAR_WIDTH, this.height);
    this.bar.resizePanel(this.height);
  }

  /**
   * Gets the currently selected item from the scroll list
   * @return Currently selected list item
   */
  public ListItem getSelected() {
    return this.list.getSelected();
  }

  /**
   * Selects and returns the item after the currently selected list item
   * @return Item after the currently selected item in the list
   */
  public ListItem getNextSelected() {
    this.list.selectNext();
    repaint();
    return this.list.getSelected();
  }

  /**
   * Sets the list directory to be the given directory path
   * @param Path path Path for the new directory
   */
  public void setDir(Path path) {
    this.list.setDir(path);
  }

  /**
   * Calls the reload list function of the scroll list to effectively refresh the status of all list items
   */
  public void reloadList() {
    this.list.reloadList();
  }

  /**
   * Adds a scrolling effect to the list and bar when the mouse is over the scroll box
   * @param MouseWheelEvent e Current mouse wheel event
   */
  public void mouseWheelMoved(MouseWheelEvent e) {
    // Scrolls the bar and list 10 pixels in the given direction
    this.bar.scroll(e.getWheelRotation()*10);
  }

  @Override
  public void paintComponent(Graphics g) {
    this.bar.paint(g);
    this.list.paint(g);
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(this.width, this.height);
  }
}
