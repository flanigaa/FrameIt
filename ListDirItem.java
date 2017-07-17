import java.nio.file.Path;
import java.awt.*;

/**
 * Class that extends ListItem and is used to specifically represent a directory
 * The class keeps track of the current completion data for the directory:
 *  i.e. things like total number of images and total number of complete images recursively in the directory
 */
public class ListDirItem extends ListItem {

  private int completeNum;        // Number of complete images within the directory
  private int fileNum;            // Total number of files within the directory
  private float completionPerc;   // Total completion percentage of the directory (i.e. [10 completed images/100 total images] == 10% completion)

  /**
   * Initializes a new item representing a directory
   * @param  Path           path          Path to the directory
   * @param  String         aName         Name to display and represent the directory
   * @param  CompletionData data          Completion data for the directory
   */
  public ListDirItem(Path path, String aName, CompletionData data) {
    super(path, aName, true, data.isCompleted());
    this.completeNum = data.getCompleteNum();
    this.fileNum = data.getFileNum();
    this.completionPerc = data.getCompletionPercent();
  }

  /**
   * Updates the directory item with the new completion data
   * @param CompletionData data New completion data
   */
  public void updateCompletionData(CompletionData data) {
    this.completed = data.isCompleted();
    this.completeNum = data.getCompleteNum();
    this.fileNum = data.getFileNum();
    this.completionPerc = data.getCompletionPercent();
    repaint();
  }

  /**
   * Returns number of images that have been completed and have save files within the directory
   * @return Number of completed images
   */
  public int getCompleteNum() {
    return this.completeNum;
  }

  /**
   * Returns the total number of images within the directory
   * @return Number of images in the directory
   */
  public int getFileNum() {
    return this.fileNum;
  }

  @Override
  public void paintComponent(Graphics g) {
    if ( this.selected ) {
      g.setColor(new Color(70, 155, 255));
      g.fillRect(this.xPos, this.yPos, this.width, this.height);
    } else if ( this.completed ) {
      g.setColor(new Color(100, 255, 100));
      g.fillRect(this.xPos, this.yPos, this.width, this.height);
    } else {
      g.setColor(new Color(255, 75, 75));
      g.fillRect(this.xPos, this.yPos, this.width, this.height);
      g.setColor(new Color(100, 255, 100));
      int completionWidth = (int)(this.width*this.completionPerc);
      g.fillRect(this.xPos, this.yPos, completionWidth, this.height);
    }

    g.setColor(new Color( 0, 0, 0 ) );
    g.drawRect(this.xPos, this.yPos, this.width, this.height);
    Font font = g.getFont();
    FontMetrics metrics = g.getFontMetrics( font );
    String displayName = this.name + "  " + this.completeNum + "/" + this.fileNum;
    while (metrics.stringWidth(displayName) > this.width-10) {
      font = new Font(font.getName(), font.getStyle(),
          (int)(font.getSize()*(3.0/4)));
      metrics = g.getFontMetrics(font);
    }
    g.setFont( font );
    int centerY = this.yPos + ((this.height - metrics.getHeight())/2) + metrics.getAscent();
    g.drawString(displayName, this.xPos+10, centerY);
  }
}
