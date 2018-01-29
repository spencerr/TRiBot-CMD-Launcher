package asm.controlflow.RedundantMethod;

/**
 * @Author : NKN
 */
public class MethodWrapper {
    public final String owner;
    public final String name;
    public final String desc;

    public MethodWrapper(String owner, String name, String desc) {
        this.owner = owner;
        this.name = name;
        this.desc = desc;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MethodWrapper) {
            MethodWrapper info = (MethodWrapper) obj;
            return owner.equals(info.owner) && name.equals(info.name) && desc.equals(info.desc);
        }
        return false;
    }


}
