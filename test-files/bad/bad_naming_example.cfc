component name="bad_naming_example" {
    
    // Constants that should be UPPER_CASE
    SERVER_CONFIG = "production";
    maxRetries = 10;
    API_BASE_URL = "https://api.example.com";
    
    function init_component() {
        var TEMP_VALUE = "test";
        variables.user_id = 123;
        local.processing_flag = true;
        
        return this;
    }
    
    function GetUserData(required string USER_ID, string full_name, boolean is_admin) {
        var user_result = "";
        var query_params = {};
        
        return user_result;
    }
    
    function process_data(required any INPUT_DATA) {
        var temp_var = "";
        local.RESULT_SET = [];
        
        return temp_var;
    }
    
    function CreateHTMLReport() {
        var html_content = "";
        return html_content;
    }
}
