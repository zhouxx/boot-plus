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

import com.alilitech.util.UnicodeUtils;
import com.alilitech.web.WebConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
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

    private ValidHandler validHandler;

    public ValidAdvice(@Nullable ValidHandler validHandler) {
        this.validHandler = validHandler;
    }

    @ExceptionHandler({ MethodArgumentNotValidException.class })
    @ResponseBody
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        if(validHandler != null) {
            ResponseEntity<Object> responseEntity = validHandler.handle(e);
            if(responseEntity != null) {
                return responseEntity;
            }
        }
        ObjectError objectError = e.getBindingResult().getAllErrors().get(0);
        return ResponseEntity.badRequest()
                .header(WebConfiguration.TIP_KEY, UnicodeUtils.stringToUnicode(objectError.getDefaultMessage()))
                .build();
    }

    @ExceptionHandler({ BindException.class})
    @ResponseBody
    public ResponseEntity<Object> handleBindException(BindException e) {
        if(validHandler != null) {
            ResponseEntity<Object> responseEntity = validHandler.handle(e);
            if(responseEntity != null) {
                return responseEntity;
            }
        }
        ObjectError objectError = e.getBindingResult().getAllErrors().get(0);
        return ResponseEntity.badRequest()
                .header(WebConfiguration.TIP_KEY, UnicodeUtils.stringToUnicode(objectError.getDefaultMessage()))
                .build();
    }

    @ExceptionHandler({ MissingServletRequestParameterException.class,  MissingPathVariableException.class, HttpMessageNotReadableException.class})
    @ResponseBody
    public ResponseEntity<Object> handleServletRequestBindingException(Exception e) {
        if(validHandler != null) {
            ResponseEntity<Object> responseEntity = validHandler.handle(e);
            if(responseEntity != null) {
                return responseEntity;
            }
        }
        return ResponseEntity.badRequest()
                .header(WebConfiguration.TIP_KEY, UnicodeUtils.stringToUnicode(e.getMessage()))
                .build();
    }

    @ExceptionHandler({ NoHandlerFoundException.class})
    @ResponseBody
    public ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException e) {
        if(validHandler != null) {
            ResponseEntity<Object> responseEntity = validHandler.handle(e);
            if(responseEntity != null) {
                return responseEntity;
            }
        }
        return ResponseEntity.notFound()
                .header(WebConfiguration.TIP_KEY, UnicodeUtils.stringToUnicode(e.getMessage()))
                .build();
    }

    @ExceptionHandler({ NoDataFoundException.class})
    @ResponseBody
    public ResponseEntity<Object> handleNoResultException(NoDataFoundException e) {
        if(validHandler != null) {
            ResponseEntity<Object> responseEntity = validHandler.handle(e);
            if(responseEntity != null) {
                return responseEntity;
            }
        }
        return ResponseEntity.notFound()
                .header(WebConfiguration.TIP_KEY, UnicodeUtils.stringToUnicode(e.getMessage()))
                .build();
    }

    @ExceptionHandler({ ValidException.class })
    @ResponseBody
    public ResponseEntity<Object> handleValidateException(ValidException e) {
        if(validHandler != null) {
            ResponseEntity<Object> responseEntity = validHandler.handle(e);
            if(responseEntity != null) {
                return responseEntity;
            }
        }
        return ResponseEntity.badRequest().header(WebConfiguration.TIP_KEY, UnicodeUtils.stringToUnicode(e.getMessage())).build();
    }

}
