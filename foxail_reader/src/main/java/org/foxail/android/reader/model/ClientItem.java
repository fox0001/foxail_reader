package org.foxail.android.reader.model;

import org.foxail.android.reader.client.ClientSource;

public class ClientItem {
    private ClientSource clientSource;
    private String name;

    public ClientItem(ClientSource clientSource, String name) {
        this.clientSource = clientSource;
        this.name = name;
    }

    public ClientSource getClientSource() {
        return clientSource;
    }

    public String getName() {
        return name;
    }
}