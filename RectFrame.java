import java.awt.geom.Rectangle2D;

/**
 * Extends the Rectangle2D.Double class but includes a type number
 */
public class RectFrame extends Rectangle2D.Double{
  private int type; // Used to represent the possible different rectangle frame types

  public RectFrame(double x, double y, double w, double h, int aType) {
    setRect(x, y, w, h);
    this.type = aType;
  }

  /**
   * Returns the given type of the rectangle
   * @return Type of the rectangle
   */
  public int getType() {
    return this.type;
  }
}
