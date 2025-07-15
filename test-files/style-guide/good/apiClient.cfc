component {

    property name="apiBaseUrl" type="string";
    property name="apiKey" type="string";
    property name="timeout" type="numeric" default="30";

    function init(required string apiBaseUrl, required string apiKey, numeric timeout = 30) {
        variables.apiBaseUrl = arguments.apiBaseUrl;
        variables.apiKey = arguments.apiKey;
        variables.timeout = arguments.timeout;
        return this;
    }

    /**
     * Makes a GET request to the API with proper error handling
     */
    struct function makeGetRequest(required string endpoint, struct params = {}) localMode="true" {
        local.url = variables.apiBaseUrl & arguments.endpoint;
        
        // Build query string from params
        if (structCount(arguments.params) > 0) {
            local.queryString = "";
            for (local.key in arguments.params) {
                local.queryString &= "&" & local.key & "=" & urlEncodedFormat(arguments.params[local.key]);
            }
            local.url &= "?" & right(local.queryString, len(local.queryString) - 1);
        }

        cfhttp(
            url: local.url,
            method: "GET",
            timeout: variables.timeout,
            result: "local.httpResult"
        ) {
            cfhttpparam(
                type: "header",
                name: "Authorization",
                value: "Bearer " & variables.apiKey
            );
            cfhttpparam(
                type: "header",
                name: "Content-Type",
                value: "application/json"
            );
        }

        return processHttpResponse(local.httpResult);
    }

    /**
     * Makes a POST request to the API with proper error handling
     */
    struct function makePostRequest(
        required string endpoint,
        required struct data
    ) localMode="true" {
        local.url = variables.apiBaseUrl & arguments.endpoint;
        local.jsonData = serializeJSON(arguments.data);

        cfhttp(
            url: local.url,
            method: "POST",
            timeout: variables.timeout,
            result: "local.httpResult"
        ) {
            cfhttpparam(
                type: "header",
                name: "Authorization",
                value: "Bearer " & variables.apiKey
            );
            cfhttpparam(
                type: "header",
                name: "Content-Type",
                value: "application/json"
            );
            cfhttpparam(
                type: "body",
                value: local.jsonData
            );
        }

        return processHttpResponse(local.httpResult);
    }

    /**
     * Processes HTTP response with proper error handling
     */
    private struct function processHttpResponse(required struct httpResult) localMode="true" {
        local.response = {
            success: false,
            statusCode: arguments.httpResult.status_code,
            data: {},
            error: ""
        };

        // Check if request was successful (2xx status codes)
        if (left(arguments.httpResult.status_code, 1) == "2") {
            local.response.success = true;
            
            try {
                if (len(trim(arguments.httpResult.fileContent))) {
                    local.response.data = deserializeJSON(arguments.httpResult.fileContent);
                }
            } catch (any e) {
                local.response.success = false;
                local.response.error = "Invalid JSON response: " & e.message;
            }
        } else {
            local.response.error = "HTTP Error " & arguments.httpResult.status_code;
            
            // Try to get error message from response body
            try {
                if (len(trim(arguments.httpResult.fileContent))) {
                    local.errorData = deserializeJSON(arguments.httpResult.fileContent);
                    if (structKeyExists(local.errorData, "message")) {
                        local.response.error &= ": " & local.errorData.message;
                    }
                }
            } catch (any e) {
                // Ignore JSON parsing errors for error responses
            }
        }

        return local.response;
    }

    /**
     * Gets user data from external API
     */
    struct function getUser(required numeric userId) localMode="true" {
        local.endpoint = "/users/" & arguments.userId;
        local.response = makeGetRequest(endpoint: local.endpoint);
        
        if (!local.response.success) {
            throw(
                type: "APIError",
                message: "Failed to retrieve user: " & local.response.error
            );
        }

        return local.response.data;
    }

    /**
     * Creates a new user via external API
     */
    struct function createUser(
        required string firstName,
        required string lastName,
        required string email
    ) localMode="true" {
        local.userData = {
            firstName: arguments.firstName,
            lastName: arguments.lastName,
            email: arguments.email
        };

        local.response = makePostRequest(
            endpoint: "/users",
            data: local.userData
        );

        if (!local.response.success) {
            throw(
                type: "APIError",
                message: "Failed to create user: " & local.response.error
            );
        }

        return local.response.data;
    }

    /**
     * Health check endpoint with proper timeout handling
     */
    boolean function isApiHealthy() localMode="true" {
        local.response = makeGetRequest(endpoint: "/health");
        return local.response.success;
    }

}
