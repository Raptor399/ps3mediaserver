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

import net.pms.PMS;
import net.pms.PmsCoreImpl;
import net.pms.api.PmsConfiguration;
import net.pms.api.PmsCore;
import net.pms.api.io.BufferedOutputFileFactory;
import net.pms.configuration.PmsConfigurationImpl;
import net.pms.io.BufferedOutputFile;
import net.pms.io.BufferedOutputFileImpl;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class CoreModule extends AbstractModule {

	@Override
	protected void configure() {
		// This is here to help with the transition to DI
		requestStaticInjection(PMS.class);

		bind(PmsCore.class).to(PmsCoreImpl.class);

		bind(PmsConfiguration.class).to(PmsConfigurationImpl.class);

		install(new FactoryModuleBuilder()
				.implement(BufferedOutputFile.class, BufferedOutputFileImpl.class).build(
						BufferedOutputFileFactory.class));
	}

}
