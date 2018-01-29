package asm.controlflow.Generic;

import org.objectweb.asm.tree.ClassNode;

import java.util.HashMap;

/*
 * @Author : NKN
 */
public abstract class DeobFrame {

    public  HashMap<String, ClassNode> classes;

    public DeobFrame(HashMap<String,ClassNode> classes){
        this.classes = classes;
    }



    public abstract HashMap<String,ClassNode> refactor();




}
