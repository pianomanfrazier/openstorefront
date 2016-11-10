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

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.usu.sdl.openstorefront.common.exception.OpenStorefrontRuntimeException;
import edu.usu.sdl.openstorefront.common.util.Convert;
import edu.usu.sdl.openstorefront.common.util.OpenStorefrontConstant;
import edu.usu.sdl.openstorefront.common.util.OpenStorefrontConstant.TaskStatus;
import edu.usu.sdl.openstorefront.common.util.StringProcessor;
import edu.usu.sdl.openstorefront.common.util.TimeUtil;
import edu.usu.sdl.openstorefront.core.annotation.APIDescription;
import edu.usu.sdl.openstorefront.core.annotation.DataType;
import edu.usu.sdl.openstorefront.core.api.model.AsyncTaskCallback;
import edu.usu.sdl.openstorefront.core.api.model.TaskFuture;
import edu.usu.sdl.openstorefront.core.api.model.TaskRequest;
import edu.usu.sdl.openstorefront.core.api.query.QueryByExample;
import edu.usu.sdl.openstorefront.core.entity.AlertType;
import edu.usu.sdl.openstorefront.core.entity.AttributeCode;
import edu.usu.sdl.openstorefront.core.entity.AttributeCodePk;
import edu.usu.sdl.openstorefront.core.entity.AttributeType;
import edu.usu.sdl.openstorefront.core.entity.AttributeXRefMap;
import edu.usu.sdl.openstorefront.core.entity.AttributeXRefType;
import edu.usu.sdl.openstorefront.core.entity.Component;
import edu.usu.sdl.openstorefront.core.entity.ComponentAttribute;
import edu.usu.sdl.openstorefront.core.entity.ComponentAttributePk;
import edu.usu.sdl.openstorefront.core.entity.ComponentIntegration;
import edu.usu.sdl.openstorefront.core.entity.LookupEntity;
import edu.usu.sdl.openstorefront.core.model.AlertContext;
import edu.usu.sdl.openstorefront.core.model.Architecture;
import edu.usu.sdl.openstorefront.core.model.AttributeAll;
import edu.usu.sdl.openstorefront.core.sort.AttributeCodeArchComparator;
import edu.usu.sdl.openstorefront.core.sort.AttributeCodeArchViewComparator;
import edu.usu.sdl.openstorefront.core.sort.AttributeCodeComparator;
import edu.usu.sdl.openstorefront.core.sort.AttributeCodeViewComparator;
import edu.usu.sdl.openstorefront.core.sort.AttributeTypeViewComparator;
import edu.usu.sdl.openstorefront.core.view.AttributeCodeSave;
import edu.usu.sdl.openstorefront.core.view.AttributeCodeView;
import edu.usu.sdl.openstorefront.core.view.AttributeCodeWrapper;
import edu.usu.sdl.openstorefront.core.view.AttributeDetail;
import edu.usu.sdl.openstorefront.core.view.AttributeTypeSave;
import edu.usu.sdl.openstorefront.core.view.AttributeTypeView;
import edu.usu.sdl.openstorefront.core.view.AttributeTypeWrapper;
import edu.usu.sdl.openstorefront.core.view.AttributeXRefView;
import edu.usu.sdl.openstorefront.core.view.AttributeXrefMapView;
import edu.usu.sdl.openstorefront.core.view.ComponentView;
import edu.usu.sdl.openstorefront.core.view.FilterQueryParams;
import edu.usu.sdl.openstorefront.core.view.RelationshipView;
import edu.usu.sdl.openstorefront.doc.annotation.RequiredParam;
import edu.usu.sdl.openstorefront.doc.security.RequireAdmin;
import edu.usu.sdl.openstorefront.security.SecurityUtil;
import edu.usu.sdl.openstorefront.validation.ValidationModel;
import edu.usu.sdl.openstorefront.validation.ValidationResult;
import edu.usu.sdl.openstorefront.validation.ValidationUtil;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

/**
 *
 * @author dshurtleff
 */
@Path("v1/resource/attributes")
@APIDescription("Attributes are the filterable data associated with the listings.")
public class AttributeResource
		extends BaseResource
{

	@Context
	HttpServletRequest request;

	@GET
	@APIDescription("Gets all active attributes and codes for the attributes in view based model.")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(AttributeTypeView.class)
	public List<AttributeTypeView> getAttributeView(
			@QueryParam("all")
			@APIDescription("Setting all to true will pull both active and inactive records")
			@DefaultValue("false") boolean all)
	{
		List<AttributeTypeView> attributeTypeViews = new ArrayList<>();

		AttributeType attributeTypeExample = new AttributeType();
		if (!all) {
			attributeTypeExample.setActiveStatus(AttributeType.ACTIVE_STATUS);
		}
		List<AttributeType> attributeTypes = service.getPersistenceService().queryByExample(AttributeType.class, attributeTypeExample);

		String codeStatus = null;
		if (!all) {
			codeStatus = AttributeCode.ACTIVE_STATUS;
		}
		List<AttributeCode> attributeCodesAll = service.getAttributeService().getAllAttributeCodes(codeStatus);

		Map<String, List<AttributeCode>> codeMap = new HashMap<>();
		for (AttributeCode code : attributeCodesAll) {
			if (codeMap.containsKey(code.getAttributeCodePk().getAttributeType())) {
				codeMap.get(code.getAttributeCodePk().getAttributeType()).add(code);
			} else {
				List<AttributeCode> codes = new ArrayList<>();
				codes.add(code);
				codeMap.put(code.getAttributeCodePk().getAttributeType(), codes);
			}
		}

		for (AttributeType attributeType : attributeTypes) {
			AttributeTypeView attributeTypeView = AttributeTypeView.toView(attributeType);
			List<AttributeCode> attributeCodes = codeMap.get(attributeType.getAttributeType());
			if (attributeCodes == null) {
				attributeCodes = new ArrayList<>();
			}
			attributeCodes.stream().forEach(code -> {
				attributeTypeView.getCodes().add(AttributeCodeView.toView(code));
			});
			attributeTypeViews.add(attributeTypeView);
		}
		attributeTypeViews.sort(new AttributeTypeViewComparator<>());
		for (AttributeTypeView attributeTypeView : attributeTypeViews) {
			if (attributeTypeView.getArchitectureFlg()) {
				attributeTypeView.getCodes().sort(new AttributeCodeArchViewComparator<>());
			} else {
				attributeTypeView.getCodes().sort(new AttributeCodeViewComparator<>());
			}
		}

		return attributeTypeViews;
	}

	@GET
	@APIDescription("Get attribute relationships")
	@Produces(MediaType.APPLICATION_JSON)
	@DataType(RelationshipView.class)
	@Path("/relationships")
	public Response getAttributeRelationships(
		@QueryParam("attributeType") String filterAttributeType 
	) 
	{
		List<RelationshipView> relationships = new ArrayList<>();
				
		AttributeType attributeTypeExample = new AttributeType();
		attributeTypeExample.setActiveStatus(AttributeType.ACTIVE_STATUS);
		attributeTypeExample.setAttributeType(filterAttributeType);
						
		List<AttributeType> attributeTypes = attributeTypeExample.findByExample();
		for (AttributeType attributeType : attributeTypes) {
			if (attributeType.getArchitectureFlg()) {
				Architecture architecture = service.getAttributeService().generateArchitecture(attributeType.getAttributeType());
				buildRelations(relationships, architecture, null);
			} else {
				List<AttributeCode> attributeCodes = service.getAttributeService().findCodesForType(attributeType.getAttributeType());
				for (AttributeCode attributeCode : attributeCodes) {
					RelationshipView relationship = new RelationshipView();
					relationship.setKey(attributeCode.getAttributeCodePk().toKey());
					relationship.setName(attributeCode.getLabel());
					relationship.setEntityType(RelationshipView.ENTITY_TYPE_ATTRIBUTE);
					relationship.setRelationType(RelationshipView.ATTRIBUTE_CODE_RELATION);
					relationship.setRelationshipLabel(attributeType.getDescription());
					relationship.setTargetKey(attributeType.getAttributeType());
					relationship.setTargetName(attributeType.getDescription());
					relationship.setTargetEntityType(RelationshipView.ENTITY_TYPE_ATTRIBUTE);
										
					relationships.add(relationship);
				}				
			}
		}		
		
		GenericEntity<List<RelationshipView>> entity = new GenericEntity<List<RelationshipView>>(relationships)
		{
		};
		return sendSingleEntityResponse(entity);
	}

	public void buildRelations(List<RelationshipView> relationships, Architecture architecture,  Architecture parent) 
	{
		if (parent != null) {
			RelationshipView relationship = new RelationshipView();
			String key = architecture.getAttributeType() + "-" + architecture.getAttributeCode();
			String keyParent = parent.getAttributeType() + "-" + parent.getAttributeCode();
			
			relationship.setKey(key);
			relationship.setName(architecture.getName());
			relationship.setEntityType(RelationshipView.ENTITY_TYPE_ATTRIBUTE);
			relationship.setRelationType(RelationshipView.ATTRIBUTE_CODE_RELATION);
			relationship.setTargetKey(keyParent);
			relationship.setTargetName(parent.getName());
			relationship.setTargetEntityType(RelationshipView.ENTITY_TYPE_ATTRIBUTE);

			relationships.add(relationship);			
		}
		
		for (Architecture child : architecture.getChildren()) {
			buildRelations(relationships, child, architecture);
		}		
		
	}	
	
	@POST
	@APIDescription("Exports attributes in JSON format. To import attributes, POST to /Upload.action?UploadAttributes with the file (Requires Admin)")
	@RequireAdmin
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/export")
	public Response exportAttributes(
			@FormParam("type")
			@RequiredParam List<String> types)
	{
		List<AttributeAll> attributes = new ArrayList<>();

		AttributeType attributeTypeExample = new AttributeType();
		attributeTypeExample.setActiveStatus(AttributeType.ACTIVE_STATUS);

		boolean restrictTypes = false;
		Set<String> typeSet = new HashSet<>();
		if (types != null) {
			restrictTypes = true;
			typeSet.addAll(types);
		}

		List<AttributeType> attributeTypes = service.getPersistenceService().queryByExample(AttributeType.class, new QueryByExample(attributeTypeExample));
		for (AttributeType attributeType : attributeTypes) {
			if (restrictTypes && typeSet.contains(attributeType.getAttributeType())) {
				AttributeAll attributeAll = new AttributeAll();
				attributeAll.setAttributeType(attributeType);

				List<AttributeCode> attributeCodes = service.getAttributeService().findCodesForType(attributeType.getAttributeType());
				attributeCodes.stream().forEach(code -> {
					attributeAll.getAttributeCodes().add(code);
				});
				if (attributeType.getArchitectureFlg()) {
					attributeAll.getAttributeCodes().sort(new AttributeCodeArchComparator<>());
				} else {
					attributeAll.getAttributeCodes().sort(new AttributeCodeComparator<>());
				}
				attributes.add(attributeAll);
			}
		}

		String data;
		try {
			data = StringProcessor.defaultObjectMapper().writeValueAsString(attributes);
		} catch (JsonProcessingException ex) {
			throw new OpenStorefrontRuntimeException("Unable to export attributes.  Unable to generate JSON.", ex);
		}

		Response.ResponseBuilder response = Response.ok(data);
		response.header("Content-Type", MediaType.APPLICATION_JSON);
		response.header("Content-Disposition", "attachment; filename=\"attributes.json\"");
		return response.build();
	}

	@GET
	@APIDescription("Gets attribute types based on filter")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(AttributeType.class)
	@Path("/attributetypes")
	public Response getAttributeTypes(@BeanParam FilterQueryParams filterQueryParams)
	{
		ValidationResult validationResult = filterQueryParams.validate();
		if (!validationResult.valid()) {
			return sendSingleEntityResponse(validationResult.toRestError());
		}

		AttributeTypeWrapper entity = service.getAttributeService().getFilteredTypes(filterQueryParams);
		return sendSingleEntityResponse(entity);
	}

	@GET
	@APIDescription("Gets attribute type")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(AttributeType.class)
	@Path("/attributetypes/{type}")
	public Response getAttributeTypeById(
			@PathParam("type")
			@RequiredParam String type,
			@QueryParam("view")
			@APIDescription("Setting forces the attribute to return the view model.")
			@DefaultValue("false") boolean view,
			@QueryParam("all")
			@APIDescription("Setting forces the attribute to return the view model.")
			@DefaultValue("false") boolean all)
	{
		if (!view) {
			AttributeType attributeType = service.getPersistenceService().findById(AttributeType.class, type);
			if (attributeType == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			} else {
				return Response.ok(attributeType).build();
			}
		} else {
			AttributeTypeView attributeTypeView = new AttributeTypeView();
			AttributeType typeObj = service.getPersistenceService().findById(AttributeType.class, type);
			if (typeObj != null) {
				attributeTypeView = AttributeTypeView.toView(typeObj);
			} else {
				typeObj = new AttributeType();
				typeObj.setAttributeType(AttributeType.TYPE);
			}
			List<AttributeCode> attributeCodes = service.getAttributeService().findCodesForType(typeObj.getAttributeType(), all);
			for (AttributeCode code : attributeCodes) {
				attributeTypeView.getCodes().add(AttributeCodeView.toView(code));
			}
			if (attributeTypeView.getArchitectureFlg()) {
				attributeTypeView.getCodes().sort(new AttributeCodeArchViewComparator<>());
			} else {
				attributeTypeView.getCodes().sort(new AttributeCodeViewComparator<>());
			}
			return Response.ok(attributeTypeView).build();
		}
	}

	@GET
	@APIDescription("Gets attribute type")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(AttributeType.class)
	@Path("/attributetypes/{type}/{code}")
	public Response getAttributeCodeById(
			@PathParam("type")
			@RequiredParam String type,
			@PathParam("code")
			@RequiredParam String code)
	{
		AttributeCodePk pk = new AttributeCodePk();
		pk.setAttributeCode(code);
		pk.setAttributeType(type);
		AttributeCode attributeCode = service.getPersistenceService().findById(AttributeCode.class, pk);
		if (attributeCode == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		} else {
			return Response.ok(attributeCode).build();
		}
	}
	
	@GET
	@APIDescription("Gets attribute code details")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(AttributeDetail.class)
	@Path("/attributetypes/{type}/{code}/detail")
	public Response getAttributeCodeViewById(
			@PathParam("type")
			@RequiredParam String type,
			@PathParam("code")
			@RequiredParam String code)
	{
		AttributeCodePk pk = new AttributeCodePk();
		pk.setAttributeCode(code);
		pk.setAttributeType(type);
		AttributeCode attributeCode = service.getPersistenceService().findById(AttributeCode.class, pk);
		if (attributeCode == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		} else {
			return Response.ok(AttributeDetail.toView(attributeCode)).build();
		}
	}	

	@GET
	@APIDescription("Gets attribute code base on filter. Always sorted by sort Order or label")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(AttributeCode.class)
	@Path("/attributetypes/{type}/attributecodes")
	public Response getAttributeCodes(
			@PathParam("type")
			@RequiredParam String type,
			@BeanParam FilterQueryParams filterQueryParams)
	{
		ValidationResult validationResult = filterQueryParams.validate();
		if (!validationResult.valid()) {
			return sendSingleEntityResponse(validationResult.toRestError());
		}

		List<AttributeCode> attributeCodes = getAttributeCodesForType(type, filterQueryParams);

		GenericEntity<List<AttributeCode>> entity = new GenericEntity<List<AttributeCode>>(attributeCodes)
		{
		};
		return sendSingleEntityResponse(entity);
	}

	@GET
	@APIDescription("Gets attribute code base on filter. Always sorted by sort Order or label")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(AttributeCodeWrapper.class)
	@Path("/attributetypes/{type}/attributecodeviews")
	public Response getAttributeCodeViews(
			@PathParam("type")
			@RequiredParam String type,
			@BeanParam FilterQueryParams filterQueryParams)
	{
		ValidationResult validationResult = filterQueryParams.validate();
		if (!validationResult.valid()) {
			return sendSingleEntityResponse(validationResult.toRestError());
		}
		AttributeCodeWrapper views = service.getAttributeService().getFilteredCodes(filterQueryParams, type);
		return sendSingleEntityResponse(views);
	}

	private List<AttributeCode> getAttributeCodesForType(String type, FilterQueryParams filterQueryParams)
	{
		AttributeCode attributeCodeExample = new AttributeCode();
		attributeCodeExample.setActiveStatus(filterQueryParams.getStatus());
		AttributeCodePk attributeCodePk = new AttributeCodePk();
		attributeCodePk.setAttributeType(type.toUpperCase());
		attributeCodeExample.setAttributeCodePk(attributeCodePk);

		List<AttributeCode> attributeCodes = service.getPersistenceService().queryByExample(AttributeCode.class, new QueryByExample(attributeCodeExample));
		attributeCodes = filterQueryParams.filter(attributeCodes);
		attributeCodes.sort(new AttributeCodeComparator<>());
		return attributeCodes;
	}

	@GET
	@APIDescription("Gets architecture")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(Architecture.class)
	@Path("/attributetypes/{type}/architecture")
	public Response getArchitecture(
			@PathParam("type")
			@RequiredParam String type)
	{
		Architecture architecture = null;
		AttributeType attributeType = service.getAttributeService().findType(type);
		if (attributeType != null) {
			architecture = service.getAttributeService().generateArchitecture(type);
		}
		return sendSingleEntityResponse(architecture);
	}

	@GET
	@APIDescription("Gets an attribute code.")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(AttributeCode.class)
	@Path("/attributetypes/{type}/attributecodes/{code}")
	public Response getAttributeCode(
			@PathParam("type")
			@RequiredParam String type,
			@PathParam("code")
			@RequiredParam String code)
	{
		AttributeCodePk attributeCodePk = new AttributeCodePk();
		attributeCodePk.setAttributeCode(code);
		attributeCodePk.setAttributeType(type);
		AttributeCode attributeCode = service.getPersistenceService().findById(AttributeCode.class, attributeCodePk);

		return sendSingleEntityResponse(attributeCode);
	}
	
	@GET
	@APIDescription("Get the components which contain a specified attribute type and code")
	@Produces(MediaType.APPLICATION_JSON)
	@DataType(AttributeCode.class)
	@Path("/attributetypes/{type}/attributecodes/{code}/components")
	public List<ComponentView> getComponentsWithAttributeCode(
			@PathParam("type")
			@RequiredParam String type,
			@PathParam("code")
			@RequiredParam String code)
	{
		List<ComponentView> components = new ArrayList<>();

		ComponentAttribute componentAttributeExample = new ComponentAttribute();
		ComponentAttributePk componentAttributePk = new ComponentAttributePk();
		componentAttributePk.setAttributeType(type);
		componentAttributePk.setAttributeCode(code);
		componentAttributeExample.setActiveStatus(AttributeCode.ACTIVE_STATUS);
		componentAttributeExample.setComponentAttributePk(componentAttributePk);
		List<ComponentAttribute> attributeComponents = service.getPersistenceService().queryByExample(ComponentAttribute.class, new QueryByExample(componentAttributeExample));
		for (ComponentAttribute attributeComponent : attributeComponents) {
			
			Component component = service.getPersistenceService().findById(Component.class, attributeComponent.getComponentAttributePk().getComponentId());
			
			ComponentView view = ComponentView.toView(component);
			
			if (view != null) {
				
				components.add(view);
			}
		}
		return components;
	}

	@POST
	@RequireAdmin
	@APIDescription("Adds a new attribute type")
	@Consumes({MediaType.APPLICATION_JSON})
	@Path("/attributetypes")
	public Response postAttributeType(AttributeTypeSave attributeTypeSave)
	{
		AttributeType attributeType = attributeTypeSave.getAttributeType();
		if (attributeTypeSave.getComponentTypeRestrictions() != null
				&& attributeTypeSave.getComponentTypeRestrictions().isEmpty() == false) {
			attributeType.setRequiredRestrictions(attributeTypeSave.getComponentTypeRestrictions());
		} else if (attributeTypeSave.getComponentTypeRestrictions() == null) {
			attributeType.setRequiredRestrictions(attributeTypeSave.getComponentTypeRestrictions());
		}

		if (attributeTypeSave.getAssociatedComponentTypes() != null
				&& attributeTypeSave.getAssociatedComponentTypes().isEmpty() == false) {
			attributeType.setAssociatedComponentTypes(attributeTypeSave.getAssociatedComponentTypes());
		} else if (attributeTypeSave.getAssociatedComponentTypes() == null) {
			attributeType.setAssociatedComponentTypes(attributeTypeSave.getAssociatedComponentTypes());
		}

		return handleAttributePostPutType(attributeType, true);
	}

	@PUT
	@RequireAdmin
	@APIDescription("Updates an attribute type")
	@Consumes({MediaType.APPLICATION_JSON})
	@Path("/attributetypes/{type}")
	public Response updateAttributeType(
			@PathParam("type")
			@RequiredParam String type,
			AttributeTypeSave attributeTypeSave)
	{
		AttributeType existing = service.getPersistenceService().findById(AttributeType.class, type);
		if (existing != null) {
			AttributeType attributeType = attributeTypeSave.getAttributeType();
			if (attributeTypeSave.getComponentTypeRestrictions() != null
					&& attributeTypeSave.getComponentTypeRestrictions().isEmpty() == false) {
				attributeType.setRequiredRestrictions(attributeTypeSave.getComponentTypeRestrictions());
			} else if (attributeTypeSave.getComponentTypeRestrictions() == null) {
				attributeType.setRequiredRestrictions(attributeTypeSave.getComponentTypeRestrictions());
			}
			if (attributeTypeSave.getAssociatedComponentTypes() != null
					&& attributeTypeSave.getAssociatedComponentTypes().isEmpty() == false) {
				attributeType.setAssociatedComponentTypes(attributeTypeSave.getAssociatedComponentTypes());
			} else if (attributeTypeSave.getAssociatedComponentTypes() == null) {
				attributeType.setAssociatedComponentTypes(attributeTypeSave.getAssociatedComponentTypes());
			}
			attributeType.setAttributeType(type);
			return handleAttributePostPutType(attributeType, true);
		} else {
			throw new OpenStorefrontRuntimeException("Unable to find existing type.", "Make sure type exists before call PUT");
		}
	}

	private Response handleAttributePostPutType(AttributeType attributeType, boolean post)
	{
		ValidationModel validationModel = new ValidationModel(attributeType);
		validationModel.setConsumeFieldsOnly(true);
		ValidationResult validationResult = ValidationUtil.validate(validationModel);
		validationResult.merge(attributeType.customValidation());
		if (validationResult.valid()) {
			attributeType.setActiveStatus(LookupEntity.ACTIVE_STATUS);
			attributeType.setCreateUser(SecurityUtil.getCurrentUserName());
			attributeType.setUpdateUser(SecurityUtil.getCurrentUserName());
			service.getAttributeService().saveAttributeType(attributeType, false);
		} else {
			return Response.ok(validationResult.toRestError()).build();
		}
		if (post) {
			AttributeType attributeTypeCreated = service.getPersistenceService().findById(AttributeType.class, attributeType.getAttributeType());
			return Response.created(URI.create("v1/resource/attributes/attributetypes/"
					+ StringProcessor.urlEncode(attributeType.getAttributeType()))).entity(attributeTypeCreated).build();
		} else {
			return Response.ok().build();
		}
	}

	@DELETE
	@RequireAdmin
	@APIDescription("Remove a type (Inactivates).  Note: this inactivates all attribute type associations. Runs in a background task.")
	@Path("/attributetypes/{type}")
	public void deleteAttributeType(
			@PathParam("type")
			@RequiredParam String type)
	{
		AttributeType attributeType = service.getPersistenceService().findById(AttributeType.class, type);
		if (attributeType != null) {
			service.getPersistenceService().setStatusOnEntity(AttributeType.class, type, AttributeType.PENDING_STATUS);

			TaskRequest taskRequest = new TaskRequest();
			taskRequest.setAllowMultiple(false);
			taskRequest.setQueueable(true);
			taskRequest.setName("Inactivating Attribute Type");
			taskRequest.setDetails("Attribute Type: " + type);
			taskRequest.getTaskData().put("Type", type);
			taskRequest.getTaskData().put("Status", attributeType.getActiveStatus());
			taskRequest.setCallback(new AsyncTaskCallback()
			{

				@Override
				public void beforeExecute(TaskFuture taskFuture)
				{
				}

				@Override
				public void afterExecute(TaskFuture taskFuture)
				{
					if (TaskStatus.FAILED.equals(taskFuture.getStatus())) {
						service.getPersistenceService().setStatusOnEntity(AttributeType.class, (String) taskFuture.getTaskData().get("Type"), (String) taskFuture.getTaskData().get("Status"));
					}
				}

			});
			service.getAsyncProxy(service.getAttributeService(), taskRequest).removeAttributeType(type);
		}
	}

	@DELETE
	@RequireAdmin
	@APIDescription("Delete a type and all attribute type associations (codes, component attributes).  Runs in a background task.")
	@Path("/attributetypes/{type}/force")
	public void hardDeleteAttributeType(
			@PathParam("type")
			@RequiredParam String type)
	{
		AttributeType attributeType = service.getPersistenceService().findById(AttributeType.class, type);
		if (attributeType != null) {
			service.getPersistenceService().setStatusOnEntity(AttributeType.class, type, AttributeType.PENDING_STATUS);

			TaskRequest taskRequest = new TaskRequest();
			taskRequest.setAllowMultiple(false);
			taskRequest.setQueueable(true);
			taskRequest.setName("Deleting Attribute Type");
			taskRequest.setDetails("Attribute Type: " + type);
			taskRequest.getTaskData().put("Type", type);
			taskRequest.getTaskData().put("Status", attributeType.getActiveStatus());
			taskRequest.setCallback(new AsyncTaskCallback()
			{

				@Override
				public void beforeExecute(TaskFuture taskFuture)
				{
				}

				@Override
				public void afterExecute(TaskFuture taskFuture)
				{
					if (TaskStatus.FAILED.equals(taskFuture.getStatus())) {
						service.getPersistenceService().setStatusOnEntity(AttributeType.class, (String) taskFuture.getTaskData().get("Type"), (String) taskFuture.getTaskData().get("Status"));
					}
				}

			});
			service.getAsyncProxy(service.getAttributeService(), taskRequest).cascadeDeleteAttributeType(type);
		}
	}

	@POST
	@RequireAdmin
	@APIDescription("Activate a type.  Note: this activates all attribute type associations. Runs in a background task.")
	@Consumes({MediaType.APPLICATION_JSON})
	@Path("/attributetypes/{type}")
	public void activateType(
			@PathParam("type")
			@RequiredParam String type)
	{
		AttributeType attributeType = service.getPersistenceService().findById(AttributeType.class, type);
		if (attributeType != null) {
			service.getPersistenceService().setStatusOnEntity(AttributeType.class, type, AttributeType.PENDING_STATUS);
			TaskRequest taskRequest = new TaskRequest();
			taskRequest.setAllowMultiple(false);
			taskRequest.setQueueable(true);
			taskRequest.setName("Activating Attribute Type");
			taskRequest.setDetails("Attribute Type: " + type);
			taskRequest.getTaskData().put("Type", type);
			taskRequest.getTaskData().put("Status", attributeType.getActiveStatus());
			taskRequest.setCallback(new AsyncTaskCallback()
			{

				@Override
				public void beforeExecute(TaskFuture taskFuture)
				{
				}

				@Override
				public void afterExecute(TaskFuture taskFuture)
				{
					if (TaskStatus.FAILED.equals(taskFuture.getStatus())) {
						service.getPersistenceService().setStatusOnEntity(AttributeType.class, (String) taskFuture.getTaskData().get("Type"), (String) taskFuture.getTaskData().get("Status"));
					}
				}

			});
			service.getAsyncProxy(service.getAttributeService(), taskRequest).activateAttributeType(type);
		}
	}

	@PUT
	@RequireAdmin
	@APIDescription("Updates an attribute code")
	@Consumes({MediaType.APPLICATION_JSON})
	@Path("/attributetypes/{type}/sortorder")
	public Response updateAttributeCode(
			@PathParam("type")
			@RequiredParam String type,
			AttributeTypeView attributeType)
	{
		AttributeCodePk attributeCodePk = new AttributeCodePk();
		attributeCodePk.setAttributeType(type);

		for (AttributeCodeView code : attributeType.getCodes()) {
			attributeCodePk.setAttributeCode(code.getCode());
			service.getAttributeService().saveAttributeCodeSortOrder(attributeCodePk, code.getSortOrder());
		}

		return Response.ok(attributeType).build();
	}

	@POST
	@RequireAdmin
	@APIDescription("Adds a new attribute code")
	@Consumes({MediaType.APPLICATION_JSON})
	@Path("/attributetypes/{type}/attributecodes")
	public Response postAttributeCode(
			@PathParam("type")
			@RequiredParam String type,
			AttributeCode attributeCode)
	{
		attributeCode.getAttributeCodePk().setAttributeType(type);
		return handleAttributePostPutCode(attributeCode, true);
	}

	@POST
	@APIDescription("Creates a new user-generated attribute code")
	@Consumes({MediaType.APPLICATION_JSON})
	@Path("/attributetypes/{type}/attributecodes/user")
	public Response postUserAttributeCode(
			@PathParam("type")
			@RequiredParam String type,
			AttributeCodeSave attributeCodeSave)
	{
		AttributeType attributeType = service.getPersistenceService().findById(AttributeType.class, type);
		if (attributeType != null) {
			// The attribute type must allow user-generated codes to continue
			if (Convert.toBoolean(attributeType.getAllowUserGeneratedCodes())) {
				// Get a list of attribute codes that have this label.
				AttributeCode codeExample = new AttributeCode();
				codeExample.setLabel(attributeCodeSave.getCodeLabel());
				List<AttributeCode> codes = service.getPersistenceService().queryByExample(AttributeCode.class, codeExample);


				// If any of the codes we found belong to the attribute type in question,
				// then the label is not new and we can just return the existing code.

				// If this label appears in another attribute type, we can use the code as a "hint" 
				// for a reasonable string to use for our new program-generated attribute code.
				Queue<String> codeStringHints = new LinkedList<>();
				for (AttributeCode code : codes) {
					if (code.getAttributeCodePk().getAttributeType().equals(type)) {
						return Response.ok(code).build();
					}
					else {
						codeStringHints.add(code.getAttributeCodePk().getAttributeCode());
					}
				}


				// At this point, we have determined that the label is new and therefore
				// we must generate a code for it.
				
				// We add to our candidate list a simple truncated
				// version of the label in uppercase.
				String spaceless = attributeCodeSave.getCodeLabel().replaceAll("\\s+","");
				String truncated = spaceless.substring(0, Math.min(OpenStorefrontConstant.FIELD_SIZE_CODE, spaceless.length())).toUpperCase();
				codeStringHints.add(truncated);

				String newCode = null;
				boolean found = false;
				int num = 1;
				while (!found) {
					if (!codeStringHints.isEmpty()) {
						newCode = codeStringHints.poll();
						found = !(service.getAttributeService().checkIfCodeExistsForType(type, newCode));
					}
					else {
						newCode = truncated.substring(0, truncated.length()-String.valueOf(num).length()).concat(String.valueOf(num));
						found = !(service.getAttributeService().checkIfCodeExistsForType(type, newCode));
						if (!found) num++;
					}
				}


				AttributeCode newAttributeCode = new AttributeCode();
				newAttributeCode.setLabel(attributeCodeSave.getCodeLabel());
				AttributeCodePk newAttributeCodePk = new AttributeCodePk();
				newAttributeCodePk.setAttributeType(type);
				newAttributeCodePk.setAttributeCode(newCode);
				newAttributeCode.setAttributeCodePk(newAttributeCodePk);

				// Save it away and return the new attribute code as response
				
				return handleAttributePostPutCode(newAttributeCode, true, true);
				
			}
			else {
				return Response.status(Response.Status.FORBIDDEN).build();
			}
		}

		return Response.status(Response.Status.NOT_FOUND).build();
	}

	@PUT
	@RequireAdmin
	@APIDescription("Updates an attribute code")
	@Consumes({MediaType.APPLICATION_JSON})
	@Path("/attributetypes/{type}/attributecodes/{code}")
	public Response updateAttributeCode(
			@PathParam("type")
			@RequiredParam String type,
			@PathParam("code")
			@RequiredParam String code,
			AttributeCode attributeCode)
	{
		AttributeCodePk attributeCodePk = new AttributeCodePk();
		attributeCodePk.setAttributeCode(code);
		attributeCodePk.setAttributeType(type);
		attributeCode.setAttributeCodePk(attributeCodePk);
		AttributeCode existing = service.getPersistenceService().findById(AttributeCode.class, attributeCodePk);
		if (existing != null) {
			return handleAttributePostPutCode(attributeCode, false);
		} else {
			throw new OpenStorefrontRuntimeException("Unable to find existing code.", "Make sure type exists before call PUT");
		}
	}

	private Response handleAttributePostPutCode(AttributeCode attributeCode, boolean post) {
		return handleAttributePostPutCode(attributeCode, post, false);
	}
	
	private Response handleAttributePostPutCode(AttributeCode attributeCode, boolean post, boolean alert)
	{
		ValidationModel validationModel = new ValidationModel(attributeCode);
		validationModel.setConsumeFieldsOnly(true);
		ValidationResult validationResult = ValidationUtil.validate(validationModel);
		if (validationResult.valid()) {
			attributeCode.setActiveStatus(LookupEntity.ACTIVE_STATUS);
			attributeCode.setCreateUser(SecurityUtil.getCurrentUserName());
			attributeCode.setUpdateUser(SecurityUtil.getCurrentUserName());
			service.getAttributeService().saveAttributeCode(attributeCode, false);
			if (alert) {
				AlertContext alertContext = new AlertContext();
				alertContext.setAlertType(AlertType.USER_DATA);
				alertContext.setDataTrigger(attributeCode);
				service.getAlertService().checkAlert(alertContext);
			}
		} else {
			return Response.ok(validationResult.toRestError()).build();
		}
		if (post) {
			AttributeCode attributeCodeCreated = service.getPersistenceService().findById(AttributeCode.class, attributeCode.getAttributeCodePk());
			return Response.created(URI.create("v1/resource/attributes/attributetypes/"
					+ StringProcessor.urlEncode(attributeCode.getAttributeCodePk().getAttributeType())
					+ "/attributecodes/"
					+ StringProcessor.urlEncode(attributeCode.getAttributeCodePk().getAttributeCode()))).entity(attributeCodeCreated).build();
		} else {
			return Response.ok(attributeCode).build();
		}
	}

	@DELETE
	@RequireAdmin
	@APIDescription("Remove a Code (Inactivates) and inactivates all attribute type associations. Runs in background.")
	@Path("/attributetypes/{type}/attributecodes/{code}")
	public void deleteAttributeCode(
			@PathParam("type")
			@RequiredParam String type,
			@PathParam("code")
			@RequiredParam String code)
	{
		AttributeCodePk attributeCodePk = new AttributeCodePk();
		attributeCodePk.setAttributeCode(code);
		attributeCodePk.setAttributeType(type);

		AttributeCode attributeCode = service.getPersistenceService().findById(AttributeCode.class, attributeCodePk);
		if (attributeCode != null) {
			service.getPersistenceService().setStatusOnEntity(AttributeCode.class, attributeCodePk, AttributeCode.PENDING_STATUS);

			TaskRequest taskRequest = new TaskRequest();
			taskRequest.setAllowMultiple(false);
			taskRequest.setName("Inactivating Attribute Code");
			taskRequest.setDetails("Type: " + type + " Code: " + code);
			taskRequest.getTaskData().put("Type", type);
			taskRequest.getTaskData().put("Code", code);
			taskRequest.getTaskData().put("Status", attributeCode.getActiveStatus());
			taskRequest.setCallback(new AsyncTaskCallback()
			{

				@Override
				public void beforeExecute(TaskFuture taskFuture)
				{
				}

				@Override
				public void afterExecute(TaskFuture taskFuture)
				{
					if (TaskStatus.FAILED.equals(taskFuture.getStatus())) {

						AttributeCodePk extisingAttributeCodePk = new AttributeCodePk();
						extisingAttributeCodePk.setAttributeCode((String) taskFuture.getTaskData().get("Code"));
						extisingAttributeCodePk.setAttributeType((String) taskFuture.getTaskData().get("Type"));

						service.getPersistenceService().setStatusOnEntity(AttributeCode.class, extisingAttributeCodePk, (String) taskFuture.getTaskData().get("Status"));
					}
				}

			});
			service.getAsyncProxy(service.getAttributeService(), taskRequest).removeAttributeCode(attributeCodePk);
		}
	}

	@DELETE
	@RequireAdmin
	@APIDescription("Delete a Code and all attribute code associations. Runs in background.")
	@Path("/attributetypes/{type}/attributecodes/{code}/force")
	public void hardDeleteAttributeCode(
			@PathParam("type")
			@RequiredParam String type,
			@PathParam("code")
			@RequiredParam String code)
	{
		AttributeCodePk attributeCodePk = new AttributeCodePk();
		attributeCodePk.setAttributeCode(code);
		attributeCodePk.setAttributeType(type);

		AttributeCode attributeCode = service.getPersistenceService().findById(AttributeCode.class, attributeCodePk);
		if (attributeCode != null) {
			service.getPersistenceService().setStatusOnEntity(AttributeCode.class, attributeCodePk, AttributeCode.PENDING_STATUS);

			TaskRequest taskRequest = new TaskRequest();
			taskRequest.setAllowMultiple(false);
			taskRequest.setName("Deleting Attribute Code");
			taskRequest.setDetails("Type: " + type + " Code: " + code);
			taskRequest.getTaskData().put("Type", type);
			taskRequest.getTaskData().put("Code", code);
			taskRequest.getTaskData().put("Status", attributeCode.getActiveStatus());
			taskRequest.setCallback(new AsyncTaskCallback()
			{

				@Override
				public void beforeExecute(TaskFuture taskFuture)
				{
				}

				@Override
				public void afterExecute(TaskFuture taskFuture)
				{
					if (TaskStatus.FAILED.equals(taskFuture.getStatus())) {

						AttributeCodePk extisingAttributeCodePk = new AttributeCodePk();
						extisingAttributeCodePk.setAttributeCode((String) taskFuture.getTaskData().get("Code"));
						extisingAttributeCodePk.setAttributeType((String) taskFuture.getTaskData().get("Type"));

						service.getPersistenceService().setStatusOnEntity(AttributeCode.class, extisingAttributeCodePk, (String) taskFuture.getTaskData().get("Status"));
					}
				}

			});
			service.getAsyncProxy(service.getAttributeService(), taskRequest).cascadeDeleteAttributeCode(attributeCodePk);
		}
	}

	@GET
	@APIDescription("Download the file attachment for an attribute code")
	@Path("/attributetypes/{type}/attributecodes/{code}/attachment")
	public Response downloadAttributeCodeAttachment(
			@PathParam("type")
			@RequiredParam String type,
			@PathParam("code")
			@RequiredParam String code)
	{

		AttributeCodePk attributeCodePk = new AttributeCodePk();
		attributeCodePk.setAttributeType(type);
		attributeCodePk.setAttributeCode(code);
		AttributeCode attributeCode = service.getPersistenceService().findById(AttributeCode.class, attributeCodePk);

		if (attributeCode != null && !attributeCode.getAttachmentOriginalFileName().equals("")) {

			java.nio.file.Path path = attributeCode.pathToAttachment();

			if (path.toFile().exists()) {
				Response.ResponseBuilder response = Response.ok((StreamingOutput) (OutputStream output) -> {
					Files.copy(path, output);
				});
				response.header("Content-Type", attributeCode.getAttachmentMimeType());
				response.header("Content-Disposition", "attachment; filename=\"" + attributeCode.getAttachmentOriginalFileName() + "\"");
				return response.build();
			}

		}
		return Response.status(Response.Status.NOT_FOUND).build();

	}

	@DELETE
	@RequireAdmin
	@APIDescription("Delete the file attachment for an attribute code")
	@Path("/attributetypes/{type}/attributecodes/{code}/attachment")
	public void deleteAttributeCodeAttachment(
			@PathParam("type")
			@RequiredParam String type,
			@PathParam("code")
			@RequiredParam String code)
	{

		AttributeCodePk attributeCodePk = new AttributeCodePk();
		attributeCodePk.setAttributeType(type);
		attributeCodePk.setAttributeCode(code);
		AttributeCode attributeCode = service.getPersistenceService().findById(AttributeCode.class, attributeCodePk);

		if (attributeCode != null) {
			service.getAttributeService().removeAttributeCodeAttachment(attributeCode);
		}

	}

	@POST
	@RequireAdmin
	@APIDescription("Activate a Code and all associated data.  Runs in background.")
	@Consumes({MediaType.APPLICATION_JSON})
	@Path("/attributetypes/{type}/attributecodes/{code}")
	public void activateCode(
			@PathParam("type")
			@RequiredParam String type,
			@PathParam("code")
			@RequiredParam String code)
	{
		AttributeCodePk attributeCodePk = new AttributeCodePk();
		attributeCodePk.setAttributeCode(code);
		attributeCodePk.setAttributeType(type);

		AttributeCode attributeCode = service.getPersistenceService().findById(AttributeCode.class, attributeCodePk);
		if (attributeCode != null) {
			service.getPersistenceService().setStatusOnEntity(AttributeCode.class, attributeCodePk, AttributeCode.PENDING_STATUS);

			TaskRequest taskRequest = new TaskRequest();
			taskRequest.setAllowMultiple(false);
			taskRequest.setName("Activating Attribute Code");
			taskRequest.setDetails("Type: " + type + " Code: " + code);
			taskRequest.getTaskData().put("Type", type);
			taskRequest.getTaskData().put("Code", code);
			taskRequest.getTaskData().put("Status", attributeCode.getActiveStatus());
			taskRequest.setCallback(new AsyncTaskCallback()
			{

				@Override
				public void beforeExecute(TaskFuture taskFuture)
				{
				}

				@Override
				public void afterExecute(TaskFuture taskFuture)
				{
					if (TaskStatus.FAILED.equals(taskFuture.getStatus())) {

						AttributeCodePk extisingAttributeCodePk = new AttributeCodePk();
						extisingAttributeCodePk.setAttributeCode((String) taskFuture.getTaskData().get("Code"));
						extisingAttributeCodePk.setAttributeType((String) taskFuture.getTaskData().get("Type"));

						service.getPersistenceService().setStatusOnEntity(AttributeCode.class, extisingAttributeCodePk, (String) taskFuture.getTaskData().get("Status"));
					}
				}

			});
			service.getAsyncProxy(service.getAttributeService(), taskRequest).activateAttributeCode(attributeCodePk);
		}
	}

	@GET
	@APIDescription("Gets the list of active mapping for attributes to fields")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(AttributeXrefMapView.class)
	@Path("/attributexreftypes/detail")
	public List<AttributeXrefMapView> getMappingTypes()
	{
		List<AttributeXrefMapView> attributeXrefMapViews = new ArrayList<>();

		AttributeXRefType example = new AttributeXRefType();
		example.setActiveStatus(AttributeXRefType.ACTIVE_STATUS);
		List<AttributeXRefType> types = service.getPersistenceService().queryByExample(AttributeXRefType.class, new QueryByExample(example));

		for (AttributeXRefType type : types) {
			AttributeXrefMapView model = new AttributeXrefMapView();
			AttributeType attType = service.getPersistenceService().findById(AttributeType.class, type.getAttributeType());
			model.setAttributeName(attType.getDescription());
			model.setAttributeType(type.getAttributeType());
			model.setFieldName(type.getFieldName());
			model.setFieldId(type.getFieldId());
			model.setIssueType(type.getIssueType());
			model.setProjectType(type.getProjectType());

			AttributeXRefMap tempMap = new AttributeXRefMap();
			tempMap.setActiveStatus(AttributeXRefMap.ACTIVE_STATUS);
			tempMap.setAttributeType(type.getAttributeType());
			model.setMapping(service.getPersistenceService().queryByExample(AttributeXRefMap.class, tempMap));

			attributeXrefMapViews.add(model);
		}

		return attributeXrefMapViews;
	}

	@GET
	@APIDescription("Gets the list of mapping for attributes to fields")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(AttributeXrefMapView.class)
	@Path("/attributexreftypes/detail/distinct")
	public List<AttributeXrefMapView> getDistinctProjectMappings()
	{
		Set<AttributeXrefMapView> attributeXrefMapViews = new HashSet<>();

		AttributeXRefType example = new AttributeXRefType();
		example.setActiveStatus(AttributeXRefType.ACTIVE_STATUS);
		List<AttributeXRefType> types = service.getPersistenceService().queryByExample(AttributeXRefType.class, new QueryByExample(example));

		for (AttributeXRefType type : types) {
			AttributeXrefMapView model = new AttributeXrefMapView();
			model.setIssueType(type.getIssueType());
			model.setProjectType(type.getProjectType());
			attributeXrefMapViews.add(model);
		}

		return new ArrayList<>(attributeXrefMapViews);
	}

	@GET
	@APIDescription("Gets the list of mapping for attributes to fields based on the type passed in.  It will show inactive types as well.")
	@Produces({MediaType.APPLICATION_JSON})
	@DataType(AttributeXrefMapView.class)
	@Path("/attributexreftypes/{attributeType}/detail")
	public Response getMappingType(
			@PathParam("attributeType") String attributeType)
	{
		AttributeXrefMapView model = null;

		AttributeXRefType example = new AttributeXRefType();
		example.setAttributeType(attributeType);
		AttributeXRefType attributeXRefType = service.getPersistenceService().queryOneByExample(AttributeXRefType.class, example);

		if (attributeXRefType != null) {
			model = new AttributeXrefMapView();
			AttributeType attType = service.getPersistenceService().findById(AttributeType.class, attributeXRefType.getAttributeType());
			model.setAttributeName(attType.getDescription());
			model.setAttributeType(attributeXRefType.getAttributeType());
			model.setFieldName(attributeXRefType.getFieldName());
			model.setFieldId(attributeXRefType.getFieldId());
			model.setIssueType(attributeXRefType.getIssueType());
			model.setProjectType(attributeXRefType.getProjectType());

			AttributeXRefMap tempMap = new AttributeXRefMap();
			tempMap.setActiveStatus(AttributeXRefMap.ACTIVE_STATUS);
			tempMap.setAttributeType(attributeXRefType.getAttributeType());
			model.setMapping(service.getPersistenceService().queryByExample(AttributeXRefMap.class, tempMap));
		}
		return sendSingleEntityResponse(model);
	}

	@POST
	@RequireAdmin
	@APIDescription("Save an attribute cross-ref mapping")
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/attributexreftypes/detail")
	public Response saveMapping(
			@RequiredParam AttributeXRefView attributeXref)
	{
		attributeXref.getType().setCreateDts(TimeUtil.currentDate());
		attributeXref.getType().setUpdateDts(TimeUtil.currentDate());
		attributeXref.getType().setCreateUser(SecurityUtil.getCurrentUserName());
		attributeXref.getType().setUpdateUser(SecurityUtil.getCurrentUserName());
		attributeXref.getType().setActiveStatus(ComponentIntegration.ACTIVE_STATUS);
		ValidationModel validationModel = new ValidationModel(attributeXref.getType());
		validationModel.setConsumeFieldsOnly(true);
		ValidationResult validationResult = ValidationUtil.validate(validationModel);

		for (AttributeXRefMap map : attributeXref.getMap()) {
			map.setCreateDts(TimeUtil.currentDate());
			map.setUpdateDts(TimeUtil.currentDate());
			map.setCreateUser(SecurityUtil.getCurrentUserName());
			map.setUpdateUser(SecurityUtil.getCurrentUserName());
			map.setActiveStatus(ComponentIntegration.ACTIVE_STATUS);

			validationModel = new ValidationModel(map);
			validationModel.setConsumeFieldsOnly(true);
			ValidationResult mapValidationResult = ValidationUtil.validate(validationModel);
			validationResult.merge(mapValidationResult);
		}
		if (validationResult.valid()) {
			service.getAttributeService().saveAttributeXrefMap(attributeXref);

			return Response.created(URI.create("v1/resource/attributes/attributexreftypes/"
					+ StringProcessor.urlEncode(attributeXref.getType().getAttributeType())
					+ "/detail")).build();
		} else {
			return Response.ok(validationResult.toRestError()).build();
		}
	}

	@DELETE
	@RequireAdmin
	@APIDescription("Remove a type and all mapping")
	@Path("/attributexreftypes/{attributeType}")
	public void deleteMappingType(
			@PathParam("attributeType")
			@RequiredParam String type)
	{
		service.getAttributeService().deleteAttributeXrefType(type);
	}

}
