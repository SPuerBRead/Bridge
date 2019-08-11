package bridge.service;

import bridge.mapper.DnsRecordRebindMapper;
import bridge.model.DnsRecordRebind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DnsRecordRebindService {

    @Autowired
    private DnsRecordRebindMapper dnsRecordRebindMapper;

    public void addDnsRecordRebind(DnsRecordRebind dnsRecordRebind) {
        dnsRecordRebindMapper.insert(dnsRecordRebind);
    }

    public DnsRecordRebind getDnsRecordRebindBySubdomain(String subdomain) {
        return dnsRecordRebindMapper.selectDnsRecordRebindBySubdomain(subdomain);
    }

    public List getAllDnsRecordRebind(String userDomain) {
        return dnsRecordRebindMapper.getAll(userDomain);
    }

    public DnsRecordRebind getDnsRecordRebindByID(String id) {
        return dnsRecordRebindMapper.selectDnsRecordRebindByID(id);
    }

    public void delOneDnsRecordRebind(String id) {
        dnsRecordRebindMapper.deleteOneByID(id);
    }

    public void delAllDnsRecordRebind(int logid) {
        dnsRecordRebindMapper.deleteAllDnsRecordRebindBylogID(logid);
    }

    public void updateDnsRecordRebindByID(DnsRecordRebind dnsRecordRebind) {
        dnsRecordRebindMapper.updateByID(dnsRecordRebind);
    }
}
