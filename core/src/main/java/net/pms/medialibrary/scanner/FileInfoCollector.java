package net.pms.medialibrary.scanner;

import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOManagedFile;

import com.google.common.base.Optional;

public interface FileInfoCollector {

	/**
	 * Analyze.
	 *
	 * @param mf a managed file containing the path to a file
	 * @param sourceFileInfo the file info to update
	 * @return true, if the analysis was successful
	 */
	public boolean analyzeAndUpdate(DOManagedFile mf, DOFileInfo sourceFileInfo);

	/**
	 * Analyzes the file if mf is a file or all files contained in the folder if
	 * mf is a folder.
	 * 
	 * @param mf a managed file containing the path to a file or folder
	 * @return the analyzed file info
	 */
	public Optional<DOFileInfo> analyze(DOManagedFile mf);
}
