package com.second.hand.trading.server.enums;


/**
 * @author myl
 * 错误信息枚举类
 */
public enum ErrorMsg {

    ACCOUNT_EXIT(400, "用户已存在"),
    ACCOUNT_Ban(400, "账号已被封禁"),
    ACCOUNT_NOT_EXIT(404, "用户不存在"),
    PASSWORD_IS_NOT_SAME(400, "密码不一致"),
    PASSWORD_RESET_ERROR(400, "修改密码失败"),
    EMAIL_SEND_ERROR(400, "邮件发送失败 请重试"),
    PARAM_ERROR(400, "参数错误"),
    SYSTEM_ERROR(500, "系统错误"),
    REGISTER_ERROR(400, "注册失败"),
    FILE_TYPE_ERROR(400, "文件类型错误 请选择.jpg或.png"),
    FILE_UPLOAD_ERROR(400, "文件上传失败"),
    FILE_NOT_EXIT(404, "文件不存在"),
    DATA_NOT_EXIST(404, "数据不存在"),
    FILE_DOWNLOAD_ERROR(400, "文件下载异常"),
    FILE_SIZE_ERROR(400, "文件过大"),
    OPERAT_FREQUENCY(400, "操作频繁 稍后重试"),
    MISSING_PARAMETER(400, "缺少参数"),
    COOKIE_ERROR(401, "请重新登录"),
    LOGIN_EXPIRED(401, "登录已过期 请重新登录"),
    EMAIL_LOGIN_ERROR(400, "登录失败 账号或密码错误"),
    JSON_READ_ERROR(400, "json参数解析错误"),
    FORM_NUMBER_ERROR(400, "表单id错误"),
    REPEAT_COMMIT_ERROR(400, "请勿重复提交"),
    COMMIT_FAIL_ERROR(400, "提交失败"),
    FAVORITE_EXIT(400, "收藏已存在");

    private String msg;
    private int errorCode;

    ErrorMsg(int errorCode, String msg) {
        this.errorCode = errorCode;
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
