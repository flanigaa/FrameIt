import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

/**
 * Class to represent a panel where only one option may be active at once.
 * It is used to control the current mode in the control panel as only draw or delete can be active and not both at once.
 * Once one mode is selected, if there is already a selected mode, it will become deselected
 */
public class SingleTogglePanel extends JPanel {

  private int width;                          // Width of the toggle panel
  private int height;                         // Height of the toggle panel
  private LinkedList<ToggleButton> buttons;   // List of the toggle buttons in the panel
  private ToggleButton toggled;               // Stores the currently selected/toggled button

  /**
   * Creates a new toggle panel with no buttons. Buttons can then be added through the addButton() method
   * @param  int w             Pixel width of the toggle panel
   * @param  int h             Pixel height of the toggle panel
   */
  public SingleTogglePanel(int w, int h) {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.width = w;
    this.height = h;
    setPreferredSize(new Dimension(this.width, this.height));
    this.buttons = new LinkedList<ToggleButton>();
  }

  /**
   * Resizes the panel itself to fit the new size parameters. Does not resize the buttons within it
   * @param int w New width of the toggle panel
   * @param int h New height of the toggle panel
   */
  public void resizePanel(int w, int h) {
    this.width = w;
    this.height = h;
    setPreferredSize(new Dimension(this.width, this.height));
  }

  /**
   * Adds a new toggle button to the panel
   * @param ToggleButton button New toggle button to add
   */
  public void addButton(ToggleButton button) {
    this.buttons.add(button);
    add(button);
    if (this.toggled == null) {
      updateToggle(button);
    }
    repaint();
  }

  /**
   * Updates the currently toggled button to the given button and untoggles the previously toggled button if there is one
   * @param ToggleButton toggle New button to toggle
   */
  public void updateToggle(ToggleButton toggle) {
    // Untoggles the currently toggled button if it is not the same button as the given
    if (this.toggled != null) {
      if (this.toggled == toggle )
        return;
      else
        this.toggled.toggle();
    }
    // Sets the currently toggled button to the given one and then actually toggles it
    this.toggled = toggle;
    this.toggled.toggle();
  }

  /**
   * Returns the currently selected button
   * @return Currently selected toggle button
   */
  public ToggleButton getToggled() {
    return this.toggled;
  }
}
