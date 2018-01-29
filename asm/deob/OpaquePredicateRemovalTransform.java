package asm.deob;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.LinkedList;
import java.util.List;

public class OpaquePredicateRemovalTransform extends Transform {

    public static int parameterCount(MethodNode mn) {
        String desc = mn.desc.split("(|)")[1];
        boolean inDesc = false;
        int count = 0;
        for (char c : desc.toCharArray()) {
            if (inDesc) {
                if (c == ';') {
                    inDesc = false;
                    count++;
                }
                continue;
            }
            switch (c) {
                case 'L':
                    inDesc = true;
                    break;
                default:
                    count++;
                    break;
            }
        }
        return count;
    }

    public ClassNode[] transform(ClassNode[] elements) {
        List<AbstractInsnNode> remove = new LinkedList<AbstractInsnNode>();
        for (ClassNode element : elements) {
            for (MethodNode method : element.methods) {
                switch (method.desc.charAt(method.desc.indexOf(')') - 1)) {
                    case 'I':
                    case 'S':
                    case 'B':
                        int var = parameterCount(method);
                        for (AbstractInsnNode node : method.instructions.toArray()) {
                            if (node.getOpcode() == Opcodes.ILOAD) {
                                remove.clear();
                                VarInsnNode varInsn = (VarInsnNode) node;
                                remove.add(varInsn);
                                if (varInsn.var == var) {
                                    if (varInsn.getNext() != null &&
                                            varInsn.getNext().getType() == AbstractInsnNode.JUMP_INSN &&
                                            varInsn.getNext().getOpcode() != Opcodes.GOTO) {
                                        method.instructions.insert(
                                                varInsn.getNext(),
                                                new JumpInsnNode(
                                                        Opcodes.GOTO, ((JumpInsnNode) varInsn.getNext()).label));
                                        remove.add(varInsn.getNext());
                                        remove.add(varInsn.getPrevious());
                                    } else if (varInsn.getNext() != null &&
                                            varInsn.getNext().getNext() != null &&
                                            varInsn.getNext().getNext().getType() == AbstractInsnNode.JUMP_INSN &&
                                            varInsn.getNext().getNext().getOpcode() != Opcodes.GOTO) {
                                        method.instructions.insert(
                                                varInsn.getNext(),
                                                new JumpInsnNode(
                                                        Opcodes.GOTO, ((JumpInsnNode) varInsn.getNext().getNext()).label));
                                        remove.add(varInsn.getNext());
                                        remove.add(varInsn.getNext());
                                    }
                                }
                                for (ClassNode element_ : elements) {
                                    List<AbstractInsnNode> remove_ = new LinkedList<AbstractInsnNode>();
                                    for (MethodNode method_ : element_.methods) {
                                        if (method_.instructions.size() <= 0) {
                                            continue;
                                        }
                                        remove_.clear();
                                        for (AbstractInsnNode ain : method_.instructions.toArray()) {
                                            if (ain instanceof MethodInsnNode) {
                                                MethodInsnNode min = (MethodInsnNode) ain;
                                                if (min.owner.equals(element.name) &&
                                                        min.name.equals(method.name) &&
                                                        min.desc.equals(method.desc) &&
                                                        min.getPrevious() != null &&
                                                        (min.getPrevious().getOpcode() == Opcodes.LDC ||
                                                                min.getPrevious().getOpcode() == Opcodes.BIPUSH ||
                                                                min.getPrevious().getOpcode() == Opcodes.SIPUSH)) {
                                                    remove_.add(min.getPrevious());
                                                }
                                            }
                                        }
                                        if (remove_.size() > 0 && remove.size() == 3) {
                                            for (AbstractInsnNode ain : remove) {
                                                if (ain == null) {
                                                    continue;
                                                }
                                                method.instructions.remove(ain);
                                            }
                                            for (AbstractInsnNode ain : remove_) {
                                                method_.instructions.remove(ain);
                                            }
                                        }
                                        add();
                                    }
                                }
                            }
                        }
                        break;
                }
            }
        }
        return elements;
    }

    @Override
    public String result() {
        StringBuilder builder = new StringBuilder("\t\t\t↔ Executed ");
        builder.append(name()).append(" in ").append(exec()).append("ms\n\t\t\t\tRemoved ")
                .append(counter()).append(" dead tree leaves");
        return builder.toString();
    }

	@Override
	public void transform(List<ClassNode> elements) {
		transform(elements.toArray(new ClassNode[]{}));
	}
	public void transform() {
	}
}