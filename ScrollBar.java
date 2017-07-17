import javax.swing.JComponent;
import java.awt.*;
import java.awt.event.*;

public class ScrollBar extends JComponent implements MouseListener, MouseMotionListener {

  private ScrollList list;                  // Scroll list controlled by the scroll bar
  private int height;                       // Total height of the scroll bar panel
  private float heightRatio;                // Fractional size of the bar, representative of the viewing space
  private float curPos;                     // Current Y position of the middle of the bar
  private float defaultPosition;            // Default Y position when the bar is at the top of the panel
  private boolean onBar;                    // Whether the mouse is still held down after clicking the bar
  private float orgClickDif;                // Distance from the center of the bar to where the bar was clicked (relative position)
  public static final int BAR_WIDTH = 11;   // Total pixel width of the bar

  /**
   * Initializes a scroll bar that links to a scroll list and allows it to be scrolled using a bar
   * @param  ScrollList aList         Scroll list to link the bar to
   * @param  int        h             Pixel height of the bar panel
   */
  public ScrollBar(ScrollList aList, int h) {
    this.list = aList;
    this.height = h;
    setPreferredSize(new Dimension(ScrollBar.BAR_WIDTH, this.height));
    this.heightRatio = 0;
    this.defaultPosition = 0;
    this.curPos = this.defaultPosition;
    this.onBar = false;
    this.orgClickDif = 0;
    addMouseListener(this);
    addMouseMotionListener(this);
  }

  /**
   * Resize the scroll bar and panel with respect to the new, given height
   * @param int h New pixel height to give the entire scroll panel
   */
  public void resizePanel(int h) {
    float barProgress = (this.curPos-barHeight())/this.height;
    this.height = h;
    setPreferredSize(new Dimension(ScrollBar.BAR_WIDTH, this.height));
    // Moves the bar to the correct position
    setCurPos(barHeight()+this.height*barProgress);
  }

  /**
   * Resets the bar to a default set of values
   */
  public void resetBar() {
    setHeightRatio(0);
    setCurPos(this.defaultPosition);
  }

  /**
   * Checks whether or not the coordinates are contained in the bar's current position
   * @param  int x             X coordinate
   * @param  int y             Y coordinate
   * @return     Whether or not the coordinates are in the bar
   */
  public boolean containsBar(int x, int y) {
    if (x >= 0 && x <= ScrollBar.BAR_WIDTH &&
        y >= this.curPos-barHeight()/2 && y <= this.curPos+barHeight()/2)
      return true;
    else
      return false;
  }

  /**
   * Calculates and returns the current height of the bar
   * @return Current height of the bar
   */
  public float barHeight() {
    return this.height*this.heightRatio;
  }

  /**
   * Calculates and returns the fraction of the list that has been scrolled through
   * @return Fraction of the list that has been scrolled through
   */
  public float getListFrac() {
    return (this.curPos-barHeight()/2)/this.height;
  }

  /**
   * Scrolls the bar toward the position by the given pixel amount.
   * @param int pos    Y coordinate to scroll toward
   * @param int amount Pixel amount to scroll toward the pos. Value should be positive
   */
  public void scrollToward(int pos, int amount) {
    if (pos > this.curPos)
      setCurPos(this.curPos+amount);
    else if (pos < this.curPos)
      setCurPos(this.curPos-amount);
  }

  /**
   * Scrolls the bar by the given pixel amount
   * @param int amount Pixel amount to scroll. Should be positive to scroll down and negative to scroll up
   */
  public void scroll(int amount) {
    setCurPos(this.curPos+amount);
  }

  /**
   * Sets the ratio of the total height of the panel that should be covered by the bar.
   * As more items are added to the list, the bar ratio should decrease along with the actual size.
   * @param float heightRatio New percentage of the scroll bar panel that the bar covers
   */
	public void setHeightRatio(float heightRatio) {
		this.heightRatio = heightRatio;
    setCurPos(barHeight()/2);
    repaint();
	}

  /**
   * Moves the middle of the bar to the new given Y position and scrolls the associated list to its new position
   * If a value is too small, it is changed to the smallest possible value.
   * If a value is too large, it is changed to the largest possible value.
   * @param float curPos Y coordinate to place the middle of the bar
   */
	public void setCurPos(float curPos) {
    if (curPos < barHeight()/2)
      this.curPos = barHeight()/2;
    else if (curPos > this.height-barHeight()/2)
      this.curPos = this.height-barHeight()/2;
    else
		  this.curPos = curPos;
    repaint();
    this.list.scroll(getListFrac());
	}

  /**
   * If the bar is still "pressed" then when the mouse is dragged, the bar will move to the mouse's y position
   * @param MouseEvent e Current mouse event
   */
  public void mouseDragged(MouseEvent e) {
    if (this.onBar)
      setCurPos(e.getY() + this.orgClickDif);
  }

  /**
   * If the mouse is pressed while on the bar, the bar will become "pressed" and will be able to be dragged until the mouse is released
   * If the mouse is not over the bar, but still in the panel, the bar will move 10 pixels toward the mouse position
   * @param MouseEvent e Current mouse event
   */
  public void mousePressed(MouseEvent e) {
    if (!containsBar(e.getX(), e.getY()))
      scrollToward(e.getY(), 10);
    else {
      this.onBar = true;
      this.orgClickDif = this.curPos - e.getY();
    }
  }

  /**
   * Resets the mouse values for the bar when the mouse is released
   * @param MouseEvent e Current mouse event
   */
  public void mouseReleased(MouseEvent e) {
    this.onBar = false;
    this.orgClickDif = 0;
  }

  public void mouseMoved(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mouseClicked(MouseEvent e) {
  }

  @Override
  public void paintComponent(Graphics g) {
    g.setColor(new Color(255, 255, 255));
    g.fillRect(0, 0, ScrollBar.BAR_WIDTH, this.height);
    g.setColor(new Color(0, 0, 0));
    g.drawRect(0, 0, ScrollBar.BAR_WIDTH-1, this.height-1);

    if (barHeight() > 0) {
      g.setColor(new Color(190, 190, 190));
      g.fillRect(0, (int)(this.curPos-barHeight()/2), ScrollBar.BAR_WIDTH, (int)barHeight());
      g.setColor(new Color(0, 0, 0));
      g.drawRect(0, (int)(this.curPos-barHeight()/2), ScrollBar.BAR_WIDTH-1, (int)barHeight());
    }
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(ScrollBar.BAR_WIDTH, this.height);
  }
}
