component {

    property name="maxThreads" type="numeric" default="5";
    property name="timeout" type="numeric" default="60";

    static {
        THREAD_PREFIX = "batchProcessor_";
        DEFAULT_TIMEOUT = 60;
    }

    function init(numeric maxThreads = 5, numeric timeout = 60) {
        variables.maxThreads = arguments.maxThreads;
        variables.timeout = arguments.timeout;
        return this;
    }

    /**
     * Processes a batch of items using threads with proper locking
     */
    struct function processBatch(required array items, required string processingFunction) localMode="true" {
        local.results = {
            success: [],
            errors: [],
            totalProcessed: 0
        };

        local.threadNames = [];
        local.batchSize = ceiling(arrayLen(arguments.items) / variables.maxThreads);

        // Create threads for batch processing
        for (local.i = 1; local.i <= variables.maxThreads; local.i++) {
            local.startIndex = (local.i - 1) * local.batchSize + 1;
            local.endIndex = min(local.i * local.batchSize, arrayLen(arguments.items));

            if (local.startIndex <= arrayLen(arguments.items)) {
                local.threadName = static.THREAD_PREFIX & createUUID();
                arrayAppend(local.threadNames, local.threadName);

                local.batchItems = [];
                for (local.j = local.startIndex; local.j <= local.endIndex; local.j++) {
                    arrayAppend(local.batchItems, arguments.items[local.j]);
                }

                thread name=local.threadName
                       batchItems=local.batchItems
                       processingFunction=arguments.processingFunction {
                    
                    thread.results = {
                        success: [],
                        errors: []
                    };

                    for (local.item in attributes.batchItems) {
                        try {
                            local.result = invoke("", attributes.processingFunction, {item: local.item});
                            arrayAppend(thread.results.success, local.result);
                        } catch (any e) {
                            arrayAppend(thread.results.errors, {
                                item: local.item,
                                error: e.message
                            });
                        }
                    }
                }
            }
        }

        // Wait for all threads to complete and collect results
        local.timeoutTime = getTickCount() + (variables.timeout * 1000);
        
        while (getTickCount() < local.timeoutTime) {
            local.allComplete = true;
            
            for (local.threadName in local.threadNames) {
                if (!structKeyExists(cfthread, local.threadName) || 
                    cfthread[local.threadName].status != "COMPLETED") {
                    local.allComplete = false;
                    break;
                }
            }
            
            if (local.allComplete) {
                break;
            }
            
            sleep(100); // Wait 100ms before checking again
        }

        // Collect results with proper locking
        cflock(name="batchProcessorResults", timeout=10, throwOnTimeout=true) {
            for (local.threadName in local.threadNames) {
                if (structKeyExists(cfthread, local.threadName)) {
                    local.threadData = cfthread[local.threadName];
                    
                    if (local.threadData.status == "COMPLETED" && 
                        structKeyExists(local.threadData, "results")) {
                        
                        arrayAppend(local.results.success, local.threadData.results.success, true);
                        arrayAppend(local.results.errors, local.threadData.results.errors, true);
                    } else {
                        arrayAppend(local.results.errors, {
                            thread: local.threadName,
                            error: "Thread failed to complete or timed out"
                        });
                    }
                }
            }
        }

        local.results.totalProcessed = arrayLen(local.results.success) + arrayLen(local.results.errors);
        return local.results;
    }

    /**
     * Processes items asynchronously with proper error handling
     */
    string function processAsync(required array items, required string processingFunction) localMode="true" {
        local.threadName = static.THREAD_PREFIX & "async_" & createUUID();

        thread name=local.threadName
               items=arguments.items
               processingFunction=arguments.processingFunction
               timeout=variables.timeout {
            
            thread.startTime = now();
            thread.results = {
                success: [],
                errors: [],
                completed: false
            };

            try {
                for (local.item in attributes.items) {
                    local.result = invoke("", attributes.processingFunction, {item: local.item});
                    arrayAppend(thread.results.success, local.result);
                }
                thread.results.completed = true;
            } catch (any e) {
                thread.results.error = e.message;
                thread.results.completed = false;
            }

            thread.endTime = now();
            thread.duration = dateDiff("s", thread.startTime, thread.endTime);
        }

        return local.threadName;
    }

    /**
     * Gets the status of an async process
     */
    struct function getAsyncStatus(required string threadName) localMode="true" {
        local.status = {
            exists: false,
            completed: false,
            results: {},
            error: ""
        };

        if (structKeyExists(cfthread, arguments.threadName)) {
            local.threadData = cfthread[arguments.threadName];
            local.status.exists = true;
            local.status.completed = (local.threadData.status == "COMPLETED");
            
            if (local.status.completed && structKeyExists(local.threadData, "results")) {
                local.status.results = local.threadData.results;
            }
            
            if (structKeyExists(local.threadData, "error")) {
                local.status.error = local.threadData.error;
            }
        }

        return local.status;
    }

    /**
     * Safely increments a shared counter using proper locking
     */
    numeric function incrementProcessedCount() localMode="true" {
        if (isNull(application.processedCount)) {
            cflock(name="processedCountInit", timeout=5, throwOnTimeout=true) {
                if (isNull(application.processedCount)) {
                    application.processedCount = 0;
                }
            }
        }

        cflock(name="processedCountIncrement", timeout=5, throwOnTimeout=true) {
            application.processedCount++;
            local.newCount = application.processedCount;
        }

        return local.newCount;
    }

    /**
     * Example processing function
     */
    struct function processItem(required struct item) localMode="true" {
        // Simulate processing time
        sleep(randRange(100, 500));
        
        return {
            id: arguments.item.id,
            processed: true,
            processedAt: now()
        };
    }

}
