
    public class Lexical {

    // ====== CONFIG ======
    private static final Set<String> KEYWORDS =
            new HashSet<>(Arrays.asList("if", "then", "else"));

    // Arithmetic + relational operators (not including '=' to treat it as ASSIGN)
    private static final Set<String> OPERATORS =
            new HashSet<>(Arrays.asList("+", "-", "*", "/", ">", "<"));

    // ====== SYMBOL TABLE ======
    private static final List<Symbol> symbolTable = new ArrayList<>();

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter your code (type 'end' on a new line to stop):");

        int line = 1;
        while (true) {
            System.out.print("Line " + line + ": ");
            String text = input.nextLine();
            if (text.equalsIgnoreCase("end")) break;

            analyzeLine(text, line);
            line++;
        }


        printSymbolTable();
    }

    private static void analyzeLine(String text, int lineNumber) {

        StringTokenizer tokenizer =
                new StringTokenizer(text, " +-*/=()><;", true);

        while (tokenizer.hasMoreTokens()) {
            String raw = tokenizer.nextToken();
            String token = raw.trim();

        
            if (token.isEmpty()) continue;

            // ====== KEYWORDS ======
            if (KEYWORDS.contains(token)) {
                addToken(lineNumber, "KEYWORD", token);
            }
            // ====== ASSIGNMENT OPERATOR ======
            else if ("=".equals(token)) {
                addToken(lineNumber, "ASSIGN", token);
            }
            // ====== OTHER OPERATORS ======
            else if (OPERATORS.contains(token)) {
                addToken(lineNumber, "OPERATOR", token);
            }
            // ====== IDENTIFIER ======
            else if (isIdentifier(token)) {
                addToken(lineNumber, "IDENTIFIER", token);
            }
            // ====== INTEGER LITERAL ======
            else if (isInteger(token)) {
                addToken(lineNumber, "INTEGER_LITERAL", token);
            }
            // ====== ERROR CASES ======
            else {
                classifyAndReportError(token, lineNumber);
            }
        }
    }

    // ---------- Helper: identifier ----------

    private static boolean isIdentifier(String s) {
        if (s.isEmpty()) return false;
        char first = s.charAt(0);
        if (!Character.isLetter(first) && first != '_') return false;

        for (char c : s.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c != '_') {
                return false;
            }
        }
        return true;
    }

    // ---------- Helper: integer ----------
    private static boolean isInteger(String s) {
        if (s.isEmpty()) return false;
        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    // ---------- Helper: error classification ----------
    private static void classifyAndReportError(String token, int line) {
        boolean startsWithDigit = Character.isDigit(token.charAt(0));
        boolean hasLetter = false;
        boolean hasInvalidChar = false;
        char invalidChar = 0;

        for (char c : token.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (!Character.isLetterOrDigit(c) && c != '_') {
                hasInvalidChar = true;
                invalidChar = c;
            }
        }

        if (startsWithDigit) {
          
            if (hasLetter) {
                System.out.printf(
                        "[Line %d] ERROR: Illegal identifier (starts with digit): %s%n",
                        line, token);
            } else {
              
                System.out.printf(
                        "[Line %d] ERROR: Invalid integer literal (mixed digits and other chars): %s%n",
                        line, token);
            }
        } else if (hasInvalidChar) {
         
            System.out.printf(
                    "[Line %d] ERROR: Illegal identifier (invalid character '%c'): %s%n",
                    line, invalidChar, token);
        } else {
            
            System.out.printf(
                    "[Line %d] ERROR: Unrecognized token: %s%n",
                    line, token);
        }
    }

    // ---------- Helper: add token to symbol table + print ----------
    private static void addToken(int line, String type, String lexeme) {
 
        symbolTable.add(new Symbol(lexeme, type, line));
 
        System.out.printf("[Line %d] %-16s -> %s%n", line, type, lexeme);
    }

    // ---------- Print symbol table ----------
    private static void printSymbolTable() {
        System.out.println("\n===== SYMBOL TABLE =====");
        System.out.printf("%-5s %-16s %-10s%n", "Line", "Type", "Lexeme");
        System.out.println("----------------------------------------");
        for (Symbol s : symbolTable) {
            System.out.printf("%-5d %-16s %-10s%n",
                    s.line, s.type, s.lexeme);
        }
    }

    // ---------- Symbol class ----------
    private static class Symbol {
        String lexeme;
        String type;
        int line;

        Symbol(String lexeme, String type, int line) {
            this.lexeme = lexeme;
            this.type = type;
            this.line = line;
        }
    }
}
    
}
