import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.nio.file.*;

/**
 * Class containing a static method to check if the given path is an image or not
 */
public abstract class ImageChecker {

  /**
   * Returns whether or not the path is an image
   * @param  Path path          Path to the file to type check
   * @return      Whether or not the file type is an image
   */
  public static boolean isImage(Path path) {
    try {
      String mimetype = Files.probeContentType(path);
      String type = mimetype.split("/")[0];
      if (type.equals("image"))
        return true;
      else
        return false;
    } catch (Exception e) {
      System.out.println("Error checking file type! " + e);
      return false;
    }
  }
}
