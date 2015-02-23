/*
 * Copyright 2015 Space Dynamics Laboratory - Utah State University Research Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.usu.sdl.openstorefront.web.rest.resource;

import edu.usu.sdl.openstorefront.doc.APIDescription;
import edu.usu.sdl.openstorefront.doc.DataType;
import edu.usu.sdl.openstorefront.doc.RequireAdmin;
import edu.usu.sdl.openstorefront.storage.model.GeneralMedia;
import edu.usu.sdl.openstorefront.validation.ValidationResult;
import edu.usu.sdl.openstorefront.web.rest.model.FilterQueryParams;
import edu.usu.sdl.openstorefront.web.rest.model.GeneralMediaView;
import java.util.List;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author dshurtleff
 */
@Path("v1/resource/generalmedia")
@APIDescription("General media is used for articles, badges...etc.  Dynamic Resources.")
public class GeneralMediaResource
		extends BaseResource
{

	@GET
	@RequireAdmin
	@APIDescription("Gets all general media records.")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(GeneralMediaView.class)
	public Response getGeneralMedia(@BeanParam FilterQueryParams filterQueryParams)
	{
		ValidationResult validationResult = filterQueryParams.validate();
		if (!validationResult.valid()) {
			return sendSingleEntityResponse(validationResult.toRestError());
		}

		GeneralMedia generalMediaExample = new GeneralMedia();
		generalMediaExample.setActiveStatus(filterQueryParams.getStatus());
		List<GeneralMedia> generalMedia = service.getPersistenceService().queryByExample(GeneralMedia.class, generalMediaExample);
		generalMedia = filterQueryParams.filter(generalMedia);
		List<GeneralMediaView> generalMediaViews = GeneralMediaView.toViewList(generalMedia);

		GenericEntity<List<GeneralMediaView>> entity = new GenericEntity<List<GeneralMediaView>>(generalMediaViews)
		{
		};
		return sendSingleEntityResponse(entity);
	}

	@GET
	@RequireAdmin
	@APIDescription("Gets a general media record.")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(GeneralMediaView.class)
	@Path("/{name}")
	public Response getGeneralMedia(
			@PathParam("name") String name)
	{
		GeneralMedia generalMediaExample = new GeneralMedia();
		generalMediaExample.setName(name);
		GeneralMedia generalMedia = service.getPersistenceService().queryOneByExample(GeneralMedia.class, generalMediaExample);
		return sendSingleEntityResponse(generalMedia);
	}

	//Post
	//Delete
}
