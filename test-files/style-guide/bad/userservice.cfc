component{

// Bad: constants not properly named, no static scope
MAX_USERS = 100;
admin_type = "admin";
regular_TYPE = "regular";

// Bad: no accessors, exposing internal variables
this.DS = "";
this.maxUsers = 100;

// Bad: no proper constructor, no return this
function init(DS, maxUsers){
variables.DS = DS;
variables.maxUsers = maxUsers;
}

// Bad: poor naming, no return type, no localMode, directly accessing external scopes
function getUser(ID){
qGetUser = queryExecute("SELECT * FROM users WHERE id = " & ID, {}, {datasource: application.datasource});
if(qGetUser.recordCount==0){
throw("User not found");
}
return qGetUser;
}

// Bad: poor formatting, no validation, SQL injection vulnerability
function createUser(fname, lname, email, type){
SQL = "INSERT INTO users (first_name, last_name, email, user_type) VALUES ('" & fname & "', '" & lname & "', '" & email & "', '" & type & "')";
result = queryExecute(SQL, {}, {datasource: application.datasource});
return result;
}

// Bad: inconsistent formatting, no curly braces, poor whitespace
function updateUser(userID,fname,lname,email)
{
if(fname!="")
qUpdate = queryExecute("UPDATE users SET first_name = '" & fname & "' WHERE id = " & userID, {}, {datasource: application.datasource});
if(lname!="")
qUpdate = queryExecute("UPDATE users SET last_name = '" & lname & "' WHERE id = " & userID, {}, {datasource: application.datasource});
return true;
}

// Bad: no named arguments, poor function naming
function validateUser(email, pwd){
query = queryExecute("SELECT password_hash FROM users WHERE email = '" & email & "'", {}, {datasource: application.datasource});
if(query.recordCount>0){
if(query.password_hash==hash(pwd, "SHA-256")){
return true;
}
}
return false;
}

// Bad: accessing session scope directly, no error handling
function loginUser(email, pwd){
if(validateUser(email, pwd)){
session.user = email;
session.loggedIn = true;
return true;
}
return false;
}

// Bad: no locking on shared resources, race condition
function incrementCounter(){
if(isNull(application.counter)){
application.counter = 1;
}else{
application.counter++;
}
}

// Bad: unnecessary pound signs, poor readability
function getFullName(firstName, lastName){
return "#firstName# #lastName#";
}

// Bad: no var scoping, will cause thread safety issues
function processUsers(){
for(i=1; i<=10; i++){
userData = getUserData(i);
processedData = processData(userData);
}
return processedData;
}

// Bad: using cfhttp without proper error handling
function checkExternalAPI(){
cfhttp(url="https://api.example.com/users", method="GET", throwOnError=true);
return cfhttp.fileContent;
}

// Bad: poor method chaining format
function configureService(){
return this.setMaxUsers(200).setDataSource("userDB").enableCaching(true).setLogLevel("INFO");
}

// Bad: exposing private functionality, poor encapsulation
function getInternalData(){
return variables;
}

}
