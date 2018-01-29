package asm.controlflow;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.Frame;

/**
 * Created by Greg on 1/2/2015.
 */
public class RemoveDeadCode {

    public static void removeDeadCode(ClassNode cn){
        org.objectweb.asm.tree.analysis.Analyzer asmAnalyzer = new org.objectweb.asm.tree.analysis.Analyzer(new BasicInterpreter());

        for(MethodNode mn : cn.methods){
            try{
                asmAnalyzer.analyze(cn.name, mn);

                Frame[] analyzerFrames = asmAnalyzer.getFrames();
                AbstractInsnNode[] ains = mn.instructions.toArray();

                for(int i = 0; i < analyzerFrames.length; i++) {
                    if(analyzerFrames[i] == null && !(ains[i] instanceof LabelNode)) {
                        mn.instructions.remove(ains[i]);
                        System.out.println("Removing dead code..");
                    }
                }

            }catch (AnalyzerException e){
                e.printStackTrace();
            }
        }
    }
}
