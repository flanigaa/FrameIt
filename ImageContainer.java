import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.*;
import java.util.LinkedList;
import java.awt.geom.Rectangle2D;
import java.nio.file.*;

/**
 * Class that holds the image within a container as well as keeping and operating the drawn boxes
 */
public class ImageContainer extends JComponent implements MouseListener, MouseMotionListener {

  private int width;                            // Total width of the image container
  private int height;                           // Total height of the image container
  private int imgXPos;                          // Starting X coordinate of the image
  private int imgYPos;                          // Starting Y coordinate of the image
  private int xDif;                             // Difference between the start X of the container and the image X pos
  private int yDif;                             // Difference between the start Y of the container and the image Y pos
  private ListItem openedItem;                  // The list item currently opened in the container
  private BufferedImage orgImg;                 // Buffered image of the unedited image
  private BufferedImage scaledImg;              // Buffered image of the rescaled image
  private float imgScale;                       // Current scale that the image is being displayed at
  private MarkerControlPanel controlPanel;      // Control panel associated with the image
  private LinkedList<Rectangle2D> bboxes;       // List of currently displayed rectangles
  private LinkedList<Rectangle2D> redoList;     // List of rectangles that have been deleted or undone
  private boolean clearedLast;                  // Whether or not the last operation was a clear
  private boolean mousePressed;                 // Whether or not the mouse is currently pressed
  private boolean drawStarted;                  // Whether or not the drawing of a rectangle has been started
  private float rectOrgX;                       // The original starting X coordinate of the rectangle currently being drawn
  private float rectOrgY;                       // The original starting Y coordinate of the rectangle currently being drawn
  private float rectStartX;                     // The adjusted starting X coordinate of the rectangle currently being drawn
  private float rectStartY;                     // The adjusted starting Y coordinate of the rectangle currently being drawn
  private float rectWidth;                      // The width of the rectangle currently being drawn
  private float rectHeight;                     // The height of the rectangle currently being drawn
  public static final int MIN_RECT_AREA = 15;   // The minimum area allowed for a saved rectangle at the current viewing scale

  /**
   * Initializes an empty image container
   * @param  int w             Pixel height of the container
   * @param  int h             Pixel width of the container
   */
  public ImageContainer(int w, int h) {
    this.width = w;
    this.height = h;
    this.imgXPos = 0;
    this.imgYPos = 0;
    this.xDif = 0;
    this.yDif = 0;
    setPreferredSize(new Dimension(this.width, this.height));
    this.openedItem = null;
    this.orgImg = null;
    this.scaledImg = null;
    this.imgScale = 1;
    this.bboxes = new LinkedList<Rectangle2D>();
    this.redoList = new LinkedList<Rectangle2D>();
    this.clearedLast = false;
    this.mousePressed = false;
    this.drawStarted = false;
    this.rectOrgX = 0;
    this.rectOrgY = 0;
    this.rectStartX = 0;
    this.rectStartY = 0;
    this.rectWidth = 0;
    this.rectHeight = 0;
    addMouseListener(this);
    addMouseMotionListener(this);
  }

  /**
   * Resizes the image container to fit the new width and height
   * @param int w New width of the container
   * @param int h New height of the container
   */
  public void resizeContainer(int w, int h) {
    this.width = w;
    this.height = h;
    setPreferredSize(new Dimension(this.width, this.height));
    if (this.orgImg != null) {
      updateRectangleScale();
    }
  }

  /**
   * Updates the scale of the image and rectangles to fit the current size of the conatainer
   */
  public void updateImageScale() {
    if (this.orgImg != null) {
      this.imgScale = rescaleRatio(this.orgImg, this.width, this.height);
      this.scaledImg = rescaleImg(this.orgImg, this.imgScale);
      this.xDif = this.width - this.scaledImg.getWidth();
      this.yDif = this.height - this.scaledImg.getHeight();
      this.imgXPos = this.xDif/2;
      this.imgYPos = this.yDif/2;
    }
  }

  /**
   * Updates the scale of all rectangles to fit the current width and height
   */
  public void updateRectangleScale() {
    if (this.orgImg != null) {
      LinkedList<Rectangle2D> orgBoxes = getRescaledRectangles(this.bboxes);
      LinkedList<Rectangle2D> orgRedos = getRescaledRectangles(this.redoList);
      updateImageScale();
      this.bboxes = getScaledRectangles(orgBoxes);
      this.redoList = getScaledRectangles(orgRedos);
    }
  }

  /**
   * Sets the given list of rectangles to be the current displayed list
   * @param LinkedList<Rectangle2D> rects List of rectangles to set as the displayed set
   */
  public void loadRectangles(LinkedList<Rectangle2D> rects) {
    this.bboxes = getScaledRectangles(rects);
  }

  /**
   * Opens the list item as an image in the image container
   * @param ListItem item List item to open as an image
   */
  public void openImage(ListItem item) {
    Path path = item.getPath();
    if (ImageChecker.isImage(path)) {
      try {
        this.orgImg = ImageIO.read(path.toFile());
        updateImageScale();
        this.bboxes = new LinkedList<Rectangle2D>();
        this.redoList = new LinkedList<Rectangle2D>();
        this.mousePressed = false;
        this.rectStartX = 0;
        this.rectStartY = 0;
        this.rectWidth = 0;
        this.rectHeight = 0;

        if (this.openedItem != null)
          this.openedItem.setOpen(false);

        this.openedItem = item;
        this.openedItem.setOpen(true);

        repaint();
      } catch (Exception e) {
        System.out.println("Error!--" + e);
      }
    }
  }

  /**
   * Rescales the original by the given ratio
   * @param  BufferedImage org           Original image to rescale
   * @param  float         scaleRatio    Ratio to scale the image by
   * @return               New image with the same aspect ratio but scaled by the scale ratio
   */
  public BufferedImage rescaleImg(BufferedImage org, float scaleRatio) {
    int orgW = org.getWidth();
    int orgH = org.getHeight();
    int newW = (int)(orgW*scaleRatio);
    int newH = (int)(orgH*scaleRatio);

    BufferedImage scaled = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = scaled.createGraphics();
    g2d.drawImage(org, 0, 0, newW, newH, null);
    g2d.dispose();

    return scaled;
  }

  /**
   * Finds the correct scale to size the image by in order for it to fit within its given bounds
   * @param  BufferedImage img           Unscaled image to be displayed
   * @param  int           maxX          Max width allowed for the image
   * @param  int           maxY          Max height allowed for the image
   * @return               Returns the scale that the image should be multiplied by
   */
  public float rescaleRatio(BufferedImage img, int maxX, int maxY) {
    int orgX = img.getWidth();
    int orgY = img.getHeight();

    float scalar = 1;
    if (orgX == maxX || orgY == maxY)
      scalar = 1;
    else
      scalar = scale(orgX, maxX, orgY, maxY);

    return scalar;
  }

  /**
   * Returns the percentage that the dimensions of the image should be scaled by.
   * "rescaleRatio()" should be called instead of this method as it uses this method
   * @param  int orgX          Original width of the image
   * @param  int maxX          Max allowed width for the image
   * @param  int orgY          Original height of the image
   * @param  int maxY          Max allowed height for the image
   * @return     Returns the scale to multiply the dimensions of the image by
   */
  public float scale(int orgX, int maxX, int orgY, int maxY) {
    float xPerDif = ((float)maxX)/orgX;
    float yPerDif = ((float)maxY)/orgY;
    // returns the smaller of the two scales
    return Math.min(xPerDif, yPerDif);
  }

  /**
   * Associates the given control panel to the image container
   * @param MarkerControlPanel controls Control panel to associate with the image container
   */
  public void addControls(MarkerControlPanel controls) {
    this.controlPanel = controls;
  }

  /**
   * Undos the last draw related action on the image
   */
  public void undo() {
    // If a clear was the last action, restores all rectangles before the clear
    if (this.clearedLast) {
      this.bboxes = (LinkedList<Rectangle2D>)this.redoList.clone();
      this.redoList.clear();
      this.clearedLast = false;
    // If the last action was not a clear, undos the last drawn rectangle and adds it to the redo list
    } else if (!this.bboxes.isEmpty()) {
      this.redoList.push(this.bboxes.pop());
    }
    repaint();
  }

  /**
   * Redos the last undo or delete
   */
  public void redo() {
    if (!this.redoList.isEmpty() && !this.clearedLast) {
      this.bboxes.push(this.redoList.pop());
      repaint();
    }
  }

  /**
   * Clears all existing drawn rectangles on the image and adds them to the redo list
   */
  public void clear() {
    this.redoList = (LinkedList<Rectangle2D>)this.bboxes.clone();
    this.bboxes.clear();
    this.clearedLast = true;
    repaint();
  }

  /**
   * Rescales all existing rectangles from the scaled image to match the original dimensions of the image
   * The function is used before saving to the file
   * @param  LinkedList<Rectangle2D> rects         The list of rectangles to rescale
   * @return                         List of all rescaled rectangles to match the original image
   */
  public LinkedList<Rectangle2D> getRescaledRectangles(LinkedList<Rectangle2D> rects) {
    LinkedList<Rectangle2D> rescaledRects = new LinkedList<Rectangle2D>();
    for (Rectangle2D rect : rects)
      rescaledRects.add(rescaleRectangle(rect));
    return rescaledRects;
  }

  /**
   * Rescales all existing rectangles from the scaled image to match the original dimensions of the image
   * The function is used before saving to the file
   * @return List of all rescaled rectangles to match the original image
   */
  public LinkedList<Rectangle2D> getRescaledRectangles() {
    LinkedList<Rectangle2D> rescaledRects = new LinkedList<Rectangle2D>();
    for (Rectangle2D rect : this.bboxes)
      rescaledRects.add(rescaleRectangle(rect));
    return rescaledRects;
  }

  /**
   * Rescales a given rectangle to match the dimensions of the original image
   * @param  Rectangle2D org           Original rectangle to rescale
   * @return             Rescaled rectangle
   */
  public Rectangle2D rescaleRectangle(Rectangle2D org) {
    float newStartX = (float)(org.getX()-this.imgXPos)*(1/this.imgScale);
    float newStartY = (float)(org.getY()-this.imgYPos)*(1/this.imgScale);
    float newWidth = (float)org.getWidth()*(1/this.imgScale);
    float newHeight = (float)org.getHeight()*(1/this.imgScale);
    return new Rectangle2D.Float(newStartX, newStartY, newWidth, newHeight);
  }

  /**
   * Rescales the given list of rectangles to match the current scale of the image
   * Used when loading in save file for the image
   * @param  LinkedList<Rectangle> orgRects      List of rectangles to load in and fit to the image
   * @return                       List of rescaled rectangles to match the image
   */
  public LinkedList<Rectangle2D> getScaledRectangles(LinkedList<Rectangle2D> orgRects) {
    LinkedList<Rectangle2D> scaledRects = new LinkedList<Rectangle2D>();
    for (Rectangle2D rect : orgRects)
      scaledRects.add(scaleRectangle(rect));
    return scaledRects;
  }

  /**
   * Rescales a given rectangle to match the dimensions of the scaled image
   * Used when reloading
   * @param  Rectangle org           Rectangle to rescale
   * @return           Rescaled rectangle to match the scaled image
   */
  public Rectangle2D scaleRectangle(Rectangle2D org) {
    float newStartX = (float)(org.getX()*(this.imgScale))+this.imgXPos;
    float newStartY = (float)(org.getY()*(this.imgScale))+this.imgYPos;
    float newWidth = (float)org.getWidth()*(this.imgScale);
    float newHeight = (float)org.getHeight()*(this.imgScale);
    return new Rectangle2D.Float(newStartX, newStartY, newWidth, newHeight);
  }

  /**
   * Whether or not the given coordinates are located within the bounds of the image
   * @param  int x             Current X coordinate to check
   * @param  int y             Current Y coordinate to check
   * @return     Whether or not the coordinates are within the image
   */
  public boolean containsImage(int x, int y) {
    if (this.scaledImg == null)
      return false;
    else if (x >= this.imgXPos && x <= this.imgXPos+this.scaledImg.getWidth() &&
        y >= this.imgYPos && y <= this.imgYPos+this.scaledImg.getHeight())
      return true;
    else
      return false;
  }

  /**
   * Deletes all rectangles within the given coordinates
   * @param int x X coordinate
   * @param int y Y coordinate
   */
  public void deleteOverlapRects(int x, int y) {
    int numOfDeletes = 0;
    for (int i=0; i < this.bboxes.size(); i++) {
      int listIdx = i-numOfDeletes;
      Rectangle2D curRect = this.bboxes.get(listIdx);
      if (curRect.contains(x, y)) {
        this.redoList.push(curRect);
        this.bboxes.remove(listIdx);
        numOfDeletes++;
      }
    }
    if (numOfDeletes > 0)
      repaint();
  }

  /**
   * Returns the unaltered buffered image
   * @return Original image
   */
  public BufferedImage getOrgImg() {
    return this.orgImg;
  }

  /**
   * Whenever the mouse is clicked and dragged the method is called
   * @param MouseEvent e Current mouse event
   */
  public void mouseDragged(MouseEvent e) {
    // If the mouse is currently within the image, updates or starts the drawn rectangle
    if (containsImage(e.getX(), e.getY())) {
      if (this.controlPanel.getMode() != null && this.controlPanel.getMode().equals("draw")) {

        // If a rectangle drawing is not currently started
        if (!drawStarted) {
          this.mousePressed = true;
          this.drawStarted = true;
          this.rectOrgX = e.getX();
          this.rectOrgY = e.getY();
          this.rectStartX = e.getX();
          this.rectStartY = e.getY();
        }

        if (e.getX() < this.rectOrgX) {
          this.rectStartX = e.getX();
          this.rectWidth = this.rectOrgX - e.getX();
        } else
          this.rectWidth = e.getX() - this.rectStartX;

        if (e.getY() < this.rectOrgY) {
          this.rectStartY = e.getY();
          this.rectHeight = this.rectOrgY - e.getY();
        } else
          this.rectHeight = e.getY() - this.rectStartY;
        if (this.clearedLast) {
          this.redoList.clear();
          this.clearedLast = false;
        }
        repaint();
      }
    // If the mouse is not within the image, ends the rectangle currently being drawn
    } else {
      if (this.drawStarted) {
        if (this.controlPanel.getMode() != null && this.controlPanel.getMode().equals("draw")) {
          this.mousePressed = false;
          this.bboxes.push(new Rectangle2D.Float(this.rectStartX, this.rectStartY,
              this.rectWidth, this.rectHeight));
          this.drawStarted = false;
          if (this.clearedLast) {
            this.redoList.clear();
            this.clearedLast = false;
          }
          repaint();
        }
      }
    }
  }

  /**
   * Whenever the mouse is pressed down
   * @param MouseEvent e Current mouse event
   */
  public void mousePressed(MouseEvent e) {
    if (containsImage(e.getX(), e.getY())) {
      // Starts drawing a new rectangle if in draw mode
      if (this.controlPanel.getMode() != null && this.controlPanel.getMode().equals("draw")) {
        this.mousePressed = true;
        this.drawStarted = true;
        this.rectOrgX = e.getX();
        this.rectOrgY = e.getY();
        this.rectStartX = e.getX();
        this.rectStartY = e.getY();
        this.rectWidth = 0;
        this.rectHeight = 0;
        if (this.clearedLast) {
          this.redoList.clear();
          this.clearedLast = false;
        }
      // Runs the deletion cycle if in deletion mode
      } else if (this.controlPanel.getMode() != null && this.controlPanel.getMode().equals("delete")) {
        deleteOverlapRects(e.getX(), e.getY());
      }
      repaint();
    }
  }

  /**
   * Whenever the mouse is released
   * @param MouseEvent e Current mouse event
   */
  public void mouseReleased(MouseEvent e) {
    if (this.drawStarted) {
      if (this.controlPanel.getMode() != null && this.controlPanel.getMode().equals("draw")) {
        this.mousePressed = false;
        if (this.rectWidth*this.rectHeight >= ImageContainer.MIN_RECT_AREA)
          this.bboxes.push(new Rectangle2D.Float(this.rectStartX, this.rectStartY,
              this.rectWidth, this.rectHeight));
        this.drawStarted = false;
        if (this.clearedLast) {
          this.redoList.clear();
          this.clearedLast = false;
        }
        repaint();
      }
    }
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
    Graphics2D g2d = (Graphics2D)g;
    g2d.setColor(new Color(0, 0, 0));
    g2d.fillRect(0, 0, this.width, this.height);
    g2d.drawImage(this.scaledImg, null, this.imgXPos, this.imgYPos);

    if (mousePressed) {
      g2d.setColor(new Color(0, 255, 0));
      g2d.drawRect((int)this.rectStartX-1, (int)this.rectStartY-1, (int)this.rectWidth+2, (int)this.rectHeight+2);
      g2d.setColor(new Color(255, 0, 0));
      g2d.drawRect((int)this.rectStartX, (int)this.rectStartY, (int)this.rectWidth, (int)this.rectHeight);
    }

    for (Rectangle2D rect : this.bboxes) {
      g2d.setColor(new Color(0, 255, 0));
      g2d.drawRect((int)rect.getX()-1, (int)rect.getY()-1, (int)rect.getWidth()+2, (int)rect.getHeight()+2);
      g2d.setColor(new Color(255, 0, 0));
      g2d.drawRect((int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight());
    }
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(this.width, this.height);
  }
}
