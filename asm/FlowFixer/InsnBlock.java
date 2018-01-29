package asm.FlowFixer;

import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.ArrayList;

/**
 * Created by Greg on 1/1/2015.
 */
public class InsnBlock {

    private ArrayList<AbstractInsnNode> instructions = new ArrayList<AbstractInsnNode>();
    private int label;

    public InsnBlock() {

    }

    public void setLabel(int i) {
        this.label = i;
    }

    public ArrayList<AbstractInsnNode> getInstructions() {
        return this.instructions;
    }

    public int getLabel() {
        return this.label;
    }

    public void addInstruction(AbstractInsnNode insn) {
        instructions.add(insn);
    }

    public int getInstructionCount() {
        return this.instructions.size();
    }
}
