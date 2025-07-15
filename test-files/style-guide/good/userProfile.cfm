<cfparam name="url.userId" type="numeric" default="0">
<cfparam name="url.showDetails" type="boolean" default="false">
<cfparam name="form.firstName" type="string" default="">
<cfparam name="form.lastName" type="string" default="">
<cfparam name="form.email" type="email" default="">

<cfif url.userId eq 0>
    <cfthrow type="ValidationError" message="User ID is required">
</cfif>

<cfset userService = new UserService("userDB")>

<cftry>
    <cfset userInfo = userService.getUserById(userId: url.userId)>
    
    <cfcatch type="UserNotFound">
        <cfoutput>
            <div class="error-message">
                <h2>User Not Found</h2>
                <p>The requested user could not be found.</p>
            </div>
        </cfoutput>
        <cfabort>
    </cfcatch>
</cftry>

<cfif structKeyExists(form, "submit") and len(trim(form.firstName))>
    <cfset updateResult = userService.updateUser(
        userId: url.userId,
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email
    )>
    
    <cfif updateResult>
        <cfset userInfo = userService.getUserById(userId: url.userId)>
        <cfset successMessage = "User profile updated successfully!">
    </cfif>
</cfif>

<!DOCTYPE html>
<html>
<head>
    <title>User Profile</title>
    <style>
        .user-profile { margin: 20px; }
        .form-group { margin-bottom: 15px; }
        .error-message { color: red; }
        .success-message { color: green; }
    </style>
</head>
<body>
    <div class="user-profile">
        <h1>User Profile</h1>
        
        <cfif structKeyExists(variables, "successMessage")>
            <div class="success-message">
                <cfoutput>#successMessage#</cfoutput>
            </div>
        </cfif>
        
        <cfoutput>
            <div class="user-info">
                <h2>#userInfo.firstName# #userInfo.lastName#</h2>
                <p><strong>Email:</strong> #userInfo.email#</p>
                <p><strong>User Type:</strong> #userInfo.userType#</p>
                
                <cfif url.showDetails>
                    <p><strong>Created:</strong> #dateFormat(userInfo.createdDate, "mm/dd/yyyy")#</p>
                    <p><strong>Last Updated:</strong> #dateFormat(userInfo.lastUpdated, "mm/dd/yyyy")#</p>
                </cfif>
            </div>
        </cfoutput>
        
        <h3>Update Profile</h3>
        <form method="post" action="userProfile.cfm">
            <input type="hidden" name="userId" value="<cfoutput>#url.userId#</cfoutput>">
            
            <div class="form-group">
                <label for="firstName">First Name:</label>
                <input type="text" 
                       id="firstName" 
                       name="firstName" 
                       value="<cfoutput>#userInfo.firstName#</cfoutput>"
                       required>
            </div>
            
            <div class="form-group">
                <label for="lastName">Last Name:</label>
                <input type="text" 
                       id="lastName" 
                       name="lastName" 
                       value="<cfoutput>#userInfo.lastName#</cfoutput>"
                       required>
            </div>
            
            <div class="form-group">
                <label for="email">Email:</label>
                <input type="email" 
                       id="email" 
                       name="email" 
                       value="<cfoutput>#userInfo.email#</cfoutput>"
                       required>
            </div>
            
            <div class="form-group">
                <input type="submit" name="submit" value="Update Profile">
            </div>
        </form>
        
        <cfif url.showDetails>
            <cfset recentActivity = userService.getRecentActivity(userId: url.userId)>
            
            <cfif recentActivity.recordCount gt 0>
                <h3>Recent Activity</h3>
                <table border="1">
                    <tr>
                        <th>Date</th>
                        <th>Action</th>
                        <th>Details</th>
                    </tr>
                    <cfoutput query="recentActivity">
                        <tr>
                            <td>#dateFormat(activityDate, "mm/dd/yyyy")#</td>
                            <td>#actionType#</td>
                            <td>#actionDetails#</td>
                        </tr>
                    </cfoutput>
                </table>
            </cfif>
        </cfif>
    </div>
</body>
</html>
