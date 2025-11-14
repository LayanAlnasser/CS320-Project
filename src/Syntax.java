import java.util.*;
public class Syntax {

    // ====== OPTIONS ======
    private static final boolean SHOW_PARSE_TREE = true;
    private static final boolean SHOW_TRACE      = false;

    // ====== TOKEN TYPES ======
    enum TokenType {
        IDENT,          // identifier
        INT_LITERAL,    // integer literal
        PLUS, MINUS,    // + -
        STAR, SLASH,    // * /
        LPAREN, RPAREN, // ( )
        EOF,            // end of input
        ERROR           // lexical error token
    }

    // ====== TOKEN CLASS ======
    static class Token {
        final TokenType type;
        final String lexeme;
        final int pos;        // position in the line (0-based)
        final String lexError; // lexical error message (if any)

        Token(TokenType type, String lexeme, int pos) {
            this(type, lexeme, pos, null);
        }

        Token(TokenType type, String lexeme, int pos, String lexError) {
            this.type = type;
            this.lexeme = lexeme;
            this.pos = pos;
            this.lexError = lexError;
        }

        @Override
        public String toString() {
            return type + "('" + lexeme + "')";
        }
    }

    // ====== LEXER ======
    static class Lexer {
        private final String input;
        private final int length;
        private int index = 0;

        Lexer(String input) {
            this.input = input;
            this.length = input.length();
        }

        List<Token> tokenize() {
            List<Token> tokens = new ArrayList<>();

            while (true) {
                skipWhitespace();
                if (index >= length) {
                    tokens.add(new Token(TokenType.EOF, "", index));
                    break;
                }

                char c = input.charAt(index);
                int startPos = index;

                // identifier: letter or '' then letters/digits/
                if (Character.isLetter(c) || c == '_') {
                    StringBuilder sb = new StringBuilder();
                    sb.append(c);
                    index++;
                    while (index < length) {
                        char nc = input.charAt(index);
                        if (Character.isLetterOrDigit(nc) || nc == '_') {
                            sb.append(nc);
                            index++;
                        } else break;
                    }
                    tokens.add(new Token(TokenType.IDENT, sb.toString(), startPos));
                }
                // integer literal (valid or invalid)
                else if (Character.isDigit(c)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(c);
                    index++;
                    boolean invalid = false;

                    while (index < length) {
                        char nc = input.charAt(index);
                        if (Character.isDigit(nc)) {
                            sb.append(nc);
                            index++;
                        } else if (Character.isLetter(nc) || nc == '_') { // 23b2, 4int
                            sb.append(nc);
                            index++;
                            invalid = true;
                        } else break;
                    }

                    String lex = sb.toString();
                    if (invalid) {
                        tokens.add(new Token(
                                TokenType.ERROR,
                                lex,
                                startPos,
                                "E005 Invalid integer literal (digits mixed with letters): " + lex
                        ));
                    } else {
                        tokens.add(new Token(TokenType.INT_LITERAL, lex, startPos));
                    }
                }
                // single-character tokens
                else {
                    switch (c) {
                        case '+':
                            tokens.add(new Token(TokenType.PLUS, "+", startPos));
                            index++;
                            break;
                        case '-':
                            tokens.add(new Token(TokenType.MINUS, "-", startPos));
                            index++;
                            break;
                        case '*':
                            tokens.add(new Token(TokenType.STAR, "*", startPos));
                            index++;
                            break;
                        case '/':
                            tokens.add(new Token(TokenType.SLASH, "/", startPos));
                            index++;
                            break;
                        case '(':
                            tokens.add(new Token(TokenType.LPAREN, "(", startPos));
                            index++;
                            break;
                        case ')':
                            tokens.add(new Token(TokenType.RPAREN, ")", startPos));
                            index++;
                            break;
                        default:
                            // unknown char → lexical error
                            tokens.add(new Token(
                                    TokenType.ERROR,
                                    Character.toString(c),
                                    startPos,
                                    "E006 Invalid character: '" + c + "'"
                            ));
                            index++;
                            break;
                    }
                }
            }

            return tokens;
        }

        private void skipWhitespace() {
            while (index < length && Character.isWhitespace(input.charAt(index))) {
                index++;
            }
        }
    }

    // ====== PARSE TREE NODE ======
    static class Node {
        final String label;
        final List<Node> children = new ArrayList<>();

        Node(String label) { this.label = label; }

        void add(Node child) { children.add(child); }
    }

    // ====== PARSER ======
    static class Parser {
        private final List<Token> tokens;
        private final String originalInput;
        private int index = 0;

        // error list
        static class Err {
            final String code;
            final String message;
            final int pos;

            Err(String code, String message, int pos) {
                this.code = code;
                this.message = message;
                this.pos = pos;
            }
        }

        final List<Err> errors = new ArrayList<>();

        Parser(List<Token> tokens, String originalInput) {
            this.tokens = tokens;
            this.originalInput = originalInput;
        }

        private Token peek() {
            return tokens.get(index);
        }

        private boolean isAtEnd() {
            return peek().type == TokenType.EOF;
        }

        private Token advance() {
            if (!isAtEnd()) index++;
            return tokens.get(index - 1);
        }

        private void trace(String msg) {
            if (SHOW_TRACE) {
                System.out.println("TRACE: " + msg + " | next=" + peek());
            }
        }

        private void addError(String code, String msg, int pos) {
            errors.add(new Err(code, msg, pos));
        }

        private boolean isPlusOrMinus(Token t) {
            return t.type == TokenType.PLUS || t.type == TokenType.MINUS;
        }

        private boolean isStarOrSlash(Token t) {
            return t.type == TokenType.STAR || t.type == TokenType.SLASH;
        }

        private boolean startsFactor(Token t) {
            return t.type == TokenType.IDENT ||
                   t.type == TokenType.INT_LITERAL ||
                   t.type == TokenType.LPAREN;
        }

        private void syncTo(TokenType... syncTypes) {
            Set<TokenType> set = new HashSet<>(Arrays.asList(syncTypes));
            while (!isAtEnd() && !set.contains(peek().type)) {
                advance();
            }
        }

        // ===== ENTRY POINT =====
        Node parse() {
            Node root = expr();

            // Extra tokens after valid expr
            if (!isAtEnd()) {
                Token t = peek();
                addError("E007",
                        "Extra tokens after end of expression (unexpected '" +
                                t.lexeme + "')",
                        t.pos);
            }

            return root;
        }

        // <expr> → <term> {(+ | -) <term>}
        Node expr() {
            trace("enter expr");
            Node node = new Node("EXPR");
            node.add(term());

            // (+|-) term ...
            while (!isAtEnd() && isPlusOrMinus(peek())) {
                Token op = advance();
                Node bin = new Node("BINOP " + op.lexeme);
                bin.add(term());
                node.add(bin);
            }

           
            while (!isAtEnd() && startsFactor(peek())) {
                Token t = peek();
                addError("E001",
                        "Missing '+' or '-' between terms before '" + t.lexeme + "'",
                        t.pos);
                node.add(term());
            }

            trace("exit expr");
            return node;
        }

        // <term> → <factor> {(* | /) <factor>}
        Node term() {
            trace("enter term");
            Node node = new Node("TERM");
            node.add(factor());

            // (*|/) factor ...
            while (!isAtEnd() && isStarOrSlash(peek())) {
                Token op = advance();
                Node bin = new Node("BINOP " + op.lexeme);
                bin.add(factor());
                node.add(bin);
            }


            while (!isAtEnd() && startsFactor(peek())) {
                Token t = peek();
                addError("E002",
                        "Missing '*' or '/' between factors before '" + t.lexeme + "'",
                        t.pos);
                node.add(factor());
            }

            trace("exit term");
            return node;
        }

        // <factor> → IDENT | INT_LITERAL | '(' <expr> ')'
        Node factor() {
            trace("enter factor");
            Token t = peek();

            // Lexical error from lexer
            if (t.type == TokenType.ERROR) {
             
                String msg = t.lexError != null ? t.lexError : "Lexical error in token: " + t.lexeme;
                addError(msg.substring(0, 4), msg, t.pos); // code أول 4 حروف مثل "E005"
                advance();
                return new Node("ERROR_TOKEN");
            }

            if (t.type == TokenType.IDENT) {
                advance();
                return new Node("IDENT(" + t.lexeme + ")");
            }

            if (t.type == TokenType.INT_LITERAL) {
                advance();
                return new Node("INT(" + t.lexeme + ")");
            }

            if (t.type == TokenType.LPAREN) {
                Token open = advance(); // '('
                Node inside = expr();
                if (peek().type != TokenType.RPAREN) {
                    addError("E003",
                            "Missing ')' to match '('",
                            open.pos);
                    // panic-mode: skip until ) or EOF
                    syncTo(TokenType.RPAREN, TokenType.EOF);
                    if (peek().type == TokenType.RPAREN) {
                        advance(); // consume ')'
                    }
                } else {
                    advance(); // consume ')'
                }
                Node par = new Node("PAREN_EXPR");
                par.add(inside);
                return par;
            }

           
            addError("E004",
                    "Expected identifier, integer literal, or '(' but found '" + t.lexeme + "'",
                    t.pos);
           
            syncTo(TokenType.PLUS, TokenType.MINUS,
                   TokenType.STAR, TokenType.SLASH,
                   TokenType.RPAREN, TokenType.EOF);
            return new Node("ERROR_FACTOR");
        }

        boolean hasErrors() {
            return !errors.isEmpty();
        }

        void printErrors() {
            if (!hasErrors()) {
                System.out.println("No syntax errors.");
                return;
            }

            System.out.println("SYNTAX / LEXICAL ERRORS:");
            for (Err e : errors) {
                System.out.printf("  - [%s] %s (at position %d)%n",
                        e.code, e.message, e.pos);
            }

            // Print input and pointer caret line
            System.out.println("Input:  " + originalInput);
            if (!errors.isEmpty()) {
              
                int firstPos = errors.get(0).pos;
                System.out.print("        ");
                for (int i = 0; i < firstPos; i++) System.out.print(" ");
                System.out.println("^");
            }
        }
    }

    // ====== PRINT PARSE TREE ======
    private static void printTree(Node node) {
        printTree(node, 0);
    }

    private static void printTree(Node node, int indent) {
        for (int i = 0; i < indent; i++) System.out.print("  ");
        System.out.println(node.label);
        for (Node child : node.children) {
            printTree(child, indent + 1);
        }
    }

    // ====== MAIN ======
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== Part 2: Syntax Analyzer (with error types) ===");
        System.out.println("Enter an expression (type 'end' to quit):");

        while (true) {
            System.out.print("> ");
            String line = sc.nextLine();
            if (line.equalsIgnoreCase("end")) break;
            if (line.trim().isEmpty()) continue;

            // 1) Lexical analysis
            Lexer lexer = new Lexer(line);
            List<Token> tokens = lexer.tokenize();

            // 2) Parsing
            Parser parser = new Parser(tokens, line);
            Node tree = parser.parse();

            // 3) Results
            parser.printErrors();
            if (!parser.hasErrors() && SHOW_PARSE_TREE) {
                System.out.println("Parse Tree:");
                printTree(tree);
            }

            System.out.println();
        }
    }
}
