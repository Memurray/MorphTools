README

Extra Features Implemented:
 * Arbitrary control point resolution
	-controlled via slider on main window
 * Control Point group move
	-Right clicking and dragging will draw a red rectangle on the image side being interacted with
	-When the mouse is released, all the points that are within the selection area will be highlighted
	-The corresponding points on the other image will be highlighted as well
	-Left clicking and dragging on any of the points in the selected area will move all the points selected
	-Right clicking anywhere (and not dragging) will disable group select
	-Selecting a point not in the group will disable group select and normal single point interaction is immediately availible

Issue (only 1 found in testing):
 * Group selection intersection slowdown
	-While the program does not crash, it is possible to hit major slow downs when performing group selected
	 control point moving where line intersection is detected. The program will recover and will not allow illegal placement
	 but the sheer number of calculations being performed and rejected can result in an unresponsive user experience until
	 user suggested position is clearly valid and the program has successfully caught up.

Feature Explanation:
 * Main window
	-Movable Control Points for Left Render Space, clicking and dragging a point will move it's location, prevents triangle intersection
	-Movable Control Points for Right Render Space, clicking and dragging a point will move it's location, prevents triangle intersection
	-Load image file into Left Render Space, In the top left corner is the file select, clicking it offers the ability to "Set Left Image"
	-Load image file into Right Render Space, In the top left corner is the file select, clicking it offers the ability to "Set Right Image"
	-Left image brightness control, using the slider bar below the left render space, brightness can be adjust between -100 and +100
	-Right image brightness control, using the slider bar below the right render space, brightness can be adjust between -100 and +100
	-Control Point resolution, defaults to 10x10 control points per image, but can be adjust with slider bar in the center bottom of the screen, allowing between a 5x5 and 20x20
	-Preview Warp button, pressing it will open a new window called Preview whose features will be explained in the section below
	-Render Morph button, pressing it will open a new window called Render whose features will be explained in the section below
 * Preview Warp
	-Using the locations of the control points from the main window (at the time when the preview button was pressed) it can display 
	 the change between control point locations in the left image and the right image
	-Frames per second slider, defaults to 30fps however using the slider bar on the left bottom, can be adjusted to between 10 and 60 fps
	-Transition time slider, defaults to 4 seconds however using the slider bar on the center bottom, can be adjust to between 1 and 10 seconds
	-Preview button, will snapshot the values of the two sliders and will display control point movement over time in the panel space above the button
 * Render Morph
	-Will only open if both images have been selected and they are the same size
	-Using all the information defined in the main window, presents the ability to see how the images morph to one another, and can save intermediate image to jpgs
	-Frames per second slider,,defaults to 30fps however using the slider bar on the left bottom, can be adjusted to between 10 and 60 fps 
	-Transition time slider, defaults to 4 seconds however using the slider bar on the center bottom, can be adjust to between 1 and 10 seconds
	-Preview button, will snapshot the values of the two sliders and will display over time how the morph looks
	-Render to files button, will snapshot the values of the two slides and will display over time how the morph looks while also saving each frame to a jpg file
	 When this process is complete, a popup window will display that render has completed
	-Rendered files will be present in the "images" folder in the same overall directory as this project

Acknowledgements
 * MorphTools.java was provided by Dr. Seales and other than adjusting alpha values, nothing was changed
 * Triangle.java was provided by Dr. Seales and was not altered
 * Blend function in RenderLogic.java is a slightly modified version of a function I found online at "http://www.informit.com/articles/article.aspx?p=1245201"
 * Everything else was solely my own product
	
	

	
	
		