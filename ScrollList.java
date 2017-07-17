import javax.swing.JComponent;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.awt.event.*;
import java.nio.file.*;
import java.util.Iterator;
import java.io.File;

/**
 * Class to represent a list of items in a directory
 */
public class ScrollList extends JComponent implements MouseListener {

  private ArrayList<ListItem> items;            // Array list of all list items
  private LinkedList<ListItem> displayedItems;  // Linked list of all currently displayed items
  private ScrollBox parent;                     // Scroll box that the list is nested in
  private ImageMarker imgEditor;                // Image marker that images open in
  private ScrollBar bar;                        // Scroll bar that is associated with the list
  private int width;                            // Total width of the list
  private int height;                           // Total height of the list
  private int curStartIdx;                      // Index in the items list of the first displayed item
  private Path orgDir;                          // Original directory of the list
  private Path curDir;                          // Currently active directory of the list
  private Path saveFolder;                      // Folder to save the files to
  private ListItem selected;                    // Currently selected list item
  private int maxItems;                         // Max number of displayed items
  public static final int ITEM_HEIGHT = 30;     // Default height of each item in the list

  /**
   * Initializes a new scroll list for the given starting directory
   * @param  ScrollBox par           Scroll box containing the list
   * @param  Path      imgDir        Directory to start from
   * @param  Path      saveDir       Directory to save files in
   * @param  int       w             Pixel width of the list
   * @param  int       h             Pixel height of the list
   */
  public ScrollList(ScrollBox par, ImageMarker editor, Path imgDir, Path saveDir, int w, int h) {
    this.items = new ArrayList<ListItem>();
    this.displayedItems = new LinkedList<ListItem>();
    this.parent = par;
    this.imgEditor = editor;
    this.bar = null;
    this.width = w;
    this.height = h;
    setPreferredSize(new Dimension(this.width, this.height));
    this.curStartIdx = 0;
    this.orgDir = imgDir;
    this.curDir = this.orgDir;
    this.saveFolder = saveDir;
    this.selected = null;
    this.maxItems = this.height/ScrollList.ITEM_HEIGHT;

    // Fills the list from the current directory
    fillList();

    // Adds itself as a mouse listener
    addMouseListener(this);
  }

  /**
   * Resizes the list based on the new given dimensions
   * @param int w New width of the list
   * @param int h New height of the list
   */
  public void resizeList(int w, int h) {
    this.width = w;
    this.height = h;
    setPreferredSize(new Dimension(this.width, this.height));
    this.maxItems = this.height/ScrollList.ITEM_HEIGHT;
    // Adds or removes any needed or extra items from the display list
    updateDisplayedSize();
  }

  /**
   * Updates the size of the currently displayed items in the list to fit the new
   * number of max allowed items
   */
  public void updateDisplayedSize() {
    // Shrinks the number of displayed items until it is equal to the max items
    if (this.displayedItems.size() > this.maxItems) {
      if (this.maxItems <= 0)
        return;
      while (this.displayedItems.size() > this.maxItems)
        this.displayedItems.removeLast();

    /*
      If the list doesn't have enough items, adds until there are no more
      undisplayed items or the max number is met
     */
    } else if (this.displayedItems.size() < this.maxItems) {
      while (this.displayedItems.size() < this.maxItems) {
        if (this.items.isEmpty())
          return;
        else {
          ListItem nextItem = this.displayedItems.get(this.displayedItems.size()-1).getNext();
          if (nextItem != null)
            this.displayedItems.add(nextItem);
          else
              break;
        }
      }
      // If all items are ever display, then the scroll bar is reset and disappears
      if (this.displayedItems.size() == this.items.size())
        this.bar.resetBar();
    }
    // Updates the scroll bar to match the list
    if (this.bar != null && this.items.size() > this.maxItems) {
      this.bar.setHeightRatio(((float)this.displayedItems.size())/this.items.size());
    }
  }

  /**
   * Changes the current loaded directory of the list
   * @param Path dirPath Path to the new directory
   */
  public void setDir(Path dirPath) {
    // Enters the new directory as long as it is not past the starting directory
    if (!dirPath.equals(this.orgDir.getParent())) {
      this.curDir = dirPath;
      // Refills the list with the new directory
      fillList();
    }
  }

  /**
   * Fills the item list with all directories and images from the current directory
   */
  public void fillList() {
    try {
      Iterator<Path> pathList = Files.newDirectoryStream(this.curDir).iterator();

      // Clears the lists and resets the scroll bar
      clearItems();
      if (this.bar != null) {
        this.bar.resetBar();
      }

      // Adds the first item of the list as a backtrack directory that directs to the parent directory
      this.items.add(new ListItem(this.curDir.getParent(), "..", true, false));

      // For each file or directory in the current directory, attempts to make a list item and add it
      while (pathList.hasNext()) {
        Path nextPath = pathList.next();
        boolean isDir = nextPath.toFile().isDirectory();
        boolean completed = false;

        // Adds the item as a directory if it is one
        if (isDir) {
          // Checks for the total number and completed number of IMAGES in the directory
          CompletionData completionData = checkDirCompletion(nextPath);
          // If the directory has no IMAGES then it is not displayed in the list
          if (completionData.getFileNum() > 0)
            this.items.add(new ListDirItem(nextPath, nextPath.getFileName().toString(),
                completionData));
        // If the item is not a directory, checks for its completion and then adds it to the list
        } else if (ImageChecker.isImage(nextPath)) {
          Path savePath = ScrollList.convertToSavePath(this.orgDir, this.saveFolder, nextPath);
          completed = ScrollList.checkFileCompletion(savePath);
          ListItem item = new ListItem(nextPath, nextPath.getFileName().toString(),
              isDir, completed);
          if (this.imgEditor.isOpen(nextPath))
            item.setOpen(true);
          this.items.add(item);
        }
      }

      // Alphanumerically sorts all items in the list
      this.items.sort(null);
      chainItems();

      // Fills the displayed items list
      fillDisplayedItems();

    } catch (Exception e) {
      System.out.println("Error when loading current directory for scroll list! " + e);
    } finally {
      // Repaints the component at the end of the loading of the list
      repaint();
    }
  }

  /**
   * Fills the displayed list with the max number of possible items
   */
  public void fillDisplayedItems() {
    // Fills the displayed items list until it is full
    for (ListItem item : this.items) {
      if (this.displayedItems.size() < this.maxItems)
        this.displayedItems.add(item);
      else
        break;
    }

    // If there is an associated scroll bar, adjusts its size to match the total number of items in the list
    if (this.bar != null && this.items.size() > this.maxItems) {
      this.bar.setHeightRatio(((float)this.displayedItems.size())/this.items.size());
    }

    this.parent.repaint();
  }

  public void chainItems() {
    if (this.items.isEmpty())
      return;

    ListItem current = this.items.get(0);
    ListItem next = null;
    for (int i=1; i < this.items.size(); i++) {
      next = this.items.get(i);
      current.setNext(next);
      current = next;
    }
  }

  /**
   * Reloads the list and revalidates the statuses of all items currently in the list
   */
  public void reloadList() {
    for (ListItem item : this.items) {
      if (!item.isDirectory()) {
        Path savePath = ScrollList.convertToSavePath(this.orgDir, this.saveFolder, item.getPath());
        boolean completed = ScrollList.checkFileCompletion(savePath);
        item.setCompleted(completed);
      } else if (item instanceof ListDirItem){
        ListDirItem dirItem = (ListDirItem)item;
        CompletionData completionData = checkDirCompletion(item.getPath());
        dirItem.updateCompletionData(completionData);
      }
    }
    repaint();
  }

  /**
   * Static method used to generate the save path for a single image's results
   * @param  String commonAncestor Closest common ancestor folder with the image path and the save folder
   * @param  String saveFolder     Outer save directory to save the image results
   * @param  String imgPath        Path to the image
   * @return        Returns a string path to the same subdirectories that the image is in
   *                        within the save folder
   *                For example, if the image has the path of "workingDirectory/images/Folder1/Folder2/imgPath.jpg"
   *                        the method will return a string of "workingDirectory/saveFolder/Folder1/Folder2/imgPath.txt"
   */
  public static Path convertToSavePath(Path commonAncestor, Path saveFolder, Path imgPath) {
    Path endImgPath = commonAncestor.relativize(imgPath);
    Path savePath = saveFolder.resolve(endImgPath).getParent();

    String imgName = endImgPath.getFileName().toString();
    String imgNameNoExt = imgName.substring(0, imgName.lastIndexOf('.'));
    savePath = savePath.resolve(imgNameNoExt+".txt");
    return savePath;
  }

  /**
   * Checks to see if the save file exists
   * @param  Path   savePath      Path where the save file should be stored
   * @return        Whether or not the save file exists
   */
  public static boolean checkFileCompletion(Path savePath) {
    try {
      File saveFile = savePath.toFile();
      if (saveFile.exists())
        return true;
      else
        return false;
    } catch (Exception e) {
      System.out.println("Error when checking file completion! " + e);
      return false;
    }
  }

  /**
   * Recursively checks the completion of a directory
   * @param  Path dirPath       Path for the directory
   * @return      Completion data for the directory
   */
  public CompletionData checkDirCompletion(Path dirPath) {
    int completedFileCount = 0;
    int fileCount = 0;
    try {
      Iterator<Path> pathList = Files.newDirectoryStream(dirPath).iterator();

      // For each file and directory within the current directory
      while (pathList.hasNext()) {
        Path nextPath = pathList.next();

        // If the path is a directory, recursively checks completion
        if (nextPath.toFile().isDirectory()) {
          CompletionData curData = checkDirCompletion(nextPath);
          completedFileCount += curData.getCompleteNum();
          fileCount += curData.getFileNum();

        // If the path is not a directory
        } else {
          // Only collects completion information for image files
          if (ImageChecker.isImage(nextPath)) {
            Path savePath = ScrollList.convertToSavePath(this.orgDir, this.saveFolder, nextPath);
            if (ScrollList.checkFileCompletion(savePath))
              completedFileCount++;
            fileCount++;
          }
        }

      }
    } catch (Exception e) {
      System.out.println("Error when checking directory completion! " + e);
    }

    return new CompletionData(completedFileCount, fileCount);
  }

  /**
   * Clears all items in both item lists
   */
  public void clearItems() {
    this.items = new ArrayList<ListItem>();
    this.displayedItems = new LinkedList<ListItem>();
  }

  /**
   * Whether or not there are more items in the list than the max amount of displayed items
   * @return Whether or not the list can be scrolled through
   */
  public boolean isScrollable() {
    return this.items.size() > this.maxItems;
  }

  /**
   * Scrolls the list to the progress percentage
   * For example, if the scroll bar is 50% down the panel, the list should start
   *    halfway through the number of items
   * @param float frac Progress fraction of the list
   */
  public void scroll(float frac) {
    // Calculates the correct index to start the list at
    int startIdx = (int)(this.items.size()*frac);

    // If the starting index is the same, do nothing
    if (startIdx == curStartIdx)
      return;

    // Clears all items from the list
    this.removeAll();
    this.displayedItems.clear();

    // Creates an iterator through the list of items at the starting at the new start index
    ListIterator<ListItem> itr = this.items.listIterator(startIdx);

    // Adds items to the displayed list while it can hold more and while the items list has items
    while (this.displayedItems.size() < this.maxItems && itr.hasNext()) {
      ListItem next = itr.next();
      this.displayedItems.add(next);
      add(next);
    }

    this.curStartIdx = startIdx;
    this.parent.repaint();
  }

  /**
   * Finds the list items currently displayed at a given y coordinate value
   * @param  int pos           Y coordinate value to find item at
   * @return     Returns the item displayed at the position if there is one and null otherwise
   */
  public ListItem itemDisplayedAt(int pos) {
    int idx = pos/ScrollList.ITEM_HEIGHT;
    if (idx < this.displayedItems.size())
      return this.displayedItems.get(idx);
    else
      return null;
  }

  /**
   * Sets the selection of the given item and unselects the previously selected item
   * @param ListItem selection New item to select
   */
  public void selectItem(ListItem selection) {
    if (selection != null) {
      if (this.selected != null)
        this.selected.setSelected(false);
      selection.setSelected(true);
      this.selected = selection;
    }
    this.parent.repaint();
  }

  /**
   * Selects the next item after the currently selected item if there is one
   */
  public void selectNext() {
    if (this.selected == null)
      return;

    ListItem next = this.selected.getNext();
    selectItem(next);
    this.parent.repaint();
  }

  /**
   * Selects the item displayed under the mouse when the mouse is pressed
   * @param MouseEvent e Current mouse event
   */
  public void mousePressed(MouseEvent e) {
    ListItem select = itemDisplayedAt(e.getY());
    if (select != null) {
      selectItem(select);
    }
  }

  /**
   * Opens the directory or image under the mouse when double-clicked
   * @param MouseEvent e Current mouse event
   */
  public void mouseClicked(MouseEvent e) {
    if (e.getClickCount() == 2) {
      ListItem selected = itemDisplayedAt(e.getY());
      if (selected != null) {
        selectItem(selected);
        if (selected.isDirectory())
          setDir(selected.getPath());
        else
          this.parent.triggerImgOpen(selected);
      }
    }
  }

  public void mouseReleased(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  /**
   * Sets the associated scroll bar to the scroll list
   * @param ScrollBar bar Scroll bar associated with the list
   */
  public void setBar(ScrollBar bar) {
    this.bar = bar;
    if (this.items.size() > this.maxItems)
      this.bar.setHeightRatio(((float)this.displayedItems.size())/this.items.size());
  }

  /**
   * Returns the currently selected list item
   * @return Currently selected item
   */
  public ListItem getSelected() {
    return this.selected;
  }

  public String toString() {
    String string = new String();
    for (ListItem item : this.items)
      string += item.toString() + "\n";
    return string;
  }

  @Override
  public void paintComponent(Graphics g) {
    ListIterator<ListItem> itr = this.displayedItems.listIterator();
    g.setColor(new Color(255, 255, 255));
    g.fillRect(0, 0, this.width, this.height);

    for (int i=0; i < this.displayedItems.size(); i++) {
      ListItem next = itr.next();
      int rectY = i*ScrollList.ITEM_HEIGHT;
      next.setDimensions(0, rectY, this.width, ScrollList.ITEM_HEIGHT);
      next.paintComponent(g);
    }

    g.setColor(new Color(0, 0, 0));
    g.drawRect(0, 0, this.width, this.height);
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension( this.width, this.height );
  }
}
