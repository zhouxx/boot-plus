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
package com.alilitech.web.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;


/**
 * 文件查看或文件下载基类，定义了流和返回类型
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public abstract class AbstractStreamingResponseBody<T extends AbstractStreamingResponseBody> implements StreamingResponseBody {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

    protected InputStream inputStream;

    protected AbstractStreamingResponseBody(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    protected AbstractStreamingResponseBody(InputStream inputStream, MediaType mediaType) {
        this.inputStream = inputStream;
        this.mediaType = mediaType;
    }

    protected AbstractStreamingResponseBody(File file) {
        try {
            this.inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }
    }

    protected AbstractStreamingResponseBody(File file, MediaType mediaType) {
        this(file);
        this.mediaType = mediaType;
    }

    public AbstractStreamingResponseBody<T> mediaType(MediaType mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        int ch;
        while ((ch = inputStream.read()) != -1) {
            outputStream.write(ch);
        }
    }

    /**
     * covert to ResponseEntity
     */
    public ResponseEntity<T> toResponseEntity() {
        return toResponseEntity(HttpStatus.OK, new HttpHeaders());
    }

    public abstract ResponseEntity<T> toResponseEntity(HttpStatus httpStatus, HttpHeaders headers);

}
