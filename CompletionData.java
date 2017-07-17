/**
 * Class to represent the completion status of a directory
 */
public class CompletionData {
  private int completeNum;        // Number of images completed within the directory
  private int fileNum;            // Total number of image files within the directory
  private float completionPerc;   // Percentage of total completion within the directory
  private boolean completed;      // Whether or not the directory is fully complete (all images completed)

  /**
   * Initializes a new set of completion data
   * @param  int complete      Number of completed images in the directory
   * @param  int total         Total number of images in the directory
   */
  public CompletionData(int complete, int total) {
    this.completeNum = complete;
    this.fileNum = total;
    this.completionPerc = ((float)complete)/total;
    this.completed = (complete == total);
  }

  /**
   * Returns the number of completed images within the directory
   * @return Number of completed images
   */
  public int getCompleteNum() {
    return this.completeNum;
  }

  /**
   * Returns the total number of image files within the directory
   * @return Total number of images
   */
  public int getFileNum() {
    return this.fileNum;
  }

  /**
   * Returns the percentage of completion for the directory and all of its images
   * @return Completion percentage
   */
  public float getCompletionPercent() {
    return this.completionPerc;
  }

  /**
   * Returns whether or not all images have been completed in the directory
   * @return Whether or not the entire directory is completed
   */
  public boolean isCompleted() {
    return this.completed;
  }
}
