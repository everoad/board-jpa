package com.kbj.restapi.events;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

public class EventEntityModel extends EntityModel<Event> {

  public EventEntityModel(Event event, Link... links) {
    super(event, links);
    add(linkTo(EventController.class).slash(event.getId()).withSelfRel());
  }
}