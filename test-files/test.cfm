&lt;cfscript&gt;
    // Simple CFML test file
    function testFunction() {
        var x = 1;
        var y = 2;
        return x + y;
    }
    
    result = testFunction();
    writeOutput("Result: " & result);
&lt;/cfscript&gt;

&lt;cfquery name="testQuery" datasource="myDS"&gt;
    SELECT * FROM users
    WHERE id = &lt;cfqueryparam value="1" cfsqltype="cf_sql_integer"&gt;
&lt;/cfquery&gt;

&lt;cfoutput&gt;
    &lt;h1&gt;Test Page&lt;/h1&gt;
    &lt;p&gt;Query record count: #testQuery.recordcount#&lt;/p&gt;
&lt;/cfoutput&gt;
