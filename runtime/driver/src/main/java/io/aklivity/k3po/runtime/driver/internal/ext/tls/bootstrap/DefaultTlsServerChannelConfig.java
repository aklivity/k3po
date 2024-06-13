/*
 * Copyright 2024 Aklivity Inc.
 *
 * Aklivity licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.aklivity.k3po.runtime.driver.internal.ext.tls.bootstrap;

import java.io.File;

import javax.net.ssl.SSLParameters;

import io.aklivity.k3po.runtime.driver.internal.netty.bootstrap.channel.DefaultServerChannelConfig;

public class DefaultTlsServerChannelConfig extends DefaultServerChannelConfig implements TlsServerChannelConfig {

    private SSLParameters parameters;
    private File keyStoreFile;
    private char[] keyStorePassword;
    private File trustStoreFile;
    private char[] trustStorePassword;
    private String[] applicationProtocols;
    private boolean needClientAuth;
    private boolean wantClientAuth;
    private String[] cipherSuites;

    @Override
    public void setParameters(SSLParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public SSLParameters getParameters() {
        return parameters;
    }

    @Override
    public void setKeyStoreFile(
        File keyStoreFile)
    {
        this.keyStoreFile = keyStoreFile;
    }

    @Override
    public File getKeyStoreFile()
    {
        return keyStoreFile;
    }

    @Override
    public void setKeyStorePassword(
        char[] keyStorePassword)
    {
        this.keyStorePassword = keyStorePassword;
    }

    @Override
    public char[] getKeyStorePassword()
    {
        return keyStorePassword;
    }

    @Override
    public void setTrustStoreFile(
        File trustStoreFile)
    {
        this.trustStoreFile = trustStoreFile;
    }

    @Override
    public File getTrustStoreFile()
    {
        return trustStoreFile;
    }

    @Override
    public void setTrustStorePassword(
        char[] trustStorePassword)
    {
        this.trustStorePassword = trustStorePassword;
    }

    @Override
    public char[] getTrustStorePassword()
    {
        return trustStorePassword;
    }

    @Override
    public String[] getApplicationProtocols()
    {
        return applicationProtocols;
    }

    @Override
    public void setApplicationProtocols(
            String[] applicationProtocol)
    {
        this.applicationProtocols = applicationProtocol;
    }

    @Override
    public String[] getCipherSuites()
    {
        return cipherSuites;
    }

    @Override
    public void setCipherSuites(
            String[] cipherSuites)
    {
        this.cipherSuites = cipherSuites;
    }

    @Override
    protected boolean setOption0(
        String key,
        Object value)
    {
        switch (key) {
            case "keyStoreFile":
                keyStoreFile = new File((String) value);
                break;
            case "trustStoreFile":
                trustStoreFile = new File((String) value);
                break;
            case "keyStorePassword":
                keyStorePassword = ((String) value).toCharArray();
                break;
            case "trustStorePassword":
                trustStorePassword = ((String) value).toCharArray();
                break;
            case "applicationProtocols":
                applicationProtocols = ((String) value).split(",");
                break;
            case "needClientAuth":
                needClientAuth = Boolean.valueOf((String) value);
                break;
            case "wantClientAuth":
                wantClientAuth = Boolean.valueOf((String) value);
                break;
            case "cipherSuites":
                cipherSuites = ((String) value).split(",");
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public boolean getWantClientAuth() {
        return wantClientAuth;
    }

    @Override
    public boolean getNeedClientAuth() {
        return needClientAuth;
    }

}
