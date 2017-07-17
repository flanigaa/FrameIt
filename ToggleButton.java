import java.awt.*;
import java.awt.event.*;

/**
 * Extends the functionality of an app button to be togglable rather than just executable
 */
public class ToggleButton extends AppButton {

  private SingleTogglePanel togglePanel;  // Toggle panel that the button is in
  private boolean toggled;                // Whether or not the button is currently toggled
  private Color toggleColor;              // Color of the button when toggled
  private String mode;                    // String representing the "mode" that the button initializes

  public ToggleButton(SingleTogglePanel panel, String name, String mode, Color toggle, int w, int h, Runnable fun) {
    super(name, w, h, fun);
    this.mode = mode;
    this.togglePanel = panel;
    this.toggled = false;
    this.toggleColor = toggle;
  }

  /**
   * Returns the mode string of the button
   * @return Mode of the button
   */
  public String getMode() {
    return this.mode;
  }

  /**
   * Returns whether or not the button is currently toggled
   * @return Whether or not the button is currently toggled
   */
  public boolean getToggled() {
    return this.toggled;
  }

  /**
   * Toggles the button to its opposite. If it was toggled, it becomes untoggled, and vice-versa
   */
  public void toggle() {
    this.toggled = !this.toggled;
    if (this.toggled)
      this.color = this.toggleColor;
    else
      this.color = this.defaultColor;
    repaint();
  }

  /**
   * Overrides the default click operation of the app button to toggle the button when the mouse is clicked over it
   * @param MouseEvent e Current mouse event
   */
  @Override
  public void mouseClicked(MouseEvent e) {
    if (containsButton(e.getX(), e.getY())) {
      if (this.togglePanel != null)
        this.togglePanel.updateToggle(this);
      else
        toggle();

      if (this.func != null)
        this.func.run();
      repaint();
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }
}
