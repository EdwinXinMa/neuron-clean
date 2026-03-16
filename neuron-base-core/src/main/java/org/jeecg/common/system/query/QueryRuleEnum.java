package org.jeecg.common.system.query;

import org.jeecg.common.util.oConvertUtils;

public enum QueryRuleEnum {
    GT(">","gt","gt"),
    GE(">=","ge","ge"),
    LT("<","lt","lt"),
    LE("<=","le","le"),
    EQ("=","eq","eq"),
    NE("!=","ne","ne"),
    IN("IN","in","in"),
    LIKE("LIKE","like","like"),
    NOT_LIKE("NOT_LIKE","not_like","not_like"),
    LEFT_LIKE("LEFT_LIKE","left_like","left_like"),
    RIGHT_LIKE("RIGHT_LIKE","right_like","right_like"),
    EQ_WITH_ADD("EQWITHADD","eq_with_add","eq_with_add"),
    LIKE_WITH_AND("LIKEWITHAND","like_with_and","like_with_and"),
    LIKE_WITH_OR("LIKEWITHOR","like_with_or","like_with_or"),
    SQL_RULES("USE_SQL_RULES","ext","ext"),
    LINKAGE("LINKAGE","linkage","linkage"),
    NOT_LEFT_LIKE("NOT_LEFT_LIKE","not_left_like","not_left_like"),
    NOT_RIGHT_LIKE("NOT_RIGHT_LIKE","not_right_like","not_right_like"),
    EMPTY("EMPTY","empty","empty"),
    NOT_EMPTY("NOT_EMPTY","not_empty","not_empty"),
    NOT_IN("NOT_IN","not_in","not_in"),
    ELE_MATCH("ELE_MATCH","elemMatch","elemMatch"),
    ELE_NOT_MATCH("ELE_NOT_MATCH","elemNotMatch","elemNotMatch"),
    RANGE("RANGE","range","range"),
    NOT_RANGE("NOT_RANGE","not_range","not_range"),
    CUSTOM_MONGODB("CUSTOM_MONGODB","custom_mongodb","custom_mongodb");

    private String value;
    private String condition;
    private String msg;

    QueryRuleEnum(String value, String condition, String msg) {
        this.value = value;
        this.condition = condition;
        this.msg = msg;
    }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public static QueryRuleEnum getByValue(String value) {
        if (oConvertUtils.isEmpty(value)) {
            return null;
        }
        for (QueryRuleEnum val : values()) {
            if (val.getValue().equals(value) || val.getCondition().equalsIgnoreCase(value)) {
                return val;
            }
        }
        return null;
    }
}
