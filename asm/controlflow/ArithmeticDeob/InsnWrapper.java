package asm.controlflow.ArithmeticDeob;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

/**
 * @Author : Krazy_Meerkat
 */
public class InsnWrapper {
    public final String owner;
    public final List <AbstractInsnNode> insnArray;
    public final MethodNode mn;

    public InsnWrapper(MethodNode mn, String owner, List<AbstractInsnNode> insnArray) {
        this.mn = mn;
        this.owner = owner;
        this.insnArray = insnArray;
    }


}
