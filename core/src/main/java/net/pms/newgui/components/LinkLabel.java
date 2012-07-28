package net.pms.newgui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

import net.pms.api.PmsCore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * A label looking like a link. When clicked, the link is being opened in the
 * default browser
 * 
 * @author pw
 */
public class LinkLabel extends JLabel {
	private static final long serialVersionUID = 7773644902756548797L;
	private static final Logger log = LoggerFactory.getLogger(LinkLabel.class);
	private String link;
	private String text;
	private final PmsCore pmsCore;

	/**
	 * Interface for a factory to be generated by Guice, using <a
	 * href="https://code.google.com/p/google-guice/wiki/AssistedInject">
	 * AssistedInject</a>.
	 */
	public interface Factory {
		public LinkLabel create(@Assisted("text") String text, @Assisted("link") String link);
	}

	/**
	 * Creates a new link label
	 * @param text the text to display
	 * @param link the link to open when clicked
	 */
	@AssistedInject
	public LinkLabel(PmsCore core, @Assisted("text") String text, @Assisted("link") String link) {
		pmsCore = core;
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setForeground(Color.BLUE);
		
		this.text = text;
		setLink(link);
		setText(text);

		// format the text to make it look like a link

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(isEnabled()) {
					try {
						pmsCore.getRegistry().browseURI(getLink());
					} catch (Exception ex) {
						log.debug("Failed to open url in web browser", ex);
					}
				}
			}
		});
	}

	@Override
	public void setText(String text) {
		String newText;
		if(isEnabled()) {
			newText = text;
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("<html>");
			sb.append("<a href=\"");
			sb.append(link);
			sb.append("\">");
			sb.append(text);
			sb.append("</a>");
			sb.append("</html>");	
			newText = sb.toString();
		}
		super.setText(newText);
	}

	/**
	 * Gets the link.
	 *
	 * @return the link
	 */
	public String getLink() {
		return link;
	}

	/**
	 * Sets the link.
	 *
	 * @param link the new link
	 */
	public void setLink(String link) {
		this.link = link;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		setText(text);
		super.setEnabled(enabled);
	}
}
