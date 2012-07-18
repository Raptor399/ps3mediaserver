/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package net.pms;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.IOException;

import javax.inject.Inject;
import javax.swing.JOptionPane;

import net.pms.api.PmsConfiguration;
import net.pms.api.PmsCore;
import net.pms.configuration.PmsConfigurationImpl;
import net.pms.di.PmsGuice;
import net.pms.logging.LoggingConfigFileLoader;
import net.pms.newgui.ProfileChooser;
import net.pms.util.PropertiesUtil;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

/**
 * This class takes care of initializing and starting up PMS.
 *
 * FIXME: This class should not extend PmsCoreImpl. For now, during the
 * process rewriting the code, it does so anyway to allow easy development.
 * Once rewriting is finished and the rest of the code has no direct
 * dependency to PMS, the "extends PmsCoreImpl" must be removed.
 */
public class PMS {
	/** The logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(PMS.class);

	@Inject
	public static PmsCore pmsCore;

	private static final String SCROLLBARS = "scrollbars";
	private static final String NATIVELOOK = "nativelook";
	private static final String CONSOLE = "console";
	private static final String NOCONSOLE = "noconsole";
	private static final String PROFILES = "profiles";

	/**
	 * This class is not supposed to be instantiated.
	 * Use {@link #get()} instead.
	 */
	private PMS() {
	}

	/**
	 * Main method called on program start takes care of loading the correct
	 * configuration and creating the singleton PMS instance.
	 *
	 * @param args The command line arguments.
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public static void main(final String args[]) throws IOException, ConfigurationException {
		boolean displayProfileChooser = false;
		boolean headless = true;

		// Initialize the Guice dependency injector
		Injector injector = new PmsGuice().getInjector();

		if (args.length > 0) {
			for (int a = 0; a < args.length; a++) {
				if (args[a].equals(CONSOLE)) {
					System.setProperty(CONSOLE, Boolean.toString(true));
				} else if (args[a].equals(NATIVELOOK)) {
					System.setProperty(NATIVELOOK, Boolean.toString(true));
				} else if (args[a].equals(SCROLLBARS)) {
					System.setProperty(SCROLLBARS, Boolean.toString(true));
				} else if (args[a].equals(NOCONSOLE)) {
					System.setProperty(NOCONSOLE, Boolean.toString(true));
				} else if (args[a].equals(PROFILES)) {
					displayProfileChooser = true;
				}
			}
		}

		try {
			Toolkit.getDefaultToolkit();
			if (GraphicsEnvironment.isHeadless()) {
				if (System.getProperty(NOCONSOLE) == null) {
					System.setProperty(CONSOLE, Boolean.toString(true));
				}
			} else {
				headless = false;
			}
		} catch (final Throwable t) {
			System.err.println("Toolkit error: " + t.getMessage());
			if (System.getProperty(NOCONSOLE) == null) {
				System.setProperty(CONSOLE, Boolean.toString(true));
			}
		}

		if (!headless && displayProfileChooser) {
			ProfileChooser.display();
		}

		try {
			PmsConfiguration configuration = injector.getInstance(PmsConfigurationImpl.class);

			assert getConfiguration() != null;

			// Load the (optional) logback config file. This has to be called after 'new PmsConfiguration'
			// as the logging starts immediately and some filters need the PmsConfiguration.
			LoggingConfigFileLoader.load();

			try {
				PmsCore pmsCore = injector.getInstance(PmsCore.class);

				if (pmsCore.init()) {
					LOGGER.info("The server should now appear on your renderer");
				} else {
					LOGGER.error("A serious error occurred during PMS init");
				}
			} catch (final Exception e) {
				LOGGER.error("A serious error occurred during PMS init", e);
			}
		} catch (final Throwable t) {
			System.err.println("Configuration error: " + t.getMessage());
			LOGGER.error("Configuration error", t);
			JOptionPane.showMessageDialog(null, "Configuration error:"+t.getMessage(), "Error initalizing PMS!", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Returns the singleton instance of the PMS core.
	 *
	 * @return The PMS instance.
	 */
	public static PmsCore get() {
		return pmsCore;
	}

	/**
	 * Returns the singleton instance of the PMS configuration object.
	 *
	 * @return The configuration instance.
	 */
	public static PmsConfiguration getConfiguration() {
		return pmsCore.getConfiguration();
	}

	/**
	 * Returns the build version of PMS.
	 *
	 * @return The build version.
	 */
	public static String getVersion() {
		return pmsCore.getVersion();
	}
}
