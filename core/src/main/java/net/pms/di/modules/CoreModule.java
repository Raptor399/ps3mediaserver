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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package net.pms.di.modules;

import net.pms.PmsCoreImpl;
import net.pms.api.PmsConfiguration;
import net.pms.api.PmsCore;
import net.pms.api.encoders.AviDemuxerInputStreamFactory;
import net.pms.api.io.BufferedOutputFileFactory;
import net.pms.api.io.PipeIPCProcessFactory;
import net.pms.api.io.PipeProcessFactory;
import net.pms.api.io.ProcessWrapperFactory;
import net.pms.configuration.PmsConfigurationImpl;
import net.pms.dlna.CueFolder;
import net.pms.dlna.DLNAMediaDatabase;
import net.pms.dlna.DVDISOFile;
import net.pms.dlna.DVDISOTitle;
import net.pms.dlna.PlaylistFolder;
import net.pms.dlna.RarredEntry;
import net.pms.dlna.RarredFile;
import net.pms.dlna.RealFile;
import net.pms.dlna.ZippedFile;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.dlna.virtual.VirtualVideoAction;
import net.pms.encoders.AviDemuxerInputStream;
import net.pms.encoders.PlayerFactory;
import net.pms.io.BufferedOutputFile;
import net.pms.io.BufferedOutputFileImpl;
import net.pms.io.PipeIPCProcess;
import net.pms.io.PipeProcess;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;
import net.pms.logging.FrameAppender;
import net.pms.medialibrary.gui.tab.libraryview.FileDisplayTable;
import net.pms.medialibrary.gui.tab.libraryview.FileDisplayer;
import net.pms.network.HTTPServer;
import net.pms.network.HttpServerPipelineFactory;
import net.pms.network.Request;
import net.pms.network.RequestHandler;
import net.pms.network.RequestHandlerV2;
import net.pms.network.RequestV2;
import net.pms.network.UPNPHelper;
import net.pms.newgui.plugins.PluginAboutPanel;
import net.pms.newgui.plugins.PluginDetailDialog;
import net.pms.newgui.plugins.PluginGroupPanel;
import net.pms.util.CodecUtil;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class CoreModule extends AbstractModule {
	@Override
	protected void configure() {
		// This is here to help with the transition to DI
		requestStaticInjection(UPNPHelper.class);
		requestStaticInjection(CodecUtil.class);
		requestStaticInjection(PlayerFactory.class);
		requestStaticInjection(FrameAppender.class);

		bind(PmsCore.class).to(PmsCoreImpl.class);

		bind(PmsConfiguration.class).to(PmsConfigurationImpl.class);

		install(new FactoryModuleBuilder()
				.implement(AviDemuxerInputStream.class, AviDemuxerInputStream.class)
				.build(AviDemuxerInputStreamFactory.class));

		install(new FactoryModuleBuilder()
			.implement(BufferedOutputFile.class, BufferedOutputFileImpl.class)
			.build(BufferedOutputFileFactory.class));

		install(new FactoryModuleBuilder()
				.implement(CueFolder.class, CueFolder.class)
				.build(CueFolder.Factory.class));

		install(new FactoryModuleBuilder()
				.implement(DLNAMediaDatabase.class, DLNAMediaDatabase.class)
				.build(DLNAMediaDatabase.Factory.class));

		install(new FactoryModuleBuilder()
				.implement(DVDISOFile.class, DVDISOFile.class)
				.build(DVDISOFile.Factory.class));

		install(new FactoryModuleBuilder()
				.implement(DVDISOTitle.class, DVDISOTitle.class)
				.build(DVDISOTitle.Factory.class));

		install(new FactoryModuleBuilder()
				.implement(FileDisplayer.class, FileDisplayer.class)
				.build(FileDisplayer.Factory.class));

		install(new FactoryModuleBuilder()
				.implement(FileDisplayTable.class, FileDisplayTable.class)
				.build(FileDisplayTable.Factory.class));

		install(new FactoryModuleBuilder()
				.implement(HttpServerPipelineFactory.class, HttpServerPipelineFactory.class)
				.build(HttpServerPipelineFactory.Factory.class));

		install(new FactoryModuleBuilder()
				.implement(HTTPServer.class, HTTPServer.class)
				.build(HTTPServer.Factory.class));
		
		install(new FactoryModuleBuilder()
				.implement(PipeIPCProcess.class, PipeIPCProcess.class)
				.build(PipeIPCProcessFactory.class));

		install(new FactoryModuleBuilder()
				.implement(PipeProcess.class, PipeProcess.class)
				.build(PipeProcessFactory.class));

		install(new FactoryModuleBuilder()
				.implement(PlaylistFolder.class, PlaylistFolder.class)
				.build(PlaylistFolder.Factory.class));

		install(new FactoryModuleBuilder()
				.implement(PluginAboutPanel.class, PluginAboutPanel.class)
				.build(PluginAboutPanel.Factory.class));

		install(new FactoryModuleBuilder()
				.implement(PluginDetailDialog.class, PluginDetailDialog.class)
				.build(PluginDetailDialog.Factory.class));

		install(new FactoryModuleBuilder()
				.implement(PluginGroupPanel.class, PluginGroupPanel.class)
				.build(PluginGroupPanel.Factory.class));

		install(new FactoryModuleBuilder()
				.implement(ProcessWrapper.class, ProcessWrapperImpl.class)
				.build(ProcessWrapperFactory.class));

		install(new FactoryModuleBuilder()
				.implement(RarredEntry.class, RarredEntry.class)
				.build(RarredEntry.Factory.class));

		install(new FactoryModuleBuilder()
				.implement(RarredFile.class, RarredFile.class)
				.build(RarredFile.Factory.class));

		install(new FactoryModuleBuilder()
				.implement(RealFile.class, RealFile.class)
				.build(RealFile.Factory.class));

		install(new FactoryModuleBuilder()
				.implement(Request.class, Request.class)
				.build(Request.Factory.class));

		install(new FactoryModuleBuilder()
				.implement(RequestHandler.class, RequestHandler.class)
				.build(RequestHandler.Factory.class));

		install(new FactoryModuleBuilder()
				.implement(RequestHandlerV2.class, RequestHandlerV2.class)
				.build(RequestHandlerV2.Factory.class));

		install(new FactoryModuleBuilder()
				.implement(RequestV2.class, RequestV2.class)
				.build(RequestV2.Factory.class));

		install(new FactoryModuleBuilder()
				.implement(VirtualFolder.class, VirtualFolder.class)
				.build(VirtualFolder.Factory.class));

		install(new FactoryModuleBuilder()
				.implement(VirtualVideoAction.class, VirtualVideoAction.class)
				.build(VirtualVideoAction.Factory.class));

		install(new FactoryModuleBuilder()
				.implement(ZippedFile.class, ZippedFile.class)
				.build(ZippedFile.Factory.class));
	}
}
