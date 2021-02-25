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
package com.alilitech.web.file;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.InputStream;


/**
 * 文件查看，用法返回ResponseEntity<AbstractStreamingResponseBody>
 * new FileViewStreamingResponseBody(new File("")).mediaType(MediaType...).toResponseEntity();
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class FileViewStreamingResponseBody extends AbstractStreamingResponseBody<FileViewStreamingResponseBody> {

    public FileViewStreamingResponseBody(InputStream inputStream) {
        super(inputStream);
    }

    public FileViewStreamingResponseBody(InputStream inputStream, MediaType mediaType) {
        super(inputStream, mediaType);
    }

    public FileViewStreamingResponseBody(File file) {
        super(file);
    }

    public FileViewStreamingResponseBody(File file, MediaType mediaType) {
        super(file, mediaType);
    }

    @Override
    public ResponseEntity<FileViewStreamingResponseBody> toResponseEntity() {
        return ResponseEntity.ok().contentType(mediaType).body(this);
    }


}
