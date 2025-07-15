component accessors="true" {

    // Static constants - properly named in UPPER_CASE
    static {
        DEFAULT_MAX_USERS = 100;
        USER_TYPE_ADMIN = "admin";
        USER_TYPE_REGULAR = "regular";
    }

    // Properties with proper accessors
    property name="dataSource" type="string";
    property name="maxUsers" type="numeric" default="100";

    /**
     * Constructor that initializes the component
     */
    function init(required string dataSource, numeric maxUsers = 100) {
        variables.dataSource = arguments.dataSource;
        variables.maxUsers = arguments.maxUsers;
        return this;
    }

    /**
     * Retrieves a user by ID with proper error handling
     */
    User function getUserById(required numeric userId) localMode="true" {
        local.sql = "SELECT * FROM users WHERE id = ?";
        
        local.result = queryExecute(
            sql: local.sql,
            params: [arguments.userId],
            options: {
                datasource: variables.dataSource,
                returntype: "query"
            }
        );

        if (local.result.recordCount == 0) {
            throw(type="UserNotFound", message="User with ID #arguments.userId# not found");
        }

        return new User(local.result);
    }

    /**
     * Creates a new user with proper validation
     */
    boolean function createUser(
        required string firstName,
        required string lastName,
        required string email,
        string userType = "regular"
    ) localMode="true" {
        
        // Validate input parameters
        if (!len(trim(arguments.firstName))) {
            throw(type="ValidationError", message="First name is required");
        }
        
        if (!isValid("email", arguments.email)) {
            throw(type="ValidationError", message="Invalid email address");
        }

        local.sql = "
            INSERT INTO users (first_name, last_name, email, user_type, created_date)
            VALUES (?, ?, ?, ?, ?)
        ";

        local.result = queryExecute(
            sql: local.sql,
            params: [
                arguments.firstName,
                arguments.lastName,
                arguments.email,
                arguments.userType,
                now()
            ],
            options: {
                datasource: variables.dataSource
            }
        );

        return true;
    }

    /**
     * Updates user information with proper parameter validation
     */
    boolean function updateUser(
        required numeric userId,
        string firstName,
        string lastName,
        string email
    ) localMode="true" {
        
        local.updates = [];
        local.params = [];

        if (structKeyExists(arguments, "firstName") && len(trim(arguments.firstName))) {
            arrayAppend(local.updates, "first_name = ?");
            arrayAppend(local.params, arguments.firstName);
        }

        if (structKeyExists(arguments, "lastName") && len(trim(arguments.lastName))) {
            arrayAppend(local.updates, "last_name = ?");
            arrayAppend(local.params, arguments.lastName);
        }

        if (structKeyExists(arguments, "email") && isValid("email", arguments.email)) {
            arrayAppend(local.updates, "email = ?");
            arrayAppend(local.params, arguments.email);
        }

        if (arrayLen(local.updates) == 0) {
            throw(type="ValidationError", message="No valid fields to update");
        }

        // Add userId to params for WHERE clause
        arrayAppend(local.params, arguments.userId);

        local.sql = "UPDATE users SET " & arrayToList(local.updates, ", ") & " WHERE id = ?";

        local.result = queryExecute(
            sql: local.sql,
            params: local.params,
            options: {
                datasource: variables.dataSource
            }
        );

        return true;
    }

    /**
     * Validates user credentials using named arguments
     */
    boolean function validateCredentials(required string email, required string password) localMode="true" {
        local.sql = "SELECT password_hash FROM users WHERE email = ? AND active = 1";
        
        local.result = queryExecute(
            sql: local.sql,
            params: [arguments.email],
            options: {
                datasource: variables.dataSource,
                returntype: "query"
            }
        );

        if (local.result.recordCount == 0) {
            return false;
        }

        return hash(arguments.password, "SHA-256") == local.result.password_hash;
    }

    /**
     * Method chaining example with proper formatting
     */
    UserService function configure() localMode="true" {
        return this.setMaxUsers(200)
                  .setDataSource("userDB")
                  .enableCaching(true)
                  .setLogLevel("INFO");
    }

    // Private helper method (in variables scope)
    private boolean function isUserActive(required numeric userId) localMode="true" {
        local.sql = "SELECT active FROM users WHERE id = ?";
        
        local.result = queryExecute(
            sql: local.sql,
            params: [arguments.userId],
            options: {
                datasource: variables.dataSource,
                returntype: "query"
            }
        );

        return local.result.recordCount > 0 && local.result.active;
    }

}
