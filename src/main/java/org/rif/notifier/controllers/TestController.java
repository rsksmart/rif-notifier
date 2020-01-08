package org.rif.notifier.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.rif.notifier.constants.ControllerConstants;
import org.rif.notifier.models.entities.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import org.rif.notifier.repositories.*;

@Api(tags = {"Test resource"})
@RestController
public class TestController {

    @Autowired
    private TestRepository testRepository;

    @ApiOperation(value = "Retrieve test entities",
            response = Test.class, responseContainer = ControllerConstants.LIST_RESPONSE_CONTAINER)
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = {ControllerConstants.CONTENT_TYPE_APPLICATION_JSON})
    public  ResponseEntity<List<Test>> test() {
        List<Test> tests = testRepository.findAll();
        System.out.println(tests.size());
        return new ResponseEntity<>(tests, HttpStatus.OK);
    }}
