package bridge.service;

import bridge.mapper.DnslogMapper;
import bridge.model.DnsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class DnsLogService {

    @Autowired
    private DnslogMapper dnslogMapper;

    public void addDnsLog(DnsLog dnsLog) {
        dnslogMapper.insert(dnsLog);
    }

    public List getAllDnslog(String userDomain) {
        return dnslogMapper.getAll(userDomain);
    }

    public void delOneDnslog(String id) {
        dnslogMapper.deleteOneByID(id);
    }

    public void delAllDnslog(int logid) {
        dnslogMapper.deleteAllBylogID(logid);
    }

    public DnsLog getDnslogByID(String id) {
        return dnslogMapper.selectDnslogByID(id);
    }


    public List getDnsLogByHost(String host) {return dnslogMapper.selectDnsLogByHost(host);}
}
