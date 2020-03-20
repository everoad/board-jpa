package com.kbj.restapi.events;

import com.kbj.restapi.accounts.Account;
import com.kbj.restapi.accounts.CurrentUser;
import com.kbj.restapi.common.ErrorsEntityModel;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;


@Controller @AllArgsConstructor
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE + ";charset=utf-8")
public class EventController {

  private final EventRepository eventRepository;
  private final ModelMapper modelMapper;
  private final EventValidator eventValidator;

  @GetMapping
  public ResponseEntity<?> queryEvents(Pageable pageable, PagedResourcesAssembler<Event> assembler,
                                       @CurrentUser Account currentUser) {
    Page<Event> page = this.eventRepository.findAll(pageable);
    PagedModel<EntityModel<Event>> pagedModel = assembler.toModel(page, e -> new EventEntityModel(e));
    pagedModel.add(new Link("/docs/index.html#resources-events-list").withRel("profile"));
    if (currentUser != null) {
      pagedModel.add(linkTo(EventController.class).withRel("create-event"));
    }
    return ResponseEntity.ok(pagedModel);
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> queryEvent(@PathVariable Integer id,
                                      @CurrentUser Account currentUser) {
    Optional<Event> optionalEvent = this.eventRepository.findById(id);
    if (optionalEvent.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    Event event = optionalEvent.get();
    EventEntityModel eventEntityModel = new EventEntityModel(event);
    eventEntityModel.add(new Link("/docs/index.html#resources-events-get").withRel("profile"));
    if (event.getManager().equals(currentUser)) {
      eventEntityModel.add(linkTo(EventController.class).slash(event.getId()).withRel("update-event"));
    }
    return ResponseEntity.ok(eventEntityModel);
  }

  @PostMapping
  public ResponseEntity<?> createEvent(@RequestBody @Valid EventDto eventDto, Errors errors,
                                       @CurrentUser Account currentUser) {
    if (errors.hasErrors()) {
      return badRequest(errors);
    }

    eventValidator.validate(eventDto, errors);
    if (errors.hasErrors()) {
      return badRequest(errors);
    }

    Event event = modelMapper.map(eventDto, Event.class);
    event.update();
    event.setManager(currentUser);
    Event newEvent = this.eventRepository.save(event);

    WebMvcLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(newEvent.getId());
    URI createUri = linkTo(EventController.class).slash(newEvent.getId()).toUri();
    EventEntityModel model = new EventEntityModel(newEvent);
    model.add(linkTo(EventController.class).withRel("query-events"));
    model.add(selfLinkBuilder.withRel("update-event"));
    model.add(new Link("/docs/index.html#resources-events-create").withRel("profile"));
    return ResponseEntity.created(createUri).body(model);
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> updateEvent(@PathVariable Integer id,
                                       @RequestBody @Valid EventDto eventDto, Errors errors,
                                       @CurrentUser Account currentUser) {
    if (errors.hasErrors()) {
      return badRequest(errors);
    }
    this.eventValidator.validate(eventDto, errors);
    if (errors.hasErrors()) {
      return badRequest(errors);
    }

    Optional<Event> optionalEvent = this.eventRepository.findById(id);
    if (optionalEvent.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    Event existingEvent = optionalEvent.get();
    if (!existingEvent.getManager().equals(currentUser)) {
      return new ResponseEntity(HttpStatus.UNAUTHORIZED);
    }

    this.modelMapper.map(eventDto, existingEvent);
    Event savedEvent = this.eventRepository.save(existingEvent);
    EventEntityModel eventEntityModel = new EventEntityModel(savedEvent);
    eventEntityModel.add(new Link("/docs/index.html#resources-events-update").withRel("profile"));
    return ResponseEntity.ok(eventEntityModel);
  }

  private ResponseEntity<?> badRequest(Errors errors) {
    return ResponseEntity.badRequest().body(new ErrorsEntityModel(errors));
  }

}