package asm.controlflow.RedundantMethod;

import asm.controlflow.Generic.DeobFrame;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.*;


public class MethodRemoval extends DeobFrame {


    public MethodRemoval(HashMap<String, ClassNode> classes){
        super(classes);

    }
    @Override
    public HashMap<String,ClassNode> refactor(){
        List<MethodWrapper>remove = getRedundantMethods();
        List<MethodNode> toRemove = new ArrayList<MethodNode>();
        for(MethodWrapper wrap : remove){
            for(ClassNode node : classes.values()){
                if(!wrap.owner.equals(node.name))
                    continue;
                for (MethodNode mn : (Iterable<MethodNode>) node.methods) {
                    if(mn.name.equals(wrap.name) && mn.desc.equals(wrap.desc))
                        toRemove.add(mn);
                }
                for(MethodNode mn : toRemove)
                    node.methods.remove(mn);
            }
        }
        return classes;
    }


    private void check(MethodWrapper wrap, List<MethodWrapper> used){
        if(!wrap.owner.contains("java") && !used.contains(wrap)){
            used.add(wrap);
        }
    }
    private boolean hasMethod(ClassNode node, String name, String desc){
        ListIterator<MethodNode> mnIt = node.methods.listIterator();
        while(mnIt.hasNext()){
            MethodNode mn = mnIt.next();
            if(mn.name.equals(name) && mn.desc.equals(desc)){
                return true;
            }

        }
        return false;
    }

    public List<MethodWrapper> getRedundantMethods(){
        List<MethodWrapper> used = new ArrayList<MethodWrapper>();
        List<MethodWrapper> all = new ArrayList<MethodWrapper>();
        for(ClassNode node : classes.values()){
            Iterator<MethodNode> mnIt;
            mnIt = node.methods.listIterator();
            while(mnIt.hasNext()) {
                MethodNode mn = mnIt.next();
                MethodWrapper wrap = new MethodWrapper(node.name, mn.name, mn.desc);
                if (!all.contains(wrap))
                    all.add(wrap);

                if (wrap.name.contains("init"))
                    check(wrap, used);
                if (Modifier.isAbstract(mn.access))
                    check(wrap, used);
                String classSuper = node.superName;
                while (!classSuper.equals("java/lang/Object")) {
                    ClassNode superNode;
                    if (classSuper.contains("java")) {
                        superNode = new ClassNode();
                        try {
                            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                            node.accept(cw);
                            byte[] b = cw.toByteArray();
                            ClassReader cr = new ClassReader(b);
                            cr.accept(superNode, 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        superNode = classes.get(classSuper);
                    }
                    if (hasMethod(superNode, mn.name, mn.desc)) {
                        check(new MethodWrapper(classSuper, wrap.name, wrap.desc), used);
                        check(wrap, used);
                        break;
                    }
                    classSuper = superNode.superName;

                }

                Iterator<AbstractInsnNode> instIt = mn.instructions.iterator();
                while (instIt.hasNext()) {
                    AbstractInsnNode insn = (AbstractInsnNode) instIt.next();
                    if (insn instanceof MethodInsnNode) {
                        MethodInsnNode mns = (MethodInsnNode) insn;
                        if (!mns.owner.contains("java") && !mns.name.contains("init")) {
                            if (hasMethod(classes.get(mns.owner), mns.name, mns.desc)) {
                                check(new MethodWrapper(mns.owner, mns.name, mns.desc), used);
                            } else {
                                classSuper = classes.get(mns.owner).superName;
                                while (!classSuper.contains("java")) {
                                    ClassNode cn = classes.get(classSuper);
                                    if (hasMethod(cn, mns.name, mns.desc)) {
                                        check(new MethodWrapper(cn.name, mns.name, mns.desc), used);
                                        break;
                                    }
                                    classSuper = cn.superName;

                                }
                            }

                        }
                    }
                }
            }
            if(node.interfaces.size() > 0){
                Iterator<String> itIt = node.interfaces.listIterator();
                while(itIt.hasNext()){
                    String name = (String)itIt.next();
                    ClassNode itNode;
                    if(name.contains("java")){
                        itNode = new ClassNode();
                        try {
                            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                            node.accept(cw);
                            byte[] b = cw.toByteArray();
                            ClassReader cr = new ClassReader(b);
                            cr.accept(itNode,0);
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    } else {
                        itNode = classes.get(name);
                    }


                    mnIt = itNode.methods.listIterator();
                    while(mnIt.hasNext()){
                        MethodNode mn = mnIt.next();
                        check(new MethodWrapper(node.name, mn.name, mn.desc), used);
                    }

                }
            }
            try{Thread.sleep(1);}catch(Exception e){}
        }
        List<MethodWrapper> toRemove = new ArrayList<MethodWrapper>();
        for(MethodWrapper wrap : all){
            if(!used.contains(wrap)) {

                toRemove.add(wrap);
            }
        }
        System.out.println("Removed "+Integer.toString(toRemove.size())+"/"+Integer.toString(all.size())+" redundant methods!");
        return toRemove;
    }



}