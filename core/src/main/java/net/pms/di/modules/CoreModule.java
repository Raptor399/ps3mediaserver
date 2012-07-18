package net.pms.di.modules;

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
		bind(PmsCore.class).to(PmsCoreImpl.class);

		bind(PmsConfiguration.class).to(PmsConfigurationImpl.class);

		install(new FactoryModuleBuilder()
				.implement(BufferedOutputFile.class, BufferedOutputFileImpl.class).build(
						BufferedOutputFileFactory.class));
	}

}
