/*
 *    Copyright 2017-2021 the original author or authors.
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@ControllerAdvice
public class ValidAdvice {

    private final ValidHandler validHandler;

    public ValidAdvice(@Nullable ValidHandler validHandler) {
        this.validHandler = validHandler;
    }

    @ExceptionHandler({ MethodArgumentNotValidException.class })
    @ResponseBody
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ResponseEntity<Object> responseEntity = customValidHandler(e);
        if(responseEntity != null) {
            return responseEntity;
        }
        return ResponseEntity.badRequest()
                .body(new CommonBody(e.getBindingResult().getAllErrors()));
    }

    @ExceptionHandler({ BindException.class})
    @ResponseBody
    public ResponseEntity<Object> handleBindException(BindException e) {
        ResponseEntity<Object> responseEntity = customValidHandler(e);
        if(responseEntity != null) {
            return responseEntity;
        }
        return ResponseEntity.badRequest()
                .body(new CommonBody(e.getBindingResult().getAllErrors()));
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
        return ResponseEntity.badRequest()
                .body(new CommonBody(e.getMessage()));
    }

    private ResponseEntity<Object> customValidHandler(Exception e) {
        if(validHandler == null) {
            return null;
        }
        return validHandler.handle(e);
    }

}
