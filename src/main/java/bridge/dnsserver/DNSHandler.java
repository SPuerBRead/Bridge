package bridge.dnsserver;

import bridge.config.DnslogConfig;
import bridge.model.DnsLog;
import bridge.model.DnsRecordA;
import bridge.model.DnsRecordRebind;
import bridge.service.DnsLogService;
import bridge.service.DnsRecordAService;
import bridge.service.DnsRecordRebindService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.jws.Oneway;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class DNSHandler extends SimpleChannelInboundHandler<DatagramDnsQuery> {

    private String domain = DnslogConfig.dnslogDomain;

    @Autowired
    private DnsLogService dnsLogService;

    private static Map<String, byte[]> questionDomainMap = Maps.newConcurrentMap();

    private static Map<String, List<String>> rebindRecordMap = Maps.newConcurrentMap();

    @Autowired
    private DnsRecordAService dnsRecordAService;

    @Autowired
    private DnsRecordRebindService dnsRecordRebindService;


    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramDnsQuery query) throws Exception {
        ByteBuf answerIP;
        int logID;
        DatagramDnsResponse response = new DatagramDnsResponse(query.recipient(), query.sender(), query.id());
        DefaultDnsQuestion dnsQuestion = query.recordAt(DnsSection.QUESTION);
        query.sender().getHostName();
        String connectIP = query.sender().getAddress().toString();
        String domainRegex = "\\.\\d+\\." + domain.replace(".", "\\.") + "\\.$";
        String subDomain = dnsQuestion.name().replaceAll(domainRegex, "");
        String[] hd = dnsQuestion.name().replace('.' + domain + '.', "").split("\\.");
        try {
            logID = Integer.parseInt(hd[hd.length - 1]);
        } catch (NumberFormatException n) {
            return;
        }
        String ipRegex = "^((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}$";
        String rebindRegex = "^((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}$";
        if (Pattern.compile(rebindRegex).matcher(subDomain).matches()) {
            String[] s = subDomain.split("\\.");
            byte[] ip = new byte[4];
            byte[] rebindIP = new byte[4];
            for (int i = 0; i < s.length; i++) {
                if (i < 4) {
                    ip[i] = (byte) Integer.parseInt(s[i]);
                } else {
                    rebindIP[i - 4] = (byte) Integer.parseInt(s[i]);
                }
            }
            if (questionDomainMap.containsKey(dnsQuestion.name())) {
                answerIP = Unpooled.wrappedBuffer(questionDomainMap.get(dnsQuestion.name()));
                questionDomainMap.remove(dnsQuestion.name());
            } else {
                answerIP = Unpooled.wrappedBuffer(ip);
                questionDomainMap.put(dnsQuestion.name(), rebindIP);
            }
        } else if (Pattern.compile(ipRegex).matcher(subDomain).matches()) {
            String[] s = subDomain.split("\\.");
            byte[] ip = new byte[s.length];
            for (int i = 0; i < s.length; i++) {
                ip[i] = (byte) Integer.parseInt(s[i]);
            }
            answerIP = Unpooled.wrappedBuffer(ip);
        }else if(dnsRecordAService.getDnsRecordABySubdomain(dnsQuestion.name().substring(0,dnsQuestion.name().length()-1)) != null){
            DnsRecordA dnsRecordA = dnsRecordAService.getDnsRecordABySubdomain(dnsQuestion.name().substring(0,dnsQuestion.name().length()-1));
            List<Byte> byteIP = stringIP2ByteArrayIP(dnsRecordA.getIp());
            answerIP = Unpooled.wrappedBuffer(new byte[]{byteIP.get(0), byteIP.get(1), byteIP.get(2), byteIP.get(3)});
        }else if(dnsRecordRebindService.getDnsRecordRebindBySubdomain(dnsQuestion.name().substring(0,dnsQuestion.name().length()-1)) != null){
            DnsRecordRebind dnsRecordRebind = dnsRecordRebindService.getDnsRecordRebindBySubdomain(dnsQuestion.name().substring(0,dnsQuestion.name().length()-1));
            String rebindSubDomain = dnsRecordRebind.getSubdomain();
            if(rebindRecordMap.containsKey(rebindSubDomain)){
                String matchIP = isInIPC(connectIP.split("/")[1], rebindRecordMap.get(rebindSubDomain));
                 if(!matchIP.equals("")){
                     List<Byte> byteIP = stringIP2ByteArrayIP(dnsRecordRebind.getIp2());
                     answerIP = Unpooled.wrappedBuffer(new byte[]{byteIP.get(0), byteIP.get(1), byteIP.get(2), byteIP.get(3)});
                     rebindRecordMap.get(rebindSubDomain).remove(matchIP);
                 }else{
                     List<Byte> byteIP = stringIP2ByteArrayIP(dnsRecordRebind.getIp1());
                     answerIP = Unpooled.wrappedBuffer(new byte[]{byteIP.get(0), byteIP.get(1), byteIP.get(2), byteIP.get(3)});
                     rebindRecordMap.get(rebindSubDomain).add(connectIP.split("/")[1]);
                 }

            }else{
                List<String> tmpList = new ArrayList<>();
                System.out.println(connectIP);
                tmpList.add(connectIP.split("/")[1]);
                rebindRecordMap.put(rebindSubDomain, tmpList);
                List<Byte> byteIP = stringIP2ByteArrayIP(dnsRecordRebind.getIp1());
                answerIP = Unpooled.wrappedBuffer(new byte[]{byteIP.get(0), byteIP.get(1), byteIP.get(2), byteIP.get(3)});
            }
            System.out.println(rebindRecordMap);
        } else {
            List<Byte> byteIP = stringIP2ByteArrayIP(DnslogConfig.ip);
            answerIP = Unpooled.wrappedBuffer(new byte[]{byteIP.get(0), byteIP.get(1), byteIP.get(2), byteIP.get(3)});
        }
        response.addRecord(DnsSection.QUESTION, dnsQuestion);
        DefaultDnsRawRecord queryAnswer = new DefaultDnsRawRecord(dnsQuestion.name(), DnsRecordType.A, 0, answerIP);
        response.addRecord(DnsSection.ANSWER, queryAnswer);
        ctx.writeAndFlush(response);
        String address = getIPAdderssInfo(connectIP.split("/")[1]);
        String connectAddress = connectIP+' '+address;
        if(!dnsQuestion.name().replaceAll("\\d+\\." + domain.replace(".", "\\.") + "\\.$", "").equals("")){
            DnsLog dnslog = new DnsLog(UUID.randomUUID().toString(), dnsQuestion.name().substring(0, dnsQuestion.name().length() - 1), new Timestamp(System.currentTimeMillis()), connectAddress, dnsQuestion.type().toString(), logID);
            dnsLogService.addDnsLog(dnslog);
            getIPAdderssInfo(connectIP.split("/")[1]);
        }
    }


    private List<Byte> stringIP2ByteArrayIP(String ip) {
        return Arrays.asList(ip.split("\\.")).stream().map(x -> (byte) Integer.parseInt(x)).collect(Collectors.toList());
    }

    private String getIPAdderssInfo(String ip) throws IOException {
        String address = "";
        String requestURL = "http://ip-api.com/json/"+ip+"?lang=zh-CN";
        RestTemplate restTemplate = new RestTemplate();
        try{
            String responseText = restTemplate.getForEntity(requestURL,String.class).getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            Map responseTextMap = objectMapper.readValue(responseText,Map.class);
            if(responseTextMap.containsKey("status") && responseTextMap.get("status").equals("success")){
                if(responseTextMap.containsKey("country")){
                    address+=responseTextMap.get("country");
                }else{
                    address+="unknown";
                }
                if(responseTextMap.containsKey("city")){
                    address+="/"+responseTextMap.get("city");
                }else {
                    address+="/unknown";
                }
            }
        }catch (HttpServerErrorException e){
            return "";
        }
        return address;
    }

    private String isInIPC(String thisIP, List<String> ipList){
        String[] thisIPList = thisIP.split("\\.");
        for(String ip : ipList) {
            String[] cIpList = ip.split("\\.");
            if(thisIPList[0].equals(cIpList[0]) && thisIPList[1].equals(cIpList[1]) && thisIPList[2].equals(cIpList[2])){
                return ip;
            }
        }
        return "";
    }


}