import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Transient Campaign 1 mutation applier (single-file source program, JDK 21 launcher).
 *
 * <pre>java Mutator.java mutants.txt list            -- print "ID SELECTION" lines
 * java Mutator.java mutants.txt apply ID /repo    -- apply one mutant; fails unless old text occurs exactly once</pre>
 */
public class Mutator {

    record Mutant(String id, String file, String selection, String note, String oldText, String newText) {}

    static List<Mutant> parse(Path definitions) throws Exception {
        List<Mutant> mutants = new ArrayList<>();
        String id = null, file = null, selection = null, note = null;
        StringBuilder oldText = null, newText = null, current = null;
        for (String line : Files.readAllLines(definitions, StandardCharsets.UTF_8)) {
            if (line.startsWith("=== ")) {
                if (id != null) {
                    mutants.add(new Mutant(id, file, selection, note, strip(oldText), strip(newText)));
                }
                id = line.substring(4).trim();
                file = selection = note = null;
                oldText = newText = current = null;
            } else if (current == null && line.startsWith("file: ")) {
                file = line.substring(6).trim();
            } else if (current == null && line.startsWith("selection: ")) {
                selection = line.substring(11).trim();
            } else if (current == null && line.startsWith("note: ")) {
                note = line.substring(6).trim();
            } else if (line.equals("--- old")) {
                current = oldText = new StringBuilder();
            } else if (line.equals("--- new")) {
                current = newText = new StringBuilder();
            } else if (current != null) {
                current.append(line).append('\n');
            }
        }
        if (id != null) {
            mutants.add(new Mutant(id, file, selection, note, strip(oldText), strip(newText)));
        }
        return mutants;
    }

    static String strip(StringBuilder text) {
        if (text == null) {
            throw new IllegalArgumentException("missing old/new block");
        }
        String s = text.toString();
        return s.endsWith("\n") ? s.substring(0, s.length() - 1) : s;
    }

    public static void main(String[] args) throws Exception {
        List<Mutant> mutants = parse(Path.of(args[0]));
        if (args[1].equals("list")) {
            for (Mutant m : mutants) {
                System.out.println(m.id() + " " + m.selection());
            }
            return;
        }
        Mutant mutant = mutants.stream().filter(m -> m.id().equals(args[2])).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown mutant " + args[2]));
        Path target = Path.of(args[3]).resolve(mutant.file());
        String source = Files.readString(target, StandardCharsets.UTF_8).replace("\r\n", "\n");
        int first = source.indexOf(mutant.oldText());
        if (first < 0 || source.indexOf(mutant.oldText(), first + 1) >= 0) {
            System.err.println("MUTANT-NOT-UNIQUE " + mutant.id());
            System.exit(3);
        }
        Files.writeString(target, source.substring(0, first) + mutant.newText()
                + source.substring(first + mutant.oldText().length()), StandardCharsets.UTF_8);
        System.out.println("APPLIED " + mutant.id() + " " + mutant.file());
    }
}
