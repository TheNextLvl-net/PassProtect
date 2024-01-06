package net.thenextlvl.passprotect.api.dictionary;

import lombok.Getter;
import net.thenextlvl.core.api.errors.file.FileSaveException;
import net.thenextlvl.core.api.file.formats.TextFile;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Dictionary extends TextFile {

    @Getter
    @Nonnull
    private static final List<Dictionary> dictionaries = new ArrayList<>();

    static {
        File[] files = new File("/usr/share/dict").listFiles((file, name) -> !name.contains("."));
        if (files != null) for (File file : files) {
            new Dictionary(file) {
                @Override
                public void save() throws FileSaveException {
                }
            }.register();
        }
    }

    public Dictionary(String file) {
        super(file);
    }

    public Dictionary(String path, String file) {
        super(path, file);
    }

    public Dictionary(File file) {
        super(file);
    }

    @Nonnull
    public Dictionary register() {
        if (!dictionaries.contains(this)) dictionaries.add(this);
        return this;
    }

    @Nonnull
    public Dictionary unregister() {
        dictionaries.remove(this);
        return this;
    }

    @Nonnull
    public List<String> getWords() {
        return Arrays.asList(getContent());
    }

    public static boolean isWord(String word) {
        if (word.indexOf(' ') != -1) {
            String[] words = word.split(" ");
            for (String w : words) if (!isWord(w)) return false;
            return true;
        }
        for (Dictionary dictionary : dictionaries) if (dictionary.getWords().contains(word.toLowerCase())) return true;
        return false;
    }
}
