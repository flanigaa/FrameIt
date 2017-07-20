import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

/**
 * Class to hold the controls for the image marker/image container
 */
public class MarkerControlPanel extends JPanel {

  private int width;                        // Width of the control panel
  private int height;                       // Height of the entire panel component (not just controls)
  private ImageMarker mainPanel;            // Parent image marker component
  private ImageContainer imageContainer;    // Image container contained in the image marker
  private SingleTogglePanel modePanel;      // The top controls for controlling modes (i.e. draw and delete)
  private LinkedList<AppButton> buttons;    // List of all of the buttons
  private int defaultButtonHeight;          // Default height to use for the buttons
  private Box.Filler bottomFiller;          // Filler component to make sure the controls don't space out clear to the bottom of the window

  /**
   * Creates a control panel with a draw, delete, undo, redo, clear, save, and a save and proceed button
   * @param  ImageMarker    main          Parent image marker that the control panel will control
   * @param  ImageContainer image         Image container that the controls will be used on
   * @param  int            w             Total pixel width of the panel
   * @param  int            h             Total pixel heigh of the panel
   */
  public MarkerControlPanel(ImageMarker main, ImageContainer image, int w, int h) {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.width = w;
    this.height = h;
    this.defaultButtonHeight = this.height/25;
    setPreferredSize(new Dimension(this.width, this.height));

    this.mainPanel = main;
    this.imageContainer = image;

    this.bottomFiller = (Box.Filler)Box.createVerticalGlue();
    this.bottomFiller.changeShape(this.bottomFiller.getMinimumSize(),
        new Dimension(0, this.height/2), this.bottomFiller.getMaximumSize());

    this.modePanel = new SingleTogglePanel(this.width, 3*this.height/30);
    add(this.modePanel);
    this.buttons = new LinkedList<AppButton>();

    addModeButtons();
    addButtons();
    add(this.bottomFiller);
  }

  /**
   * Resizes the control panel and the buttons within it to fit within the new given parameters
   * @param int w New width of the panel
   * @param int h New height of the panel
   */
  public void resizePanel(int w, int h) {
    this.width = w;
    this.height = h;
    this.defaultButtonHeight = this.height/25;
    setPreferredSize(new Dimension(this.width, this.height));
    // Resizes the panel containing the modes
    this.modePanel.resizePanel(this.width, 3*this.height/30);
    // Resizes all of the buttons, including those in the mode panel
    resizeButtons(this.width, this.defaultButtonHeight);
    // Readjusts the size of the filler component at the bottom
    this.bottomFiller.changeShape(this.bottomFiller.getMinimumSize(),
        new Dimension(0, this.height/2), this.bottomFiller.getMaximumSize());
  }

  /**
   * Resizes all of the buttons in the panel, including those in the mode panel
   * @param int w New width of the buttons
   * @param int h New height for each button
   */
  public void resizeButtons(int w, int h) {
    for (AppButton button : this.buttons) {
      button.resizeButton(w, h);
    }
  }

  /**
   * Creates and adds all the mode buttons (draw, delete) to the panel
   */
  public void addModeButtons() {
    ToggleButton drawEasyButton = new ToggleButton(this.modePanel, "Draw Easy Face",
        "draw", 0, new Color(150, 150, 150), this.width-1, this.defaultButtonHeight, null);
    this.modePanel.addButton(drawEasyButton);
    this.buttons.add(drawEasyButton);

    ToggleButton drawHardButton = new ToggleButton(this.modePanel, "Draw Hard Face",
        "draw", 1, new Color(150, 150, 150), this.width-1, this.defaultButtonHeight, null);
    this.modePanel.addButton(drawHardButton);
    this.buttons.add(drawHardButton);

    ToggleButton deleteButton = new ToggleButton(this.modePanel, "Delete", "delete",
        -1, new Color(150, 150, 150), this.width-1, this.defaultButtonHeight, null);
    this.modePanel.addButton(deleteButton);
    this.buttons.add(deleteButton);
  }

  /**
   * Creates and adds the five function buttons to the control panel
   */
  public void addButtons() {
    AppButton undoButton = new AppButton("Undo", this.width-1, this.defaultButtonHeight, new Runnable() {
        @Override
        public void run() {
          imageContainer.undo();
        }});
    add(undoButton);
    this.buttons.add(undoButton);

    AppButton redoButton = new AppButton("Redo", this.width-1, this.defaultButtonHeight, new Runnable() {
        @Override
        public void run() {
          imageContainer.redo();
        }});
    add(redoButton);
    this.buttons.add(redoButton);

    AppButton clearButton = new AppButton("Clear", this.width-1, this.defaultButtonHeight, new Runnable() {
        @Override
        public void run() {
          imageContainer.clear();
        }});
    add(clearButton);
    this.buttons.add(clearButton);

    AppButton saveButton = new AppButton("Save", this.width-1, this.defaultButtonHeight, new Runnable() {
        @Override
        public void run() {
          mainPanel.save();
        }});
    add(saveButton);
    this.buttons.add(saveButton);

    AppButton saveProceedButton = new AppButton("Save and Proceed", this.width-1, this.defaultButtonHeight, new Runnable() {
        @Override
        public void run() {
          mainPanel.save();
          mainPanel.getFileExplorer().openNext();
        }});
    add(saveProceedButton);
    this.buttons.add(saveProceedButton);
  }

  /**
   * Returns the currently selected mode in the mode panel
   * @return Currently selected mode
   */
  public String getMode() {
    ToggleButton toggled = this.modePanel.getToggled();
    if (toggled != null)
      return toggled.getMode();
    else
      return null;
  }

  public int getDrawType() throws Exception {
    ToggleButton toggled = this.modePanel.getToggled();
    if (toggled != null) {
      if (!toggled.getMode().equals("draw")) {
        throw new Exception("Toggled button has no valid type!");
      } else {
        return toggled.getType();
      }
    } else
      return -1;
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(this.width, this.height);
  }
}
