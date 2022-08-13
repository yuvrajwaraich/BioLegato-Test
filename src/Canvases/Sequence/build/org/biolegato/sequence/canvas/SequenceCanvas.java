package org.biolegato.sequence.canvas;

/*
 * SequenceCanvas.java
 *
 * Created on December 3, 2007, 1:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.Reader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.biolegato.main.BLMain;
import org.biolegato.main.DataCanvas;
import org.biopcd.parser.PCD;
import org.biolegato.sequence.data.Dataset;
import org.biolegato.sequence.data.DataFormat;

/**
 * <p>The BioLegato sequence-based canvas class (based on GDE).</p>
 *
 * <p>Add anything that might cause problems or complicate the modification of
 *    this class in general within this section!</p>
 *
 * <p>Some special things to watch out for when editing the SequenceCanvas
 *    object:</p>
 *
 * <ol>
 *      <li>Be sure to handle the readonly property whenever possible
 *
 *          <p>The properties for BioLegato contains an optional property called
 *          readonly, that should do, as the name implies, write-protect the
 *          data.  The 'editable' constant stores the opposite value of the
 *          readonly property.  To ensure that this happens, when programming
 *          you must wrap all methods that involve canvas manipulation with the
 *          following:</p>
 * <pre>{@code
 *      if (editable) {
 *          // CODE goes in here (readonly status is OFF)
 *      }
 * }</pre>
 *      </li>
 *
 *      <li><p>You must handle that there can be up to two text panes at any
 *             given time.</p>
 *
 *          <p>Because the canvas can be split into two separate text panes, any
 *          methods which interact with the primary text pane (dataCollector),
 *          should also be able to interact with the secondary/alternate text
 *          pane (altDataCollector).  If this is not done, coding may introduce
 *          unforseen bugs into the code.</p>
 *
 *          <p><b>IMPORTANT: Also note that if there is no secondary/alternate
 *              canvas being used (i.e. the text canvas has not yet been "Split"
 *              into two text panes), the variable "altDataCollector" will be
 *              null.  Your code must also handle this situation by not
 *              interacting with altDataCollector unless it is not null.</b></p>
 *
 *          <p><b>IMPORTANT: Also be sure to look at the code for splitAction
 *              and joinAction whenever you modify the variable dataCollector.
 *              This is because these two action commands contain the code to
 *              construct and destruct the alternate text pane
 *              (altDataCollector).</b></p>
 *      </li>
 *
 *      <li>Menu items are hidden depending on whether the current selected
 *          sequence or part of is within the list pane, or text pane.
 *
 *          <p><b>See selectionMade and valueChanged methods</b></p>
 *      </li>
 * </ol>
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public final class SequenceCanvas extends DataCanvas
                                  implements ListSelectionListener {
    /**
     * The "Open..." menu item action.  This action will open a file and read it
     * into the sequence canvas.  The user will select the file to open by using
     * a JFileChooser dialog box.  The open dialog box will use DataFormat
     * objects as file filters, to allow the user to select the file type, and
     * to ensure that the code for parsing each file is associated with the
     * same object as the file filter (which the user will select).  In the case
     * that the user selects the "accept all" file filter, auto-detection code
     * will be executed.  This auto-detection code is contained in the method:
     *  org.biolegato.sequence.data.DataFormat.autodetect
     **
     * @see org.biolegato.sequence.data.DataFormat
     * @see org.biolegato.sequence.data.DataFormat#autodetect(java.util.Scanner)
     */
    public final AbstractAction openAction = new AbstractAction("Open...") {
        /**
         * Serialization number - required for no warnings
         */
        private static final long serialVersionUID = 7526472295622776157L;

        /**
         * Sets the mnemonic for the event.
         */
        {
            putValue(MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_O));
        }

        /**
         * Event handler - displays a JFileChooser so that the user can select
         * and open a file.  Once the file is selected, the file will be read
         * into the current canvas, using whichever FileFilter choice the user
         * selected.  Each FileFilter in the JFileChooser window will correspond
         * to a DataFormat object; the only exception being the "all files"
         * option, which will cause BioLegato to auto-detect the file format
         * before reading the file.
         **
         * @param evt ignored by this method.
         * @see org.biolegato.sequence.data.DataFormat
         * @see org.biolegato.sequence.data.DataFormat#autodetect(java.util.Scanner)
         */
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            // A variable for storing all of the files the user selects inside
            // the JFileChooser window.
            File[] openFiles;
            // The JFileChooser windwow, from which the user selects the file
            // to open.
            JFileChooser openDlg = new JFileChooser();

            // Configure the JFileChooser object to use PCD's current directory
            // as its default directory; enable multiple file selection; and to
            // enable the "accept all" file filter (which will correspond to
            // file type auto-detection).
            openDlg.setCurrentDirectory(PCD.getCurrentPWD());
            openDlg.setAcceptAllFileFilterUsed(true);
            openDlg.setMultiSelectionEnabled(true);

            // Iterate through all of the DataFormat classes available, and add
            // each file format FileFilter object to the JFileChooser.  This
            // enables the user to select whichever file format they desire.
            for (DataFormat currentFormat : DataFormat.FORMAT_LIST) {
                openDlg.addChoosableFileFilter(currentFormat);
            }

            // If the user clicks the OK button, then check to see if at least
            // one file is selected, and also update the current PCD path (such
            // that any subsequent new file choosers will use the directory last
            // browsed as their default directory.  If files are selected, then
            // read them into the canvas.
            if (openDlg.showOpenDialog(getJFrame())
                    == JFileChooser.APPROVE_OPTION) {
                // Extract the files (if any) selected in the JFileChooser.
                openFiles = openDlg.getSelectedFiles();

                // Prevent parsing a null value for openFiles.
                if (openFiles != null) {
                    // Iterate through all of the files selected in the
                    // JFileChooser object.
                    for (File opf : openFiles) {
                        // Ensure that the file exists and is a file (i.e. skip
                        // directories and non-existent files).
                        if (opf.exists() && opf.isFile() && opf.length() > 0) {
                            try {
                                // Branch.  If a file filter was selected, then
                                // use the file filter object to parse the file.
                                // (All file filters in the JFileChooser are
                                // DataFormat objects, which have code to parse
                                // their files.)
                                if (openDlg.getFileFilter() != null
                                        && !openDlg.getFileFilter().equals(
                                        openDlg.getAcceptAllFileFilter())) {
                                    readFile((DataFormat)
                                            openDlg.getFileFilter(),
                                            new FileReader(opf), false,false);
                                } else {
                                    // Autodetect the file type.
                                    readFile("", new FileReader(opf), false,false);
                                }
                            } catch (Throwable e) {
                                // Print a stack trace if any error occurs.
                                e.printStackTrace(System.err);
                            }
                        }
                    }
                }

                // Update the PCD working directory.
                if (openDlg.getCurrentDirectory() != null) {
                    PCD.setCurrentPWD(openDlg.getCurrentDirectory());
                }
            }
        }
    };

    /**
     * The "Save ALL As..." menu item action.  This action will save the entire
     * contents of the canvas to a file and write it to disk.  The user will
     * select the file by using a JFileChooser dialog box.  The file chooser
     * will use DataFormat objects as file filters, to allow the user to select
     * the file type, and to ensure that the code for parsing each file is
     * associated with the same object/class as the file filter (which the user
     * will select).
     **
     * @see org.biolegato.sequence.data.DataFormat
     */
    public final AbstractAction saveAsAction
            = new AbstractAction("Save ALL As...") {

        /**
         * Serialization number - required for no warnings
         */
        private static final long serialVersionUID = 7526472295622776157L;

        /**
         * Sets the mnemonic for the event.
         */
        {
            putValue(MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_S));
        }

        /**
         * Event handler - displays a JFileChooser so that the user can select
         * and save a file.  Once the file is selected, the entire contents of
         * the canvas will be written to a file, using whichever FileFilter
         * choice the user selected.  Each FileFilter in the JFileChooser window
         * will correspond to a DataFormat object.
         **
         * @param evt ignored by this method.
         * @see org.biolegato.sequence.data.DataFormat
         */
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            // The JFileChooser windwow, from which the user selects the file
            // to save the contents of the canvas to.
            JFileChooser saveDialog = new JFileChooser();

            // Configure the JFileChooser object to use PCD's current directory
            // as its default directory, and disable the "accept all" file
            // filter option.
            saveDialog.setCurrentDirectory(PCD.getCurrentPWD());
            saveDialog.setAcceptAllFileFilterUsed(false);

            // Iterate through all of the DataFormat classes available, and add
            // each file format FileFilter object to the JFileChooser.  This
            // enables the user to select whichever file format they desire.
            for (DataFormat currentFormat : DataFormat.FORMAT_LIST) {
                saveDialog.addChoosableFileFilter(currentFormat);
            }

            // If the user clicks the OK button, then check to see if at least
            // one file is selected, and also update the current PCD path (such
            // that any subsequent new file choosers will use the directory last
            // browsed as their default directory.  If a file is selected, write
            // to that file.
            if (saveDialog.showSaveDialog(getJFrame())
                    == JFileChooser.APPROVE_OPTION
                    && saveDialog.getSelectedFile() != null
                    && (!saveDialog.getSelectedFile().exists()
                    || javax.swing.JOptionPane.showConfirmDialog(null,
                            "Overwrite existing file?", "Overwrite",
                            javax.swing.JOptionPane.OK_CANCEL_OPTION,
                            javax.swing.JOptionPane.QUESTION_MESSAGE)
                        != javax.swing.JOptionPane.CANCEL_OPTION)) {
                // Write to the actual file.
                try {
                    // Create a file writer object to write to the file.
                    FileWriter writer
                            = new FileWriter(saveDialog.getSelectedFile());

                    // Write the actual data to the file.
                    writeFile(((DataFormat) saveDialog.getFileFilter()),
                            writer, true);

                    // Flush and close the file writer buffer, to ensure that
                    // the file is written properly to disk.
                    writer.flush();
                    writer.close();
                } catch (Throwable e) {
                    // Print a stack trace if any error occurs.
                    e.printStackTrace(System.err);
                }
                
                // Update the PCD working directory.
                if (saveDialog.getCurrentDirectory() != null) {
                    PCD.setCurrentPWD(saveDialog.getCurrentDirectory());
                }
            }
        }
    };

    /**
     * The "Properties..." menu item action.  This action will open a window, so
     * the user can change the properties of the sequence canvas.  The
     * properties window is controlled (and displayed, etc.) by the
     * SequenceCanvasProperties class.
     **
     * @see org.biolegato.sequence.canvas.SequenceCanvasProperties
     */
    public final AbstractAction propertiesAction
            = new AbstractAction("Properties...") {

        /**
         * Serialization number - required for no warnings
         */
        private static final long serialVersionUID = 7526472295622776157L;

        /**
         * Sets the mnemonic for the event.
         */
        {
            putValue(MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_P));
        }

        /**
         * The "Properties..." menu item action.  This action will open a
         * window, so the user can change the properties of the sequence canvas.
         * The properties window is controlled (and displayed, etc.) by the
         * SequenceCanvasProperties class.
         **
         * @param evt ignored by this method.
         * @see org.biolegato.sequence.canvas.SequenceCanvasProperties
         */
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            new SequenceCanvasProperties(getJFrame(),
                                                canvasSelf).setVisible(true);
        }
    };

    /**
     * <p>The "Cut" menu item action.  This action will copy any content
     *    currently selected in the canvas to the clipboard, and then delete
     *    that selected content.  This behaviour is identical to the "Cut"
     *    action in most word processor programs (e.g. Office, OpenOffice, etc.)
     * </p>
     *
     * <p><i>Please note that this method will not work if the read only
     *       property is set.  This method will check the readonly property by
     *       checking the editable field.  If the editable field is set to false
     *       this method will not paste or execute any further code.</i></p>
     **
     * @see org.biolegato.sequence.canvas.SequenceCanvas#copyClipboard()
     * @see org.biolegato.sequence.canvas.SequenceCanvasObject#deleteSelection()
     */
    public final AbstractAction cutAct = new AbstractAction("Cut") {

        /**
         * Serialization number - required for no warnings
         */
        private static final long serialVersionUID = 7526472295622777033L;

        /**
         * Sets the mnemonic for the event.
         */
        {
            putValue(MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_C));
        }


        /**
         * <p>The "Cut" menu item action.  This action will copy any content
         *    currently selected in the canvas to the clipboard, and then delete
         *    that selected content.  This behaviour is identical to the "Cut"
         *    action in most word processor programs (e.g. Office, OpenOffice,
         *    etc.)</p>
         *
         * <p><i>Please note that this method will not work if the read only
         *       property is set.  This method will check the readonly property
         *       by checking the editable field.  If the editable field is set
         *       to false this method will not paste or execute any further
         *       code.</i></p>
         **
         * @param evt ignored by this method.
         * @see org.biolegato.sequence.canvas.SequenceCanvas#copyClipboard()
         * @see org.biolegato.sequence.canvas.SequenceCanvasObject#deleteSelection()
         */
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            // Handle the read only property (i.e. prevent any possible
            // data manipulation if editable is set to false!)
            if (editable) {
                copyClipboard();
                currentPane.deleteSelection();
            }
        }
    };

    /**
     * The "Copy" menu item action.  This action will copy any content currently
     * selected in the canvas to the clipboard.  This is done by calling the
     * copyClipboard() function.
     **
     * @see org.biolegato.sequence.canvas.SequenceCanvas#copyClipboard()
     */
    public final AbstractAction copyAct = new AbstractAction("Copy") {

        /**
         * Serialization number - required for no warnings
         */
        private static final long serialVersionUID = 7526472295622777033L;

        /**
         * Sets the mnemonic for the event.
         */
        {
            putValue(MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_Y));
        }

        /**
         * The "Copy" menu item action.  This action will copy any content
         * currently selected in the canvas to the clipboard.  This is done by
         * calling the copyClipboard() function.
         **
         * @param evt ignored by this method.
         * @see org.biolegato.sequence.canvas.SequenceCanvas#copyClipboard()
         */
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            copyClipboard();
        }
    };

    /**
     * <p>The "Paste" menu item action.  This action will delete any currently
     * selected content, and then write the content in the clipboard (if any)
     * to the canvas.  This behaviour is almost identical to the "Paste" action
     * in most word processor programs (e.g. Office, OpenOffice, etc.) with only
     * the odd minor differences due to this being a sequence canvas.</p>
     * 
     * <p><i>Please note that this method will not work if the read only
     *       property is set.  This method will check the readonly property by
     *       checking the editable field.  If the editable field is set to false
     *       this method will not paste or execute any further code.</i></p>
     **
     * @see org.biolegato.sequence.canvas.SequenceCanvas#pasteClipboard()
     */
    public final AbstractAction pasteAct = new AbstractAction("Paste") {

        /**
         * Serialization number - required for no warnings
         */
        private static final long serialVersionUID = 7526472295622777033L;

        /**
         * Sets the mnemonic for the event.
         */
        {
            putValue(MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_P));
        }

        /**
         * <p>The "Paste" menu item action.  This action will delete any
         * currently selected content, and then write the content in the
         * clipboard (if any) to the canvas.  This behaviour is almost identical
         * to the "Paste" action in most word processor programs (e.g. Office,
         * OpenOffice, etc.) with only the odd minor differences due to this
         * being a sequence canvas.</p>
         *
         * <p><i>Please note that this method will not work if the read only
         *       property is set.  This method will check the readonly property
         *       by checking the editable field.  If the editable field is set
         *       to false this method will not paste or execute any further
         *       code.</i></p>
         **
         * @param evt ignored by this method.
         * @see org.biolegato.sequence.canvas.SequenceCanvas#pasteClipboard()
         */
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            // Handle the read only property (i.e. prevent any possible
            // data manipulation if editable is set to false!)
            if (editable) {
                pasteClipboard();
            }
        }
    };

    /**
     * <p>The "Change case" menu item action.  This action will call the change
     * case method of either the sequence list or text area (depending on which
     * object - the list of text area - was used last).  In turn, the case of
     * any selected string will be changed, according to the rules outlined in
     * either changeCase method (these rules should be the same for both!)</p>
     * <p>Please see either the sequence list or sequence text area's changeCase
     *    function for further details.</p>
     **
     * @see org.biolegato.sequence.canvas.SequenceList#changeCase()
     * @see org.biolegato.sequence.canvas.SequenceTextArea#changeCase() 
     */
    public final AbstractAction changeCaseAction
                                        = new AbstractAction("Change case") {

        /**
         * Serialization number - required for no warnings
         */
        private static final long serialVersionUID = 7526472295622777033L;

        /**
         * Sets the mnemonic for the event.
         */
        {
            putValue(MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_E));
        }


        /**
         * <p>The "Change case" menu item action.  This action will call the
         * change case method of either the sequence list or text area
         * (depending on which object - the list of text area - was used last).
         * In turn, the case of any selected string will be changed, according
         * to the rules outlined in either changeCase method (these rules should
         * be the same for both!)</p>
         * <p>Please see either the sequence list or sequence text area's
         *    changeCase function for further details.</p>
         **
         * @param evt ignored by this method.
         * @see org.biolegato.sequence.canvas.SequenceList#changeCase()
         * @see org.biolegato.sequence.canvas.SequenceTextArea#changeCase()
         */
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            currentPane.changeCase();
        }
    };
    
    /**
     * Action for splitting the text area portion of the canvas into two.  The
     * split is accomplished by creating a second text area, then displaying
     * both in a JSplitPane.  Both text areas are synchronized through listeners
     * and by using the same Dataset object.  In addition, this action will
     * remove the Split option from all menus and replace it with a Join option
     * (to undo the text area splitting).
     */
    private final AbstractAction splitAction = new AbstractAction("Split") {

        /**
         * Serialization number - required for no warnings
         */
        private static final long serialVersionUID = 7526472295622777032L;

        /**
         * Action for splitting the text area portion of the canvas into two.
         * The split is accomplished by creating a second text area, then
         * displaying both in a JSplitPane.  Both text areas are synchronized
         * through listeners and by using the same Dataset object.  In addition,
         * this action will remove the Split option from all menus and replace
         * it with a Join option (to undo the text area splitting).
         **
         * @param evt ignored by this method.
         */
        public void actionPerformed(ActionEvent evt) {

            // Create the new sequence text area.
            altDataCollector = new SequenceTextArea(canvasSelf, datamodel);

            // Remove the old sequence text area from the canvas pane and
            // replace it with a JSplitPane (containing both text areas; each
            // text area is also enclosed within a scroll pane, the scroll pane
            // is inside the JSplitPane).
            canvasPane.remove(dataPane);
            altDataPane = new JScrollPane(altDataCollector,
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                       dataPane, altDataPane);
            splitPane.setAlignmentX(JSplitPane.CENTER_ALIGNMENT);
            splitPane.setAlignmentY(JSplitPane.CENTER_ALIGNMENT);
            splitPane.setOneTouchExpandable(false);
            splitPane.setDividerLocation(0.5);
            splitPane.setResizeWeight(0.5);
            splitPane.doLayout();
            canvasPane.add(splitPane);
            
            // Ensure that the scroll bar for the split canvas is syncrhonized.
            altDataPane.setVerticalScrollBar(dataPane.getVerticalScrollBar());
            
            // Refresh the display.
            canvasPane.doLayout();
            canvasPane.validate();
            canvasPane.repaint(50L);

            // Remove the split option from the text area's popup menu and
            // replace it with the join action.
            dataCollector.popup.remove(splitMenuItem);
            dataCollector.popup.add(joinMenuItem);

            // Add the join action to the secondary text area's popup menu.
            altDataCollector.popup.add(new JMenuItem(joinAction));
        }
    };

    /**
     * Action for joining a split text area back into one text area.  The split
     * is accomplished by creating a second text area, then displaying both in a
     * JSplitPane.  Both text areas are synchronized through listeners and by
     * using the same Dataset object.  Thus, to join the two text areas, all
     * that needs to be done is: delete one of the text areas, replace the
     * JSplitPane with the remaining canvas (which is still enclosed in a scroll
     * pane).  In addition, this action will remove the Join option from all
     * menus and replace it with a Split option (to redo the text area
     * splitting).
     */
    public final AbstractAction joinAction = new AbstractAction("Join") {

        /**
         * Serialization number - required for no warnings
         */
        private static final long serialVersionUID = 7526472295622777032L;

        /**
         * Action for joining a split text area back into one text area.  The
         * split is accomplished by creating a second text area, then displaying
         * both in a JSplitPane.  Both text areas are synchronized through
         * listeners and by using the same Dataset object.  Thus, to join the
         * two text areas, all that needs to be done is: delete one of the text
         * areas, replace the JSplitPane with the remaining canvas (which is
         * still enclosed in a scroll pane).  In addition, this action will
         * remove the Join option from all menus and replace it with a Split
         * option (to redo the text area splitting).
         **
         * @param evt ignored by this method.
         */
        public void actionPerformed(ActionEvent e) {
            // If the currently selected panel is the alternate/split/extra text
            // area, then set the currently selected panel to the original text
            // area.
            if (currentPane == altDataCollector) {
                currentPane = dataCollector;
            }

            // If the split panel is not null (it should be not null), then
            // remove it from the canvas, and replace it with the original text
            // area.
            if (splitPane != null) {
                canvasPane.remove(splitPane);
                splitPane = null;
                altDataPane = null;
                canvasPane.add(dataPane);
            }
            
            // Fix the scrollbar.
            dataPane.setVerticalScrollBar(dataPane.getVerticalScrollBar());
            
            // Refresh the display.
            canvasPane.doLayout();
            canvasPane.validate();
            canvasPane.repaint(50L);

            // Remove the join option from the text area's popup menu and
            // replace it with the split action.
            dataCollector.popup.remove(joinMenuItem);
            dataCollector.popup.add(splitMenuItem);
        }
    };
    /**
     * The current font for the Sequence canvas.  This font is used by the
     * sequence list and sequence text areas.  This font is set by the function:
     * 'updateFont' (in the SequenceCanvas), which is called by the
     * SequenceCanvasProperties window, whenever the font properties are
     * changed (see the 'actionPerformed' and 'stateChanged' in the
     * SequenceCanvas class).  Fixed-width fonts are generally preferred, as
     * they prevent visual artifacts occurring in the sequence text area's print
     * functions.
     **
     * @see org.biolegato.sequence.canvas.SequenceCanvas#updateFont(int, boolean)
     * @see org.biolegato.sequence.canvas.SequenceCanvasProperties#actionPerformed(java.awt.event.ActionEvent) 
     * @see org.biolegato.sequence.canvas.SequenceCanvasProperties#stateChanged(javax.swing.event.ChangeEvent)
     */
    Font currentFont = DEFAULT_FONT;
    /**
     * The main canvas display panel.  This panel will contain 'mainPane', a
     * JSeparator, and 'statusBar' (the canvas's status bar).  This display
     * panel is initialized to use a page-axis (vertical) box layout.
     */
    private Box mainDisplay = new Box(BoxLayout.PAGE_AXIS);
    /**
     * The current Dataset object for the SequenceCanvas, SequenceTextArea, and
     * SequenceList.  Using one Dataset object ensures that all three classes
     * will have their contents synchronized.
     */
    private final Dataset datamodel = new Dataset(this);
    /**
     * A list of sequences used for selecting individual sequences.  This list
     * is displayed on the left-most side of the canvas.
     */
    private SequenceList nameList = new SequenceList(this, datamodel);
    /**
     * The main text area for sequence data manipulation.  This is displayed on
     * the right-most side of the canvas, and is used for editing the individual
     * characters of the sequences in the canvas.
     */
    private static SequenceTextArea dataCollector = null;
    /**
     * The alternate text area for data manipulation (for splits).  This text
     * area is only displayed when the text area is split.  This text area is
     * located between the main sequence text area (dataCollector) and the
     * sequence list (nameList).
     **
     * @see org.biolegato.sequence.canvas.SequenceCanvas#joinAction
     * @see org.biolegato.sequence.canvas.SequenceCanvas#splitAction
     */
    private static SequenceTextArea altDataCollector = null;
    /**
     * Stores which canvas contains the cursor and any current data selections.
     * This is used for determining which canvas to call with selection
     * manipulation functions, such as change case, copy, paste, etc.
     **
     * @see org.biolegato.sequence.canvas.SequenceCanvas#cutAct
     * @see org.biolegato.sequence.canvas.SequenceCanvas#copyAct
     * @see org.biolegato.sequence.canvas.SequenceCanvas#pasteAct
     * @see org.biolegato.sequence.canvas.SequenceCanvas#changeCaseAction
     */
    private SequenceCanvasObject currentPane = nameList;
    /**
     * The scroll pane containing the main sequence text area (dataCollector).
     **
     * @see org.biolegato.sequence.canvas.SequenceCanvas#dataCollector
     * @see org.biolegato.sequence.canvas.SequenceCanvas#joinAction
     * @see org.biolegato.sequence.canvas.SequenceCanvas#splitAction
     */
    private JScrollPane dataPane = null;
    /**
     * The scroll pane containing the alternate/split sequence text area
     * (altDataCollector).  This sequence text area is only used for splitting
     * the main sequence text area.  See altDataCollector for more details.
     **
     * @see org.biolegato.sequence.canvas.SequenceCanvas#altDataCollector
     * @see org.biolegato.sequence.canvas.SequenceCanvas#dataCollector
     * @see org.biolegato.sequence.canvas.SequenceCanvas#joinAction
     * @see org.biolegato.sequence.canvas.SequenceCanvas#splitAction
     */
    private JScrollPane altDataPane = null;
    /**
     * The box panel to store either the scroll-enclosed sequence text area
     * directly, or the JSplitPane containing the scroll-enclosed main and split
     * sequence text areas.
     **
     * @see org.biolegato.sequence.canvas.SequenceCanvas#dataPane
     * @see org.biolegato.sequence.canvas.SequenceCanvas#splitPane
     */
    private Box canvasPane = new Box(BoxLayout.LINE_AXIS);
    /**
     * A split pane for displaying both scroll-enclosed sequence text areas --
     * dataCollector (in dataPane) and altDataCollector (in altDataPane) --
     * side-by-side.  This split pane should displayed inside canvasPane
     * whenever the sequence text area is in its "split" state.
     **
     * @see org.biolegato.sequence.canvas.SequenceCanvas#altDataCollector
     * @see org.biolegato.sequence.canvas.SequenceCanvas#dataCollector
     * @see org.biolegato.sequence.canvas.SequenceCanvas#joinAction
     * @see org.biolegato.sequence.canvas.SequenceCanvas#splitAction
     */
    private JSplitPane splitPane = null;
    /**
     * Reference pointer to self.
     */
    private final SequenceCanvas canvasSelf = this;
    /**
     * The caret position portion of the canvas status bar: this label tracks
     * the row and column position of the text caret, within the sequence text
     * area.
     */
    private final JLabel status = new JLabel("Row: 1 Col: 1");
    /**
     * The insertion mode status portion of the canvas status bar: this label
     * tracks the insertion status of the sequence text area.
     */
    private final JLabel insertStatus = new JLabel("     ");
    /**
     * A menu item object for the "Select sequence by name" action.
     * See SequenceList.selectByNameAction for the actual function and its
     * details.
     **
     * @see org.biolegato.sequence.canvas.SequenceList#selectByNameAction
     */
    private final JMenuItem selectByNameMenuItem
            = new JMenuItem(nameList.selectByNameAction);
    /**
     * A menu item object for the "Split canvas" action.
     * See SequenceCanvas.splitAction for the actual function and its details.
     **
     * @see org.biolegato.sequence.canvas.SequenceCanvas#splitAction
     */
    private final JMenuItem splitMenuItem = new JMenuItem(splitAction);
    /**
     * A menu item object for the "Re-joining split canvases" action.
     * See SequenceCanvas.joinAction for the actual function and its details.
     **
     * @see org.biolegato.sequence.canvas.SequenceCanvas#joinAction
     */
    private final JMenuItem joinMenuItem = new JMenuItem(joinAction);
    /**
     * A menu item object for the "Group sequences" action.
     * See SequenceList.groupAction for the actual function and its details.
     **
     * @see org.biolegato.sequence.canvas.SequenceList#groupAction
     */
    private final JMenuItem groupMI = new JMenuItem(nameList.groupAction);
    /**
     * A menu item object for the "Ungroup sequences" action.
     * See SequenceList.ungroupAction for the actual function and its details.
     **
     * @see org.biolegato.sequence.canvas.SequenceList#ungroupAction
     */
    private final JMenuItem ungroupMI = new JMenuItem(nameList.ungroupAction);
    /**
     * A menu item object for the "Get sequence info..." action.
     * See SequenceList.getInfoAction for the actual function and its details.
     **
     * @see org.biolegato.sequence.canvas.SequenceList#getInfoAction
     */
    private final JMenuItem getInfoMI = new JMenuItem(nameList.getInfoAction);
    /**
     * Stores whether or not the data within the canvas is editable
     * (this is read from BioLegato's properties).  Because this variable is a
     * final constant, it is given package access (to facilitate other classes
     * easily reading its value).
     */
    final boolean editable = !"true".equalsIgnoreCase(getProperty("readonly"));
    /**
     * The default font for the canvas (12pt monospaced).
     */
    public static final Font DEFAULT_FONT = new Font("Monospaced",
                                                     Font.PLAIN, 12);
    /**
     * Used for serialization purposes, to enable compilation without warnings.
     */
    private static final long serialVersionUID = 7526472295622776154L;

/////////////////
//*************//
//* CONSTANTS *//
//*************//
/////////////////
    /**
     * The list of all colour masks loaded in the system.  This list is filled,
     * by default, with all of the colour masks in the "seqcoloursdir" path(s).
     * seqcoloursdir is a BioLegato property, which can be initialized in an
     * external properties file.  The specification for "seqcoloursdir" is a
     * list of paths, separated by the path separator key (: in UNIX, ; in
     * Windows).  The paths are loaded recursively, thus any directories and
     * sub-directories, specified or contained in the path, will be parsed.
     * Only files found in the path ending with ".csv" will be read.  The ".csv"
     * files will be treated as character colour masks.  For more informaton on
     * the character colour mask specification, please see:
     *          ColourMask.readCharMaskFile
     **
     * @see org.biolegato.sequence.canvas.ColourMask#readCharMaskFile(java.io.File) 
     */
    public final List<ColourMask> colourMasks = new ArrayList<ColourMask>() {
        /**
         * Initialize the list object, and load all of the colour masks
         * contained in the seqcolourdirs path list.
         */
        {
            String[] searchpaths = null;

            searchpaths = BLMain.toPathList(getProperty("seqcolourdirs"));
            for (String path : searchpaths) {
                try {
                    loadMasks(new File(path));
                } catch (Exception th) {
                    th.printStackTrace(System.err);
                }
            }
        }

        /**
         * Loads all of the colour masks contained in a file or directory,
         * recursively, using the ColourMask.readCharMaskFile function.
         **
         * @param location the file object to read colour masks from.
         */
        private void loadMasks(File location) {
            if (location.exists() && location.canRead()) {
                if (location.isDirectory()) {
                    for (File file : location.listFiles()) {
                        loadMasks(file);
                    }
                } else if (location.isFile() && location.getAbsolutePath()
                            .toLowerCase().endsWith(".csv")
                        && location.canRead() && location.exists()) {
                    try {
                        add(ColourMask.readCharMaskFile(location));
                    } catch (IOException ioe) {
                        ioe.printStackTrace(System.err);
                    }
                }
            }
        }
    };
    /**
     * The default colour mask for BioLegato.  This colour mask is determined
     * by the getDefaultMask() function.
     **
     * @see org.biolegato.sequence.canvas.SequenceCanvas#getDefaultMask() 
     */
    public final ColourMask DEFAULT_MASK = getDefaultMask();


    /**
     * Creates a new instance of a SequenceCanvas.  This is the most basic
     * constructor available.
     */
    public SequenceCanvas() {
        this(null);
    }

    /**
     * Creates a new instance of a SequenceCanvas.  This constructor will accept
     * a Map of values for BioLegato properties.
     **
     * @param props the BioLegato properties to initialize the canvas with.
     *              This parameter is ignored if null.
     */
    public SequenceCanvas(Map<? extends Object, ? extends Object> props) {
        super(props);

        // A counter used to ensure that menu items are inserted in the proper
        // order within each JMenu object.  This specific counter is used for
        // the "Edit" JMenu.
        int mcount = 0;

        //////////////////////////////////////
        //**********************************//
        //* ADD THE DEFAULT TOP MENU ITEMS *//
        //**********************************//
        //////////////////////////////////////
        // Add the "Open" button.
        addMenuHeading("File").insert(new JMenuItem(openAction), 0);

        // Add the "Save As..." button.
        addMenuHeading("File").insert(new JMenuItem(saveAsAction), 1);

        // Add the "Properties" button.
        addMenuHeading("File").insert(new JMenuItem(propertiesAction), 2);

        // Handle the read only property (i.e. prevent any possible
        // data manipulation if readonly is set to true!
        if (editable) {
            // NOTE: mcount++ increments count after retrieving its value for
            //       use in the below functions this choice of count++ allows
            //       variable length menus or simple re-ordering of choices.
            // NOTE: the parameter 1 is used to ensure that the Edit menu is
            //       inserted just left of the File menu.
            addMenuHeading(1,"Edit").insert(new JMenuItem(cutAct), mcount++);
            addMenuHeading("Edit").insert(new JMenuItem(copyAct), mcount++);
            addMenuHeading("Edit").insert(new JMenuItem(pasteAct), mcount++);
        }
        // Add menu items from the sequence list object.
        addMenuHeading("Edit").insert(groupMI, mcount++);
        addMenuHeading("Edit").insert(ungroupMI, mcount++);
        addMenuHeading("Edit").insert(getInfoMI, mcount++);
        addMenuHeading("Edit").insert(new JMenuItem(nameList.selectGroupAction),
                mcount++);
        addMenuHeading("Edit").insert(new JMenuItem(nameList.selectAllAction),
                mcount++);
        addMenuHeading("Edit").insert(new JMenuItem(changeCaseAction),
                mcount++);
        addMenuHeading("Edit").insert(selectByNameMenuItem, mcount++);

        // Create a new text area object.
        dataCollector = new SequenceTextArea(canvasSelf, datamodel);

        // Setup listeners for the sequence list.
        nameList.addListSelectionListener(canvasSelf);

        // Add the split text area menu item to the main text area's popup menu.
        dataCollector.popup.add(splitMenuItem);

        // Create the scroll pane for the main sequence text area.
        dataPane = new JScrollPane(dataCollector,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        // Create the scroll pane for the sequence list.
        JScrollPane listPane = new JScrollPane(nameList,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        // Initialize the preferred sizes for the sequence list and text area.
        dataPane.setPreferredSize(new Dimension(200, 150));
        listPane.setPreferredSize(new Dimension(100, 150));

        // Make the vertical scroll bars, for the sequence list and text area,
        // the same size.
        dataPane.setVerticalScrollBar(listPane.getVerticalScrollBar());

        // Add the scroll-pane enclosed sequence text area to the canvas.
        canvasPane.add(dataPane);

        // Create the main split pane, which splits the sequence list from the
        // sequence text area.
        JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                listPane, canvasPane);
        mainPane.setAlignmentX(JSplitPane.CENTER_ALIGNMENT);
        mainPane.setAlignmentY(JSplitPane.CENTER_ALIGNMENT);

        // Create the status bar for the sequence canvas.
        Box statusBar = new Box(BoxLayout.LINE_AXIS);
        statusBar.setAlignmentX(Box.LEFT_ALIGNMENT);
        statusBar.add(status);
        statusBar.add(insertStatus);

        // Add the main split pane (which splits the sequence list from the
        // sequence text area), a separator, and the status bar to the canvas.
        mainDisplay.add(mainPane, BorderLayout.PAGE_START);
        mainDisplay.add(new JSeparator(SwingConstants.HORIZONTAL));
        mainDisplay.add(statusBar, BorderLayout.PAGE_END);

        // Update the font, thereby setting the font in all of the text panes
        // and list panes associated with this canvas instance.
        updateFont(12, false);
    }

    /**
     * Starts a new instance of BioLegato, using the sequence canvas.
     **
     * @param args the command line arguments for BioLegato.
     */
    public static void main (String[] args) {
        BLMain.main(SequenceCanvas.class, args);
    }

    /**
     * Possible vestigial code!
     * This code is used to provide a plugin name for the canvas.  The purpose
     * of the plugin name system was to allow multiple canvases to be loaded
     * (via. BioLegato's plugin system), and then have the properties select
     * which canvas to use based on the plugin names.  This architecture has
     * since been replaced, and so this code may no longer be used.
     **
     * @return     the name to display for the canvas in
     *             all program text referring to it.
     * @deprecated vestigial code from a previous BioLegato framework structure.
     */
    public String getPluginName() {
        return "Sequence";
    }

    /**
     * This method receives cursor updates for use in the status bar, and
     * updates the row and column number displayed in the sequence canvas's
     * status bar.
     **
     * @param column the new column number/X-coordinate of the cursor within the
     *               sequence text area.
     * @param row    the new row number/Y-coordinate of the cursor within the
     *               sequence text area.
     */
    public void cursorChange(int column, int row) {
        status.setText("Row: " + (row + 1) + " Col: " + (column + 1));
    }

    /**
     * Thie method receives insertion mode change updates for use in the status
     * bar.  This method will update the status bar to show [INS] whenever the
     * text canvas is put into insertion mode.  Whenever the text canvas is not
     * in insertion mode, the [INS] will disappear.  This method will also
     * ensure, if the sequence text area is "split", that both sequence text
     * area objects will have synchronized insertion mode statuses.  This is
     * accomplished by calling each SequenceTextArea's insertionMode method.
     **
     * @param mode the new insertion mode status for the sequence text area.
     * @see org.biolegato.sequence.canvas.SequenceCanvas#insertStatus
     * @see org.biolegato.sequence.canvas.SequenceTextArea#insertionMode(boolean)
     */
    public void insertionMode(boolean mode) {
        // Update the status bar with the new insertion mode status.
        if (mode) {
            insertStatus.setText("[INS]");
        } else {
            insertStatus.setText("     ");
        }

        // Ensure that the insertion mode is consistent for both canvases, if
        // the canvas is split into two.
        if (altDataCollector != null) {
            dataCollector.insertionMode(mode);
            altDataCollector.insertionMode(mode);
        }
    }

    /**
     * This method listens to the sequence list, and deselects any text in the
     * sequence text area(s) whenever something is selected in the sequence
     * list.  This ensures that only one SequenceCanvasObject has selected
     * sequences (or text) at a time.  This method also adds the sequence list
     * specific commands to BioLegato's menu bar.  The current sequence list
     * specific commands are: group, ungroup, and get info.
     **
     * @param evt this parameter is currently ignored by this method.
     * @see org.biolegato.sequence.canvas.SequenceCanvasObject
     * @see org.biolegato.sequence.canvas.SequenceCanvas#groupMI
     * @see org.biolegato.sequence.canvas.SequenceCanvas#ungroupMI
     * @see org.biolegato.sequence.canvas.SequenceCanvas#getInfoMI
     * @see org.biolegato.sequence.canvas.SequenceList#groupAction
     * @see org.biolegato.sequence.canvas.SequenceList#ungroupAction
     * @see org.biolegato.sequence.canvas.SequenceList#getInfoAction
     * @see org.biolegato.sequence.canvas.SequenceTextArea#clearSelection()
     */
    public void valueChanged(ListSelectionEvent e) {
        // Ensure that the currently selected SequenceCanvasObject is not the
        // sequence list, before proceeding.  If the sequence list was
        // previously selected, then there is no point in executing the code
        // below, because there should be no text selected in the sequnece text
        // areas, and the sequelce list specific menu items should already be in
        // BioLegato's menu bar (under the "Edit" menu heading).
        if (currentPane != nameList) {
            // Update the currently focused SequenceCanvasObject pointer.
            currentPane = nameList;

            // clear the primary text pane's selection(s)
            dataCollector.clearSelection();

            // If the canvas is split, deselect any text in the the alternate
            // sequence text area object.
            if (altDataCollector != null) {
                altDataCollector.clearSelection();
            }

            // Add the menu items specific to the sequence list.  These items
            // are removed from BioLegato's menu bar whenever text is selected
            // in the sequence text area (because these commands are ONLY
            // applicable to sequences selected in the sequence list).
            addMenuHeading("Edit").insert(groupMI, 3);
            addMenuHeading("Edit").insert(ungroupMI, 4);
            addMenuHeading("Edit").insert(getInfoMI, 5);
        }
    }

    /**
     * Manages mutual exclusion between the textareas and the list
     * This method listens to the sequence text area, and deselects any
     * sequences in the sequence list, and the other text area (if the text area
     * is split), whenever something is selected in one of the sequence text
     * areas.  This ensures that only one SequenceCanvasObject has selected
     * sequences (or text) at a time.  This method also removes any sequence
     * list specific commands to BioLegato's menu bar, if they are currently
     * present.  The current sequence list specific commands are: group,
     * ungroup, and get info.
     **
     * @param source the source of the event -- this is used to differentiate
     *               between the main and alternate canvas, when the canvas is
     *               split in two.
     * @see org.biolegato.sequence.canvas.SequenceCanvasObject
     * @see org.biolegato.sequence.canvas.SequenceCanvas#groupMI
     * @see org.biolegato.sequence.canvas.SequenceCanvas#ungroupMI
     * @see org.biolegato.sequence.canvas.SequenceCanvas#getInfoMI
     * @see org.biolegato.sequence.canvas.SequenceList#groupAction
     * @see org.biolegato.sequence.canvas.SequenceList#ungroupAction
     * @see org.biolegato.sequence.canvas.SequenceList#getInfoAction
     * @see org.biolegato.sequence.canvas.SequenceList#clearSelection() 
     */
    public void selectionMade(SequenceTextArea source) {
        // Ensure that the "source" variable is valid (i.e. not null).  Also,
        // for efficiency, ensure that the user has actually switched
        // SequenceCanvasObjects (to demonstrate why this is important, suppose
        // that a user selects text in the main text area -- assume the text
        // area is not split -- this method will be called; then suppose the
        // user changes their selection in the text area, this method will be
        // called a second time, but the source will still be the main text
        // area, and thus the code in the if statement does not need to be
        // executed).
        if (source != null && currentPane != source) {
            // Update the currently focused SequenceCanvasObject pointer.
            currentPane = source;

            // Deselect anything selected in the sequence list.
            nameList.clearSelection();

            // Determine whether the selection made was from the main text area,
            // or the secondary text canvas.  This code is only here to handle
            // the situation where the canvas is split -- i.e. there are two
            // text areas: a primary/main and a secondary/alternate.
            //
            // If text was previously selected in the other text area, it will
            // be deselected.  For example, if the user first selects some text
            // in the main text area, then selects some text in the alternate
            // text area, the text in the main text area will be deselected;
            // thus, preventing text to be selected in both text areas
            // simultaneously.
            if (altDataCollector != null && source != altDataCollector) {
                altDataCollector.clearSelection();
            } else {
                dataCollector.clearSelection();
            }

            // Remove all of the menu items that do not apply to sequence text
            // areas.  Please note that these menu items will be re-added when a
            // sequence is selected in the sequence list.  Also, note that if
            // these menu items are not present, errors will NOT be generated;
            // thus, it is safe to execute the below code, even if the previous
            // selection was the other text area (in a split text area).
            addMenuHeading("Edit").remove(groupMI);
            addMenuHeading("Edit").remove(ungroupMI);
            addMenuHeading("Edit").remove(getInfoMI);
        }
    }

    /**
     * Coordinates text length changes between split text areas.  This method is
     * called by the Dataset sequence object container's insert and delete
     * methods to ensure that there is consistency between the scroll bars in
     * the main and alternate text areas (in a split text area).  This will also
     * ensure that the area affected will be repainted.
     **
     * @param x the text X-coordinate (column) the change occurred.
     * @param y the text Y-coordinate (row) the change occurred.
     * @param length the length of text length changed (can be negative).
     */
    public static void textLengthChanged(int x, int y, int length) {
        // Ensure that the text length change event is conveyed to the main
        // sequence text area.
        dataCollector.textLengthChanged(x, y, length);

        // If the text area is split, then also convey the text length change
        // to the secondary sequence text area.
        if (altDataCollector != null) {
            altDataCollector.textLengthChanged(x, y, length);
        }
    }

    /**
     * Updates the font for the Sequence canvas and its sub-objects
     **
     * @param size the new font size
     * @param bold whether to bold the font
     */
    public final void updateFont(int size, boolean bold) {
        // Set the current font of the canvas, taking into account the
        // parameters passed (font size and whether to bold), and the relative
        // font family of the current font ("Monospaced" by default).
        currentFont
                = currentFont.deriveFont((bold ? Font.BOLD : Font.PLAIN), size);

        // Update the main sequence text area's font.
        dataCollector.setFont(currentFont);

        // Change the current font for the sequence list, and update the row
        // height for the font - i.e. java does not set the hight properly for
        // the sequence list on its own (the margins need to be set properly,
        // otherwise the sequence list will be out of alignment with all of the
        // sequence text areas).
        nameList.setFont(currentFont);
        nameList.setFixedCellHeight(nameList
                .getFontMetrics(currentFont).getHeight());
        
        // Repaint the sequence list.
        nameList.repaint();

        // If the text area is split into two, be sure to update the second text
        // area's font, too!
        if (altDataCollector != null) {
            altDataCollector.setFont(currentFont);
        }
    }

    /**
     * Writes data out from the canvas to an Appendable object.  This method
     * just converts the file format from a string to a DataFormat object
     * (using DataFormat.getFormat), then calls: writeFile(DataFormat,
     * Appendable, boolean).
     **
     * @param  formatString the file format to use for writing the file.
     * @param  out          the "file" (or stream) to write out to.
     * @param  forceall     write the entire contents of the canvas
     *                      instead of just the currently selected
     *                      sequences in the canvas.
     * @throws IOException  if an error occurs while writing
     * @see org.biolegato.sequence.data.DataFormat#getFormat(java.lang.String)
     */
    public void writeFile(String formatString, Appendable out, boolean forceall)
                                                            throws IOException {
        writeFile(DataFormat.getFormat(formatString), out, forceall);
    }
    
    /**
     * Writes data out from the canvas.
     **
     * @param  format      the file format to use for writing the file.
     * @param  out         the "file" (or stream) to write out to.
     * @param  forceall    write the entire contents of the canvas
     *                     instead of just the currently selected
     *                     sequences in the canvas.
     * @throws IOException if an error occurs while writing
     * @see org.biolegato.sequence.data.DataFormat#autodetect(java.util.Scanner)
     * @see org.biolegato.sequence.data.DataFormat#convertTo(java.lang.Appendable, org.biolegato.sequence.data.Dataset, int)
     * @see org.biolegato.sequence.canvas.SequenceCanvasObject#writeOut(org.biolegato.sequence.data.DataFormat, java.lang.Appendable) 
     */
    public void writeFile(DataFormat format, Appendable out, boolean forceall)
                                                            throws IOException {
        final int maxlines = datamodel.getSize();

        // Ensure that the format is supported by the canvas.
        if (format != null) {
            // If forceall is set to true, do NOT obtain the file content from
            // the any text or sequences selecte in the canvas.  Instead, dump
            // the contents of the entire canvas (i.e. not just the data
            // selected) into the Appendable object.
            if (forceall) {
                // Convert and write the data to the Appendable object.
                for (int count = 0; count < maxlines; count++) {
                    format.convertTo(out, datamodel, count);
                }
            } else if (currentPane != null) {
                // Convert and write the current selection to the Appendable
                // object.
                currentPane.writeOut(format, out);
            }
        } else {
            error("Unsupported file format", "SequenceCanvas");
        }
    }

    /**
     * Reads a data into the canvas.  This method just converts the file
     * format from a string to a DataFormat object (using DataFormat.getFormat),
     * then calls: readFile(DataFormat, Appendable, boolean).  The only
     * exception is if the formatString parameter is "".  In that case, the null
     * value is used as the format for the readFile(DataFormat, Appendable,
     * boolean) version of the method.  The null DataFormat  value indicates
     * that auto-detection should occur.
     **
     * @param  formatString the file format to use for parsing the file.  If the
     *                      string "" is passed, the PCDIO object should auto-
     *                      detect the format of the data.
     * @param  in           the "file" (or stream) to read in from.
     * @param  overwrite    whether to overwrite the currently selected
     *                      data in the current canvas with the data
     *                      being imported by this function/method.
     * @throws IOException  if an error occurs while reading
     * @see org.biolegato.sequence.data.DataFormat#getFormat(java.lang.String)
     */
    public void readFile(String formatString, Reader in, boolean overwrite, boolean forceall)
                                                            throws IOException {
        // Handles autodetection.  Whenever a blank or null format string is
        // passed to this method, autodetection will be performed; however, the
        // null value behaviour should remain undocumented, because "" is the
        // preferred way to indicate autodetection should occur.
        if (formatString == null || "".equals(formatString)) {
            // Autodeted the data format
            readFile((DataFormat) null, in, overwrite,forceall);
        } else {
            // Retrieve the data format object that corresponds to the
            // data format string passed.
            readFile(DataFormat.getFormat(formatString), in, overwrite,forceall);
        }
    }

    /**
     * Reads a data into the canvas.
     **
     * @param  formatString the file format to use for parsing the file.  If the
     *                      null value is passed, then the data format should be
     *                      auto detected.
     * @param  in           the "file" (or stream) to read in from.
     * @param  overwrite    whether to overwrite the currently selected
     *                      data in the current canvas with the data
     *                      being imported by this function/method.
     * @throws IOException  if an error occurs while reading
     * @see java.util.Scanner
     * @see org.biolegato.sequence.data.DataFormat#autodetect(java.util.Scanner)
     * @see org.biolegato.sequence.data.DataFormat#convertFrom(org.biolegato.sequence.data.Dataset, java.util.Scanner, int, int)
     * @see org.biolegato.sequence.canvas.SequenceCanvasObject#readIn(org.biolegato.sequence.data.DataFormat, java.util.Scanner, boolean)
     */
    private void readFile(DataFormat format, Reader in, boolean overwrite, boolean forceall)
                                                            throws IOException {
        Scanner scanner = null;

        // Create a scanner object to pass to the data format parser, thereby
        // simplifying the whole parsing process.  This scanner will read the
        // file in a similar manner to the BufferedReader class, except that the
        // scanner can use different delimiters and pattern matching.  Be sure
        // to always set your delimiter within parsing methods.
        scanner = new Scanner(in);

        // Perform format autodetection, if the format parameter is null.
        if (format == null) {
            format = DataFormat.autodetect(scanner);
        }

        // Handle the overwrite parameter.  If the overwrite flag parameter is
        // set to true, the current selection should be overwritten (via.
        // calling the current canvas pane's read method).  If the overwrite
        // flag is set to false, then the sequences should, instead, be added to
        // the end of the data storage model.
        if (overwrite) {
            currentPane.readIn(format, scanner, overwrite);
        } else {
            format.convertFrom(datamodel, scanner, 0, datamodel.getSize());
        }
    }

    /**
     * Displays the canvas.  This is done by returning the SequenceCanvas class
     * variable 'mainDisplay'.
     **
     * @return a component representing the canvas (visually)
     * @see org.biolegato.sequence.canvas.SequenceCanvas#mainDisplay
     */
    public Component display () {
        return mainDisplay;
    }

///////////////////////////
//***********************//
//* CLIPBOARD FUNCTIONS *//
//***********************//
///////////////////////////
    /**
     * Pastes the current contents of the clipboard (null if empty) into the
     * canvas at the current cursor position (within either the text area or
     * the sequence list).
     **
     * @see org.biolegato.sequence.data.DataFormat#autodetect(java.util.Scanner)
     * @see org.biolegato.sequence.data.DataFormat#convertFrom(org.biolegato.sequence.data.Dataset, java.util.Scanner, int, int)
     * @see org.biolegato.sequence.canvas.SequenceCanvasObject#readIn(org.biolegato.sequence.data.DataFormat, java.util.Scanner, boolean)
     */
    public void pasteClipboard() {
        // The format parser to use for the clipboard data with.
        DataFormat format;
        // The scanner object to pass to the data format parser.  This is used
        // to simplify the parsing methods, such as auto-detection.
        Scanner scanner;
        // The transferrable object to assist in retrieving information from
        // the clipboard.
        Transferable t;

        // Handle the read only property (i.e. prevent any possible
        // data manipulation if readonly is set to true!)
        if (editable) {
            // Obtain the Transferrable object to help read from the clipboard.
            // Transferrable objects are used by java to perform both clipboard
            // and drag-and-drop operations.
            t = Toolkit.getDefaultToolkit()
                    .getSystemClipboard().getContents(null);

            try {
                // Ensure that the Transferrable object is not null
                if (t != null) {
                    if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        // Create a new scanner object to parse the clipboard
                        // data with.
                        scanner = new Scanner((String)
                                t.getTransferData(DataFlavor.stringFlavor));

                        // TODO: add caret position update functionality?

                        // Obtain the data format of the clipboard data
                        // via autodetection.
                        format = DataFormat.autodetect(scanner);

                        // As long as we understand (can dected) the format
                        // properly, read in the file using the correct
                        // data format parser.  (If we didn't detect an
                        // understandable data format, the "format"
                        // variable will be null.)
                        if (format != null) {
                            // DO NOT OVERWRITE SELECTIONS IN THE SEQUENCE LIST!
                            // (TODO: evaluate whether this should be changed.)
                            currentPane.readIn(format, scanner,
                                    (currentPane != nameList));
                        }
                    }
                }
            } catch (UnsupportedFlavorException e) {
            } catch (IOException e) {
            }
        }
    }

    /**
     * Copies the currently selected sequences or text from the sequence canvas
     * to the clipboard.  The format used for copying data to the clipboard is
     * the GenBANK format.  This is to ensure that as many features as possible
     * are preserved when copying.
     **
     * @see org.biolegato.sequence.data.DataFormat#GENBANK
     * @see org.biolegato.sequence.data.GenBankFile2008
     * @see org.biolegato.sequence.canvas.SequenceCanvasObject#writeOut(org.biolegato.sequence.data.DataFormat, java.lang.Appendable)
     */
    public void copyClipboard() {
        // The string builder used to store the convertd canvas data (instead
        // of a file stream) from the writeOut method.  This data is later
        // passed to the clipboard.  The StringBuilder object was chosen because
        // of its speed, and because it implements the Appendable interface.
        StringBuilder result = new StringBuilder();

        try {
            // Write the canvas data to the StringBuilder object.
            currentPane.writeOut(DataFormat.GENBANK, result);

            // Update the clipboard to store the same data in the StringBuilder
            // object.
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(result.toString()), null);
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }

    /**
     * Load the default colour masks into BioLegato (this was the only way I was
     * able to get the default colour mask to load properly and efficiently).
     **
     * @return the default colour mask object (null if problems occur).
     */
    public ColourMask getDefaultMask() {
        ColourMask result = null;

        // If the colour mask list contains masks, then search it for a
        // "default" mask (i.e. a mask with the name "default").
        if (colourMasks != null) {
            for (ColourMask c : colourMasks) {
                // If a default mask is found, set the return object to the
                // found mask, and break the for loop.
                if ("default".equalsIgnoreCase(c.toString())) {
                    result = c;
                    break;
                }
            }
        }

        // Return the results of the search.
        return result;
    }
}
