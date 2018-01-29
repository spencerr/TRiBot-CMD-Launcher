package asm;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

public class InsnSearcher {
    private final ArrayList<JumpInsnNode> labels = new ArrayList<JumpInsnNode>();
    public int index = 0;
    private InsnList list;
    private AbstractInsnNode current;

    public InsnSearcher(MethodNode m) {
        this.list = m.instructions;
        this.current = list.getFirst();
    }

    public AbstractInsnNode getCurrent() {
        return current;
    }

    public void setCurrent(AbstractInsnNode in) {
        current = in;
    }

    public AbstractInsnNode getNext(int opcode) {

        while (current != null) {
            if (current.getOpcode() == opcode) {
                AbstractInsnNode old = current;
                current = current.getNext();
                return old;
            }
            current = current.getNext();
        }
        return null;
    }

    public AbstractInsnNode getNext() {
        if (current != null) {
            current = current.getNext();
            while (current != null && current.getOpcode() == -1) {
                current = current.getNext();
            }
        }
        return current;
    }

    public AbstractInsnNode getPrevious(int opcode) {
        while (current != null) {
            if (current.getOpcode() == opcode) {
                AbstractInsnNode old = current;
                current = current.getPrevious();
                return old;
            }
            current = current.getPrevious();
        }
        return null;
    }

    public AbstractInsnNode getPrevious() {
        current = current.getPrevious();
        while (current.getOpcode() == -1)
            current = current.getPrevious();
        return current;
    }

    public LdcInsnNode getNextLDC(Object cst) {
        AbstractInsnNode in;
        while ((in = getNext(Opcodes.LDC)) != null) {
            LdcInsnNode ln = (LdcInsnNode) in;
            if (ln.cst.equals(cst))
                return ln;


        }
        return null;
    }

    public LdcInsnNode getPreviousLDC(Object cst) {
        AbstractInsnNode in;
        while ((in = getPrevious(Opcodes.LDC)) != null) {
            LdcInsnNode ln = (LdcInsnNode) in;
            if (ln.cst.equals(cst))
                return ln;
        }
        return null;
    }

    public IntInsnNode getNextPush(int opcode, int value) {
        AbstractInsnNode in;
        while ((in = getNext(opcode)) != null) {
            IntInsnNode iin = (IntInsnNode) in;
            if (iin.operand == value)
                return iin;
        }
        return null;
    }

    public List<AbstractInsnNode> analyze(int opcode) {
        reset();
        List<AbstractInsnNode> list = new ArrayList<AbstractInsnNode>();
        AbstractInsnNode in;
        while ((in = getNext(opcode)) != null) {
            list.add(in);
        }
        return list;
    }

    public FieldInsnNode getNextField(int opcode, String desc, boolean eq) {
        AbstractInsnNode in;
        while ((in = getNext(opcode)) != null) {
            FieldInsnNode ln = (FieldInsnNode) in;
            if (eq ? ln.desc.equals(desc) : ln.desc.contains(desc))
                return ln;
        }
        return null;
    }


    public FieldInsnNode getPreviousField(int opcode, String desc, String owner, boolean eq) {
        AbstractInsnNode in;
        while ((in = getPrevious(opcode)) != null) {

            FieldInsnNode ln = (FieldInsnNode) in;
            if ((eq ? ln.desc.equals(desc) : ln.desc.contains(desc)) && ln.owner.equals(owner))
                return ln;
        }
        return null;
    }

    public MethodInsnNode getNextMethod(int opcode, String desc) {
        AbstractInsnNode in;
        while ((in = getNext(opcode)) != null) {
            MethodInsnNode ln = (MethodInsnNode) in;
            if (ln.desc.toLowerCase().contains(desc.toLowerCase()))
                return ln;
        }
        return null;
    }

    public MethodInsnNode getNextWildMethod(int opcode, Wildcard desc) {
        AbstractInsnNode in;
        while ((in = getNext(opcode)) != null) {
            MethodInsnNode ln = (MethodInsnNode) in;
            if (desc.matches(ln.desc))
                return ln;
        }
        return null;
    }

    public int getIndex() {
        return list.indexOf(current);
    }

    public void setIndex(int index) {
        current = list.get(index);
    }

    public void reset() {
        current = list.getFirst();
    }

    public boolean hasNext() {
        return getNext() != null;
    }
}