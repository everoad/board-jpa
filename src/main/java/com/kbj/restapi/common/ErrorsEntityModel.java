package com.kbj.restapi.common;

import com.kbj.restapi.index.IndexController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.validation.Errors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class ErrorsEntityModel extends EntityModel<Errors> {

    public ErrorsEntityModel(Errors errors, Link... links) {
        super(errors, links);
        add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
    }

}
