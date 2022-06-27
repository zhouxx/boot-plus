/*
 *    Copyright 2017-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.alilitech.web.valid;

import com.alilitech.web.CommonBody;
import org.hibernate.validator.internal.engine.MessageInterpolatorContext;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.MessageInterpolator;
import javax.validation.ValidatorFactory;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@ControllerAdvice
public class ValidAdvice {

    private final ValidHandler validHandler;

    private final ValidatorFactory validatorFactory;

    public ValidAdvice(@Nullable ValidHandler validHandler, ValidatorFactory validatorFactory) {
        this.validHandler = validHandler;
        this.validatorFactory = validatorFactory;
    }

    @ExceptionHandler({ MethodArgumentNotValidException.class })
    @ResponseBody
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ResponseEntity<Object> responseEntity = customValidHandler(e);
        if(responseEntity != null) {
            return responseEntity;
        }
        return ResponseEntity.badRequest()
                .body(new CommonBody(e.getBindingResult().getAllErrors().stream()
                        .map(objectError -> new ValidMessage(((FieldError)objectError).getField(), objectError.getDefaultMessage()))
                        .collect(Collectors.toList())));
    }

    @ExceptionHandler({ BindException.class })
    @ResponseBody
    public ResponseEntity<Object> handleBindException(BindException e) {
        ResponseEntity<Object> responseEntity = customValidHandler(e);
        if(responseEntity != null) {
            return responseEntity;
        }
        return ResponseEntity.badRequest()
                .body(new CommonBody(e.getBindingResult().getAllErrors().stream()
                        .map(objectError -> new ValidMessage(((FieldError)objectError).getField(), objectError.getDefaultMessage()))
                        .collect(Collectors.toList())));
    }

    @ExceptionHandler({ MissingServletRequestParameterException.class,  MissingPathVariableException.class, HttpMessageNotReadableException.class})
    @ResponseBody
    public ResponseEntity<Object> handleServletRequestBindingException(Exception e) {
        ResponseEntity<Object> responseEntity = customValidHandler(e);
        if(responseEntity != null) {
            return responseEntity;
        }
        return ResponseEntity.badRequest()
                .body(new CommonBody(e.getMessage()));
    }

    @ExceptionHandler({ NoHandlerFoundException.class})
    @ResponseBody
    public ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException e) {
        ResponseEntity<Object> responseEntity = customValidHandler(e);
        if(responseEntity != null) {
            return responseEntity;
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new CommonBody(HttpStatus.NOT_FOUND.value(), e.getMessage()));
    }

    @ExceptionHandler({ NoDataFoundException.class})
    @ResponseBody
    public ResponseEntity<Object> handleNoResultException(NoDataFoundException e) {
        ResponseEntity<Object> responseEntity = customValidHandler(e);
        if(responseEntity != null) {
            return responseEntity;
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new CommonBody(HttpStatus.NOT_FOUND.value(), e.getMessage()));
    }

    @ExceptionHandler({ ValidException.class })
    @ResponseBody
    public ResponseEntity<Object> handleValidateException(ValidException e) {
        ResponseEntity<Object> responseEntity = customValidHandler(e);
        if(responseEntity != null) {
            return responseEntity;
        }
        MessageInterpolator messageInterpolator = validatorFactory.getMessageInterpolator();
        MessageInterpolatorContext context = new MessageInterpolatorContext(null,
                e.getValidatedValue(),
                Object.class,
                StringUtils.isEmpty(e.getPropertyPath()) ? null : PathImpl.createPathFromString(e.getPropertyPath()),
                e.getPlaceholderMap(),
                Collections.emptyMap());
        String message = messageInterpolator.interpolate(e.getMessage(), context);
        CommonBody body = StringUtils.isEmpty(e.getPropertyPath()) ? new CommonBody(message) : new CommonBody(Collections.singletonList(new ValidMessage(e.getPropertyPath(), message)));
        return ResponseEntity.badRequest()
                .body(body);
    }

    private ResponseEntity<Object> customValidHandler(Exception e) {
        if(validHandler == null) {
            return null;
        }
        return validHandler.handle(e);
    }

}
