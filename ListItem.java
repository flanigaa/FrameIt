import java.nio.file.Path;
import javax.swing.JComponent;
import java.awt.*;

/**
 * Class used to represent a single item within a list
 */
public class ListItem extends JComponent implements Comparable<ListItem> {

  protected Path path;          // Path to the item
  protected String name;        // Display name of the path (usually the end of the path)
  protected ListItem next;      // The next item in the list (used for easy selection of next item)
  protected boolean isDir;      // Whether or not the item is a directory
  protected boolean open;       // Whether or not the item is currently open in the image marker
  protected boolean selected;   // Whether or not the item is currently selected
  protected boolean completed;  // Whether or not the item has been previously completed

  // If the item is currently displayed, these dimension attributes will be used to draw it in the correct location
  protected int xPos;           // Starting X coordinate
  protected int yPos;           // Starting Y coordinate
  protected int width;          // Total width of the item
  protected int height;         // Total height of the item

  /**
   * Initializes a new list item to represent an item within a directory
   * @param  Path    path          Path to the item
   * @param  String  aName         Name to display and represent the item
   * @param  boolean dir           Whether or not the item is a directory
   * @param  boolean comp          Whether or not the item has been completed
   */
  public ListItem(Path path, String aName, boolean dir, boolean comp) {
    this.path = path;
    this.name = aName;
    this.next = null;
    this.isDir = dir;
    this.open = false;
    this.selected = false;
    this.completed = comp;
  }

  /**
   * Sets the dimensions to use when or if drawing the item
   * @param int x Starting X coordinate
   * @param int y Starting Y coordinate
   * @param int w Total width of the item
   * @param int h Total height of the item
   */
  public void setDimensions(int x, int y, int w, int h) {
    this.xPos = x;
    this.yPos = y;
    this.width = w;
    this.height = h;
  }

  /**
   * Returns whether or not the item is a directory
   * @return Whether or not the itme is a directory
   */
  public boolean isDirectory() {
    return this.isDir;
  }

  /**
   * Returns whether or not the item has been completed
   * @return Whether or not the itme has been completed
   */
  public boolean isCompleted() {
    return this.completed;
  }

  /**
   * Returns whether or not the item is currently selected
   * @return Whether or not the itme is currently selected
   */
  public boolean isSelected() {
    return this.selected;
  }

  /**
   * Returns whether or not the item is currently open in the image marker
   * @return Whether or not the itme is currently open
   */
  public boolean isOpen() {
    return this.open;
  }

  /**
   * Sets the next item in the list to the item
   * @param ListItem item Next item in the list
   */
  public void setNext(ListItem item) {
    this.next = item;
  }

  /**
   * Sets the open status of the item
   * @param boolean set Whether or not the item is currently open
   */
  public void setOpen(boolean set) {
    this.open = set;
    repaint();
  }

  /**
   * Sets the selection status of the item
   * @param boolean set Whether or not the item is currently selected
   */
  public void setSelected(boolean set) {
    this.selected = set;
    repaint();
  }

  /**
   * [setCompleted description]
   * @param boolean set [description]
   */
  public void setCompleted(boolean set) {
    this.completed = set;
    repaint();
  }

  /**
   * Returns the Path to the item
   * @return Path to the item
   */
	public Path getPath() {
		return this.path;
	}

  /**
   * Returns the next item in the list
   * @return Next item in the list
   */
  public ListItem getNext() {
    return this.next;
  }

  public String toString() {
    return String.format("%s at %s -- is dir: %b", this.name,
        this.path.toString(), this.isDir);
  }

  public int compareTo(ListItem o2) {
    return this.name.compareTo(o2.name);
  }

  @Override
  public void paintComponent(Graphics g) {
    // blue background if selected
    if (this.selected)
      g.setColor(new Color(70, 155, 255));
    // else purple if open
    else if (this.open)
      g.setColor(new Color(191, 110, 254));
    // else green if completed
    else if (this.completed)
      g.setColor(new Color(100, 255, 100));
    // else red
    else
      g.setColor(new Color(255, 75, 75));

    g.fillRect(this.xPos, this.yPos, this.width, this.height);
    g.setColor(new Color(0, 0, 0));
    g.drawRect(this.xPos, this.yPos, this.width, this.height);
    Font font = g.getFont();
    FontMetrics metrics = g.getFontMetrics( font );
    while (metrics.stringWidth(this.name) > this.width-10) {
      font = new Font(font.getName(), font.getStyle(),
          (int)(font.getSize()*(3.0/4)));
      metrics = g.getFontMetrics(font);
    }
    g.setFont( font );
    int centerY = this.yPos + ((this.height - metrics.getHeight())/2) + metrics.getAscent();
    g.drawString(this.name, this.xPos+10, centerY);
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(500-ScrollBar.BAR_WIDTH, ScrollList.ITEM_HEIGHT);
  }
}
