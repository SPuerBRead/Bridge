package bridge.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class DnsLog implements Serializable {

    private static final long serialVersionUID = 1L;
    private String id;
    private String host;
    private Timestamp time;
    private String ip;
    private String type;
    private int logid;


    public DnsLog() {
        super();
    }

    public DnsLog(String id, String host, Timestamp time, String ip, String type, int logid) {
        super();
        this.id = id;
        this.host = host;
        this.time = time;
        this.ip = ip;
        this.type = type;
        this.logid = logid;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLogid() {
        return logid;
    }

    public void setLogid(int logid) {
        this.logid = logid;
    }
}
