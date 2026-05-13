package it.unibs.ingsoft.presentation.view.cli.common;

import it.unibs.ingsoft.presentation.view.interfaces.common.IAppView;
import it.unibs.ingsoft.presentation.view.interfaces.common.OperationCancelledException;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Implementazione console (stdin/stdout) di {@link IAppView}.
 */
public final class ConsoleUI implements IAppView {
    public static final String CANCEL_KEYWORD = "annulla";

    public static final String HINT_ANNULLA =
            "Digita '" + CANCEL_KEYWORD + "' per annullare.";

    private static final String SEPARATORE_DOPPIO = "=".repeat(60);

    private final Scanner scanner;

    public ConsoleUI(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public void stampa(String testo) {
        System.out.println(testo);
    }

    @Override
    public void newLine() {
        System.out.println();
    }

    @Override
    public void header(String titolo) {
        newLine();
        System.out.println(SEPARATORE_DOPPIO);
        System.out.println("  " + titolo.toUpperCase());
        System.out.println(SEPARATORE_DOPPIO);
    }

    @Override
    public void stampaSezione(String titolo) {
        newLine();
        System.out.println("-- " + titolo + " " + "-".repeat(Math.max(0, 56 - titolo.length())));
    }

    @Override
    public void pausa() {
        System.out.print("Premere INVIO per continuare...");
        scanner.nextLine();
    }

    @Override
    public void stampaSuccesso(String msg) {
        stampa("  OK | " + msg);
    }

    @Override
    public void stampaErrore(String msg) {
        stampa("  !! | " + msg);
    }

    @Override
    public void stampaAvviso(String msg) {
        stampa("  /!\\  | " + msg);
    }

    @Override
    public void stampaInfo(String msg) {
        stampa("  (i)  | " + msg);
    }

    @Override
    public void pausaConSpaziatura() {
        IAppView.super.pausaConSpaziatura();
    }

    @Override
    public void stampaMenu(String titolo, String[] lista, String uscitaLabel) {
        if (!titolo.isBlank())
            header(titolo);

        if (lista.length == 0)
            return;

        IntStream.range(0, lista.length)
                .forEach(i -> stampa((i + 1) + ") " + lista[i]));

        stampa("0) " + uscitaLabel);
        newLine();
    }

    @Override
    public void stampaMenu(String titolo, String[] lista) {
        stampaMenu(titolo, lista, "Torna");
    }

    @Override
    public String acquisisciStringa(String prompt) {
        System.out.print(prompt);
        String line = scanner.nextLine();
        String trimmed = (line == null) ? "" : line.trim();

        if (CANCEL_KEYWORD.equalsIgnoreCase(trimmed)) throw new OperationCancelledException();

        return trimmed;
    }

    @Override
    public String acquisisciStringaConValidazione(String prompt,
                                                  Predicate<String> validatore,
                                                  String messaggioErrore) {
        while (true) {
            String val = acquisisciStringa(prompt);
            if (validatore.test(val)) return val;
            stampaErrore(messaggioErrore);
        }
    }

    @Override
    public String acquisisciPassword(String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] pwd = console.readPassword(prompt);
            String value = pwd != null ? new String(pwd) : "";
            String trimmed = value.trim();

            if (CANCEL_KEYWORD.equalsIgnoreCase(trimmed)) throw new OperationCancelledException();

            return value;
        }

        return acquisisciStringa(prompt);
    }

    @Override
    public int acquisisciIntero(String prompt, int min, int max) {
        while (true) {
            String s = acquisisciStringa(prompt);
            try {
                int v = Integer.parseInt(s);

                if (v < min || v > max) {
                    stampa("Inserisci un numero tra " + min + " e " + max + ".");
                    continue;
                }

                return v;
            } catch (NumberFormatException e) {
                stampa("Inserisci un intero valido.");
            }
        }
    }

    @Override
    public boolean acquisisciSiNo(String prompt) {
        while (true) {
            String s = acquisisciStringa(prompt + " (s/n): ").toLowerCase();
            if (s.equals("s") || s.equals("si") || s.equals("sì"))
                return true;
            if (s.equals("n") || s.equals("no"))
                return false;

            stampa("Rispondi con s/n.");
        }
    }

    @Override
    public List<String> acquisisciListaNomi(String titolo) {
        while (true) {
            stampa(titolo);
            stampaInfo("Riga vuota per terminare. " + HINT_ANNULLA);
            newLine();

            List<String> list = new ArrayList<>();

            while (true) {
                System.out.print("[" + list.size() + "] > ");
                String line = scanner.nextLine();
                String trimmed = (line == null) ? "" : line.trim();

                if (CANCEL_KEYWORD.equalsIgnoreCase(trimmed)) throw new OperationCancelledException();

                if (trimmed.isEmpty()) break;

                boolean duplicato = list.stream().anyMatch(n -> n.equalsIgnoreCase(trimmed));
                if (duplicato) {
                    stampaAvviso("'" + trimmed + "' già presente nella lista, ignorato.");
                    continue;
                }

                list.add(trimmed);
            }

            if (list.isEmpty()) {
                stampaAvviso("Nessun nome inserito.");
                if (!acquisisciSiNo("Vuoi riprovare?")) return list;
                newLine();
                continue;
            }

            String riepilogo = String.join(", ", list);
            stampaInfo(list.size() + " element" + (list.size() == 1 ? "o" : "i") +
                    " inserit" + (list.size() == 1 ? "o" : "i") + ": " + riepilogo);

            if (acquisisciSiNo("Confermare?")) return list;

            newLine();
        }
    }

    @Override
    public <T> Optional<T> selezionaElemento(String prompt, List<T> elementi) {
        return selezionaElemento(prompt, elementi, String::valueOf);
    }

    @Override
    public <T> Optional<T> selezionaElemento(String prompt, List<T> elementi,
                                             Function<T, String> labelMapper) {
        if (elementi.isEmpty()) {
            stampa("  (nessun elemento disponibile)");
            return Optional.empty();
        }

        stampa(prompt);
        for (int i = 0; i < elementi.size(); i++)
            stampa("  " + (i + 1) + ") " + labelMapper.apply(elementi.get(i)));
        stampa("  0) Annulla");
        newLine();

        try {
            int choice = acquisisciIntero("Scelta: ", 0, elementi.size());
            return choice == 0 ? Optional.empty() : Optional.of(elementi.get(choice - 1));
        } catch (OperationCancelledException e) {
            return Optional.empty();
        }
    }

    @Override
    public <T> Optional<T> selezionaElementoConInfo(String prompt, List<T> elementi,
                                                    Function<T, String> infoMapper) {
        if (elementi.isEmpty()) {
            stampa("  (nessun elemento disponibile)");
            return Optional.empty();
        }

        stampa(prompt);
        for (int i = 0; i < elementi.size(); i++)
            stampa("  " + (i + 1) + ") " + elementi.get(i) +
                    "  [" + infoMapper.apply(elementi.get(i)) + "]");
        stampa("  0) Annulla");
        newLine();

        try {
            int choice = acquisisciIntero("Scelta: ", 0, elementi.size());
            return choice == 0 ? Optional.empty() : Optional.of(elementi.get(choice - 1));
        } catch (OperationCancelledException e) {
            return Optional.empty();
        }
    }
}
