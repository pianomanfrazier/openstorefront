///*
// * Copyright 2017 Space Dynamics Laboratory - Utah State University Research Foundation.
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// * See NOTICE.txt for more information.
// */
//package edu.usu.sdl.openstorefront.web.extension;
//
//import edu.usu.sdl.openstorefront.web.rest.RestConfiguration;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.inject.Inject;
//import net.sourceforge.stripes.action.ActionBean;
//import net.sourceforge.stripes.action.Resolution;
//import net.sourceforge.stripes.controller.ExecutionContext;
//import net.sourceforge.stripes.controller.Interceptor;
//import net.sourceforge.stripes.controller.Intercepts;
//import net.sourceforge.stripes.controller.LifecycleStage;
//import org.glassfish.hk2.api.ServiceLocator;
//
///**
// *
// * @author kbair
// */
//@Intercepts(LifecycleStage.ActionBeanResolution)
//public class StripesInjection
//		implements Interceptor
//{
//
//	@Inject
//	public ServiceLocator serviceLocator;
//	
//	private static final Logger LOG = Logger.getLogger(StripesInjection.class.getName());
//
//	@Override
//	public Resolution intercept(ExecutionContext context) throws Exception
//	{
//		Resolution resolution = context.proceed();
//		ActionBean bean = context.getActionBean();
//		if (bean != null) {
//			LOG.log(Level.INFO, String.format("Running injection for instance of %s", bean.getClass().getSimpleName()));
//			ServiceLocator locator = RestConfiguration.serviceLocator; //ServiceLocatorUtilities.createAndPopulateServiceLocator(); // ServiceLocatorFactory.getInstance().find("myLocator"); // (String) context.getActionBeanContext().getServletContext().getAttribute(RestConfiguration.SERVICE_LOCATOR_KEY));
//			locator.
////			RequestScope rs = new RequestScope();
////			rs.runInScope(() -> {
////			});
//		} else {
//			LOG.log(Level.INFO, "no action bean");
//		}
//		return resolution;
//	}
//
//}
