component{

this.apiURL = "";
this.apiKey = "";

function init(url, key){
this.apiURL = url;
this.apiKey = key;
}

// Bad: poor error handling, no localMode, accessing external scopes
function getUser(userID){
cfhttp(url="#this.apiURL#/users/#userID#", method="GET", throwOnError=true) {
cfhttpparam(type="header", name="Authorization", value="Bearer #this.apiKey#");
}
return deserializeJSON(cfhttp.fileContent);
}

// Bad: SQL injection vulnerability, no validation
function createUser(firstName, lastName, email){
userData = {
"firstName": firstName,
"lastName": lastName,
"email": email
};

cfhttp(url="#this.apiURL#/users", method="POST", throwOnError=true) {
cfhttpparam(type="header", name="Authorization", value="Bearer #this.apiKey#");
cfhttpparam(type="header", name="Content-Type", value="application/json");
cfhttpparam(type="body", value="#serializeJSON(userData)#");
}

// Bad: storing in session without proper validation
session.lastCreatedUser = cfhttp.fileContent;
return cfhttp.fileContent;
}

// Bad: no error handling, race condition
function updateUserCounter(){
if(isNull(application.userCounter)){
application.userCounter = 1;
}else{
application.userCounter++;
}
}

// Bad: poor method chaining, no return types
function configure(){
return this.setURL("http://api.example.com").setKey("secret123").setTimeout(60);
}

// Bad: exposing internal data, no encapsulation
function getConfig(){
return {
"url": this.apiURL,
"key": this.apiKey,
"session": session,
"application": application
};
}

// Bad: unnecessary pound signs, poor readability
function buildURL(endpoint){
return "#this.apiURL##endpoint#";
}

// Bad: no var scoping, will cause thread safety issues
function processMultipleUsers(userList){
for(i=1; i<=arrayLen(userList); i++){
userData = getUser(userList[i]);
processedData = processUserData(userData);
}
return processedData;
}

// Bad: using evaluate function, security risk
function executeFunction(functionName, args){
return evaluate("#functionName#(#args#)");
}

// Bad: no curly braces, poor formatting
function validateResponse(response)
if(response.status_code == 200)
return true;
else
return false;
}

// Bad: direct session access without checking
function getUserFromSession(){
return session.userData;
}

// Bad: using iif function instead of proper conditional
function getDefaultValue(value){
return iif(len(value), DE(value), DE("default"));
}

}
