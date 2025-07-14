component {
    
    property name="test";
    
    function testNoCurlyBraces(string id) {
        if (id == "test") return "found";
        
        for (var i = 1; i <= 10; i++) writeOutput(i);
        
        while (someCondition) doSomething();
        
        return "";
    }
    
    function veryLongFunctionThatExceedsTheMaximumLengthLimitAndShouldBeRefactoredIntoSmallerMethods() {
        var result = "";
        var data = [];
        var processed = {};
        var errors = [];
        var warnings = [];
        var info = {};
        var config = {};
        var settings = {};
        var options = {};
        var parameters = {};
        var values = {};
        var items = [];
        var elements = [];
        var components = {};
        var services = {};
        var helpers = {};
        var utilities = {};
        var converters = {};
        var validators = {};
        var formatters = {};
        var parsers = {};
        var builders = {};
        var factories = {};
        var managers = {};
        var handlers = {};
        var processors = {};
        var analyzers = {};
        var generators = {};
        var calculators = {};
        var transformers = {};
        var serializers = {};
        var deserializers = {};
        var encoders = {};
        var decoders = {};
        var compressors = {};
        var extractors = {};
        var filters = {};
        var sorters = {};
        var mappers = {};
        var reducers = {};
        var collectors = {};
        var aggregators = {};
        var accumulators = {};
        var iterators = {};
        var enumerators = {};
        var comparators = {};
        var evaluators = {};
        var executors = {};
        var schedulers = {};
        var dispatchers = {};
        var routers = {};
        var controllers = {};
        var presenters = {};
        var viewmodels = {};
        var repositories = {};
        var datasources = {};
        var connections = {};
        var transactions = {};
        var sessions = {};
        var caches = {};
        var logs = {};
        var metrics = {};
        var monitors = {};
        var trackers = {};
        var reporters = {};
        var notifiers = {};
        var messengers = {};
        var publishers = {};
        var subscribers = {};
        var listeners = {};
        var observers = {};
        var broadcasters = {};
        var receivers = {};
        var transmitters = {};
        var communicators = {};
        var coordinators = {};
        var orchestrators = {};
        var choreographers = {};
        var directors = {};
        var supervisors = {};
        var administrators = {};
        var operators = {};
        var maintainers = {};
        var installers = {};
        var deployers = {};
        var migrators = {};
        var updaters = {};
        var patchers = {};
        var fixers = {};
        var cleaners = {};
        var optimizers = {};
        var compilers = {};
        var linkers = {};
        var loaders = {};
        var runners = {};
        var executors2 = {};
        var finalResult = "This function is way too long and should be refactored into smaller, more manageable functions";
        return finalResult;
    }
    
    function testLineLength() {
        var reallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyLongVariableName = "This line is extremely long and exceeds the maximum line length limit of 120 characters which should trigger a violation";
        return reallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyReallyLongVariableName;
    }
    
    function noReturnType(name) {
        return "Hello " & name;
    }
    
    function hasReturnType(required name, age, active) {
        return "Person: " & name & " (" & age & ")";
    }
}
