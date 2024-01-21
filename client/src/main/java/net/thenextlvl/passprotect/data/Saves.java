package net.thenextlvl.passprotect.data;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.TreeSet;

@Getter
@Setter
@Accessors(fluent = true)
public class Saves {
    private static final TreeSet<Category> categories = new TreeSet<>();
}
