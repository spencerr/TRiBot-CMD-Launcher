package asm.FlowFixer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Iterator;

/**
 * Created by Greg on 1/1/2015.
 */
public class FlowFixer {

    private MethodNode mn;

    public FlowFixer(MethodNode mn) {
        this.mn = mn;
    }

    public void correctMethodGraph() {
        for (Iterator<AbstractInsnNode> iterator = mn.instructions.iterator(); iterator.hasNext(); ) {
            AbstractInsnNode ain = (AbstractInsnNode) iterator.next();
            InsnBlock block = new InsnBlock();

            if (ain.getOpcode() != Opcodes.GOTO) {
                block.addInstruction(ain);
            } else {

            }
        }
    }
}
