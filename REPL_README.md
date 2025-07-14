# CFML REPL Feature

The Lucee Toolbox now includes a **CFML REPL (Read-Eval-Print Loop)** for interactive CFML development and testing.

## Quick Start

### Option 1: Using the JAR directly
```bash
java -jar target/lucee-toolbox-1.0.0.jar --mode repl
```

### Option 2: Using the convenience script
```bash
./cfml-repl.sh
```

## Features

- **Interactive CFML Evaluation**: Type CFML code and see results immediately
- **Command History**: Navigate through your previous commands
- **Built-in Help**: Type `help` to see available commands  
- **Screen Clearing**: Type `clear` to clear the screen
- **Easy Exit**: Type `exit` or `quit` to leave the REPL

## Available Commands

| Command | Description |
|---------|-------------|
| `help` | Show help message with examples |
| `history` | Show command history |
| `clear` | Clear the screen |
| `exit` / `quit` | Exit the REPL |

## CFML Examples

Here are some examples you can try in the REPL:

```cfml
cfml> writeOutput('Hello, World!')
Hello, World!

cfml> name = 'Lucee'
Variable 'name' set to Lucee

cfml> now()
Mon Jul 14 12:57:00 BST 2025

cfml> arrayLen([1,2,3])
3

cfml> structKeyExists({a:1}, 'a')
true
```

## Current Implementation

The current implementation uses **mock evaluation** for demonstration purposes. The REPL shows:
- Variable assignments
- Function calls  
- Expression evaluation
- Basic CFML syntax handling

## Future Enhancements

To enable **full Lucee integration**, the following needs to be added:

1. **Lucee Dependencies**: Add proper Lucee Maven dependencies
2. **Script Engine Integration**: Use Lucee's JavaScript ScriptEngine API
3. **Context Persistence**: Maintain variables and functions between evaluations
4. **Advanced Features**: 
   - Multi-line input support
   - Code completion
   - Syntax highlighting
   - Error handling with stack traces

## Integration with VS Code

The REPL can be integrated into the Lucee Toolbox VS Code extension:

1. **Terminal Integration**: Launch REPL in integrated terminal
2. **Code Execution**: Send selected code to REPL
3. **Output Display**: Show REPL results in output panel
4. **Command Palette**: Add "Start CFML REPL" command

## Development

To add full Lucee integration:

1. Add Lucee dependencies to `pom.xml`
2. Update `CFMLRepl.java` to use Lucee's ScriptEngine
3. Implement proper CFML context management
4. Add support for complex CFML features

## Building

```bash
mvn clean package
```

This creates `target/lucee-toolbox-1.0.0.jar` with the REPL included.

## License

Same as Lucee Toolbox - see main project license.
