package com.kbj.restapi.events;

import com.kbj.restapi.accounts.AccountRepository;
import com.kbj.restapi.accounts.AccountService;
import com.kbj.restapi.common.AppProperties;
import com.kbj.restapi.common.BaseControllerTests;
import com.kbj.restapi.common.TestDescription;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class EventControllerTests extends BaseControllerTests {

  @Autowired
  EventRepository eventRepository;

  @Autowired
  AccountService accountService;

  @Autowired
  AccountRepository accountRepository;

  @Autowired
  AppProperties appProperties;


  @Test
  @TestDescription("정상적으로 이벤트를 생성하는 테스트")
  public void createEvent() throws Exception {
    EventDto event = EventDto.builder()
      .name("Spring")
      .description("hihihi")
      .beginEnrollmentDateTime(LocalDateTime.of(2020, 2, 27, 11, 29, 30))
      .closeEnrollmentDateTime(LocalDateTime.of(2020, 2, 28, 0, 0, 0))
      .beginEventDateTime(LocalDateTime.of(2020, 3, 1, 8, 0, 0, 0))
      .endEventDateTime(LocalDateTime.of(2020, 3, 2, 0, 0, 0, 0))
      .basePrice(100)
      .maxPrice(200)
      .limitOfEnrollment(100)
      .location("강남역")
      .build();

    //event.setId(10);
    //Mockito.when(eventRepository.save(event)).thenReturn(event);

    this.mockMvc.perform(post("/api/events/")
            .header(HttpHeaders.AUTHORIZATION, getAccessToken())
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaTypes.HAL_JSON)
      .content(objectMapper.writeValueAsString(event)))
      .andDo(print())
      .andExpect(status().isCreated())
      .andExpect(header().exists(HttpHeaders.LOCATION))
      .andExpect(jsonPath("id").exists())
      .andExpect(jsonPath("free").value(false))
      .andExpect(jsonPath("offline").value(true))
      .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
      //.andExpect(jsonPath("_links.self").exists())
      //.andExpect(jsonPath("_links.query-events").exists())
      //.andExpect(jsonPath("_links.update-event").exists())
      .andDo(document("create-event",
              links(
                      linkWithRel("self").description("link to self"),
                      linkWithRel("query-events").description("link to query events"),
                      linkWithRel("update-event").description("link to update an existing"),
                      linkWithRel("profile").description("link to profile")
              ),
              requestHeaders(
                      headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                      headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
              ),
              requestFields(
                      fieldWithPath("name").description("Name of new event"),
                      fieldWithPath("description").description("description of new event"),
                      fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                      fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
                      fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                      fieldWithPath("endEventDateTime").description("date time of end of new event"),
                      fieldWithPath("location").description("location of new event"),
                      fieldWithPath("basePrice").description("base price of new event"),
                      fieldWithPath("maxPrice").description("max price of new event"),
                      fieldWithPath("limitOfEnrollment").description("limit of enrollment of new event")
              ),
              responseHeaders(
                      headerWithName(HttpHeaders.LOCATION).description("location header"),
                      headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
              ),
              responseFields(
                      fieldWithPath("id").description("Id of new event"),
                      fieldWithPath("name").description("Name of new event"),
                      fieldWithPath("description").description("description of new event"),
                      fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                      fieldWithPath("closeEnrollmentDateTime").description("date time of close of new event"),
                      fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                      fieldWithPath("endEventDateTime").description("date time of end of new event"),
                      fieldWithPath("location").description("location of new event"),
                      fieldWithPath("basePrice").description("base price of new event"),
                      fieldWithPath("maxPrice").description("max price of new event"),
                      fieldWithPath("limitOfEnrollment").description("limit of enrollment of new event"),
                      fieldWithPath("free").description("It tells is this event is free or not"),
                      fieldWithPath("offline").description("It tells is this event is offline or not"),
                      fieldWithPath("eventStatus").description("event status"),
                      fieldWithPath("manager.id").description("event manager id"),
                      fieldWithPath("_links.self.href").description("link to self"),
                      fieldWithPath("_links.query-events.href").description("link to query events"),
                      fieldWithPath("_links.update-event.href").description("link to update an existing"),
                      fieldWithPath("_links.profile.href").description("link to profile")
              )
            )
      )
      ;
  }

  private String getAccessToken() throws Exception {
    ResultActions perform = this.mockMvc.perform(post("/oauth/token")
            .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
            .param("username", appProperties.getUserUsername())
            .param("password", appProperties.getUserPassword())
            .param("grant_type", "password"));

    String content = perform.andReturn().getResponse().getContentAsString();
    Jackson2JsonParser parser = new Jackson2JsonParser();
    String access_token = parser.parseMap(content).get("access_token").toString();
    return "Bearer " + access_token;
  }


  @Test
  @TestDescription("입력 받을 수 없는 값을 사용한 경우 에러가 발생하는 테스트")
  public void createEvent_Bad_Request() throws Exception {
    Event event = Event.builder()
      .id(100)
      .name("Spring")
      .beginEnrollmentDateTime(LocalDateTime.of(2020, 2, 27, 11, 29, 30))
      .closeEnrollmentDateTime(LocalDateTime.of(2020, 2, 28, 0, 0, 0))
      .beginEventDateTime(LocalDateTime.of(2020, 3, 1, 8, 0, 0, 0))
      .endEventDateTime(LocalDateTime.of(2020, 3, 2, 0, 0, 0, 0))
      .basePrice(100)
      .maxPrice(200)
      .limitOfEnrollment(100)
      .location("강남역")
      .free(true)
      .offline(false)
      .eventStatus(EventStatus.PUBLISHED)
      .build();

    //event.setId(10);
    //Mockito.when(eventRespository.save(event)).thenReturn(event);

    mockMvc.perform(post("/api/events/")
            .header(HttpHeaders.AUTHORIZATION, getAccessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(event))
            .accept(MediaTypes.HAL_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest());
  }


  @Test
  @TestDescription("입력값이 비어있는 경우 에러가 발생하는 테스트")
  public void createEvent_Bad_Request_Empty_Input() throws Exception {
    EventDto eventDto = EventDto.builder()
      .build();

    this.mockMvc.perform(
        post("/api/events/").contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAccessToken())
          .content(objectMapper.writeValueAsString(eventDto))
          .accept(MediaTypes.HAL_JSON)
      )
      .andExpect(status().isBadRequest());
  }


  @Test
  @TestDescription("입력 값이 잘못된 경우에 에러가 발생하는 테스트")
  public void createEvent_Bad_Request_Wrong_Input() throws Exception {
    EventDto eventDto = EventDto.builder()
        .name("Spring")
        .description("Hello REST API")
        .beginEnrollmentDateTime(LocalDateTime.of(2020, 2, 26, 11, 29, 30))
        .closeEnrollmentDateTime(LocalDateTime.of(2020, 2, 25, 0, 0, 0))
        .beginEventDateTime(LocalDateTime.of(2020, 2, 24, 8, 0, 0, 0))
        .endEventDateTime(LocalDateTime.of(2020, 2, 23, 0, 0, 0, 0))
        .basePrice(10000)
        .maxPrice(200)
        .limitOfEnrollment(100)
        .location("강남역")
        .build();

    this.mockMvc.perform(post("/api/events/").contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, getAccessToken())
        .content(objectMapper.writeValueAsString(eventDto))
        .accept(MediaTypes.HAL_JSON))
        .andExpect(status().isBadRequest())
        .andDo(print())
        .andExpect(jsonPath("content[0].objectName").exists())
        .andExpect(jsonPath("content[0].field").exists())
        .andExpect(jsonPath("content[0].defaultMessage").exists())
        .andExpect(jsonPath("content[0].code").exists())
        .andExpect(jsonPath("content[0].rejectedValue").exists())
        .andExpect(jsonPath("_links.index").exists())
    ;
  }

  @Test
  @TestDescription("30개의 이벤트를 10개씩 2번째 페이지 조회하기")
  public void queryEvents() throws Exception {
    // Given
    IntStream.range(0, 30).forEach(this::generateEvent);

    this.mockMvc.perform(get("/api/events")
            .param("page", "1")
            .param("size", "10")
            .param("sort", "name,DESC"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("page").exists())
            .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
            .andExpect(jsonPath("_links.self").exists())
            .andExpect(jsonPath("_links.profile").exists())
            .andDo(document("query-events"))
    ;
  }


  @Test
  @TestDescription("기존의 이벤트를 하나 조회하기")
  public void queryEvent() throws Exception {
    // Given
    Event event = this.generateEvent(100);

    // When & Then
    this.mockMvc.perform(get("/api/events/{id}", event.getId()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("id").exists())
            .andExpect(jsonPath("name").exists())
            .andExpect(jsonPath("_links.self").exists())
            .andExpect(jsonPath("_links.profile").exists())
            .andDo(document("get-an-event"))
    ;
  }


  @Test
  @TestDescription("없는 이벤트를 조회할 경우 404 응답받기")
  public void queryEvent404() throws Exception {
    this.mockMvc.perform(get("/api/events/{id}", 1234567))
            .andDo(print())
            .andExpect(status().isNotFound())
    ;
  }


  @Test
  @TestDescription("이벤트 수정하기")
  public void updateEvent() throws Exception {
    Event event = this.generateEvent(100);

    String name = "Hello name!!";
    String description = "Hello description";
    EventDto eventDto = this.modelMapper.map(event, EventDto.class);
    eventDto.setName(name);
    eventDto.setDescription(description);

    this.mockMvc.perform(put("/api/events/{id}", event.getId())
            .header(HttpHeaders.AUTHORIZATION, getAccessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(eventDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("id").exists())
            .andExpect(jsonPath("name").value(name))
            .andExpect(jsonPath("description").value(description))
            .andExpect(jsonPath("_links.self").exists())
            .andExpect(jsonPath("_links.profile").exists())
            .andDo(document("update-event"))
    ;
  }

  private Event generateEvent(int i) {
    Event event = Event.builder()
            .name("Name " + i)
            .description("Description " + i)
            .beginEnrollmentDateTime(LocalDateTime.of(2020, 2, 27, 11, 29, 30))
            .closeEnrollmentDateTime(LocalDateTime.of(2020, 2, 28, 0, 0, 0))
            .beginEventDateTime(LocalDateTime.of(2020, 3, 1, 8, 0, 0, 0))
            .endEventDateTime(LocalDateTime.of(2020, 3, 2, 0, 0, 0, 0))
            .basePrice(100)
            .maxPrice(200)
            .limitOfEnrollment(100)
            .location("강남역")
            .build();

    return this.eventRepository.save(event);
  }
}