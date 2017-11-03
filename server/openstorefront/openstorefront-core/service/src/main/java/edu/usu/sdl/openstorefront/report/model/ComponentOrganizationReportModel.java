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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dshurtleff
 */
public class ComponentOrganizationReportModel
		extends BaseReportModel
{

	private List<ComponentOrganizationReportLineModel> data = new ArrayList<>();

	private long totalOrganizations;
	private long totalComponent;

	public ComponentOrganizationReportModel()
	{
	}

	@Override
	public List<ComponentOrganizationReportLineModel> getData()
	{
		return data;
	}

	public void setData(List<ComponentOrganizationReportLineModel> data)
	{
		this.data = data;
	}

	public long getTotalOrganizations()
	{
		return totalOrganizations;
	}

	public void setTotalOrganizations(long totalOrganizations)
	{
		this.totalOrganizations = totalOrganizations;
	}

	public long getTotalComponent()
	{
		return totalComponent;
	}

	public void setTotalComponent(long totalComponent)
	{
		this.totalComponent = totalComponent;
	}

}