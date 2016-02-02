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

/* global Ext */

Ext.define('OSF.component.template.BaseBlock', {
	extend: 'Ext.panel.Panel',
	alias: 'osf.widget.template.BaseBlock',
	
	updateHandler: function(entry){
		return entry;
	},
		
	initComponent: function () {
		this.callParent();
	},
	
	updateTemplate: function (entry) {
		var block = this;
		var data = block.updateHandler(entry);
		if (data) {
			block.update(data);
		}
	}
	
});

Ext.define('OSF.component.template.Description', {
	extend: 'OSF.component.template.BaseBlock',
	alias: 'osf.widget.template.Description',
	
	tpl: new Ext.XTemplate(
		'<h2>Description</h2>',	
		'{description}'	
	),
		
	initComponent: function () {
		this.callParent();
	}	
	
});

Ext.define('OSF.component.template.Resources', {
	extend: 'OSF.component.template.BaseBlock',
	alias: 'osf.widget.template.Resources',
	
	titleCollapse: true,
	collapsible: true,
	title: 'Location of Entry Artifacts',
	
	tpl: new Ext.XTemplate(
		' <table class="details-table" width="100%">',	
		'	<tr><th class="details-table">Name</th><th class="details-table">Link</th></tr>',
		'	<tpl for="resources">',	
		'		<tr class="details-table">',
		'			<td class="details-table"><b>{resourceTypeDesc}</b></td>',
		'			<td class="details-table"><a href="actualLink" class="details-table" target="_blank">{link}</a></td>',
		'		</tr>',
		'	</tpl>',
		'</table>'		
	),
		
	initComponent: function () {
		this.callParent();
	},
	
	updateHandler: function(entry){
		if (!entry.resources || entry.resources.length === 0) {
			this.setHidden(true);
		}		
		Ext.Array.sort(entry.resources, function(a, b){
			return a.resourceTypeDesc.localeCompare(b.resourceTypeDesc);	
		});		
		return entry;
	}	
	
});

Ext.define('OSF.component.template.Contacts', {
	extend: 'OSF.component.template.BaseBlock',
	alias: 'osf.widget.template.Contacts',
	
	titleCollapse: true,
	collapsible: true,
	title: 'Points of Contact',
	
	tpl: new Ext.XTemplate(
		' <table class="details-table" width="100%">',	
		'	<tr><th class="details-table">Name</th><th class="details-table">Position</th><th class="details-table">Phone</th><th class="details-table">Email</th></tr>',
		'	<tpl for="contacts">',	
		'		<tr class="details-table">',
		'			<td class="details-table"><b>{name}</b> <br> ({organization})</td>',
		'			<td class="details-table">{positionDescription}</td>',
		'			<td class="details-table"><tpl if="phone">{phone)</tpl><tpl if="!phone">—</tpl></td>',
		'			<td class="details-table"><a href="mailto:{email}" class="details-table">{email}</a></td>',
		'		</tr>',
		'	</tpl>',
		'</table>'
	),
		
	initComponent: function () {
		this.callParent();
	},
	
	updateHandler: function(entry){
		if (!entry.contacts || entry.contacts.length === 0) {
			this.setHidden(true);
		}
		Ext.Array.each(entry.contacts, function(contact){
			if (!contact.phone){
				contact.phone = null;
			}
		});
		
		Ext.Array.sort(entry.contacts, function(a, b){
			return a.name.localeCompare(b.name);	
		});				
		return entry;
	}	
	
});

Ext.define('OSF.component.template.Vitals', {
	extend: 'OSF.component.template.BaseBlock',
	alias: 'osf.widget.template.Vitals',
	
	titleCollapse: true,
	collapsible: true,
	title: 'Entry Vitals',
	
	tpl: new Ext.XTemplate(
		' <table class="details-table" width="100%">',			
		'	<tpl for="vitals">',	
		'		<tr class="details-table">',
		'			<td class="details-table"><b>{label}</b> <tpl if="tip"><i class="fa fa-question-circle" data-qtip="{tip}" data-qtitle="{value}" data-qalignTarget="bl-tl" data-qclosable="true" ></i></tpl></td>',
		'			<td class="details-table highlight-{highlightStyle}"><a href="#" class="details-table" onclick="">{value}</a></td>',
		'		</tr>',
		'	</tpl>',
		'</table>'		
	),
		
	initComponent: function () {
		this.callParent();
	},
	
	updateHandler: function(entry){
		if ((!entry.attributes || entry.attributes.length === 0) && 
				(!entry.metadata || entry.metadata.length === 0)) {
			this.setHidden(true);
		}
		
		//normalize and sort
		var vitals = [];
		if (entry.attributes) {
			Ext.Array.each(entry.attributes, function(item){
				vitals.push({
					label: item.typeDescription,
					value: item.codeDescription,
					highlightStyle: item.highlightStyle,
					type: item.type,
					code: item.code,
					tip: item.codeLongDescription
				});
			});
		}
		
		if (entry.metadata) {
			Ext.Array.each(entry.metadata, function(item){
				vitals.push({
					label: item.label,
					value: item.value
				});			
			});
		}
		
		Ext.Array.sort(vitals, function(a, b){
			return a.label.localeCompare(b.label);	
		});
		entry.vitals = vitals;
		
		return entry;
	}	
	
});

Ext.define('OSF.component.template.Dependencies', {
	extend: 'OSF.component.template.BaseBlock',
	alias: 'osf.widget.template.Dependencies',
	
	titleCollapse: true,
	collapsible: true,
	title: 'External Dependencies',
	
	tpl: new Ext.XTemplate(
		' <table class="details-table" width="100%">',			
		'	<tpl for="dependencies">',	
		'		<tr class="details-table">',
		'			<td class="details-table"><b>{dependencyName} {version}</b> <br>',
		'			<tpl if="dependancyReferenceLink"><a href="{dependancyReferenceLink}" class="details-table" target="_blank">{dependancyReferenceLink}</a><br></tpl> ',
		'			<tpl if="comment">{comment}</tpl> ',
		'			</td>',
		'		</tr>',
		'	</tpl>',
		'</table>'
	),
		
	initComponent: function () {
		this.callParent();
	},
	
	updateHandler: function(entry){
		if (!entry.dependencies || entry.dependencies.length === 0) {
			this.setHidden(true);
		}
		Ext.Array.sort(entry.dependencies, function(a, b){
			return a.dependencyName.localeCompare(b.dependencyName);	
		});				
		return entry;
	}	
	
});

Ext.define('OSF.component.template.DI2EEvalLevel', {
	extend: 'OSF.component.template.BaseBlock',
	alias: 'osf.widget.template.DI2EEvalLevel',
	
	titleCollapse: true,
	collapsible: true,
	title: 'DI2E Evaluation Level',
	
	tpl: new Ext.XTemplate(
		' <table class="details-table" width="100%">',					
		'		<tr class="details-table">',
		'			<th class="details-table"><b>{evalLevels.level.typeDesciption}</b></th>',
		'			<td class="details-table highlight-{evalLevels.level.highlightStyle}" ><h3>{evalLevels.level.label}</h3>{evalLevels.level.description}</td>',
		'		</tr>',	
		'		<tr class="details-table">',
		'			<th class="details-table"><b>{evalLevels.state.typeDesciption}</b></th>',
		'			<td class="details-table highlight-{evalLevels.state.highlightStyle}" ><h3>{evalLevels.state.label}</h3>{evalLevels.state.description}</td>',
		'		</tr>',	
		'		<tr class="details-table">',
		'			<th class="details-table"><b>{evalLevels.intent.typeDesciption}</b></th>',
		'			<td class="details-table highlight-{evalLevels.intent.highlightStyle}" ><h3>{evalLevels.intent.label}</h3>{evalLevels.intent.description}</td>',
		'		</tr>',			
		'</table>'		
	),
		
	initComponent: function () {
		this.callParent();
	},
	
	updateHandler: function(entry){
		
		var evalLevels = {};		
		if (!entry.attributes) {
			this.setHidden(true);
		} else {
			Ext.Array.each(entry.attributes, function(item){
				if (item.type === 'DI2ELEVEL') {
					evalLevels.level = {};
					evalLevels.level.typeDesciption = item.typeDescription; 
					evalLevels.level.code = item.code; 
					evalLevels.level.label = item.codeDescription; 
					evalLevels.level.description = item.codeLongDescription;
					evalLevels.level.highlightStyle = item.highlightStyle;
				} else if (item.type === 'DI2ESTATE') {
					evalLevels.state = {};
					evalLevels.state.typeDesciption = item.typeDescription; 
					evalLevels.state.code = item.code; 
					evalLevels.state.label = item.codeDescription; 
					evalLevels.state.description = item.codeLongDescription;
					evalLevels.state.highlightStyle = item.highlightStyle;
				} else if (item.type === 'DI2EINTENT') {
					evalLevels.intent = {};
					evalLevels.intent.typeDesciption = item.typeDescription; 
					evalLevels.intent.code = item.code; 
					evalLevels.intent.label = item.codeDescription; 
					evalLevels.intent.description = item.codeLongDescription; 
					evalLevels.intent.highlightStyle = item.highlightStyle;
				}
			});			
		}
		entry.evalLevels = evalLevels;
				
		return entry;
	}	
	
});

Ext.define('OSF.component.template.EvaluationSummary', {
	extend: 'OSF.component.template.BaseBlock',
	alias: 'osf.widget.template.EvaluationSummary',
	
	titleCollapse: true,
	collapsible: true,
	title: 'Reusability Factors (5=best)',
	
	tpl: new Ext.XTemplate(
		'<div class="rolling-container">',			
		'	<div class="rolling-container-row">',
		'		<tpl for="evaluation.evaluationSections">',	
		'			<div class="rolling-container-block">',
		'				<div class="detail-eval-item ">',
		'					<span class="detail-eval-label">{name} <tpl if="sectionDescription"><i class="fa fa-question-circle" data-qtip="{sectionDescription}" data-qtitle="{name}" data-qalignTarget="bl-tl" data-qclosable="true" ></i></tpl></span>',
		'					<span class="detail-eval-score" data-qtip="{actualScore}">{display}</span>',	
		'				</div>',	
		'			</div>',
		'		</tpl>',
		'	</div>',
		'</div>'
	),
		
	initComponent: function () {
		this.callParent();
	},
	
	updateHandler: function(entry){
		if (!entry.evaluation || entry.evaluation.evaluationSections.length === 0) {
			this.setHidden(true);		
			return null;
		} else {
			Ext.Array.each(entry.evaluation.evaluationSections, function(section){
				if (section.notAvailable || section.actualScore <= 0) {
					section.display = null;
				} else {
					var score = Math.round(section.actualScore);
					section.display = "";
					for (var i= 0; i<score; i++){
						section.display += '<i class="fa fa-circle detail-evalscore"></i>';
					}
				}				
			});
			
			
			Ext.Array.sort(entry.evaluation.evaluationSections, function(a, b){
				return a.name.localeCompare(b.name);	
			});
			return entry;
		}
	}	
	
});

Ext.define('OSF.component.template.Media', {
	extend: 'OSF.component.template.BaseBlock',
	alias: 'osf.widget.template.Media',
	
	scrollable: 'x',
	
	tpl: new Ext.XTemplate(
		' <h2>Screenshots / Media</h2>',	
		'	<tpl for="componentMedia">',	
		'		<div class="detail-media-block">',
		'		<tpl switch="mediaTypeCode">',
		'				<tpl case="IMG">',
		'					<img src="{link}" height="150" alt="{[values.caption ? values.caption : values.filename]}" onclick="MediaViewer(\'{mediaTypeCode}\', \'{link}\', \'{caption}\', \'{filename}\');" />',		
		'				<tpl case="AUD">',
		'					<i class="fa fa-sound-o" onclick=""></i>',
		'				<tpl case="VID">',
		'					<i class="fa fa-video-o" onclick=""></i>',		
		'				<tpl case="ARC">',
		'					<i class="fa fa-archive-o" onclick=""></i>',
		'				<tpl case="TEX">',
		'					<i class="fa fa-text-o" onclick=""></i>',
		'				<tpl case="OTH">',
		'					<i class="fa fa-file-o" onclick=""></i>',
		'			</tpl>',
		'			<tpl if="caption"><p class="detail-media-caption">{caption}</p></tpl>',
		'		</div>',
		'	</tpl>'
	),
		
	initComponent: function () {
		this.callParent();
	},
	
	updateHandler: function(entry){
		if (!entry.componentMedia || entry.componentMedia.length === 0) {
			this.setHidden(true);
		}
	
		return entry;
	}	
	
});