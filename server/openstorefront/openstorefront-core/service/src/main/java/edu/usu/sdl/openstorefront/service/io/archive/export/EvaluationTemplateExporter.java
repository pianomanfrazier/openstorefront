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
package edu.usu.sdl.openstorefront.service.io.archive.export;

import com.fasterxml.jackson.core.type.TypeReference;
import edu.usu.sdl.openstorefront.common.util.StringProcessor;
import edu.usu.sdl.openstorefront.core.entity.EvaluationTemplate;
import edu.usu.sdl.openstorefront.core.entity.Highlight;
import edu.usu.sdl.openstorefront.service.io.archive.BaseExporter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.java.truevfs.access.TFile;
import net.java.truevfs.access.TFileInputStream;
import net.java.truevfs.access.TFileOutputStream;

/**
 *
 * @author dshurtleff
 */
public class EvaluationTemplateExporter
		extends BaseExporter
{

	private static final Logger LOG = Logger.getLogger(EvaluationTemplateExporter.class.getName());
	private static final String DATA_DIR = "/evaluationtemplates/";

	@Override
	public int getPriority()
	{
		return 10;
	}

	@Override
	public String getExporterSupportEntity()
	{
		return EvaluationTemplate.class.getSimpleName();
	}

	@Override
	public List<BaseExporter> getAllRequiredExports()
	{
		List<BaseExporter> exporters = new ArrayList<>();
		exporters.add(new ChecklistQuestionExporter());
		exporters.add(new ChecklistTemplateExporter());
		exporters.add(new SectionTemplateExporter());
		exporters.add(this);
		return exporters;
	}

	@Override
	public void exportRecords()
	{
		EvaluationTemplate evaluationTemplate = new EvaluationTemplate();
		evaluationTemplate.setActiveStatus(Highlight.ACTIVE_STATUS);
		List<EvaluationTemplate> evaluationTemplates = evaluationTemplate.findByExample();

		File dateFile = new TFile(archiveBasePath + DATA_DIR + "templates.json");

		try (OutputStream out = new TFileOutputStream(dateFile)) {
			StringProcessor.defaultObjectMapper().writeValue(out, evaluationTemplates);
		} catch (IOException ex) {
			LOG.log(Level.WARNING, MessageFormat.format("Unable to export eval templates.{0}", ex));
			addError("Unable to export eval templates");
		}

		archive.setRecordsProcessed(archive.getRecordsProcessed() + evaluationTemplates.size());
		archive.setStatusDetails("Exported " + evaluationTemplates.size() + " eval templates");
		archive.save();
	}

	@Override
	public void importRecords()
	{
		File dataDir = new TFile(archiveBasePath + DATA_DIR);
		File files[] = dataDir.listFiles();
		if (files != null) {
			for (File dataFile : files) {
				try (InputStream in = new TFileInputStream(dataFile)) {
					archive.setStatusDetails("Importing: " + dataFile.getName());
					archive.save();

					List<EvaluationTemplate> templates = StringProcessor.defaultObjectMapper().readValue(in, new TypeReference<List<EvaluationTemplate>>()
					{
					});
					for (EvaluationTemplate template : templates) {
						template.save();
					}

					archive.setRecordsProcessed(archive.getRecordsProcessed() + templates.size());
					archive.save();

				} catch (Exception ex) {
					LOG.log(Level.WARNING, "Failed to Load eval templates", ex);
					addError("Unable to load eval templates: " + dataFile.getName());
				}
			}
		} else {
			LOG.log(Level.FINE, "No eval templates to load.");
		}
	}

	@Override
	public long getTotalRecords()
	{
		EvaluationTemplate evaluationTemplate = new EvaluationTemplate();
		evaluationTemplate.setActiveStatus(Highlight.ACTIVE_STATUS);
		return service.getPersistenceService().countByExample(evaluationTemplate);
	}

}
