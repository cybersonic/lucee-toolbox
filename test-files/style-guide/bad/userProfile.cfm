<CFIF structKeyExists(URL, "userID")>
    <CFSET userID = URL.userID>
<CFELSE>
    <CFSET userID = 0>
</CFIF>

<CFQUERY name="qUser" datasource="#application.datasource#">
    SELECT * FROM users WHERE id = #userID#
</CFQUERY>

<cfif qUser.recordCount eq 0>
    User not found!
    <cfabort>
</cfif>

<cfif structKeyExists(form, "submit")>
    <cfquery name="updateUser" datasource="#application.datasource#">
        UPDATE users 
        SET first_name = '#form.firstName#',
            last_name = '#form.lastName#',
            email = '#form.email#'
        WHERE id = #userID#
    </cfquery>
    
    <cfquery name="qUser" datasource="#application.datasource#">
        SELECT * FROM users WHERE id = #userID#
    </cfquery>
</cfif>

<html>
<head>
    <title>User Profile</title>
</head>
<body>
    <H1>User Profile</H1>
    
    <cfoutput>
        <h2>#qUser.first_name# #qUser.last_name#</h2>
        <p>Email: #qUser.email#</p>
        <p>Type: #qUser.user_type#</p>
    </cfoutput>
    
    <form method="post">
        <input type="hidden" name="userID" value="#userID#">
        
        First Name: <input type="text" name="firstName" value="#qUser.first_name#"><br>
        Last Name: <input type="text" name="lastName" value="#qUser.last_name#"><br>
        Email: <input type="text" name="email" value="#qUser.email#"><br>
        
        <input type="submit" name="submit" value="Update">
    </form>
    
    <cfif structKeyExists(URL, "showDetails")>
        <cfquery name="activity" datasource="#application.datasource#">
            SELECT * FROM user_activity WHERE user_id = #userID#
        </cfquery>
        
        <cfif activity.recordCount gt 0>
            <h3>Recent Activity</h3>
            <table>
                <cfoutput query="activity">
                    <tr>
                        <td>#activity_date#</td>
                        <td>#action_type#</td>
                        <td>#action_details#</td>
                    </tr>
                </cfoutput>
            </table>
        </cfif>
    </cfif>
    
    <cfif session.user_type eq "admin">
        <cfquery name="allUsers" datasource="#application.datasource#">
            SELECT * FROM users
        </cfquery>
        
        <h3>All Users (Admin View)</h3>
        <cfoutput query="allUsers">
            <p>#first_name# #last_name# - #email#</p>
        </cfoutput>
    </cfif>
</body>
</html>
