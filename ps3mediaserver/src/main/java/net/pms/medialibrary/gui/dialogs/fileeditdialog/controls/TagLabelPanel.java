package net.pms.medialibrary.gui.dialogs.fileeditdialog.controls;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.pms.medialibrary.gui.shared.TagLabel;
import net.pms.medialibrary.gui.shared.WrapLayout;

/**
 * This panel is being used to show a list of TagLabels. Besides the tags, an add button to add more tags is being 
 * @author pw
 *
 */
public class TagLabelPanel extends JPanel {
	private static final long serialVersionUID = 4440237111852696163L;

	private static ImageIcon iiAdd;
	
	//holds the listeners subscribing for delete event notifications
	private List<ActionListener> tagLabelListeners = new ArrayList<ActionListener>();
	private TagLabel tlEditing;

	private JPanel pTagValues;
	
	/**
	 * Constructor
	 * @param tagValues the initial tag values
	 */
	public TagLabelPanel(List<String> tagValues) {
		setLayout(new BorderLayout(3, 0));
		
		//add add button
		initImageIcons();
		JLabel lAdd = new JLabel(iiAdd);
		lAdd.setToolTipText("Add");
		lAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
		lAdd.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				createNewLabel();
			}
		});
		add(lAdd, BorderLayout.LINE_START);
		
		//add all the tag values
		pTagValues = new JPanel(new WrapLayout(FlowLayout.LEFT, 0, 0));
		for(String tagValue : tagValues) {
			final TagLabel tl = createTagLabel(tagValue);
			pTagValues.add(tl);
		}
		add(pTagValues, BorderLayout.CENTER);
		refreshCommas();
	}
	
	/**
	 * Adds a new label at the end
	 */
	private void createNewLabel() {
		TagLabel tl = createTagLabel("");
		pTagValues.add(tl);
		refreshCommas();
		tlEditing = tl;
		tl.beginEdit();
	}

	/**
	 * A TagLabelListener will be notified of all TagLabel events
	 * These are: Delete, BeginEdit, EndEdit, Layout and NewTagValue
	 * @param l the listener to be notified
	 */
	public void addTagLabelListener(ActionListener l) {
		tagLabelListeners.add(l);
	}

	/**
	 * Gets the label currently being edited or null if none is being edited
	 * @return the label being edited or null
	 */
	public TagLabel getEditingLabel() {
		return tlEditing;
	}
	
	/**
	 * Gets the configured tag values
	 * @return the list of configured tag values
	 */
	public List<String> getTagValues() {
		List<String> res = new ArrayList<String>();
		for(int i = 0; i < pTagValues.getComponentCount(); i++) {
			JComponent c = (JComponent) pTagValues.getComponent(i);
			if(c instanceof TagLabel) {
				String tv = ((TagLabel)c).getTagValue();
				if(!tv.equals("")) {
					//only add not empty values (can happen if getting the values while a new label is being edited)
					res.add(tv);
				}
			}
		}
		return res;
	}

	/**
	 * Lazy-initializes the static image icons
	 */
	private void initImageIcons() {
		//lazy initialize icons only once
		if(iiAdd == null) {
			try {
				iiAdd = new ImageIcon(ImageIO.read(getClass().getResource("/resources/images/add-12.png")));
			} catch (IOException e1) {
				//do nothing
			}
		}
	}

	/**
	 * Tells all the labels to draw a comma after the tag value, except for the last one
	 */
	private void refreshCommas() {
		for(int i = 0; i < pTagValues.getComponentCount(); i++) {
			JComponent c = (JComponent) pTagValues.getComponent(i);
			if(c instanceof TagLabel) {
				((TagLabel)c).setAddComma(i < pTagValues.getComponentCount() - 1);
			}
		}
	}

	/**
	 * Notifies all subscribed listeners using actionPerformed
	 * @param command the ActionEvent command
	 */
	private void fireActionEvent(String command) {
		for(ActionListener l : tagLabelListeners) {
			l.actionPerformed(new ActionEvent(this, 0, command));
		}
	}
	
	/**
	 * Creates a new TagLabel with the given tag value and subscribes to its events
	 * @param tagValue the text to show in the TagLabel
	 * @return the created TagLabel
	 */
	private TagLabel createTagLabel(String tagValue) {
		final TagLabel tl = new TagLabel(tagValue);
		tl.addTagLabelListener(new ActionListener() {					
		@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals(TagLabel.ACTION_COMMAND_BEGIN_EDIT)) {
					tlEditing = tl;
				} else if (e.getActionCommand().equals(TagLabel.ACTION_COMMAND_DELETE)) {
					pTagValues.remove(tl);
					refreshCommas();
				} else if (e.getActionCommand().equals(TagLabel.ACTION_COMMAND_END_EDIT)) {
					tlEditing = null;
				} else if (e.getActionCommand().equals(TagLabel.ACTION_COMMAND_NEW_TAGVALUE)) {
					tlEditing.applyEdit();
					createNewLabel();
				}
				//propagate every event
				fireActionEvent(e.getActionCommand());
			}
		});
		return tl;
	}
 }