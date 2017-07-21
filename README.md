# FrameIt
## What is FrameIt?
FrameIt is a simple Java application written to allow for the creation on ground truth for image files.

In its current state, FrameIt allows for multiple "types" of frames to be used to differentiate marking styles i.e. easy and hard. When you open FrameIt, it will load the images folder and all image files and directories inside of it--ignoring any file that cannot be opened as an image. The interface is simple and easy to use--allowing for the ability to easily mark any ground truth in an image able to be outlined by a rectangle.

## How to use FrameIt:
First, you must have a directory named "images" in the same directory as the FrameIt.jar. Within that "images" folder you can have more nested folders or images by themselves.

After you have the "images" folder, you can now open FrameIt and it will load all directories and files able to be loaded as an image into a file explorer on the left side. For each directory, the number of images within it are counted and checked for completion--showing a "completed/total" next to the name. This completion is also represented by the color progress of the directory in the list.
Any file that does not have a corresponding save file will be displayed as red in the list, and will turn green once a save is created. As a directory becomes more complete, it will visually fill with green.

The file explorer on the side can be navigated through by either double clicking or selecting an item by a single click and then clicking the open button at the top. Once the open action has been performed on an image file, it will be opened in the middle of the window.

In the file explorer colors represent the following:
* Red: Incomplete directory or file without a save
* Green: Complete directory or file
* Blue: Currently selected item in the list. The selected directory or file will be opened if the open button is clicked
* Purple: Marks the image that is currently opened in the editor

Once an image is open, you can click and drag to draw rectangles anywhere on the image.

Other controls in FrameIt include:
* Draw (can have multiple draw modes):

  Draws a rectangle corresponding to the click and drag of the mouse over the image. Release to finish drawing the current rectangle.
* Delete:

  Deletes any rectangles that contain the mouse when clicked. Can be undone using the __redo__ button.
* Undo:

  Undoes the last rectangle drawn or undoes a clear.
* Redo:

  Redoes the last undo __or__ delete.
* Clear:

  Clears all existing rectangles that are currently drawn. Can be undone using the __undo__ button.
* Save:

  Saves the current progress of an image to a save file. See below for details on how the saves are formatted.
* Save and Proceed:

  Saves the progress and opens the next image in the explorer.

## How the Files are Saved:
After saving an image, FrameIt creates a "saves" folder in the same directory as the FrameIt.jar and the "images" folder. If the image is located within nested directories, the save file will be nested in the same manner. For example, if the image is located in "source/images/folder_1/folder_2/img.jpg", the save file will be "source/saves/folder_1/folder_2/img.txt".

## Formatting of Save files:
The saves are formatted as follows:
* Line 1- nested_directories(if any)/name_of_image (folder_1/folder_2/img.txt in the above example)
* Line 2- Dimensions of the image when it was loaded in
* Line 3- Number of frames drawn in the image when saved
* Lines 4 to End- Each line after the third represents a single rectangle

Each rectangle line represents the following:

startX, startY, width, height, type

The type will be 0 as a default unless saved otherwise.
