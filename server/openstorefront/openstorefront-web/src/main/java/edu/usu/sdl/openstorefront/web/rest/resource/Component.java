/*
 * Copyright 2014 Space Dynamics Laboratory - Utah State University Research Foundation.
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
import edu.usu.sdl.openstorefront.doc.RequiredParam;
import edu.usu.sdl.openstorefront.web.rest.model.ComponentDetailView;
import edu.usu.sdl.openstorefront.web.rest.model.ComponentView;
import edu.usu.sdl.openstorefront.web.rest.model.RestListResponse;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Component Resource
 *
 * @author dshurtleff
 */
@Path("v1/resource/components")
@APIDescription("Components are the central resource of the system.  The majority of the listing are components.")
public class Component
		extends BaseResource
{

	@GET
	@APIDescription("Get a list of components <br>(Note: this only the top level component object, See Componet Detail for composite resource.)")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(ComponentView.class)
	public RestListResponse getComponents()
	{
		List<ComponentView> componentViews = service.getComponentService().getComponents();
		return sendListResponse(componentViews);
	}

	@GET
	@APIDescription("Gets a component <br>(Note: this only the top level component object)")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(ComponentView.class)
	@Path("/{id}")
	public ComponentView getComponentSingle(
			@PathParam("id")
			@RequiredParam String componentId)
	{
		ComponentView componentView = service.getComponentService().getComponent(componentId);
		return componentView;
	}

	@GET
	@APIDescription("Gets full component details (This the packed view for displaying)")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(ComponentDetailView.class)
	@Path("/{id}/detail")
	public ComponentDetailView getComponentDetails(
			@PathParam("id")
			@RequiredParam String componentId)
	{
		ComponentDetailView componentDetail = service.getComponentService().getComponentDetails(componentId);
		return componentDetail;
	}

	//TODO: Review this needs to be adjusted as it breaks on deploy.
//	@GET
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentAttribute.class)
//	@Path("/{id}/attribute")
//	public List<ComponentAttribute> getComponentAttribute(
//			@PathParam("id")
//			@RequiredParam String id)
//	{
//		return service.getComponentService().getBaseComponent(ComponentAttribute.class, id);
//	}
//
//
//	@DELETE
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentAttribute.class)
//	@Path("/{id}/attribute")
//	public void deleteComponentAttribute(
//			@PathParam("id")
//			@RequiredParam String componentId,
//			@RequiredParam String itemType,
//			@RequiredParam String itemCode)
//	{
//				service.getComponentService().deactivateBaseComponent(ComponentAttribute.class, itemType, itemCode, componentId);
//	}
//
//
//	@POST
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentAttribute.class)
//	@Path("/{id}/attribute")
//	public ComponentAttribute addComponentAttribute(
//			@PathParam("id")
//			@RequiredParam ComponentAttribute attribute)
//	{
//		service.getComponentService().saveComponentAttribute(attribute);
//		return attribute;
//	}
//
//	@POST
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentAttribute.class)
//	@Path("/{id}/attribute")
//	public Response addComponentAttribute(
//			@PathParam("id")
//			@RequiredParam String id,
//			@RequiredParam ComponentAttribute attribute)
//	{
//		service.getComponentService().saveComponentAttribute(attribute);
//		return Response.ok().build();
//	}
//
//	@DELETE
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentContact.class)
//	@Path("/{id}/contact")
//	public void deleteComponentContact(
//			@PathParam("id")
//			@RequiredParam String componentId,
//			@RequiredParam String itemId)
//	{
//		service.getComponentService().deactivateBaseComponent(ComponentContact.class, itemId, componentId);
//	}
//
//	@POST
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentContact.class)
//	@Path("/{id}/contact")
//	public ComponentContact addComponentContact(
//			@PathParam("id")
//			@RequiredParam ComponentContact contact)
//	{
//		service.getComponentService().saveComponentContact(contact);
//		return contact;
//	}
//
//	@PUT
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentContact.class)
//	@Path("/{id}/contact")
//	public ComponentContact updateComponentContact(
//			@PathParam("id")
//			@RequiredParam ComponentContact contact)
//	{
//		service.getComponentService().saveComponentContact(contact);
//		return contact;
//	}
//
//	@GET
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentEvaluationSection.class)
//	@Path("/{id}/section")
//	public List<ComponentEvaluationSection> getComponentEvaluationSection(
//			@PathParam("id")
//			@RequiredParam String id)
//	{
//		return service.getComponentService().getBaseComponent(ComponentEvaluationSection.class, id);
//	}
//
//	@DELETE
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentEvaluationSection.class)
//	@Path("/{id}/section")
//	public void deleteComponentEvaluationSection(
//			@PathParam("id")
//			@RequiredParam String componentId,
//			@RequiredParam String itemId)
//	{
//		service.getComponentService().deactivateBaseComponent(ComponentEvaluationSection.class, itemId, componentId);
//	}
//
//	@POST
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentEvaluationSection.class)
//	@Path("/{id}/section")
//	public ComponentEvaluationSection addComponentEvaluationSection(
//			@PathParam("id")
//			@RequiredParam ComponentEvaluationSection section)
//	{
//		service.getComponentService().saveComponentEvaluationSection(section);
//		return section;
//	}
//
//	@PUT
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentEvaluationSection.class)
//	@Path("/{id}/section")
//	public ComponentEvaluationSection updateComponentEvaluationSection(
//			@PathParam("id")
//			@RequiredParam ComponentEvaluationSection section)
//	{
//		service.getComponentService().saveComponentEvaluationSection(section);
//		return section;
//	}
//
//	@GET
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentEvaluationSchedule.class)
//	@Path("/{id}/schedule")
//	public List<ComponentEvaluationSchedule> getComponentEvaluationSchedule(
//			@PathParam("id")
//			@RequiredParam String id)
//	{
//		return service.getComponentService().getBaseComponent(ComponentEvaluationSchedule.class, id);
//	}
//
//	@DELETE
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentEvaluationSchedule.class)
//	@Path("/{id}/schedule")
//	public void deleteComponentEvaluationSchedule(
//			@PathParam("id")
//			@RequiredParam String componentId,
//			@RequiredParam String itemId)
//	{
//		service.getComponentService().deactivateBaseComponent(ComponentEvaluationSchedule.class, itemId, componentId);
//	}
//
//	@POST
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentEvaluationSchedule.class)
//	@Path("/{id}/schedule")
//	public ComponentEvaluationSchedule addComponentEvaluationSchedule(
//			@PathParam("id")
//			@RequiredParam ComponentEvaluationSchedule schedule)
//	{
//		service.getComponentService().saveComponentEvaluationSchedule(schedule);
//		return schedule;
//	}
//
//	@PUT
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentEvaluationSchedule.class)
//	@Path("/{id}/schedule")
//	public ComponentEvaluationSchedule updateComponentEvaluationSchedule(
//			@PathParam("id")
//			@RequiredParam ComponentEvaluationSchedule schedule)
//	{
//		service.getComponentService().saveComponentEvaluationSchedule(schedule);
//		return schedule;
//	}
//
//	@GET
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentMedia.class)
//	@Path("/{id}/media")
//	public List<ComponentMedia> getComponentMedia(
//			@PathParam("id")
//			@RequiredParam String id)
//	{
//		return service.getComponentService().getBaseComponent(ComponentMedia.class, id);
//	}
//
//	@DELETE
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentMedia.class)
//	@Path("/{id}/media")
//	public void deleteComponentMedia(
//			@PathParam("id")
//			@RequiredParam String componentId,
//			@RequiredParam String itemId)
//	{
//		service.getComponentService().deactivateBaseComponent(ComponentMedia.class, itemId, componentId);
//	}
//
//	@POST
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentMedia.class)
//	@Path("/{id}/media")
//	public ComponentMedia addComponentMedia(
//			@PathParam("id")
//			@RequiredParam ComponentMedia media)
//	{
//		service.getComponentService().saveComponentMedia(media);
//		return media;
//	}
//
//	@PUT
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentMedia.class)
//	@Path("/{id}/media")
//	public ComponentMedia updateComponentMedia(
//			@PathParam("id")
//			@RequiredParam ComponentMedia media)
//	{
//		service.getComponentService().saveComponentMedia(media);
//		return media;
//	}
//
//	@GET
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentMetadata.class)
//	@Path("/{id}/metadata")
//	public List<ComponentMetadata> getComponentMetadata(
//			@PathParam("id")
//			@RequiredParam String id)
//	{
//		return service.getComponentService().getBaseComponent(ComponentMetadata.class, id);
//	}
//
//	@DELETE
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentMetadata.class)
//	@Path("/{id}/metadata")
//	public void deleteComponentMetadata(
//			@PathParam("id")
//			@RequiredParam String componentId,
//			@RequiredParam String itemId)
//	{
//		service.getComponentService().deactivateBaseComponent(ComponentMetadata.class, itemId, componentId);
//	}
//
//	@POST
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentMetadata.class)
//	@Path("/{id}/metadata")
//	public ComponentMetadata addComponentMetadata(
//			@PathParam("id")
//			@RequiredParam ComponentMetadata metadata)
//	{
//		service.getComponentService().saveComponentMetadata(metadata);
//		return metadata;
//	}
//
//	@PUT
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentMetadata.class)
//	@Path("/{id}/metadata")
//	public ComponentMetadata updateComponentMetadata(
//			@PathParam("id")
//			@RequiredParam ComponentMetadata metadata)
//	{
//		service.getComponentService().saveComponentMetadata(metadata);
//		return metadata;
//	}
//
//	@GET
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentQuestion.class)
//	@Path("/{id}/question")
//	public List<ComponentQuestion> getComponentQuestion(
//			@PathParam("id")
//			@RequiredParam String id)
//	{
//		return service.getComponentService().getBaseComponent(ComponentQuestion.class, id);
//	}
//
//	@DELETE
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentQuestion.class)
//	@Path("/{id}/question")
//	public void deleteComponentQuestion(
//			@PathParam("id")
//			@RequiredParam String componentId,
//			@RequiredParam String itemId)
//	{
//		service.getComponentService().deactivateBaseComponent(ComponentQuestion.class, itemId, componentId);
//	}
//
//	@POST
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentQuestion.class)
//	@Path("/{id}/question")
//	public ComponentQuestion addComponentQuestion(
//			@PathParam("id")
//			@RequiredParam ComponentQuestion question)
//	{
//		service.getComponentService().saveComponentQuestion(question);
//		return question;
//	}
//
//	@PUT
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentQuestion.class)
//	@Path("/{id}/question")
//	public ComponentQuestion updateComponentQuestion(
//			@PathParam("id")
//			@RequiredParam ComponentQuestion question)
//	{
//		service.getComponentService().saveComponentQuestion(question);
//		return question;
//	}
//
//	@GET
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentQuestionResponse.class)
//	@Path("/{id}/response")
//	public List<ComponentQuestionResponse> getComponentQuestionResponse(
//			@PathParam("id")
//			@RequiredParam String id)
//	{
//		return service.getComponentService().getBaseComponent(ComponentQuestionResponse.class, id);
//	}
//
//	@DELETE
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentQuestionResponse.class)
//	@Path("/{id}/response")
//	public void deleteComponentQuestionResponse(
//			@PathParam("id")
//			@RequiredParam String componentId,
//			@RequiredParam String itemId)
//	{
//		service.getComponentService().deactivateBaseComponent(ComponentQuestionResponse.class, itemId, componentId);
//	}
//
//	@POST
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentQuestionResponse.class)
//	@Path("/{id}/response")
//	public ComponentQuestionResponse addComponentQuestionResponse(
//			@PathParam("id")
//			@RequiredParam ComponentQuestionResponse response)
//	{
//		service.getComponentService().saveComponentQuestionResponse(response);
//		return response;
//	}
//
//	@PUT
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentQuestionResponse.class)
//	@Path("/{id}/response")
//	public ComponentQuestionResponse updateComponentQuestionResponse(
//			@PathParam("id")
//			@RequiredParam ComponentQuestionResponse response)
//	{
//		service.getComponentService().saveComponentQuestionResponse(response);
//		return response;
//	}
//
//	@GET
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentResource.class)
//	@Path("/{id}/resource")
//	public List<ComponentResource> getComponentResource(
//			@PathParam("id")
//			@RequiredParam String id)
//	{
//		return service.getComponentService().getBaseComponent(ComponentResource.class, id);
//	}
//
//	@DELETE
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentResource.class)
//	@Path("/{id}/resource")
//	public void deleteComponentResource(
//			@PathParam("id")
//			@RequiredParam String componentId,
//			@RequiredParam String itemId)
//	{
//		service.getComponentService().deactivateBaseComponent(ComponentResource.class, itemId, componentId);
//	}
//
//	@POST
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentResource.class)
//	@Path("/{id}/resource")
//	public ComponentResource addComponentResource(
//			@PathParam("id")
//			@RequiredParam ComponentResource resource)
//	{
//		service.getComponentService().saveComponentResource(resource);
//		return resource;
//	}
//
//	@PUT
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentResource.class)
//	@Path("/{id}/resource")
//	public ComponentResource updateComponentResource(
//			@PathParam("id")
//			@RequiredParam ComponentResource resource)
//	{
//		service.getComponentService().saveComponentResource(resource);
//		return resource;
//	}
//
//	@GET
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentReview.class)
//	@Path("/{id}/review")
//	public List<ComponentReview> getComponentReview(
//			@PathParam("id")
//			@RequiredParam String id)
//	{
//		return service.getComponentService().getBaseComponent(ComponentReview.class, id);
//	}
//
//	@DELETE
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentReview.class)
//	@Path("/{id}/review")
//	public void deleteComponentReview(
//			@PathParam("id")
//			@RequiredParam String componentId,
//			@RequiredParam String itemId)
//	{
//		service.getComponentService().deactivateBaseComponent(ComponentReview.class, itemId, componentId);
//	}
//
//	@POST
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentReview.class)
//	@Path("/{id}/review")
//	public ComponentReview addComponentReview(
//			@PathParam("id")
//			@RequiredParam ComponentReview review)
//	{
//		service.getComponentService().saveComponentReview(review);
//		return review;
//	}
//
//	@PUT
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentReview.class)
//	@Path("/{id}/review")
//	public ComponentReview updateComponentReview(
//			@PathParam("id")
//			@RequiredParam ComponentReview review)
//	{
//		service.getComponentService().saveComponentReview(review);
//		return review;
//	}
//
//	@GET
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentReviewCon.class)
//	@Path("/{id}/con")
//	public List<ComponentReviewCon> getComponentReviewCon(
//			@PathParam("id")
//			@RequiredParam String id)
//	{
//		return service.getComponentService().getBaseComponent(ComponentReviewCon.class, id);
//	}
//
//	@DELETE
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentReviewCon.class)
//	@Path("/{id}/con")
//	public void deleteComponentReviewCon(
//			@PathParam("id")
//			@RequiredParam String componentId,
//			@RequiredParam String itemId)
//	{
//		service.getComponentService().deactivateBaseComponent(ComponentReviewCon.class, itemId, componentId);
//	}
//
//	@POST
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentReviewCon.class)
//	@Path("/{id}/con")
//	public ComponentReviewCon addComponentReviewCon(
//			@PathParam("id")
//			@RequiredParam ComponentReviewCon con)
//	{
//		service.getComponentService().saveComponentReviewCon(con);
//		return con;
//	}
//
//	@PUT
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentReviewCon.class)
//	@Path("/{id}/con")
//	public ComponentReviewCon updateComponentReviewCon(
//			@PathParam("id")
//			@RequiredParam ComponentReviewCon con)
//	{
//		service.getComponentService().saveComponentReviewCon(con);
//		return con;
//	}
//
//	@GET
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentReviewPro.class)
//	@Path("/{id}/pro")
//	public List<ComponentReviewPro> getComponentReviewPro(
//			@PathParam("id")
//			@RequiredParam String id)
//	{
//		return service.getComponentService().getBaseComponent(ComponentReviewPro.class, id);
//	}
//
//	@DELETE
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentReviewPro.class)
//	@Path("/{id}/pro")
//	public void deleteComponentReviewPro(
//			@PathParam("id")
//			@RequiredParam String componentId,
//			@RequiredParam String itemId)
//	{
//		service.getComponentService().deactivateBaseComponent(ComponentReviewPro.class, itemId, componentId);
//	}
//
//	@POST
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentReviewPro.class)
//	@Path("/{id}/pro")
//	public ComponentReviewPro addComponentReviewPro(
//			@PathParam("id")
//			@RequiredParam ComponentReviewPro pro)
//	{
//		service.getComponentService().saveComponentReviewPro(pro);
//		return pro;
//	}
//
//	@PUT
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentReviewPro.class)
//	@Path("/{id}/pro")
//	public ComponentReviewPro updateComponentReviewPro(
//			@PathParam("id")
//			@RequiredParam ComponentReviewPro pro)
//	{
//		service.getComponentService().saveComponentReviewPro(pro);
//		return pro;
//	}
//
//	@GET
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentTag.class)
//	@Path("/{id}/tag")
//	public List<ComponentTag> getComponentTag(
//			@PathParam("id")
//			@RequiredParam String id)
//	{
//		return service.getComponentService().getBaseComponent(ComponentTag.class, id);
//	}
//
//	@DELETE
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentTag.class)
//	@Path("/{id}/tag")
//	public void deleteComponentTag(
//			@PathParam("id")
//			@RequiredParam String componentId,
//			@RequiredParam String itemId)
//	{
//		service.getComponentService().deactivateBaseComponent(ComponentTag.class, itemId, componentId);
//	}
//
//	@POST
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentTag.class)
//	@Path("/{id}/tag")
//	public ComponentTag addComponentTag(
//			@PathParam("id")
//			@RequiredParam ComponentTag tag)
//	{
//		service.getComponentService().saveComponentTag(tag);
//		return tag;
//	}
//
//	@PUT
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentTag.class)
//	@Path("/{id}/tag")
//	public ComponentTag updateComponentTag(
//			@PathParam("id")
//			@RequiredParam ComponentTag tag)
//	{
//		service.getComponentService().saveComponentTag(tag);
//		return tag;
//	}
//
//	@GET
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentTracking.class)
//	@Path("/{id}/tracking")
//	public List<ComponentTracking> getComponentTracking(
//			@PathParam("id")
//			@RequiredParam String id)
//	{
//		return service.getComponentService().getBaseComponent(ComponentTracking.class, id);
//	}
//
//	@DELETE
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@DataType(ComponentTracking.class)
//	@Path("/{id}/tracking")
//	public void deleteComponentTracking(
//			@PathParam("id")
//			@RequiredParam String componentId,
//			@RequiredParam String itemId)
//	{
//		service.getComponentService().deactivateBaseComponent(ComponentTracking.class, itemId, componentId);
//	}
//
//	@POST
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentTracking.class)
//	@Path("/{id}/tracking")
//	public ComponentTracking addComponentTracking(
//			@PathParam("id")
//			@RequiredParam ComponentTracking tracking)
//	{
//		service.getComponentService().saveComponentTracking(tracking);
//		return tracking;
//	}
//
//	@PUT
//	@APIDescription("Gets full component details (This the packed view for displaying)")
//	@Produces({MediaType.APPLICATION_JSON})
//	@Consumes(MediaType.APPLICATION_JSON)
//	@DataType(ComponentTracking.class)
//	@Path("/{id}/tracking")
//	public ComponentTracking updateComponentTracking(
//			@PathParam("id")
//			@RequiredParam ComponentTracking tracking)
//	{
//		service.getComponentService().saveComponentTracking(tracking);
//		return tracking;
//	}
//
}
