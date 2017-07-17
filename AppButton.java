import javax.swing.JComponent;
import java.awt.*;
import java.awt.event.*;

/**
 * Class that represents a functional button. Once the button is clicked, the function associated with the button will then be executed
 */
public class AppButton extends JComponent implements MouseListener {

  protected String name;    // Text to display within the button
  protected int width;      // Width of the button
  protected int height;     // Height of the button
  protected Runnable func;  // Function to execute when the button is clicked
  protected Color color;    // Background color of the button
  protected Color defaultColor = new Color(255, 255, 255);  // Default color value for the button background
  protected Color pressedColor = new Color(150, 150, 150);  // Default color to use while the button is pressed

  /**
   * Creates a new button that executes a function when pressed
   * @param  String   name          Text to display within the button
   * @param  int      w             Pixel width of the button
   * @param  int      h             Pixel height of the button
   * @param  Runnable fun           Function to execute when the button is clicked
   */
  public AppButton(String name, int w, int h, Runnable fun) {
    this.name = name;
    this.width = w;
    this.height = h;
    this.func = fun;
    this.color = this.defaultColor;
    setPreferredSize(new Dimension(this.width, this.height));
    addMouseListener(this);
  }

  /**
   * Runs the function associated with the button
   */
  public void run() {
    this.func.run();
  }

  /**
   * Resizes the button to the given parameters
   * @param int w New width of the button
   * @param int h New height of the button
   */
  public void resizeButton(int w, int h) {
    this.width = w;
    this.height = h;
    setPreferredSize(new Dimension(this.width, this.height));
  }

  /**
   * Checks whether or not the given coordinates are within the bounds of the button
   * @param  int x             X position to check
   * @param  int y             Y position to check
   * @return     [description]
   */
  public boolean containsButton(int x, int y) {
    if (x >= 0 && x <= this.width && y >= 0 && y <= this.height)
      return true;
    else
      return false;
  }

  /**
   * When the mouse is pressed within the button, changes its displayed color to the pressed color (gray)
   * @param MouseEvent e Current mouse event
   */
  public void mousePressed(MouseEvent e) {
    if (containsButton(e.getX(), e.getY())) {
      this.color = this.pressedColor;
      repaint();
    }
  }

  /**
   * When the mouse is released, resets the color of the button to the default color
   * @param MouseEvent e Current mouse event
   */
  public void mouseReleased(MouseEvent e) {
    this.color = this.defaultColor;
    repaint();
  }

  /**
   * When the mouse is clicked over the button, the associated function is executed
   * @param MouseEvent e Current mouse event
   */
  public void mouseClicked(MouseEvent e) {
    if (containsButton(e.getX(), e.getY()))
      this.func.run();
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  @Override
  public void paintComponent(Graphics g) {
    g.setColor(this.color);
    g.fillRect(0, 0, this.width, this.height);
    g.setColor(new Color(0, 0, 0 ));
    g.drawRect(0, 0, this.width-1, this.height);
    Font font = g.getFont();
    FontMetrics metrics = g.getFontMetrics(font);
    while (metrics.stringWidth(this.name) > this.width) {
      font = new Font(font.getName(), font.getStyle(), (int)(font.getSize()*((float)3/4)));
      metrics = g.getFontMetrics(font);
    }
    g.setFont( font );
    int centerX = 0 + this.width/2 - metrics.stringWidth( this.name )/2;
    int centerY = 0 + ((this.height - metrics.getHeight())/2) + metrics.getAscent();
    g.drawString(this.name, centerX, centerY);
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(this.width, this.height);
  }
}
