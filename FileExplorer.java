import java.awt.*;
import javax.swing.*;
import java.nio.file.*;

/**
 * Class that allows the navigation of the image directory and all image files within
 */
public class FileExplorer extends JPanel {

  private int width;              // Total width of the file explorer
  private int height;             // Total height of the file explorer
  private int openButtonHeight;   // Height of the open button
  private ScrollBox scrollBox;    // The scroll box containing the list of all files and scroll bar
  private AppButton openButton;   // The open button at the top of the file explorer
  private ImageMarker imgEditor;  // The image editor of the application (used in class to send image paths to open)

  /**
   * Initializes a file explorer with the given size and starting from the given directory
   * @param  ImageMarker editor        Image editor associated with the file explorer
   * @param  Path        imgDir        Path to the outermost image directory
   * @param  Path        saveDir       Path to the folder to save files in
   * @param  int         w             Pixel width to build the explorer with
   * @param  int         h             Pixel height to build the explorer with
   */
  public FileExplorer(ImageMarker editor, Path imgDir, Path saveDir, int w, int h) {
    super(new BorderLayout());
    this.width = w;
    this.height = h;
    setPreferredSize(new Dimension(this.width, this.height));
    this.openButtonHeight = h/25;

    this.imgEditor = editor;

    // Initializes the scroll box and adds it to the file explorer JPanel
    this.scrollBox = new ScrollBox(this, this.imgEditor, imgDir, saveDir, this.width, this.height-this.openButtonHeight);
    add(scrollBox, BorderLayout.CENTER);

    // Initializes the open button with its function and adds it to the JPanel
    this.openButton = new AppButton("Open", this.width, this.openButtonHeight, new Runnable() {
      @Override
      public void run() {
        ListItem selected = scrollBox.getSelected();
        if (selected == null)
          return;
        if (selected.isDirectory()) {
          scrollBox.setDir(selected.getPath());
        } else {
          openImage(selected);
        }
        scrollBox.repaint();
      }
    });
    add(openButton, BorderLayout.NORTH);
  }

  /**
   * Opens the given ListItem as an image in the image editor
   * @param ListItem selected The currently selected item in the scroll list
   */
  public void openImage(ListItem selected) {
    // If the image has not been completed, it will open normally
    if (!selected.isCompleted())
      imgEditor.openImage(selected);
    // Otherwise, if it has been previously completed, it will load its save data
    else
      imgEditor.openCompletedImage(selected);
  }

  /**
   * Resizes the file explorer and all of its inner components to match the given dimensions
   * @param int w New pixel width of the panel
   * @param int h New pixel height of the panel
   */
  public void resizeExplorer(int w, int h) {
    this.width = w;
    this.height = h;
    this.openButtonHeight = h/25;
    setPreferredSize(new Dimension(this.width, this.height));

    // Resizes the inner components
    this.scrollBox.resizeBox(this.width, this.height-this.openButtonHeight);
    this.openButton.resizeButton(this.width, this.openButtonHeight);

    repaint();
  }

  /**
   * Opens the list item that is after the currently selected item as an image
   * in the image editor
   */
  public void openNext() {
    ListItem selected = this.scrollBox.getNextSelected();
    if (selected == null)
      return;

    if (!selected.isCompleted())
      imgEditor.openImage(selected);
    else
      imgEditor.openCompletedImage(selected);
    repaint();
  }

  /**
   * Passes the reload action to the contained list. This is used after saving an
   * image and then updates the visual status of all items in the list
   */
  public void reloadList() {
    this.scrollBox.reloadList();
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(this.width, this.height);
  }
}
