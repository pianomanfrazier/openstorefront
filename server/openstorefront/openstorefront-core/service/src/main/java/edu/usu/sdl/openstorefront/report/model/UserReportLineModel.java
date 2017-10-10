/*
 * Copyright 2017 Space Dynamics Laboratory - Utah State University Research Foundation.
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
package edu.usu.sdl.openstorefront.report.model;

/**
 *
 * @author dshurtleff
 */
public class UserReportLineModel
{
	private String username;
	private String organization;
	private String GUID;
	private String firstName;
	private String lastName;
	private String email;
	private String userType;
	private String firstLoginDate;
	private String lastLoginDate;
	private long activeWatches;
	private long activeReviews;
	private long activeQuestions;
	private long activeQuestionResponse;
	private long tags;
	private long entryViews;

	public UserReportLineModel()
	{
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getOrganization()
	{
		return organization;
	}

	public void setOrganization(String organization)
	{
		this.organization = organization;
	}

	public String getGUID()
	{
		return GUID;
	}

	public void setGUID(String GUID)
	{
		this.GUID = GUID;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getUserType()
	{
		return userType;
	}

	public void setUserType(String userType)
	{
		this.userType = userType;
	}

	public String getFirstLoginDate()
	{
		return firstLoginDate;
	}

	public void setFirstLoginDate(String firstLoginDate)
	{
		this.firstLoginDate = firstLoginDate;
	}

	public String getLastLoginDate()
	{
		return lastLoginDate;
	}

	public void setLastLoginDate(String lastLoginDate)
	{
		this.lastLoginDate = lastLoginDate;
	}

	public long getActiveWatches()
	{
		return activeWatches;
	}

	public void setActiveWatches(long activeWatches)
	{
		this.activeWatches = activeWatches;
	}

	public long getActiveReviews()
	{
		return activeReviews;
	}

	public void setActiveReviews(long activeReviews)
	{
		this.activeReviews = activeReviews;
	}

	public long getActiveQuestions()
	{
		return activeQuestions;
	}

	public void setActiveQuestions(long activeQuestions)
	{
		this.activeQuestions = activeQuestions;
	}

	public long getActiveQuestionResponse()
	{
		return activeQuestionResponse;
	}

	public void setActiveQuestionResponse(long activeQuestionResponse)
	{
		this.activeQuestionResponse = activeQuestionResponse;
	}

	public long getTags()
	{
		return tags;
	}

	public void setTags(long tags)
	{
		this.tags = tags;
	}

	public long getEntryViews()
	{
		return entryViews;
	}

	public void setEntryViews(long entryViews)
	{
		this.entryViews = entryViews;
	}
			
}
