package asm.deob;

import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public abstract class Transform {

    private int counter;
    private int total;
    private long exec;

    public void execute(List<ClassNode> elements) {
        long start = System.currentTimeMillis();
        transform(elements);
        exec = System.currentTimeMillis() - start;
    }

    public abstract void transform(List<ClassNode> elements);

    public String name() {
        return getClass().getSimpleName().substring(0, getClass().getSimpleName().indexOf("Transform"));
    }

    public int counter() {
        return counter;
    }

    public int total() {
        return total;
    }

    public abstract String result();

    public void add() {
        counter++;
        total++;
    }

    public void tAdd() {
        total++;
    }

    public long exec() {
        return exec;
    }
}