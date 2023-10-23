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

import org.springframework.http.*;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/**
 * 文件下载，用法返回ResponseEntity<AbstractStreamingResponseBody>
 * new FileDownloadStreamingResponseBody(new File("")).fileName(fileName).mediaType(MediaType...).toResponseEntity()
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class FileDownloadStreamingResponseBody extends AbstractStreamingResponseBody<FileDownloadStreamingResponseBody> {

    protected String fileName;

    protected Charset charset = StandardCharsets.UTF_8;

    public FileDownloadStreamingResponseBody(InputStream inputStream) {
        super(inputStream);
    }

    public FileDownloadStreamingResponseBody(InputStream inputStream, MediaType mediaType) {
        super(inputStream, mediaType);
    }

    public FileDownloadStreamingResponseBody(File file) {
        super(file);
    }

    public FileDownloadStreamingResponseBody(File file, MediaType mediaType) {
        super(file, mediaType);
    }

    public FileDownloadStreamingResponseBody fileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public FileDownloadStreamingResponseBody charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    @Override
    public ResponseEntity<FileDownloadStreamingResponseBody> toResponseEntity(HttpStatus httpStatus, HttpHeaders headers) {
        ContentDisposition contentDisposition = ContentDisposition.builder("attachment").filename(this.fileName, charset).build();
        return ResponseEntity.status(httpStatus)
                .headers(headers)
                .contentType(mediaType)
                .header("Content-Disposition", contentDisposition.toString())
                .body(this);
    }
}
