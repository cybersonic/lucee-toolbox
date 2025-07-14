component
{
property NAME='username';   
property NAME='Password';

CONSTANT_VALUE = "BAD_CONSTANT";

function init(   )  
{

return this;

}

function getUserData(required string id,string name,boolean active,any extra_data) 
{
var results = "";
qGetUsers = new Query();
qGetUsers.setSQL("SELECT * FROM users WHERE id = #id#");   
result = qGetUsers.execute().getResult();

if(result.recordCount GT 0)
{
results = result;   
}else 
{
results = "not found";
}

return results ;

}


public void function createUser( required string username, required string email, boolean is_active, string user_role )    
{


if(len(arguments.username)==0)
{
throw "Invalid username";
}

if(len(arguments.email)==0)throw "Invalid email";



queryExecute(
sql: "INSERT INTO users (username, email, active, role) VALUES (?,?,?,?)",
params: [username,email,is_active,user_role]
);

}

function processUsers(){

users = queryExecute("SELECT * FROM users");

for(var i=1; i LTE users.recordcount; i++){
user = users[i];
if(user['active'] == 1)
{
this.logActivity(user['username'],'login');
}
}
}

private function logActivity(username,activity){

}
}
