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
package edu.usu.sdl.openstorefront.service;

import com.orientechnologies.orient.core.record.impl.ODocument;
import edu.usu.sdl.openstorefront.common.exception.OpenStorefrontRuntimeException;
import edu.usu.sdl.openstorefront.common.manager.PropertiesManager;
import edu.usu.sdl.openstorefront.common.util.Convert;
import edu.usu.sdl.openstorefront.core.api.NotificationService;
import edu.usu.sdl.openstorefront.core.api.query.GenerateStatementOption;
import edu.usu.sdl.openstorefront.core.api.query.QueryByExample;
import edu.usu.sdl.openstorefront.core.api.query.SpecialOperatorModel;
import edu.usu.sdl.openstorefront.core.entity.NotificationEvent;
import edu.usu.sdl.openstorefront.core.entity.NotificationEventReadStatus;
import edu.usu.sdl.openstorefront.core.spi.NotificationEventListerner;
import edu.usu.sdl.openstorefront.core.view.FilterQueryParams;
import edu.usu.sdl.openstorefront.core.view.NotificationEventView;
import edu.usu.sdl.openstorefront.core.view.NotificationEventWrapper;
import edu.usu.sdl.openstorefront.service.manager.OSFCacheManager;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.sf.ehcache.Element;
import org.apache.commons.lang3.StringUtils;

/**
 * Handles Notification Events
 *
 * @author dshurtleff
 */
public class NotificationServiceImpl
		extends ServiceProxy
		implements NotificationService
{

	private static final String LISTENER_KEY = "NOTIFICATION_LISTENERS";

	@Override
	public NotificationEventWrapper getAllEventsForUser(String username, FilterQueryParams queryParams)
	{
		// Initialize Response Object
		NotificationEventWrapper notificationEventWrapper = new NotificationEventWrapper();

		// Initialize Notification Event Query
		String eventQuery = "SELECT FROM " + NotificationEvent.class.getSimpleName() + " WHERE activeStatus = '" + NotificationEvent.ACTIVE_STATUS + "'";

		// Check For Username
		if (username != null) {

			// Add User-Specific Filtering To Query
			eventQuery += " AND (username = '" + username + "' OR (username IS NULL AND roleGroup IS NULL))";
		}

		/////////////////////
		// Get Total Count //
		/////////////////////
		// Modify Query To Get Count
		String eventCountQuery = eventQuery.replace("SELECT FROM", "SELECT COUNT(*) FROM");

		// Request Count
		List<ODocument> countDocuments = persistenceService.query(eventCountQuery, null);

		// Initialize Count Variable
		Long totalCount;

		// Check For Count Results
		if (!countDocuments.isEmpty()) {

			// Set Total Count
			totalCount = countDocuments.get(0).field("COUNT");
		} else {

			// Set Total Count To Zero
			// (Something Happened)
			totalCount = 0L;
		}

		// Set Total Count On Response Object
		notificationEventWrapper.setTotalNumber(totalCount);

		/////////////////////
		// End Total Count //
		/////////////////////
		//////////////////////
		// Sorting & Offset //
		//////////////////////
		// Handle Sorting (In Query)
		eventQuery += " ORDER BY " + queryParams.getSortField() + " " + queryParams.getSortOrder();

		// Handle Offset (In Query)
		eventQuery += " SKIP " + queryParams.getOffset();

		// Handle Limit (In Query)
		eventQuery += " LIMIT " + queryParams.getMax();

		//////////////////////////
		// End Sorting & Offset //
		//////////////////////////
		// Request Notification Events
		List<NotificationEvent> notificationEvents = persistenceService.query(eventQuery, null);

		// Set Result Set Size In Response Object
		notificationEventWrapper.setResults(notificationEvents.size());

		// Set Returned Notification Events In Response Object
		notificationEventWrapper.setData(NotificationEventView.toView(notificationEvents));

		//mark read flag
		Set<String> eventIds = new HashSet<>();
		for (NotificationEventView view : notificationEventWrapper.getData()) {
			eventIds.add(view.getEventId());
		}

		if (eventIds.isEmpty() == false) {
			String query = "select from " + NotificationEventReadStatus.class.getSimpleName() + " where username = :usernameParam and eventId in :eventIdSetParam ";
			Map<String, Object> paramMap = new HashMap<>();
			paramMap.put("usernameParam", username);
			paramMap.put("eventIdSetParam", eventIds);

			List<NotificationEventReadStatus> readStatuses = persistenceService.query(query, paramMap);

			Set<String> readSet = new HashSet<>();
			for (NotificationEventReadStatus readStatus : readStatuses) {
				readSet.add(readStatus.getEventId());
			}

			for (NotificationEventView view : notificationEventWrapper.getData()) {
				if (readSet.contains(view.getEventId())) {
					view.setReadMessage(true);
				}
			}

		}

		//apply read filter
		if (Convert.toBoolean(queryParams.getAll()) == false) {
			notificationEventWrapper.setData(notificationEventWrapper.getData().stream().filter(r -> r.getReadMessage() == false).collect(Collectors.toList()));
		}

		// Return Response
		return notificationEventWrapper;
	}

	@Override
	public void registerNotificationListerner(NotificationEventListerner notificationEventListerner)
	{
		Element element = OSFCacheManager.getApplicationCache().get(LISTENER_KEY);
		if (element == null) {
			List<NotificationEventListerner> listeners = new ArrayList<>();
			element = new Element(LISTENER_KEY, listeners);
			OSFCacheManager.getApplicationCache().put(element);
		}
		((List<NotificationEventListerner>) element.getObjectValue()).add(notificationEventListerner);
	}

	@Override
	public NotificationEvent postEvent(NotificationEvent notificationEvent)
	{
		notificationEvent.setEventId(persistenceService.generateId());
		notificationEvent.populateBaseCreateFields();
		notificationEvent = persistenceService.persist(notificationEvent);

		Element element = OSFCacheManager.getApplicationCache().get(LISTENER_KEY);
		if (element != null) {
			List<NotificationEventListerner> listerners = (List<NotificationEventListerner>) element.getObjectValue();
			for (NotificationEventListerner listerner : listerners) {
				listerner.processEvent(persistenceService.deattachAll(notificationEvent));
			}
		}
		return notificationEvent;
	}

	@Override
	public void deleteEvent(String eventId)
	{
		NotificationEvent notificationEvent = persistenceService.findById(NotificationEvent.class, eventId);
		if (notificationEvent != null) {

			NotificationEventReadStatus notificationEventReadStatus = new NotificationEventReadStatus();
			notificationEventReadStatus.setEventId(eventId);
			persistenceService.deleteByExample(notificationEventReadStatus);

			persistenceService.delete(notificationEvent);
		}
	}

	@Override
	public void cleanupOldEvents()
	{
		int maxDays = Convert.toInteger(PropertiesManager.getValueDefinedDefault(PropertiesManager.KEY_NOTIFICATION_MAX_DAYS));

		LocalDateTime archiveTime = LocalDateTime.now();
		archiveTime = archiveTime.minusDays(maxDays);
		archiveTime = archiveTime.truncatedTo(ChronoUnit.DAYS);
		String deleteQuery = "updateDts < :maxUpdateDts";

		ZonedDateTime zdt = archiveTime.atZone(ZoneId.systemDefault());
		Date archiveDts = Date.from(zdt.toInstant());

		Map<String, Object> queryParams = new HashMap<>();
		queryParams.put("maxUpdateDts", archiveDts);

		persistenceService.deleteByQuery(NotificationEvent.class, deleteQuery, queryParams);
	}

	@Override
	public void markEventAsRead(String eventId, String username)
	{
		Objects.requireNonNull(eventId);
		Objects.requireNonNull(username);

		NotificationEventReadStatus notificationEventReadStatus = new NotificationEventReadStatus();
		notificationEventReadStatus.setReadStatusId(persistenceService.generateId());
		notificationEventReadStatus.setEventId(eventId);
		notificationEventReadStatus.setUsername(username);

		persistenceService.persist(notificationEventReadStatus);
	}

	@Override
	public void markEventAsUnRead(String eventId, String username)
	{
		Objects.requireNonNull(eventId);
		Objects.requireNonNull(username);

		NotificationEventReadStatus notificationEventReadStatus = new NotificationEventReadStatus();
		notificationEventReadStatus.setEventId(eventId);
		notificationEventReadStatus.setUsername(username);

		NotificationEventReadStatus temp = notificationEventReadStatus.findProxy();

		persistenceService.delete(temp);
	}

	@Override
	public void deleteEventsForUser(String username)
	{
		if (StringUtils.isNotBlank(username)) {
			//mark all event global events and read
			NotificationEvent notificationEventExample = new NotificationEvent();
			notificationEventExample.setActiveStatus(NotificationEvent.ACTIVE_STATUS);
			QueryByExample queryByExample = new QueryByExample(notificationEventExample);

			NotificationEvent notificationNotExample = new NotificationEvent();
			notificationNotExample.setUsername(QueryByExample.STRING_FLAG);
			notificationNotExample.setRoleGroup(QueryByExample.STRING_FLAG);
			SpecialOperatorModel specialOperatorModel = new SpecialOperatorModel(notificationNotExample);
			specialOperatorModel.getGenerateStatementOption().setOperation(GenerateStatementOption.OPERATION_NULL);
			queryByExample.getExtraWhereCauses().add(specialOperatorModel);

			List<NotificationEvent> notificationEvents = persistenceService.queryByExample(queryByExample);
			for (NotificationEvent notificationEvent : notificationEvents) {
				markEventAsRead(notificationEvent.getEventId(), username);
			}

			//delete user events
			NotificationEvent notificationEvent = new NotificationEvent();
			notificationEvent.setUsername(username);
			persistenceService.deleteByExample(notificationEvent);
		} else {
			throw new OpenStorefrontRuntimeException("Username is required.", "Check data passed in.");
		}

	}

}
