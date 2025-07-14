/**
 * Sample component for testing parser functionality
 * @author Developer
 * @version 1.0
 */
component displayName="Sample Component" accessors="true" {
    
    // Properties
    property name="id" type="numeric" default="0";
    property name="name" type="string" default="";
    property name="active" type="boolean" default="true";
    
    /**
     * Constructor
     * @param name The name of the component
     */
    public function init(string name = "Default") {
        variables.name = arguments.name;
        variables.id = randRange(1, 1000);
        return this;
    }
    
    /**
     * Get the component name
     * @return string
     */
    public string function getName() {
        return variables.name;
    }
    
    /**
     * Set the component name
     * @param name The new name
     */
    public void function setName(required string name) {
        if (len(trim(arguments.name)) == 0) {
            throw(type="InvalidArgument", message="Name cannot be empty");
        }
        variables.name = arguments.name;
    }
    
    /**
     * Toggle active status
     */
    public function toggleActive() {
        variables.active = !variables.active;
        return this;
    }
    
    /**
     * Get component info as struct
     * @return struct
     */
    public struct function getInfo() {
        return {
            id: variables.id,
            name: variables.name,
            active: variables.active,
            timestamp: now()
        };
    }
    
    // Private helper function
    private function validateName(required string name) {
        var isValid = len(trim(arguments.name)) > 0;
        
        // Check for invalid characters
        if (isValid) {
            var invalidChars = ["<", ">", "&", "\"", "'"];
            for (var char in invalidChars) {
                if (find(char, arguments.name)) {
                    isValid = false;
                    break;
                }
            }
        }
        
        return isValid;
    }
}
