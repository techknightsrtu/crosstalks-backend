package com.develup.crosstalks.crosstalksapis.controllers;

import static com.develup.crosstalks.crosstalksapis.constants.PathConstants.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AUTH_CONTROLLER_PATH)
public class AuthController {

  @GetMapping(LOGIN)
  public String getAuth() {
    return "Hello Cross Talks!";
  }

}
