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
package edu.usu.sdl.openstorefront.validation;

import edu.usu.sdl.openstorefront.common.exception.OpenStorefrontRuntimeException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.BeanUtils;

/**
 *
 * @author dshurtleff
 */
public abstract class BaseRule
{

	public RuleResult processField(Field field, Object dataObject)
	{
		RuleResult validationResult = null;

		if (validate(field, dataObject) == false) {
			validationResult = createResults(field, dataObject);
		}

		return validationResult;
	}

	protected RuleResult createResults(Field field, Object dataObject)
	{
		RuleResult validationResult = new RuleResult();
		validationResult.setEntityClassName(dataObject.getClass().getSimpleName());
		validationResult.setFieldName(field.getName());
		validationResult.setMessage(getMessage(field));
		validationResult.setValidationRule(getValidationRule(field));
		try {
			validationResult.setInvalidData(BeanUtils.getProperty(dataObject, field.getName()));
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
			throw new OpenStorefrontRuntimeException("Unexpected error occur trying to get value from object.", ex);
		}

		return validationResult;
	}

	protected abstract boolean validate(Field field, Object dataObject);

	protected abstract String getMessage(Field field);

	protected abstract String getValidationRule(Field field);
}
