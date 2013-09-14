/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androidhive.openhourgoogle.api;

import android.content.Context;
import android.content.res.Resources;
import com.androidhive.openhourgoogle.R;

/**
 * Author: JessicaC
 * Date: 9/12/2013
 * Map google service error codes to error messages.
 */
public class GoogleServiceErrorMessages {

    // Don't allow instantiation
    private GoogleServiceErrorMessages() {}

    public static String getErrorString(Context context, String status) {

        // Get a handle to resources, to allow the method to retrieve messages.
        Resources mResources = context.getResources();

        // Define a string to contain the error message
        String errorString;

        // Decide which error message to get, based on the status.
        if(status.equals("ZERO_RESULTS")) {
            errorString = mResources.getString(R.string.status_zero_results);
        }
        else if(status.equals("UNKNOWN_ERROR")) {
            errorString = mResources.getString(R.string.status_unknown_error);
        }
        else if(status.equals("OVER_QUERY_LIMIT")) {
            errorString = mResources.getString(R.string.status_over_query_limit);
        }
        else if(status.equals("REQUEST_DENIED")) {
            errorString = mResources.getString(R.string.status_request_denied);
        }
        else if(status.equals("INVALID_REQUEST")) {
            errorString = mResources.getString(R.string.status_invalid_request);
        }
        else {
            errorString = mResources.getString(R.string.status_unknown_error);
        }
        // Return the error message
        return errorString;
    }
}
