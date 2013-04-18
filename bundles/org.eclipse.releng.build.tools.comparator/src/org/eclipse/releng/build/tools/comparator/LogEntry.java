
package org.eclipse.releng.build.tools.comparator;

import java.util.ArrayList;
import java.util.List;

public class LogEntry {

    private String       name;
    private List<String> reasons = new ArrayList<String>();
    private List<String> info    = new ArrayList<String>();

    public LogEntry() {

    }

    public void addInfo(final String infoline) {
        info.add(infoline);
    }

    public void addReason(final String reason) {
        reasons.add(reason);
    }

    public List<String> getInfo() {
        return info;
    }

    public String getName() {
        return name;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setInfo(final List<String> info) {
        this.info = info;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setReasons(final List<String> reasons) {
        this.reasons = reasons;
    }
}
