package com.ibm.paymentsSample;
/**
* Copyright 2016 IBM Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

public class Constants {
    static final String ACTION_LOGIN = "sample.ibm.com.sampleapp.broadcast.login";
    static final String ACTION_LOGIN_SUCCESS = "sample.ibm.com.sampleapp.broadcast.login.success";
    static final String ACTION_LOGIN_FAILURE = "sample.ibm.com.sampleapp.broadcast.login.failure";
    static final String ACTION_LOGIN_REQUIRED = "sample.ibm.com.sampleapp.broadcast.login.required";

    static final String ACTION_LOGOUT = "sample.ibm.com.sampleapp.broadcast.logout";
    static final String ACTION_LOGOUT_SUCCESS = "sample.ibm.com.sampleapp.broadcast.logout.success";
    static final String ACTION_LOGOUT_FAILURE = "sample.ibm.com.sampleapp.broadcast.logout.failure";

    static final String PREFERENCES_FILE = "sample.ibm.com.sampleapp.preferences";
    static final String PREFERENCES_KEY_USER = "sample.ibm.com.sampleapp.preferences.user";
}
