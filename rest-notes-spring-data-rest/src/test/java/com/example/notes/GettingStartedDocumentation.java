/*
 * Copyright 2014 the original author or authors.
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

package com.example.notes;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.restdocs.core.RestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.hateoas.MediaTypes;
import org.springframework.restdocs.core.RestDocumentationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = RestNotesSpringDataRest.class)
@WebAppConfiguration
public class GettingStartedDocumentation {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@Before
	public void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
				.apply(new RestDocumentationConfiguration()).build();
	}

	@Test
	public void index() throws Exception {
		this.mockMvc.perform(get("/").accept(MediaTypes.HAL_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("_links.notes", is(notNullValue())))
				.andExpect(jsonPath("_links.tags", is(notNullValue())))
				.andDo(document("index"));
	}

	@Test
	public void creatingANote() throws JsonProcessingException, Exception {
		String noteLocation = createNote();
		getNote(noteLocation);

		String tagLocation = createTag();
		getTag(tagLocation);

		String taggedNoteLocation = createTaggedNote(tagLocation);
		getTaggedNote(taggedNoteLocation);
		getTags(taggedNoteLocation);

		tagExistingNote(noteLocation, tagLocation);
		getTaggedExistingNote(noteLocation);
		getTagsForExistingNote(noteLocation);
	}

	String createNote() throws Exception {
		Map<String, String> note = new HashMap<String, String>();
		note.put("title", "Note creation with cURL");
		note.put("body", "An example of how to create a note using cURL");

		String noteLocation = this.mockMvc
				.perform(
						post("/notes").contentType(MediaTypes.HAL_JSON).content(
								objectMapper.writeValueAsString(note)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", notNullValue()))
				.andDo(document("create-note"))
				.andReturn().getResponse().getHeader("Location");
		return noteLocation;
	}

	void getNote(String noteLocation) throws Exception {
		this.mockMvc.perform(get(noteLocation))
			.andExpect(status().isOk())
			.andExpect(jsonPath("title", is(notNullValue())))
			.andExpect(jsonPath("body", is(notNullValue())))
			.andExpect(jsonPath("_links.tags", is(notNullValue())))
			.andDo(document("get-note"));
	}

	String createTag() throws Exception, JsonProcessingException {
		Map<String, String> tag = new HashMap<String, String>();
		tag.put("name", "getting-started");

		String tagLocation = this.mockMvc
				.perform(
						post("/tags").contentType(MediaTypes.HAL_JSON).content(
								objectMapper.writeValueAsString(tag)))
						.andExpect(status().isCreated())
						.andExpect(header().string("Location", notNullValue()))
						.andDo(document("create-tag"))
						.andReturn().getResponse().getHeader("Location");
		return tagLocation;
	}

	void getTag(String tagLocation) throws Exception {
		this.mockMvc.perform(get(tagLocation))
			.andExpect(status().isOk())
			.andExpect(jsonPath("name", is(notNullValue())))
			.andExpect(jsonPath("_links.notes", is(notNullValue())))
			.andDo(document("get-tag"));
	}

	String createTaggedNote(String tag) throws Exception {
		Map<String, Object> note = new HashMap<String, Object>();
		note.put("title", "Tagged note creation with cURL");
		note.put("body", "An example of how to create a tagged note using cURL");
		note.put("tags", Arrays.asList(tag));

		String noteLocation = this.mockMvc
				.perform(
						post("/notes").contentType(MediaTypes.HAL_JSON).content(
								objectMapper.writeValueAsString(note)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", notNullValue()))
				.andDo(document("create-tagged-note"))
				.andReturn().getResponse().getHeader("Location");
		return noteLocation;
	}

	void getTaggedNote(String tagLocation) throws Exception {
		this.mockMvc.perform(get(tagLocation))
			.andExpect(status().isOk())
			.andExpect(jsonPath("title", is(notNullValue())))
			.andExpect(jsonPath("body", is(notNullValue())))
			.andExpect(jsonPath("_links.tags", is(notNullValue())))
			.andDo(document("get-tagged-note"));
	}

	void getTags(String taggedNoteLocation) throws Exception {
		String tagsLocation = getLink(this.mockMvc.perform(get(taggedNoteLocation))
				.andReturn(), "tags");

		this.mockMvc.perform(get(tagsLocation))
			.andExpect(status().isOk())
			.andExpect(jsonPath("_embedded.tags", hasSize(1)))
			.andDo(document("get-tags"));
	}

	void tagExistingNote(String noteLocation, String tagLocation) throws Exception {
		Map<String, Object> update = new HashMap<String, Object>();
		update.put("tags", Arrays.asList(tagLocation));

		this.mockMvc.perform(
				patch(noteLocation).contentType(MediaTypes.HAL_JSON).content(
						objectMapper.writeValueAsString(update)))
				.andExpect(status().isNoContent())
				.andDo(document("tag-existing-note"));

	}

	void getTaggedExistingNote(String tagLocation) throws Exception {
		this.mockMvc.perform(get(tagLocation))
			.andExpect(status().isOk())
			.andDo(document("get-tagged-existing-note"));
	}

	void getTagsForExistingNote(String taggedNoteLocation) throws Exception {
		String tagsLocation = getLink(this.mockMvc.perform(get(taggedNoteLocation))
				.andReturn(), "tags");
		this.mockMvc.perform(get(tagsLocation))
			.andExpect(status().isOk())
			.andExpect(jsonPath("_embedded.tags", hasSize(1)))
			.andDo(document("get-tags-for-existing-note"));
	}

	private String getLink(MvcResult result, String href)
			throws UnsupportedEncodingException {
		return JsonPath.parse(result.getResponse().getContentAsString()).read(
				"_links.tags.href");
	}
}
