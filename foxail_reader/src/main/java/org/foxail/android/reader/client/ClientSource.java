package org.foxail.android.reader.client;

/**
 * 客户端来源
 */
public enum ClientSource {
    cnbeta(CnBetaClient.class)
    ,oschina(OsChinaClient.class)
    ;

    private Class clientClass;

    private ClientSource(Class clientClass){
        this.clientClass = clientClass;
    }

    public Class getClientClass() {
        return clientClass;
    }
}
