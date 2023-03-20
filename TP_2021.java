
import java.util.*;

class UnknownInstructionException extends Exception {

}

class InvalidValueException extends Exception {

}

class InvalidNameException extends Exception {

}

class SyntaxErrorException extends Exception {

}

class UnknownVariableException extends Exception {
    public UnknownVariableException() {
    }
}

class ParentheseFermanteException extends Exception {

}

class ParentheseOuvranteException extends Exception {

}

interface Extractable {
    void ExtraireComposants(String s) throws Exception;
}

class Print {
    private Expression expression;

    public Print(Expression expression) {
        this.expression = expression;
    }

    public void print() {
        try {
            if (Check()) {
                System.out.println("La valeur est : " + expression.Evaluer());
            }
        } catch (ParentheseFermanteException e) {
            System.err.println("Erreur : parenthèse fermante manquante");

        } catch (ParentheseOuvranteException e) {
            System.err.println("Erreur : parenthèse ouvrante manquante");
        } catch (UnknownVariableException e) {
            System.err.println("Erreur : unknown variable");
        } catch (Exception e) {
            System.err.println("Erreur : Expression erronée");
        }
    }

    private boolean Check() throws ParentheseOuvranteException, ParentheseFermanteException {
        ExpressionParenthesee e = new ExpressionParenthesee(expression.toString());
        return e.evaluate();
    }

    public Expression getExpression() {
        return expression;
    }

}

interface Evaluable {
    double Evaluer() throws Exception;
}

class CoupleTerme {
    private Terme terme;
    private Character signe;

    public CoupleTerme(Character signe, Terme terme) throws SyntaxErrorException {
        if (signe != '+' && signe != '-') {
            System.out.println("at 83");
            throw new SyntaxErrorException();
        }
        this.signe = signe;
        this.terme = terme;
    }

    public Terme getTerme() {
        return terme;
    }

    public Character getSigne() {
        return signe;
    }

}

class CoupleFacteur {
    private Facteur facteur;
    private Character operation;

    public CoupleFacteur(Character operation, Facteur facteur) throws SyntaxErrorException {
        if (operation != '/' && operation != '*') {
            System.out.println("at 105");
            throw new SyntaxErrorException();
        }
        this.operation = operation;
        this.facteur = facteur;
    }

    public Facteur getFacteur() {
        return facteur;
    }

    public Character getOperation() {
        return operation;
    }

}

class Terme implements Evaluable, Extractable {
    List<CoupleFacteur> facteurs;

    public Terme(String fct) throws Exception {
        this.facteurs = new ArrayList<>();
        ExtraireComposants(fct);
    }

    @Override
    public String toString() {
        if (facteurs.size() == 0)
            return "";
        StringBuilder sb = new StringBuilder("");
        sb.append(facteurs.get(0).toString());
        for (int i = 1; i < facteurs.size(); i++) {
            sb.append(facteurs.get(i).getOperation());
            sb.append(facteurs.get(i).getFacteur().toString());
        }
        return sb.toString();
    }

    public List<CoupleFacteur> getFacteurs() {
        return facteurs;
    }

    @Override
    public double Evaluer() throws ArithmeticException, SyntaxErrorException, Exception {
        double res = ((CoupleFacteur) facteurs.get(0)).getFacteur().Evaluer();
        for (int i = 1; i < facteurs.size(); i++) {
            switch (facteurs.get(i).getOperation()) {
                case '*':
                    res *= facteurs.get(i).getFacteur().Evaluer();
                    break;
                case '/':
                    try {
                        res /= facteurs.get(i).getFacteur().Evaluer();
                    } catch (ArithmeticException e) {
                        System.err.println("Cannot divide / 0");
                        e.printStackTrace();
                    }
                    break;
                default:
                    System.out.println("at 164");
                    throw new SyntaxErrorException();
            }
        }
        return res;
    }

    @Override
    public void ExtraireComposants(String s) throws Exception {
        char c = '*';
        int k = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '*' || s.charAt(i) == '/') {
                CoupleFacteur ct = new CoupleFacteur(c, new Facteur(s.substring(k, i)));
                this.facteurs.add(ct);
                c = s.charAt(i);
                k = i + 1;
            }
        }
        CoupleFacteur ct = new CoupleFacteur(c, new Facteur(s.substring(k)));
        this.facteurs.add(ct);
    }

}

class Facteur implements Evaluable, Extractable {
    List<Element> elements;

    public Facteur(String elems) throws Exception {
        this.elements = new ArrayList<>();
        ExtraireComposants(elems);
    }

    @Override
    public String toString() {
        if (elements.size() == 0)
            return "";
        StringBuilder sb = new StringBuilder("");
        sb.append(elements.get(0).toString());
        for (int i = 1; i < elements.size(); i++) {
            sb.append('^');
            sb.append(elements.get(i).toString());
        }
        return sb.toString();
    }

    public List<Element> getElements() {
        return elements;
    }

    @Override
    public double Evaluer() throws Exception {
        double res = elements.get(0).Evaluer();
        for (int i = 1; i < elements.size(); i++) {
            res = Math.pow(res, elements.get(i).Evaluer());
        }
        return res;
    }

    @Override
    public void ExtraireComposants(String s) throws Exception {
        String[] ar = s.split("^");
        for (String st : ar) {
            try {
                this.elements.add(new Nombre(st));
            } catch (InvalidValueException e1) {
                try {
                    this.elements.add(new Variable(st));
                } catch (InvalidNameException e2) {
                    try {
                        this.elements.add(new Expression(st));
                    } catch (Exception e3) {
                        this.elements.add(new Fonction(Type.valueOf(st.substring(0, st.indexOf("("))),
                                new Expression(st.substring(st.indexOf("(") + 1))));
                    }
                }
            }
        }
    }
}

enum Type {
    SIN("sin"), COS("cos"), TAN("tan"),
    SQRT("sqrt"), ABS("abs");

    String nom;

    private Type(String nom) {
        this.nom = nom;
    }

    public String getNom() {
        return nom;
    }
}

class Fonction extends Element {
    Expression expression;
    Type type;

    public Fonction(Type type, Expression expression) {
        super(type.getNom());
        this.expression = expression;
        this.type = type;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        sb.append(type.getNom());
        sb.append('(');
        sb.append(expression.toString());
        sb.append(')');
        return sb.toString();
    }

    @Override
    public double Evaluer() throws Exception {
        switch (type) {
            case SIN:
                return Math.sin(expression.Evaluer());
            case COS:
                return Math.cos(expression.Evaluer());
            case TAN:
                return Math.tan(expression.Evaluer());
            case SQRT:
                return Math.sqrt(expression.Evaluer());
            case ABS:
                return Math.abs(expression.Evaluer());
            default:
                return 0;
        }
    }
}

class Variable extends Element {
    public Variable(String nom) throws InvalidNameException {
        super(nom);
        for (char c : nom.toCharArray()) {
            if (!Character.isDigit(c) && !Character.isLetter(c)) {
                System.out.println("at 308");
                throw new InvalidNameException();
            }
        }
    }

    @Override
    public String toString() {
        return valeur;
    }

    @Override
    public double Evaluer() throws UnknownVariableException {
        try {
            return Let.map.get(this.valeur).Evaluer();
        } catch (Exception e) {
            System.out.println("at 324");
            throw new UnknownVariableException();
        }
    }

}

class Nombre extends Element {

    public Nombre(String valeur) throws InvalidValueException {
        super(valeur);
        for (char c : valeur.toCharArray()) {
            if (!Character.isDigit(c)) {
                System.out.println("at 337");
                throw new InvalidValueException();
            }
        }
    }

    @Override
    public String toString() {
        return valeur;
    }

    public double Evaluer() {
        return Double.parseDouble(valeur);
    }
}

class Pile {
    public Deque<Character> pile = new LinkedList<Character>();

    public void empiler(char x) {
        pile.add(x);
    }

    public Character depiler() throws Exception {
        if (this.estVide()) {
            System.out.println("at 362");
            throw new EmptyStackException();
        }
        return pile.pop();
    }

    public Character sommet() {
        return pile.peek();
    }

    public boolean estVide() {
        return pile.isEmpty();
    }
}

class ExpressionParenthesee {
    String expression;

    public ExpressionParenthesee(String expression) {
        this.expression = expression;
    }

    public boolean evaluate() throws ParentheseFermanteException, ParentheseOuvranteException {
        if (expression == null)
            return true;
        Pile pile = new Pile();
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == '(') {
                pile.empiler(c);
            } else if (c == ')') {
                try {
                    while (pile.depiler() != '(') {
                    }
                } catch (Exception ex) {
                    System.out.println("at 397");
                    throw new ParentheseOuvranteException();
                }
            }
        }
        if (pile.estVide()) {
            return true;
        } else {
            System.out.println("at 405");
            throw new ParentheseFermanteException();
        }
    }
}

abstract class Element implements Evaluable {

    protected String valeur;

    public Element(String valeur) {
        this.valeur = valeur;
    }
}

class Expression extends Element implements Extractable {
    List<CoupleTerme> termes;

    public Expression(String exp) throws Exception {
        super(trimParentheses(exp));
        exp = trimParentheses(exp);
        System.out.println("REE " + exp);
        this.termes = new ArrayList<>();
        ExtraireComposants(exp);
    }

    private static String trimParentheses(String st){
        StringBuilder sb = new StringBuilder("");
        int f=0,l=st.length();
        for(int i=0;i<st.length();i++){
            if(st.charAt(i)!='(')
            {
                f=i;
                break;
            }
        }
        for(int j=st.length()-1;j>=0;j--){
            if(st.charAt(j)!=')')
            {
                l=j;
                break;
            }
        }
        return st.substring(f,l+1);
    }

    public double Evaluer() throws Exception {
        double res = (termes.get(0).getSigne() == '-' ? -1 : 1) * termes.get(0).getTerme().Evaluer();
        for (int i = 1; i < termes.size(); i++) {
            switch (termes.get(i).getSigne()) {
                case '+':
                    res += termes.get(i).getTerme().Evaluer();
                    break;
                case '-':
                    res -= termes.get(i).getTerme().Evaluer();
                    break;
            }
        }
        return res;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        if (termes.get(0).getSigne() == '-') {
            sb.append('-');
        }
        sb.append(termes.get(0).getTerme().toString());
        for (int i = 1; i < termes.size(); i++) {
            sb.append(termes.get(i).getSigne());
            sb.append(termes.get(i).getTerme().toString());
        }
        return sb.toString();
    }

    public void addTerme(CoupleTerme t) {
        this.termes.add(t);
    }

    public List<CoupleTerme> getTermes() {
        return termes;
    }

    @Override
    public void ExtraireComposants(String s) throws Exception {
        char c = s.charAt(0) == '-' ? '-' : '+';
        int k = c == '+' ? 0 : 1;
        for (int i = c == '+' ? 0 : 1; i < s.length(); i++) {
            if (s.charAt(i) == '+' || s.charAt(i) == '-') {
                CoupleTerme ct = new CoupleTerme(c, new Terme(s.substring(k, i)));
                this.termes.add(ct);
                c = s.charAt(i);
                k = i + 1;
            }
        }
        CoupleTerme ct = new CoupleTerme(c, new Terme(s.substring(k)));
        this.termes.add(ct);
    }

}

class Let implements Extractable {
    public static final Map<String, Expression> map = new HashMap<>(); // maps var -> value

    public Let(String let) throws Exception {
        ExtraireComposants(let);
        System.out.println("Ok");
    }

    @Override
    public void ExtraireComposants(String s) throws Exception {
        if (!s.contains("=")) {
            System.out.println("at 496");
            throw new SyntaxErrorException();
        }
        Variable variable = new Variable(s.substring(0, s.indexOf('=')).trim());
        Expression expression = new Expression(s.substring(s.indexOf('=') + 1).trim());
        map.put(variable.toString(), expression);
    }

}

public class TP_2021 {
    public static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        System.out.print(">> ");
        String inst = sc.nextLine();
        while (!inst.equals("end")) {
            inst = inst.toLowerCase();
            try {
                if (inst.equals("show")) {
                    System.out.println(Let.map.toString());
                } else {
                    switch (Check(inst)) {
                        case "print":
                            Print p = new Print(new Expression(inst.substring(inst.indexOf(" ")).trim()));
                            p.print();
                            break;
                        case "let":
                            Let l = new Let(inst.substring(inst.indexOf(" ")).trim());
                            break;
                    }
                }
            } catch (UnknownInstructionException e) {
                System.err.println("Erreur : print ou let seulement");
            }
            System.out.print(">> ");
            inst = sc.nextLine();
        }
    }

    static String Check(String st) throws UnknownInstructionException {
        String[] ar = st.split(" ");
        if (!ar[0].equals("print") && !ar[0].equals("let")) {
            System.out.println("at 534");
            throw new UnknownInstructionException();
        }
        return ar[0];
    }
}
