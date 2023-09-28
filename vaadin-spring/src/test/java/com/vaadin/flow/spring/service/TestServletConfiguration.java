/*
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.spring.service;

import com.vaadin.flow.server.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ComponentScan
@SpringBootConfiguration
public class TestServletConfiguration {

    @Configuration(proxyBeanMethods = false)
    static class TestConfig {
        @Bean
        MyRequestInterceptor myFilter() {
            return new MyRequestInterceptor();
        }

        @Bean
        MyVaadinComandInterceptor myVaadinComandInterceptor() {
            return new MyVaadinComandInterceptor();
        }

        @Bean
        VaadinInterceptorsServiceInitListener vaadinRequestInterceptorServiceInitListener(
                ObjectProvider<VaadinRequestInterceptor> requestInterceptors,
                ObjectProvider<VaadinCommandInterceptor> commandInterceptors) {
            return new VaadinInterceptorsServiceInitListener(
                    requestInterceptors, commandInterceptors);
        }
    }

    static class MyRequestInterceptor implements VaadinRequestInterceptor {

        @Override
        public void requestStart(VaadinRequest request,
                VaadinResponse response) {
            request.setAttribute("started", "true");
        }

        @Override
        public void handleException(VaadinRequest request,
                VaadinResponse response, VaadinSession vaadinSession,
                Exception t) {
            request.setAttribute("error", "true");
        }

        @Override
        public void requestEnd(VaadinRequest request, VaadinResponse response,
                VaadinSession session) {
            request.setAttribute("stopped", "true");
        }
    }

    static class MyVaadinComandInterceptor implements VaadinCommandInterceptor {

        @Override
        public void commandExecutionStart(Map<Object, Object> context,
                Command command) {

        }

        @Override
        public void handleException(Map<Object, Object> context,
                Command command, Exception t) {

        }

        @Override
        public void commandExecutionEnd(Map<Object, Object> context,
                Command command) {

        }
    }
}