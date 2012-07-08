package net.pms.di.modules;

import net.pms.PmsCoreImpl;
import net.pms.api.PmsConfiguration;
import net.pms.api.PmsCore;
import net.pms.configuration.PmsConfigurationImpl;

import com.google.inject.AbstractModule;

public class CoreModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(PmsCore.class).to(PmsCoreImpl.class);

		bind(PmsConfiguration.class).to(PmsConfigurationImpl.class);
	}

}
