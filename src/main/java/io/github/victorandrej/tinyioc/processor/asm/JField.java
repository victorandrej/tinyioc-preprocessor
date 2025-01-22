package io.github.victorandrej.tinyioc.processor.asm;

public class JField {
    private String name;
    private String type;
    private  int modifier;


    public JField(String name,int modifier, String type) {
        this.name = name;
        this.modifier = modifier;
        this.type = type;
    }

    public int getModifier() {
        return modifier;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
