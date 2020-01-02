/**
 *    Copyright 2017-2020 the original author or authors.
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/**
 * 文件下载，用法返回ResponseEntity<AbstractStreamingResponseBody>
 * new FileDownloadStreamingResponseBody(new File("")).fileName(fileName).mediaType(MediaType...).toResponseEntity()
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class FileDownloadStreamingResponseBody extends AbstractStreamingResponseBody {

    protected String fileName;

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

    public AbstractStreamingResponseBody fileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    @Override
    public ResponseEntity<FileDownloadStreamingResponseBody> toResponseEntity() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String userAgent = request.getHeader("User-Agent");

        String attachFileName = fileName;

        try {
            // 针对IE或者以IE为内核的浏览器：
            if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
                attachFileName = java.net.URLEncoder.encode(attachFileName, "UTF-8");
            } else { // 非IE浏览器的处理：
                attachFileName = new String(attachFileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            }
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage());
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header("Content-Disposition", "attachment;filename=" + attachFileName)
                .body(this);
    }
}
