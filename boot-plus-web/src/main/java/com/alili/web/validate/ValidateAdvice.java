/**
 *    Copyright 2017-2019 the original author or authors.
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
package com.alili.web.validate;

import com.alili.core.util.UnicodeUtils;
import com.alili.web.config.WebConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@ControllerAdvice
public class ValidateAdvice {

    private ValidateHandler validateHandler;

    public ValidateAdvice(@Nullable ValidateHandler validateHandler) {
        this.validateHandler = validateHandler;
    }

    @ExceptionHandler({ MethodArgumentNotValidException.class })
    @ResponseBody
    public ResponseEntity handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        if(validateHandler != null) {
            return validateHandler.handle(e);
        }
        ObjectError objectError = e.getBindingResult().getAllErrors().get(0);
        return ResponseEntity.badRequest()
                .header(WebConfig.TIP_KEY, UnicodeUtils.stringToUnicode(objectError.getDefaultMessage()))
                .build();
    }

    @ExceptionHandler({ ValidateException.class })
    @ResponseBody
    public ResponseEntity handleValidateException(ValidateException e) {
        if(validateHandler != null) {
            return validateHandler.handle(e);
        }
        return ResponseEntity.badRequest().header(WebConfig.TIP_KEY, UnicodeUtils.stringToUnicode(e.getMessage())).build();
        //return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

}
