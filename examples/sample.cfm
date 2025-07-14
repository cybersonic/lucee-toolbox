<!--- Sample CFM template for testing parser functionality --->
<cfparam name="url.action" default="list">
<cfparam name="url.id" default="0">

<cfscript>
    // Initialize variables
    variables.title = "Sample Page";
    variables.users = [
        {id: 1, name: "John Doe", email: "john@example.com", active: true},
        {id: 2, name: "Jane Smith", email: "jane@example.com", active: false},
        {id: 3, name: "Bob Johnson", email: "bob@example.com", active: true}
    ];
    
    // Helper function
    function getActiveUsers(users) {
        return users.filter(function(user) {
            return user.active;
        });
    }
    
    // Process action
    switch (url.action) {
        case "view":
            variables.selectedUser = users.find(function(user) {
                return user.id == url.id;
            });
            break;
        case "active":
            variables.users = getActiveUsers(variables.users);
            break;
    }
</cfscript>

<!DOCTYPE html>
<html>
<head>
    <title><cfoutput>#variables.title#</cfoutput></title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
        .inactive { color: #999; }
    </style>
</head>
<body>
    <h1><cfoutput>#variables.title#</cfoutput></h1>
    
    <cfif url.action == "view" AND isDefined("variables.selectedUser")>
        <h2>User Details</h2>
        <cfoutput>
            <p><strong>ID:</strong> #variables.selectedUser.id#</p>
            <p><strong>Name:</strong> #variables.selectedUser.name#</p>
            <p><strong>Email:</strong> #variables.selectedUser.email#</p>
            <p><strong>Status:</strong> 
                <cfif variables.selectedUser.active>
                    Active
                <cfelse>
                    <span class="inactive">Inactive</span>
                </cfif>
            </p>
        </cfoutput>
        <p><a href="?action=list">Back to List</a></p>
        
    <cfelseif url.action == "view">
        <p>User not found.</p>
        <p><a href="?action=list">Back to List</a></p>
        
    <cfelse>
        <h2>Users</h2>
        <p>
            <a href="?action=list">All Users</a> | 
            <a href="?action=active">Active Only</a>
        </p>
        
        <cfif arrayLen(variables.users) GT 0>
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <cfloop array="#variables.users#" index="user">
                        <cfoutput>
                            <tr>
                                <td>#user.id#</td>
                                <td>#user.name#</td>
                                <td>#user.email#</td>
                                <td>
                                    <cfif user.active>
                                        Active
                                    <cfelse>
                                        <span class="inactive">Inactive</span>
                                    </cfif>
                                </td>
                                <td>
                                    <a href="?action=view&id=#user.id#">View</a>
                                </td>
                            </tr>
                        </cfoutput>
                    </cfloop>
                </tbody>
            </table>
        <cfelse>
            <p>No users found.</p>
        </cfif>
    </cfif>
    
    <cfif isDefined("url.debug")>
        <hr>
        <h3>Debug Information</h3>
        <cfdump var="#variables#" label="Variables">
        <cfdump var="#url#" label="URL Parameters">
    </cfif>
    
</body>
</html>
