package asm.controlflow.ArithmeticDeob;

import asm.controlflow.Generic.DeobFrame;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;

/*
 * @Author : Krazy_Meerkat
 */
public class ArithmeticDeob extends DeobFrame {

    public ArithmeticDeob(HashMap<String, ClassNode> classes) {
        super(classes);

    }

    @Override
    public HashMap<String, ClassNode> refactor() {
        //if (!Updater.useOutput)
        //    Deob.deobOutput.add("*   Starting Arithmetic Deob*"+System.getProperty("line.separator"));
        System.out.println("*   Starting Arithmetic Deob*");
        List<InsnWrapper> replace = getInstructions();
        for (InsnWrapper wrap : replace) {
            for (ClassNode node : classes.values()) {
                if (!wrap.owner.equals(node.name))
                    continue;
                for (MethodNode mn : (Iterable<MethodNode>) node.methods) {
                    if (mn.name.equals(wrap.mn.name) && mn.desc.equals(wrap.mn.desc)) {
                        int i = 0;
                        while (i < mn.instructions.size()) {
                            if (!wrap.insnArray.get(i).equals(null))
                                mn.instructions.set(mn.instructions.get(i), wrap.insnArray.get(i));
                            i++;
                        }
                    }
                }
            }
        }
        //if (!Updater.useOutput)
        //    Deob.deobOutput.add("*   Arithmetic Deob Finished*"+System.getProperty("line.separator"));
        System.out.println("*   Arithmetic Deob Finished*");
        return classes;
    }

    public static final <T> void swap(List<T> l, int i, int j) {
        Collections.swap(l, i, j);
    }

    public List<InsnWrapper> getInstructions() {
        Integer LDCIMULGETSTATIC = 0;
        Boolean FoundLDC = false;
        Boolean FoundLDCthenIMUL = false;
        Boolean FoundLDCthenIMULthenGETSTATIC = false;
        Integer GETSTATICIMULLDC = 0;
        Boolean FoundGETSTATIC = false;
        Boolean FoundGETSTATICthenIMUL = false;
        List<AbstractInsnNode> insnArray = new ArrayList<AbstractInsnNode>();
        List<InsnWrapper> theData = new ArrayList<InsnWrapper>();
        Iterator it = classes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            ClassNode node = (ClassNode) pairs.getValue();
            ListIterator<MethodNode> mnIt = node.methods.listIterator();
            while (mnIt.hasNext()) {
                MethodNode mn = mnIt.next();
                ListIterator<AbstractInsnNode> it2 = mn.instructions.iterator();
                int i = 0;
                insnArray = new ArrayList<AbstractInsnNode>();
                FoundLDCthenIMUL = false;
                FoundLDC = false;
                FoundLDCthenIMULthenGETSTATIC = false;
                FoundGETSTATICthenIMUL = false;
                FoundGETSTATIC = false;
                while (it2.hasNext()) {
                    AbstractInsnNode AbsInsn = it2.next();
                    insnArray.add(AbsInsn);
                    if (FoundLDCthenIMUL) {
                        if (AbsInsn.getOpcode() == Opcodes.GETSTATIC) {
                            FoundLDCthenIMULthenGETSTATIC = true; // LDC IMUL GETSTATIC
                        }
                        FoundLDCthenIMUL = false;
                        FoundLDC = false;
                    }
                    if (FoundLDC) {
                        if (AbsInsn.getOpcode() == Opcodes.IMUL) {
                            FoundLDCthenIMUL = true; // LDC IMUL
                        }
                        FoundLDC = false;
                    }
                    if (AbsInsn.getOpcode() == Opcodes.LDC) {
                        FoundLDC = true; // LDC
                    }
                    if (FoundGETSTATICthenIMUL) {
                        if (AbsInsn.getOpcode() == Opcodes.LDC) {
                            GETSTATICIMULLDC++; // Count GETSTATIC IMUL LDC
                        }
                        FoundGETSTATICthenIMUL = false;
                        FoundGETSTATIC = false;
                    }
                    if (FoundGETSTATIC) {
                        if (AbsInsn.getOpcode() == Opcodes.IMUL) {
                            FoundGETSTATICthenIMUL = true; // GETSTATIC IMUL
                        }
                        FoundGETSTATIC = false;
                    }
                    if (AbsInsn.getOpcode() == Opcodes.GETSTATIC) {
                        FoundGETSTATIC = true; // GETSTATIC
                    }
                    if (FoundLDCthenIMULthenGETSTATIC) {
                        swap(insnArray, i - 2, i);
                        LDCIMULGETSTATIC++;
                        FoundLDCthenIMULthenGETSTATIC = false;
                    }
                    i++;
                }
                theData.add(new InsnWrapper(mn, node.name, insnArray));
            }
        }
        //if (!Updater.useOutput)
        //    Deob.deobOutput.add("*      "+Integer.toString(LDCIMULGETSTATIC)+"/"+Integer.toString(GETSTATICIMULLDC+LDCIMULGETSTATIC)+" expressions modified*"+System.getProperty("line.separator"));
        System.out.println("*      " + Integer.toString(LDCIMULGETSTATIC) + "/" + Integer.toString(GETSTATICIMULLDC + LDCIMULGETSTATIC) + " expressions modified*");
        return theData;
    }
}