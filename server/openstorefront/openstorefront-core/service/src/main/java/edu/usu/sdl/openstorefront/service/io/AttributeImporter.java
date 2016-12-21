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
package edu.usu.sdl.openstorefront.service.io;

import edu.usu.sdl.openstorefront.common.manager.FileSystemManager;
import edu.usu.sdl.openstorefront.common.manager.Initializable;
import edu.usu.sdl.openstorefront.core.entity.ApplicationProperty;
import edu.usu.sdl.openstorefront.core.entity.AttributeCode;
import edu.usu.sdl.openstorefront.core.entity.AttributeType;
import edu.usu.sdl.openstorefront.service.io.parser.MainAttributeParser;
import edu.usu.sdl.openstorefront.service.io.parser.OldBaseAttributeParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Imports Attributes
 *
 * @author dshurtleff
 */
public class AttributeImporter
		extends BaseDirImporter
		implements Initializable
{

	private static final Logger LOG = Logger.getLogger(AttributeImporter.class.getName());

	@Override
	public void initialize()
	{
		String lastSyncDts = serviceProxy.getSystemService().getPropertyValue(ApplicationProperty.ATTRIBUTE_IMPORTER_LAST_SYNC_DTS);
		if (lastSyncDts == null) {
			//get the files and process.
			List<File> attributeFiles = new ArrayList<>();
			for (FileMap fileMap : FileMap.values()) {
				attributeFiles.add(FileSystemManager.getImportAttribute(fileMap.getFilename()));
			}
			filesUpdatedOrAdded(attributeFiles.toArray(new File[0]));
		}
	}

	@Override
	public void shutdown()
	{
	}

	@Override
	protected String getSyncProperty()
	{
		return ApplicationProperty.ATTRIBUTE_IMPORTER_LAST_SYNC_DTS;
	}

	@Override
	protected void processFile(File file)
	{
		//log
		LOG.log(Level.INFO, MessageFormat.format("Syncing Attributes: {0}", file));
		for (FileMap fileMap : FileMap.values()) {
			if (fileMap.getFilename().equals(file.getName())) {
				try (InputStream in = new FileInputStream(file)) {
					Map<AttributeType, List<AttributeCode>> attributeMap = fileMap.getParser().parse(in);
					serviceProxy.getAttributeService().syncAttribute(attributeMap);
				} catch (IOException ex) {
					LOG.log(Level.SEVERE, "Failed processing file: " + file, ex);
				}
			}
		}
		LOG.log(Level.INFO, MessageFormat.format("Finish Syncing Attributes: {0}", file));
	}

	@Override
	public boolean isStarted()
	{
		return true;
	}

	private enum FileMap
	{

		ATTIBUTES("allattributes.json", new MainAttributeParser());

		private final String filename;
		private final OldBaseAttributeParser parser;

		private FileMap(String filename, OldBaseAttributeParser parser)
		{
			this.filename = filename;
			this.parser = parser;
		}

		public String getFilename()
		{
			return filename;
		}

		public OldBaseAttributeParser getParser()
		{
			return parser;
		}

	}

}
