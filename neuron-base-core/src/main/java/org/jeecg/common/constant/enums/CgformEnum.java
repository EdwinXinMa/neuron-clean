package org.jeecg.common.constant.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Compatibility shim for hibernate-re jar.
 * Must be an exact copy because enums cannot be extended.
 */
public enum CgformEnum {

    ONE(1, "one", "/jeecg/code-template-online", "default.one", "classic", new String[]{"vue3","vue","vue3Native"}),
    MANY(2, "many", "/jeecg/code-template-online", "default.onetomany", "classic", new String[]{"vue"}),
    JVXE_TABLE(2, "jvxe", "/jeecg/code-template-online", "jvxe.onetomany", "default", new String[]{"vue3","vue","vue3Native"}),
    ERP(2, "erp", "/jeecg/code-template-online", "erp.onetomany", "ERP", new String[]{"vue3","vue","vue3Native"}),
    INNER_TABLE(2, "innerTable", "/jeecg/code-template-online", "inner-table.onetomany", "innerTable", new String[]{"vue3","vue"}),
    TAB(2, "tab", "/jeecg/code-template-online", "tab.onetomany", "Tab", new String[]{"vue3","vue"}),
    TREE(3, "tree", "/jeecg/code-template-online", "default.tree", "tree", new String[]{"vue3","vue","vue3Native"});

    int type;
    String code;
    String templatePath;
    String stylePath;
    String note;
    String[] vueStyle;

    CgformEnum(int type, String code, String templatePath, String stylePath, String note, String[] vueStyle) {
        this.type = type;
        this.code = code;
        this.templatePath = templatePath;
        this.stylePath = stylePath;
        this.note = note;
        this.vueStyle = vueStyle;
    }

    public static String getTemplatePathByConfig(String code) {
        return getCgformEnumByConfig(code).templatePath;
    }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }
    public String getTemplatePath() { return templatePath; }
    public void setTemplatePath(String templatePath) { this.templatePath = templatePath; }
    public String getStylePath() { return stylePath; }
    public void setStylePath(String stylePath) { this.stylePath = stylePath; }
    public String[] getVueStyle() { return vueStyle; }
    public void setVueStyle(String[] vueStyle) { this.vueStyle = vueStyle; }

    public static CgformEnum getCgformEnumByConfig(String code) {
        for (CgformEnum e : CgformEnum.values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }

    public static List<Map<String, Object>> getJspModelList(int type) {
        List<Map<String, Object>> ls = new ArrayList<>();
        for (CgformEnum e : CgformEnum.values()) {
            if (e.type == type) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", e.code);
                map.put("note", e.note);
                ls.add(map);
            }
        }
        return ls;
    }
}
