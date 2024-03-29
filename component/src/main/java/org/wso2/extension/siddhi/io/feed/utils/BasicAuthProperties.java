package org.wso2.extension.siddhi.io.feed.utils;

/**
 * This class keeps basic auth username, password etc.
 */
public class BasicAuthProperties {
    private String userName;
    private String userPass;
    private boolean isEnable = false;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPass() {
        return userPass;
    }

    public void setUserPass(String userPass) {
        this.userPass = userPass;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }
}
