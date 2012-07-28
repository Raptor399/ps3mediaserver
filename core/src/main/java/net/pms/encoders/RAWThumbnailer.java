package net.pms.encoders;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JComponent;

import net.pms.PMS;
import net.pms.api.PmsConfiguration;
import net.pms.api.io.ProcessWrapperFactory;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.formats.Format;
import net.pms.io.InternalJavaProcessImpl;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapper;

@Singleton
public class RAWThumbnailer extends Player {
	public final static String ID = "rawthumbs";

	private static ProcessWrapperFactory processWrapperFactory;
	private static PmsConfiguration configuration;

	@Inject
	RAWThumbnailer(PmsConfiguration configuration,
			ProcessWrapperFactory processWrapperFactory) {
		this.configuration = configuration;
		this.processWrapperFactory = processWrapperFactory;
	}

	protected String[] getDefaultArgs() {
		return new String[]{"-e", "-c"};
	}

	@Override
	public String[] args() {
		return getDefaultArgs();

	}

	@Override
	public JComponent config() {
		return null;
	}

	@Override
	public String executable() {
		return configuration.getDCRawPath();
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public ProcessWrapper launchTranscode(String fileName, DLNAResource dlna, DLNAMediaInfo media,
		OutputParams params) throws IOException {

		params.waitbeforestart = 1;
		params.minBufferSize = 1;
		params.maxBufferSize = 5;
		params.hidebuffer = true;

		if (media == null || media.getThumb() == null) {
			return null;
		}

		if (media.getThumb().length == 0) {
			try {
				media.setThumb(getThumbnail(params, fileName));
			} catch (Exception e) {
				return null;
			}
		}

		byte copy[] = new byte[media.getThumb().length];
		System.arraycopy(media.getThumb(), 0, copy, 0, media.getThumb().length);
		media.setThumb(new byte[0]);

		ProcessWrapper pw = new InternalJavaProcessImpl(new ByteArrayInputStream(copy));
		return pw;
	}

	@Override
	public String mimeType() {
		return "image/jpeg";
	}

	@Override
	public String name() {
		return "dcraw Thumbnailer";
	}

	@Override
	public int purpose() {
		return MISC_PLAYER;
	}

	@Override
	public int type() {
		return Format.IMAGE;
	}

	public byte[] getThumbnail(OutputParams params, String fileName) throws Exception {
		params.log = false;

		String cmdArray[] = new String[4];
		cmdArray[0] = configuration.getDCRawPath();
		cmdArray[1] = "-e";
		cmdArray[2] = "-c";
		cmdArray[3] = fileName;
		ProcessWrapper pw = processWrapperFactory.create(cmdArray, params);
		pw.runInSameThread();


		InputStream is = pw.getInputStream(0);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int n = -1;
		byte buffer[] = new byte[4096];
		while ((n = is.read(buffer)) > -1) {
			baos.write(buffer, 0, n);
		}
		is.close();
		byte b[] = baos.toByteArray();
		baos.close();
		return b;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCompatible(DLNAMediaInfo mediaInfo) {
		if (mediaInfo != null) {
			// TODO: Determine compatibility based on mediaInfo
			return false;
		} else {
			// No information available
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCompatible(Format format) {
		if (format != null && format.getType() == Format.AUDIO) {
			Format.Identifier id = format.getIdentifier();

			if (id.equals(Format.Identifier.RAW)) {
				return true;
			}
		}

		return false;
	}
}
