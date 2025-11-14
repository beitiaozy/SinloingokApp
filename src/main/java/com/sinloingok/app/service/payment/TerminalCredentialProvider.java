package com.sinloingok.app.service.payment;

import com.sinloingok.app.dao.TerminalCredentialMapper;
import com.sinloingok.app.models.payment.TerminalCredential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// com.sinloingok.app.service.payment.impl.TerminalCredentialProviderImpl
@Service
public class TerminalCredentialProvider {

    @Autowired
    private TerminalCredentialMapper mapper;

    public TerminalCredential getByOnlyCode(String onlyCode) {
        return mapper.selectByOnlyCode(onlyCode);
    }

    public TerminalCredential getBySiteAndAccount(Long netSiteId, Long accountId) {
        return mapper.selectBySiteAndAccount(netSiteId, accountId);
    }

    public List<TerminalCredential> listByAccount(Long accountId) {
        return mapper.listByAccount(accountId);
    }

    public void saveOrUpdate(TerminalCredential tc) {
        if (tc.getId() == null) {
            mapper.insert(tc);
        } else {
            mapper.updateById(tc);
        }
    }
}
