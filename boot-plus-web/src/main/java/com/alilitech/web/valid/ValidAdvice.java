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
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.MessageInterpolator;
import javax.validation.ValidatorFactory;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@ControllerAdvice
@ConditionalOnMissingBean(ResponseEntityExceptionHandler.class)
public class ValidAdvice extends ResponseEntityExceptionHandler {

    protected final ValidatorFactory validatorFactory;

    public ValidAdvice(ValidatorFactory validatorFactory) {
        this.validatorFactory = validatorFactory;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return ResponseEntity.badRequest()
                .body(new CommonBody(e.getBindingResult().getAllErrors().stream()
                        .map(objectError -> new ValidMessage(((FieldError)objectError).getField(), objectError.getDefaultMessage()))
                        .collect(Collectors.toList())));
    }

    @Override
    protected ResponseEntity<Object> handleBindException(BindException e, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return ResponseEntity.badRequest()
                .body(new CommonBody(e.getBindingResult().getAllErrors().stream()
                        .map(objectError -> new ValidMessage(((FieldError)objectError).getField(), objectError.getDefaultMessage()))
                        .collect(Collectors.toList())));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException e, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return ResponseEntity.badRequest().body(new CommonBody(e.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException e, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return ResponseEntity.badRequest().body(new CommonBody(e.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return ResponseEntity.badRequest().body(new CommonBody(e.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException e, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new CommonBody(HttpStatus.NOT_FOUND.value(), e.getMessage()));
    }

    @ExceptionHandler({ ValidException.class })
    @ResponseBody
    public ResponseEntity<Object> handleValidException(ValidException e, WebRequest request) {
        return handleNotValid(e, request);
    }

    protected ResponseEntity<Object> handleNotValid(ValidException e, WebRequest request) {
        MessageInterpolator messageInterpolator = validatorFactory.getMessageInterpolator();
        MessageInterpolatorContext context = new MessageInterpolatorContext(null,
                e.getValidatedValue(),
                Object.class,
                StringUtils.hasLength(e.getPropertyPath()) ? PathImpl.createPathFromString(e.getPropertyPath()) : null,
                e.getPlaceholderMap(),
                Collections.emptyMap(),
                ExpressionLanguageFeatureLevel.DEFAULT,
                false);
        String message = messageInterpolator.interpolate(e.getMessage(), context);
        CommonBody body = StringUtils.hasLength(e.getPropertyPath()) ? new CommonBody(e.getHttpStatus().value(), Collections.singletonList(new ValidMessage(e.getPropertyPath(), message))) : new CommonBody(e.getHttpStatus().value(), message);
        return ResponseEntity.status(e.getHttpStatus()).body(body);
    }

}
