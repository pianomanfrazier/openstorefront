/*
 * Copyright 2016 Space Dynamics Laboratory - Utah State University Research Foundation.
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
package edu.usu.sdl.openstorefront.core.model;

import edu.usu.sdl.openstorefront.core.annotation.ConsumeField;
import edu.usu.sdl.openstorefront.core.entity.FileAttributeMap;
import edu.usu.sdl.openstorefront.core.entity.FileDataMap;
import edu.usu.sdl.openstorefront.validation.ValidationResult;

/**
 *
 * @author dshurtleff
 */
public class DataMapModel
{
	@ConsumeField
	private FileDataMap fileDataMap;
	
	@ConsumeField
	private FileAttributeMap fileAttributeMap;

	public DataMapModel()
	{
	}
	
	public ValidationResult validate() 
	{
		ValidationResult result = new ValidationResult();
		if (fileDataMap != null) {
			result.merge(fileDataMap.validate(true));
		}
		
		if (fileAttributeMap != null) {
			result.merge(fileAttributeMap.validate(true));
		}		
		return result;
	}
	
	public FileDataMap getFileDataMap()
	{
		return fileDataMap;
	}

	public void setFileDataMap(FileDataMap fileDataMap)
	{
		this.fileDataMap = fileDataMap;
	}

	public FileAttributeMap getFileAttributeMap()
	{
		return fileAttributeMap;
	}

	public void setFileAttributeMap(FileAttributeMap fileAttributeMap)
	{
		this.fileAttributeMap = fileAttributeMap;
	}

}
